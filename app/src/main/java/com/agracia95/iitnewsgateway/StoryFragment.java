package com.agracia95.iitnewsgateway;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class StoryFragment extends Fragment
{
    private DateFormat df = new SimpleDateFormat("MMM dd, yyyy HH:mm");
    public StoryFragment() {}

    public static StoryFragment newInstance(Article article, int index, int max)
    {
        StoryFragment frag = new StoryFragment();
        Bundle bdl = new Bundle(1);
        bdl.putSerializable("ARTICLE_DATA", article);
        bdl.putInt("INDEX", index);
        bdl.putInt("TOTAL", max);
        frag.setArguments(bdl);
        return frag;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View fragment_layout = inflater.inflate(R.layout.fragment_story, container, false);

        final Article currentArticle = (Article) getArguments().getSerializable("ARTICLE_DATA");
        int index = getArguments().getInt("INDEX");
        int max = getArguments().getInt("TOTAL");

        TextView title = fragment_layout.findViewById(R.id.article_title);
        title.setText(currentArticle.getTitle());
        title.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                goToUrl(currentArticle.getUrl());
            }
        });

        TextView date = fragment_layout.findViewById(R.id.article_date);
        date.setText(df.format(currentArticle.getDate()));

        TextView author = fragment_layout.findViewById(R.id.article_author);
        author.setText(currentArticle.getAuthor());

        ImageView image = fragment_layout.findViewById(R.id.article_image);
        Picasso picasso = new Picasso.Builder(getActivity().getApplicationContext()).build();
        picasso.load(currentArticle.getImageUrl())
                .error(R.drawable.image_not_found)
                .into(image);
        image.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                goToUrl(currentArticle.getUrl());
            }
        });

        TextView description = fragment_layout.findViewById(R.id.article_description);
        description.setText(currentArticle.getDescription());
        description.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                goToUrl(currentArticle.getUrl());
            }
        });

        TextView count = fragment_layout.findViewById(R.id.article_count);
        count.setText(String.format(Locale.getDefault(), "%d of %d", index, max));

        return fragment_layout;
    }


    private void goToUrl(String urlString)
    {
        Uri uri = Uri.parse(urlString);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}
