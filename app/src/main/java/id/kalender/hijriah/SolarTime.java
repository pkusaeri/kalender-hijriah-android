package id.kalender.hijriah;

import java.time.*;

/** Perkiraan matahari terbenam dengan persamaan NOAA/Meeus dan refraksi 0,833 derajat. */
public final class SolarTime {
    public static ZonedDateTime sunset(LocalDate date, double latitude, double longitude, ZoneId zone) {
        int n=date.getDayOfYear();
        double gamma=2*Math.PI/365.0*(n-1);
        double eq=229.18*(0.000075+0.001868*Math.cos(gamma)-0.032077*Math.sin(gamma)-0.014615*Math.cos(2*gamma)-0.040849*Math.sin(2*gamma));
        double decl=0.006918-0.399912*Math.cos(gamma)+0.070257*Math.sin(gamma)-0.006758*Math.cos(2*gamma)+0.000907*Math.sin(2*gamma)-0.002697*Math.cos(3*gamma)+0.00148*Math.sin(3*gamma);
        double lat=Math.toRadians(latitude), zenith=Math.toRadians(90.833);
        double cosH=(Math.cos(zenith)/(Math.cos(lat)*Math.cos(decl)))-Math.tan(lat)*Math.tan(decl);
        cosH=Math.max(-1,Math.min(1,cosH));
        double ha=Math.toDegrees(Math.acos(cosH));
        ZonedDateTime noon=date.atTime(12,0).atZone(zone);
        int offsetMinutes=noon.getOffset().getTotalSeconds()/60;
        double minutes=720-4*longitude-eq+offsetMinutes+4*ha;
        return date.atStartOfDay(zone).plusSeconds(Math.round(minutes*60));
    }
    public static double lat(android.content.Context c){ return c.getSharedPreferences("settings",0).getFloat("latitude",-6.2088f); }
    public static double lon(android.content.Context c){ return c.getSharedPreferences("settings",0).getFloat("longitude",106.8456f); }
    public static LocalDate effectiveCivilDate(android.content.Context c){
        ZoneId z=ZoneId.systemDefault(); ZonedDateTime now=ZonedDateTime.now(z); LocalDate d=now.toLocalDate();
        return now.isBefore(sunset(d,lat(c),lon(c),z))?d:d.plusDays(1);
    }
}
