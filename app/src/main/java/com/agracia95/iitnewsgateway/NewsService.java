package com.agracia95.iitnewsgateway;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class NewsService extends Service
{
    private static final String TAG = "NewsService";
    private boolean running = true;
    private final ArrayList<Article> articleList = new ArrayList<>();
    private ServiceReceiver receiver;


    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        receiver = new ServiceReceiver();
        IntentFilter filter = new IntentFilter(MainActivity.ACTION_MSG_TO_SERVICE);
        registerReceiver(receiver, filter);

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {

                while (running)
                {
                    try
                    {
                        Thread.sleep(250);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    if (articleList.isEmpty())
                        continue;

                    Intent intent = new Intent();
                    intent.setAction(MainActivity.ACTION_NEWS_STORY);
                    intent.putExtra(MainActivity.ARTICLES_DATA, articleList);
                    sendBroadcast(intent);

                    articleList.clear();
                }

            }
        }).start();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        unregisterReceiver(receiver);
        running = false;
        super.onDestroy();
    }

    public void setArticles(List<Article> articles)
    {
        articleList.clear();
        articleList.addAll(articles);
    }

    class ServiceReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action == null)
                return;
            if (action.equals(MainActivity.ACTION_MSG_TO_SERVICE))
            {
                Source source = (Source) intent.getSerializableExtra(MainActivity.SOURCE_DATA);
                new NewsArticleDownloader((NewsService) context).execute(source);
            }
        }
    }
}
