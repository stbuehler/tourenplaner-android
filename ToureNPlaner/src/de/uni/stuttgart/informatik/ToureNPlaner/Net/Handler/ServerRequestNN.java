package de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler;

import java.util.ArrayList;

import de.uni.stuttgart.informatik.ToureNPlaner.Data.Node;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.Constraints.Constraint;
import de.uni.stuttgart.informatik.ToureNPlaner.Handler.AlgorithmRequestNN;
import de.uni.stuttgart.informatik.ToureNPlaner.Handler.Observer;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Session;

public final class ServerRequestNN extends RequestHandler implements AlgorithmRequestNN {
	final Node node;

	public ServerRequestNN(Observer listener, Session session, Node node) {
		super(listener, session);
		this.node = node;
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	protected String getSuffix() {
		return "/algnns";
	}
	
	@Override
	protected ArrayList<Node> getNodes() {
		ArrayList<Node> nodes = new ArrayList<Node>(1);
		nodes.add(node);
		return nodes;
	}

	@Override
	protected ArrayList<Constraint> getConstraints() {
		return null;
	}
}
