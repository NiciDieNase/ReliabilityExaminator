/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.inovex.fbuerkle.reliabilityexaminator;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoAreaDescriptionMetaData;
import com.google.atap.tangoservice.TangoErrorException;

import java.util.ArrayList;

/**
 * This class interfaces a Tango Object and maintains a full list of ADF UUIds. Whenever an adf is
 * deleted or added, getFullUUIDList needs to be called to update the UUIDList within this class.
 */
public class ADFDataSource {
	private static final String TAG = ADFDataSource.class.getSimpleName();
	private Tango mTango;
	private ArrayList<String> mFullUUIDList = null;
	private Context mContext;

	public ADFDataSource(Context context,Tango tango) {
		mContext = context;
		mTango = tango;
	}

	public String[] getFullUUIDList() {
		try {
			mFullUUIDList = mTango.listAreaDescriptions();
		} catch (TangoErrorException e) {
			if(mContext != null){
				Toast.makeText(mContext, R.string.tango_error, Toast.LENGTH_SHORT).show();
			}
			Log.d(TAG,"Tango Error");
		}
		if (mFullUUIDList.size() == 0) {
			if(mContext != null){
				Toast.makeText(mContext, R.string.no_adfs_tango_error, Toast.LENGTH_SHORT).show();
			}
			Log.d(TAG,"No ADFS");
		}
		Log.d(TAG,"Return UUID-Array");
		return mFullUUIDList.toArray(new String[mFullUUIDList.size()]);
	}

	public String[] getUUIDNames(String[] list) {
		if(mFullUUIDList == null){
			getFullUUIDList();
		}
		TangoAreaDescriptionMetaData metadata = new TangoAreaDescriptionMetaData();
//		String[] list = new String[mFullUUIDList.size()];
		for (int i = 0; i < mFullUUIDList.size(); i++) {
			try {
				metadata = mTango.loadAreaDescriptionMetaData(mFullUUIDList.get(i));
			} catch (TangoErrorException e) {
				if(mContext != null){
					Toast.makeText(mContext, R.string.tango_error, Toast.LENGTH_SHORT).show();
				}
				Log.d(TAG,"Tango Error while loading ADF Metadata");
			}
			byte[] bytes = metadata.get(TangoAreaDescriptionMetaData.KEY_NAME);
			if(bytes != null){
				list[i] = new String(bytes);
			} else {
				list[i] = "<" + mFullUUIDList.get(i) + ">";
			}
			Log.d(TAG,list[i]);
		}
		return list;
	}
}
