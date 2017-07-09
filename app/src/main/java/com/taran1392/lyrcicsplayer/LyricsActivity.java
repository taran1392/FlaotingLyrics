package com.taran1392.lyrcicsplayer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.taran1392.lyrcicsplayer.data.DbHelper;
import com.taran1392.lyrcicsplayer.data.Lyrics;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

public class LyricsActivity extends AppCompatActivity {

    TextView artistTextView;
    TextView albumTextView,lyricsTextView;
String LOG_TAG="LyricsPlayer";
    CollapsingToolbarLayout collapsingToolbarLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyrics);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                       if(!TextUtils.isEmpty(lyricsTextView.getText())) {
                           Intent i = new Intent(LyricsActivity.this, AddLyricsActivity.class);
                           i.putExtra("lyrics", lyricsTextView.getText());
                            i.putExtra("track",collapsingToolbarLayout.getTitle().toString());
                           startActivity(i);
                       }
            }
        });


        AppBarLayout appBarLayout=(AppBarLayout)findViewById(R.id.main_appbar);

        collapsingToolbarLayout=(CollapsingToolbarLayout)findViewById(R.id.main_collapsing);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                toolbar.setAlpha(Math.abs(verticalOffset / (float)
                        appBarLayout.getTotalScrollRange()));



                if (verticalOffset == -collapsingToolbarLayout.getHeight() + toolbar.getHeight()) {
                    //toolbar is collapsed here
                    //write your code here

                   // collapsingToolbarLayout.setTitle("Collapsed");
                }else{

                    //expanded

                    collapsingToolbarLayout.setTitle( "" +LyricsActivity.this.getIntent().getStringExtra("track"));



                }

            }
        });


        artistTextView=(TextView)findViewById(R.id.artist);
        albumTextView=(TextView)findViewById(R.id.album);

lyricsTextView=(TextView)findViewById(R.id.lyricTextView);

        Intent intent= getIntent();


        collapsingToolbarLayout.setTitle(intent.getStringExtra("track"));
        //albumTextView.setText(intent.getStringExtra("album"));
        artistTextView.setText(intent.getStringExtra("artist"));
        String track=intent.getStringExtra("track");
        String artist=intent.getStringExtra("a");
        //String album=intent.getStringExtra("track");


        LyricsLoadTask lyricsLoadTask=new LyricsLoadTask(track,artist,"",intent.getStringExtra("url"));


        lyricsLoadTask.execute();


    }




    class LyricsLoadTask extends AsyncTask<String,Void,String> {

        boolean cancel;
        final static String rooturl="http://www.azlyrics.com/lyrics/";
        String Lurl;
        String artist,track,album;
        LyricsLoadTask(String track,String artist,String album,String url)
        {
            this.artist=artist;
            this.album=album;
            this.track=track;

         Lurl=url;
            cancel=false;

            Log.d(LOG_TAG,"Task Created ");
        }

        @Override
        protected String doInBackground(String...url) {

            try {

                Lyrics LyricsFromDb= DbHelper.getInstance(LyricsActivity.this).getLyricsByArtist(this.track,this.artist);
                if(LyricsFromDb!=null) {

                    Log.d(LOG_TAG,"Lyrics from DB " );

                    return LyricsFromDb.getLyrics();

                }
                Log.d(LOG_TAG,Lurl);
                Document doc = Jsoup.connect(Lurl).get();


                Elements divs= doc.select("div");
                int i=0;
                if(!cancel) {
                    for (Element e : divs
                            ) {

//        Log.d(LOG_TAG, "Div clASS" + e.className() + " cc ");
                        if(e.className().equals("ringtone")){

                            break;
                        }

                        i++;

                    }


                    Log.d(LOG_TAG,"L found "+divs.get(i+1).html());

                    String lyric=divs.get(i+1).html();

                    lyric=lyric.replaceAll("(?s)<!--.*?-->", "");
                    lyric=lyric.replaceAll("<br>","\n");



                    Lyrics newLyrics=new Lyrics();
                    newLyrics.setArtist(this.artist);
                    newLyrics.setTrack(this.track);
                    newLyrics.setLyrics(lyric);
                    newLyrics.setAlbum(this.album);
                    //add to db
                    DbHelper.getInstance(LyricsActivity.this).insertLyrics(newLyrics);

                    return lyric;

                }
            }catch (Exception e){

                Log.d(LOG_TAG,"Failed to load the page "+e);
                e.printStackTrace();


            }
            return null;
        }


        @Override
        protected void onPostExecute(String o) {
            //super.onPostExecute(o);
            if(!cancel) {
                if (o != null) {

                    lyricsTextView.setText(o);

                } else {

//                    lyricsTextView.setText("Sorry! Failed to get Lyrics for "+this.track);
  //                  searchButton.setVisibility(View.VISIBLE);

                }

            }

        }

        public void cancelTask(){

            cancel=true;
        }

    }


}
