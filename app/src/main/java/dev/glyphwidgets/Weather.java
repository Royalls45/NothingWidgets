package dev.glyphwidgets;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

final class Weather {
    static void fetch(Context context) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String city = context.getSharedPreferences("glyph", Context.MODE_PRIVATE).getString("city", "Moscow");
                String q = URLEncoder.encode(city, StandardCharsets.UTF_8.name());
                JSONObject place = get("https://geocoding-api.open-meteo.com/v1/search?name=" + q + "&count=1&language=en&format=json").getJSONArray("results").getJSONObject(0);
                double lat = place.getDouble("latitude"), lon = place.getDouble("longitude"); String name = place.optString("name", city);
                JSONObject current = get("https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lon + "&current=temperature_2m,relative_humidity_2m,weather_code&timezone=auto").getJSONObject("current");
                int temp = (int)Math.round(current.getDouble("temperature_2m")); int code = current.optInt("weather_code", 0);
                String description = description(code);
                context.getSharedPreferences("glyph", Context.MODE_PRIVATE).edit().putString("weather_temp", temp + "°").putString("weather_detail", name.toUpperCase() + "  /  " + description).apply();
                AppWidgetManager m = AppWidgetManager.getInstance(context); for(int id:m.getAppWidgetIds(new ComponentName(context, WeatherWidget.class))) GlyphWidgetProvider.update(context,m,id,"weather");
            } catch (Exception ignored) { }
        });
    }
    private static JSONObject get(String address) throws Exception {
        HttpURLConnection c=(HttpURLConnection)new URL(address).openConnection(); c.setConnectTimeout(10000); c.setReadTimeout(10000); c.setRequestProperty("User-Agent","GlyphWidgets/1.0");
        try(java.io.InputStream in=c.getInputStream(); java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream()){ byte[] buf=new byte[4096]; int n; while((n=in.read(buf))!=-1) out.write(buf,0,n); return new JSONObject(out.toString("UTF-8")); } finally { c.disconnect(); }
    }
    private static String description(int c) { if(c==0)return "CLEAR"; if(c<=3)return "CLOUDY"; if(c==45||c==48)return "FOG"; if(c>=51&&c<=67)return "DRIZZLE"; if(c>=71&&c<=77)return "SNOW"; if(c>=80&&c<=82)return "SHOWERS"; if(c>=95)return "THUNDER"; return "WEATHER"; }
}
