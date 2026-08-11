package id.kalender.hijriah;

import android.app.*;
import android.content.*;
import android.os.Build;
import com.batoulapps.adhan.PrayerTimes;
import java.time.LocalDate;
import java.util.Date;

public final class PrayerAlarmScheduler {
    private static final int[] INDEX={0,2,3,4,5};
    public static void update(Context c){
        AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        for(int day=0;day<2;day++)for(int pos=0;pos<5;pos++)am.cancel(pending(c,day*10+pos,""));
        if(!c.getSharedPreferences("settings",0).getBoolean("alarm_enabled",false))return;
        long now=System.currentTimeMillis();
        for(int day=0;day<2;day++){
            PrayerTimes t=PrayerUtils.times(c,LocalDate.now().plusDays(day)); Date[] ds=PrayerUtils.dates(t);
            for(int pos=0;pos<5;pos++){
                int idx=INDEX[pos]; long when=ds[idx].getTime(); if(when<=now)continue;
                PendingIntent pi=pending(c,day*10+pos,PrayerUtils.NAMES[idx]);
                if(Build.VERSION.SDK_INT>=31&&!am.canScheduleExactAlarms())am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);
                else am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);
            }
        }
    }
    private static PendingIntent pending(Context c,int code,String name){
        Intent i=new Intent(c,PrayerAlarmReceiver.class).putExtra("prayer",name);
        return PendingIntent.getBroadcast(c,600+code,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
}
