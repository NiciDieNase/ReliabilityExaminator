package de.inovex.fbuerkle.reliabilityexaminator.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.atap.tangoservice.Tango;

import de.inovex.fbuerkle.reliabilityexaminator.Handler.SensorHandler;
import de.inovex.fbuerkle.reliabilityexaminator.Handler.TangoHandler;
import de.inovex.fbuerkle.reliabilityexaminator.R;
import de.inovex.fbuerkle.reliabilityexaminator.SelectADFDialog;

public class ExaminatorActivity extends AppCompatActivity implements SelectADFDialog.ADFSelectListener{

	private static final String TAG = ExaminatorActivity.class.getSimpleName();

	private SensorHandler mSensorHandler;
	private TangoHandler mTangoHandler;

	enum ADFaction{undef, export, load;}
	ADFaction mADFaction = ADFaction.undef;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ViewGroup rootView = (ViewGroup) findViewById(R.id.layout_tango);

		TextView textView = (TextView) findViewById(R.id.tv_pitch_value);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
//				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//						.setAction("Action", null).show();
				mADFaction = ADFaction.load;
				new SelectADFDialog().show(getFragmentManager(),"selectADF");
			}
		});

		startActivityForResult(
				Tango.getRequestPermissionIntent(Tango.PERMISSIONTYPE_ADF_LOAD_SAVE),
				Tango.TANGO_INTENT_ACTIVITYCODE);

		String uuid = getIntent().getStringExtra("UUID");

		mSensorHandler = new SensorHandler(this, rootView);
		mTangoHandler = new TangoHandler(this, rootView, uuid);
	}


	@Override
	protected void onResume() {
		super.onResume();
		mTangoHandler.onResume();
		mSensorHandler.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mTangoHandler.onPause();
		mSensorHandler.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.action_settings:
				return true;
			case R.id.action_export_adf:
				mADFaction = ADFaction.export;
				new SelectADFDialog().setmContext(this).show(getFragmentManager(),"selectADF");
				return true;
			case R.id.action_load_adf:
				mADFaction = ADFaction.load;
				new SelectADFDialog().setmContext(this).show(getFragmentManager(),"selectADF");
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onADFSelected(String uuid) {
		if(ADFaction.undef.equals(mADFaction)){
			Log.e(TAG,"Action should not be undefined");
			return;
		} else if(ADFaction.export.equals(mADFaction)){
			mTangoHandler.exportADF(uuid);
		} else if(ADFaction.load.equals(mADFaction)){
			Intent i = new Intent(this, ExaminatorActivity.class);
			i.putExtra("uuid",uuid);
			startActivity(i);
			this.finish();
		}
		mADFaction = ADFaction.undef;
	}
}