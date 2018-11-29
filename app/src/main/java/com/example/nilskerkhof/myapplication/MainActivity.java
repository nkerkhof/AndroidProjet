package com.example.nilskerkhof.myapplication;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private VideoView videoView;
    private MediaController mediaController;
    private Chapter[] chapters ;
    private OurViewModel mModel;
    private WebView myWebView;
    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModel = ViewModelProviders.of(this).get(OurViewModel.class);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        myWebView = findViewById(R.id.webView);
        myWebView.setWebViewClient(new WebViewClient());

        videoView = findViewById(R.id.videoView);


        if(mediaController == null){
            mediaController = new MediaController(MainActivity.this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
        }
        try{
            videoView.setVideoURI(Uri.parse("https://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4"));
            videoView.start();
            videoView.seekTo(mModel.getmCurrentPosition().getValue());
        } catch(Exception e){
            Log.e("Error oups",e.getMessage());
        }

        LinearLayout chaptLay = findViewById(R.id.boutons);
        chapters = parseJsonChapters();
        for(int i = 0; i<chapters.length; i++){
            Button b = new Button(this);
            b.setText(chapters[i].getTitle());
            b.setId(i);
            chaptLay.addView(b);
            chapters[i].setAssocButton(b);
            b.setOnClickListener(new OurClickListener(chapters[i].getMsTime()));
        }


        final Observer<Integer> posObserver = new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable final Integer position) {
                //changeTimeTo(position);
                changeURLTo(position);
                for(Chapter c : chapters){
                    Button b = c.getAssocButton();
                    setUnactiveColorButton(b);
                }
                setActiveColorButton(getButtonByTime(position));
            }
        };
        mModel.getmCurrentPosition().observe(this, posObserver);

        mHandler = new Handler();
        mStatusChecker.run();
    }


    public Button getButtonByTime(int ms){
        return getChapterByTime(ms).getAssocButton();
    }

    public void setActiveColorButton(Button b){
        b.setBackgroundColor(Color.BLACK);
        b.setTextColor(Color.WHITE);
    }

    public void setUnactiveColorButton(Button b){
        b.setBackgroundColor(Color.WHITE);
        b.setTextColor(Color.BLACK);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Chapter[] parseJsonChapters(){
        Chapter allChapters[] = new Chapter[0];
        String jsonChapters = loadJSONFromAsset("chapters.json");
        try{
            JSONObject json = new JSONObject(jsonChapters);
            JSONArray chapters = json.getJSONArray("chapters");
            allChapters = new Chapter[chapters.length()];
            for(int i=0;i<chapters.length();i++){
                JSONObject chap = chapters.getJSONObject(i);
                int mseconds = chap.getInt("start_time")*1000;
                String title = chap.getString("title");
                String url = chap.getString("url");
                allChapters[i] = new Chapter(title, mseconds, url);
            }

        } catch(Exception e){
            Log.e("Error oups", e.getMessage());
        }
        return allChapters;
    }

    public class Chapter{
        private String title;
        private int msTime;
        private Button assocButton;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        private String url;

        public Chapter(String title, int msTime, String url) {
            this.title = title;
            this.msTime = msTime;
            this.url = url;
        }
        public Button getAssocButton() {
            return assocButton;
        }

        public void setAssocButton(Button assocButton) {
            this.assocButton = assocButton;
        }
        public String getTitle() {
            return title;
        }

        public int getMsTime() {
            return msTime;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setMsTime(int msTime) {
            this.msTime = this.msTime;
        }
    }


    public String loadJSONFromAsset(String fileName) {
        String json = null;
        try {
            InputStream is = this.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return json;
        }
        return json;
    }


    public class OurClickListener implements View.OnClickListener {
        int ms;
        public OurClickListener(int milis){
            ms = milis;
        }
        @Override
        public void onClick(View v) {
            changeTimeTo(ms);
            mModel.getmCurrentPosition().setValue(ms);
        }
    }

    public void changeTimeTo(int ms){
        videoView.seekTo(ms);
    }

    public void changeURLTo(int ms){
        String urlToLoad = getChapterByTime(ms).getUrl();
        if(!urlToLoad.equals(myWebView.getOriginalUrl())){
            myWebView.loadUrl(getChapterByTime(ms).getUrl());
        }
    }

    public Chapter getChapterByTime(int ms){
        int worstTime = 99999999;
        Chapter bestC = null;
        for(int i=0; i<chapters.length; i++) {
            int diff = ms - chapters[i].getMsTime();
            if (diff >= 0 && diff < worstTime) {
                worstTime = diff;
                bestC = chapters[i];
            }
        }
        return bestC;
    }
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                mModel.getmCurrentPosition().postValue(videoView.getCurrentPosition());
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception

                mHandler.postDelayed(mStatusChecker, 1000);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mStatusChecker);
    }

}
