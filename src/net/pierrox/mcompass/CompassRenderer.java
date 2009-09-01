package net.pierrox.mcompass;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.opengl.GLSurfaceView;
import android.util.Log;

/**
 * Render a pair of tumbling cubes.
 */

class CompassRenderer implements GLSurfaceView.Renderer, SensorEventListener {
    private Turntable mTurnTable;
	private SensorManager mSensorManager;
	private float mAngles[];
	private Context mContext;
	private Sensor mOrientationSensor;
	private float[][] mAnglesRingBuffer;
	private int mNumAngles;
	private int mRingBufferIndex;
	static private final int RING_BUFFER_SIZE=10; 
    
    public CompassRenderer(Context context) {
    	mContext = context;
        mTurnTable = new Turntable();
        
        mNumAngles=0;
        mRingBufferIndex=0;
        mAnglesRingBuffer=new float[RING_BUFFER_SIZE][3];
        mAngles=new float[3];
        mAngles[0]=0;
        mAngles[1]=0;
        mAngles[2]=0;
        
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        
        mOrientationSensor=mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(this, mOrientationSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslatef(0f, 0f, -3f);
        
        if(mNumAngles>0) {
        	gl.glRotatef(mAngles[1]/mNumAngles+90,  1, 0, 0);
        	gl.glRotatef(-mAngles[2]/mNumAngles, 0, 0, 1);
        	gl.glRotatef(mAngles[0]/mNumAngles+180, 0, 1, 0);
        }
        
        mTurnTable.draw(gl);
    }

    public int[] getConfigSpec() {
        int[] configSpec = {
            EGL10.EGL_DEPTH_SIZE,   16,
            EGL10.EGL_NONE
        };
        return configSpec;
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
         gl.glViewport(0, 0, width, height);

         float ratio = (float) width / height;
         gl.glMatrixMode(GL10.GL_PROJECTION);
         gl.glLoadIdentity();
         gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
         gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);

      	 gl.glClearColor(0,0,1,1);

         gl.glEnable(GL10.GL_CULL_FACE);
         gl.glShadeModel(GL10.GL_SMOOTH);
         gl.glEnable(GL10.GL_DEPTH_TEST);
         gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
         
         mTurnTable.buildTextures(gl);
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
    	if(event.sensor==mOrientationSensor) {
    		if(mNumAngles==RING_BUFFER_SIZE) {
	    		mAngles[0]-=mAnglesRingBuffer[mRingBufferIndex][0];
	    		mAngles[1]-=mAnglesRingBuffer[mRingBufferIndex][1];
	    		mAngles[2]-=mAnglesRingBuffer[mRingBufferIndex][2];
    		} else {
    			mNumAngles++;
    		}
    		
    		mAnglesRingBuffer[mRingBufferIndex][0]=event.values[0];
    		mAnglesRingBuffer[mRingBufferIndex][1]=event.values[1];
    		mAnglesRingBuffer[mRingBufferIndex][2]=event.values[2];
    		
    		mAngles[0]+=mAnglesRingBuffer[mRingBufferIndex][0];
    		mAngles[1]+=mAnglesRingBuffer[mRingBufferIndex][1];
    		mAngles[2]+=mAnglesRingBuffer[mRingBufferIndex][2];
    		
    		//Log.i("mcompass", "0:"+mAngles[0]+", 1:"+mAngles[1]+", 2:"+mAngles[2]);
    		
    		mRingBufferIndex++;
    		if(mRingBufferIndex==RING_BUFFER_SIZE) {
    			mRingBufferIndex=0;
    		}
    	}
    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}

	public void onResume() {
		mSensorManager.registerListener(this, mOrientationSensor, SensorManager.SENSOR_DELAY_GAME);
	}

	public void onPause() {
		mSensorManager.unregisterListener(this, mOrientationSensor);
	}
}
