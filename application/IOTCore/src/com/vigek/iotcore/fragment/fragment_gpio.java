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

public class fragment_gpio extends Fragment{
	
	private GpioAdapter mAdapter;
	private Context mContext;
	private ListView mGpioListView;

    private IDeviceController mDeviceController;
    private Deviceinfo mCurrentDevice;
    private TextView mDeviceNameView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContext = getActivity().getApplicationContext();
		int sectionNumber = getArguments().getInt(MainActivity.ARG_SECTION_NUMBER);
		mCurrentDevice = (Deviceinfo)getArguments().getParcelable(MainActivity.ARG_DEVICE);
		MainActivity.mCurrentFragment = sectionNumber;

		View rootView = (LinearLayout) inflater.inflate(R.layout.fragment_gpio , container, false);
		mDeviceNameView = (TextView)rootView.findViewById(R.id.gpio_device_name);
		if(mCurrentDevice!=null)
		{
			mDeviceNameView.setText(mCurrentDevice.getDeviceName());
		}	
		
		mAdapter = GpioAdapter.createPositionAdapter(mContext, mCurrentDevice);
		mGpioListView = (ListView)rootView.findViewById(R.id.gpioListv);
		mGpioListView.setAdapter(mAdapter);
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
}
