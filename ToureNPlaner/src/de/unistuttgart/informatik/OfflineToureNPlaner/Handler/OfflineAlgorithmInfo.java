package de.unistuttgart.informatik.OfflineToureNPlaner.Handler;

import java.util.ArrayList;

import de.uni.stuttgart.informatik.ToureNPlaner.Data.AlgorithmInfo;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.Node;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.Constraints.ConstraintType;
import de.uni.stuttgart.informatik.ToureNPlaner.Handler.AlgorithmRequestNN;
import de.uni.stuttgart.informatik.ToureNPlaner.Handler.AlgorithmRequest;
import de.uni.stuttgart.informatik.ToureNPlaner.Handler.Observer;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Session;

public class OfflineAlgorithmInfo implements AlgorithmInfo {
	final private ArrayList<ConstraintType> constraintTypes = new ArrayList<ConstraintType>();
	final private ArrayList<ConstraintType> pointConstraintTypes = new ArrayList<ConstraintType>();

	@Override
	public final String toString() {
		return getName();
	}

	@Override
	public String getName() {
		// TODO translate
		return "Offline Shortest Path CH";
	}

	@Override
	public String getDescription() {
		// TODO translate
		return "Calculates the shortest path with offline graph data";
	}

	@Override
	public boolean sourceIsTarget() {
		return false;
	}

	@Override
	public String getUrlsuffix() {
		return null;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public int getMinPoints() {
		return 2;
	}

	@Override
	public int getMaxPoints() {
		return 2;
	}

	@Override
	public ArrayList<ConstraintType> getPointConstraintTypes() {
		return pointConstraintTypes;
	}

	@Override
	public ArrayList<ConstraintType> getConstraintTypes() {
		return constraintTypes;
	}

	@Override
	public AlgorithmRequest execute(Observer listener, Session session) {
		final OfflineHandler handler = new OfflineHandler(listener, session);
		handler.execute();
		return handler;
	}

	@Override
	public AlgorithmRequestNN executeNN(Observer listener, Session session, Node node) {
		final OfflineNNHandler handler = new OfflineNNHandler(listener, node);
		handler.execute();
		return handler;
	}
}
