package com.example.siqidong.stocksearch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.io.InputStream;
import android.widget.ListView;
import android.widget.ImageView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.AsyncTask;
import org.json.*;
import org.json.JSONObject;

import uk.co.senab.photoview.PhotoViewAttacher;
import android.app.AlertDialog;

/**
 * Created by siqidong on 4/22/16.
 */
public class ResultCurrent extends Fragment {

    private String status = "";
    private String name = "";
    private String symbol = "";
    private String lastprice = "";
    private String change = "";
    private String changepercent = "";
    private String timestamp = "";
    private String marketcap = "";
    private String volume = "";
    private String changeytd = "";
    private String changeytdpercent = "";
    private String high = "";
    private String low = "";
    private String open = "";
    private String imgURL = "";
    protected Bitmap myBitmap = null;
    PhotoViewAttacher mAttacher;
    LayoutInflater thisinflater = null;
    ViewGroup thiscontainer = null;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        thisinflater = inflater;
        thiscontainer = container;

        ResultActivity activity = (ResultActivity) getActivity();

        String currentData = activity.getStockData();

        View view = inflater.inflate(R.layout.view_current,container,false);
        CurrentViewAdapter adapter = new CurrentViewAdapter(getActivity(), generateCurrentData(currentData));

        ListView listView = (ListView) view.findViewById(R.id.stockDetailList);
        listView.setAdapter(adapter);

        final ImageView imageView = (ImageView) view.findViewById(R.id.stockChart);
        imgURL = "http://chart.finance.yahoo.com/t?s="+symbol+"&lang=en-US&width=500&height=350";
        new DownloadImageTask(imageView).execute(imgURL);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayYahoo(imageView);
            }
        });
        return view;
    }

    private void displayYahoo(ImageView imageView) {

        ImageView tempImageView = imageView;

        AlertDialog.Builder imageDialog = new AlertDialog.Builder(this.getContext());

        View layout = thisinflater.inflate(R.layout.yahoochart,thiscontainer,false);
        ImageView image = (ImageView) layout.findViewById(R.id.yahoo);
        image.setImageDrawable(tempImageView.getDrawable());
        mAttacher = new PhotoViewAttacher(image);

        imageDialog.setView(layout);

        imageDialog.create();
        imageDialog.show();
    }

    private ArrayList<CurrentItem> generateCurrentData(String dataString){
        ArrayList<CurrentItem> items = new ArrayList<CurrentItem>();

        JSONObject dataJSON= null;
        try {
            dataJSON = new JSONObject(dataString);
            status = dataJSON.getString("Status");

            if(dataJSON.length() > 1) {
                name = dataJSON.getString("Name");
                symbol = dataJSON.getString("Symbol");
                lastprice = dataJSON.getString("LastPrice");
                change = dataJSON.getString("Change");
                changepercent = dataJSON.getString("ChangePercent");
                timestamp = dataJSON.getString("Timestamp");
                marketcap = dataJSON.getString("MarketCap");
                volume = dataJSON.getString("Volume");
                changeytd = dataJSON.getString("ChangeYTD");
                changeytdpercent = dataJSON.getString("ChangePercentYTD");
                high = dataJSON.getString("High");
                low = dataJSON.getString("Low");
                open = dataJSON.getString("Open");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        items.add(new CurrentItem("NAME",name));
        items.add(new CurrentItem("SYMBOL",symbol));
        items.add(new CurrentItem("LASTPRICE","$ "+lastprice));
        double num1 = Double.parseDouble(changepercent);
        if(num1>0.0) {
            items.add(new CurrentItem("CHANGE",change+" (+"+changepercent+"%)"));
        }
        else {
            items.add(new CurrentItem("CHANGE",change+" ("+changepercent+"%)"));
        }
        items.add(new CurrentItem("TIMESTAMP",timestamp.substring(0,timestamp.length()-3)));
        items.add(new CurrentItem("MARKETCAP",marketcap));
        items.add(new CurrentItem("VOLUME",volume));
        double num2 = Double.parseDouble(changeytdpercent);
        if(num2>0.0) {
            items.add(new CurrentItem("CHANGEYTD",changeytd+" (+"+changeytdpercent+"%)"));
        }
        else if (num2<0.0){
            items.add(new CurrentItem("CHANGEYTD",changeytd+" ("+changeytdpercent+"%)"));
        }
        else {
            items.add(new CurrentItem("CHANGEYTD",changeytd+" ("+changeytdpercent+"%)"));
        }
        items.add(new CurrentItem("HIGH","$ "+high));
        items.add(new CurrentItem("LOW","$ "+low));
        items.add(new CurrentItem("OPEN","$ "+open));
        return items;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap imgIcon = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                imgIcon = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            myBitmap = imgIcon;
            return imgIcon;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}