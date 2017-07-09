package com.taran1392.lyrcicsplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MusicPlayerBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");

        if(intent.getAction().equals("com.android.music.playstatechanged")){

            //start service

            Intent i= new Intent(context.getApplicationContext(),FloatingLyrics.class);
            context.getApplicationContext().startService(i);
            Log.d("LyricsPlayer","Intent received "+intent.getAction());
        }

    }
}
