package de.unistuttgart.informatik.OfflineToureNPlaner.Graph;

import android.util.Log;

import com.carrotsearch.hppc.IntArrayDeque;
import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntIntOpenHashMap;

/**
 * Calculates the shortest path between two nodes in a {@link NestedGraph}
 */
public final class Dijkstra {
	/** Graph to calculate paths in */
	private final NestedGraph graph;

	/**
	 * @param graph Graph to calculate paths in
	 */
	public Dijkstra(NestedGraph graph) {
		this.graph = graph;
	}

	/**
	 * Nodes on the shortest path after {@link #run(int, int)} returned true.
	 */
	public IntArrayDeque path_nodes = new IntArrayDeque();
	/**
	 * Edges between {@link #path_nodes nodes} on the shortest path after {@link #run(int, int)} returned true.
	 */
	public IntArrayDeque path_edges = new IntArrayDeque();
	/**
	 * Sum of the {@link #path_edges edge} distances after {@link #run(int, int)} returned true.
	 */
	public int path_length;

	/**
	 * Fill public result values {@link #path_nodes}, {@link #path_edges}, {@link #path_length} from internal state.
	 */
	private void backtrack(int start, int dest, IntArrayList nodeData, IntIntOpenHashMap nodeDataIndex) {
		for (int n = dest; n != start;) {
			final int ndx = nodeDataIndex.get(n);
			path_nodes.addFirst(n);
			path_edges.addFirst(nodeData.get(ndx+2));
			n = nodeData.get(ndx+1);
		}
		path_nodes.addFirst(start);
		// Log.d("Dijkstra", "-- temporary sizes: " + nodeData.size() + " " + nodeDataIndex.size());
	}

	/**
	 * Calculates the shortest path between {@code start} and {@code destination}.
	 * <p>
	 * Clears {@link #path_nodes}, {@link #path_edges}, {@link #path_length}, whether returning true or not.
	 *
	 * @param start
	 * @param destination
	 * @return true when a path was found, false of no path was found
	 */
	public boolean run(int start, int destination) {
		final long startTime = System.currentTimeMillis();
		path_edges.clear();
		path_nodes.clear();
		path_length = 0;

		final IntArrayList nodeData = new IntArrayList(16384);
		final IntIntOpenHashMap nodeDataIndex = new IntIntOpenHashMap(4096);

		/* store node id with distance; each time a new shortest way to a node is found, a new entry is created - the old is not removed */
		final Heap heap = new Heap();
		heap.insert(start, 0);

		nodeDataIndex.put(start, nodeData.size());
		nodeData.add(0);

		final NestedGraphEdgeIterator iterator = new NestedGraphEdgeIterator();

		while (!heap.isEmpty()) {
			final int nodeId = heap.peekMinId();
			final int nodeDist = heap.peekMinDist();
			heap.removeMin();

			if (nodeId == destination) {
				path_length = nodeDist;
				backtrack(start, destination, nodeData, nodeDataIndex);
				Log.d("Dijkstra", "<- Found path in " + (System.currentTimeMillis() - startTime) + "ms");
				return true;
			}

			if (nodeDist > nodeData.get(nodeDataIndex.get(nodeId))) {
				// we already have this node on a shorter path - see heap comment above
				continue;
			}

			iterator.load(graph, nodeId);
			while (iterator.next()) {
				int nextDist = nodeDist + iterator.dist;
				int nextTarget = iterator.target;

				final int nodeDataSize = nodeData.size();
				final int ndx = nodeDataIndex.putOrAdd(nextTarget, nodeDataSize, 0);
				if (ndx == nodeDataSize) {
					nodeData.add(nextDist);
					nodeData.add(nodeId, iterator.edgeid);
					heap.insert(nextTarget, nextDist); // new entry
				} else if (nextDist < nodeData.get(ndx)) {
					nodeData.set(ndx, nextDist);
					nodeData.set(ndx+1, nodeId);
					nodeData.set(ndx+2, iterator.edgeid);
					heap.insert(nextTarget, nextDist); // new entry - old entries to nextTarget with longer distance are not removed
				}
			}
		}

		Log.d("Dijkstra", "<- Found no path in " + (System.currentTimeMillis() - startTime) + "ms");
		return false;
	}
}
