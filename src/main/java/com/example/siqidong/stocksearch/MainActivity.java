package com.example.siqidong.stocksearch;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import java.util.ArrayList;
import android.util.Log;

import android.widget.EditText;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import android.graphics.Color;
import java.io.BufferedReader;
import android.os.AsyncTask;
import java.net.HttpURLConnection;
import java.net.URL;
import android.content.Intent;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.util.concurrent.ExecutionException;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.text.TextWatcher;
import android.text.Editable;
import org.json.JSONArray;
import org.json.JSONObject;

import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.content.Context;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import android.support.annotation.NonNull;
import android.os.Handler;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final int TENSECONDS = 10000;
    public static final String MYDATA = "myData";
    public static final String DEFAULT = "N/A";
    protected String stockData="";
    protected String newsData = "";
    protected String historicalData = "";
    String inputText="";
    private Toolbar toolbar;
    AutoCompleteTextView auto;
    ArrayList<String> autoResults = new ArrayList<String>();
    ArrayAdapter<String> autoAdapter;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    ArrayList<FavoriteItem> items = new ArrayList<FavoriteItem>();
    FavoriteViewAdapter adapter;
    DynamicListView listView;
    Handler handler = new Handler();
    Runnable runnable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // PART-1 AUTOCOMPLETE
        auto = (AutoCompleteTextView) findViewById(R.id.inputText);
        auto.setThreshold(3);
        auto.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int arg1, int arg2, int arg3) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String userText = auto.getText().toString().trim();
                userText = userText.replace(" ", "+").toLowerCase();
                if(userText.length()>=3) {
                    new FetchAutoComplete(userText).execute();
                }
            }
        });

        //PART-2 CUSTOM TOOLBAR
        toolbar = (Toolbar) findViewById(R.id.maintoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.drawable.mainicon);
        getSupportActionBar().setTitle("  Stock Market Viewer");

        //PART-3 AUTOREFRESH SWITCH CONTROL
        Switch s = (Switch) findViewById(R.id.autoRefreshSwitch);
        assert s != null;
        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                autoRefresh(isChecked);

            }
        });
    }

    private void autoRefresh(boolean isChecked) {
        if(isChecked==true) {
            handler.postDelayed(runnable = new Runnable(){
                public void run(){
                    handler.postDelayed(this, TENSECONDS);
                    for(int i = 0; i<items.size(); i++) {
                        String currentitem = items.get(i).getSymbol();
                        new UpdateData(i,currentitem).execute();
                        showToast("Stock Updated");
                    }
                }
            }, TENSECONDS);
        }
        else {
            handler.removeCallbacks(runnable);
        }
    }

    private void showToast(String s) {
        Toast toast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //PART-4 SET FAVORITE ADAPTER AND DRAW THE LIST
        adapter = new FavoriteViewAdapter(this, generateFavoriteData());

        listView = (DynamicListView) findViewById(R.id.favoriteList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>adapter,View v, int position,long id){
                inputText = ((FavoriteItem)adapter.getItemAtPosition(position)).getSymbol();
                clickFavorite(inputText);
            }
        });

        listView.enableSwipeToDismiss(new OnDismissCallback() {
            @Override
            public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
                for (int position : reverseSortedPositions) {
                    deleteStockMessage(position);
                }
            }
        });
    }

    //USED IN 4, USED TO HANDLE CLICK EVENT IN FAVORITE LIST
    private void clickFavorite(String inputText) {

        try {
            FetchStockData myStockTask = new FetchStockData(this, inputText);
            myStockTask.execute(this).get();
            myStockTask.cancel(true);

            FetchHistoricalData myHistoricalTask = new FetchHistoricalData(this, inputText);
            myHistoricalTask.execute(this).get();
            myHistoricalTask.cancel(true);

            FetchNewsData myNewsTask = new FetchNewsData(this, inputText);
            myNewsTask.execute(this).get();
            myNewsTask.cancel(true);

            Intent intent = new Intent(this, ResultActivity.class);
            Bundle extras = new Bundle();
            extras.putString("inputText", inputText.toUpperCase());
            extras.putString("stockData", stockData);
            extras.putString("historicalData", historicalData);
            extras.putString("newsData", newsData);
            intent.putExtras(extras);
            startActivity(intent);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    //USED IN 4, DISPLAY DELETE FAVORITE ITEM MESSAGE AND CALL DELETE FUNCTION
    private void deleteStockMessage(final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Want to delete "+adapter.getItem(position).getName()+" from favorites?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deleteStock(position);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    //USED IN 4, IF USER CONFIRMS DELETE, HANDLE THIS OPERATION
    private void deleteStock(int stockNum) {

        String thisstock = adapter.getItem(stockNum).getSymbol();

        sp = getSharedPreferences(MYDATA, Context.MODE_PRIVATE);
        String savedData = sp.getString(MYDATA,DEFAULT);
        editor = sp.edit();

        String[] allSaved = savedData.split(",");
        if(allSaved.length==1) {
            editor.putString(MYDATA,DEFAULT);
            editor.commit();
        }
        else {
            int position = 0;
            for(int i=0; i<allSaved.length; i++){
                if(thisstock.equals(allSaved[i])) {
                    position = i;
                    break;
                }
            }
            String newData = "";
            for(int i=0; i<allSaved.length; i++) {
                if(i!=position) {
                    newData += allSaved[i]+",";
                }
            }
            newData = newData.substring(0,newData.length()-1);
            editor.putString(MYDATA,newData);
            editor.commit();
        }

        adapter.remove(adapter.getItem(stockNum));
    }

    // USED IN P4, TO GET THE FAVORITE LIST ADAPTER DATA
    private ArrayList<FavoriteItem> generateFavoriteData(){
        items.clear();
        sp = getSharedPreferences(MYDATA, Context.MODE_PRIVATE);
        String savedData = sp.getString(MYDATA,DEFAULT);
        if(savedData.equals(DEFAULT)) {
            return items;
        }
        else {
            String[] allItem = savedData.split(",");
            for (int i = 0; i < allItem.length; i++) {
                FavoriteItem newItem = null;
                try {
                    newItem = new InitFavorite(allItem[i]).execute().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                items.add(newItem);
            }
        }
        return items;
    }

    //GET QUOTE HANDLER
    public void submitForm(View view) throws ExecutionException, InterruptedException {

        EditText autoText = (EditText)findViewById(R.id.inputText);
        inputText =  autoText.getText().toString().trim();
        if(inputText==null || inputText.equals("")) {
            new AlertDialog.Builder(this)
                    .setMessage("Please enter a Stock Name/Symbol")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    }).show();
        }
        else {
            FetchStockData myStockTask = new FetchStockData(this, inputText);
            myStockTask.execute(this).get();
            myStockTask.cancel(true);

            if (stockData.equals("{\"Status\":\"No\"}")) {
                new AlertDialog.Builder(this)
                        .setMessage("Invalid Symbol")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        }).show();
            }
            else if (stockData.equals("{\"Status\":\"Fail\"}")) {
                new AlertDialog.Builder(this)
                        .setMessage("Stock Information is not available")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        }).show();
            }
            else {
                FetchHistoricalData myHistoricalTask = new FetchHistoricalData(this, inputText);
                myHistoricalTask.execute(this).get();
                myHistoricalTask.cancel(true);

                FetchNewsData myNewsTask = new FetchNewsData(this, inputText);
                myNewsTask.execute(this).get();
                myNewsTask.cancel(true);

                Intent intent = new Intent(this, ResultActivity.class);
                Bundle extras = new Bundle();
                extras.putString("inputText", inputText.toUpperCase());
                extras.putString("stockData", stockData);
                extras.putString("historicalData", historicalData);
                extras.putString("newsData", newsData);
                intent.putExtras(extras);
                startActivity(intent);
            }
        }
    }

    //CLEAR INPUT TEXT
    public void clearForm(View view) {
        EditText autoText = (EditText)findViewById(R.id.inputText);
        autoText.setText("");
    }

    //REFRESH THE FAVORITE LIST DATA
    public void refresh(View view) {
        for(int i = 0; i<items.size(); i++) {
            String currentitem = items.get(i).getSymbol();
            new UpdateData(i,currentitem).execute();
        }
        showToast("Stock Updated");
    }

    //GET FAVORITE LIST ADAPTER ITEM OBJECT
    private class InitFavorite extends AsyncTask<String, Void, FavoriteItem> {

        private String symbol = "";
        private String name = "";
        private String lastprice = "";
        private Double change = 0.0;
        private String marketcap = "";

        InitFavorite(String input){
            symbol = input;
        }

        protected FavoriteItem doInBackground(String... message) {

            URL url;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            JSONObject dataJSON= null;
            try {
                url = new URL("http://stocksearch-1270.appspot.com/?type=getQuote&symbol="+symbol);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                dataJSON = new JSONObject(result.toString());

                name = dataJSON.getString("Name");
                lastprice = dataJSON.getString("LastPrice");
                change = Double.parseDouble(dataJSON.getString("ChangePercent"));
                marketcap = dataJSON.getString("MarketCap");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return new FavoriteItem(symbol,name,lastprice,change,marketcap);
        }

        protected void onPostExecute(String result) {
            return;
        }
    }

    //UPDATE FAVORITE LIST DATA
    private class UpdateData extends AsyncTask<String, Void, String> {

        private String symbol = "";
        private int pos;

        UpdateData(int position, String input){
            pos = position;
            symbol = input;
        }

        protected String doInBackground(String... message) {

            URL url;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            try {
                url = new URL("http://stocksearch-1270.appspot.com/?type=getQuote&symbol="+symbol);

                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return result.toString();
        }

        protected void onPostExecute(String result) {

            JSONObject dataJSON= null;
            try {
                dataJSON = new JSONObject(result);

                String name = dataJSON.getString("Name");
                String lastprice = dataJSON.getString("LastPrice");
                String changepercent = dataJSON.getString("ChangePercent");
                String marketcap = dataJSON.getString("MarketCap");

                View iview = listView.getChildAt(pos);

                TextView iname = (TextView) iview.findViewById(R.id.favoName);
                TextView iprice = (TextView) iview.findViewById(R.id.favoPrice);
                TextView ichange = (TextView) iview.findViewById(R.id.favoChange);
                TextView imarket = (TextView) iview.findViewById(R.id.favoMarketcap);

                iname.setText(name);
                iprice.setText("$ "+lastprice);
                if (changepercent.equals("0.00")) {
                    ichange.setText(changepercent+"%");
                }
                else if(changepercent.charAt(0)=='-') {
                    ichange.setText("-"+changepercent+"%");
                    ichange.setBackgroundColor(Color.RED);
                }
                else {
                    ichange.setText("+"+changepercent+"%");
                    ichange.setBackgroundColor(Color.GREEN);
                }
                imarket.setText("Market Cap: "+marketcap);

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
    }

    //USED IN 1, GET THE AUTOCOMPLETE LIST DATA AND DISPLAY
    private class FetchAutoComplete extends AsyncTask<String, Void, String> {

        private String symbol = "";

        FetchAutoComplete(String input){
            symbol = input;
        }

        protected String doInBackground(String... message) {

            URL url;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();

            try {
                url = new URL("http://stocksearch-1270.appspot.com/?type=lookup&search="+symbol);

                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return result.toString();
        }

        protected void onPostExecute(String result) {

            autoResults.clear();
            try {
                JSONArray jsonArray = new JSONArray(result);

                for(int i = 0; i < jsonArray.length(); i++) {
                    String symbol = jsonArray.getJSONObject(i).getString("Symbol");
                    String name = jsonArray.getJSONObject(i).getString("Name");
                    String exchange = jsonArray.getJSONObject(i).getString("Exchange");
                    autoResults.add(symbol+" - "+name+" ("+exchange+")");
                }

                autoAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.select_dialog_item, autoResults){
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);
                        TextView textView = (TextView) view.findViewById(android.R.id.text1);
                        textView.setTextColor(Color.BLACK);
                        return view;
                    }
                };
                auto.setAdapter(autoAdapter);
                auto.showDropDown();

                auto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String selected = autoAdapter.getItem(position).toString();
                        String[] splitSelected = selected.split(" ");
                        auto.setText(splitSelected[0]);
                    }
                });
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
    }

    //GET STOCK DETAIL DATA
    private class FetchStockData extends AsyncTask<Object, Void, String> {

        private MainActivity activity;
        private String symbol = "";

        FetchStockData(MainActivity view, String input){
            this.activity = view;
            symbol = input;
        }

        protected String doInBackground(Object... message) {

            URL url;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();

            try {
                url = new URL("http://stocksearch-1270.appspot.com/?type=getQuote&symbol="+symbol);

                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            activity.stockData = result.toString();
            return result.toString();
        }

        protected void onPostExecute(String result) {
            return;
        }
    }

    //GET NEWS DATA
    private class FetchNewsData extends AsyncTask<Object, Void, String> {

        private MainActivity activity;
        private String symbol = "";

        FetchNewsData(MainActivity view, String input){
            this.activity = view;
            symbol = input;
        }

        protected String doInBackground(Object... message) {

            URL url;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();

            try {
                url = new URL("http://stocksearch-1270.appspot.com/?type=news&search="+symbol);

                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            activity.newsData = result.toString();
            return result.toString();
        }

        protected void onPostExecute(String result) {
            return;
        }
    }

    //GET HISTORICAL DATA
    private class FetchHistoricalData extends AsyncTask<Object, Void, String> {

        private MainActivity activity;
        private String symbol = "";

        FetchHistoricalData(MainActivity view, String input){
            this.activity = view;
            symbol = input;
        }

        protected String doInBackground(Object... message) {

            URL url;
            HttpURLConnection urlConnection = null;
            StringBuilder result = new StringBuilder();
            String param = "{\"Normalized\":false,\"NumberOfDays\":1095,\"DataPeriod\":\"Day\",\"Elements\":[{\"Symbol\":\""+symbol+"\",\"Type\":\"price\",\"Params\":[\"ohlc\"]}]}";

            try {
                url = new URL("http://stocksearch-1270.appspot.com/?type=chart&symbol="+param);

                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            activity.historicalData = result.toString();
            return result.toString();
        }

        protected void onPostExecute(String result) {
            return;
        }
    }

}
