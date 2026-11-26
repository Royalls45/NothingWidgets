package dev.glyphwidgets;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;

final class Steps {
    static void read(Context context) {
        if (Build.VERSION.SDK_INT >= 29 && context.checkSelfPermission("android.permission.ACTIVITY_RECOGNITION") != PackageManager.PERMISSION_GRANTED) return;
        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (sensor == null) return;
        SensorEventListener listener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor s, int a) {}
            public void onSensorChanged(SensorEvent e) {
                sm.unregisterListener(this);
                android.content.SharedPreferences p = context.getSharedPreferences("glyph", Context.MODE_PRIVATE);
                String today = new java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(new java.util.Date());
                float base = p.getFloat("steps_base", -1); String day = p.getString("steps_day", "");
                if (!today.equals(day) || base < 0 || e.values[0] < base) { base = e.values[0]; p.edit().putFloat("steps_base", base).putString("steps_day", today).apply(); }
                int count = Math.max(0, (int)(e.values[0] - base)); p.edit().putInt("steps_today", count).apply();
                GlyphWidgetProvider.refreshAll(context);
            }
        };
        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
