package dev.glyphwidgets;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity {
    private EditText city;
    private EditText note;
    private TextView status;
    private int pad(float dp) { return (int)(dp * getResources().getDisplayMetrics().density + .5f); }
    @Override public void onCreate(Bundle b) { super.onCreate(b); getWindow().setStatusBarColor(Color.rgb(8,8,8)); getWindow().setNavigationBarColor(Color.rgb(8,8,8)); build(); }
    private TextView text(String s, int size, int color, boolean mono) {
        TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); t.setFontFeatureSettings(mono ? "kern" : ""); if(mono)t.setTypeface(Typeface.MONOSPACE); return t;
    }
    private void build() {
        int white=Color.WHITE, grey=Color.rgb(160,160,160), red=Color.rgb(230,35,43);
        LinearLayout page=new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setPadding(pad(22),pad(28),pad(22),pad(26)); page.setBackgroundColor(Color.rgb(8,8,8));
        ScrollView scroll=new ScrollView(this); scroll.setFillViewport(true); scroll.addView(page);
        page.addView(text("GLYPH\nWIDGETS",34,white,true)); TextView intro=text("Минималистичные виджеты для любого Android",14,grey,false); intro.setPadding(0,pad(10),0,pad(24)); page.addView(intro);
        page.addView(text("КОЛЛЕКЦИЯ",11,red,true));
        addCard(page,"01 / DIGITAL CLOCK","Точечный цифровой формат времени и даты"); addCard(page,"02 / METRO CLOCK","Крупные часы с датой"); addCard(page,"03 / ORBIT CLOCK","Орбитальный циферблат");
        addCard(page,"04 / WEATHER","Текущая температура • Open-Meteo"); addCard(page,"05 / PEDOMETER","Шаги за день • датчик телефона"); addCard(page,"06 / BATTERY RING","Кольцо уровня заряда"); addCard(page,"07 / BATTERY BARS","Заряд полосой"); addCard(page,"08 / SCREEN TIME","Общее время использования за день");
        addCard(page,"09 / TIMER","Запуск, пауза, сброс и +1 минута"); addCard(page,"10 / WATER","Счётчик стаканов на сегодня"); addCard(page,"11 / QUOTE","Цитаты по нажатию"); addCard(page,"12 / STICKY NOTE","Сохранённая заметка"); addCard(page,"13 / WORLD CLOCK","Москва, Лондон и Токио"); addCard(page,"14 / LIFE IN DOTS","Прогресс текущего дня");
        page.addView(text("НАСТРОЙКИ",11,red,true),lp(-1,-2,8,10));
        city=new EditText(this); city.setSingleLine(true); city.setTextColor(white); city.setHintTextColor(grey); city.setHint("Город, например Moscow"); city.setText(getSharedPreferences("glyph",0).getString("city","Moscow")); city.setPadding(pad(12),0,pad(12),0); city.setBackgroundColor(Color.rgb(25,25,25)); page.addView(city,lp(-1,54,0,12));
        note=new EditText(this); note.setMinLines(2); note.setMaxLines(4); note.setTextColor(white); note.setHintTextColor(grey); note.setHint("Текст заметки для виджета"); note.setText(getSharedPreferences("glyph",0).getString("note","")); note.setPadding(pad(12),pad(8),pad(12),pad(8)); note.setBackgroundColor(Color.rgb(25,25,25)); page.addView(note,lp(-1,-2,0,12));
        Switch format=new Switch(this); format.setText("24-часовой формат"); format.setTextColor(white); format.setChecked(getSharedPreferences("glyph",0).getBoolean("24h",true)); page.addView(format,lp(-1,48,0,8));
        Button save=button("СОХРАНИТЬ НАСТРОЙКИ"); page.addView(save,lp(-1,52,0,8));
        save.setOnClickListener(v->{getSharedPreferences("glyph",0).edit().putString("city",city.getText().toString().trim().isEmpty()?"Moscow":city.getText().toString().trim()).putString("note",note.getText().toString().trim().isEmpty()?"Нажмите, чтобы оставить заметку":note.getText().toString().trim()).putBoolean("24h",format.isChecked()).apply(); GlyphWidgetProvider.refreshData(this); status.setText("ГОТОВО  /  ВИДЖЕТЫ ОБНОВЛЯЮТСЯ");});
        Button steps=button("РАЗРЕШИТЬ ДОСТУП К ШАГАМ"); page.addView(steps,lp(-1,50,0,8)); steps.setOnClickListener(v->{if(Build.VERSION.SDK_INT>=29 && checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)!=PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION},41); else {Steps.read(this); status.setText("ДАТЧИК ШАГОВ ВКЛЮЧЁН");}});
        Button usage=button("РАЗРЕШИТЬ СТАТИСТИКУ ЭКРАНА"); page.addView(usage,lp(-1,50,0,8)); usage.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
        status=text("НАЖМИТЕ ВИДЖЕТ, ЧТОБЫ ОБНОВИТЬ ДАННЫЕ",10,grey,true); status.setPadding(0,pad(18),0,0); page.addView(status);
        setContentView(scroll); GlyphWidgetProvider.refreshData(this);
    }
    private void addCard(LinearLayout page,String title,String desc) { LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(pad(14),pad(12),pad(14),pad(12)); card.setBackgroundColor(Color.rgb(20,20,20)); TextView a=text(title,14,Color.WHITE,true), b=text(desc,11,Color.rgb(155,155,155),false); card.addView(a); b.setPadding(0,pad(4),0,0); card.addView(b); page.addView(card,lp(-1,-2,0,8)); }
    private Button button(String label) { Button b=new Button(this); b.setText(label); b.setTextColor(Color.WHITE); b.setTextSize(12); b.setTypeface(Typeface.MONOSPACE,Typeface.BOLD); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(42,42,42))); return b; }
    private LinearLayout.LayoutParams lp(int w,int h,int top,int bottom) { LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w<0?w:pad(w),h<0?h:pad(h)); p.setMargins(0,pad(top),0,pad(bottom)); return p; }
    @Override protected void onResume() { super.onResume(); if(status!=null && ScreenTime.hasAccess(this)) { ScreenTime.read(this); status.setText("ДОСТУП К ЭКРАННОМУ ВРЕМЕНИ ВКЛЮЧЁН"); } }
    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] results) { super.onRequestPermissionsResult(requestCode,permissions,results); if(requestCode==41 && results.length>0 && results[0]==PackageManager.PERMISSION_GRANTED) { Steps.read(this); status.setText("ДАТЧИК ШАГОВ ВКЛЮЧЁН"); } }
}
