package com.vigek.iotcore.fragment;

import com.vigek.iotcore.MainActivity;
import com.vigek.iotcore.R;
import com.vigek.iotcore.adapter.GpioAdapter;
import com.vigek.iotcore.bean.Deviceinfo;
import com.vigek.iotcore.manager.IDeviceController;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Button;
import android.widget.TextView;

public class fragment_controller extends Fragment implements OnClickListener {

	private Context mContext;

	private Button   mCameraControllerView;
	private Button   mGpioControllerView;
	private TextView mDeviceNameView;
	private TextView mDeviceIDView;
	private IDeviceController mDeviceController;
    private Deviceinfo mCurrentDevice;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContext = getActivity().getApplicationContext();
		int sectionNumber = getArguments().getInt(MainActivity.ARG_SECTION_NUMBER);
		MainActivity.mCurrentFragment = sectionNumber;
		
		mCurrentDevice = (Deviceinfo)getArguments().getParcelable(MainActivity.ARG_DEVICE);

		View rootView = (LinearLayout) inflater.inflate(R.layout.fragment_controller , container, false);
		
		mCameraControllerView = (Button)rootView.findViewById(R.id.bt_camera);
		mCameraControllerView.setOnClickListener(this);
		
		mGpioControllerView = (Button)rootView.findViewById(R.id.bt_gpio);
		mGpioControllerView.setOnClickListener(this);
		
		mDeviceNameView = (TextView)rootView.findViewById(R.id.controller_device_name);
		if(mCurrentDevice!=null)
		{
			mDeviceNameView.setText("Name: "+mCurrentDevice.getDeviceName());
		}
		
		mDeviceIDView = (TextView)rootView.findViewById(R.id.controller_device_id);
		if(mCurrentDevice!=null)
		{
			mDeviceIDView.setText("ID: "+mCurrentDevice.getFeedId());
		}
		
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
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		if(mCurrentDevice!=null)
		{
			switch(id)
			{
			case R.id.bt_camera:
				mDeviceController.onDeviceSelcted(IDeviceController.CONTROLLER_TYPE_CAMERA, mCurrentDevice);
				break;
			case R.id.bt_gpio:
				mDeviceController.onDeviceSelcted(IDeviceController.CONTROLLER_TYPE_GPIO, mCurrentDevice);
				break;
			}
		}
	}

}
