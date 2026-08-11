package id.kalender.hijriah;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.View;

/** One source of truth for every screen, so a theme changes the complete UI. */
public final class ThemePalette {
    public final int navy, background, surface, surfaceSoft, border;
    public final int textPrimary, muted, accent, gold, onAccent, heroSecondary, accentSoft;
    public final boolean light;

    private ThemePalette(int navy, int background, int surface, int surfaceSoft, int border,
                         int textPrimary, int muted, int accent, int gold, int onAccent,
                         int heroSecondary, int accentSoft, boolean light) {
        this.navy=navy; this.background=background; this.surface=surface; this.surfaceSoft=surfaceSoft;
        this.border=border; this.textPrimary=textPrimary; this.muted=muted; this.accent=accent;
        this.gold=gold; this.onAccent=onAccent; this.heroSecondary=heroSecondary;
        this.accentSoft=accentSoft; this.light=light;
    }

    public static ThemePalette from(Context context) {
        String id=context.getSharedPreferences("settings",0).getString("app_theme","zamrud");
        if ("safir".equals(id)) return new ThemePalette(
            c("#102441"),c("#0B1628"),c("#12283A"),c("#152D43"),c("#2D4B61"),
            c("#F4F8FB"),c("#A9BFD0"),c("#167F83"),c("#FFCF70"),Color.WHITE,
            c("#A9BFD0"),c("#40C3BA"),false);
        if ("zaitun".equals(id)) return new ThemePalette(
            c("#263B32"),c("#F2ECDF"),c("#FFFAF0"),c("#DDD5C5"),c("#CFC5B2"),
            c("#26362F"),c("#6F7B73"),c("#4B7962"),c("#E8C66A"),Color.WHITE,
            c("#D5DDD7"),c("#477A62"),true);
        return new ThemePalette(
            c("#071A33"),c("#07131F"),c("#102331"),c("#162B36"),c("#29404B"),
            c("#F7F5EF"),c("#AEBDB7"),c("#087F5B"),c("#F4C95D"),Color.WHITE,
            c("#C9D9D3"),c("#087F5B"),false);
    }

    public void applySystemBars(Activity activity) {
        activity.getWindow().setStatusBarColor(navy);
        activity.getWindow().setNavigationBarColor(light?background:navy);
        if(Build.VERSION.SDK_INT>=26){
            int flags=activity.getWindow().getDecorView().getSystemUiVisibility();
            flags=light?(flags|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR):(flags&~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            activity.getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    private static int c(String value){return Color.parseColor(value);}
}
