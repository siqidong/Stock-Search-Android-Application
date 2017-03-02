package com.example.siqidong.stocksearch;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.text.Html;
import android.text.method.LinkMovementMethod;

public class NewsViewAdapter extends ArrayAdapter<NewsItem> {

    private final Context context;
    private final ArrayList<NewsItem> itemsArrayList;

    public NewsViewAdapter(Context context, ArrayList<NewsItem> itemsArrayList) {

        super(context, R.layout.news_list_format, itemsArrayList);

        this.context = context;
        this.itemsArrayList = itemsArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // 1. Create inflater
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        View rowView = inflater.inflate(R.layout.news_list_format, parent, false);

        // 3. Get the two text view from the rowView
        TextView titleView = (TextView) rowView.findViewById(R.id.title);
        TextView contentView = (TextView) rowView.findViewById(R.id.content);
        TextView publisherView = (TextView) rowView.findViewById(R.id.publisher);
        TextView dateView = (TextView) rowView.findViewById(R.id.date);

        String title = itemsArrayList.get(position).getTitle();
        String url = itemsArrayList.get(position).getLink();
        titleView.setText(Html.fromHtml("<a href="+url+">"+title));
        titleView.setMovementMethod(LinkMovementMethod.getInstance());

        contentView.setText(itemsArrayList.get(position).getContent());
        publisherView.setText(itemsArrayList.get(position).getPublisher());
        dateView.setText(itemsArrayList.get(position).getDate());

        return rowView;
    }
}
