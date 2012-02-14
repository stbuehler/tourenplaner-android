package de.uni.stuttgart.informatik.ToureNPlaner.UI.Activities;

import android.app.ExpandableListActivity;
import android.os.Bundle;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Toast;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.BillingItem;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler.BillingListHandler;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler.RawHandler;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Observer;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Session;
import de.uni.stuttgart.informatik.ToureNPlaner.UI.Adapters.BillingListAdapter;

import java.util.ArrayList;

public class BillingScreen extends ExpandableListActivity implements Observer, OnScrollListener {
	private BillingListAdapter adapter;
	private BillingListHandler handler;
	private Session session;

	private ArrayList<BillingItem> billinglist = new ArrayList<BillingItem>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// If we get created for the first time we get our data from the intent
		Bundle data = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
		session = (Session) data.getSerializable(Session.IDENTIFIER);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		adapter = new BillingListAdapter(this, billinglist);
		setListAdapter(adapter);
		getExpandableListView().setOnScrollListener(this);
		registerForContextMenu(getExpandableListView());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCompleted(RawHandler caller, Object object) {
		handler = null;
		setProgressBarIndeterminateVisibility(false);
		adapter.addAll((ArrayList<BillingItem>) object);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onError(RawHandler caller, Object object) {
		handler = null;
		setProgressBarIndeterminateVisibility(false);
		Toast.makeText(this, ((Exception) object).getLocalizedMessage(), Toast.LENGTH_LONG);
	}

	@Override
	public void onScroll(AbsListView arg0, int firstVisible, int visibleCount, int totalCount) {
		boolean loadMore =
				firstVisible + visibleCount >= totalCount - 10;

		if (loadMore && handler == null) {
			setProgressBarIndeterminateVisibility(true);
			handler = new BillingListHandler(this, session, 15, adapter.getGroupCount());
			handler.execute();
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
	}
}