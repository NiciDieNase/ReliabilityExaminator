package de.inovex.fbuerkle.reliabilityexaminator;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.google.atap.tangoservice.Tango;

/**
 * Created by felix on 15/12/16.
 */

public class ImportADFDialog extends DialogFragment implements ImportAdapter.ViewHolder.FileSelectListener {

	private static final String TAG = "SelectADFDialog";
	private SelectADFDialog.ADFSelectListener mListener;
	private Context mContext;
	private Tango mTango;

	public ImportADFDialog setTango(Tango mTango){
		this.mTango = mTango;
		return this;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Select ADF");

		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.adf_list,null);

		ADFDataSource source = new ADFDataSource(mContext,mTango);
		RecyclerView rv = (RecyclerView) view.findViewById(R.id.recyclerView);
		rv.setHasFixedSize(true);
		rv.setLayoutManager(new LinearLayoutManager(getActivity()));
		rv.setAdapter(new ImportAdapter(this));

		builder.setView(view);
		builder.setCancelable(true);
		return builder.create();
	}

	@Override
	public void onFileSelect(String path) {
		mTango.importAreaDescriptionFile(path);
		this.dismiss();
	}
}