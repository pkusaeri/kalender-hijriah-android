package id.kalender.hijriah;

import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;

public final class AdhanAudio {
    private AdhanAudio() {}

    public static Uri soundUri(Context context) {
        android.content.SharedPreferences prefs=context.getSharedPreferences("settings",0);
        String mode=prefs.getString("sound_mode","device");
        if("builtin_beautiful".equals(mode))return resource(context,R.raw.beautiful_adhan);
        if("builtin_doha".equals(mode))return resource(context,R.raw.doha_adhan);
        if("local".equals(mode)){
            String value=prefs.getString("adhan_uri","");
            return value.isEmpty()?resource(context,R.raw.beautiful_adhan):Uri.parse(value);
        }
        String value=prefs.getString("ringtone_uri","");
        Uri fallback=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        return value.isEmpty()?fallback:Uri.parse(value);
    }

    public static float volume(Context context) {
        return Math.max(0,Math.min(100,context.getSharedPreferences("settings",0).getInt("adhan_volume",80)))/100f;
    }

    private static Uri resource(Context context,int id){
        return Uri.parse("android.resource://"+context.getPackageName()+"/"+id);
    }
}
