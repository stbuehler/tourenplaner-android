/*
 * Copyright 2012 ToureNPlaner
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.uni.stuttgart.informatik.ToureNPlaner.UI.Activities.MapScreen;

import android.content.Context;
import android.hardware.*;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import de.uni.stuttgart.informatik.ToureNPlaner.ToureNPlanerApplication;
import org.mapsforge.core.GeoPoint;

import java.lang.ref.WeakReference;
import java.util.List;

import static java.lang.Math.pow;

class GpsListener implements android.location.LocationListener, SensorEventListener {
	private final WeakReference<MapScreen> mapScreen;
	private boolean enabled;
	private boolean following;
	private GeoPoint lastKnownLocation = null;

	private static final String IDENTIFIER = "GpsListenerFollowing";
	private SensorManager sensorMgr;

	public GpsListener(MapScreen mapScreen, Bundle savedInstanceState, GeoPoint geoPoint) {
		this.mapScreen = new WeakReference<MapScreen>(mapScreen);
		lastKnownLocation = geoPoint;
		enabled = lastKnownLocation != null;
		if (savedInstanceState != null)
			following = savedInstanceState.getBoolean(IDENTIFIER, false);
		//TODO: too much battery drain when running all the time?
		sensorMgr = (SensorManager) ToureNPlanerApplication.getContext().getSystemService(Context.SENSOR_SERVICE);

		List<Sensor> sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (sensors.size() > 0) sensorGrav = sensors.get(0);

		sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
		if (sensors.size() > 0) sensorMag = sensors.get(0);

		//TODO: Y U NO WORK???
		//int sensordelay = SensorManager.SENSOR_DELAY_NORMAL;
		int sensordelay = (int) pow(10,6);
		sensorMgr.registerListener(this, sensorGrav, sensordelay);
		sensorMgr.registerListener(this, sensorMag, sensordelay);
	}

	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(IDENTIFIER, following);
	}

	private static final float grav[] = new float[3]; //Gravity (a.k.a accelerometer data)
	private static final float mag[] = new float[3]; //Magnetic
	private static final float rotation[] = new float[9]; //Rotation matrix in Android format
	private static final float orientation[] = new float[3]; //azimuth, pitch, roll
	private static float smoothed[] = new float[3];

	@Override
	public void onLocationChanged(Location location) {
		gmf = new GeomagneticField((float) location.getLatitude(),
				(float) location.getLongitude(),
				(float) location.getAltitude(),
				System.currentTimeMillis());
		GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
		lastKnownLocation = geoPoint;
		MapScreen ms = mapScreen.get();
		if (ms != null) {
			//TODO: Do we want to use bearing data from gps or is the compass enough for us?
			//ms.nodeOverlay.updateGpsMarker(geoPoint, location.hasBearing() ? location.getBearing() : -1);
			ms.nodeOverlay.updateGpsMarker(geoPoint);
			if (following) {
				ms.mapView.setCenter(geoPoint);
			}
		} else {
			Log.d("tp", "Updated location, but have no mapscreen to display it");
		}
	}

	@Override
	public void onStatusChanged(String s, int i, Bundle bundle) {
	}

	@Override
	public void onProviderEnabled(String s) {
		enabled = true;
		MapScreen ms = mapScreen.get();
		if (ms != null)
			ms.invalidateOptionsMenu();
	}

	@Override
	public void onProviderDisabled(String s) {
		enabled = false;
		lastKnownLocation = null;
		MapScreen ms = mapScreen.get();
		if (ms != null) {
			ms.nodeOverlay.updateGpsMarker(null);
			ms.invalidateOptionsMenu();
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isFollowing() {
		return following;
	}

	public void setFollowing(boolean following) {
		this.following = following;
		if (following && lastKnownLocation != null) {
			MapScreen ms = mapScreen.get();
			if (ms != null) {
				ms.mapView.setCenter(lastKnownLocation);
			}
		}
	}

	// http://code.google.com/p/android-compass/source/browse/trunk/src/com/jwetherell/compass/common/LowPassFilter.java
	public static float[] lowpassfilter(float[] input, float[] prev) {
		if (input == null || prev == null)
			throw new NullPointerException("input and prev float arrays must be non-NULL");
		if (input.length != prev.length)
			throw new IllegalArgumentException("input and prev must be the same length");

		for (int i = 0; i < input.length; i++) {
			prev[i] = prev[i] + 0.2f * (input[i] - prev[i]);
		}
		return prev;
	}

	private static GeomagneticField gmf = null;
	private static double floatBearing = 0;
	private static Sensor sensorGrav = null;
	private static Sensor sensorMag = null;

	@Override
	public void onSensorChanged(SensorEvent event) {
		//Log.d("tp", event.sensor.getName() + " Event");
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			smoothed = lowpassfilter(event.values, grav);
			grav[0] = smoothed[0];
			grav[1] = smoothed[1];
			grav[2] = smoothed[2];
		} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			smoothed = lowpassfilter(event.values, mag);
			mag[0] = smoothed[0];
			mag[1] = smoothed[1];
			mag[2] = smoothed[2];
		}

//		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//			grav[0] = event.values[0];
//			grav[1] = event.values[1];
//			grav[2] = event.values[2];
//		} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//			mag[0] = event.values[0];
//			mag[1] = event.values[1];
//			mag[2] = event.values[2];
//		}

		//Get rotation matrix given the gravity and geomagnetic matrices
		SensorManager.getRotationMatrix(rotation, null, grav, mag);
		SensorManager.getOrientation(rotation, orientation);
		//TODO: rotated screen doesn't work yet
		floatBearing = orientation[0];
		//Log.d("tp", "Azimuth: " + orientation[0] + " pitch " + orientation[1] + " roll " + orientation[2]);

		//Convert from radians to degrees
		floatBearing = Math.toDegrees(floatBearing); //degrees east of true north (180 to -180)

		//Compensate for the difference between true north and magnetic north
		if (gmf != null) floatBearing += gmf.getDeclination();

		//adjust to 0-360
		if (floatBearing < 0) floatBearing += 360;
		if (mapScreen != null && mapScreen.get() != null && mapScreen.get().nodeOverlay != null) {
			mapScreen.get().nodeOverlay.setDirection(floatBearing);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {
		//To change body of implemented methods use File | Settings | File Templates.
	}
}
