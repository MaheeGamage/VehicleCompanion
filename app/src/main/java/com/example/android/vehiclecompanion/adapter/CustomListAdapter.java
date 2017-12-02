package com.example.android.vehiclecompanion.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import com.example.android.vehiclecompanion.R;
import com.example.android.vehiclecompanion.model.Branch;

public class CustomListAdapter extends BaseAdapter {
	private Activity activity;
	private LayoutInflater inflater;
	private List<Branch> branchItems;

	public CustomListAdapter(Activity activity, List<Branch> branchItems) {
		this.activity = activity;
		this.branchItems = branchItems;
	}

	@Override
	public int getCount() {
		return branchItems.size();
	}

	@Override
	public Object getItem(int location) {
		return branchItems.get(location);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (inflater == null)
			inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null)
			convertView = inflater.inflate(R.layout.list_row, null);

		TextView title = (TextView) convertView.findViewById(R.id.title);
		TextView location = (TextView) convertView.findViewById(R.id.location);

		// getting branch data for the row
		Branch b = branchItems.get(position);
		
		// name
		title.setText(b.getName());

		// location
		location.setText(b.getName());

		return convertView;
	}

}