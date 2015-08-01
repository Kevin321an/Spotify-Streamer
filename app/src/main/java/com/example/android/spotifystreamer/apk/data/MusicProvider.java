/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.spotifystreamer.apk.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class MusicProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MusicDbHelper mOpenHelper;

    static final int ARTIST = 100;
    static final int TRACKS_WITH_ID = 101;
    static final int ARTIST_WITH_NAME = 102;
    static final int TRACKS = 300;

    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

    static{
        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
        
        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
        sWeatherByLocationSettingQueryBuilder.setTables(
                MusicContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        MusicContract.LocationEntry.TABLE_NAME +
                        " ON " + MusicContract.WeatherEntry.TABLE_NAME +
                        "." + MusicContract.WeatherEntry.COLUMN_LOC_KEY +
                        " = " + MusicContract.LocationEntry.TABLE_NAME +
                        "." + MusicContract.LocationEntry._ID);
    }

    //location.location_setting = ?
    private static final String sTrackIDSelection =
            MusicContract.LocationEntry.TABLE_NAME+
                    "." + MusicContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? ";

    /*//location.location_setting = ? AND date >= ?
    private static final String sLocationSettingWithStartDateSelection =
            MusicContract.LocationEntry.TABLE_NAME+
                    "." + MusicContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " +
                    MusicContract.WeatherEntry.COLUMN_DATE + " >= ? ";
                    */

    //location.location_setting = ? AND date = ?
    private static final String sLocationSettingAndDaySelection =
            MusicContract.WeatherEntry.TABLE_NAME+
                    "." + MusicContract.WeatherEntry.ARTIST_NAME + " = ? ";

    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = MusicContract.WeatherEntry.getLocationSettingFromUri(uri);


        String[] selectionArgs;
        String selection;
        selection = sTrackIDSelection;
        selectionArgs = new String[]{locationSetting};


        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getAritistList(
            Uri uri, String[] projection, String sortOrder) {
        //String locationSetting = MusicContract.WeatherEntry.getLocationSettingFromUri(uri);

        String[] selectionArgs=new String[]{};

        return sWeatherByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sLocationSettingAndDaySelection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    /*
       Here is where you need to create the UriMatcher. This UriMatcher will
        match each URI to the WEATHER, WEATHER_WITH_LOCATION, WEATHER_WITH_LOCATION_AND_DATE,
        and LOCATION integer constants defined above.  You can test this by uncommenting the
        testUriMatcher test within TestUriMatcher.
     */
    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MusicContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, MusicContract.PATH_WEATHER, ARTIST);
        matcher.addURI(authority, MusicContract.PATH_WEATHER + "/*", TRACKS_WITH_ID);
        matcher.addURI(authority, MusicContract.PATH_WEATHER + "/*/#", ARTIST_WITH_NAME);

        matcher.addURI(authority, MusicContract.PATH_LOCATION, TRACKS);
        return matcher;
    }

    /*
        We just create a new MusicDbHelper for later use here.
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new MusicDbHelper(getContext());
        return true;
    }

    /*
        the getType function that uses the UriMatcher.  You can test this by uncommenting testGetType in TestProvider.
     */
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case ARTIST_WITH_NAME:
                return MusicContract.WeatherEntry.CONTENT_ITEM_TYPE;
            case TRACKS_WITH_ID:
                return MusicContract.WeatherEntry.CONTENT_TYPE;
            case ARTIST:
                return MusicContract.WeatherEntry.CONTENT_TYPE;
            case TRACKS:
                return MusicContract.LocationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "weather/*/*"
            case ARTIST_WITH_NAME:
            {
                retCursor = getAritistList(uri, projection, sortOrder);
                break;
            }
            // "weather/*"
            case TRACKS_WITH_ID: {
                retCursor = getWeatherByLocationSetting(uri, projection, sortOrder);
                break;
            }
            // "weather"
            case ARTIST: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MusicContract.WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "location"
            case TRACKS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MusicContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
        the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case ARTIST: {
                //normalizeDate(values);
                long _id = db.insert(MusicContract.WeatherEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MusicContract.WeatherEntry.buildWeatherUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRACKS: {
                long _id = db.insert(MusicContract.LocationEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MusicContract.LocationEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case ARTIST:
                rowsDeleted = db.delete(
                        MusicContract.WeatherEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRACKS:
                rowsDeleted = db.delete(
                        MusicContract.LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }


    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case ARTIST:
                rowsUpdated = db.update(MusicContract.WeatherEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case TRACKS:
                rowsUpdated = db.update(MusicContract.LocationEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ARTIST:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MusicContract.WeatherEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // This is a method specifically to assist the testing
    // framework in running smoothly.
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}