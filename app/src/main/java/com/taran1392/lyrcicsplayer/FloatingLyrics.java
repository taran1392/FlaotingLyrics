package com.taran1392.lyrcicsplayer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;



import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.taran1392.lyrcicsplayer.data.DbHelper;
import com.taran1392.lyrcicsplayer.data.Lyrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.*;
import org.jsoup.select.Elements;

public class FloatingLyrics extends Service {

    BroadcastReceiver broadcastReceiver;



    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {


    Log.d(LOG_TAG,"Service on Start Command frfeceieve");
        return super.onStartCommand(intent, flags, startId);


    }


    LyricsLoadTask atask;
    String LOG_TAG ="LyricsPlayer";
    TextView artist,track,album,lyricsTextView;


    private WindowManager mWindowManager;
        private View mFloatingView,mBottomView;
        WindowManager.LayoutParams params,params2;

    Button searchButton;
        int height;


        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {
            super.onCreate();


Log.d(LOG_TAG,"Service created");
            broadcastReceiver=new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {


                    //Toast.makeText(MainActivity.this,"Intent Recieved "+intent.getAction(),Toast.LENGTH_LONG).show();

                    Bundle bundle = intent.getExtras();
                    if (bundle != null) {
                        Set<String> keys = bundle.keySet();
                        Iterator<String> it = keys.iterator();
                        Log.e(LOG_TAG,"Dumping Intent start");
                        /*while (it.hasNext()) {
                            String key = it.next();
                          //  Log.d(LOG_TAG,"[" + key + "=" + bundle.get(key)+"]");
                        }*/
                        Log.d(LOG_TAG,"Dumping Intent end");
                    }




                    track.setText( bundle.getString("track","NA"));

                    album.setText( bundle.getString("album","NA"));

                    artist.setText( bundle.getString("artist","NA"));


                    if(atask!=null)
                        atask.cancelTask();

                         mFloatingView.findViewById(R.id.search_button).setVisibility(View.GONE);
                        lyricsTextView.setText("Fetching Lyrics for "+bundle.getString("track","NA"));
                    atask=new LyricsLoadTask( bundle.getString("track","NA").toLowerCase(), bundle.getString("artist","NA").toLowerCase(),album.getText().toString());
                    atask.execute();


                }
            };

            IntentFilter filter=new IntentFilter();
            filter.addAction("com.android.music.metachanged");




            //Inflate the floating view layout we created
            mFloatingView = LayoutInflater.from(this).inflate(R.layout.floatingwidget_layout, null);
            mBottomView = LayoutInflater.from(this).inflate(R.layout.bottom, null);


            searchButton=(Button)mFloatingView.findViewById(R.id.search_button);

            //Add the view to the window.
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            //Specify the view position
            //params.gravity = Gravity.TOP | Gravity.LEFT;        //Initially view will be added to top-left corner
            params.x = 0;
            params.y = 100;






            //Add the view to the window
            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);


            height=getScreenHeight(mWindowManager);

            mBottomView.setVisibility(View.GONE);

            params2 = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            //Specify the view position
            params2.gravity = Gravity.BOTTOM | Gravity.CENTER;        //Initially view will be added to top-left corner
            params2.x = 0;
            //params2.y = 100;

            mWindowManager.addView(mBottomView,params2);
            mWindowManager.addView(mFloatingView, params);

            //mWindowManager.getDefaultDisplay().getHeight();




//The root element of the collapsed view layout
            final View collapsedView = mFloatingView.findViewById(R.id.collapse_view);
//The root element of the expanded view layout
            final View expandedView = mFloatingView.findViewById(R.id.expanded_view);




            artist=(TextView)expandedView.findViewById(R.id.artist);
            album=(TextView)expandedView.findViewById(R.id.album);

            track=(TextView)expandedView.findViewById(R.id.track);
            lyricsTextView=(TextView)expandedView.findViewById(R.id.lyricTextView);
//Set the close button
            ImageView closeButtonCollapsed = (ImageView) mFloatingView.findViewById(R.id.close_btn);
            closeButtonCollapsed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //close the service and remove the from from the window
                    stopSelf();
                }
            });



            mFloatingView.findViewById(R.id.collapse_view).setOnTouchListener(new View.OnTouchListener() {
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;


                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:


                            //remember the initial position.
                            initialX = params.x;
                            initialY = params.y;


                            //get the touch location
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();


                            //

                            return true;
                        case MotionEvent.ACTION_MOVE:
                            //Calculate the X and Y coordinates of the view.


                            params.x = initialX + (int) (event.getRawX() - initialTouchX);
                            params.y = initialY + (int) (event.getRawY() - initialTouchY);

                            int deltaWidth=(int) (event.getRawX() - initialTouchX);
                            int deltaHeight=(int) (event.getRawX() - initialTouchX);



                            // collapsedView.setP
                            mBottomView.setVisibility(View.VISIBLE);

                            if(event.getRawY() > height-mBottomView.getHeight()){
                                //collapsedView.setForeground();
                                collapsedView.setBackground(new ColorDrawable( 0x79eb2727 ));

                            }else{

                                collapsedView.setBackground(new ColorDrawable(Color.TRANSPARENT));


                            }

                            //params.width=params.width+deltaWidth;
                            //params.height=params.height+deltaHeight;

                            Log.d(LOG_TAG,"new width"+params.width+deltaWidth+"");

                            Log.d(LOG_TAG,"new Height"+params.height+deltaHeight+"");
                            //Update the layout with new X & Y coordinate
                            mWindowManager.updateViewLayout(mFloatingView, params );



                            //mFloatingView.findViewById(R.id.bottom_view).setVisibility(View.VISIBLE);
                            return true;

                        case MotionEvent.ACTION_UP:
                            int Xdiff = (int) (event.getRawX() - initialTouchX);
                            int Ydiff = (int) (event.getRawY() - initialTouchY);



                            //mWindowManager.removeView(mBottomView);
                            mBottomView.setVisibility(View.GONE);


                           // mFloatingView.findViewById(R.id.root_container).getLayoutParams().width= WindowManager.LayoutParams.WRAP_CONTENT;
                            ///mFloatingView.getLayoutParams().width= WindowManager.LayoutParams.WRAP_CONTENT;

                           // params.width=WindowManager.LayoutParams.WRAP_CONTENT;
                            //mFloatingView.findViewById(R.id.bottom_view).setVisibility(View.GONE);
                            mWindowManager.updateViewLayout(mFloatingView,params);

                            //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                            //So that is click event.
                            if (Xdiff < 10 && Ydiff < 10) {


                                if (isViewCollapsed()) {
                                    //When user clicks on the image view of the collapsed layout,
                                    //visibility of the collapsed layout will be changed to "View.GONE"
                                    //and expanded view will become visible.

                                    if(event.getRawY() > height-mBottomView.getHeight()){
                                        stopSelf();




                                    }
                                    //collapsedView.setVisibility(View.GONE);
                                    // expandedView.setVisibility(View.VISIBLE);
                                }





                                if(expandedView.getVisibility()==View.VISIBLE){

                                    expandedView.setVisibility(View.GONE);
                                }else{
                                    expandedView.setVisibility(View.VISIBLE);



                                }
                            }








                            return true;
                    }
                    return false;
                }
            });


            registerReceiver( broadcastReceiver,filter);



        }

        public int getScreenHeight(WindowManager wm){
            Display display = wm.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;

            //return "{" + width + "," + height + "}";
            return height;


        }
        private boolean isViewCollapsed() {
            return mFloatingView == null || mFloatingView.findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
        }
        @Override
        public void onDestroy() {
            super.onDestroy();
            if (mFloatingView != null) mWindowManager.removeView(mFloatingView);
            unregisterReceiver(broadcastReceiver);

        }



        class LyricsLoadTask extends AsyncTask<String,Void,String>{

             boolean cancel;
            final static String rooturl="http://www.azlyrics.com/lyrics/";
            String Lurl;
            String artist,track,album;
            LyricsLoadTask(String track,String artist,String album)
            {
                    this.artist=artist;
                this.album=album;
                this.track=track;
               String track2= track.replaceAll("\\s+","");
                String artist2= artist.replaceAll("\\s+","");

                track2= track2.replaceAll("'","");
                    Lurl=rooturl+artist2+"/"+track2+".html";
                    cancel=false;

                Log.d(LOG_TAG,"Task Created ");
            }

            @Override
            protected String doInBackground(String...url) {

                try {

                    Lyrics LyricsFromDb= DbHelper.getInstance(FloatingLyrics.this).getLyrics(this.track,this.album);
                    if(LyricsFromDb!=null) {

                     Log.d(LOG_TAG,"Lyrics from DB " );

                        return LyricsFromDb.getLyrics();

                    }
                    Thread.sleep(1000);
                    if(cancel)
                        return  null;
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
    DbHelper.getInstance(FloatingLyrics.this).insertLyrics(newLyrics);

    return lyric;

}
                }catch (Exception e){

                            Log.d(LOG_TAG,"Failed to load the page "+e);


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

                        lyricsTextView.setText("Sorry! Failed to get Lyrics for "+this.track);
                        searchButton.setVisibility(View.VISIBLE);

                    }

                }

            }

              public void cancelTask(){

                  cancel=true;
              }

        }

    class SearchAycnTask extends AsyncTask<String,Void,ArrayList<Map<String,String>>> {

        boolean cancel;
        final static String rooturl="http://search.azlyrics.com/search.php?w=songs&q=";
        String Lurl;
        String searchString;
        ArrayList<Map<String,String>> searchResults;
        SearchAycnTask(String search,int page)
        {

            searchString=search;
            Lurl=rooturl+search+"&page="+page;
            cancel=false;

            this.searchResults=new ArrayList<Map<String,String>>();
            Log.d(LOG_TAG,"Task Created ");
        }

        @Override
        protected ArrayList<Map <String,String>> doInBackground(String...url) {



            try {

                Log.d(LOG_TAG,Lurl);
                Document doc = Jsoup.connect(Lurl).get();


                Elements tdCells= doc.select("td");
                int i=0;
                if(!cancel) {
                    for (Element td : tdCells
                            ) {

//        Log.d(LOG_TAG, "Div clASS" + e.className() + " cc ");
                        if (td.className().equals("text-left visitedlyr")) {

                            String track=td.select("a").text();
                            String lyricsUrl =td.select("a").attr("href");
                            String artist=td.select("b").get(1).text();
                            String smallLyrics=td.select("small").text();

                            Log.d(LOG_TAG,track +" By "+artist);
                            Map<String,String> map=new HashMap<String,String>();

                            map.put("title",track +" By "+artist);
                            map.put("description",smallLyrics);
                            map.put("artist",artist);
                            map.put("url",lyricsUrl);
                            map.put("track",track);

                            this.searchResults.add(map);
                        }


                    }

                    return this.searchResults;
                }
            }catch (Exception e){

                Log.d(LOG_TAG,"Failed to load the page "+e);


            }
            return null;
        }


        @Override
        protected void onPostExecute(ArrayList<Map<String,String>> o) {
            //super.onPostExecute(o);
            if(!cancel) {
                if (o != null) {

                    Log.d(LOG_TAG,"Search results received "+o.size());
                    simpleAdapter=new SimpleAdapter(MainActivity.this,o,R.layout.item_list,new String[]{"title","description"},new int[]{R.id.title,R.id.desc });
                    listView.setAdapter(simpleAdapter);

                } else {


                }

            }

        }

        public void cancelTask(){

            cancel=true;
        }

    }


}













