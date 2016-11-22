package de.inovex.fbuerkle.reliabilityexaminator;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by felix on 04/04/16.
 */
public class SelectADFDialog extends DialogFragment implements ADFAdapter.ViewHolder.ADFSelectListener {

	private static final String TAG = "SelectADFDialog";
	private ADFSelectListener mListener;

	public interface ADFSelectListener {
		public void onADFSelected(String uuid);
	}

	@Override
	public void onADFSelect(String uuid) {
		mListener.onADFSelected(uuid);
		this.dismiss();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mListener = (ADFSelectListener) activity;
		} catch (ClassCastException e){
			Log.d(TAG, activity.toString() + "must implement ADFSelectListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Select ADF");

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.adf_list,null);

		ADFDataSource source = new ADFDataSource(getActivity());
		String[] ids = source.getFullUUIDList();
		String[] names = source.getUUIDNames();
		RecyclerView rv = (RecyclerView) view.findViewById(R.id.recyclerView);
		rv.setHasFixedSize(true);
		rv.setLayoutManager(new LinearLayoutManager(getActivity()));
		rv.setAdapter(new ADFAdapter(this,names,ids));

		builder.setView(view);
		builder.setCancelable(true);
		return builder.create();
	}
}
