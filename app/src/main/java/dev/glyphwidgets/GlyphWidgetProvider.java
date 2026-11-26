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
import android.os.SystemClock;
import android.view.View;
import android.widget.RemoteViews;
import android.util.TypedValue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GlyphWidgetProvider extends AppWidgetProvider {
    static final String ACTION_WIDGET = "dev.glyphwidgets.ACTION_WIDGET";
    protected String kind() { return "clock"; }

    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) update(context, manager, id, kind());
        if ("weather".equals(kind())) Weather.fetch(context);
        if ("steps".equals(kind())) Steps.read(context);
        if ("screen".equals(kind())) ScreenTime.read(context);
    }

    @Override public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (ACTION_WIDGET.equals(action)) {
            String widgetKind = intent.getStringExtra("kind");
            String command = intent.getStringExtra("command");
            int id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            performAction(context, widgetKind, command);
            if (id != AppWidgetManager.INVALID_APPWIDGET_ID) update(context, AppWidgetManager.getInstance(context), id, widgetKind);
            return;
        }
        if (Intent.ACTION_TIME_TICK.equals(action) || Intent.ACTION_TIME_CHANGED.equals(action) || Intent.ACTION_DATE_CHANGED.equals(action) || Intent.ACTION_TIMEZONE_CHANGED.equals(action) || Intent.ACTION_BATTERY_CHANGED.equals(action)) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, getClass()));
            for (int id : ids) update(context, manager, id, kind());
        }
    }

    static void update(Context c, AppWidgetManager manager, int id, String kind) {
        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget);
        SharedPreferences p = c.getSharedPreferences("glyph", Context.MODE_PRIVATE);
        String label, value = "", detail = "";
        v.setViewVisibility(R.id.widget_clock, View.GONE);
        v.setViewVisibility(R.id.widget_clock_detail, View.GONE);
        v.setViewVisibility(R.id.widget_value, View.VISIBLE);
        v.setViewVisibility(R.id.widget_detail, View.VISIBLE);
        v.setViewVisibility(R.id.widget_graphic, View.GONE);
        v.setViewVisibility(R.id.widget_progress, View.GONE);
        v.setViewVisibility(R.id.widget_chronometer, View.GONE);
        v.setViewVisibility(R.id.widget_world, View.GONE);
        v.setViewVisibility(R.id.widget_actions, View.GONE);

        switch (kind) {
            case "metro":
                label = "ЧАСЫ / METRO";
                v.setViewVisibility(R.id.widget_clock, View.VISIBLE);
                v.setViewVisibility(R.id.widget_clock_detail, View.VISIBLE);
                v.setTextViewTextSize(R.id.widget_clock, TypedValue.COMPLEX_UNIT_SP, 47);
                v.setCharSequence(R.id.widget_clock, "setFormat24Hour", p.getBoolean("24h", true) ? "HH:mm" : "h:mm");
                v.setCharSequence(R.id.widget_clock, "setFormat12Hour", p.getBoolean("24h", true) ? "HH:mm" : "h:mm");
                v.setCharSequence(R.id.widget_clock_detail, "setFormat24Hour", "EEEE  /  dd MMM");
                v.setCharSequence(R.id.widget_clock_detail, "setFormat12Hour", "EEEE  /  dd MMM");
                v.setViewVisibility(R.id.widget_detail, View.GONE);
                break;
            case "orbit":
                label = "ЧАСЫ / ORBIT";
                value = new SimpleDateFormat(p.getBoolean("24h", true) ? "HH:mm" : "h:mm", Locale.getDefault()).format(new Date());
                detail = new SimpleDateFormat("EEEE  /  dd MMM", Locale.getDefault()).format(new Date()).toUpperCase(Locale.ROOT);
                v.setViewVisibility(R.id.widget_graphic, View.VISIBLE);
                v.setImageViewBitmap(R.id.widget_graphic, WidgetGraphics.orbit(c));
                v.setTextViewTextSize(R.id.widget_value, TypedValue.COMPLEX_UNIT_SP, 17);
                break;
            case "weather":
                label = "ПОГОДА / NOW";
                value = p.getString("weather_temp", "--°");
                detail = p.getString("weather_detail", p.getString("city", "МОСКВА").toUpperCase(Locale.ROOT) + "  /  НАЖМИТЕ ДЛЯ ОБНОВЛЕНИЯ");
                v.setTextViewTextSize(R.id.widget_value, TypedValue.COMPLEX_UNIT_SP, 42);
                break;
            case "steps": {
                label = "ШАГИ / TODAY";
                int steps = p.getInt("steps_today", 0);
                value = String.format(Locale.getDefault(), "%,d", steps).replace(',', ' ');
                detail = "ЦЕЛЬ  8 000  /  ШАГОВ ЗА ДЕНЬ";
                int progress = Math.min(100, steps / 80);
                v.setViewVisibility(R.id.widget_progress, View.VISIBLE);
                v.setProgressBar(R.id.widget_progress, 100, progress, false);
                break;
            }
            case "battery":
            case "battery_bars": {
                Intent b = c.registerReceiver(null, new IntentFilterCompat().batteryFilter());
                int level = b == null ? 0 : b.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = b == null ? 100 : b.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
                int pct = scale == 0 ? 0 : level * 100 / scale;
                boolean charging = b != null && (b.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING || b.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_FULL);
                label = "ЗАРЯД / " + ("battery".equals(kind) ? "RING" : "BARS");
                value = pct + "%";
                detail = charging ? "ПИТАНИЕ ПОДКЛЮЧЕНО" : "АККУМУЛЯТОР";
                if ("battery".equals(kind)) {
                    v.setViewVisibility(R.id.widget_graphic, View.VISIBLE);
                    v.setImageViewBitmap(R.id.widget_graphic, WidgetGraphics.batteryRing(c, pct));
                    v.setViewVisibility(R.id.widget_value, View.GONE);
                    v.setViewVisibility(R.id.widget_detail, View.GONE);
                } else {
                    v.setTextViewTextSize(R.id.widget_value, TypedValue.COMPLEX_UNIT_SP, 42);
                    v.setViewVisibility(R.id.widget_progress, View.VISIBLE);
                    v.setProgressBar(R.id.widget_progress, 100, pct, false);
                }
                break;
            }
            case "screen":
                label = "ЭКРАН / TODAY";
                value = p.getString("screen_time", "--ч --м");
                detail = "ВРЕМЯ ИСПОЛЬЗОВАНИЯ";
                v.setTextViewTextSize(R.id.widget_value, TypedValue.COMPLEX_UNIT_SP, 32);
                break;
            case "timer": {
                label = "GLYPH TIMER";
                TimerWidget.State timer = TimerWidget.state(c);
                v.setViewVisibility(R.id.widget_chronometer, View.VISIBLE);
                v.setViewVisibility(R.id.widget_value, View.GONE);
                v.setChronometerCountDown(R.id.widget_chronometer, true);
                long base = SystemClock.elapsedRealtime() + timer.remaining * 1000L;
                v.setChronometer(R.id.widget_chronometer, base, "%s", timer.running && timer.remaining > 0);
                detail = timer.running ? "ИДЁТ  /  НАЖМИТЕ ПАУЗА" : "5 МИНУТ  /  ЗАПУСК ПО КНОПКЕ";
                v.setViewVisibility(R.id.widget_actions, View.VISIBLE);
                v.setTextViewText(R.id.action_left, "↺");
                v.setTextViewText(R.id.action_main, timer.running ? "Ⅱ  ПАУЗА" : "▶  СТАРТ");
                v.setTextViewText(R.id.action_right, "+1м");
                setAction(c, v, R.id.action_left, kind, "timer_reset", id);
                setAction(c, v, R.id.action_main, kind, "timer_toggle", id);
                setAction(c, v, R.id.action_right, kind, "timer_add", id);
                break;
            }
            case "water": {
                label = "ВОДА / DAILY";
                int count = waterCount(p);
                value = String.valueOf(count);
                detail = "СТАКАНОВ СЕГОДНЯ";
                v.setViewVisibility(R.id.widget_actions, View.VISIBLE);
                v.setTextViewText(R.id.action_left, "−");
                v.setTextViewText(R.id.action_main, "＋  ДОБАВИТЬ СТАКАН");
                v.setTextViewText(R.id.action_right, "↺");
                setAction(c, v, R.id.action_left, kind, "water_minus", id);
                setAction(c, v, R.id.action_main, kind, "water_plus", id);
                setAction(c, v, R.id.action_right, kind, "water_reset", id);
                break;
            }
            case "quote": {
                String[] quotes = {"Меньше шума,\nбольше смысла.", "Тишина — тоже\nсигнал.", "Каждая точка\nважна.", "Делай нужное.\nОстальное убери."};
                int index = Math.floorMod(p.getInt("quote_index", 0), quotes.length);
                label = "ЦИТАТА / " + String.format(Locale.ROOT, "%02d", index + 1);
                value = quotes[index]; detail = "НАЖМИТЕ ДЛЯ СЛЕДУЮЩЕЙ";
                v.setTextViewTextSize(R.id.widget_value, TypedValue.COMPLEX_UNIT_SP, 21);
                break;
            }
            case "note":
                label = "ЗАМЕТКА / STICKY";
                value = p.getString("note", "Нажмите, чтобы\nоставить заметку");
                detail = "НАЖМИТЕ, ЧТОБЫ ИЗМЕНИТЬ";
                v.setTextViewTextSize(R.id.widget_value, TypedValue.COMPLEX_UNIT_SP, 18);
                break;
            case "world":
                label = "МИРОВЫЕ ЧАСЫ";
                detail = "МОСКВА  /  ЛОНДОН  /  ТОКИО";
                v.setViewVisibility(R.id.widget_value, View.GONE);
                v.setViewVisibility(R.id.widget_detail, View.GONE);
                v.setViewVisibility(R.id.widget_world, View.VISIBLE);
                v.setCharSequence(R.id.world_moscow, "setTimeZone", "Europe/Moscow");
                v.setCharSequence(R.id.world_london, "setTimeZone", "Europe/London");
                v.setCharSequence(R.id.world_tokyo, "setTimeZone", "Asia/Tokyo");
                boolean h24 = p.getBoolean("24h", true);
                for (int clockId : new int[]{R.id.world_moscow, R.id.world_london, R.id.world_tokyo}) {
                    v.setCharSequence(clockId, "setFormat24Hour", h24 ? "HH:mm" : "h:mm");
                    v.setCharSequence(clockId, "setFormat12Hour", h24 ? "HH:mm" : "h:mm");
                }
                break;
            case "life": {
                label = "LIFE IN DOTS";
                java.util.Calendar now = java.util.Calendar.getInstance();
                int hour = now.get(java.util.Calendar.HOUR_OF_DAY);
                int minute = now.get(java.util.Calendar.MINUTE);
                int dayProgress = (hour * 60 + minute) * 100 / 1440;
                value = dayProgress + "%";
                detail = "СЕГОДНЯ  /  ВРЕМЯ ПРОХОДИТ";
                v.setViewVisibility(R.id.widget_graphic, View.VISIBLE);
                v.setImageViewBitmap(R.id.widget_graphic, WidgetGraphics.lifeDots(c, dayProgress));
                v.setTextViewTextSize(R.id.widget_value, TypedValue.COMPLEX_UNIT_SP, 28);
                break;
            }
            default:
                label = "ЧАСЫ / DIGITAL";
                v.setViewVisibility(R.id.widget_clock, View.VISIBLE);
                v.setViewVisibility(R.id.widget_clock_detail, View.VISIBLE);
                v.setCharSequence(R.id.widget_clock, "setFormat24Hour", p.getBoolean("24h", true) ? "HH:mm" : "h:mm");
                v.setCharSequence(R.id.widget_clock, "setFormat12Hour", p.getBoolean("24h", true) ? "HH:mm" : "h:mm");
                v.setCharSequence(R.id.widget_clock_detail, "setFormat24Hour", "EEEE  /  dd MMM");
                v.setCharSequence(R.id.widget_clock_detail, "setFormat12Hour", "EEEE  /  dd MMM");
                v.setViewVisibility(R.id.widget_detail, View.GONE);
                break;
        }
        v.setTextViewText(R.id.widget_label, label);
        v.setTextViewText(R.id.widget_value, value);
        v.setTextViewText(R.id.widget_detail, detail);
        v.setTextViewText(R.id.widget_index, kindIndex(kind));

        Intent open = new Intent(c, MainActivity.class).putExtra("widget_kind", kind).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if ("quote".equals(kind)) {
            v.setOnClickPendingIntent(R.id.widget_root, actionIntent(c, kind, "quote_next", id));
        } else {
            v.setOnClickPendingIntent(R.id.widget_root, PendingIntent.getActivity(c, id + kind.hashCode(), open, PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag()));
        }
        manager.updateAppWidget(id, v);
    }

    static void refreshAll(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        refreshType(c, m, ClockWidget.class, "clock");
        refreshType(c, m, MetroClockWidget.class, "metro");
        refreshType(c, m, OrbitClockWidget.class, "orbit");
        refreshType(c, m, BatteryWidget.class, "battery");
        refreshType(c, m, BatteryBarsWidget.class, "battery_bars");
        refreshType(c, m, StepsWidget.class, "steps");
        refreshType(c, m, WeatherWidget.class, "weather");
        refreshType(c, m, ScreenWidget.class, "screen");
        refreshType(c, m, TimerWidget.class, "timer");
        refreshType(c, m, WaterWidget.class, "water");
        refreshType(c, m, QuoteWidget.class, "quote");
        refreshType(c, m, NoteWidget.class, "note");
        refreshType(c, m, WorldClockWidget.class, "world");
        refreshType(c, m, LifeWidget.class, "life");
    }
    static void refreshData(Context c) { Weather.fetch(c); Steps.read(c); ScreenTime.read(c); refreshAll(c); }
    static void refreshScreen(Context c) { AppWidgetManager m=AppWidgetManager.getInstance(c); for(int id:m.getAppWidgetIds(new ComponentName(c,ScreenWidget.class))) update(c,m,id,"screen"); }
    private static void refreshType(Context c, AppWidgetManager m, Class<?> cls, String kind) {
        for (int id : m.getAppWidgetIds(new ComponentName(c, cls))) update(c, m, id, kind);
    }
    private static int immutableFlag() { return Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0; }
    private static String kindIndex(String kind) {
        String[] kinds = {"clock", "metro", "orbit", "weather", "steps", "battery", "battery_bars", "screen", "timer", "water", "quote", "note", "world", "life"};
        for (int i=0;i<kinds.length;i++) if (kinds[i].equals(kind)) return String.format(Locale.ROOT, "%02d", i+1);
        return "00";
    }
    private static void setAction(Context c, RemoteViews v, int viewId, String kind, String command, int widgetId) {
        v.setOnClickPendingIntent(viewId, actionIntent(c, kind, command, widgetId));
    }
    private static PendingIntent actionIntent(Context c, String kind, String command, int widgetId) {
        Intent i = new Intent(c, provider(kind)).setAction(ACTION_WIDGET).putExtra("kind", kind).putExtra("command", command).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        return PendingIntent.getBroadcast(c, widgetId * 31 + command.hashCode(), i, PendingIntent.FLAG_UPDATE_CURRENT | immutableFlag());
    }
    private static Class<?> provider(String kind) {
        if ("metro".equals(kind)) return MetroClockWidget.class;
        if ("orbit".equals(kind)) return OrbitClockWidget.class;
        if ("battery".equals(kind)) return BatteryWidget.class;
        if ("battery_bars".equals(kind)) return BatteryBarsWidget.class;
        if ("steps".equals(kind)) return StepsWidget.class;
        if ("weather".equals(kind)) return WeatherWidget.class;
        if ("screen".equals(kind)) return ScreenWidget.class;
        if ("timer".equals(kind)) return TimerWidget.class;
        if ("water".equals(kind)) return WaterWidget.class;
        if ("quote".equals(kind)) return QuoteWidget.class;
        if ("note".equals(kind)) return NoteWidget.class;
        if ("world".equals(kind)) return WorldClockWidget.class;
        if ("life".equals(kind)) return LifeWidget.class;
        return ClockWidget.class;
    }
    private static void performAction(Context c, String kind, String command) {
        if (kind == null || command == null) return;
        SharedPreferences p = c.getSharedPreferences("glyph", Context.MODE_PRIVATE);
        SharedPreferences.Editor e = p.edit();
        if ("quote_next".equals(command)) e.putInt("quote_index", p.getInt("quote_index", 0) + 1).apply();
        if ("water_plus".equals(command)) e.putInt("water_count", waterCount(p) + 1).apply();
        if ("water_minus".equals(command)) e.putInt("water_count", Math.max(0, waterCount(p) - 1)).apply();
        if ("water_reset".equals(command)) e.putInt("water_count", 0).apply();
        if ("timer_toggle".equals(command)) TimerWidget.toggle(c);
        if ("timer_reset".equals(command)) TimerWidget.reset(c);
        if ("timer_add".equals(command)) TimerWidget.addMinute(c);
    }
    private static int waterCount(SharedPreferences p) {
        String today = new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date());
        if (!today.equals(p.getString("water_day", ""))) {
            p.edit().putString("water_day", today).putInt("water_count", 0).apply();
            return 0;
        }
        return p.getInt("water_count", 0);
    }
}

class IntentFilterCompat { android.content.IntentFilter batteryFilter() { return new android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED); } }
