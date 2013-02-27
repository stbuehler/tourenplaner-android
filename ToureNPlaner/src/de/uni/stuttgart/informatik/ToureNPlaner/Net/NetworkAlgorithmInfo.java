package de.uni.stuttgart.informatik.ToureNPlaner.Net;

import java.util.ArrayList;

import de.uni.stuttgart.informatik.ToureNPlaner.Data.AlgorithmInfo;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.Node;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.Constraints.ConstraintType;
import de.uni.stuttgart.informatik.ToureNPlaner.Handler.AlgorithmRequestNN;
import de.uni.stuttgart.informatik.ToureNPlaner.Handler.AlgorithmRequest;
import de.uni.stuttgart.informatik.ToureNPlaner.Handler.Observer;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler.RequestHandler;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler.ServerRequestNN;

public class NetworkAlgorithmInfo implements AlgorithmInfo {
	final private String version;
	final private String name;
	final private String description;
	final private String urlsuffix;
	final private int minPoints;
	final private int maxPoints;
	final private boolean sourceIsTarget;
	final private boolean isHidden;
	final private ArrayList<ConstraintType> constraintTypes;
	final private ArrayList<ConstraintType> pointConstraintTypes;

	public NetworkAlgorithmInfo(String version, String name, String description, String urlsuffix, int minPoints,
			int maxPoints, boolean sourceIsTarget, boolean isHidden,
			ArrayList<ConstraintType> constraintTypes, ArrayList<ConstraintType> pointConstraintTypes) {
		this.version = version;
		this.name = name;
		this.description = description;
		this.urlsuffix = urlsuffix;
		this.minPoints = minPoints;
		this.maxPoints = maxPoints;
		this.sourceIsTarget = sourceIsTarget;
		this.isHidden = isHidden;
		this.constraintTypes = constraintTypes;
		this.pointConstraintTypes = pointConstraintTypes;
	}

	@Override
	public final String toString() {
		return getName();
	}

	@Override
	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean sourceIsTarget() {
		return sourceIsTarget;
	}

	@Override
	public String getUrlsuffix() {
		return urlsuffix;
	}

	@Override
	public boolean isHidden() {
		return isHidden;
	}

	@Override
	public int getMinPoints() {
		return minPoints;
	}

	@Override
	public int getMaxPoints() {
		return maxPoints;
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
		final RequestHandler handler = new RequestHandler(listener, session);
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
