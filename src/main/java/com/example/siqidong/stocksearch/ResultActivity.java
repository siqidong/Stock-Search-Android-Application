package com.example.siqidong.stocksearch;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import org.json.*;
import org.json.JSONObject;
import android.support.v7.widget.Toolbar;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.facebook.FacebookSdk;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import android.util.Log;
import com.facebook.login.LoginManager;
import com.facebook.share.model.ShareLinkContent;
import android.net.Uri;
import java.util.Arrays;
import com.facebook.share.widget.ShareDialog;

public class ResultActivity extends AppCompatActivity {

    public static final String MYDATA = "myData";
    public static final String DEFAULT = "N/A";
    TabLayout tabLayout;
    MyViewPager viewPager;
    private String inputText = "";
    private String stockData = "";
    private String historicalData = "";
    private String newsData = "";
    private String companyName = "";
    private String companyPrice = "";
    private Toolbar toolbar;
    CallbackManager callbackManager;
    LoginManager manager;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    int clickNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_result);

        Bundle extras = getIntent().getExtras();
        if(extras==null) {
            return;
        }
        inputText = extras.getString("inputText");
        stockData = extras.getString("stockData");
        historicalData = extras.getString("historicalData");
        newsData = extras.getString("newsData");

        setCompanyInfo(stockData);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(companyName);

        ImageButton facebookButton = (ImageButton) toolbar.findViewById(R.id.fbButton);
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postFacebook();
            }
        });

        boolean ifSaved = checkFavoStatus(inputText);
        ToggleButton starButton = (ToggleButton) toolbar.findViewById(R.id.starButton);
        if(ifSaved==true) {
            clickNum = 0;
            starButton.setChecked(true);
        }
        else {
            clickNum = 1;
            starButton.setChecked(false);
        }
        starButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickNum++;
                changeFavorite(clickNum);
            }
        });

        viewPager = (MyViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(new CustomPagerAdapter(getSupportFragmentManager()));

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private class CustomPagerAdapter extends FragmentPagerAdapter {

        private String fragments [] = {"CURRENT","HISTORICAL","NEWS"};

        public CustomPagerAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new ResultCurrent();
                case 1:
                    return new ResultHistorical();
                case 2:
                    return new ResultNews();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragments[position];
        }

    }

    public void setCompanyInfo(String stockData) {

        JSONObject dataJSON= null;
        try {
            dataJSON = new JSONObject(stockData);
            companyName = dataJSON.getString("Name");
            companyPrice = dataJSON.getString("LastPrice");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void postFacebook() {
        if(AccessToken.getCurrentAccessToken()!=null) {
            callbackManager = CallbackManager.Factory.create();
            manager = LoginManager.getInstance();
            facebookShare();
        }
        else {
            callbackManager = CallbackManager.Factory.create();
            manager = LoginManager.getInstance();
            manager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
            manager.registerCallback(callbackManager,
                    new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            facebookShare();
                        }

                        @Override
                        public void onCancel() {
                        }

                        @Override
                        public void onError(FacebookException exception) {
                            showToast("Error happened during connection");
                        }
                    });
        }
    }

    public void changeFavorite(int click) {
        sp = getSharedPreferences(MYDATA, Context.MODE_PRIVATE);
        String savedData = sp.getString(MYDATA,DEFAULT);
        editor = sp.edit();
        //click is even number, not saved, save it
        if(click%2==0) {
            if(savedData.equals(DEFAULT)) {
                editor.putString(MYDATA,inputText);
                editor.commit();
                return;
            }
            else {
                String newData = savedData+","+inputText;
                editor.putString(MYDATA,newData);
                editor.commit();
                return;
            }
        }//click is odd number, already saved, delete it
        else {
            String[] allSaved = savedData.split(",");
            if(allSaved.length==1) {
                editor.putString(MYDATA,DEFAULT);
                editor.commit();
                return;
            }
            else {
                int position = 0;
                for(int i=0; i<allSaved.length; i++){
                    if(inputText.equals(allSaved[i])) {
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
                return;
            }
        }
    }

    private boolean checkFavoStatus(String stockSymbol) {
        sp = getSharedPreferences(MYDATA, Context.MODE_PRIVATE);

        String savedData = sp.getString(MYDATA,DEFAULT);
        if(savedData.equals(DEFAULT)) {
            return false;
        }
        else {
            String[] allSaved = savedData.split(",");
            for(int i=0; i<allSaved.length; i++){
                if(stockSymbol.equals(allSaved[i])) {
                    return true;
                }
            }
            return false;
        }
    }

    public String getInputText() {
        return inputText;
    }

    public String getStockData() {
        return stockData;
    }

    public String getHistoricalData() {
        return historicalData;
    }

    public String getNewsData() {
        return newsData;
    }

    public void showToast(String s) {
        Toast toast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void facebookShare(){

        showToast("Sharing "+ companyName + "!");

        ShareDialog shareDialog = new ShareDialog(this);

        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentTitle("Current Stock Price of "+companyName+" $"+companyPrice)
                    .setContentDescription("Stock Information of "+companyName)
                    .setContentUrl(Uri.parse("http://finance.yahoo.com/q?s="+inputText))
                    .setImageUrl(Uri.parse("http://chart.finance.yahoo.com/t?s="+inputText+"&lang=en-US&width=350&height=250"))
                    .build();

            shareDialog.show(linkContent);
        }

    }

}
