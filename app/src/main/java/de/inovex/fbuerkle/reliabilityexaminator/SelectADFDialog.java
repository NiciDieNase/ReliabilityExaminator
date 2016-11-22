package de.inovex.fbuerkle.reliabilityexaminator;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.google.atap.tangoservice.Tango;

/**
 * Created by felix on 04/04/16.
 */
public class SelectADFDialog extends DialogFragment implements ADFAdapter.ViewHolder.ADFSelectListener {

	private static final String TAG = "SelectADFDialog";
	private ADFSelectListener mListener;
	private Context mContext;
	private Tango tango;

	public interface ADFSelectListener {
		public void onADFSelected(String uuid);
	}

	@Override
	public void onADFSelect(String uuid) {
		mListener.onADFSelected(uuid);
		this.dismiss();
	}

	@Override
	public void onAttach(Context context) {
		mContext = context;
		super.onAttach(context);
		try {
			mListener = (ADFSelectListener) context;
		} catch (ClassCastException e){
			Log.d(TAG, context.toString() + "must implement ADFSelectListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Select ADF");

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.adf_list,null);

		ADFDataSource source = new ADFDataSource(mContext);
		String[] ids = source.getFullUUIDList();
		String[] names = source.getUUIDNames();
//		String[] ids = new String[0];
//		String[] names = new String[0];
		RecyclerView rv = (RecyclerView) view.findViewById(R.id.recyclerView);
		rv.setHasFixedSize(true);
		rv.setLayoutManager(new LinearLayoutManager(getActivity()));
		rv.setAdapter(new ADFAdapter(this,names,ids));

		builder.setView(view);
		builder.setCancelable(true);
		return builder.create();
	}
}
