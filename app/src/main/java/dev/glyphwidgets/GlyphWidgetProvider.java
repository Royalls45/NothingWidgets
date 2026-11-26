package dev.glyphwidgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.view.View;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GlyphWidgetProvider extends AppWidgetProvider {
    protected String kind() { return "clock"; }

    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) update(context, manager, id, kind());
        if ("weather".equals(kind())) Weather.fetch(context);
        if ("steps".equals(kind())) Steps.read(context);
        if ("screen".equals(kind())) ScreenTime.read(context);
    }

    @Override public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (Intent.ACTION_TIME_TICK.equals(intent.getAction()) || Intent.ACTION_TIME_CHANGED.equals(intent.getAction()) || Intent.ACTION_DATE_CHANGED.equals(intent.getAction()) || Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction()) || Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, getClass()));
            for (int id : ids) update(context, manager, id, kind());
        }
    }

    static void update(Context c, AppWidgetManager manager, int id, String kind) {
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget);
        String label, value, detail;
        SharedPreferences p = c.getSharedPreferences("glyph", Context.MODE_PRIVATE);
        switch (kind) {
            case "battery": {
                Intent b = c.registerReceiver(null, new IntentFilterCompat().batteryFilter());
                int level = b == null ? 0 : b.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = b == null ? 100 : b.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
                int pct = scale == 0 ? 0 : level * 100 / scale;
                boolean charging = b != null && b.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING;
                label = "GLYPH / BATTERY"; value = pct + "%"; detail = charging ? "CHARGING  /  TAP TO REFRESH" : "POWER  /  TAP TO REFRESH"; break;
            }
            case "steps": label = "GLYPH / PEDOMETER"; value = p.getInt("steps_today", 0) + ""; detail = "STEPS TODAY  /  TAP TO REFRESH"; break;
            case "weather": label = "GLYPH / WEATHER"; value = p.getString("weather_temp", "--°"); detail = p.getString("weather_detail", p.getString("city", "MOSCOW").toUpperCase(Locale.ROOT) + "  /  TAP TO REFRESH"); break;
            case "screen": label = "GLYPH / SCREEN TIME"; value = p.getString("screen_time", "--h --m"); detail = "TODAY  /  TAP TO OPEN SETTINGS"; break;
            default:
                label = "GLYPH / CLOCK";
                value = new SimpleDateFormat(p.getBoolean("24h", true) ? "HH:mm" : "h:mm", Locale.getDefault()).format(new Date());
                detail = new SimpleDateFormat("EEEE  /  dd MMM", Locale.getDefault()).format(new Date()).toUpperCase(Locale.ROOT);
        }
        v.setTextViewText(R.id.widget_label, label); v.setTextColor(R.id.widget_label, 0xFFE53935);
        if ("clock".equals(kind)) {
            String pattern = p.getBoolean("24h", true) ? "HH:mm" : "h:mm";
            v.setViewVisibility(R.id.widget_clock, android.view.View.VISIBLE); v.setViewVisibility(R.id.widget_value, android.view.View.GONE);
            v.setViewVisibility(R.id.widget_clock_detail, android.view.View.VISIBLE); v.setViewVisibility(R.id.widget_detail, android.view.View.GONE);
            v.setCharSequence(R.id.widget_clock, "setFormat24Hour", pattern); v.setCharSequence(R.id.widget_clock, "setFormat12Hour", pattern);
            v.setCharSequence(R.id.widget_clock_detail, "setFormat24Hour", "EEEE  /  dd MMM"); v.setCharSequence(R.id.widget_clock_detail, "setFormat12Hour", "EEEE  /  dd MMM");
        } else { v.setViewVisibility(R.id.widget_clock, android.view.View.GONE); v.setViewVisibility(R.id.widget_value, android.view.View.VISIBLE); v.setTextViewText(R.id.widget_value, value); v.setViewVisibility(R.id.widget_clock_detail, android.view.View.GONE); v.setViewVisibility(R.id.widget_detail, android.view.View.VISIBLE); v.setTextViewText(R.id.widget_detail, detail); }
        Intent open = new Intent(c, MainActivity.class).putExtra("widget_kind", kind).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        v.setOnClickPendingIntent(R.id.widget_root, PendingIntent.getActivity(c, id + kind.hashCode(), open, PendingIntent.FLAG_UPDATE_CURRENT | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0)));
        manager.updateAppWidget(id, v);
    }

    static void refreshAll(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        refreshType(c, m, ClockWidget.class, "clock"); refreshType(c, m, BatteryWidget.class, "battery");
        refreshType(c, m, StepsWidget.class, "steps"); refreshType(c, m, WeatherWidget.class, "weather"); refreshType(c, m, ScreenWidget.class, "screen");
    }
    static void refreshData(Context c) { Weather.fetch(c); Steps.read(c); ScreenTime.read(c); refreshAll(c); }
    static void refreshScreen(Context c) { AppWidgetManager m=AppWidgetManager.getInstance(c); for(int id:m.getAppWidgetIds(new ComponentName(c,ScreenWidget.class))) update(c,m,id,"screen"); }
    private static void refreshType(Context c, AppWidgetManager m, Class<?> cls, String kind) {
        for (int id : m.getAppWidgetIds(new ComponentName(c, cls))) update(c, m, id, kind);
    }
}

class IntentFilterCompat { android.content.IntentFilter batteryFilter() { return new android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED); } }
