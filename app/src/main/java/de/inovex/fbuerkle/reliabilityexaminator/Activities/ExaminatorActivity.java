package de.inovex.fbuerkle.reliabilityexaminator.Activities;

import android.app.FragmentManager;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoAreaDescriptionMetaData;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.inovex.fbuerkle.reliabilityexaminator.Handler.ProtocolHandler;
import de.inovex.fbuerkle.reliabilityexaminator.Handler.SensorHandler;
import de.inovex.fbuerkle.reliabilityexaminator.Handler.TangoHandler;
import de.inovex.fbuerkle.reliabilityexaminator.R;
import de.inovex.fbuerkle.reliabilityexaminator.SelectADFDialog;
import de.inovex.fbuerkle.reliabilityexaminator.SetAdfNameDialog;

public class ExaminatorActivity
		extends AppCompatActivity
		implements SelectADFDialog.ADFSelectListener,
			SetAdfNameDialog.CallbackListener{

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
	@BindView(R.id.tv_adf_status_value) TextView adfStatus;
	@BindView(R.id.progressBar) ProgressBar progressBar;
	private String uuid;
	private boolean arealearning;

	@Override
	public void onAdfNameOk(String name, String uuid) {
		mTangoHandler.saveADF(name);
	}

	@Override
	public void onAdfNameCancelled() {

	}

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
				showSetAdfNameDialog();
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
		}
		fabSave.hide();
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
				new SelectADFDialog().setmContext(this,mTangoHandler.getTango())
						.show(getFragmentManager(),"selectADF");
				return true;
			case R.id.action_load_adf:
				mADFaction = ADFaction.load;
				new SelectADFDialog().setmContext(this,mTangoHandler.getTango())
						.show(getFragmentManager(),"selectADF");
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

	public void loadADF(String uuid) {
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

	public void showLoadingDialog(){
		progressBar.setIndeterminate(true);
		progressBar.setVisibility(View.VISIBLE);
	}

	public void hideLoadingDialog(){
		progressBar.setVisibility(View.GONE);
	}

	    /**
     * Shows a dialog for setting the ADF name.
     */
    private void showSetAdfNameDialog() {
        Bundle bundle = new Bundle();
        bundle.putString(TangoAreaDescriptionMetaData.KEY_NAME, "New ADF");
        // UUID is generated after the ADF is saved.
        bundle.putString(TangoAreaDescriptionMetaData.KEY_UUID, this.uuid);

        FragmentManager manager = getFragmentManager();
        SetAdfNameDialog setAdfNameDialog = new SetAdfNameDialog();
        setAdfNameDialog.setArguments(bundle);
        setAdfNameDialog.show(manager, "ADFNameDialog");
    }
}