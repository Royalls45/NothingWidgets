package dev.glyphwidgets;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.app.AppOpsManager;
import android.os.Process;
import android.provider.Settings;
import java.util.Calendar;

final class ScreenTime {
    static boolean hasAccess(Context c) { try { AppOpsManager a=(AppOpsManager)c.getSystemService(Context.APP_OPS_SERVICE); int mode=a.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,Process.myUid(),c.getPackageName()); return mode==AppOpsManager.MODE_ALLOWED; } catch(Exception e) { return false; } }
    static java.util.List<UsageStats> stats(Context c) {
        UsageStatsManager u=(UsageStatsManager)c.getSystemService(Context.USAGE_STATS_SERVICE); Calendar day=Calendar.getInstance(); day.set(Calendar.HOUR_OF_DAY,0); day.set(Calendar.MINUTE,0); day.set(Calendar.SECOND,0); day.set(Calendar.MILLISECOND,0);
        return u.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,day.getTimeInMillis(),System.currentTimeMillis());
    }
    static void read(Context c) {
        long ms=0; try { for(UsageStats s:stats(c)) ms+=s.getTotalTimeInForeground(); } catch(Exception ignored) {}
        long mins=ms/60000; String value=(mins/60)+"h "+(mins%60)+"m";
        c.getSharedPreferences("glyph",Context.MODE_PRIVATE).edit().putString("screen_time",value).apply();
        GlyphWidgetProvider.refreshScreen(c);
    }
}
