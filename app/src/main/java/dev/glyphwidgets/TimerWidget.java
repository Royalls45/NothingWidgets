package dev.glyphwidgets;
import android.content.Context;
import android.content.SharedPreferences;
public class TimerWidget extends GlyphWidgetProvider {
    @Override protected String kind(){return "timer";}
    static State state(Context c) {
        SharedPreferences p=c.getSharedPreferences("glyph",0); boolean run=p.getBoolean("timer_running",false);
        long remaining=p.getLong("timer_remaining",300), end=p.getLong("timer_end",0);
        if(run) remaining=Math.max(0,(end-System.currentTimeMillis()+999)/1000);
        if(run && remaining==0) { p.edit().putBoolean("timer_running",false).putLong("timer_remaining",0).apply(); run=false; }
        return new State(remaining,run);
    }
    static void toggle(Context c) { State s=state(c); SharedPreferences.Editor e=c.getSharedPreferences("glyph",0).edit(); if(s.running) e.putBoolean("timer_running",false).putLong("timer_remaining",s.remaining).apply(); else e.putBoolean("timer_running",true).putLong("timer_end",System.currentTimeMillis()+Math.max(1,s.remaining)*1000).apply(); }
    static void reset(Context c) { c.getSharedPreferences("glyph",0).edit().putBoolean("timer_running",false).putLong("timer_remaining",300).apply(); }
    static void addMinute(Context c) { State s=state(c); long n=s.remaining+60; SharedPreferences.Editor e=c.getSharedPreferences("glyph",0).edit(); if(s.running)e.putLong("timer_end",System.currentTimeMillis()+n*1000); else e.putLong("timer_remaining",n); e.apply(); }
    static final class State { final long remaining; final boolean running; State(long r,boolean b){remaining=r;running=b;} }
}
