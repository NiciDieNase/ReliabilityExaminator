package de.inovex.fbuerkle.reliabilityexaminator;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by felix on 04/04/16.
 */
public class ADFAdapter extends RecyclerView.Adapter {

	private final String[] names;
	private final String[] ids;
	private final ViewHolder.ADFSelectListener mListener;

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public String id;
		public TextView bigText;
		public TextView smallText;
		private ADFSelectListener mListener;

		public ViewHolder(ADFSelectListener listener, View itemView) {
			super(itemView);
			this.mListener = listener;
			this.bigText = (TextView) itemView.findViewById(android.R.id.text1);
			this.smallText = (TextView) itemView.findViewById(android.R.id.text2);
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ViewHolder.this.mListener.onADFSelect(id);
				}
			});
		}

		public interface ADFSelectListener{
			public void onADFSelect(String uuid);
		}
	}

	public ADFAdapter(ViewHolder.ADFSelectListener listener, String[]names, String[] ids){
		this.mListener = listener;
		this.names = names;
		this.ids = ids;
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
		((ViewHolder)holder).bigText.setText(names[position]);
		((ViewHolder)holder).smallText.setText(ids[position]);
		((ViewHolder)holder).id = ids[position];
	}

	@Override
	public int getItemCount() {
		return ids.length;
	}
}
