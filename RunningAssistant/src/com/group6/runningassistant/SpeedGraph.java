package com.group6.runningassistant;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.widget.LinearLayout;

public class SpeedGraph extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.speed_graph);
	

	GraphViewSeries exampleSeries = new GraphViewSeries(new GraphViewData[] {
		   
		   //	for(i=0;i<30;i++)  
			    new GraphViewData(1,1)
		      , new GraphViewData(2, 1.5d)
		      , new GraphViewData(3, 2.5d)
		      , new GraphViewData(4, 1.0d)
		      , new GraphViewData(5,0 )
			  , new GraphViewData(6, 1.0d)
			  , new GraphViewData(7,2 )
		});
		 
		GraphView graphView = new LineGraphView(
		      this // context
		      , "Speed-Time Graph" // heading
		);
		graphView.addSeries(exampleSeries); // data
		graphView.getGraphViewStyle().setGridColor(Color.GREEN);
		graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.YELLOW);
		graphView.getGraphViewStyle().setVerticalLabelsColor(Color.RED);
		graphView.getGraphViewStyle().setTextSize((float) 20);
		graphView.getGraphViewStyle().setNumHorizontalLabels(5);
		graphView.getGraphViewStyle().setNumVerticalLabels(6);
		//graphView.getGraphViewStyle().setVerticalLabelsWidth(300);
		
		graphView.getGraphViewStyle().setVerticalLabelsAlign(Align.CENTER);
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.speed_graph);
		layout.addView(graphView);

}
}
