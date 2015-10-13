package com.vigek.iotcore.adapter;

import java.util.Collection;
import java.util.List;

import com.vigek.iotcore.R;
import com.vigek.iotcore.bean.Deviceinfo;
import com.vigek.iotcore.manager.DeviceListManager;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;



public class DeviceAdapter extends BaseAdapter{

	private Context context;
	private DeviceListManager deviceManager;
	private volatile static int n = 0;

	
	public DeviceAdapter(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
		deviceManager = DeviceListManager.getInstance(context);		
	}
	public static DeviceAdapter createSelectDeviceAdapter(Context context) {
		return new DeviceAdapter(context);
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		int count=0;
		count = deviceManager.getDeviceList().size();
		return count;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		
		if(deviceManager.getDeviceList().size() == 0)
			return null;
		else
			return deviceManager.getDeviceList().get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public  void removeItem( int arg0)
	{
		 deviceManager.removeDevice(arg0);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		View v = null;
		TextView productNameV;
		TextView deviceNameV;
		ImageView deviceIconV;
		TextView deviceIdV;
		TextView pos_labelV;
		TextView pos_latitudeV;
		TextView pos_longitudeV;
		
		LinearLayout backgroundV;
		LinearLayout backgroundV2;
		String productName;
		String deviceName;
		String deviceId;
		String pos_label;
		String pos_latitude;
		String pos_longitude;
		int	   deviceIcon;
		
		DeviceHolder holder;

		if( convertView == null ) {
			v = LayoutInflater.from(context).inflate(R.layout.device_list_view, null);
			holder = new DeviceHolder(v);
			v.setTag(holder);
		}
		else {
			v = convertView;
			holder = (DeviceHolder) v.getTag();
		}

		backgroundV = holder.getBackgroundView();

		if(deviceManager.getSelectedDeviceId().contains(position))
		{
			backgroundV.setBackgroundResource(R.drawable.card_selected_background);
		}
		else
		{
			backgroundV.setBackgroundResource(R.drawable.card_unpressed_background);
		}
		
		Deviceinfo device = deviceManager.getDeviceList().get(position);
		productName = device.getProductname();
		deviceName = device.getDeviceName();
		deviceId = device.getFeedId();

		deviceIcon = deviceManager.getDeviceIcon(device);

		deviceIconV = holder.getDeviceIconView();
		productNameV = holder.getProductNameView();
		productNameV.setText(productName);
		deviceNameV = holder.getDeviceNameView();
		deviceNameV.setText(deviceName);		
		deviceIdV  = holder.getDeviceIdView();
		deviceIdV.setText(deviceId);	

// 2		
//		Bitmap bitmap=BitmapFactory.decodeResource(context.getResources(), deviceIcon);
//		
//		deviceIconV.setImageBitmap(bitmap);
//		deviceIconV.postInvalidate();
//		
// 1
		deviceIconV.setImageResource(deviceIcon);
		/*
		if(position == 0 || (position % 2) ==0 )
		{
			backgroundV.setBackgroundColor(Color.LTGRAY);
			deviceNameV.setTextColor(Color.DKGRAY);
			deviceIdV.setTextColor(Color.DKGRAY);
		}
		else
		{
			backgroundV.setBackgroundColor(Color.WHITE);
			deviceNameV.setTextColor(Color.GRAY);
			deviceIdV.setTextColor(Color.GRAY);
		}*/
		return v;
	}
	
	class DeviceHolder {
		View v;
		
		ImageView icon;
		TextView product;
		TextView name;
		TextView id;
		TextView pos_label;
		TextView pos_latitude;
		TextView pos_longitude;
		
		LinearLayout background;
		LinearLayout background2;	

		DeviceHolder(View v){
			this.v = v;
		}

		ImageView getDeviceIconView()
		{
			if(icon == null)
			{
				icon = (ImageView)v.findViewById(R.id.device_icon);
			}
			return icon;
		}
		TextView getProductNameView(){
			if (product == null) {
				product = (TextView)v.findViewById(R.id.product_name);
			}
			return product;
		}
		
		TextView getDeviceNameView(){
			if (name == null) {
				name = (TextView)v.findViewById(R.id.device_name);
			}
			return name;
		}
		
		TextView getDeviceIdView(){
			if(id == null) {
				id = (TextView)v.findViewById(R.id.device_id);
			}
			return id;
		}
	
		LinearLayout getBackgroundView()
		{
			if(background == null){
				background = (LinearLayout)v.findViewById(R.id.background);
			}
			return background;
		}

	}
}