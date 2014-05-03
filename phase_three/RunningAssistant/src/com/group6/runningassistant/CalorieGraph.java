package com.group6.runningassistant;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

public class CalorieGraph extends Activity {
	double n;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calorie_graph);
		Bundle bundle = getIntent().getExtras();
        ArrayList<String> ar = bundle.getStringArrayList("cal_key");
        String [] distance = ar.toArray(new String[ar.size()]);
       n=1;
        if(ar.size()>10)
        {
        	n=Math.ceil((ar.size()/10)+1.0);
        }
        double [] dis = new double[1000];
        for(int i=0;i<1000;i++)
              	dis[i]=0;
        
        for(int i=0;i<ar.size();i++)
        {
        	 dis[i]=Double.parseDouble(distance[i]);
        }
       
        GraphViewData[] data = new GraphViewData[(int) (ar.size()/n)+2]; 
        data[0]=new GraphViewData(0, 0); 
       int i;
        for (i=1; i<=(int) (ar.size()/n); i++) {  
            data[i] = new GraphViewData(n*i, dis[(int) (n*i)]);  
         }
        data[i] = new GraphViewData(ar.size()-1, dis[ar.size()-1]);


	
		 
		GraphView graphView = new LineGraphView(
		      this // context
		      , "calorie-Time Graph" // heading
		);
		graphView.addSeries(new GraphViewSeries(data)); // data
		graphView.getGraphViewStyle().setGridColor(Color.GREEN);
		graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.YELLOW);
		graphView.getGraphViewStyle().setVerticalLabelsColor(Color.RED);
		graphView.getGraphViewStyle().setTextSize((float) 20);
		graphView.getGraphViewStyle().setNumHorizontalLabels(10);
		graphView.getGraphViewStyle().setNumVerticalLabels(10);
		//graphView.getGraphViewStyle().setVerticalLabelsWidth(300);
		
		graphView.getGraphViewStyle().setVerticalLabelsAlign(Align.CENTER);
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.calorie_graph);
		layout.addView(graphView);

}
}

