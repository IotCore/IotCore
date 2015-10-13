package com.vigek.iotcore.manager;

//import java.util.Vector;
import java.sql.SQLException;  
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

import com.vigek.iotcore.R;
import com.vigek.iotcore.app.AppConfig;
import com.vigek.iotcore.bean.Deviceinfo;
import com.vigek.iotcore.db.DeviceinfoDao;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.util.Log;

public class DeviceListManager{
	private String TAG = DeviceListManager.class.getSimpleName();
	private static List<Deviceinfo> deviceList = new ArrayList<Deviceinfo>();
	private DeviceinfoDao mDeviceinfoDao;
	
	public static final int DEVICE_TYPE_IOT_CORE = 0x3000;
	public static final int DEVICE_TYPE_NIGHT_LIGHT = 0x4000;
	public static final int DEVICE_TYPE_SMOKE_ALARM = 0x5000;
	public static final int DEVICE_TYPE_AIR_CONDITIONER = 0x6000;
	
	public static final String DeviceSubTopic[] = {"state", "picture", "audio"};
	public static final String DevicePubTopic[] = {"cmd"};

	
	private static volatile DeviceListManager __instance;
	private Context context;
	private HashSet<Integer> mSelectedDevice = new HashSet<Integer>();
	private String mAuthKey = null;

	private static volatile boolean update_flag = false;

	private static ArrayList<DataSetObserver> mObserver = new ArrayList<DataSetObserver>();
	private static ArrayList<IUpdateListener> mListener = new ArrayList<IUpdateListener>();
	
	static int retryCnt = 0;
	
	private static CountDownLatch handlerInitLatch;
	
	public static DeviceListManager getInstance(Context appContext) {
		if (__instance == null) {
			synchronized (DeviceListManager.class)
			{	
			__instance = new DeviceListManager(appContext);
			
			}
		}
		return __instance;
	}
	
	public DeviceListManager(Context ctx)
	{
		this.context=ctx;
		this.mDeviceinfoDao = new DeviceinfoDao(context);
		handlerInitLatch = new CountDownLatch(1);
		getDeviceList(AppConfig.config_authKey);
	}

	public List<Deviceinfo> getDeviceList()
	{
		try {
			handlerInitLatch.await();
		} catch (InterruptedException ie) {
			// continue?
		}
		if((deviceList.size()>0)&&(deviceList.get(0).getFeedId().equals("unknown")))
		{
			deviceList.remove(0);
		}
		return deviceList;
	}
	
	public  void removeDevice(Deviceinfo device)
	{
		deviceList.remove(device);
		if(mDeviceinfoDao!=null)
		{
			mDeviceinfoDao.delete(device);
		}
		if(mObserver != null)
		{
			for(DataSetObserver d: mObserver)
			{
			d.onInvalidated();
			}
		}

	}
	
	public  void removeDevice(int position)
	{
		removeDevice(deviceList.get(position));
	}
	
	public  List<Deviceinfo> addDevice(Deviceinfo device)
	{
		update_flag = false;
		for(Deviceinfo a: deviceList)
		{
			if(a.getFeedId().equals(device.getFeedId()))// it exits already
			{	// update the information
				int index = deviceList.indexOf(a);
				deviceList.set(index, device);
				update_flag = true;
				if(mDeviceinfoDao!=null)
				{
					mDeviceinfoDao.update(device);
				}				
				if(mObserver != null)
				{
					for(DataSetObserver d: mObserver)
					{
					d.onInvalidated();
					}
				}

			return deviceList;		
			}
		}
		
		Log.v("addDevice","updated is" + update_flag);
		
			if(update_flag == false) //add this device
			{
				deviceList.add(device);
				if(mDeviceinfoDao!=null)
				{
					mDeviceinfoDao.add(device);
				}
			}
			
			if(mObserver != null)
			{
				for(DataSetObserver d: mObserver)
				{
				d.onInvalidated();
				}
			}

		return deviceList;		
	}
	
	public  Deviceinfo getDeviceByID(int id)
	{
		if(mDeviceinfoDao!=null)
		{
			return mDeviceinfoDao.get(id);
		}
		
		return null;
		
	}
	
	
	public  Deviceinfo getDeviceBySn(String sn)
	{
		if(mDeviceinfoDao!=null)
		{
			return mDeviceinfoDao.getDeviceBySn(sn);
		}
		return null;
	}

	
	public  void updateDevice(Deviceinfo device)
	{
		for(Deviceinfo b: deviceList)
		{
			if(b.getFeedId().equals(device.getFeedId()))
			{
				deviceList.set(deviceList.indexOf(b),device);
				break;
			}
		}
			mDeviceinfoDao.update(device);
			if(mObserver != null)
			{
				for(DataSetObserver d: mObserver)
				{
				d.onInvalidated();
				}
			}

	}
	
	public synchronized void setSelectDevice(int _device)
	{
		mSelectedDevice.add(_device);
	}
	
	public synchronized void removeSelecteDevice(int _device)
	{
		mSelectedDevice.remove(_device);
	}
	
	public synchronized void clearSelectedDevice()
	{
		mSelectedDevice.clear();
	}
	
	public synchronized void selectAllDevices()
	{
		mSelectedDevice.clear();
		for(int i=0; i< deviceList.size();i++)
		{
			mSelectedDevice.add(i);
		}
	}
	
	public synchronized HashSet<Integer> getSelectedDeviceId()
	{
		return mSelectedDevice;
	}
	
	public synchronized List<Deviceinfo> getSelectedDevice()
	{
		List<Deviceinfo> l = new ArrayList<Deviceinfo>();
		
		if(deviceList!=null){
			for(int i: mSelectedDevice)
			{
				l.add(deviceList.get(i));
			}
			return l;
		}
		else
		{
			return null;
		}
	}

	public void removeSelectedDevice() {
		// TODO Auto-generated method stub
		for(int i: mSelectedDevice)
		{
			removeDevice(i);
		}
		mSelectedDevice.clear();
	}	
	
	public synchronized int getSelectedDeviceCount()
	{
		return mSelectedDevice.size();
	}
		
	public synchronized int getDeviceCount()
	{
		if(deviceList==null)
		{
			return 0;
		}
		else
		{
		return deviceList.size();
		}
	}
	
	public synchronized void getDeviceListOld(String _authKey)
	{
		mAuthKey = _authKey;
		new AsyncDeviceList().execute(mAuthKey);
	}
	
	public  void getDeviceList(String _authKey)
	{
		mAuthKey = _authKey;

		try{
			deviceList = mDeviceinfoDao.queryForAll();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}	
		
		handlerInitLatch.countDown();
		Log.v("DeviceListManager","finish getDeviceList in background");
		
		if(mObserver != null)
		{
			for(DataSetObserver d: mObserver)
			{
			d.onInvalidated();
			}
		}
		if(mListener!=null){
			for(IUpdateListener i: mListener){
				i.update(IUpdateListener.DEVICES_UPDATED, null);
			}
		}

	}
	
	public synchronized int getDeviceIcon(Deviceinfo device)
	{
		int deviceIcon = R.drawable.ic_launcher;
//		if(device == null)
//		{
//			return deviceIcon;
//		}
//		else
//		{
//			switch(device.getDeviceType())
//			{
//				case DEVICE_TYPE_NIGHT_LIGHT:
//					deviceIcon = R.drawable.icon_light;
//					break;
//				case DEVICE_TYPE_SMOKE_ALARM:
//					deviceIcon = R.drawable.smokealarm;
//					break;
//				case DEVICE_TYPE_AIR_CONDITIONER:
//					deviceIcon = R.drawable.conditioner;
//					break;
//				default:
//					deviceIcon = R.drawable.ic_launcher;
//					break;
//			}	
//		}
		
		return deviceIcon;
   }
	
	public synchronized boolean isUpdated()
	{
		if(handlerInitLatch.getCount() == 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	class AsyncDeviceList extends AsyncTask<String, Void, Boolean> {
		String auid = null;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			Log.v("DeviceListManager","getDeviceList in background");
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... key) {
			try{
				deviceList = mDeviceinfoDao.queryForAll();
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
			return true;	
		}

		@Override
		protected void onPostExecute(Boolean _auid) {
			// TODO Auto-generated method stub
			handlerInitLatch.countDown();
			Log.v("DeviceListManager","finish getDeviceList in background");
			
			if(mObserver != null)
			{
				for(DataSetObserver d: mObserver)
				{
				d.onInvalidated();
				}
			}
			if(mListener!=null){
				for(IUpdateListener i: mListener){
					i.update(IUpdateListener.DEVICES_UPDATED, null);
				}
			}

			super.onPostExecute(_auid);
		}

	}
	
	public void RegisterUpdateListener(IUpdateListener _listener)
	{
		if(_listener!=null){
			mListener.add(_listener);
		}
	}
	public void unRegisterUpdateListener(IUpdateListener _listener)
	{
		if(_listener!=null){
			mListener.remove(_listener);
		}		
	}
	
	public void unRegisterAllUpdateListener()
	{
		mListener.clear();
	}
	
	public void RegisterObserverListener(DataSetObserver _observer) {
		if(_observer!=null){
			mObserver.add(_observer);
		}
	}
	
	public void unRegisterObserverListener(DataSetObserver _observer) {
		if(_observer!=null){
			mObserver.remove(_observer);
		}
	}
	
	public void unRegisterAllObserverListener()
	{
		mObserver.clear();
	}
}




