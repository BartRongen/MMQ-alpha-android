package com.example.bartrongen.mmqalpha;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {
    Context context = this;
    ListView lv;
    List<String> title_array, slug_array;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setTitle("MMQalpha");
        lv = (ListView) findViewById(R.id.listView);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object slug = slug_array.get(position);
                setTitle(slug.toString());
                Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                intent.putExtra("channel", slug.toString());
                startActivity(intent);
            }
        });
        new getData().execute();
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


