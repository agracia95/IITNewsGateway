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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_OK;

public class NewsArticleDownloader extends AsyncTask<Source, Void, String>
{
    private static final String TAG = "NewsArticleDownloader";
    private DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private NewsService ns;

    NewsArticleDownloader(NewsService newsService) { ns = newsService; }

    @Override
    protected void onPostExecute(String articlesJson)
    {
        List<Article> articles = parseJson(articlesJson);
        ns.setArticles(articles);
    }

    @Override
    protected String doInBackground(Source... sources)
    {
        String sourceId = sources[0].getId();
        // http://newsapi.org/v2/everything?sources=%s&amp;language=en&amp;pageSize=10&amp;apiKey=%s
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try
        {
            String base_url = ns.getApplicationContext().getString(R.string.news_article_url);
            String APIKey = ns.getApplicationContext().getString(R.string.api_key);
            Uri uri = Uri.parse(base_url)
                    .buildUpon()
                    .appendQueryParameter("sources", sourceId)
                    .appendQueryParameter("language", "en")
                    .appendQueryParameter("pageSize", "10")
                    .appendQueryParameter("apiKey", APIKey)
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

    private List<Article> parseJson(String jsonString)
    {
        List<Article> articles = new ArrayList<>();

        try
        {
            JSONObject json = new JSONObject(jsonString);
            JSONArray articlesJson = json.getJSONArray("articles");

            for (int i = 0; i < articlesJson.length(); i++) {
                JSONObject articleJson = articlesJson.getJSONObject(i);
                articles.add(new Article(
                        articleJson.getString("author"),
                        articleJson.getString("title"),
                        articleJson.getString("description"),
                        articleJson.getString("url"),
                        articleJson.getString("urlToImage"),
                        df.parse(articleJson.getString("publishedAt")))
                );
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return articles;
    }
}
