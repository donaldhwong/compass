package net.pierrox.mcompass;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class MCompass extends Activity {
    private CompassRenderer mCompassRenderer;
    private GLSurfaceView mGLSurfaceView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mCompassRenderer = new CompassRenderer(this);
        mGLSurfaceView = new GLSurfaceView(this);
        mGLSurfaceView.setRenderer(mCompassRenderer);
        setContentView(mGLSurfaceView);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResume();
        mCompassRenderer.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGLSurfaceView.onPause();
        mCompassRenderer.onPause();
    }
}
