package dev.glyphwidgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

final class WidgetGraphics {
    private static final int RED = Color.rgb(215,25,33), WHITE = Color.rgb(242,242,240), DIM = Color.rgb(65,65,65);
    private static Bitmap draw(Context c, int wdp, int hdp, Drawer drawer) {
        float d = c.getResources().getDisplayMetrics().density; int w=(int)(wdp*d), h=(int)(hdp*d);
        Bitmap b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888); Canvas canvas=new Canvas(b); canvas.scale(d,d); drawer.draw(canvas,wdp,hdp,d); return b;
    }
    static Bitmap batteryRing(Context c,int pct) { return draw(c,150,62,(x,w,h,d)->{ Paint p=paint(WHITE,2.5f); p.setStyle(Paint.Style.STROKE); RectF r=new RectF(w/2-24,h/2-23,w/2+24,h/2+23); x.drawArc(r,-90,360,false,paint(DIM,3)); x.drawArc(r,-90,360*Math.max(0,Math.min(100,pct))/100f,false,paint(RED,3)); p.setStyle(Paint.Style.FILL); p.setTextAlign(Paint.Align.CENTER); p.setTextSize(15); x.drawText(pct+"%",w/2,h/2+5,p); }); }
    static Bitmap orbit(Context c) { return draw(c,150,62,(x,w,h,d)->{ float cx=w/2,cy=h/2; Paint p=paint(DIM,1); p.setStyle(Paint.Style.STROKE); x.drawCircle(cx,cy,26,p); x.drawCircle(cx,cy,17,p); Paint red=paint(RED,2); x.drawLine(cx-29,cy,cx+29,cy,red); x.drawLine(cx,cy-29,cx,cy+29,red); x.drawCircle(cx+18,cy-18,3,paint(WHITE,1)); x.drawCircle(cx-17,cy+17,2,paint(RED,1)); }); }
    static Bitmap lifeDots(Context c,int progress) { return draw(c,150,62,(x,w,h,d)->{ Paint p=paint(RED,2); int n=48, cols=16, filled=progress*n/100; float gap=w/(cols+1f), top=(h-2*3)/6f; for(int i=0;i<n;i++) { p.setColor(i<filled?RED:DIM); x.drawCircle(gap*(i%cols+1), top*(i/cols+1), 1.7f,p); } }); }
    private static Paint paint(int color,float sw) { Paint p=new Paint(3); p.setColor(color); p.setStrokeWidth(sw); return p; }
    interface Drawer { void draw(Canvas c,int w,int h,float density); }
}
