package id.kalender.hijriah;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.media.*;
import android.os.*;

public class AdhanPlaybackService extends Service {
    public static final String ACTION_STOP="id.kalender.hijriah.STOP_ADHAN";
    private static final String CHANNEL="adhan_playback_v1";
    private static final int NOTIFICATION_ID=712;
    private MediaPlayer player;

    @Override public void onCreate(){
        super.onCreate();
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel channel=new NotificationChannel(CHANNEL,"Alarm adzan aplikasi",NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Pemutar adzan dengan volume khusus aplikasi");
            channel.setSound(null,null);channel.enableVibration(true);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        }
    }

    @Override public int onStartCommand(Intent intent,int flags,int startId){
        if(intent!=null&&ACTION_STOP.equals(intent.getAction())){stopPlayback();stopSelf();return START_NOT_STICKY;}
        String prayer=intent==null?"Salat":intent.getStringExtra("prayer");
        if(prayer==null||prayer.isEmpty())prayer="Salat";
        startForeground(NOTIFICATION_ID,notification(prayer));
        play();
        return START_NOT_STICKY;
    }

    private Notification notification(String prayer){
        Intent open=new Intent(this,MainActivity.class);
        PendingIntent content=PendingIntent.getActivity(this,71,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Intent stop=new Intent(this,AdhanPlaybackService.class).setAction(ACTION_STOP);
        PendingIntent stopAction=PendingIntent.getService(this,72,stop,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,CHANNEL):new Notification.Builder(this);
        if(Build.VERSION.SDK_INT<26)builder.setPriority(Notification.PRIORITY_HIGH);
        return builder.setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("Waktu "+prayer)
            .setContentText("Adzan sedang diputar").setContentIntent(content).setOngoing(true)
            .setColor(Color.rgb(8,127,91)).addAction(android.R.drawable.ic_media_pause,"Hentikan adzan",stopAction).build();
    }

    private void play(){
        stopPlayback();
        try{
            player=new MediaPlayer();
            player.setWakeMode(this,PowerManager.PARTIAL_WAKE_LOCK);
            player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
            player.setDataSource(this,AdhanAudio.soundUri(this));
            float volume=AdhanAudio.volume(this);player.setVolume(volume,volume);
            player.setOnCompletionListener(mp->{stopPlayback();stopSelf();});
            player.setOnErrorListener((mp,what,extra)->{stopPlayback();stopSelf();return true;});
            player.prepareAsync();player.setOnPreparedListener(MediaPlayer::start);
        }catch(Exception e){stopPlayback();stopSelf();}
    }

    private void stopPlayback(){if(player!=null){try{player.stop();}catch(Exception ignored){}player.release();player=null;}}
    @Override public void onDestroy(){stopPlayback();super.onDestroy();}
    @Override public android.os.IBinder onBind(Intent intent){return null;}
}
