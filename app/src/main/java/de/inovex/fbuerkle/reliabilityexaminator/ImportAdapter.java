package de.inovex.fbuerkle.reliabilityexaminator;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;

/**
 * Created by felix on 15/12/16.
 */

public class ImportAdapter extends RecyclerView.Adapter {

	private static final String TAG = ImportAdapter.class.getSimpleName();
	public static final String ADF_DIR = "/storage/emulated/0/TangoADFs/";
	String[] mFiles;
	private final ImportAdapter.ViewHolder.FileSelectListener mListener;

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public String id;
		public TextView bigText;
		public TextView smallText;
		private FileSelectListener mListener;

		public ViewHolder(ImportAdapter.ViewHolder.FileSelectListener listener, View itemView) {
			super(itemView);
			this.mListener = listener;
			this.bigText = (TextView) itemView.findViewById(android.R.id.text1);
			this.smallText = (TextView) itemView.findViewById(android.R.id.text2);
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ImportAdapter.ViewHolder.this.mListener.onFileSelect(ADF_DIR + id);
				}
			});
		}

		public interface FileSelectListener{
			public void onFileSelect(String uuid);
		}
	}
	public ImportAdapter(ViewHolder.FileSelectListener listener){
		this.mListener = listener;
		File adfDir =  new File(ADF_DIR);
		String[] adfFiles = adfDir.list();
		if(adfFiles != null){
			mFiles = adfFiles;
		} else {
			Log.d(TAG,"Something went horribly wrong");
		}
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
				.inflate(android.R.layout.simple_list_item_2,parent,false);
		RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) v.getLayoutParams();
		lp.setMargins(5,5,5,5);
		ViewHolder vh = new ViewHolder(mListener, v);
		return vh;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		((ViewHolder)holder).bigText.setText(mFiles[position]);
		((ViewHolder)holder).id = mFiles[position];
	}

	@Override
	public int getItemCount() {
		return mFiles.length;
	}
}
