package com.example.bartrongen.mmqalpha;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import butterknife.InjectView;

import butterknife.OnClick;
import butterknife.Optional;

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
    List<String> title_array, slug_array;
    EditText dialogText;

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
                        String value = input.getText().toString().trim();
                        Toast.makeText(getApplicationContext(), value,
                                Toast.LENGTH_SHORT).show();
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
                    String value = input.getText().toString().trim();
                    Toast.makeText(getApplicationContext(), value,
                            Toast.LENGTH_SHORT).show();
                    dlg.dismiss();
                    return false;
                }
                return true;
            }
        });
        dlg.setOnShowListener(new DialogInterface.OnShowListener()
        {
            @Override
            public void onShow(final DialogInterface dialog)
            {
                input.requestFocus();
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(input, 0);
            }
        });
        dlg.show();
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


