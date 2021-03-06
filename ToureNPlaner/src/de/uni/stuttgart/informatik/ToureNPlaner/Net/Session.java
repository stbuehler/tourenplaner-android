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

package de.uni.stuttgart.informatik.ToureNPlaner.Net;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;
import de.uni.stuttgart.informatik.ToureNPlaner.ClientSideCompute.SimpleGraph;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.*;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.Constraints.Constraint;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.Constraints.ConstraintType;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.Edits.NodeModel;
import de.uni.stuttgart.informatik.ToureNPlaner.Handler.AlgorithmRequest;
import de.uni.stuttgart.informatik.ToureNPlaner.Handler.Observer;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler.*;
import de.uni.stuttgart.informatik.ToureNPlaner.R;
import de.uni.stuttgart.informatik.ToureNPlaner.ToureNPlanerApplication;
import de.uni.stuttgart.informatik.ToureNPlaner.UI.TBTNavigation;
import de.uni.stuttgart.informatik.ToureNPlaner.Util.Base64;
import org.mapsforge.core.GeoPoint;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Session implements Serializable {
	public static final String IDENTIFIER = "session";
	public static final String DIRECTORY = "session";
	public static SimpleNetworkHandler simplehandler = null;
	public static AlgorithmRequest sesshandler = null;

	double direction = 0;
	public boolean compassenabled = false;

	public double getDirection() {
		return direction;
	}

	/**
	 * This only sets the direction, it does not update the gps marker. For the marker to be updated on the screen,
	 * mapScreen.get().nodeOverlay.updateGPSDrawableDirection() needs to be called
	 */
	public void setDirection(double floatBearing) {
		direction = floatBearing;
		//mapScreen.get().nodeOverlay.setGPSDrawableDirection(floatBearing);
	}


	private static class Data implements Serializable {
		private URL url;
		private String username;
		private String password;
		private ArrayList<AlgorithmInfo> algorithms;
		private AlgorithmInfo selectedAlgorithm;
		private User user;
		private ArrayList<Constraint> constraints;
		private int nameCounter;
	}

	private final UUID uuid;
	private static transient Data d;
	private static transient NodeModel nodeModel = new NodeModel();
	private static transient Result result;
	private static transient SyncCoreLoader cl;
	private static transient TBTResult tbtresult;
	public static transient TBTNavigation nav;

	public static File openCacheDir() {
		return new File(ToureNPlanerApplication.getContext().getCacheDir(), DIRECTORY);
	}

	public Session() {
		this.uuid = UUID.randomUUID();
		d = new Data();
		nodeModel = new NodeModel();
		result = new Result();
		cl = new SyncCoreLoader();
		tbtresult = new TBTResult();
		// Also initialize the files on the disc
		saveData();
		saveNodeModel();
		saveResult();
		savetbtResult();
	}

	private void save(Object o, String name) {
		try {
			File dir = new File(openCacheDir(), uuid.toString());
			dir.mkdirs();

			FileOutputStream outputStream = new FileOutputStream(new File(dir, name));
			ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new BufferedOutputStream(outputStream)));

			try {
				out.writeObject(o);
			} finally {
				out.close();
			}
		} catch (Exception e) {
			Log.e("ToureNPLaner", "Session saving failed", e);
		}
	}

	private void saveData() {
		save(d, "data");
		save(compassenabled, "compassenabled");
		//maybe we don't have turn by turn navigation data but if we have, we want to save it
		if (nav != null) {
			save(nav, "nav");
		}
	}

	private void saveNodeModel() {
		save(nodeModel, "nodeModel");
	}

	private void saveResult() {
		save(result, "result");
	}

	private void savetbtResult() {
		save(tbtresult, "tbtresult");
	}

	private void loadAll() {
		d = (Data) load("data");
		nodeModel = (NodeModel) load("nodeModel");
		if (nodeModel == null)
			nodeModel = new NodeModel();
		result = (Result) load("result");
		nav = (TBTNavigation) load("nav");
		compassenabled = (Boolean) load("compassenabled");
	}

	private Object load(String name) {
		try {
			File dir = new File(openCacheDir(), uuid.toString());

			FileInputStream inputStream = new FileInputStream(new File(dir, name));

			ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new BufferedInputStream(inputStream)));
			try {
				return in.readObject();
			} finally {
				in.close();
			}
		} catch (Exception e) {
			Log.e("ToureNPLaner", "Session loading failed", e);
		}
		return null;
	}

	public static final int MODEL_CHANGE = 1;
	public static final int RESULT_CHANGE = 2;
	public static final int NNS_CHANGE = 4;
	public static final int ADD_CHANGE = 8;
	public static final int DND_CHANGE = 16;
	public static final int TBT_RESULT_CHANGE = 32;

	public static class Change {
		private final int val;
		private Node node;

		public Change(int val) {
			this.val = val;
		}

		public void setNode(Node node) {
			this.node = node;
		}

		public Node getNode() {
			return node;
		}

		public boolean isModelChange() {
			return 0 < (val & MODEL_CHANGE);
		}

		public boolean isResultChange() {
			return 0 < (val & RESULT_CHANGE);
		}

		public boolean isTBTResultChange() {
			return 0 < (val & TBT_RESULT_CHANGE);
		}

		public boolean isNnsChange() {
			return 0 < (val & NNS_CHANGE);
		}

		public boolean isAddChange() {
			return 0 < (val & ADD_CHANGE);
		}

		public boolean isDndChange() {
			return 0 < (val & DND_CHANGE);
		}
	}

	public interface Listener {
		void onChange(Change change);
	}

	private transient WeakHashMap<Object, Listener> listeners = new WeakHashMap<Object, Listener>();

	private void readObject(java.io.ObjectInputStream in) throws ClassNotFoundException, IOException {
		in.defaultReadObject();
		if (d == null)
			loadAll();
		cl = new SyncCoreLoader();
		cl.setURL(d.url.toString());

		listeners = new WeakHashMap<Object, Listener>();
	}

	/**
	 * @param identifier must not have a reference to the listener a String constant or Class object is fine
	 * @param listener
	 */
	public void registerListener(Object identifier, Listener listener) {
		listeners.put(identifier, listener);
	}

	public void removeListener(Object identifier) {
		listeners.remove(identifier);
	}

	public void notifyChangeListerners(final Change change) {
		if (change.isModelChange() || change.isDndChange()) {
			nodeModel.incVersion();
		}

		if (nodeModel.size() == 0)
			d.nameCounter = 0;

		for (Listener listener : listeners.values()) {
			listener.onChange(change);
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (Session.class) {
					if (change.isModelChange()) {
						saveNodeModel();
					}
					if (change.isResultChange()) {
						saveResult();
					}
					if (change.isTBTResultChange()) {
						savetbtResult();
					}
				}
			}
		}).start();
	}

	public TBTNavigation getTBTNavigation() {
		return nav;
	}

	public Result getResult() {
		return result;
	}

	public void settbtResult(TBTResult result) {
		Session.tbtresult = result;
	}

	public TBTResult gettbtResult() {
		return Session.tbtresult;
	}

	public void setResult(Result result) {
		Session.result = result;
	}

	public void setUser(User user) {
		d.user = user;
		saveData();
	}

	public User getUser() {
		return d.user;
	}

	public NodeModel getNodeModel() {
		return nodeModel;
	}

	public void setURL(URL url) {
		d.url = url;
		cl.setURL(url.toString());
		saveData();
	}

	public URL getURL() {
		return d.url;
	}

	public boolean isPrivateURL() {
		return d.url.getProtocol().equals("https");
	}

	public void setAlgorithms(ArrayList<AlgorithmInfo> algorithms) {
		d.algorithms = algorithms;
		saveData();
	}

	public ArrayList<AlgorithmInfo> getAlgorithms() {
		return d.algorithms;
	}

	public HttpURLConnection openGetConnection(String path, String url) throws IOException {
		URL uri = new URL(url + path);

		HttpURLConnection con = (HttpURLConnection) uri.openConnection();

		if (isPrivateURL() && url.equals(getURL().toString())) {
			try {
				((HttpsURLConnection) con).setSSLSocketFactory(ToureNPlanerApplication.getSslContext().getSocketFactory());
				String userPassword = getUsername() + ":" + getPassword();
				String encoding = Base64.encodeString(userPassword);
				con.setRequestProperty("Authorization", "Basic " + encoding);
			} catch (Exception e) {
				Log.e("TP", "SSL", e);
			}
		}

		con.setRequestProperty("Accept",
				JacksonManager.ContentType.SMILE.identifier + ", " + JacksonManager.ContentType.JSON.identifier);

		return con;
	}

	public HttpURLConnection openGetConnection(String path) throws IOException {
		return openGetConnection(path, getURL().toString());
	}

	public HttpURLConnection openPostConnection(String path, String url) throws IOException {
		HttpURLConnection con = openGetConnection(path, url);

		con.setDoOutput(true);
		con.setChunkedStreamingMode(0);
		con.setRequestProperty("Content-Type", "application/json;");

		return con;
	}

	public HttpURLConnection openPostConnection(String path) throws IOException {
		return openPostConnection(path, getURL().toString());
	}

	public String getUsername() {
		return d.username;
	}

	public String getPassword() {
		return d.password;
	}

	public AlgorithmInfo getSelectedAlgorithm() {
		return d.selectedAlgorithm;
	}

	public void setSelectedAlgorithm(AlgorithmInfo selectedAlgorithm) {
		if (!selectedAlgorithm.equals(d.selectedAlgorithm)) {
			d.nameCounter = 0;
			nodeModel.clear();
			result = null;
			d.selectedAlgorithm = selectedAlgorithm;
			d.constraints = new ArrayList<Constraint>(selectedAlgorithm.getConstraintTypes().size());
			for (int i = 0; i < selectedAlgorithm.getConstraintTypes().size(); i++) {
				d.constraints.add(new Constraint(selectedAlgorithm.getConstraintTypes().get(i)));
			}
			saveData();
		}
	}

	private static String createName(int num) {
		String str = "";

		int modulo;
		num++;
		while (num > 0) {
			modulo = (num - 1) % 26;
			str = Character.toString((char) (modulo + 'A')) + str;
			num = (num - modulo) / 26;
		}

		return str;
	}

	public Node createNode(GeoPoint geoPoint) {
		if (nodeModel.size() >= getSelectedAlgorithm().getMaxPoints()) {
			return null;
		}
		String name = createName(d.nameCounter++);
		return new Node(nodeModel.getVersion(), name, name, geoPoint, d.selectedAlgorithm.getPointConstraintTypes());
	}

	public ArrayList<Constraint> getConstraints() {
		return d.constraints;
	}

	public void setConstraints(ArrayList<Constraint> constraints) {
		d.constraints = constraints;
	}

	public void setUsername(String username) {
		d.username = username;
		saveData();
	}

	public void setPassword(String password) {
		d.password = password;
		saveData();
	}

	/**
	 * @param url      The URL to connect to
	 * @param listener the Callback listener
	 * @return Use this to cancel the task with cancel(true)
	 */
	public static ServerInfoHandler createSession(String url, Observer listener) {
		ServerInfoHandler handler = new ServerInfoHandler(listener, url);
		handler.execute();
		return handler;
	}

	public class RequestInvalidException extends Exception {
		public RequestInvalidException(String detailMessage) {
			super(detailMessage);
		}
	}

	private String canPerformReason() {
		String msg = "";

		// Check if every algorithm constraint is set
		for (int i = 0; i < d.constraints.size(); i++) {
			if (d.constraints.get(i).getValue() == null) {
				msg += ToureNPlanerApplication.getContext().getString(R.string.algorithm_constraint_notset);
				break;
			}
		}

		// Check if every point constraint is set
		if (d.selectedAlgorithm.getPointConstraintTypes().isEmpty())
			if (!nodeModel.allSet())
				msg += ToureNPlanerApplication.getContext().getString(R.string.point_constraints_notset);

		if (nodeModel.size() < d.selectedAlgorithm.getMinPoints() ||
				nodeModel.size() > d.selectedAlgorithm.getMaxPoints()) {
			msg += ToureNPlanerApplication.getContext().getString(R.string.points_notset);
		}

		return msg;
	}

	public boolean canPerformRequest() {
		return canPerformReason().equals("");
	}

	public SimpleGraph getCoreGraph() throws IOException {
		return cl.getCoreGraph();
	}

	public AlgorithmRequest performRequest(Observer requestListener, boolean force) throws RequestInvalidException {
		if (canPerformRequest() && (force || result == null || nodeModel.getVersion() != result.getVersion())) {
			return d.selectedAlgorithm.execute(requestListener, this);
		} else {
			throw new RequestInvalidException(canPerformReason());
		}
	}

	public AlgorithmRequest performtbtRequestPreparation(final String tbtip) throws RequestInvalidException {
		if (getNodeModel().getNodeVector().size() < 1) {
			throw new RequestInvalidException(ToureNPlanerApplication.getContext().getString(R.string.needtargetmarker));
		}

		Location loc = ((LocationManager) ToureNPlanerApplication.getContext().getSystemService(Context.LOCATION_SERVICE)).getLastKnownLocation(LocationManager.GPS_PROVIDER);

		// if we want to change the first node's position to our position we use this:
		// nodevector.get(0).setGeoPoint(new GeoPoint(loc.getLatitude(), loc.getLongitude()));

		// instead we create a new node at our current position
		// or, if the user has already done a tbt request and is doing a new one now, the first node will be the special "Start" node and we will move it to the current gps position
		if (!getNodeModel().getNodeVector().get(0).getName().equals("Start")) {
			if (loc == null) {
				// android does not provide us with a last location :(
				// use the first node the user set or someething. Am I doing this right?
				loc = new Location("reverseGeocoded");
				loc.setLatitude(getNodeModel().getNodeVector().get(0).getLaE7()/1e7);
				loc.setLongitude(getNodeModel().getNodeVector().get(0).getLoE7() / 1e7);
			}
			for (Node n: getNodeModel().getNodeVector()) {
				n.setId(n.getId() + 1);
			}
			getNodeModel().getNodeVector().add(0, new Node(getNodeModel().getNodeVector().get(0).getId() - 1, "Start", "start", new GeoPoint(loc.getLatitude(), loc.getLongitude()), new ArrayList<ConstraintType>()));
			getNodeModel().incVersion();
		} else {
			// if we already have a start node, but no current location, there's no point in doing anything
			if (loc != null) {
				// should be the startnode
				Node n = getNodeModel().getNodeVector().get(0);
				if (!n.getName().equals("Start")) {
					//well, shit, what am I doing?
					return null;
				}
				n.setGeoPoint(new GeoPoint(loc.getLatitude(), loc.getLongitude()));
				getNodeModel().incVersion();
			}
		}
		notifyChangeListerners(new Session.Change(Session.MODEL_CHANGE));

		// We will get a new route from the ToureNPlaner server and after that is finished we let the TBTRequester be notified and it will make the tbt request
		return performRequest(new TBTRequester(tbtip), true);
	}

	private class TBTRequester implements Observer {

		private String tbtip;

		public TBTRequester(String tbtip) {
			this.tbtip = tbtip;
		}
		@Override
		public void onCompleted(Object caller, Object object) {
			nav.initTBT(tbtip);
		}

		@Override
		public void onError(Object caller, Object object) {
			Toast.makeText(ToureNPlanerApplication.getContext(),"Error:\n" + object.toString(), Toast.LENGTH_LONG).show();
		}
	}
}
