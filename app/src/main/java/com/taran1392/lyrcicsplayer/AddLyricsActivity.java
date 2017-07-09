package com.taran1392.lyrcicsplayer;

import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;


import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.support.v7.widget.SearchView;
import android.widget.Toast;

import com.taran1392.lyrcicsplayer.data.DbHelper;
import com.taran1392.lyrcicsplayer.data.Lyrics;

public class AddLyricsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    SimpleCursorAdapter simpleCursorAdapter;

    ListView listView;
    String lyrics,track;
    SearchView searchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lyrics);


        listView=(ListView)findViewById(R.id.songListView);

        lyrics=getIntent().getStringExtra("lyrics");
        track=getIntent().getStringExtra("track");

        Bundle bundle=new Bundle();
        bundle.putString("search",track);
        getSupportLoaderManager().initLoader(1,bundle,this);
searchView=(SearchView) findViewById(R.id.SearchView);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                getSupportLoaderManager().initLoader(1,null,AddLyricsActivity.this);


                Bundle bundle=new Bundle();
                bundle.putString("search",s);
                getSupportLoaderManager().restartLoader(1,bundle,AddLyricsActivity.this);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        //simpleCursorAdapter.setFilterQueryProvider();


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Cursor cursor=simpleCursorAdapter.getCursor();
                cursor.moveToPosition(i);

                String TRACK= cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                String ALBUM= cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String ARTIST= cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

//TRACK.substring()

                Lyrics newLyrics=new Lyrics();
                newLyrics.setAlbum(ALBUM);
                newLyrics.setArtist(ARTIST);
                newLyrics.setTrack(TRACK);
                newLyrics.setLyrics(lyrics);

                DbHelper.getInstance(AddLyricsActivity.this).insertLyrics(newLyrics);
                Toast.makeText(AddLyricsActivity.this,"Lyrics have been added to Db",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

       // Cursor cursor=getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI ,new String[]{MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.ALBUM },null, );
       if(!args.getString("search","").equals("")) {
            String search=args.getString("search");
           return new CursorLoader(this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media._ID}, MediaStore.Audio.Media.DISPLAY_NAME+" LIKE ?", new String[]{"%" + search + "%"}, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
       }
       else{

           return new CursorLoader(this, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media._ID}, null,null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

       }
        //return cursor;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        simpleCursorAdapter=new SimpleCursorAdapter(this,R.layout.item_song,data,new String[]{MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.DISPLAY_NAME ,MediaStore.Audio.Media._ID},new int[]{R.id.artist,R.id.track});


        listView.setAdapter(simpleCursorAdapter);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
