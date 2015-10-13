package com.vigek.iotcore.app;

//import java.text.SimpleDateFormat;  
import java.util.List;
import java.util.ArrayList;
//import java.util.Date;  
//import java.util.Map;
//import java.util.Timer;  
//import java.util.TimerTask;  
  
//import java.beans.PropertyChangeEvent;
//import java.beans.PropertyChangeListener;
//
//import com.example.smarthome.mqtt.ActionListener.Action;
//import com.example.smarthome.mqtt.ActionListener;
//import com.example.smarthome.mqtt.Connection;
//import com.example.smarthome.mqtt.Connection.ConnectionStatus;
//import com.example.smarthome.mqtt.Connection.SubscribeStatus;
//import com.example.smarthome.mqtt.MqttCallbackHandler;

import java.util.concurrent.CountDownLatch;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.vigek.iotcore.MainActivity;
import com.vigek.iotcore.R;
import com.vigek.iotcore.common.AlarmAlertWakeLock;
import com.vigek.iotcore.common.Log;
import com.vigek.iotcore.common.Notify;
import com.vigek.iotcore.manager.DeviceListManager;
import com.example.smarthome.android.mqttservice.MqttConnectionManager;
import com.example.smarthome.android.mqttservice.IMqttConnectionStatusChangeListener;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;  
import android.content.ComponentName;  
import android.content.Intent;  
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;  
import android.os.IBinder;  
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Build;

import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat.Builder;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;

public class AppTimerService extends Service{
	private String TAG = "AppTimerService";

	private static Context mContext;
	public static final int MSG_REGISTER_CLIENT = 0x1001;
	public static final int MSG_UNREGISTER_CLIENT = 0x1002;
	public static final int MSG_CONNECT = 0x1003;
	public static final int MSG_PUBLISH = 0x1004;
	public static final int MSG_SUBSCRIBE = 0x1005;
	public static final int MSG_UNSUBSCRIBE= 0x1006;
	public static final int MSG_DISCONNECT = 0x1007;
	public static final int MSG_RECONNECT = 0x1008;
	
    private static ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    private DeviceListManager mDeviceListManager;
	
	static class IncomingHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
			case MSG_REGISTER_CLIENT:
                mClients.add(msg.replyTo);
				break;
			case MSG_UNREGISTER_CLIENT:
                mClients.remove(msg.replyTo);
				break;

			default:
				super.handleMessage(msg);
			}
		}
	}
	
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	private Notification mNotification;
	
	@Override
	public  void onCreate()
	{
		super.onCreate();
		Log.i(TAG, "MqttTimerService->onCreate");
		mContext = this;
//		handlerInitLatch = new CountDownLatch(1);
		AppContext.doConnect();
		
		AppContext.scheduleNextAlarm(AppConfig.config_defaultInterval);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) {
			setMsgNotification();
		} else {
			Notification notification = new Notification();
			startForeground(1, notification);
		}	
		
		AlarmAlertWakeLock.acquireWifiLock(mContext);
	}
	
	private void setMsgNotification() {

		//指定整个通知栏的intent
		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        // Update the notification to indicate that the alert has been
        // silenced.
//        String label = msg.getClientId();
        Builder  notificationCompat = new Builder(mContext);
        
        notificationCompat.setContentTitle(mContext.getString(R.string.app_name))
        .setContentText(mContext.getString(R.string.notification_hint))
        .setSmallIcon(R.drawable.ic_launcher)
        .setOngoing(true)
        .setAutoCancel(false)
        .setPriority(Notification.PRIORITY_MAX)
        .setDefaults(Notification.DEFAULT_LIGHTS)
        .setWhen(0)
//        .addAction(R.drawable.stat_notify_alarm,
//                context.getResources().getString(R.string.alarm_alert_snooze_text),
//                pendingSnooze)
//        .addAction(android.R.drawable.ic_menu_close_clear_cancel,
//                context.getResources().getString(R.string.alarm_alert_dismiss_text),
//                pendingDismiss)
        .build();

        mNotification = notificationCompat.build();	 
	
		// 指定内容意图
		mNotification.contentIntent = pendingIntent;
		startForeground(Notify.MessageNotification, mNotification);
	}	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG,"onStartCommand!");
		
		return super.onStartCommand(intent, START_STICKY_COMPATIBILITY, startId);
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		Log.v(TAG,"onStart!");
	}	

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.v(TAG,"onBind!");
		return mMessenger.getBinder();
	}
	
    public boolean onUnbind(Intent intent) {
		Log.v(TAG,"onUnbind!");
       return false;
    }	
    
	@Override	
    public void onConfigurationChanged(Configuration newConfig) {
		Log.v(TAG,"onConfigurationChanged!");    
    }
	
    @Override    
    public void onLowMemory() {
		Log.v(TAG,"onLowMemory!");    
//		stopSelf();
   }
    
    @Override
    public void onTrimMemory(int level) {
		Log.v(TAG,"onTrimMemory!");    
//		stopSelf();
    }
    
	@Override
	public void onTaskRemoved(Intent rootIntent) {
		Log.v(TAG,"onTaskRemoved!");   
//		stopSelf();
	}
    
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.v(TAG,"onDestroy!");
		AppContext.disconnect();
		AlarmAlertWakeLock.releaseWifiLock();
		}
}
