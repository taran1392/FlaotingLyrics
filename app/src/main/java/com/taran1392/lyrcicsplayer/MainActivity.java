package com.taran1392.lyrcicsplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.taran1392.lyrcicsplayer.data.DbHelper;
import com.taran1392.lyrcicsplayer.data.Lyrics;
import com.taran1392.lyrcicsplayer.data.SearchResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
String LOG_TAG="LyricsPlayer";

    BroadcastReceiver broadcastReceiver;

    ListView listView;
    SearchView searchView;

    SimpleAdapter simpleAdapter;
    ArrayList<Map<String,String>> searchResults;
    SearchAycnTask searchAycnTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        listView=(ListView)findViewById(R.id.listView);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if(simpleAdapter.getCount()>i){

                    Map<String,String> map =(Map<String,String>) simpleAdapter.getItem(i);
Log.d(LOG_TAG,"clicked  ");

                    Intent intent=new Intent(MainActivity.this,LyricsActivity.class);
                    intent.putExtra("track",map.get("track"));
                    ///intent.putExtra("album",map.get("album"));
                    intent.putExtra("artist",map.get("artist"));
                    intent.putExtra("url",map.get("url"));
                    startActivity(intent);
                }
            }
        });
        searchView=(SearchView)findViewById(R.id.searchView);


        searchResults=new ArrayList<Map<String,String>>();

        simpleAdapter=new SimpleAdapter(this,searchResults,R.layout.item_list,new String[]{"title","description"},new int[]{R.id.title,R.id.desc });
listView.setAdapter(simpleAdapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if(searchAycnTask!=null)
                    searchAycnTask.cancelTask();

                searchAycnTask=new SearchAycnTask(query,1);
                    searchAycnTask.execute();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

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
