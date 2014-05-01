package com.group6.runningassistant;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.view.View;

public class MYGIFView extends View {
	Movie movie,movie1;
	InputStream is=null,is1=null;
	long moviestart;
	public MYGIFView(Context context) {
	super(context);
	is=context.getResources().openRawResource(R.drawable.run);
	movie=Movie.decodeStream(is);
	}
	@Override
	protected void onDraw(Canvas canvas) {
	canvas.drawColor(Color.WHITE);
	super.onDraw(canvas);
	long now=android.os.SystemClock.uptimeMillis();
	System.out.println("now="+now);
	if (moviestart == 0) { // first time
	moviestart = now;
    
	}
	int relTime = (int)((now - moviestart) % movie.duration()) ;
	movie.setTime(relTime);
	canvas.scale((float)this.getWidth() / (float)movie.width(), (float)this.getHeight() / (float)movie.height());
	movie.draw(canvas,this.getWidth()/2-20,this.getHeight()/2-40);
	movie.draw(canvas,(float)this.getWidth() / (float)movie.width(), (float)this.getHeight() / (float)movie.height());
	this.invalidate();

	}
}
