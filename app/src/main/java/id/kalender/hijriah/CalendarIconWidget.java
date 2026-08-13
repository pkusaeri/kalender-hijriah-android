package id.kalender.hijriah;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class CalendarIconWidget extends AppWidgetProvider {
    @Override public void onUpdate(Context context,AppWidgetManager manager,int[] ids){
        for(int id:ids)manager.updateAppWidget(id,views(context));
    }

    @Override public void onEnabled(Context context){IconUpdater.update(context);}

    static void updateAll(Context context){
        AppWidgetManager manager=AppWidgetManager.getInstance(context);
        ComponentName component=new ComponentName(context,CalendarIconWidgetV2.class);
        for(int id:manager.getAppWidgetIds(component))manager.updateAppWidget(id,views(context));
    }

    private static RemoteViews views(Context context){
        RemoteViews views=new RemoteViews(context.getPackageName(),R.layout.widget_calendar_icon);
        views.setImageViewResource(R.id.widget_icon,IconUpdater.currentIconResource(context));
        Intent open=new Intent(context,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending=PendingIntent.getActivity(context,92,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_icon,pending);
        return views;
    }
}
