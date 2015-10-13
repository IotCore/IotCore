package com.vigek.iotcore.fragment;

import com.vigek.iotcore.common.Log;
import com.vigek.iotcore.fragment.NavigationDrawerFragment.NavigationDrawerCallbacks;
import com.vigek.iotcore.manager.DeviceListManager;
import com.vigek.iotcore.manager.IDeviceController;
import com.vigek.iotcore.AddDeviceActivity;
import com.vigek.iotcore.MainActivity;
import com.vigek.iotcore.R;
import com.vigek.iotcore.adapter.DeviceAdapter;
import com.vigek.iotcore.app.AppConfig;
import com.vigek.iotcore.app.AppContext;
import com.vigek.iotcore.bean.Deviceinfo;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class fragment_mydevice extends Fragment implements OnItemClickListener{
	private Context mContext;
	private ListView mDeviceListView;
	private DeviceAdapter mDeviceAdapter;
    private DeviceListManager mDeviceManager;
    private IDeviceController mDeviceController;
    
    private static final int REQUEST_CODE_ADD = 1;
    private static final int REQUEST_CODE_MESSAGE = 2;
    public static final int RESULT_OK           = -1;
    	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContext = getActivity().getApplicationContext();
		int sectionNumber = getArguments().getInt(MainActivity.ARG_SECTION_NUMBER);
		MainActivity.mCurrentFragment = sectionNumber;
		
		mDeviceManager = DeviceListManager.getInstance(mContext);
		mDeviceManager.RegisterObserverListener(mDeviceListDataSetObserver);
		
		mDeviceAdapter = DeviceAdapter.createSelectDeviceAdapter(mContext);
		View rootView = (LinearLayout) inflater.inflate(R.layout.fragment_mydevice , container, false);
		mDeviceListView = (ListView)rootView.findViewById(R.id.deviceListv);
		mDeviceListView.setAdapter(mDeviceAdapter);
		mDeviceListView.setOnItemClickListener(this);
		setHasOptionsMenu(true);
		
		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mDeviceController = (IDeviceController) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
		}
		
		((MainActivity) activity).onSectionAttached(getArguments().getInt(MainActivity.ARG_SECTION_NUMBER));
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mDeviceController = null;
	}	

    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode == RESULT_OK) {
			Deviceinfo device = intent.getParcelableExtra(AppConfig.config_device);
			AppContext.getAppContext().addDevice(mContext, device);
		}
    	
    }
    
    @Override
    public void onStop()
    {
    	super.onStop();
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	Log.v("HomeFragment onResume");
    	mDeviceManager.clearSelectedDevice();
    }
    
    @Override
	public void onDestroy()
    {
    	super.onDestroy();
		mDeviceManager.unRegisterObserverListener(mDeviceListDataSetObserver);
	}
    
    
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	        inflater.inflate(R.menu.mydevice, menu);
	        super.onCreateOptionsMenu(menu,inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch(item.getItemId()){   
	    case  R.id.action_add_device:
	    	Log.v("add device ");
			Intent intent = new Intent(getActivity().getBaseContext(), AddDeviceActivity.class);
			startActivityForResult(intent,REQUEST_CODE_ADD);
	        break;  

	    }
	    return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Deviceinfo d = mDeviceManager.getDeviceList().get(position);
		mDeviceManager.clearSelectedDevice();
		mDeviceManager.setSelectDevice(position);
		if(mDeviceController != null)
		{
			mDeviceController.onDeviceSelcted(IDeviceController.CONTROLLER_TYPE_ALL, d);
		}
		
	}
	
    
	private  DataSetObserver mDeviceListDataSetObserver = new DataSetObserver() {

		@Override
		public void onChanged() {
			mDeviceAdapter.notifyDataSetChanged();
			mDeviceAdapter.notifyDataSetInvalidated();
			super.onChanged();
			
		}

		@Override
		public void onInvalidated() {
			mDeviceAdapter.notifyDataSetChanged();
			mDeviceAdapter.notifyDataSetInvalidated();
			super.onInvalidated();
		}

	};		
	
	public void setDeviceController(IDeviceController l)
	{
		mDeviceController = l;
	}
}
