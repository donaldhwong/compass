/*
 * Copyright (C) 2009 Pierre H�bert <pierrox@pierrox.net>
 * http://www.pierrox.net/mcompass/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.pierrox.mcompass;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MCompass extends Activity implements SensorEventListener {
    private CompassRenderer mCompassRenderer;
    private GLSurfaceView mGLSurfaceView;
	private TextView mHeadingView;
	
	private SensorManager mSensorManager;
	private Sensor mOrientationSensor;
	
	// data for orientation values filtering (average using a ring buffer)
	static private final int RING_BUFFER_SIZE=10;
	private float[][][] mAnglesRingBuffer;
	private int mNumAngles;
	private int mRingBufferIndex;
	private float[][] mAngles;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // initialize the ring buffer for orientation values
        mNumAngles=0;
        mRingBufferIndex=0;
        mAnglesRingBuffer=new float[RING_BUFFER_SIZE][3][2];
        mAngles=new float[3][2];
        mAngles[0][0]=0;
        mAngles[0][1]=0;
        mAngles[1][0]=0;
        mAngles[1][1]=0;
        mAngles[2][0]=0;
        mAngles[2][1]=0;
        
        // the compass renderer manages 3D objects and gl surface life cycle
        mCompassRenderer = new CompassRenderer();
        
        // create views
        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setRenderer(mCompassRenderer);
        
        mHeadingView = new TextView(this);
        mHeadingView.setTextSize(30);
        mHeadingView.setText("");
        mHeadingView.setBackgroundColor(0xffffffff);
        mHeadingView.setTextColor(0xff000000);
        
        // layout the views in a vertical box
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(mHeadingView);
        layout.addView(mGLSurfaceView);
        
        setContentView(layout);
        
        // watch for orientation changes
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mOrientationSensor=mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(this, mOrientationSensor, SensorManager.SENSOR_DELAY_GAME);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
    	if(event.sensor==mOrientationSensor) {
    		if(mNumAngles==RING_BUFFER_SIZE) {
    			// subtract oldest vector
	    		mAngles[0][0]-=mAnglesRingBuffer[mRingBufferIndex][0][0];
	    		mAngles[0][1]-=mAnglesRingBuffer[mRingBufferIndex][0][1];
	    		mAngles[1][0]-=mAnglesRingBuffer[mRingBufferIndex][1][0];
	    		mAngles[1][1]-=mAnglesRingBuffer[mRingBufferIndex][1][1];
	    		mAngles[2][0]-=mAnglesRingBuffer[mRingBufferIndex][2][0];
	    		mAngles[2][1]-=mAnglesRingBuffer[mRingBufferIndex][2][1];
    		} else {
    			mNumAngles++;
    		}

    		// convert angles into x/y
    		mAnglesRingBuffer[mRingBufferIndex][0][0]=(float) Math.cos(Math.toRadians(event.values[0]));
    		mAnglesRingBuffer[mRingBufferIndex][0][1]=(float) Math.sin(Math.toRadians(event.values[0]));
    		mAnglesRingBuffer[mRingBufferIndex][1][0]=(float) Math.cos(Math.toRadians(event.values[1]));
    		mAnglesRingBuffer[mRingBufferIndex][1][1]=(float) Math.sin(Math.toRadians(event.values[1]));
    		mAnglesRingBuffer[mRingBufferIndex][2][0]=(float) Math.cos(Math.toRadians(event.values[2]));
    		mAnglesRingBuffer[mRingBufferIndex][2][1]=(float) Math.sin(Math.toRadians(event.values[2]));
    		
    		// accumulate new x/y vector
    		mAngles[0][0]+=mAnglesRingBuffer[mRingBufferIndex][0][0];
    		mAngles[0][1]+=mAnglesRingBuffer[mRingBufferIndex][0][1];
    		mAngles[1][0]+=mAnglesRingBuffer[mRingBufferIndex][1][0];
    		mAngles[1][1]+=mAnglesRingBuffer[mRingBufferIndex][1][1];
    		mAngles[2][0]+=mAnglesRingBuffer[mRingBufferIndex][2][0];
    		mAngles[2][1]+=mAnglesRingBuffer[mRingBufferIndex][2][1];
    		
    		mRingBufferIndex++;
    		if(mRingBufferIndex==RING_BUFFER_SIZE) {
    			mRingBufferIndex=0;
    		}
    		
			// convert back x/y into angles
			float azimuth=(float) Math.toDegrees(Math.atan2((double)mAngles[0][1], (double)mAngles[0][0]));
			float pitch=(float) Math.toDegrees(Math.atan2((double)mAngles[1][1], (double)mAngles[1][0]));
			float roll=(float) Math.toDegrees(Math.atan2((double)mAngles[2][1], (double)mAngles[2][0]));
			mCompassRenderer.setOrientation(azimuth, pitch, roll);
			mHeadingView.setText("Heading: "+(int)azimuth+"°");
    	}
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
}
