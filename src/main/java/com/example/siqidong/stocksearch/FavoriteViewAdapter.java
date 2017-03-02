package com.example.siqidong.stocksearch;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.graphics.Color;

public class FavoriteViewAdapter extends ArrayAdapter<FavoriteItem> {

    private final Context context;
    private final ArrayList<FavoriteItem> itemsArrayList;

    public FavoriteViewAdapter(Context context, ArrayList<FavoriteItem> itemsArrayList) {

        super(context, R.layout.favorite_list_format, itemsArrayList);

        this.context = context;
        this.itemsArrayList = itemsArrayList;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // 1. Create inflater
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // 2. Get rowView from inflater
        View rowView = inflater.inflate(R.layout.favorite_list_format, parent, false);

        // 3. Get the two text view from the rowView
        TextView symbolView = (TextView) rowView.findViewById(R.id.favoSymbol);
        TextView nameView = (TextView) rowView.findViewById(R.id.favoName);
        TextView priceView = (TextView) rowView.findViewById(R.id.favoPrice);
        TextView changeView = (TextView) rowView.findViewById(R.id.favoChange);
        TextView marketcapView = (TextView) rowView.findViewById(R.id.favoMarketcap);

        // 4. Set the text for textView
        symbolView.setText(itemsArrayList.get(position).getSymbol());
        nameView.setText(itemsArrayList.get(position).getName());
        priceView.setText("$ "+itemsArrayList.get(position).getPrice());

        Double changepercent = itemsArrayList.get(position).getChange();
        if(changepercent >= 0.0) {
            changeView.setText("+"+changepercent+"%");
            changeView.setBackgroundColor(Color.GREEN);
        }
        else {
            changeView.setText("-"+changepercent+"%");
            changeView.setBackgroundColor(Color.RED);
        }
        marketcapView.setText("Market Cap: "+itemsArrayList.get(position).getMarketcap());

        // 5. retrn rowView
        return rowView;
    }
}
