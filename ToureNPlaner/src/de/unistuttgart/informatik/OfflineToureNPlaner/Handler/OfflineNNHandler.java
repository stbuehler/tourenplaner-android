package de.unistuttgart.informatik.OfflineToureNPlaner.Handler;

import java.io.IOException;

import org.mapsforge.core.GeoPoint;

import android.preference.PreferenceManager;
import android.util.Log;
import de.uni.stuttgart.informatik.ToureNPlaner.ToureNPlanerApplication;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.Node;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.Result;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.ResultNode;
import de.uni.stuttgart.informatik.ToureNPlaner.Handler.AlgorithmRequestNN;
import de.uni.stuttgart.informatik.ToureNPlaner.Handler.Observer;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler.AsyncHandler;
import de.unistuttgart.informatik.OfflineToureNPlaner.Data.Position;
import de.unistuttgart.informatik.OfflineToureNPlaner.Graph.GraphReader;

public final class OfflineNNHandler extends AsyncHandler implements AlgorithmRequestNN {
	private final Node node;

	public OfflineNNHandler(Observer listener, Node node) {
		super(listener);
		this.node = node;
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	protected Object doInBackground(Void... arg0) {
		Result result = null;
		GraphReader reader = null;

		final String graphLocation = PreferenceManager.getDefaultSharedPreferences(ToureNPlanerApplication.getContext()).getString("offline_ch_location", "");

		try {
			reader = new GraphReader(new java.io.File(graphLocation));
			int nodeID = reader.findPoint(Position.from(node.getGeoPoint()));
			if (-1 == nodeID) return null;
			GeoPoint point = new GeoPoint(reader.directGeo_lat / 10, reader.directGeo_lon / 10);

			result = new Result();
			ResultNode rnStart = new ResultNode(point);
			result.getPoints().add(rnStart);
		} catch (IOException e) {
			Log.e("Offline TP", "IOException", e);
			return e;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		} finally {
			try {
				if (null != reader) reader.close();
			} catch (IOException e) {
			}
		}

		return result;
	}
}
