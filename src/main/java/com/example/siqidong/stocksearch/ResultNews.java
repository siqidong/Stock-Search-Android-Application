package com.example.siqidong.stocksearch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import android.widget.ListView;

import org.json.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class ResultNews extends Fragment {


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ResultActivity activity = (ResultActivity) getActivity();
        String newsData = activity.getNewsData();

        View view = inflater.inflate(R.layout.view_news,container,false);
        NewsViewAdapter adapter = new NewsViewAdapter(getActivity(), generateNewsData(newsData));

        ListView listView = (ListView) view.findViewById(R.id.newsList);
        listView.setAdapter(adapter);

        return  view;
    }

    private ArrayList<NewsItem> generateNewsData(String dataString){
        ArrayList<NewsItem> items = new ArrayList<NewsItem>();

        JSONObject dataJSON= null;
        try {
            dataJSON = new JSONObject(dataString);
            JSONObject dataD = (JSONObject) dataJSON.get("d");
            JSONArray dataR = dataD.optJSONArray("results");

            int newsNum = dataR.length();

            for(int i = 0; i < newsNum; i++) {
                JSONObject data = dataR.getJSONObject(i);

                String title = data.getString("Title");
                String link = data.getString("Url");
                String content = data.getString("Description");
                String publisher = "Publisher: "+data.getString("Source");
                String date = data.getString("Date");

                items.add(new NewsItem(title, link, content, publisher, date));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return items;
    }
}