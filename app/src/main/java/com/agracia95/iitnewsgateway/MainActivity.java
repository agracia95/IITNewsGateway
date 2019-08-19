package com.agracia95.iitnewsgateway;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";
    static final String ACTION_MSG_TO_SERVICE = "ACTION_MSG_TO_SERVICE";
    static final String ACTION_NEWS_STORY = "ACTION_NEWS_STORY";
    static final String SOURCE_DATA = "SOURCE_DATA";
    static final String ARTICLES_DATA = "ARTICLES_DATA";

    private NewsReceiver receiver;
    private Map<String, Integer> categoryColor;
    private List<String> sourceNames;
    private Map<String, Source> sourceMap;
    private Menu catMenu;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private List<Fragment> fragments;
    private NewsPageAdapter pageAdapter;
    private ViewPager pager;
    private Source currentSource;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, NewsService.class);
        startService(intent);

        receiver = new NewsReceiver();

        categoryColor = new HashMap<>();
        sourceNames = new ArrayList<>();
        sourceMap = new HashMap<>();
        fragments = new ArrayList<>();

        drawerLayout = findViewById(R.id.drawer_layout);

        drawerList = findViewById(R.id.drawer_list);
        drawerList.setAdapter(new NewsDrawerAdapter(this, R.layout.drawer_item, sourceNames));
        drawerList.setOnItemClickListener(new ListView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                selectSource(position);
                drawerLayout.closeDrawer(drawerList);
            }
        });


        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.drawer_open, R.string.drawer_close);

        fragments = new ArrayList<>();

        pageAdapter = new NewsPageAdapter(getSupportFragmentManager());
        pager = findViewById(R.id.view_pager);
        pager.setAdapter(pageAdapter);

        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        new NewsSourceDownloader(this).execute();
    }

    @Override
    protected void onResume()
    {
        IntentFilter filter = new IntentFilter(ACTION_NEWS_STORY);
        registerReceiver(receiver, filter);
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.category_menu, menu);
        catMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (drawerToggle.onOptionsItemSelected(item)) { return true; }

        new NewsSourceDownloader(this).execute(item.getTitle().toString());
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop()
    {
        unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        Intent intent = new Intent(this, NewsService.class);
        stopService(intent);
        super.onDestroy();
    }

    public void setSources(List<Source> sources, SortedSet<String> categories)
    {
        sourceMap.clear();
        sourceNames.clear();
        for (Source s : sources)
        {
            sourceNames.add(s.getName());
            sourceMap.put(s.getName(), s);
        }

        if (categories != null)
        {
            this.categoryColor.clear();

            // Added because this would sometimes run before onCreateOptionsMenu, crashing the app
            while (catMenu == null) {}
            catMenu.clear();

            int i = 0;
            for (String s: categories)
            {
                if (!s.equals("all"))
                {
                    int[] colorIds = getResources().getIntArray(R.array.text_colors);
                    this.categoryColor.put(s, colorIds[i]);

                    // MenuItem currentItem = catMenu.getItem(i);
                    SpannableString coloredText = new SpannableString(s);
                    coloredText.setSpan(new ForegroundColorSpan(colorIds[i]), 0, s.length(), 0);
                    catMenu.add(coloredText);
                    i++;
                } else
                {
                    catMenu.add(s);
                }
            }
        }
        ((ArrayAdapter) drawerList.getAdapter()).notifyDataSetChanged();
    }

    public void selectSource(int pos)
    {
        currentSource = sourceMap.get(sourceNames.get(pos));
        Intent intent = new Intent();
        intent.setAction(ACTION_MSG_TO_SERVICE);
        intent.putExtra(SOURCE_DATA, currentSource);
        sendBroadcast(intent);
    }

    public void reDoFragments(List<Article> articles)
    {
        getSupportActionBar().setTitle(currentSource.getName());

        for (int i = 0; i < pageAdapter.getCount(); i++)
        {
            pageAdapter.notifyChangeInPosition(i);
        }

        fragments.clear();

        int count = articles.size();
        for (int i = 0; i < count; i++)
        {
            fragments.add(
                    StoryFragment.newInstance(articles.get(i), i + 1, count)
            );
        }
        pageAdapter.notifyDataSetChanged();
        pager.setCurrentItem(0);
    }

    class NewsReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action == null)
                return;
            if (action.equals(ACTION_NEWS_STORY))
            {
                ArrayList<Article> articles = (ArrayList<Article>) intent.getSerializableExtra(ARTICLES_DATA);
                reDoFragments(articles);
            }
        }
    }

    class NewsPageAdapter extends FragmentPagerAdapter
    {
        private long baseId = 0;

        NewsPageAdapter(FragmentManager fm) { super(fm); }

        @Override
        public int getItemPosition(@NonNull Object object) { return POSITION_NONE; }

        @Override
        public Fragment getItem(int i)
        {
            return fragments.get(i);
        }

        @Override
        public int getCount()
        {
            return fragments.size();
        }

        @Override
        public long getItemId(int position)
        {
            // give an ID different from position when position has been changed
            return baseId + position;
        }

        /**
         * Notify that the position of a fragment has been changed.
         * Create a new ID for each position to force recreation of the fragment
         * @param n number of items which have been changed
         */
        void notifyChangeInPosition(int n)
        {
            // shift the ID returned by getItemId outside the range of all previous fragments
            baseId += getCount() + n;
        }
    }

    private class NewsDrawerAdapter extends ArrayAdapter<String>
    {
        private final Context context;
        private final List<String> text;
        private final int layoutResId;

        public NewsDrawerAdapter(Context context, int layoutResId, List<String> text) {
            super(context, layoutResId, text);
            this.context = context;
            this.text = text;
            this.layoutResId = layoutResId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            View rowView = inflater.inflate(layoutResId, null, true);
            TextView title = rowView.findViewById(R.id.text_view);
            String sourceName = text.get(position);
            int colorId = categoryColor.get(sourceMap.get(sourceName).getCategory());

            SpannableString coloredName = new SpannableString(sourceName);
            coloredName.setSpan(new ForegroundColorSpan(colorId), 0, sourceName.length(), 0);

            title.setText(coloredName);

            return rowView;
        }
    }
}
