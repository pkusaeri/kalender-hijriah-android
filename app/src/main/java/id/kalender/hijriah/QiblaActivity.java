package id.kalender.hijriah;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

public class QiblaActivity extends Activity implements SensorEventListener {
    private static final float CALIBRATION_TARGET=240f;
    private ThemePalette theme;
    private int navy,background,surface,border,textPrimary,muted,green;
    private LinearLayout body;
    private SensorManager sensors;
    private Sensor orientationSensor,accel,magnet;
    private String sensorEngine;
    private float[] gravity,field,previousRotation;
    private float azimuth=Float.NaN,movement;
    private long lastCalibrationSample;
    private int state;
    private Compass compass;
    private ProgressBar progress;
    private TextView guidance,liveBearing;
    private double qibla;
    private float declination;
    private final Handler locationHandler=new Handler(Looper.getMainLooper());
    private LocationManager activeLocationManager;
    private Location fallbackLocation;
    private boolean locationDelivered;

    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
    private TextView tv(String s,int z,int c){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(c);v.setGravity(Gravity.CENTER_VERTICAL);v.setPadding(dp(8),dp(6),dp(8),dp(6));return v;}
    private android.graphics.drawable.GradientDrawable rounded(int color,int radius){android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        theme=ThemePalette.from(this);navy=theme.navy;background=theme.background;surface=theme.surface;border=theme.border;textPrimary=theme.textPrimary;muted=theme.muted;green=theme.accent;theme.applySystemBars(this);
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(background);
        LinearLayout bar=new LinearLayout(this);bar.setGravity(Gravity.CENTER_VERTICAL);bar.setPadding(dp(12),0,dp(12),0);bar.setBackgroundColor(navy);
        TextView back=tv("‹",30,Color.WHITE);back.setGravity(Gravity.CENTER);back.setOnClickListener(v->finish());bar.addView(back,new LinearLayout.LayoutParams(dp(46),dp(58)));
        bar.addView(tv("Arah Kiblat",19,Color.WHITE),new LinearLayout.LayoutParams(0,dp(58),1));root.addView(bar);
        body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setGravity(Gravity.CENTER_HORIZONTAL);body.setPadding(dp(24),dp(34),dp(24),dp(24));root.addView(body,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);

        sensors=(SensorManager)getSystemService(SENSOR_SERVICE);
        orientationSensor=sensors.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if(orientationSensor!=null)sensorEngine="Android Rotation Vector";
        else{
            orientationSensor=sensors.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
            if(orientationSensor!=null)sensorEngine="Geomagnetic Rotation Vector";
        }
        if(orientationSensor==null){
            accel=sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnet=sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            sensorEngine="Accelerometer + magnetometer (fallback)";
        }
        showPrerequisite();
    }

    private boolean locationEnabled(){LocationManager lm=(LocationManager)getSystemService(LOCATION_SERVICE);return Build.VERSION.SDK_INT>=28?lm.isLocationEnabled():lm.isProviderEnabled(LocationManager.GPS_PROVIDER)||lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}

    private void showPrerequisite(){
        state=0;body.removeAllViews();TextView pin=tv("⌖",62,green);pin.setGravity(Gravity.CENTER);body.addView(pin,new LinearLayout.LayoutParams(-1,dp(100)));
        TextView title=tv("Aktifkan lokasi",24,textPrimary);title.setGravity(Gravity.CENTER);title.setTypeface(null,android.graphics.Typeface.BOLD);body.addView(title);
        TextView note=tv("Arah Kiblat dihitung dari posisi Anda. Lokasi harus aktif dan diperbarui sebelum kompas dikalibrasi.",13,muted);note.setGravity(Gravity.CENTER);body.addView(note);
        TextView button=tv(locationEnabled()?"Gunakan lokasi saat ini":"Buka pengaturan lokasi",13,Color.WHITE);button.setGravity(Gravity.CENTER);button.setBackground(rounded(green,11));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(220),dp(46));p.setMargins(0,dp(24),0,0);body.addView(button,p);
        button.setOnClickListener(v->{if(!locationEnabled())startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));else requestLocation();});
    }

    private void requestLocation(){
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED&&checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},51);return;}
        state=1;locationDelivered=false;fallbackLocation=null;body.removeAllViews();ProgressBar wait=new ProgressBar(this);body.addView(wait,new LinearLayout.LayoutParams(-1,dp(70)));
        TextView label=tv("Mencari lokasi GPS dan jaringan…",16,textPrimary);label.setGravity(Gravity.CENTER);body.addView(label);
        TextView note=tv("Pastikan Lokasi aktif. Proses dapat memerlukan beberapa detik.",12,muted);note.setGravity(Gravity.CENTER);body.addView(note);
        try{
            activeLocationManager=(LocationManager)getSystemService(LOCATION_SERVICE);requestProvider(LocationManager.NETWORK_PROVIDER);requestProvider(LocationManager.GPS_PROVIDER);
            locationHandler.postDelayed(()->{if(locationDelivered)return;if(fallbackLocation!=null)locationReady(fallbackLocation);else{Toast.makeText(this,"Lokasi belum tersedia. Pastikan mode Lokasi aktif, lalu coba lagi.",Toast.LENGTH_LONG).show();showPrerequisite();}},18000);
        }catch(Exception e){Toast.makeText(this,"Layanan lokasi tidak dapat diakses",Toast.LENGTH_LONG).show();showPrerequisite();}
    }

    private void requestProvider(String provider){
        try{
            if(!activeLocationManager.isProviderEnabled(provider))return;
            Location last=activeLocationManager.getLastKnownLocation(provider);if(last!=null&&System.currentTimeMillis()-last.getTime()<21600000L&&(fallbackLocation==null||last.getTime()>fallbackLocation.getTime()))fallbackLocation=last;
            activeLocationManager.requestLocationUpdates(provider,1000,1f,locationListener,Looper.getMainLooper());
            if(Build.VERSION.SDK_INT>=30)activeLocationManager.getCurrentLocation(provider,null,getMainExecutor(),l->{if(l!=null)locationReady(l);});
        }catch(Exception ignored){}
    }

    private final LocationListener locationListener=new LocationListener(){
        @Override public void onLocationChanged(Location location){locationReady(location);}
        @Override public void onProviderEnabled(String provider){}
        @Override public void onProviderDisabled(String provider){}
        @Override public void onStatusChanged(String provider,int status,Bundle extras){}
    };

    private void locationReady(Location l){
        if(l==null||locationDelivered)return;locationDelivered=true;locationHandler.removeCallbacksAndMessages(null);try{if(activeLocationManager!=null)activeLocationManager.removeUpdates(locationListener);}catch(Exception ignored){}
        getSharedPreferences("settings",0).edit().putFloat("latitude",(float)l.getLatitude()).putFloat("longitude",(float)l.getLongitude()).putFloat("location_accuracy",l.hasAccuracy()?l.getAccuracy():-1).putBoolean("has_location",true).apply();
        qibla=PrayerUtils.qibla(this);
        GeomagneticField geomagnetic=new GeomagneticField((float)l.getLatitude(),(float)l.getLongitude(),l.hasAltitude()?(float)l.getAltitude():0f,System.currentTimeMillis());declination=geomagnetic.getDeclination();
        showCalibration();
    }

    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==51&&(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED||checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED))requestLocation();else showPrerequisite();}
    @Override protected void onDestroy(){super.onDestroy();locationHandler.removeCallbacksAndMessages(null);try{if(activeLocationManager!=null)activeLocationManager.removeUpdates(locationListener);}catch(Exception ignored){}}

    private void showCalibration(){
        state=2;movement=0;previousRotation=null;lastCalibrationSample=0;body.removeAllViews();TextView spin=tv("↻",72,green);spin.setGravity(Gravity.CENTER);body.addView(spin,new LinearLayout.LayoutParams(-1,dp(110)));
        TextView title=tv("Segarkan kompas",23,textPrimary);title.setGravity(Gravity.CENTER);title.setTypeface(null,android.graphics.Typeface.BOLD);body.addView(title);
        TextView instruction=tv("Putar pergelangan tangan membentuk angka 8. Kompas akan terbuka otomatis setelah gerakan terbaca.",14,textPrimary);instruction.setGravity(Gravity.CENTER);body.addView(instruction);
        progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax((int)CALIBRATION_TARGET);progress.setProgress(0);LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(-1,dp(14));pp.setMargins(dp(16),dp(24),dp(16),dp(8));body.addView(progress,pp);
        TextView note=tv(sensorEngine+" · jauhkan ponsel dari logam dan perangkat elektronik.",12,muted);note.setGravity(Gravity.CENTER);body.addView(note);
        if(orientationSensor==null&&(accel==null||magnet==null)){note.setText("Sensor arah tidak tersedia pada perangkat ini.");progress.setVisibility(View.GONE);}
    }

    private void showCompass(){
        state=3;body.removeAllViews();
        guidance=tv("Arah Kiblat siap",22,textPrimary);guidance.setGravity(Gravity.CENTER);guidance.setTypeface(null,android.graphics.Typeface.BOLD);body.addView(guidance);
        TextView instruction=tv("Putar badan bersama ponsel hingga Ka'bah berada tepat di tanda atas.",13,muted);instruction.setGravity(Gravity.CENTER);body.addView(instruction);
        compass=new Compass(this);LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(dp(300),dp(318));cp.setMargins(0,dp(8),0,dp(8));body.addView(compass,cp);
        liveBearing=tv("",13,muted);liveBearing.setGravity(Gravity.CENTER);body.addView(liveBearing);
        float accuracy=getSharedPreferences("settings",0).getFloat("location_accuracy",-1f);
        String locationInfo=accuracy>0?"Lokasi perangkat · akurasi ±"+Math.round(accuracy)+" m":"Lokasi perangkat";
        TextView location=tv(locationInfo+"  •  "+directionText(),11,muted);location.setGravity(Gravity.CENTER);body.addView(location);
        TextView refresh=tv("↻  Segarkan ulang kompas",13,green);refresh.setGravity(Gravity.CENTER);refresh.setOnClickListener(v->showCalibration());LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(44));rp.setMargins(0,dp(8),0,0);body.addView(refresh,rp);
        TextView engine=tv("Sensor: "+sensorEngine+" · utara sebenarnya",10,muted);engine.setGravity(Gravity.CENTER);body.addView(engine);
        updateGuidance();
    }

    private String directionText(){String d=qibla>=292.5&&qibla<337.5?"Barat Laut":qibla>=247.5?"Barat":"Arah Kiblat";return String.format(Locale.US,"%.0f°  %s",qibla,d);}

    @Override protected void onResume(){
        super.onResume();
        if(orientationSensor!=null)sensors.registerListener(this,orientationSensor,SensorManager.SENSOR_DELAY_GAME);
        else{if(accel!=null)sensors.registerListener(this,accel,SensorManager.SENSOR_DELAY_GAME);if(magnet!=null)sensors.registerListener(this,magnet,SensorManager.SENSOR_DELAY_GAME);}
        if(state==0&&locationEnabled()&&checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED)showPrerequisite();
    }
    @Override protected void onPause(){super.onPause();sensors.unregisterListener(this);}
    @Override public void onAccuracyChanged(Sensor s,int a){}

    @Override public void onSensorChanged(SensorEvent event){
        float[] rotation=new float[9];
        if(event.sensor.getType()==Sensor.TYPE_ROTATION_VECTOR||event.sensor.getType()==Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR){SensorManager.getRotationMatrixFromVector(rotation,event.values);}
        else{
            if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER)gravity=event.values.clone();else if(event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD)field=event.values.clone();
            if(gravity==null||field==null||!SensorManager.getRotationMatrix(rotation,null,gravity,field))return;
        }
        float[] screenRotation=remapForScreen(rotation);
        float[] orientation=new float[3];SensorManager.getOrientation(screenRotation,orientation);
        float raw=normalize((float)Math.toDegrees(orientation[0])+declination);
        if(Float.isNaN(azimuth))azimuth=raw;else azimuth=normalize(azimuth+0.18f*shortestDelta(azimuth,raw));

        if(state==2){
            long now=System.currentTimeMillis();
            if(previousRotation!=null&&now-lastCalibrationSample>=90){
                float[] change=new float[3];SensorManager.getAngleChange(change,screenRotation,previousRotation);
                float degrees=(float)Math.toDegrees(Math.abs(change[0])+Math.abs(change[1])+Math.abs(change[2]));
                if(degrees>.7f)movement+=Math.min(degrees,55f);
                if(progress!=null)progress.setProgress((int)Math.min(CALIBRATION_TARGET,movement));
                lastCalibrationSample=now;
                if(movement>=CALIBRATION_TARGET){showCompass();return;}
            }
            previousRotation=screenRotation.clone();
        }else if(state==3&&compass!=null){compass.invalidate();updateGuidance();}
    }

    private float[] remapForScreen(float[] source){
        Display display=getWindowManager().getDefaultDisplay();int rotation=display.getRotation();if(rotation==Surface.ROTATION_0)return source;
        float[] out=new float[9];
        if(rotation==Surface.ROTATION_90)SensorManager.remapCoordinateSystem(source,SensorManager.AXIS_Y,SensorManager.AXIS_MINUS_X,out);
        else if(rotation==Surface.ROTATION_180)SensorManager.remapCoordinateSystem(source,SensorManager.AXIS_MINUS_X,SensorManager.AXIS_MINUS_Y,out);
        else SensorManager.remapCoordinateSystem(source,SensorManager.AXIS_MINUS_Y,SensorManager.AXIS_X,out);
        return out;
    }
    private static float normalize(float value){while(value<0)value+=360;while(value>=360)value-=360;return value;}
    private static float shortestDelta(float from,float to){float d=to-from;if(d>180)d-=360;if(d<-180)d+=360;return d;}

    private void updateGuidance(){
        if(guidance==null||Float.isNaN(azimuth))return;
        float turn=shortestDelta(azimuth,(float)qibla);float distance=Math.abs(turn);boolean aligned=distance<=5f;
        if(aligned){guidance.setText("✓  Sudah menghadap Kiblat");guidance.setTextColor(green);}
        else{guidance.setText("Putar "+Math.round(distance)+"° ke "+(turn>0?"kanan":"kiri"));guidance.setTextColor(textPrimary);}
        if(liveBearing!=null)liveBearing.setText("Arah ponsel "+Math.round(normalize(azimuth))+"°  •  Kiblat "+Math.round(qibla)+"°");
    }

    private class Compass extends View{
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);Compass(Activity c){super(c);}
        @Override protected void onDraw(Canvas c){
            float x=getWidth()/2f,y=getHeight()/2f+dp(10),r=Math.min(getWidth(),getHeight()-dp(28))/2f-dp(10);
            p.setStyle(Paint.Style.FILL);p.setColor(surface);c.drawCircle(x,y,r,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(2));p.setColor(border);c.drawCircle(x,y,r,p);

            // Fixed reference: this is the physical top edge of the phone.
            p.setStyle(Paint.Style.FILL);p.setColor(green);Path top=new Path();top.moveTo(x,y-r-dp(1));top.lineTo(x-dp(7),y-r+dp(11));top.lineTo(x+dp(7),y-r+dp(11));top.close();c.drawPath(top,p);
            p.setTextAlign(Paint.Align.CENTER);p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);p.setTextSize(dp(10));p.setColor(textPrimary);c.drawText("ATAS PONSEL",x,dp(12),p);

            // Cardinal points rotate around the user, providing an unambiguous north reference.
            String[] labels={"U","T","S","B"};float[] bearings={0,90,180,270};
            p.setTextSize(dp(13));p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            for(int n=0;n<4;n++){double angle=Math.toRadians(bearings[n]-azimuth-90);float tx=x+(float)Math.cos(angle)*(r-dp(20)),ty=y+(float)Math.sin(angle)*(r-dp(20))+dp(5);p.setColor(n==0?green:muted);c.drawText(labels[n],tx,ty,p);}
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(1));p.setColor(border);
            for(int n=0;n<12;n++){double angle=Math.toRadians(n*30-azimuth-90);float x1=x+(float)Math.cos(angle)*(r-dp(8)),y1=y+(float)Math.sin(angle)*(r-dp(8));float x2=x+(float)Math.cos(angle)*(r-dp(3)),y2=y+(float)Math.sin(angle)*(r-dp(3));c.drawLine(x1,y1,x2,y2,p);}

            // The arrow starts at the user and terminates at the Ka'bah target on the rim.
            float relative=normalize((float)(qibla-azimuth));double targetAngle=Math.toRadians(relative-90);float endRadius=r-dp(43);float endX=x+(float)Math.cos(targetAngle)*endRadius,endY=y+(float)Math.sin(targetAngle)*endRadius;
            p.setStrokeWidth(dp(7));p.setStrokeCap(Paint.Cap.ROUND);p.setColor(green);c.drawLine(x,y,endX,endY,p);p.setStrokeCap(Paint.Cap.BUTT);
            c.save();c.rotate(relative,x,y);p.setStyle(Paint.Style.FILL);Path arrow=new Path();arrow.moveTo(x,y-r+dp(31));arrow.lineTo(x-dp(11),y-r+dp(49));arrow.lineTo(x+dp(11),y-r+dp(49));arrow.close();p.setColor(green);c.drawPath(arrow,p);c.restore();

            float markerRadius=r-dp(22);float kx=x+(float)Math.cos(targetAngle)*markerRadius,ky=y+(float)Math.sin(targetAngle)*markerRadius;
            p.setStyle(Paint.Style.FILL);p.setColor(Color.rgb(20,20,20));c.drawRoundRect(kx-dp(15),ky-dp(15),kx+dp(15),ky+dp(15),dp(3),dp(3),p);p.setColor(Color.rgb(244,201,93));c.drawRect(kx-dp(15),ky+dp(6),kx+dp(15),ky+dp(11),p);
            p.setTextSize(dp(9));p.setColor(textPrimary);c.drawText("KA'BAH",kx,ky-dp(21),p);

            // Center is explicitly the user/phone, never the destination.
            p.setStyle(Paint.Style.FILL);p.setColor(background);c.drawCircle(x,y,dp(24),p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(2));p.setColor(green);c.drawRoundRect(x-dp(10),y-dp(17),x+dp(10),y+dp(17),dp(4),dp(4),p);c.drawLine(x-dp(4),y-dp(13),x+dp(4),y-dp(13),p);
            p.setStyle(Paint.Style.FILL);p.setTextSize(dp(8));p.setColor(muted);c.drawText("ANDA",x,y+dp(35),p);
        }
    }
}
