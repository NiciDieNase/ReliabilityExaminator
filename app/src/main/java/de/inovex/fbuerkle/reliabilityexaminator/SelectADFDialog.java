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
	private Tango mTango;

	public interface ADFSelectListener {
		public void onADFSelected(String uuid);
	}

	@Override
	public void onADFSelect(String uuid) {
		if(null != mListener){
			mListener.onADFSelected(uuid);
		}
		this.dismiss();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Select ADF");

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.adf_list,null);

		ADFDataSource source = new ADFDataSource(mContext,mTango);
		String[] ids = source.getFullUUIDList();
		String[] names = new String[ids.length];
		source.getUUIDNames(names);
		RecyclerView rv = (RecyclerView) view.findViewById(R.id.recyclerView);
		rv.setHasFixedSize(true);
		rv.setLayoutManager(new LinearLayoutManager(getActivity()));
		rv.setAdapter(new ADFAdapter(this,names,ids));

		builder.setView(view);
		builder.setCancelable(true);
		return builder.create();
	}

	public SelectADFDialog setmContext(Context mContext,Tango tango) {
		this.mContext = mContext;
		this.mTango = tango;
		try {
			mListener = (ADFSelectListener) mContext;
		} catch (ClassCastException e){
			Log.d(TAG, mContext.toString() + "must implement ADFSelectListener");
		}
		return this;
	}
}
