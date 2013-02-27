package de.unistuttgart.informatik.OfflineToureNPlaner.Handler;

import java.io.IOException;
import java.util.ArrayList;

import org.mapsforge.core.GeoPoint;

import android.preference.PreferenceManager;
import android.util.Log;

import de.uni.stuttgart.informatik.ToureNPlaner.ToureNPlanerApplication;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.Node;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.Result;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.ResultNode;
import de.uni.stuttgart.informatik.ToureNPlaner.Handler.AlgorithmRequest;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler.AsyncHandler;
import de.unistuttgart.informatik.OfflineToureNPlaner.Data.Position;
import de.unistuttgart.informatik.OfflineToureNPlaner.Graph.Dijkstra;
import de.unistuttgart.informatik.OfflineToureNPlaner.Graph.GraphReader;
import de.unistuttgart.informatik.OfflineToureNPlaner.Graph.NestedGraph;
import de.unistuttgart.informatik.OfflineToureNPlaner.Graph.NestedSimpleGraph;

public class OfflineHandler extends AsyncHandler implements AlgorithmRequest {
	private final Node nodeStart, nodeDest;

	/**
	 * The constant used to compute time traveled on an edge from it's
	 * non euclidian distance value by time=getDist(edgeId)*travelTimeConstant
	 * time is in seconds
	 */
	public static final double travelTimeConstant = 0.02769230769230769230769230769230769;

	public OfflineHandler(de.uni.stuttgart.informatik.ToureNPlaner.Handler.Observer listener, de.uni.stuttgart.informatik.ToureNPlaner.Net.Session session) {
		super(listener);

		ArrayList<Node> points = session.getNodeModel().getNodeVector();
		assert(2 == points.size());

		nodeStart = points.get(0);
		nodeDest = points.get(1);
	}

	private static NestedGraph coreGraph = null;
	protected static synchronized NestedGraph loadCoreGraph(GraphReader reader) throws IOException, InterruptedException {
		if (coreGraph == null) coreGraph = reader.loadCoreGraph();
		return coreGraph;
	}

	private static String geoPointStr(GeoPoint point) {
		return "GeoPoint(" + point.latitudeE6 + ", " + point.longitudeE6 + ")";
	}

	@Override
	protected Object doInBackground(Void... arg0) {
		Result result = null;
		GraphReader reader = null;

		final String graphLocation = PreferenceManager.getDefaultSharedPreferences(ToureNPlanerApplication.getContext()).getString("offline_ch_location", "");

		try {
			long startTime = System.currentTimeMillis();

			Log.d("Offline TP", "** Search path from: " + geoPointStr(nodeStart.getGeoPoint()) + " -> " + geoPointStr(nodeDest.getGeoPoint()));
			reader = new GraphReader(new java.io.File(graphLocation));
			reader.openNCache(32); // each slot needs 4k memory

			// Log.d("Offline TP", "-- Searching start node...");
			int start = reader.findPoint(Position.from(nodeStart.getGeoPoint()));

			// Log.d("Offline TP", "-- Searching destination node...");
			int dest = reader.findPoint(Position.from(nodeDest.getGeoPoint()));

			if (-1 == start || -1 == dest) {
				Log.e("Offline TP", "** Couldn't find nodes for start/destination points");
				return null;
			}

			Log.d("Offline TP", "** Search path from: " + start + " -> " + dest);

			// Log.d("Offline TP", "-- Loading core graph...");
			NestedGraph coreGraph = loadCoreGraph(reader);

			// Log.d("Offline TP", "-- Reading outgoing edges transitively from source...");
			NestedSimpleGraph outGraph = reader.createGraphWithoutCore(start, true);
			outGraph.setParent(coreGraph);
			// NestedSimpleGraph graph = new NestedSimpleGraph(coreGraph);
			// reader.fillGraphWithoutCore(graph, start, true);

			// Log.d("Offline TP", "-- Reading incoming edges transitively from destination...");
			NestedSimpleGraph inGraph = reader.createGraphWithoutCore(dest, false);
			inGraph.setParent(outGraph);
			// reader.fillGraphWithoutCore(graph, dest, false);

			// Log.d("Offline TP", "-- Search shortest path: running Dijkstra...");
			Dijkstra dijkstra = new Dijkstra(inGraph);
			if (!dijkstra.run(start, dest)) {
				Log.e("Offline TP", "** Dijkstra didn't find a path");
				return null;
			}

			// Log.d("Offline TP", "-- Expanding shortcuts...");
			reader.expandShortcuts(dijkstra.path_nodes, dijkstra.path_edges);

			// Log.d("Offline TP", "-- Loading coords for path...");
			reader.loadWayCoords();

			// --------------------------------------------------------------------------------
			result = new Result();
			result.getMisc().setDistance(reader.path_euclid_length);
			result.getMisc().setTime((float) (dijkstra.path_length * travelTimeConstant));
			final int[] way_coords = new int[reader.way_coords.length];
			for (int i = 0; i < way_coords.length; i += 2) {
				way_coords[i] = reader.way_coords[i+1] / 10;
				way_coords[i+1] = reader.way_coords[i] / 10;
			};
			result.setWay(new int[][] { way_coords });

			{
				ResultNode rnStart = new ResultNode(new GeoPoint(reader.way_coords[0]/10, reader.way_coords[1]/10));
				int len = reader.way_coords.length;
				ResultNode rnDest = new ResultNode(new GeoPoint(reader.way_coords[len-2]/10, reader.way_coords[len-1]/10));
				result.getPoints().add(rnStart);
				result.getPoints().add(rnDest);
			}
			// --------------------------------------------------------------------------------

			Log.d("Offline TP", "** completed in " + (System.currentTimeMillis() - startTime) + "ms");
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
