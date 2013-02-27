package de.uni.stuttgart.informatik.ToureNPlaner.ClientSideCompute;

import java.util.ArrayList;

import de.uni.stuttgart.informatik.ToureNPlaner.Data.AlgorithmInfo;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.Node;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.Constraints.ConstraintType;
import de.uni.stuttgart.informatik.ToureNPlaner.Handler.AlgorithmRequestNN;
import de.uni.stuttgart.informatik.ToureNPlaner.Handler.AlgorithmRequest;
import de.uni.stuttgart.informatik.ToureNPlaner.Handler.Observer;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Session;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler.ServerRequestNN;

public final class ClientSideComputeAlgorithmInfo implements AlgorithmInfo {
	final private ArrayList<ConstraintType> constraintTypes = new ArrayList<ConstraintType>();
	final private ArrayList<ConstraintType> pointConstraintTypes = new ArrayList<ConstraintType>();

	@Override
	public final String toString() {
		return getName();
	}

	@Override
	public String getName() {
		return "DORC";
	}

	@Override
	public String getDescription() {
		return "Shortest Path computed on the client (experimental)";
	}

	@Override
	public boolean sourceIsTarget() {
		return false;
	}

	@Override
	public String getUrlsuffix() {
		return "updowng";
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
		final ClientComputeHandler handler = new ClientComputeHandler(listener, session);
		handler.execute();
		return handler;
	}

	@Override
	public AlgorithmRequestNN executeNN(Observer listener, Session session, Node node) {
		final ServerRequestNN handler = new ServerRequestNN(listener, session, node);
		handler.execute();
		return handler;
	}
}
