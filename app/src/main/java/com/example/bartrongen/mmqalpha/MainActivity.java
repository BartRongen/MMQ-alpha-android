package com.example.bartrongen.mmqalpha;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.api.client.http.HttpResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.InjectView;

import butterknife.OnClick;
import butterknife.Optional;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends ActionBarActivity {
    Context context = this;
    List<String> title_array, slug_array;
    EditText dialogText;
    String channelTitle;

    @InjectView(R.id.listView) ListView lv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        this.setTitle("MMQalpha");
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object slug = slug_array.get(position);
                Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                intent.putExtra("channel", slug.toString());
                startActivity(intent);
            }
        });

        getChannels();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.add_channel:
                createAddChannelDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void createAddChannelDialog() {
        final EditText input = new EditText(context);
        input.setHint("channel name");
        input.setSingleLine(true);
        final AlertDialog dlg = new AlertDialog.Builder(this).
                setTitle("Add a channel").
                setView(input).
                setCancelable(false).
                setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        channelTitle = input.getText().toString().trim();
                        Toast.makeText(getApplicationContext(), channelTitle,
                                Toast.LENGTH_SHORT).show();
                        postThatShit(channelTitle);
                        dialog.dismiss();
                    }
                }).
                setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                    }
                }).create();
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    channelTitle = input.getText().toString().trim();
                    Toast.makeText(getApplicationContext(), channelTitle,
                            Toast.LENGTH_SHORT).show();
                    postThatShit(channelTitle);
                    dlg.dismiss();
                    Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                    intent.putExtra("channel", channelTitle);
                    startActivity(intent);
                    return false;
                }
                return true;
            }
        });
        dlg.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                input.requestFocus();
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(input, 0);
            }
        });
        dlg.show();
    }

    private void getChannels() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://mmq.audio/channels";

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, (String) null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray array = response.getJSONArray("channels");
                    title_array = new ArrayList<String>();
                    slug_array = new ArrayList<String>();
                    for (int i=0; i<array.length(); i++){
                        JSONObject temp = null;
                        try {
                            temp = (JSONObject) array.get(i);
                            title_array.add(temp.getString("title"));
                            slug_array.add(temp.getString("slug"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                            context,
                            android.R.layout.simple_list_item_1,
                            title_array);

                    lv.setAdapter(arrayAdapter);
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

    private void postThatShit(String name){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://mmq.audio/add";
        JSONObject obj = new JSONObject();
        try {
            obj.put("title", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST, url, obj, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                //getActionBar().setTitle("Response: " + response.toString());
                channelTitle = "test";
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
                //channelTitle = error.networkResponse.toString();

            }
        });

        queue.add(req);
    }
}


