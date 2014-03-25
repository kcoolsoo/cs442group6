package com.group6.runningassistant;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

public class CalorieGraph extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calorie_graph);
	
int i=0;
	GraphViewSeries exampleSeries = new GraphViewSeries(new GraphViewData[] {
		   
		   //	for(i=0;i<30;i++)  
			    new GraphViewData(1,1)
		      , new GraphViewData(2, 1.5d)
		      , new GraphViewData(3, 2.5d)
		      , new GraphViewData(4, 1.0d)
		      , new GraphViewData(5,0 )
		});
		 
		GraphView graphView = new LineGraphView(
		      this // context
		      , "calorie-Time Graph" // heading
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
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.calorie_graph);
		layout.addView(graphView);

}
}

