package com.example.nilskerkhof.myapplication;

import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        Chapter[] chapters = parseJsonChapters();
        LinearLayout chaptLay = findViewById(R.id.but_layout);
        for(int i = 0; i<chapters.length; i++){
            Log.v("Ajout du bouton",chapters[i].getTitle());
            Button b = new Button(this);
            b.setText(chapters[i].getTitle());
            b.setId(i);
            chaptLay.addView(b);
        }



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
                allChapters[i] = new Chapter(title, seconds*1000);
            }

        } catch(Exception e){
            Log.e("Error oups", e.getMessage());
        }
        return allChapters;
    }

    public class Chapter{
        private String title;
        private int msTime;

        public Chapter(String title, int msTime) {
            this.title = title;
            this.msTime = msTime;
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
            this.msTime = msTime;
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
            Log.d("ICi","COOOOOOOOONNNNNNNNNNNNNNNNAAAAAAAAAAAARRRRRRRRRRRRDDDDDDDDDDDDDD");
            ex.printStackTrace();
            return json;
        }
        Log.v("PARESEUR",json);
        return json;
    }
}
