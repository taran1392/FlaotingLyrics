package com.taran1392.lyrcicsplayer.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by singh_t on 24-05-2017.
 */

public class DbHelper extends SQLiteOpenHelper {
    static String DB_NAME = "LyricsDB";
    static int VERSION = 1;


    final static String TABLE = "Lyrics";

    //columns
    final static String ID = "_ID";
    final static String TRACK = "_track";
    final static String ALBUM = "album";
    final static String ARTIST = "artist";
    final static String LYRICS = "lyrics";


    DbHelper(Context context) {

        super(context, DB_NAME, null, VERSION);

    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {


        String CREATE_POSTS_TABLE = "CREATE TABLE " + TABLE +
                "( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + // Define a primary key
                TRACK + " TEXT ," +
                ALBUM + " TEXT," +
                ARTIST + " TEXT," +
                LYRICS + " TEXT" +
                ")";

        sqLiteDatabase.execSQL(CREATE_POSTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE);
            onCreate(sqLiteDatabase);
        }

    }

    static DbHelper dbHelper;

    public static DbHelper getInstance(Context context) {

        if (dbHelper == null) {

            dbHelper = new DbHelper(context);

        }
        return dbHelper;


    }


    public void insertLyrics(Lyrics lyrics) {

        ContentValues contentValues = new ContentValues();
        //contentValues.put(ID,lyrics.getID());
        contentValues.put(TRACK, lyrics.getTrack().toLowerCase());
        contentValues.put(ALBUM, lyrics.getAlbum());
        contentValues.put(LYRICS, lyrics.getLyrics());
        contentValues.put(ARTIST, lyrics.getArtist());


        try {

            SQLiteDatabase db = getWritableDatabase();
            int rows = db.update(TABLE, contentValues, TRACK + " LIKE ?", new String[]{"%" + lyrics.getTrack()+"%"});

            if (rows == 1) {
                //update successful

                Log.d(LOG_TAG, " Updatating lyrics " +lyrics.getTrack()+ " result "+rows);
                db.close();

            } else {

                //insert new

                long r= db.insert(TABLE, null, contentValues);


                Log.d(LOG_TAG, " inserting lyrics " +lyrics.getTrack()+ " result "+r);
            }

            //


        } catch (Exception e) {


            Log.d(LOG_TAG, "Failed to insert or update Lyrics " + e);
        }

    }


    final static String LOG_TAG = "LyricsPlayer";



    public Lyrics getLyricsByTitle(String track) {

        try {
            Lyrics lyrics = new Lyrics();

            SQLiteDatabase db = getReadableDatabase();


            track= track.replaceAll("'","''");

            Cursor cursor = db.rawQuery(String.format("Select * from %s where %s LIKE '%s'", TABLE, TRACK, "%"+track+"%"), null);
            Log.d(LOG_TAG,"track "+track);
            Log.d(LOG_TAG,"query "+String.format("Select * from %s where %s='%s'", TABLE, TRACK, "%"+track+"%"));
            if (cursor.getCount() > 0) {

                cursor.moveToFirst();
                lyrics.setID(cursor.getInt(cursor.getColumnIndex(ID)));
                lyrics.setAlbum(cursor.getString(cursor.getColumnIndex(ALBUM)));
                lyrics.setTrack(cursor.getString(cursor.getColumnIndex(TRACK)));
                lyrics.setArtist(cursor.getString(cursor.getColumnIndex(ARTIST)));
                lyrics.setLyrics(cursor.getString(cursor.getColumnIndex(LYRICS)));

                return lyrics;
            }


            return null;
        } catch (Exception e) {

            Log.d(LOG_TAG,"Failed to search lyrics "+e);

            return null;
        }


    }


    public Lyrics getLyrics(String track, String album) {

        try {
            Lyrics lyrics = new Lyrics();

            SQLiteDatabase db = getReadableDatabase();
                track=track.replaceAll("'","''");
            album=album.replaceAll("'","''");

            Cursor cursor = db.rawQuery(String.format("Select * from %s where %s LIKE '%s' and %s LIKE '%s'", TABLE, TRACK, "%"+track+"%", ALBUM, "%"+album+"%"), null);

            Log.d(LOG_TAG,"track "+track+"  album "+album);
            if (cursor.getCount() > 0) {

                cursor.moveToFirst();
                lyrics.setID(cursor.getInt(cursor.getColumnIndex(ID)));
                lyrics.setAlbum(cursor.getString(cursor.getColumnIndex(ALBUM)));
                lyrics.setTrack(cursor.getString(cursor.getColumnIndex(TRACK)));
                lyrics.setArtist(cursor.getString(cursor.getColumnIndex(ARTIST)));
                lyrics.setLyrics(cursor.getString(cursor.getColumnIndex(LYRICS)));

                return lyrics;
            }


            return getLyricsByTitle(track);
        } catch (Exception e) {

            Log.d(LOG_TAG,"Failed to search lyrics "+e);

            return null;
        }


    }




    public Lyrics getLyricsByArtist(String track, String artist) {

        try {
            Lyrics lyrics = new Lyrics();

            SQLiteDatabase db = getReadableDatabase();


            Cursor cursor = db.rawQuery(String.format("Select * from %s where %s='%s' and %s='%s'", TABLE, TRACK, track, ARTIST, artist), null);

            if (cursor.getCount() > 0) {

                cursor.moveToFirst();
                lyrics.setID(cursor.getInt(cursor.getColumnIndex(ID)));
                lyrics.setAlbum(cursor.getString(cursor.getColumnIndex(ALBUM)));
                lyrics.setTrack(cursor.getString(cursor.getColumnIndex(TRACK)));
                lyrics.setArtist(cursor.getString(cursor.getColumnIndex(ARTIST)));
                lyrics.setLyrics(cursor.getString(cursor.getColumnIndex(LYRICS)));

                return lyrics;
            }


            return null;
        } catch (Exception e) {

            Log.d(LOG_TAG,"Failed to search lyrics "+e);

            return null;
        }


    }
}