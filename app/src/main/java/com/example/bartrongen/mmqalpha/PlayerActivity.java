package com.example.bartrongen.mmqalpha;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.InjectView;

import butterknife.OnClick;
import butterknife.Optional;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Bart Rongen on 26-1-2016.
 */
public class PlayerActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener, YouTubePlayer.PlaybackEventListener {

    Context context = this;
    List<String> video_code_array, video_title_array;
    List<Integer> video_r_id_array;
    YouTubePlayer player;
    Toolbar mToolbar;
    ArrayAdapter<String> arrayAdapter;
    String currentTitle;
    String currentId;
    Integer currentR_id;
    Handler handler;
    private List<VideoItem> searchResults;
    ArrayAdapter<VideoItem> adapter;
    boolean startOnStop = true;
    private Timer myTimer;

    //injecting views
    @InjectView(R.id.now_playing) TextView now_playing;
    @InjectView(R.id.search_input) EditText searchInput;
    @InjectView(R.id.videos_found) ListView videosFound;
    @InjectView(R.id.player_view) YouTubePlayerView playerView;
    @InjectView(R.id.list_upcoming) ListView lv_upcoming;
    @InjectView(R.id.linlay_search) LinearLayout ll_search;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        //sets the contentview
        setContentView(R.layout.activity_player);
        //configure toolbar
        mToolbar = (Toolbar) findViewById(R.id.mtoolbar);
        mToolbar.setTitle(getIntent().getStringExtra("channel"));
        mToolbar.inflateMenu(R.menu.menu_main);
        mToolbar.setTitleTextColor(-1);
        //inject ButterKnife for onclick methods and views
        ButterKnife.inject(this);
        //search handler
        handler = new Handler();
        //add listeners
        addEditorListener();
        addClickListener();
        getUpcoming();
        //timer for api call, every 10 s
        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //getUpcoming();
            }

        }, 0, 10000);
    }

    private void getUpcoming() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://mmq.audio/" + getIntent().getStringExtra("channel") + "/upcoming";

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, (String) null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray array = response.getJSONArray("upcoming");
                    video_code_array = new ArrayList<String>();
                    video_title_array = new ArrayList<String>();
                    video_r_id_array = new ArrayList<Integer>();
                    for (int i=0; i<array.length(); i++){
                        JSONObject temp = (JSONObject) array.get(i);
                        video_code_array.add(temp.getString("code"));
                        video_title_array.add(temp.getString("title"));
                        video_r_id_array.add(temp.getInt("r_id"));
                    }

                    arrayAdapter = new ArrayAdapter<String>(
                            context,
                            R.layout.broadcast_list_item,
                            R.id.broadcast_title,
                            video_title_array);

                    lv_upcoming.setAdapter(arrayAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub

            }
        });

        queue.add(req);
    }

    private void postVideo(String id, String title) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://mmq.audio/" + getIntent().getStringExtra("channel") + "/add";
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", id);
            obj.put("title", title);
            obj.put("duration", "180");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, obj, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                //getActionBar().setTitle("Response: " + response.toString());
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub

            }
        });

        queue.add(req);
        getUpcoming();
    }

    private void removeLastPlayed(Integer r_id) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://mmq.audio/" + getIntent().getStringExtra("channel") + "/finish";
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", r_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, obj, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                //getActionBar().setTitle("Response: " + response.toString());
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                error.getCause();

            }
        });

        queue.add(req);
        getUpcoming();
    }

    private void removeWithoutPlaying(Integer r_id) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://mmq.audio/" + getIntent().getStringExtra("channel") + "/remove";
        JSONObject obj = new JSONObject();
        try {
            obj.put("id", r_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, obj, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                //getActionBar().setTitle("Response: " + response.toString());
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
            }
        });

        queue.add(req);
        //no get request needed
        //getUpcoming();
    }


    private void addEditorListener() {
        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    searchOnYoutube(v.getText().toString());
                    return false;
                }
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    searchOnYoutube(v.getText().toString());
                    loseFocus();
                    return false;
                }
                return true;
            }
        });
    }

    private void loseFocus(){
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        } catch (NullPointerException e){}
    }

    private void searchOnYoutube(final String keywords){
        new Thread(){
            public void run(){
                YoutubeConnector yc = new YoutubeConnector(PlayerActivity.this);
                searchResults = yc.search(keywords);
                handler.post(new Runnable(){
                    public void run(){
                        updateVideosFound();
                    }
                });
            }
        }.start();
    }

    private void updateVideosFound(){
        adapter = new ArrayAdapter<VideoItem>(getApplicationContext(), R.layout.video_item, searchResults){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null){
                    convertView = getLayoutInflater().inflate(R.layout.video_item, parent, false);
                }
                ImageView thumbnail = (ImageView)convertView.findViewById(R.id.video_thumbnail);
                TextView title = (TextView)convertView.findViewById(R.id.video_title);
                TextView description = (TextView)convertView.findViewById(R.id.video_description);

                VideoItem searchResult = searchResults.get(position);

                Picasso.with(getApplicationContext()).load(searchResult.getThumbnailURL()).into(thumbnail);
                title.setText(searchResult.getTitle());
                description.setText(searchResult.getDescription());
                return convertView;
            }
        };

        videosFound.setAdapter(adapter);
    }

    private void addClickListener(){
        videosFound.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos,
                                    long id) {
                Toast.makeText(context, "Added video to the list", Toast.LENGTH_SHORT).show();
                //video_code_array.add(searchResults.get(pos).getId());
                //video_title_array.add(searchResults.get(pos).getTitle());
                arrayAdapter.notifyDataSetChanged();
                postVideo(searchResults.get(pos).getId(), searchResults.get(pos).getTitle());
            }
        });
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player_local, boolean restored) {
        if(!restored){
            player = player_local;
            player.setPlaybackEventListener(this);
            player.loadVideo(video_code_array.get(0));
            currentTitle = video_title_array.get(0);
            currentId = video_code_array.get(0);
            currentR_id = video_r_id_array.get(0);
            now_playing.setText("Now playing: " + currentTitle);
            video_code_array.remove(0);
            video_title_array.remove(0);
            video_r_id_array.remove(0);
            arrayAdapter.notifyDataSetChanged();
            //removeLastPlayed(currentR_id);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Toast.makeText(this, getString(R.string.failed), Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.broadcast_start)
    public void broadcastStart(View v){
        if (player == null){
            playerView.initialize(YoutubeConnector.KEY, this);
        } else {
            player.play();
        }
    }

    @OnClick(R.id.broadcast_pause)
    public void broadcastPause(View v) {
        if (player != null) {
            if (player.isPlaying()) {
                player.pause();
            }
        }
    }

    @OnClick(R.id.refresh)
    public void refresh(View v){
        getUpcoming();
    }

    @OnClick(R.id.tv_upcoming)
    public void upcomingClicked(View v){
        loseFocus();
        //clear videosfound list
        try {
            searchResults.clear();
            adapter.notifyDataSetChanged();
        } catch (NullPointerException e){}

        ll_search.setVisibility(View.GONE);
        lv_upcoming.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.tv_add_video)
    public void addVideoClicked(View v){
        lv_upcoming.setVisibility(View.GONE);
        ll_search.setVisibility(View.VISIBLE);
        searchInput.setText("");
    }

    public void removeFromBroadcastList(View v) {
        LinearLayout vwParentRow = (LinearLayout)v.getParent();
        int i = lv_upcoming.getPositionForView(vwParentRow);
        Integer r_idForRemove = video_r_id_array.get(i);
        video_code_array.remove(i);
        video_title_array.remove(i);
        arrayAdapter.notifyDataSetChanged();
        removeWithoutPlaying(r_idForRemove);
    }

    public void onUpcomingClick(View v) {
        LinearLayout vwParentRow = (LinearLayout) v.getParent();
        int i = lv_upcoming.getPositionForView(vwParentRow);
        if (player != null) {
            //fix so that a new video doesn't get played by onStopped()
            startOnStop = false;
            player.loadVideo(video_code_array.get(i));
            currentTitle = video_title_array.get(i);
            currentId = video_code_array.get(i);
            currentR_id = video_r_id_array.get(i);
            now_playing.setText("Now playing: " + currentTitle);
            video_code_array.remove(i);
            video_title_array.remove(i);
            video_r_id_array.remove(i);
            arrayAdapter.notifyDataSetChanged();
            removeLastPlayed(currentR_id);
        } else {
            Toast.makeText(this, "Start broadcast first", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPlaying() {

    }

    @Override
    public void onPaused() {

    }

    @Override
    public void onStopped() {
        //fix for the 2-loop
        if (startOnStop){
            //remove last played and starts next
            Log.d("YC", "set fix to false");
            startOnStop = false;
            player.loadVideo(video_code_array.get(0));
            currentTitle = video_title_array.get(0);
            currentId = video_code_array.get(0);
            currentR_id = video_r_id_array.get(0);
            now_playing.setText("Now playing: " + currentTitle);
            video_code_array.remove(0);
            video_title_array.remove(0);
            video_r_id_array.remove(0);
            arrayAdapter.notifyDataSetChanged();
            //removeLastPlayed(currentR_id);
        } else {
            Log.d("YC", "set fix to true");
            startOnStop = true;
        }
    }

    @Override
    public void onBuffering(boolean b) {

    }

    @Override
    public void onSeekTo(int i) {

    }
}