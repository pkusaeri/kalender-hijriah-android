package id.kalender.hijriah;
import android.content.*;
public class IconUpdateReceiver extends BroadcastReceiver { @Override public void onReceive(Context c,Intent i){ IconUpdater.update(c); PrayerAlarmScheduler.update(c); } }
