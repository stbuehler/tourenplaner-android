package de.uni.stuttgart.informatik.ToureNPlaner.UI.Overlays;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.InputType;
import android.view.HapticFeedbackConstants;
import android.widget.EditText;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.Edits.*;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.Node;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Session;
import de.uni.stuttgart.informatik.ToureNPlaner.R;
import de.uni.stuttgart.informatik.ToureNPlaner.UI.Activities.EditNodeScreen;
import de.uni.stuttgart.informatik.ToureNPlaner.UI.Activities.MapScreen.MapScreen;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.ItemizedOverlay;
import org.mapsforge.android.maps.overlay.OverlayItem;
import org.mapsforge.core.GeoPoint;

import java.util.ArrayList;

public class NodeOverlay extends ItemizedOverlay<OverlayItem> implements Session.Listener {
	private ArrayList<OverlayItem> list = new ArrayList<OverlayItem>();

	private MapScreen mapScreen;
	private final Session session;

	private static final int GPS_RADIUS = 10;
	private OverlayItem gpsMarker;
	private String constraintValue;
	private boolean useGps = false;

	private GpsDrawable gpsDrawable;

	public NodeOverlay(MapScreen mapScreen, Session session, GeoPoint gpsPoint) {
		// Just a workaround until the icons are loaded
		super(boundCenterBottom(mapScreen.getResources().getDrawable(R.drawable.marker)));
		this.session = session;
		this.mapScreen = mapScreen;

		setupGpsDrawable();

		loadFromModel();
		updateGpsMarker(gpsPoint);
	}

	public void setMapScreen(MapScreen mapScreen) {
		this.mapScreen = mapScreen;
	}

	private void setupGpsDrawable() {
		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setColor(Color.YELLOW);
		p.setAlpha(128);
		gpsDrawable = new GpsDrawable(p);
		gpsDrawable.setBounds(-GPS_RADIUS, -GPS_RADIUS, GPS_RADIUS, GPS_RADIUS);
	}

	public GeoPoint getGpsPosition() {
		return gpsMarker == null ? null : gpsMarker.getPoint();
	}

	private synchronized void loadFromModel() {
		list.clear();
		for (int i = 0; i < session.getNodeModel().size(); i++) {
			addMarkerToMap(session.getNodeModel().get(i));
		}
		updateIcons();
	}

	private Node dragging = null;

	@Override
	protected boolean onDragStart(int index) {
		// don't select the GPS point
		if (index >= list.size())
			return false;

		// If we drag the first node disable GPS
		if (index == 0)
			useGps = false;

		dragging = session.getNodeModel().get(index);

		return true;
	}

	@Override
	public void onDragStop(GeoPoint geoPoint, MapView mapView) {
		final int index = session.getNodeModel().getNodeVector().indexOf(dragging);
		if (index != -1) {
			final Node node = dragging;
			node.setGeoPoint(geoPoint);
			mapScreen.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mapScreen.performNNSearch(node);
					Edit edit = new UpdateNodeEdit(session, index, node);
					edit.perform();
				}
			});

		}
		dragging = null;
	}

	@Override
	public void onDragMove(GeoPoint geoPoint, MapView mapView) {
		int index = session.getNodeModel().getNodeVector().indexOf(dragging);
		if (index != -1) {
			list.get(index).setPoint(geoPoint);
			requestRedraw();
			Edit edit = new UpdateDndEdit(session, dragging, geoPoint);
			edit.perform();
		}
	}

	@Override
	public void onDragCancel() {
		dragging = null;
	}

	@Override
	public boolean onLongPress(GeoPoint geoPoint, MapView mapView) {
		if (session.getNodeModel().size() >= session.getSelectedAlgorithm().getMaxPoints()) {
			return false;
		}

		final Node node = session.createNode(geoPoint);

		mapView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);

		// Important! Try to perform the NNS Search before, notifying any one else about the change.
		// else through HTTP pipelining the NNS might be performed after the request!
		mapScreen.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mapScreen.performNNSearch(node);
			}
		});

		/*final ArrayList<ConstraintType> tempcl = session.getSelectedAlgorithm().getPointConstraintTypes();
		mapScreen.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < tempcl.size(); i++) {
					ConstraintDialog(cl.get(i).getName(), cl.get(i).getMinimumValue(), cl.get(i).getMaximumValue(), i, cl.get(i).getType());
				}
				if (session.getNodeModel().size() > 0) {
					session.getNodeModel().getNodeVector().set(session.getNodeModel().size() - 1, session.getNodeModel().getNodeVector().get(session.getNodeModel().size() - 1));
				}
			}
		});*/

		Edit edit = new AddNodeEdit(session, node, AddNodeEdit.Position.END);
		edit.perform();
		return true;
	}

	@Override
	public int size() {
		return list.size() + (gpsMarker == null ? 0 : 1);
	}

	@Override
	protected OverlayItem createItem(int i) {
		if (i == list.size())
			return gpsMarker;
		else
			return list.get(i);
	}


	@Override
	public boolean onTap(int i) {
		if (i == list.size()) {
			final GeoPoint gpsPoint = this.gpsMarker.getPoint();
			AlertDialog.Builder builder = new AlertDialog.Builder(mapScreen);
			builder.setMessage("Wollen Sie ihren aktuellen Standort als Startpunkt setzen?")
					.setCancelable(true)
					.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// transform the GpsMarker into a regular mapMarker on Position 0
							Node gpsStartnode = session.createNode(gpsPoint);
							useGps = true;
							Edit edit = new AddNodeEdit(session, gpsStartnode, AddNodeEdit.Position.BEGINNING);
							edit.perform();
						}
					})
					.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					}).create().show();
		} else {
			Intent intent = new Intent(mapScreen, EditNodeScreen.class);
			intent.putExtra("node", session.getNodeModel().get(i));
			intent.putExtra(Session.IDENTIFIER, session);
			intent.putExtra("index", i);
			mapScreen.startActivityForResult(intent, MapScreen.REQUEST_NODE);
		}

		return true;
	}

	public void addMarkerToMap(Node node) {
		NodeDrawable drawable = (NodeDrawable) boundCenterBottom(new NodeDrawable(NodeDrawable.MarkerType.START));
		drawable.setLabel(node.getName());
		OverlayItem overlayitem = new OverlayItem(node.getGeoPoint(), null, null, drawable);
		list.add(overlayitem);
	}

	private synchronized void updateIcons() {
		if (!session.getSelectedAlgorithm().sourceIsTarget() && !list.isEmpty()) {
			for (int i = 1; i < list.size() - 1; i++) {
				((NodeDrawable) list.get(i).getMarker()).setType(NodeDrawable.MarkerType.MIDDLE);
			}

			((NodeDrawable) list.get(list.size() - 1).getMarker()).setType(NodeDrawable.MarkerType.END);
			((NodeDrawable) list.get(0).getMarker()).setType(NodeDrawable.MarkerType.START);
		}

		requestRedraw();
	}

	public void updateGpsMarker(GeoPoint geoPoint) {
		// If we have a valid point
		if (geoPoint != null) {
			// Then create a new Overlayitem or update the old one
			if (gpsMarker == null)
				gpsMarker = new OverlayItem(geoPoint, null, null, gpsDrawable);
			else
				gpsMarker.setPoint(geoPoint);

		} else {
			gpsMarker = null;
		}
		if (useGps && !list.isEmpty()) {
			Edit edit = new UpdateNNSEdit(session, session.getNodeModel().get(0), geoPoint);
			edit.perform();
		}
		requestRedraw();
	}

	@Override
	public void onChange(Session.Change change) {
		if (change.isModelChange() || change.isNnsChange())
			loadFromModel();
	}

	public void ConstraintDialog(String title, final Object min, final Object max, final int constraintid, final String constraintType) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mapScreen);
		builder.setMessage("enter a value: ");
		builder.setCancelable(true);
		builder.setTitle(title);
		// Set an EditText view to get user input
		final EditText input = new EditText(mapScreen);
		String hintmessage = String.valueOf(min) + " .. " + String.valueOf(max);
		input.setHint(hintmessage);
		if (constraintType.equals("integer") || constraintType.equals("boolean")) {
			input.setInputType(InputType.TYPE_CLASS_NUMBER);
		}
		if (constraintType.equals("float") || constraintType.equals("meter") || constraintType.equals("price")) {
			input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

		}
		builder.setView(input);
		builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				constraintValue = input.getEditableText().toString();
				Boolean isNumeric = checkForDigits(constraintValue);
				if (constraintValue.equals("") || constraintValue == null) {
					constraintValue = String.valueOf(min);
				}

				if (isNumeric) {
					session.getNodeModel().get(session.getNodeModel().size() - 1).getConstraintList().get(constraintid).setValue(constraintValue);
				}
			}
		});
		builder.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		}).create().show();
	}

	public boolean checkForDigits(String str) {
		char c;
		for (int i = 0; i < str.length(); i++) {
			c = str.charAt(i);

			if (!Character.isDigit(c) && '.' != c)
				return false;
		}

		return true;
	}
}
