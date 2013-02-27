package de.unistuttgart.informatik.OfflineToureNPlaner.Data;

import java.io.Serializable;

import org.mapsforge.core.GeoPoint;

import android.location.Location;

/**
 * immutable Position object
 */
public final class Position implements Serializable {
	private static final long serialVersionUID = 1L;

	final int latitudeE7;
	final int longitudeE7;

	private Position(int latitudeE7, int longitudeE7) {
		this.latitudeE7 = latitudeE7;
		this.longitudeE7 = longitudeE7;
	}

	public static Position fromE7(int latitudeE7, int longitudeE7) {
		return new Position(latitudeE7, longitudeE7);
	}

	public static Position fromE6(int latitudeE6, int longitudeE6) {
		return new Position(latitudeE6 * 10, longitudeE6 * 10);
	}

	public static Position from(double latitude, double longitude) {
		return new Position((int) (latitude * 1e7), (int) (longitude * 1e7));
	}

	public static Position from(GeoPoint point) {
		return point == null ? null : Position.fromE6(point.latitudeE6, point.longitudeE6);
	}

	public static Position from(Location location) {
		return location == null ? null : from(location.getLatitude(), location.getLongitude());
	}

	public static boolean equals(Position a, Position b) {
		if (a == null || b == null) return a == b;
		return (a.latitudeE7 == b.latitudeE7) && (a.longitudeE7 == b.longitudeE7);
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Position)) return false;
		return equals(this, (Position) other);
	}

	public int latitudeE7() {
		return latitudeE7;
	}

	public int longitudeE7() {
		return longitudeE7;
	}

	public int latitudeE6() {
		return latitudeE7 / 10;
	}

	public int longitudeE6() {
		return longitudeE7 / 10;
	}

	public double latitude() {
		return latitudeE7 / 1.0e7d;
	}

	public double longitude() {
		return longitudeE7 / 1.0e7d;
	}

	public GeoPoint toPoint() {
		return new GeoPoint(latitudeE6(), longitudeE6());
	}

	@Override
	public String toString() {
		return String.format(java.util.Locale.US, "[%+12.7f N %+12.7f W]", latitude(), longitude());
	}
}
