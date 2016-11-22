package de.inovex.fbuerkle.reliabilityexaminator.Activities;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import de.inovex.fbuerkle.reliabilityexaminator.Handler.SensorHandler;
import de.inovex.fbuerkle.reliabilityexaminator.Handler.TangoHandler;
import de.inovex.fbuerkle.reliabilityexaminator.R;

public class ExaminatorActivity extends AppCompatActivity{

	private static final String TAG = ExaminatorActivity.class.getSimpleName();

	private SensorHandler mSensorHandler;
	private TangoHandler mTangoHandler;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

		GLSurfaceView mTopPreview = (GLSurfaceView) findViewById(R.id.top_preview);
		GLSurfaceView mBottomPreview = (GLSurfaceView) findViewById(R.id.bottom_preview);
		TextView textView = (TextView) findViewById(R.id.tv_pitch_value);

		setSupportActionBar(toolbar);
		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();
			}
		});

		mSensorHandler = new SensorHandler(this,textView);
		mTangoHandler = new TangoHandler(this, mTopPreview, mBottomPreview);
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
				// TODO export ADF
				return true;
			case R.id.action_load_adf:
				// TODO load ADF
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
}