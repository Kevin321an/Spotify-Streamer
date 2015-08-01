package com.example.android.spotifystreamer.apk;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.github.ksoichiro.android.observablescrollview.Scrollable;
import com.nineoldandroids.view.ViewHelper;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment <S extends Scrollable>extends Fragment implements ObservableScrollViewCallbacks {
    static final String DETAIL_URI="URI";
    private MusicData music;
    private String musicID;
    private MainAdapter mDetailAdapter;
    private String artist,imageLarge;


    protected View mHeader;
    protected int mFlexibleSpaceImageHeight;
    protected View mHeaderBar;
    protected View mListBackgroundView;
    protected int mActionBarSize;
    protected int mIntersectionHeight;

    private View mImage;
    private View mHeaderBackground;
    private int mPrevScrollY;
    private boolean mGapIsChanging;
    private boolean mGapHidden;
    private boolean mReady;
    private View detailRootView;
    private ObservableListView listView;
    private static String oldMusciId="";//store old MUSIC ID
    private ArrayList<MusicData> mListInstanceState;
    private static final String SAVE_PAGE_KEY = "save_page";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mListInstanceState = savedInstanceState.getParcelableArrayList(SAVE_PAGE_KEY);
        } else {
            mListInstanceState = new ArrayList<MusicData>();
        }

        //get intent from MainActivity
        Bundle arguments =getArguments();
        if(arguments!=null){
            music=arguments.getParcelable(DetailActivityFragment.DETAIL_URI);
        }
        detailRootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Intent intent = getActivity().getIntent();

        if (intent != null && intent.hasExtra("Object")) {

            music = (MusicData)intent.getExtras().getParcelable("Object");

        }
        if (music!=null){
            musicID = music.id.id;
            artist=music.artist;
            imageLarge=music.image1000;
            //fetch the artist pic in detail activity
                if (imageLarge != null) {
                    try {
                        Picasso.with(getActivity().getBaseContext()).load(imageLarge).into((ImageView) detailRootView.findViewById(R.id.image));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {
                    Picasso.with(getActivity().getBaseContext()).load(R.drawable.example).into((ImageView) detailRootView.findViewById(R.id.image));
                }
            fillGapViewDisply();
            FetchTrackTask trackTask = new FetchTrackTask();
            //if oldID same with new id, it is no need to execute the HTTP checking
            if(!oldMusciId.equals(music.id.id)){
                trackTask.execute(music.id.id);
            }
            mDetailAdapter = new MainAdapter(getActivity(), R.layout.list_item_artist_ablum, mListInstanceState);
            listView = (ObservableListView) detailRootView.findViewById(R.id.listview_detail);
            listView.setAdapter(mDetailAdapter); //shoot the ArrayAdapter on to Screen
            listviewClickListener();
        }
        oldMusciId=music.id.id;
        return detailRootView;
    }
    //listener for listview and sent intent to mediaPlayer
    private void listviewClickListener(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                //MusicData music = mDetailAdapter.getItem(position);
                ArrayList<MusicData>music=new ArrayList<MusicData>();
                for (int i=0;i<mDetailAdapter.getCount();i++){
                    music.add(mDetailAdapter.getItem(i));
                }
                if (MainActivity.getMTwoPane()){
                    MusicPlayFragment dialog= MusicPlayFragment.newInstance(music, artist,position);
                    dialog.show(getActivity().getFragmentManager(),DETAIL_URI);

                }else {
                    Intent mediaPlayer = new Intent(getActivity(), MusicPlay.class)
                            .putExtra("Object", music)
                            .putExtra(Intent.EXTRA_TEXT, artist)
                            .putExtra("position",position);

                    startActivity(mediaPlayer);
                }
                //Reference
                //http://developer.android.com/guide/components/intents-filters.html#ExampleExplicit
            }
        });
    }

    /**this part for fill gap view
     *
     */
    private void fillGapViewDisply(){
        //this part for fillGap view
        int image_height=MainActivity.getMTwoPane()?R.dimen.flexible_space_image_height_mTwoPane:R.dimen.flexible_space_image_height;
        mFlexibleSpaceImageHeight = getResources().getDimensionPixelSize(image_height);
        mActionBarSize = getActionBarSize();

        // Even when the top gap has began to change, header bar still can move
        // within mIntersectionHeight.
        mIntersectionHeight = getResources().getDimensionPixelSize(R.dimen.intersection_height);
        mImage = detailRootView.findViewById(R.id.image);
        mHeader = detailRootView.findViewById(R.id.header);
        mHeaderBar = detailRootView.findViewById(R.id.header_bar);
        mHeaderBackground = detailRootView.findViewById(R.id.header_background);
        mListBackgroundView = detailRootView.findViewById(R.id.list_background);
        ((TextView) detailRootView.findViewById(R.id.title)).setText(artist);
        //getActivity().setTitle(null);
        final ObservableListView scrollable = createScrollable();
        ScrollUtils.addOnGlobalLayoutListener((View) scrollable, new Runnable() {
            @Override
            public void run() {
                mReady = true;
                updateViews(scrollable.getCurrentScrollY(), false);
            }
        });
    }

    protected int getActionBarSize() {
        TypedValue typedValue = new TypedValue();
        int[] textSizeAttr = new int[]{R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = getActivity().obtainStyledAttributes(typedValue.data, textSizeAttr);
        int actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        return actionBarSize;
    }
    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        updateViews(scrollY, true);
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
    }

    protected void updateViews(int scrollY, boolean animated) {
        // If it's ListView, onScrollChanged is called before ListView is laid out (onGlobalLayout).
        // This causes weird animation when onRestoreInstanceState occurred,
        // so we check if it's laid out already.
        if (!mReady) {
            return;
        }
        // Translate image
        ViewHelper.setTranslationY(mImage, -scrollY / 2);


        // Translate header
        ViewHelper.setTranslationY(mHeader, getHeaderTranslationY(scrollY));

        // Show/hide gap
        final int headerHeight = mHeaderBar.getHeight();
        boolean scrollUp = mPrevScrollY < scrollY;
        if (scrollUp) {
            if (mFlexibleSpaceImageHeight - headerHeight - mActionBarSize <= scrollY) {
                changeHeaderBackgroundHeightAnimated(false, animated);
            }
        } else {
            if (scrollY <= mFlexibleSpaceImageHeight - headerHeight - mActionBarSize) {
                changeHeaderBackgroundHeightAnimated(true, animated);
            }
        }
        mPrevScrollY = scrollY;

        // Translate list background
        ViewHelper.setTranslationY(mListBackgroundView, ViewHelper.getTranslationY(mHeader));
    }

    protected float getHeaderTranslationY(int scrollY) {
        return ScrollUtils.getFloat(-scrollY + mFlexibleSpaceImageHeight - mHeaderBar.getHeight(), 0, Float.MAX_VALUE);
    }

    private void changeHeaderBackgroundHeightAnimated(boolean shouldShowGap, boolean animated) {
        if (mGapIsChanging) {
            return;
        }
        final int heightOnGapShown = mHeaderBar.getHeight();
        final int heightOnGapHidden = mHeaderBar.getHeight() + mActionBarSize;
        final float from = mHeaderBackground.getLayoutParams().height;
        final float to;
        if (shouldShowGap) {
            if (!mGapHidden) {
                // Already shown
                return;
            }
            to = heightOnGapShown;
        } else {
            if (mGapHidden) {
                // Already hidden
                return;
            }
            to = heightOnGapHidden;
        }
        if (animated) {
            com.nineoldandroids.view.ViewPropertyAnimator.animate(mHeaderBackground).cancel();
            ValueAnimator a = ValueAnimator.ofFloat(from, to);
            a.setDuration(100);
            a.setInterpolator(new AccelerateDecelerateInterpolator());
            a.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float height = (float) animation.getAnimatedValue();
                    changeHeaderBackgroundHeight(height, to, heightOnGapHidden);
                }
            });
            a.start();
        } else {
            changeHeaderBackgroundHeight(to, to, heightOnGapHidden);
        }
    }

    private void changeHeaderBackgroundHeight(float height, float to, float heightOnGapHidden) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mHeaderBackground.getLayoutParams();
        lp.height = (int) height;
        lp.topMargin = (int) (mHeaderBar.getHeight() - height);
        mHeaderBackground.requestLayout();
        mGapIsChanging = (height != to);
        if (!mGapIsChanging) {
            mGapHidden = (height == heightOnGapHidden);
        }
    }

    protected ObservableListView createScrollable() {
        ObservableListView listView = (ObservableListView) detailRootView.findViewById(R.id.listview_detail);
        listView.setScrollViewCallbacks(this);
        setDummyDataWithHeader(listView, mFlexibleSpaceImageHeight);
        return listView;
    }

    protected void setDummyDataWithHeader(ListView listView, int headerHeight) {
        View headerView = new View(getActivity());
        headerView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, headerHeight));
        headerView.setMinimumHeight(headerHeight);
        // This is required to disable header's list selector effect
        headerView.setClickable(true);
        listView.addHeaderView(headerView);
    }



    public class FetchTrackTask extends AsyncTask<String, Void, ArrayList<MusicData>> {
        private final String LOG_TAG = FetchTrackTask.class.getSimpleName();
        protected ArrayList<MusicData> doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            // Will contain the raw JSON response as a string.
            String artistJsonStr = null;

            //            GET https://api.spotify.com/v1/artists/{id}/top-tracks
            try {
                final String ARTIST_BASE_URL = "https://api.spotify.com/v1/artists/";
                final String QUERY_PARAM = "q";
                final String TYPE_PARAM = "top-tracks";
                final String COUNTRY_PARAM = "?country="+MainActivity.getMLocation();


                //URL url= new URL("https://api.spotify.com/v1/search?q=tania*&type=artist");
//                 "https://api.spotify.com/v1/artists/43ZHCT0cAZBISjO8DG9PnE/top-tracks?country=SE" -H
                Uri builtUri = Uri.parse(ARTIST_BASE_URL).buildUpon()
                        .appendPath(params[0])
                        .appendEncodedPath(TYPE_PARAM)
                        .appendEncodedPath(COUNTRY_PARAM)
                        .build();
                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI" + builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");

                }
                if (buffer.length() == 0) {
                    return null;

                }
                artistJsonStr = buffer.toString();
                //Log.v(LOG_TAG, "artisList JSON String"+artistJsonStr);

            } catch (IOException e) {
                Log.e("LOG_TAG", "Error " + e);
                return null;

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("LOG_TAG", "Error closing stream", e);
                    }
                }
            }
            try {
                return getTrackDataFromJson(artistJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;


        }
        private ArrayList<MusicData> getTrackDataFromJson(String musicJsonStr)
                throws JSONException {
            ArrayList<MusicData> music;
            String trackName;
            String albumName;
            String albumImage640;
            String albumImage300;
            String previewUrl;
            String external_urls;
            int NUMBER_OF_DISPLAY_TRACK = 10;

            //Log.v(LOG_TAG, "artisList JSON String"+musicJsonStr);

            // These are the names of the JSON objects that need to be extracted.
            final String SPOTIFY_TRACKS = "tracks";
            final String SPOTIFY_ID = "id";
            final String SPOTIFY_IMAGE = "images";
            JSONObject musicJson = new JSONObject(musicJsonStr);
            JSONArray musicArray = musicJson.getJSONArray(SPOTIFY_TRACKS);
            //Log.v(LOG_TAG, "artisList JSON String"+musicArray.toString());
            int numberOfAritist = musicArray.length();

            if (numberOfAritist < NUMBER_OF_DISPLAY_TRACK) {
                NUMBER_OF_DISPLAY_TRACK = numberOfAritist;    //by any chance if top tracks are less than 10 will result at out of boundary
            }

            music = new ArrayList<MusicData>();

            for (int i = 0; i < NUMBER_OF_DISPLAY_TRACK; i++) {
                final String OWM_ALBUM = "album";
                final String OWM_URL = "url";
                final String OWM_NAME = "name";
                final String OWM_PREVIEWURL = "preview_url";
                final String OWM_EXTERNAL_URLS = "external_urls";
                final String OWM_SPOTIFY="spotify";

                final int ALBUMIMAGE640PX_SEQUENCE = 0;
                final int ALBUMIMAGE300PX_SEQUENCE = 1;

                // Get the JSON object representing the this track
                JSONObject artistObject = musicArray.getJSONObject(i);
                trackName = artistObject.getString(OWM_NAME);
                previewUrl = artistObject.getString(OWM_PREVIEWURL);

                JSONObject album = artistObject.getJSONObject(OWM_ALBUM);
                albumName = album.getString(OWM_NAME);

                JSONObject externalURL=artistObject.getJSONObject(OWM_EXTERNAL_URLS);
                external_urls=externalURL.getString(OWM_SPOTIFY);



                //JSONObject images= artistObject.getJSONArray(SPOTIFY_IMAGE).getJSONObject(SMALL_IMG_SEQUENCE);
                JSONArray images = album.getJSONArray(SPOTIFY_IMAGE);
                if (images.length() > 0) {
                    JSONObject image640 = images.getJSONObject(ALBUMIMAGE640PX_SEQUENCE);
                    if (image640.getString(OWM_URL) != null) {
                        albumImage640 = image640.getString(OWM_URL);
                    } else {
                        albumImage640 = "";

                    }
                    JSONObject image300 = images.getJSONObject(ALBUMIMAGE300PX_SEQUENCE);
                    if (image300.getString(OWM_URL) != null) {
                        albumImage300 = image300.getString(OWM_URL);
                    } else {
                        albumImage300 = "";
                    }
                } else {
                    //TO do add a pic holder
                    albumImage640 = "";
                    albumImage300 = "";
                }
                // music.add(new MusicData(artistName, imagesUrlS, id));
                music.add(new MusicData(musicID, trackName, albumName, albumImage640, albumImage300, previewUrl,external_urls));

            }
            //output the  the formated com.example.android.spotifystreamer.apk.data
            for (MusicData s : music) {
                Log.v(LOG_TAG, "Detail entry: " + s);
            }
            return music;

        }
        protected void onPostExecute(ArrayList<MusicData> result) {
            if (result != null) {
                mDetailAdapter.clear();
                mDetailAdapter.addAll(result);
            }

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //outState.putParcelable(SAVE_PAGE_KEY, listView.onSaveInstanceState());
        outState.putParcelableArrayList(SAVE_PAGE_KEY, mListInstanceState);
        super.onSaveInstanceState(outState);

    }

}
