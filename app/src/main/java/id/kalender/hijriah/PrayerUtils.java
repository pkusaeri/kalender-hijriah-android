package id.kalender.hijriah;

import android.content.Context;
import com.batoulapps.adhan.*;
import com.batoulapps.adhan.data.DateComponents;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public final class PrayerUtils {
    public static final String[] NAMES={"Subuh","Terbit","Zuhur","Asar","Maghrib","Isya"};

    public static PrayerTimes times(Context c, LocalDate day){
        CalculationParameters p=parameters(c);
        return new PrayerTimes(new Coordinates(SolarTime.lat(c),SolarTime.lon(c)),new DateComponents(day.getYear(),day.getMonthValue(),day.getDayOfMonth()),p);
    }
    private static CalculationParameters parameters(Context c){
        String key=c.getSharedPreferences("settings",0).getString("prayer_method","KEMENAG");
        CalculationParameters p;
        switch(key){
            case "MWL": p=CalculationMethod.MUSLIM_WORLD_LEAGUE.getParameters();break;
            case "UMM": p=CalculationMethod.UMM_AL_QURA.getParameters();break;
            case "ISNA": p=CalculationMethod.NORTH_AMERICA.getParameters();break;
            case "SINGAPORE": p=CalculationMethod.SINGAPORE.getParameters();break;
            default: p=new CalculationParameters(20.0,18.0,CalculationMethod.OTHER);break;
        }
        p.madhab=Madhab.SHAFI;
        android.content.SharedPreferences s=c.getSharedPreferences("settings",0);
        p.adjustments=new PrayerAdjustments(
            s.getInt("adjust_fajr",0),0,s.getInt("adjust_dhuhr",0),
            s.getInt("adjust_asr",0),s.getInt("adjust_maghrib",0),
            s.getInt("adjust_isha",0));
        return p;
    }
    public static Date[] dates(PrayerTimes t){return new Date[]{t.fajr,t.sunrise,t.dhuhr,t.asr,t.maghrib,t.isha};}
    public static String format(Date d){return d.toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"));}
    public static String methodLabel(Context c){
        String k=c.getSharedPreferences("settings",0).getString("prayer_method","KEMENAG");
        switch(k){case "MWL":return "Muslim World League";case "UMM":return "Umm al-Qura";case "ISNA":return "ISNA";case "SINGAPORE":return "Singapore";default:return "Kemenag RI";}
    }
    public static double qibla(Context c){return new Qibla(new Coordinates(SolarTime.lat(c),SolarTime.lon(c))).direction;}
    public static String nextPrayer(Context c){
        Date now=new Date(); PrayerTimes t=times(c,LocalDate.now()); Date[] ds=dates(t);
        int[] prayer={0,2,3,4,5};
        for(int i:prayer)if(ds[i].after(now))return NAMES[i]+"  "+format(ds[i]);
        PrayerTimes tomorrow=times(c,LocalDate.now().plusDays(1));return "Subuh  "+format(tomorrow.fajr);
    }
}
