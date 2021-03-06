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

package de.uni.stuttgart.informatik.ToureNPlaner.UI.Adapters;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.TextView;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.BillingItem;
import de.uni.stuttgart.informatik.ToureNPlaner.R;

import java.util.ArrayList;

public class BillingListAdapter extends BaseExpandableListAdapter {
	private ArrayList<String> billingCaptions = new ArrayList<String>();
	private ArrayList<String[]> billingItems = new ArrayList<String[]>();
	private Context context;
	public OnClickListener buttonClicklistener;

	public BillingListAdapter(Context context, ArrayList<BillingItem> billinglist, OnClickListener buttonClicklistener) {
		this.context = context;
		addAll(billinglist);
		this.buttonClicklistener = buttonClicklistener;
	}


	public int getRequestID(int itemID) {
		String id = billingItems.get(itemID)[0];
		id = id.substring(id.indexOf(":") + 2);
		return Integer.valueOf(id);
	}

	public String getAlgSuffix(int itemID) {
		String algSuffix = billingItems.get(itemID)[1];
		algSuffix = algSuffix.substring(algSuffix.indexOf(":") + 2);
		return algSuffix;
	}

	public String getStatus(int itemID) {
		String status = billingItems.get(itemID)[5];
		status = status.substring(status.indexOf(":") + 2);
		return status;
	}

	public void addAll(ArrayList<BillingItem> items) {
		setupList(items);
	}

	private void setupList(ArrayList<BillingItem> items) {
		billingCaptions.ensureCapacity(billingCaptions.size() + items.size());
		for (int i = 0; i < items.size(); i++) {
			billingCaptions.add(context.getResources().getString(R.string.tour) + " " + String.valueOf(items.get(i).getRequestid()) + " " + items.get(i).getAlgorithm());
		}

		billingItems.ensureCapacity(billingItems.size() + items.size());
		double cost;
		String date;
		for (int i = 0; i < items.size(); i++) {
			date = items.get(i).getRequestdate();
			String DateYearDayMonth = date.substring(0, date.indexOf("T"));
			String DateTime = date.substring(date.indexOf("T") + 1, date.indexOf("."));
			cost = ((double) items.get(i).getCost()) / 100;

			String[] arr = new String[6];
			arr[0] = context.getString(R.string.requestid) + ": " + items.get(i).getRequestid();
			arr[1] = context.getString(R.string.algorithmn) + ": " + items.get(i).getAlgorithm();
			arr[2] = context.getString(R.string.cost) + ": " + cost + " " + context.getResources().getString(R.string.euro);
			arr[3] = context.getString(R.string.requestdate) + ": \n" + DateYearDayMonth + " " + DateTime;
			arr[4] = context.getString(R.string.duration) + ": " + items.get(i).getDuration() + " " + context.getResources().getString(R.string.milliseconds);
			arr[5] = context.getString(R.string.status) + ": " + items.get(i).getStatus();
			billingItems.add(arr);
		}
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return billingItems.get(groupPosition)[childPosition];
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return billingItems.get(groupPosition).length;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
	                         View convertView, ViewGroup parent) {
		TextView textView = (TextView) (convertView == null ? View.inflate(context, R.layout.billinglist_childrow, null) : convertView);
		textView.requestLayout();
		textView.setText(getChild(groupPosition, childPosition).toString());
		return textView;
	}

	@Override
	public Object getGroup(int groupPosition) {
		return billingCaptions.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return billingCaptions.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
	                         ViewGroup parent) {
		View view = convertView == null ? View.inflate(context, R.layout.billinglist_grouprow, null) : convertView;

		Button buttonGroup = (Button) view.findViewById(R.id.button);
		buttonGroup.setTag(groupPosition);
		buttonGroup.setOnClickListener(buttonClicklistener);

		TextView textViewGroup = (TextView) view.findViewById(R.id.t1);
		textViewGroup.setText(getGroup(groupPosition).toString());

		return view;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}
}

