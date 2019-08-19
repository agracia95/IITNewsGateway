package com.agracia95.iitnewsgateway;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.net.HttpURLConnection.HTTP_OK;

public class NewsSourceDownloader extends AsyncTask<String, Void, String>
{
    private static final String TAG = "NewsSourceDownloader";
    private MainActivity ma;
    private SortedSet<String> categories;

    NewsSourceDownloader(MainActivity mainActivity) { ma = mainActivity; }

    @Override
    protected void onPostExecute(String sourcesJson)
    {
        List<Source> sources = parseJson(sourcesJson);
        ma.setSources(sources, categories);
    }

    @Override
    protected String doInBackground(String... strings)
    {
        String category = "";
        if (strings.length > 0)
            category = strings[0];
        if (category.equals("") || category.equals("all"))
        {
            category = "";
            categories = new TreeSet<>();
            categories.add("all");
        }

        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try
        {
            Uri uri = Uri.parse(ma.getString(R.string.news_sources_url))
                    .buildUpon()
                    .appendQueryParameter("language", "en")
                    .appendQueryParameter("country", "us")
                    .appendQueryParameter("category", category)
                    .appendQueryParameter("apiKey", ma.getString(R.string.api_key))
                    .build();

            URL url = new URL(uri.toString());
            connection =(HttpURLConnection) url.openConnection();
            connection.connect();

            int responseCode = connection.getResponseCode();

            StringBuilder result = new StringBuilder();

            if (responseCode == HTTP_OK)
            {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String line;
                while (null != (line = reader.readLine()))
                {
                    result.append(line).append("\n");
                }

                return result.toString();
            } else
            {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));

                String line;
                while (null != (line = reader.readLine()))
                {
                    result.append(line).append("\n");
                }

                Log.w(TAG, "doInBackground: COMMUNICATION ERROR " + result);
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private List<Source> parseJson(String jsonString)
    {
        List<Source> sources = new ArrayList<>();

        try
        {
            JSONObject json = new JSONObject(jsonString);
            JSONArray sourcesJson = json.getJSONArray("sources");

            for (int i = 0; i < sourcesJson.length(); i++)
            {
                JSONObject sourceJson = sourcesJson.getJSONObject(i);
                String category = sourceJson.getString("category");

                if (categories != null)
                    categories.add(category);
                sources.add(new Source(
                        sourceJson.getString("id"),
                        sourceJson.getString("name"),
                        category));
            }

            Collections.sort(sources);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        return sources;
    }
}
