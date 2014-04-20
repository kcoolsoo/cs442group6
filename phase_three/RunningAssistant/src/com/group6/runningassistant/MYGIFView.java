package com.group6.runningassistant;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.view.View;

public class MYGIFView extends View {
	 private Movie movie,movie1;
	InputStream is=null,is1=null;
	long moviestart;
	
	
	public MYGIFView(Context context,InputStream stream) {
	super(context);
	is =stream;
	//is=context.getResources().openRawResource(run.gif);
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
	
