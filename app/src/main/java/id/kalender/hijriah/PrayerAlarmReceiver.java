package id.kalender.hijriah;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

public class PrayerAlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c,Intent intent){
        String name=intent.getStringExtra("prayer"); if(name==null||name.isEmpty())name="Salat";
        Uri sound=sound(c); NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
        String channel="prayer_"+Math.abs(sound.toString().hashCode());
        if(Build.VERSION.SDK_INT>=26){NotificationChannel ch=new NotificationChannel(channel,"Alarm waktu salat",NotificationManager.IMPORTANCE_HIGH);ch.setDescription("Pengingat waktu salat");ch.enableVibration(true);ch.setSound(sound,null);nm.createNotificationChannel(ch);}
        Intent open=new Intent(c,MainActivity.class);PendingIntent pi=PendingIntent.getActivity(c,71,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(c,channel):new Notification.Builder(c).setSound(sound);
        b.setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("Waktu "+name).setContentText("Saatnya menunaikan salat "+name).setContentIntent(pi).setAutoCancel(true).setColor(Color.rgb(8,127,91)).setPriority(Notification.PRIORITY_HIGH);
        nm.notify(710,b.build()); PrayerAlarmScheduler.update(c);
    }
    private Uri sound(Context c){
        String mode=c.getSharedPreferences("settings",0).getString("sound_mode","device");
        if("builtin_beautiful".equals(mode))return Uri.parse("android.resource://"+c.getPackageName()+"/"+R.raw.beautiful_adhan);
        if("builtin_doha".equals(mode))return Uri.parse("android.resource://"+c.getPackageName()+"/"+R.raw.doha_adhan);
        if("local".equals(mode)){
            String value=c.getSharedPreferences("settings",0).getString("adhan_uri","");
            return value.isEmpty()?Uri.parse("android.resource://"+c.getPackageName()+"/"+R.raw.beautiful_adhan):Uri.parse(value);
        }
        String value=c.getSharedPreferences("settings",0).getString("ringtone_uri","");
        return value.isEmpty()?RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM):Uri.parse(value);
    }
}
