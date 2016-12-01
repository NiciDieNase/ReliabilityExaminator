package de.inovex.fbuerkle.reliabilityexaminator.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.atap.tangoservice.Tango;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.inovex.fbuerkle.reliabilityexaminator.Handler.ProtocolHandler;
import de.inovex.fbuerkle.reliabilityexaminator.Handler.SensorHandler;
import de.inovex.fbuerkle.reliabilityexaminator.Handler.TangoHandler;
import de.inovex.fbuerkle.reliabilityexaminator.R;
import de.inovex.fbuerkle.reliabilityexaminator.SelectADFDialog;

public class ExaminatorActivity extends AppCompatActivity implements SelectADFDialog.ADFSelectListener{

	private static final String TAG = ExaminatorActivity.class.getSimpleName();
	public static final String KEY_UUID = "uuid";
	public static final String KEY_AREALEARNING = "arealearning";

	private SensorHandler mSensorHandler;
	private TangoHandler mTangoHandler;
	private ProtocolHandler mProtocolHandler;
	@BindView(R.id.layout_tango) ViewGroup rootView;
	@BindView(R.id.toolbar) Toolbar toolbar;
	@BindView(R.id.fab_log) FloatingActionButton fabLog;
	@BindView(R.id.fab_save) FloatingActionButton fabSave;
	private String uuid;
	private boolean arealearning;

	enum ADFaction{undef, export, load;}
	ADFaction mADFaction = ADFaction.undef;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
		setSupportActionBar(toolbar);
		fabLog.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mProtocolHandler.startProtocol(uuid);
				mTangoHandler.takeScreenshot();
				fabLog.hide();
			}
		});
		fabSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mTangoHandler.saveADF();
			}
		});

		startActivityForResult(
				Tango.getRequestPermissionIntent(Tango.PERMISSIONTYPE_ADF_LOAD_SAVE),
				Tango.TANGO_INTENT_ACTIVITYCODE);

		Bundle extras = getIntent().getExtras();
		this.uuid = "";
		if(extras != null){
			this.uuid = extras.getString(KEY_UUID,"");
			this.arealearning = extras.getBoolean(KEY_AREALEARNING,false);
		}
		mProtocolHandler = new ProtocolHandler();
		if(this.arealearning){
			Snackbar.make(rootView, "Starting Area Learning",Snackbar.LENGTH_SHORT).show();
			mProtocolHandler.startProtocol("learning");
		} else {
			fabSave.hide();
		}
		if(uuid != ""){
			Snackbar.make(rootView, "Started with ADF: "+uuid,Snackbar.LENGTH_SHORT).show();
			mProtocolHandler.startProtocol(uuid);
//			mTangoHandler.takeScreenshot();
			fabLog.hide();
		}

		mSensorHandler = new SensorHandler(this, rootView, mProtocolHandler);
		mTangoHandler = new TangoHandler(this, rootView, uuid, arealearning, mProtocolHandler);
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
		mProtocolHandler.flush();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mProtocolHandler.stopProtocol();
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
			case R.id.action_stop_protocol:
				mProtocolHandler.stopProtocol();
				return true;
			case R.id.action_start_arealearning:
				startArealearning();
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
			Snackbar.make(rootView, "Exported ADF: " + uuid, Snackbar.LENGTH_LONG)
					.setAction("Action", null).show();
		} else if(ADFaction.load.equals(mADFaction)){
			loadADF(uuid);
		}
		mADFaction = ADFaction.undef;
	}

	private void loadADF(String uuid) {
		Intent i = new Intent(this, ExaminatorActivity.class);
		i.putExtra(KEY_UUID,uuid);
		startActivity(i);
		this.finish();
	}

	private void startArealearning(){
		Intent i = new Intent(this, ExaminatorActivity.class);
		if(this.uuid != ""){
			i.putExtra(KEY_UUID,uuid);
		}
		i.putExtra(KEY_AREALEARNING,true);
		startActivity(i);
		finish();
	}
}