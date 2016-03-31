package com.example.bartrongen.mmqalpha;

import android.content.Context;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.InjectView;

import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


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
    boolean startOnStop = false;
    Integer fixcount = 2;
    int easteregg = 0;
    ActionMenuItemView toot;
    boolean eastereggactive = false;

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
        //easteregg
        toot = (ActionMenuItemView) findViewById(R.id.toot);

        mToolbar.setTitleTextColor(-1);
        //inject ButterKnife for onclick methods and views
        ButterKnife.inject(this);
        //search handler
        handler = new Handler();
        //add listeners
        addEditorListener();
        addClickListener();
        getUpcoming();
    }

    private void getUpcoming() {
        API.getService().getUpcoming(getIntent().getStringExtra("channel")).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, retrofit2.Response<JsonElement> response) {
                JsonArray array = response.body().getAsJsonObject().get("upcoming").getAsJsonArray();
                video_code_array = new ArrayList<String>();
                video_title_array = new ArrayList<String>();
                video_r_id_array = new ArrayList<Integer>();
                for (int i = 0; i < array.size(); i++) {
                    JsonObject temp = array.get(i).getAsJsonObject();
                    video_code_array.add(temp.get("code").getAsString());
                    video_title_array.add(temp.get("title").getAsString());
                    video_r_id_array.add(temp.get("r_id").getAsInt());
                }
                arrayAdapter = new ArrayAdapter<String>(
                        context,
                        R.layout.broadcast_list_item,
                        R.id.broadcast_title,
                        video_title_array);

                lv_upcoming.setAdapter(arrayAdapter);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

            }
        });
    }

    private void postVideo(String id, String title) {
        HashMap<String, String> map = new HashMap<>();
        map.put("id", id);
        map.put("title", title);
        map.put("duration", "180");

        API.getService().postVideo(getIntent().getStringExtra("channel"), map).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, retrofit2.Response<JsonElement> response) {

            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

            }
        });
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                getUpcoming();
            }
        }, 500);
    }

    private void removeLastPlayed(Integer r_id) {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("id", r_id);

        API.getService().removeLastPlayed(getIntent().getStringExtra("channel"), map).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, retrofit2.Response<JsonElement> response) {

            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

            }
        });
        handler.postDelayed(new Runnable() {
            public void run() {
                getUpcoming();
            }
        }, 500);
    }

    private void removeWithoutPlaying(Integer r_id) {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("id", r_id);

        API.getService().removeWithoutPlaying(getIntent().getStringExtra("channel"), map).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, retrofit2.Response<JsonElement> response) {

            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

            }
        });
        handler.postDelayed(new Runnable() {
            public void run() {
                getUpcoming();
            }
        }, 500);
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
        if (!restored){
            player = player_local;
            player.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
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
            removeLastPlayed(currentR_id);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Toast.makeText(this, getString(R.string.failed), Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.broadcast_start)
    public void broadcastStart(View v){
        if (video_code_array == null) {
            Toast.makeText(context, "Add a video to the list first", Toast.LENGTH_SHORT).show();
        } else if (player == null){
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
        if (eastereggactive) {
            toot.setIcon(getDrawable(R.drawable.ic_volume_off_white_24dp));
            eastereggactive = false;
            easteregg = 0;
        } else if (easteregg < 2){
            easteregg++;
        } else if (easteregg == 4 ){
            toot.setIcon(getDrawable(R.drawable.ic_volume_up_white_24dp));
            eastereggactive = true;
        } else {
            easteregg = 0;
        }
        getUpcoming();
    }

    @OnClick(R.id.toot)
    public void toot(View v){
        if (eastereggactive) {
            API.getService().toot(getIntent().getStringExtra("channel")).enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, retrofit2.Response<JsonElement> response) {

                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {

                }
            });
        }
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
        if (easteregg == 2 || easteregg == 3){
            easteregg++;
        } else {
            easteregg = 0;
        }
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
            removeLastPlayed(currentR_id);
        } else {
            if (fixcount == 2) {
                Log.d("YC", "set fix to true");
                startOnStop = true;
                fixcount = 0;
            } else {
                fixcount++;
            }
        }
    }

    @Override
    public void onBuffering(boolean b) {

    }

    @Override
    public void onSeekTo(int i) {

    }
}