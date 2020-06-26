package com.example.android.newsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    /** URL for news data from Guardian APIs */
    private static final String REQUEST_URL =
            "https://content.guardianapis.com/search?q=debate&tag=politics/politics&from-date=2014-01-01&api-key=test";

    /**
     * Constant value for the earthquake loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int NEWS_LOADER_ID = 1;

    private String TAG = MainActivity.class.getName();
    private ListView list_view;

    /** TextView that is displayed when the list is empty */
    private TextView mEmptyStateTextView;

    ArrayList<HashMap<String, String>> newsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        newsList = new ArrayList<>();
        list_view = findViewById(R.id.list);

        mEmptyStateTextView = findViewById(R.id.empty_view);
        list_view.setEmptyView(mEmptyStateTextView);

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // We have Internet connection.
        } else {
            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }

        new Getnews().execute();
    }

    private class Getnews extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            String jsonString = "";
            try {
                // DONE: Make a request to the URL
                jsonString = sh.makeHttpRequest(createUrl(REQUEST_URL));
            } catch (IOException e) {
                return null;
            }

            Log.e(TAG, "Response from url: " + jsonString.toString());
            if (jsonString != null) {
                try {
                    // DONE: Create a new JSONObject
                    JSONObject jsonObject = new JSONObject(jsonString);

                    // DONE: Get the JSON Array node and name it "results"
                    JSONObject response = jsonObject.getJSONObject("response");

                    JSONArray results = response.getJSONArray("results");


                    // looping through all Contacts
                    for (int i = 0; i < results.length(); i++) {
                        // DONE: get the JSONObject and its three attributes
                        JSONObject c = results.getJSONObject(i);
                        String name = c.getString("webTitle");
                        String section = c.getString("sectionName");
                        String date = c.getString("webPublicationDate");
                        String url = c.getString("webUrl");

                        // tmp hash map for a single news
                        HashMap<String, String> news = new HashMap<>();

                        // add each child node to HashMap key => value
                        news.put("name", name);
                        news.put("section", section);
                        news.put("date", date.substring(0,10));
                        news.put("url", url);

                        // adding a news to our news list
                        newsList.add(news);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                }

            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }

        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                return null;
            }
            return url;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Hide the empty state text view as the loading indicator will be displayed
            mEmptyStateTextView.setVisibility(View.GONE);

            final ListAdapter adapter = new SimpleAdapter(MainActivity.this, newsList,
                    R.layout.list_item, new String[]{"name", "section", "date", "url"},
                    new int[]{R.id.name, R.id.section, R.id.date, R.id.url});
            list_view.setAdapter(adapter);

            // Set a click listener to open the url when the list item is clicked on.
            list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    // Open link
                    TextView textView = findViewById(R.id.url);
                    String url = textView.getText().toString();
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            });
        }

    }
}