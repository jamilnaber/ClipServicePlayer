package com.example.clipserver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import com.example.ClipCommon.ServiceManger;

import java.util.ArrayList;

public class clipServiceImpl extends Service {
    private static String CHANNEL_ID = "Music player style" ;
    private MediaPlayer mediaPlayer;
    private Notification notification ;
    private static final int NOTIFICATION_ID = 1;
    public String[] clipName;
    public ArrayList<Integer> clipId;
    public static final String TAG = "TESTING1";

    public clipServiceImpl() {
    }

    public void onCreate(){
        super.onCreate();
        this.createNotificationChannel();
        notification = new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true).setContentTitle("Music Playing")
                .setTicker("Music is Playing!")
                .build();
        clipName = getResources().getStringArray(R.array.clip_names);
        clipId = getClipIds();

        startForeground(NOTIFICATION_ID,notification);
    }

    private final ServiceManger.Stub mBinder = new ServiceManger.Stub() {

        @Override
        public int test() throws RemoteException {
            return 5;
        }

        public String[] getSongList() {
            return clipName;
        }

        public void playSongAtId(int i){
            if(mediaPlayer != null && mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }
            mediaPlayer = MediaPlayer.create(clipServiceImpl.this, clipId.get(i));
            if(mediaPlayer != null){
                mediaPlayer.setLooping(false);
                mediaPlayer.start();
            }
            startForeground(NOTIFICATION_ID,notification);
        }

        public void pause(){
            if(mediaPlayer != null){
                mediaPlayer.pause();
            }
        }

        public void resume(){
            if(mediaPlayer != null){
                mediaPlayer.start();
            }
        }

        public void stop(){
            if(mediaPlayer != null){
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer= null;
            }
            stopForeground(true);
            mediaPlayer = null;
        }

        public void closeService(){
            if(mediaPlayer != null){
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer= null;
            }
            stopForeground(true);
            mediaPlayer = null;
            stopSelf();
        }
    };



    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Music Player";
            String description = "Channel for Music Player";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public ArrayList<Integer> getClipIds() {
        ArrayList<Integer> temp = new ArrayList<>();
        temp.add(R.raw.lost);
        temp.add(R.raw.memes);
        temp.add(R.raw.nekozilla);
        temp.add(R.raw.so_happy);
        temp.add(R.raw.you_and_me);
        return temp;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        stopSelf();
        stopForeground(true);
    }
}
