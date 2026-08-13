package id.kalender.hijriah;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.batoulapps.adhan.PrayerTimes;
import java.time.LocalDate;
import java.util.Date;

/** Compact prayer schedule that follows the Subuh/Maghrib ordering preference. */
public class PrayerScheduleWidget extends AppWidgetProvider {
    private static final int[] NAME_IDS={R.id.prayer_name_1,R.id.prayer_name_2,R.id.prayer_name_3,R.id.prayer_name_4,R.id.prayer_name_5};
    private static final int[] TIME_IDS={R.id.prayer_time_1,R.id.prayer_time_2,R.id.prayer_time_3,R.id.prayer_time_4,R.id.prayer_time_5};

    @Override public void onUpdate(Context context,AppWidgetManager manager,int[] ids){
        for(int id:ids)manager.updateAppWidget(id,views(context));
    }

    @Override public void onEnabled(Context context){updateAll(context);}

    static void updateAll(Context context){
        AppWidgetManager manager=AppWidgetManager.getInstance(context);
        ComponentName component=new ComponentName(context,PrayerScheduleWidget.class);
        for(int id:manager.getAppWidgetIds(component))manager.updateAppWidget(id,views(context));
    }

    private static RemoteViews views(Context context){
        RemoteViews views=new RemoteViews(context.getPackageName(),R.layout.widget_prayer_schedule);
        android.content.SharedPreferences prefs=context.getSharedPreferences("settings",0);
        boolean maghribFirst="maghrib".equals(prefs.getString("prayer_day_start","fajr"));
        LocalDate today=LocalDate.now(),cycleDate=today;
        if(maghribFirst&&new Date().before(PrayerUtils.times(context,today).maghrib))cycleDate=today.minusDays(1);

        PrayerTimes first=PrayerUtils.times(context,cycleDate);
        PrayerTimes next=PrayerUtils.times(context,cycleDate.plusDays(1));
        Date[] firstDates=PrayerUtils.dates(first),nextDates=PrayerUtils.dates(next);
        int[] indexes=maghribFirst?new int[]{4,5,0,2,3}:new int[]{0,2,3,4,5};
        Date now=new Date();
        int upcoming=-1;
        for(int pos=0;pos<indexes.length;pos++){
            int prayer=indexes[pos];
            Date time=maghribFirst&&prayer<4?nextDates[prayer]:firstDates[prayer];
            views.setTextViewText(NAME_IDS[pos],PrayerUtils.NAMES[prayer]);
            views.setTextViewText(TIME_IDS[pos],PrayerUtils.format(time));
            if(upcoming<0&&time.after(now))upcoming=pos;
        }

        ThemePalette palette=ThemePalette.from(context);
        String theme=prefs.getString("app_theme","zamrud");
        int background="safir".equals(theme)?R.drawable.widget_prayer_safir:"zaitun".equals(theme)?R.drawable.widget_prayer_zaitun:R.drawable.widget_prayer_zamrud;
        views.setInt(R.id.prayer_widget_root,"setBackgroundResource",background);
        for(int pos=0;pos<5;pos++){
            views.setTextColor(NAME_IDS[pos],pos==upcoming?palette.accent:palette.muted);
            views.setTextColor(TIME_IDS[pos],pos==upcoming?palette.gold:palette.textPrimary);
        }

        Intent open=new Intent(context,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending=PendingIntent.getActivity(context,93,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.prayer_widget_root,pending);
        return views;
    }
}
