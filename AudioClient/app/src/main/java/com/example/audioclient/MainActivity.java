package com.example.audioclient;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ClipCommon.ServiceManger;

import org.w3c.dom.Text;


public class MainActivity extends AppCompatActivity {

    Button btnStartService;
    Button btnPause;
    Button btnResume;
    Button btnStopPlayback;
    Button btnStopService;
    Button btnPlay;
    ListView clipView;
    TextView displayMessage;
    Intent adliIntent;
    ResolveInfo info;
    ComponentName c;
    String songName;
    private boolean mIsBound = false;
    private ServiceManger mServiceManger;
    public static final String TAG = "TESTING1";
    int selectSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartService = (Button) findViewById(R.id.btn_startService);
        btnPause = (Button) findViewById(R.id.btn_pause);
        btnResume = (Button) findViewById(R.id.btn_resume);
        btnStopPlayback = (Button) findViewById(R.id.btn_stopPlayBack);
        btnStopService = (Button) findViewById(R.id.btn_stopService);
        btnPlay = (Button) findViewById(R.id.btn_play);
        displayMessage = (TextView) findViewById(R.id.txt_des);
        clipView = (ListView) findViewById(R.id.clip_list_view);

        btnStartService.setText("Start Service");
        btnStartService.setOnClickListener(startService);

        btnPause.setEnabled(false);
        btnPause.setText("Pause");
        btnPause.setOnClickListener(pauseSong);

        btnResume.setEnabled(false);
        btnResume.setText("Resume");
        btnResume.setOnClickListener(resumeSong);

        btnStopPlayback.setEnabled(false);
        btnStopPlayback.setText("Stop Playback");
        btnStopPlayback.setOnClickListener(stopPlayback);

        btnPlay.setEnabled(false);
        btnPlay.setText("Play");
        btnPlay.setOnClickListener(playSong);

        btnStopService.setEnabled(false);
        btnStopService.setText("Stop Service");
        btnStopService.setOnClickListener(serviceStop);

        displayMessage.setText("Start The Service");
        clipView.setOnItemClickListener(itemLocation);
    }


    private View.OnClickListener startService = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onClick(View view) {
            boolean b;
            adliIntent = new Intent(ServiceManger.class.getName());
            info = getPackageManager().resolveService(adliIntent,0);
            c = new ComponentName(info.serviceInfo.packageName,info.serviceInfo.name);
            adliIntent.setComponent(c);
            getApplicationContext().startForegroundService(adliIntent);
            b = getApplicationContext().bindService(adliIntent, MainActivity.this.mConnection , Context.BIND_AUTO_CREATE);
            if (b) {
                btnStartService.setEnabled(false);
                btnStopService.setEnabled(true);
                displayMessage.setText("Select A Song Below");
                clipView.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.clip_names)));
            }
        }
    };


    public AdapterView.OnItemClickListener itemLocation = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            selectSong = i;
            songName = (String) ((TextView) view).getText();
            displayMessage.setText("Selected: " + songName);
            btnPlay.setEnabled(true);
        }
    };


    private View.OnClickListener pauseSong = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                mServiceManger.pause();
                displayMessage.setText("Paused: " + songName);
                btnPause.setEnabled(false);
                btnResume.setEnabled(true);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };


    private View.OnClickListener resumeSong = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                mServiceManger.resume();
                displayMessage.setText("Resumed: " + songName);
                btnPause.setEnabled(true);
                btnResume.setEnabled(false);
            } catch (RemoteException e) { e.printStackTrace(); }
        }
    };


    private View.OnClickListener stopPlayback = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try { mServiceManger.stop();} catch (RemoteException e) { e.printStackTrace(); }
            getApplicationContext().unbindService(mConnection);
            mIsBound = false;
            displayMessage.setText("Playback Stopped");
            btnResume.setEnabled(false);
            btnPause.setEnabled(false);
            btnStopPlayback.setEnabled(false);
        }
    };


    private View.OnClickListener playSong = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(!mIsBound) { getApplicationContext().bindService(adliIntent, MainActivity.this.mConnection, Context.BIND_AUTO_CREATE); }
            try {
                mServiceManger.playSongAtId(selectSong);
                displayMessage.setText("Playing: " + songName);
                btnPause.setEnabled(true);
                btnStopPlayback.setEnabled(true);
                btnPlay.setEnabled(false);
            } catch (RemoteException e) { e.printStackTrace(); }

        }
    };


    private View.OnClickListener serviceStop = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(mIsBound){ getApplicationContext().unbindService(mConnection); }
            stopService(adliIntent);
            mIsBound = false;
            displayMessage.setText("Service Stopped");
            btnResume.setEnabled(false);
            btnPause.setEnabled(false);
            btnStopPlayback.setEnabled(false);
            btnPlay.setEnabled(false);
            btnStopService.setEnabled(false);
            btnStartService.setEnabled(true);
            clipView.setAdapter(null);
        }
    };


    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder iservice) {
            mServiceManger = ServiceManger.Stub.asInterface(iservice);
            mIsBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {

            mServiceManger = null;
            mIsBound = false;
        }
    };

    @Override
    public void onDestroy(){
        Log.i(TAG, Boolean.toString(mIsBound));
        try { mServiceManger.closeService(); } catch (RemoteException e) { e.printStackTrace(); }
        if(mIsBound){ getApplicationContext().unbindService(mConnection); }
        stopService(adliIntent);
        super.onDestroy();
    }

}