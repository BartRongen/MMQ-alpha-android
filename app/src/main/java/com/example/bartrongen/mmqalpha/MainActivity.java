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
    Map<String, String> params;

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
        new getData().execute();

        params = new Map<String, String>() {
            @Override
            public void clear() {

            }

            @Override
            public boolean containsKey(Object key) {
                return false;
            }

            @Override
            public boolean containsValue(Object value) {
                return false;
            }

            @NonNull
            @Override
            public Set<Entry<String, String>> entrySet() {
                return null;
            }

            @Override
            public String get(Object key) {
                return null;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @NonNull
            @Override
            public Set<String> keySet() {
                return null;
            }

            @Override
            public String put(String key, String value) {
                return null;
            }

            @Override
            public void putAll(Map<? extends String, ? extends String> map) {

            }

            @Override
            public String remove(Object key) {
                return null;
            }

            @Override
            public int size() {
                return 0;
            }

            @NonNull
            @Override
            public Collection<String> values() {
                return null;
            }
        };


        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://mmq.audio/channels";


        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, (String) null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        //getActionBar().setTitle("Response: " + response.toString());
                        channelTitle = "test";
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });

        queue.add(jsObjRequest);
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

    public void postThatShit(String name){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://mmq.audio/add";
        params.put("title", name.toString());
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
                channelTitle = error.networkResponse.toString();

            }
        });

        queue.add(req);
    }


    class postChannel extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... args) {

            Map<String,String> titleMap = new HashMap<>();
            //titleMap.put("title", channelTitle);
            titleMap.put("city", "android_test");
            titleMap.put("review", "android_test");

            String encodedTitle = getEncodedData(titleMap);

            BufferedReader reader = null;

            try {
                //URL url = new URL("http://mmq.audio/add");
                URL url = new URL("http://2id60.win.tue.nl:2345/reviews");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);


                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                writer.write("city=android_test&review=android_test&email=android_test");
                writer.flush();
                writer.close();
                os.close();
                int responseCode = conn.getResponseCode();

                Log.i("code", String.valueOf(responseCode));

                /*
                StringBuilder sb = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null){
                    sb.append(line + "\n");
                }
                line = sb.toString();

                Log.i("custom_check", "The values received in the store part are as follows:");
                Log.i("custom_check",line);
                */
            } catch (IOException e){
                e.printStackTrace();

            } finally {
                if (reader != null){
                    try{
                        reader.close();
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
            return "true";
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }

    private String getEncodedData(Map<String,String> data) {
        StringBuilder sb = new StringBuilder();
        for(String key : data.keySet()) {
            String value = null;
            try {
                key = URLEncoder.encode(key, "UTF-8");
                value = URLEncoder.encode(data.get(key), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if(sb.length()>0)
                sb.append("&");

            sb.append(key + "=" + value);
        }
        return sb.toString();
    }

    void postChannel(String channelTitle) throws IOException {
        URL url = new URL("http://mmq.audio/add");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        Uri.Builder builder = new Uri.Builder()
                .appendQueryParameter("title", channelTitle);
        String query = builder.build().getEncodedQuery();

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(query);
        writer.flush();
        writer.close();
        os.close();

        conn.connect();
    }

    class getData extends AsyncTask<String, String, String> {

            HttpURLConnection urlConnection;
            JSONObject jsonObject = new JSONObject();

            @Override
            protected String doInBackground(String... args) {

                StringBuilder result = new StringBuilder();

                try {
                    URL url = new URL("http://mmq.audio/channels");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                }catch( Exception e) {
                    e.printStackTrace();
                }
                finally {
                    urlConnection.disconnect();
                }

                return result.toString();
            }

            @Override
            protected void onPostExecute(String result) {

                try {
                    JSONObject jsonObject = new JSONObject(result.toString());
                    JSONArray array = jsonObject.getJSONArray("channels");
                    title_array = new ArrayList<String>();
                    slug_array = new ArrayList<String>();
                    for (int i=0; i<array.length(); i++){
                        JSONObject temp = (JSONObject) array.get(i);
                        title_array.add(temp.getString("title"));
                        slug_array.add(temp.getString("slug"));
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
    }
}


