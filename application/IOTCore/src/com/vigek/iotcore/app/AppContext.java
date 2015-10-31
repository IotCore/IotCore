package com.vigek.iotcore.app;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.smarthome.android.mqttservice.IMqttConnectionStatusChangeListener;
import com.example.smarthome.android.mqttservice.MqttConnectionManager;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.vigek.iot.CommandManager;
import com.vigek.iot.IotManager;
import com.vigek.iotcore.R;
import com.vigek.iotcore.bean.Deviceinfo;
import com.vigek.iotcore.common.Log;
import com.vigek.iotcore.common.StringUtils;
import com.vigek.iotcore.manager.DeviceListManager;
import com.vigek.iotcore.manager.HMessageListManager;
import com.vigek.iotcore.mqtt.ActionListener;
import com.vigek.iotcore.mqtt.ActionListener.Action;
import com.vigek.iotcore.mqtt.MqttCallbackHandler;
import com.vigek.iotcore.mqtt.MqttTraceCallback;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class AppContext extends Application implements IMqttConnectionStatusChangeListener{
	private static AppContext mAppContext;
	private DeviceListManager mDeviceListManager;
	private static HMessageListManager mHMessageListManager;
//	private static MqttConnectionManager mMqttConnectionManager;
	
	private static IotManager mIotManager;	
	public static ExecutorService pool = Executors.newCachedThreadPool();	
	private static String TAG = "AppContext";
	public static final String ACTION_BOOT = 
			"android.intent.action.BOOT_COMPLETED";
	public static final String ACTION_NETWORK_CHANGED = 
			"android.net.conn.CONNECTIVITY_CHANGE";
	public static final String ACTION_MQTT_SERVICE = 
			"com.vigek.smokealarm.action.MQTT_SERVICE";
	public static final int NETTYPE_WIFI = 0x01;
	public static final int NETTYPE_CMWAP = 0x02;
	public static final int NETTYPE_CMNET = 0x03;	
	private BroadcastReceiver mAppInitReceiver;
	private static 	DisplayImageOptions options;	
    /** Messenger for communicating with service. */	
	private Messenger mAppTimerService = null;
	   /**
	    * Target we publish for clients to send messages to IncomingHandler.
	    */
	final Messenger mMessenger = new Messenger(new IncomingHandler());

		// Listener for when the service is connected or disconnected
	private MyServiceConnection serviceConnection = new MyServiceConnection();        
	/** Flag indicating whether we have called bind on the service. */
    boolean mIsBound;
    
	private Handler IncomingHandler = new IncomingHandler();
	public static final int MSG_REGISTER_CLIENT = 0x1001;
	public static final int MSG_UNREGISTER_CLIENT = 0x1002;
//	public static final int MSG_CONNECT = 0x1003;
//	public static final int MSG_PUBLISH = 0x1004;
//	public static final int MSG_SUBSCRIBE = 0x1005;
//	public static final int MSG_UNSUBSCRIBE= 0x1006;
//	public static final int MSG_DISCONNECT = 0x1007;
//	public static final int MSG_RECONNECT = 0x1008;	
	
	@Override
	public void onCreate() {
		super.onCreate();

		mAppContext = this;
	    Log.v(TAG,"appContext = "+mAppContext);

		mDeviceListManager = DeviceListManager.getInstance(mAppContext);
		mHMessageListManager = HMessageListManager.getInstance(mAppContext);
		mIotManager = IotManager.getInstance(mAppContext);	
		mIotManager.setMqttConnectionStatusChangeListener(this);
	    AddDemoDevice(mAppContext);
		InitDevices();		

	    Log.v(TAG,"appContext = "+mAppContext);
//		Thread.setDefaultUncaughtExceptionHandler(AppException.getAppExceptionHandler());
		mAppInitReceiver = new AppInitReceiver();
		Intent serverIntent = new Intent(mAppContext, AppTimerService.class);
	    mAppContext.startService(serverIntent);
		doBindService();	
		Log.v(TAG, "Register alarmreceiver to MqttService"+ ACTION_MQTT_SERVICE);
		IntentFilter f = new IntentFilter(ACTION_MQTT_SERVICE);
		f.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		mAppContext.registerReceiver(mAppInitReceiver,f);	
//		mContext.registerReceiver(mAppInitReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));  
		initImageLoader(mAppContext);}

// Application 
	
	public static AppContext getAppContext()
	{
		return mAppContext;
	}
// image loader

	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you may tune some of them,
		// or you can create default configuration by
		//  ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
		config.memoryCache(new WeakMemoryCache());
//		config.threadPoolSize(3);
		config.threadPriority(Thread.NORM_PRIORITY - 1);
		config.denyCacheImageMultipleSizesInMemory();
		config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
		config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
//		config.diskCacheFileCount(100);
		config.tasksProcessingOrder(QueueProcessingType.LIFO);
//		config.writeDebugLogs(); // Remove for release app

		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config.build());
	}	
	
	public static DisplayImageOptions getDisplayImageOptions()
	{
		if(options == null){
			options = new DisplayImageOptions.Builder()
			.showStubImage(R.drawable.ic_empty)
			.showImageForEmptyUri(R.drawable.ic_empty)
			.showImageOnFail(R.drawable.ic_empty)
			.cacheInMemory(false)
			.cacheOnDisk(true)
			.considerExifParams(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
			.build();
		}

		return options;
	
}	
	
// Service 
    /*
     * 
     * Handler of incoming messages from service.
    */
	


	public  Handler getAppContextHandler() {
		return IncomingHandler;
	}
	
   class IncomingHandler extends Handler {
       @Override
       public void handleMessage(Message msg) {
           switch (msg.what) {
//               case MSG_PUBLISH:
//               {
//                   Message message = Message.obtain(null, AppTimerService.MSG_PUBLISH, this.hashCode(), 0);
//                   message.replyTo = mMessenger;
//                   message.setData(msg.getData());
//					try {
//		                if(mAppTimerService!=null)
//		                {
//		                	mAppTimerService.send(message);
//		                }
//					} catch (RemoteException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//               }
//              	break;
//               case MSG_SUBSCRIBE:
//               {
//                   Message message = Message.obtain(null, AppTimerService.MSG_SUBSCRIBE, this.hashCode(), 0);
//                   message.replyTo = mMessenger;
//                   message.setData(msg.getData());
//					try {
//		                if(mAppTimerService!=null)
//		                {
//		                	mAppTimerService.send(message);
//		                }
//					} catch (RemoteException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//               }
//               	break;
//               case MSG_UNSUBSCRIBE:
//               {
//                   Message message = Message.obtain(null, AppTimerService.MSG_UNSUBSCRIBE, this.hashCode(), 0);
//                   message.replyTo = mMessenger;
//                   message.setData(msg.getData());
//					try {
//		                if(mAppTimerService!=null)
//		                {
//		                	mAppTimerService.send(message);
//		                }
//					} catch (RemoteException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//               }
//               	break;
//               case MSG_CONNECT:
//               {
//                  Message message = Message.obtain(null, AppTimerService.MSG_CONNECT, this.hashCode(), 0);
//                  message.replyTo = mMessenger;
//                  message.setData(msg.getData());
//					try {
//		                if(mAppTimerService!=null)
//		                {
//		                	mAppTimerService.send(message);
//		                }
//					} catch (RemoteException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//               }
//                	break;
//               case MSG_RECONNECT:
//               {
//                   Message message = Message.obtain(null, AppTimerService.MSG_RECONNECT, this.hashCode(), 0);
//                   message.replyTo = mMessenger;
//					try {
//		                if(mAppTimerService!=null)
//		                {
//		                	mAppTimerService.send(message);
//		                }
//					} catch (RemoteException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//               }
//               	break;
//               case MSG_DISCONNECT:
//               {
//                   Message message = Message.obtain(null, AppTimerService.MSG_DISCONNECT, this.hashCode(), 0);
//                   message.replyTo = mMessenger;
//					try {
//		                if(mAppTimerService!=null)
//		                {
//		                	mAppTimerService.send(message);
//		                }
//					} catch (RemoteException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//               }
//              	break;
               default:
                   super.handleMessage(msg);
           }
       }
   }

   /**
    * Class for interacting with the main interface of the service.
    */
	/**
	 * ServiceConnection to process when we bind to our service
	 */
	private final class MyServiceConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
           // This is called when the connection with the service has been
           // established, giving us the service object we can use to
           // interact with the service.  We are communicating with our
           // service through an IDL interface, so get a client-side
           // representation of that from the raw service object.
           Log.v(TAG,"onServiceConnected.");
           mAppTimerService = new Messenger(binder);
//           mCallbackText.setText("Attached.");

           // We want to monitor the service for as long as we are
           // connected to it.
           try {
               Message msg = Message.obtain(null,
                       AppTimerService.MSG_REGISTER_CLIENT);
               msg.replyTo = mMessenger;
               if(mAppTimerService!=null)
               {
               	mAppTimerService.send(msg);
               }
               
               // Give it some value as an example.
//               msg = Message.obtain(null,
//               		AppTimerService.MSG_PUBLISH, this.hashCode(), 0);
//               Bundle b= new Bundle();
//               b.putInt("qos",1);
//               msg.setData(b);
//               mAppTimerService.send(msg);
           } catch (RemoteException e) {
               // In this case the service has crashed before we could even
               // do anything with it; we can count on soon being
               // disconnected (and then reconnected if it can be restarted)
               // so there is no need to do anything here.
           }
           
//           
//           // As part of the sample, tell the user what happened.
//           Toast.makeText(Binding.this, R.string.remote_service_connected,
//                   Toast.LENGTH_SHORT).show();		
           }

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mAppTimerService = null;
           Log.v(TAG,"onServiceDisconnected.");
//
//           // As part of the sample, tell the user what happened.
//           Toast.makeText(Binding.this, R.string.remote_service_disconnected,
//                   Toast.LENGTH_SHORT).show();
		}
	}

	
	public static void scheduleNextAlarm(long delayInMilliseconds) {
		
		String action = ACTION_MQTT_SERVICE;
//		Log.d(TAG, "Register alarmreceiver to MqttService"+ action);
//		service.registerReceiver(alarmReceiver, new IntentFilter(action));

		PendingIntent pendingIntent= PendingIntent.getBroadcast(mAppContext, 0, new Intent(
				action), PendingIntent.FLAG_UPDATE_CURRENT);
		
		
		long nextAlarmInMilliseconds = System.currentTimeMillis()
				+ delayInMilliseconds;
		Log.v(TAG, "Schedule next alarm at " + nextAlarmInMilliseconds);
		AlarmManager alarmManager = (AlarmManager) mAppContext
				.getSystemService(Service.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, nextAlarmInMilliseconds,
				pendingIntent);
	}		
		
	public void StartAppTimerService()
	{
	    Log.v(TAG,"StartAppTimerService"+mAppContext);
    	boolean isMqttTimerServiceRunning = false;
    	boolean isMqttServiceRunning = false;
		//intent.putExtra(DeviceListManager.SELECT_DEVICE, SelectedDevice);
		 ActivityManager manager = (ActivityManager)mAppContext.getSystemService(Context.ACTIVITY_SERVICE);   
		    for (RunningServiceInfo service :manager.getRunningServices(Integer.MAX_VALUE)) {   
		    if("com.vigek.smokealarm.app.AppTimerService".equals(service.service.getClassName()))   
		     {   
		    	isMqttTimerServiceRunning = true;   
		     } 
		    if("com.example.smarthome.android.mqttservice.MqttService".equals(service.service.getClassName()))
		    {
		    	isMqttServiceRunning = true;
		    }
		    
		   }   
		     Log.v(TAG,"com.vigek.smokealarm.app.AppTimerService is running? "+isMqttTimerServiceRunning );
		     Log.v(TAG,"com.example.smarthome.android.mqttservice.MqttService? "+isMqttServiceRunning );
//		     Log.v(TAG,"MqttConnectionManager is Connected? "+ mMqttConnectionManager.isConnected() );
		     Log.v(TAG, "mAppTimerService="+ mAppTimerService);
		    
		    if ((!isMqttTimerServiceRunning) ||(mAppTimerService == null)){   
		    		Intent serverIntent = new Intent(mAppContext, AppTimerService.class);
		    	    mAppContext.startService(serverIntent);
		    		doBindService();	
		    }
		    else
		    {
		    	if(!isMqttServiceRunning)
		    	{
		    		doConnect();
		    	}
		    	else
		    	{
		    		mIotManager.reconnect();
		    	}
		    }
	}
	

    
    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(mAppContext, AppTimerService.class), serviceConnection,Context.BIND_ABOVE_CLIENT);
        mIsBound = true;
//        mCallbackText.setText("Binding.");
    }
    
  public  void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mAppTimerService != null) {
                try {
                    Message msg = Message.obtain(null,
                    		AppTimerService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mAppTimerService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }
            
            // Detach our existing connection.
            unbindService(serviceConnection);
            mIsBound = false;
            Log.v(TAG,"Unbinding.");
        }
    }	

// Device Manager
	
	private void InitDevices(){
		Log.v(TAG,"init");
		pool.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mDeviceListManager.getDeviceList(AppConfig.config_authKey);
			}
			
		});
		
		pool.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mHMessageListManager.restoreHMessageList(null);
			}
			
		});		
	}
	
	public  void addDevice(Context context, Deviceinfo device)
	{
		DeviceListManager mDeviceListManager = DeviceListManager.getInstance(context);
		mDeviceListManager.addDevice(device);
		subscribeDevice(device);
		
	}
	
	public void updateDevice(Context context, Deviceinfo device)
	{
		DeviceListManager mDeviceListManager = DeviceListManager.getInstance(context);
		mDeviceListManager.updateDevice(device);		
	}
	
	public void deleteDevice(Context context, Deviceinfo device)
	{
		DeviceListManager mDeviceListManager = DeviceListManager.getInstance(context);
		mDeviceListManager.removeDevice(device);
//		unsubscribeDevice(device);
	}

	public  void AddDemoDevice(Context context)
	{
/*		Deviceinfo DemoDevice1 = mDeviceListManager.getDeviceBySn("12345678");
		if(DemoDevice1 == null)
		{

			DemoDevice1 = new Deviceinfo("smoke alarm demo", "12345678", "smoke alarm", "Demo", DeviceListManager.DEVICE_TYPE_SMOKE_ALARM);
			addDevice(context,DemoDevice1);			
		}
		Deviceinfo DemoDevice2 = mDeviceListManager.getDeviceBySn("88888888");
		if(DemoDevice2 == null)
		{
			DemoDevice2 = new Deviceinfo("light demo", "88888888", "light", "Demo", DeviceListManager.DEVICE_TYPE_NIGHT_LIGHT);

			addDevice(context,DemoDevice2);			
		}
*/
		Deviceinfo DemoDevice3 = mDeviceListManager.getDeviceBySn("22222222");
		if(DemoDevice3 == null)
		{
			DemoDevice3 = new Deviceinfo("IOT Core demo", "22222222", "IOT Core", "Demo", DeviceListManager.DEVICE_TYPE_IOT_CORE);

			addDevice(context,DemoDevice3);			
		}		
	}
	
	public static  Deviceinfo getCurrentDevice(Context context, String topic)
	{
		DeviceListManager mDeviceListManager = DeviceListManager.getInstance(context);
		String  feed[] = topic.split("\\"+AppConfig.config_topic_split);
		String  sn = feed[0];

		return mDeviceListManager.getDeviceBySn(sn);
	}
	

	@Override
	public void OnConnectionStatusChanged() {
		// TODO Auto-generated method stub
//		Notify.toast(mContext, ""+MqttConnectionManager.getConnectionStatus(), 2);
		if(MqttConnectionManager.getConnectionStatus() == MqttConnectionManager.CONNECTED)
		{
			List<Deviceinfo> devicelist = mDeviceListManager.getDeviceList();
			if(devicelist.size()>0){
				for(Deviceinfo d : devicelist)
				{
//					for(String l: DeviceListManager.DeviceSubTopic)
//					{
						if(d!=null){
							subscribeDevice(d);
						}
//					}
				}
			}

		}			
		
	}

	
// common utils
	
	private static void subscribeDevice(Deviceinfo d) {
		// TODO Auto-generated method stub
	      String[] topics = new String[1];
	      topics[0] = d.getFeedId()+ AppConfig.config_topic_split + DeviceListManager.DeviceSubTopic[1];
	      ActionListener al = new ActionListener(mAppContext, Action.SUBSCRIBE, mIotManager.getClientHandle(), topics);
	      
	      int qos = AppConfig.config_defaultQos;
	      mIotManager.subscribe(topics[0], al, qos);
	}
	
	private static void unsubscribeDevice(Deviceinfo d) {
		// TODO Auto-generated method stub
	      String[] topics = new String[1];
	      topics[0] = d.getFeedId()+ AppConfig.config_topic_split + DeviceListManager.DeviceSubTopic[1];
	      ActionListener al = new ActionListener(mAppContext, Action.UNSUBSCRIBE, mIotManager.getClientHandle(), topics);
	      
	      int qos = AppConfig.config_defaultQos;
	      mIotManager.unsubscribe(topics[0], al);
	}
	
	public static void unsubscribeTopic(String topic)
	{
	      String[] topics = new String[1];
	      topics[0] = topic;
	      ActionListener al = new ActionListener(mAppContext, Action.UNSUBSCRIBE, mIotManager.getClientHandle(), topics);

	      mIotManager.unsubscribe(topic, al);
	}
	
	public PackageInfo getPackageInfo() {
		PackageInfo info = null;
		try { 
			info = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {    
			e.printStackTrace(System.err);
		} 
		if(info == null) info = new PackageInfo();
		return info;
	}

	public static void SendGPIOCommand(Deviceinfo mCurrentDevice, int gpioNum, int type) {
		// TODO Auto-generated method stub
	// fill the mCommandManager  with gpio command		
			CommandManager.getCommandManager(mAppContext).setWorkType(CommandManager.GPIO_MODE);
			byte[] payload = CommandManager.getCommandManager(mAppContext).toByteArray(CommandManager.GPIO_MODE, gpioNum, type);
		    String[] args = new String[2];
		    args[0] = "message";
		    args[1] = "topic";

		    ActionListener al = new ActionListener(mAppContext, Action.PUBLISH, MqttConnectionManager.getClientHandle(), args);
			
			if(mCurrentDevice!=null)
			{
				int qos = AppConfig.config_defaultQos;
				boolean retained = AppConfig.config_defaultRetained;
				
				mIotManager.SendCommand(mCurrentDevice.getFeedId(), payload, al, qos, retained);
			}
	}

	public static void doConnect() {
		// TODO Auto-generated method stub
		mIotManager.deInitMqttConnectionManager();
//		mDeviceListManager = DeviceListManager.getInstance(mAppContext);
//		mHMessageListManager = HMessageListManager.getInstance(mAppContext);
//		mHMessageListManager.RegisterObserverListener(mHMessageListDataSetObserver);    
//		mDeviceListManager.setDialogUpdateListener(getUpdateListener()); 
//		mDeviceListManager.getDeviceList("123");
	   	int port = AppConfig.getAppConfig(mAppContext).getServerPort();
	   	if(port == 0)
	   	{
	   		port = AppConfig.config_defaultPort;
	   	}
	   	String serverURI = AppConfig.getAppConfig(mAppContext).getServerURI();
	   	if(StringUtils.isEmpty(serverURI))
	   	{
	   		serverURI = AppConfig.config_defaultServerURI;
	   	}
	   	String clientId = AppConfig.getAppConfig(mAppContext).getClientId();
			Log.v(TAG,"connect....!");
	 	
	   	connect(clientId, serverURI, port, false);
	   	mIotManager.InitMqttConnectionManager();
	}

	public static void disconnect() {
		// TODO Auto-generated method stub
		mIotManager.disconnect();
	}

	public static void SendCaptureCommand(Deviceinfo mCurrentDevice) {
		// TODO Auto-generated method stub
	//// fill the mCommandManager  with capture command		
		CommandManager.getCommandManager(mAppContext).setWorkType(CommandManager.PHOTO_MODE);

		byte[] payload = CommandManager.getCommandManager(mAppContext).toByteArray(CommandManager.PHOTO_MODE, 0, 0);
	    String[] args = new String[2];
	    args[0] = "message";
	    args[1] = "topic";

	    ActionListener al = new ActionListener(mAppContext, Action.PUBLISH, MqttConnectionManager.getClientHandle(), args);
		if(mCurrentDevice!=null)
		{
			int qos = AppConfig.config_defaultQos;
			boolean retained = AppConfig.config_defaultRetained;
			mIotManager.SendCommand(mCurrentDevice.getFeedId(), payload, al, qos, retained);
		}
	}

	public static void connect(String clientId, String host, int port, boolean ssl) {
		// TODO Auto-generated method stub
		String clientHandle = null;
		if(ssl)
		{
			clientHandle = "ssl://" + host + ":" + port+""+clientId;
		}
		else
		{
			clientHandle = "tcp://" +host+":"+port+""+clientId;
		}
	    String[] actionArgs = new String[1];
	    actionArgs[0] = clientId;
	    ActionListener al = new ActionListener(mAppContext,
		        ActionListener.Action.CONNECT, clientHandle, actionArgs);
	    MqttCallbackHandler ml = new MqttCallbackHandler(mAppContext, clientHandle);
	    MqttTraceCallback tl = new MqttTraceCallback();
	    
	    mIotManager.connect(clientId, host, port, ssl, al, ml, tl);
	    mIotManager.setClientHandle(clientHandle);
	}

	public static void connectionLost() {
		// TODO Auto-generated method stub
		mIotManager.connectionLost();
	}
}
