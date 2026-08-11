package id.kalender.hijriah;

import android.content.*;
import android.os.Build;

public class PrayerAlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c,Intent intent){
        String name=intent.getStringExtra("prayer"); if(name==null||name.isEmpty())name="Salat";
        Intent playback=new Intent(c,AdhanPlaybackService.class).putExtra("prayer",name);
        try{if(Build.VERSION.SDK_INT>=26)c.startForegroundService(playback);else c.startService(playback);}catch(Exception ignored){}
        PrayerAlarmScheduler.update(c);
    }
}
