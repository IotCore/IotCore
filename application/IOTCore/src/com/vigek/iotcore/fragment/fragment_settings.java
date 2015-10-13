package com.vigek.iotcore.fragment;

import com.example.smarthome.android.mqttservice.IMqttConnectionStatusChangeListener;
import com.example.smarthome.android.mqttservice.MqttConnectionManager;
import com.vigek.iotcore.MainActivity;
import com.vigek.iotcore.R;
import com.vigek.iotcore.adapter.GpioAdapter;
import com.vigek.iotcore.app.AppConfig;
import com.vigek.iotcore.app.AppContext;
import com.vigek.iotcore.bean.Deviceinfo;
import com.vigek.iotcore.common.Log;
import com.vigek.iotcore.common.Notify;
import com.vigek.iotcore.manager.IDeviceController;
import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class fragment_settings extends Fragment implements IMqttConnectionStatusChangeListener {
	private Context mContext;
	private AppContext mAppContext;
	private MqttConnectionManager mMqttConnectionManager;
	private TextView mClientIdView;
	private TextView mConnectionStatusView;
	private TextView mServerURIView;
	private TextView mServerPortView;
	
	private String mClientId;
	private String mConnectionStatus;
	private String mServerURI;
	private String mServerPort;
	private int    mPort;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContext = getActivity().getApplicationContext();
		int sectionNumber = getArguments().getInt(MainActivity.ARG_SECTION_NUMBER);
		MainActivity.mCurrentFragment = sectionNumber;

		mAppContext = (AppContext)getActivity().getApplication();
		mMqttConnectionManager = MqttConnectionManager.getInstance(mContext);
	    mMqttConnectionManager.setMqttConnectionStatusChangeListener(this);
		View rootView = (LinearLayout) inflater.inflate(R.layout.fragment_settings , container, false);
		mClientIdView = (TextView)rootView.findViewById(R.id.clientId);
		mConnectionStatusView = (TextView)rootView.findViewById(R.id.StatusHintView);
		mServerURIView = (TextView)rootView.findViewById(R.id.serverURI);
		mServerPortView = (TextView)rootView.findViewById(R.id.Mqttport);
	
		mClientId = AppConfig.getAppConfig(mContext).getClientId();
		mClientIdView.setText(mClientId);
		
		OnConnectionStatusChanged();
		
		setHasOptionsMenu(true);
		return rootView;
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((MainActivity) activity).onSectionAttached(getArguments().getInt(MainActivity.ARG_SECTION_NUMBER));
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}	
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	        inflater.inflate(R.menu.settings, menu);
	        super.onCreateOptionsMenu(menu,inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch(item.getItemId()){   
	    case  R.id.action_connect:
	    	mServerURI = mServerURIView.getText().toString();
            if (mServerURI.equals(AppConfig.empty) || mServerPortView.getText().toString().equals(AppConfig.empty)/* || clientId.equals(AppConfig.empty)*/)
            {
              String notificationText = this.getString(R.string.missingOptions);
              Notify.toast(mContext, notificationText, Toast.LENGTH_LONG);
              return true;
            }	
            else
            {
            	mPort = Integer.parseInt(mServerPortView.getText().toString());
            	AppConfig.getAppConfig(mContext).setServerURI(mServerURI);
//            	AppConfig.getAppConfig(mContext).setServerPort(mPort);
//				 Message msg = Message.obtain(mAppContext.getAppContextHandler(), AppContext.MSG_CONNECT);
//				 Bundle bundle = new Bundle();
//	             bundle.putString(AppConfig.config_clientId,AppConfig.getAppConfig(mContext).getClientId());
//	             bundle.putString(AppConfig.config_server,mServerURI);
//	             bundle.putInt(AppConfig.config_port,mPort);
//	             bundle.putBoolean(AppConfig.config_ssl,false);
//				 msg.setData(bundle);
//				 msg.sendToTarget();		
            	AppContext.connect(AppConfig.getAppConfig(mContext).getClientId(),mServerURI, mPort, false);
            }
	        break;  

	    }
	    return super.onOptionsItemSelected(item);
	}


	@Override
	public void OnConnectionStatusChanged() {
		// TODO Auto-generated method stub
		int s  =  MqttConnectionManager.getConnectionStatus();
//		Notify.toast(mContext, ""+MqttConnectionManager.getConnectionStatus(), 2);
		switch(s)
		{
		case MqttConnectionManager.CONNECTING:
			mConnectionStatusView.setText("connecting");
			break;
		case MqttConnectionManager.CONNECTED:
			mConnectionStatusView.setText("connected");
			break;
		case MqttConnectionManager.DISCONNECTED:
			mConnectionStatusView.setText("diconnected");
			break;
		case MqttConnectionManager.DISCONNECTING:
			mConnectionStatusView.setText("disconnecting");
			break;
		case MqttConnectionManager.ERROR:
			mConnectionStatusView.setText("error");
			break;
		case MqttConnectionManager.NONE:
			mConnectionStatusView.setText("unknown");
			break;
		}
		
}
	
	
}
