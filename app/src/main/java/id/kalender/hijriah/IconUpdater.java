package id.kalender.hijriah;

import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import java.time.*;
import java.time.chrono.HijrahDate;
import java.time.temporal.ChronoField;

public final class IconUpdater {
    private static final String PACKAGE="id.kalender.hijriah.";
    private static final String HIJRI_PREFIX=PACKAGE+"Day";
    private static final String GREG_PREFIX=PACKAGE+"GregDay";

    public static void update(Context c){
        android.content.SharedPreferences prefs=c.getSharedPreferences("settings",Context.MODE_PRIVATE);
        int correction=prefs.getInt("correction",0);
        int hijriDay=HijrahDate.from(SolarTime.effectiveCivilDate(c).plusDays(correction)).get(ChronoField.DAY_OF_MONTH);
        boolean gregorian="gregorian".equals(prefs.getString("main_calendar","hijri"));
        String theme=prefs.getString("app_theme","zamrud");
        String hijriPrefix="safir".equals(theme)?PACKAGE+"SafirDay":"zaitun".equals(theme)?PACKAGE+"ZaitunDay":HIJRI_PREFIX;
        String gregPrefix="safir".equals(theme)?PACKAGE+"SafirGregDay":"zaitun".equals(theme)?PACKAGE+"ZaitunGregDay":GREG_PREFIX;
        String desired=gregorian
            ?gregPrefix+String.format("%02d",LocalDate.now().getDayOfMonth())
            :hijriPrefix+String.format("%02d",hijriDay);

        PackageManager pm=c.getPackageManager();
        String previous=prefs.getString("active_icon_component","");
        pm.setComponentEnabledSetting(new ComponentName(c,desired),PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
        if(previous.isEmpty()){
            for(int i=1;i<=30;i++){
                String old=HIJRI_PREFIX+String.format("%02d",i);
                if(!old.equals(desired))pm.setComponentEnabledSetting(new ComponentName(c,old),PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
            }
        }else if(!previous.equals(desired)){
            pm.setComponentEnabledSetting(new ComponentName(c,previous),PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
        }
        prefs.edit().putString("active_icon_component",desired).apply();
        CalendarIconWidget.updateAll(c);
        schedule(c,gregorian);
    }

    public static int currentIconResource(Context c){
        android.content.SharedPreferences prefs=c.getSharedPreferences("settings",Context.MODE_PRIVATE);
        int correction=prefs.getInt("correction",0);
        int hijriDay=HijrahDate.from(SolarTime.effectiveCivilDate(c).plusDays(correction)).get(ChronoField.DAY_OF_MONTH);
        boolean gregorian="gregorian".equals(prefs.getString("main_calendar","hijri"));
        String theme=prefs.getString("app_theme","zamrud");
        int day=gregorian?LocalDate.now().getDayOfMonth():hijriDay;
        String name="icon_"+theme+"_"+(gregorian?"g":"h")+String.format("%02d",day);
        int id=c.getResources().getIdentifier(name,"mipmap",c.getPackageName());
        return id==0?R.mipmap.ic_launcher:id;
    }

    private static void schedule(Context c,boolean gregorian){
        Intent i=new Intent(c,IconUpdateReceiver.class);
        PendingIntent pi=PendingIntent.getBroadcast(c,91,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        ZoneId zone=ZoneId.systemDefault();ZonedDateTime now=ZonedDateTime.now(zone);LocalDate today=now.toLocalDate();
        ZonedDateTime sunset=SolarTime.sunset(today,SolarTime.lat(c),SolarTime.lon(c),zone);
        if(!now.isBefore(sunset))sunset=SolarTime.sunset(today.plusDays(1),SolarTime.lat(c),SolarTime.lon(c),zone);
        ZonedDateTime next=gregorian?now.toLocalDate().plusDays(1).atStartOfDay(zone):sunset;
        long trigger=next.toInstant().toEpochMilli()+5000;
        AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
        if(android.os.Build.VERSION.SDK_INT<31||am.canScheduleExactAlarms())am.setExactAndAllowWhileIdle(AlarmManager.RTC,trigger,pi);
        else am.setAndAllowWhileIdle(AlarmManager.RTC,trigger,pi);
    }
}
