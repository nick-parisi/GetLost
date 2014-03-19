package edu.bc.cs.parisin.cs344s14.getlost; // !!!!!!!!!!!!!!  USE YOUR OWN PACKAGE NAME HERE !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

// Adapted from http://www.codingforandroid.com/2011/01/using-orientation-sensors-simple.html

// Need android.permission.ACCESS_COARSE_LOCATION
// and  android.permission.ACCESS_FINE_LOCATION

interface CompassListener {
	public void onCompassChanged(float azimuth, float pitch, float roll);
};

public class Compass implements SensorEventListener {
	private float azimuth = Float.NaN, pitch=Float.NaN, roll=Float.NaN;
	private float[] mGravity     = {Float.NaN, Float.NaN, Float.NaN};
	private float[] mGeomagnetic = {Float.NaN, Float.NaN, Float.NaN};
	private SensorManager mSensorManager;
	private Sensor accelerometer, magnetometer;
	private CompassListener listener = null;

	public Compass(Context context) {
		mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
		accelerometer  = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer   = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}
	
	public float getAzimuth() { return (float)Math.toDegrees(azimuth); } // compass direction
	public float getPitch()   { return (float)Math.toDegrees(pitch  ); }
	public float getRoll()    { return (float)Math.toDegrees(roll   ); }
	
	public float getMag(int which) { 
		if (mGeomagnetic!=null)
			return mGeomagnetic[which];
		else return Float.NaN;
	}
	
	public void setListener(CompassListener listener) {
		this.listener = listener;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// ignore, not much we can do about it except maybe show some user visible indicator
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			for (int i=0; i<3; ++i) mGravity[i] = event.values[i];
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			for (int i=0; i<3; ++i) mGeomagnetic[i] = event.values[i];
		if (!Float.isNaN(mGravity[0]) && !Float.isNaN(mGeomagnetic[0])) {
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				azimuth = orientation[0]; // orientation contains: azimuth, pitch and roll
				pitch   = orientation[1];
				roll    = orientation[2];
				if (listener != null)
					listener.onCompassChanged(getAzimuth(), getPitch(), getRoll());
			} else {
				Log.e("GetLost", "Couldn't get rotation matrix");
			}
		}
	}

	// Call this from your Activity's onResume() method
	public void startListening() {
		boolean accel    = mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
		boolean magnetic = mSensorManager.registerListener(this, magnetometer,  SensorManager.SENSOR_DELAY_UI);
		if (!accel)
			Log.e("GetLost", "Unable to listen to accelerometer");
		if (!magnetic)
			Log.e("GetLost", "Unable to listen to magnetometer");
	}

	// Call this from your Activity's onPause() method
	public void stopListening() {
		mSensorManager.unregisterListener(this);
	}
}