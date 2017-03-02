package com.example.siqidong.stocksearch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class ResultHistorical extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ResultActivity activity = (ResultActivity) getActivity();
        final String symbol = activity.getInputText();
        final String historicalData = activity.getHistoricalData();

        View view = inflater.inflate(R.layout.view_historical,container,false);

        final WebView webview = (WebView) view.findViewById(R.id.historicalChart);
        webview.getSettings().setJavaScriptEnabled(true);

        webview.loadUrl("file:///android_asset/chart.html");

        webview.setWebViewClient(new WebViewClient(){
            public void onPageFinished(WebView view, String url){
                webview.loadUrl("javascript:doit('" + historicalData +"','"+symbol+ "')");
            }
        });

        return view;
    }

}



