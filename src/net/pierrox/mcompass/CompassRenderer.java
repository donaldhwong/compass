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

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLSurfaceView;

class CompassRenderer implements GLSurfaceView.Renderer {
    private Turntable mTurnTable;

	private float mAzimuth;
	private float mPitch=-35;
	private float mRoll=-10;
    
    public CompassRenderer() {
        mTurnTable = new Turntable();
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

      	 gl.glClearColorx(1<<16, 1<<16, 1<<16, 1<<16);

         gl.glEnable(GL10.GL_CULL_FACE);
         gl.glShadeModel(GL10.GL_SMOOTH);
         gl.glEnable(GL10.GL_DEPTH_TEST);
         gl.glEnable(GL10.GL_NORMALIZE);
         gl.glEnable(GL10.GL_COLOR_MATERIAL);
         
         gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
         gl.glEnable(GL10.GL_BLEND);
         
         float lightAmbient[] = { 0.2f, 0.2f, 0.2f, 1.0f };
         float lightDiffuse[] = { 0.9f, 0.9f, 0.9f, 1.0f };
         float lightSpecular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
         float lightPosition[] = { 0f, 0f, 3f, 0.0f };
         float lightDirection[] = { 0f, 0f, -1f };
         
         gl.glEnable(GL10.GL_LIGHTING);
         gl.glEnable(GL10.GL_LIGHT0);

         gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbient, 0);
         gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuse, 0);
         gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, lightSpecular, 0);
         gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosition, 0);
         gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPOT_DIRECTION, lightDirection, 0);
         gl.glLightf(GL10.GL_LIGHT0, GL10.GL_SPOT_CUTOFF, 1.2f);
         gl.glLightf(GL10.GL_LIGHT0, GL10.GL_SPOT_EXPONENT, 20.0f);
         
         mTurnTable.buildTextures(gl);
    }
    
    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        
        gl.glTranslatef(0f, 0f, -3f);
        
        gl.glRotatef(mPitch+90,  1, 0, 0);
        gl.glRotatef(-mRoll, 0, 0, 1);
        gl.glRotatef(mAzimuth+180, 0, 1, 0);
        
        gl.glTranslatef(0f, 0.7f, 0f);
        
        mTurnTable.draw(gl);
    }
    
    public void setOrientation(float azimuth, float pitch, float roll) {
    	mAzimuth=azimuth;
    	mPitch=pitch;
    	mRoll=roll;
    }
    
    public void setParameters(int detailsLevel, boolean reversedRing) {
    	mTurnTable.setDetailsLevel(detailsLevel);
    	mTurnTable.setReversedRing(reversedRing);
    }
}
