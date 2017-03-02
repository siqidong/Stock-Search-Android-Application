package com.example.siqidong.stocksearch;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CurrentViewAdapter extends ArrayAdapter<CurrentItem> {

    private final Context context;
    private final ArrayList<CurrentItem> itemsArrayList;

    public CurrentViewAdapter(Context context, ArrayList<CurrentItem> itemsArrayList) {

        super(context, R.layout.stock_detail_list_format, itemsArrayList);

        this.context = context;
        this.itemsArrayList = itemsArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.stock_detail_list_format, parent, false);

        TextView labelView = (TextView) rowView.findViewById(R.id.label);
        TextView valueView = (TextView) rowView.findViewById(R.id.value);

        String title = itemsArrayList.get(position).getTitle();
        String description = itemsArrayList.get(position).getDescription();
        labelView.setText(title);
        valueView.setText(description);

        if(title.equals("CHANGE") || title.equals("CHANGEYTD")) {
            if(description.contains("+")) {
                valueView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.stockup, 0);
            }
            if (description.contains("-")) {
                valueView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.stockdown, 0);
            }
        }

        return rowView;
    }

}