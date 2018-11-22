package com.example.nilskerkhof.myapplication;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.net.Uri;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;

public class MainActivity extends AppCompatActivity {

    private VideoView videoView;
    private MediaController mediaController;
    private Chapter[] chapters ;
    private OurViewModel mModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModel = ViewModelProviders.of(this).get(OurViewModel.class);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        WebView myWebView = findViewById(R.id.webView);
        myWebView.setWebViewClient(new WebViewClient());
        myWebView.loadUrl("https://en.wikipedia.org/wiki/Big_Buck_Bunny");
        videoView = findViewById(R.id.videoView);
        if(mediaController == null){
            mediaController = new MediaController(MainActivity.this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
        }
        try{
            videoView.setVideoURI(Uri.parse("https://download.blender.org/peach/bigbuckbunny_movies/BigBuckBunny_320x180.mp4"));
        } catch(Exception e){
            Log.e("Error oups",e.getMessage());
        }

        LinearLayout chaptLay = findViewById(R.id.boutons);
        chapters = parseJsonChapters();
        for(int i = 0; i<chapters.length; i++){
            Log.v("Ajout du bouton",chapters[i].getTitle());
            Button b = new Button(this);
            b.setText(chapters[i].getTitle());
            b.setId(i);
            chaptLay.addView(b);
            chapters[i].setAssocButton(b);
            Log.v("CHANGET ",String.valueOf(chapters[i].getsTime()));
            b.setOnClickListener(new OurClickListener(chapters[i].getsTime()*1000));
        }

        final Observer<Integer> posObserver = new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable final Integer position) {
                changeTimeTo(position);
            }
        };

        mModel.getmCurrentPosition().observe(this, posObserver);
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
                int seconds = chap.getInt("start_time");
                String title = chap.getString("title");
                allChapters[i] = new Chapter(title, seconds);
            }

        } catch(Exception e){
            Log.e("Error oups", e.getMessage());
        }
        return allChapters;
    }

    public class Chapter{
        private String title;
        private int sTime;
        private Button assocButton;

        public Chapter(String title, int sTime) {
            this.title = title;
            this.sTime = sTime;
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

        public int getsTime() {
            return sTime;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setsTime(int msTime) {
            this.sTime = sTime;
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
        Log.v("PARESEUR",json);
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
        }
    }

    public boolean changeTimeTo(int ms){
        Log.d("CHANGEMENT A ",String.valueOf(ms));
        videoView.seekTo(ms);
        return true;
    }

}
