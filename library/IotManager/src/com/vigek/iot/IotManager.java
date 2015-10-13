package com.vigek.iot;
import com.example.smarthome.android.mqttservice.Connection;
import com.example.smarthome.android.mqttservice.IMqttConnectionStatusChangeListener;
import com.example.smarthome.android.mqttservice.MqttConnectionManager;
import com.example.smarthome.android.mqttservice.MqttTraceHandler;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import android.content.Context;

public class IotManager {
	
	private static IotManager instance = null;
//	private static CommandManager mCommandManager;
	private static MqttConnectionManager mMqttConnectionManager;	
	
	private Context mContext;
	private static String clientHandle=null; 
	
	private  IotManager(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
//		mCommandManager = CommandManager.getCommandManager(mContext);
		mMqttConnectionManager = MqttConnectionManager.getInstance(mContext);		
	}

	public synchronized static IotManager getInstance(Context context)
	{
		if (instance == null)
		{
			synchronized (IotManager.class)
			{
				if (instance == null)
				{
					instance = new IotManager(context);
				}
			}
		}

		return instance;
	}

	public void setMqttConnectionStatusChangeListener(IMqttConnectionStatusChangeListener l) {
		// TODO Auto-generated method stub
		mMqttConnectionManager.setMqttConnectionStatusChangeListener(l);
	}
	
	public void SendCommand(String feedId, byte[] payload, IMqttActionListener al, int qos, boolean retained ) {
		// TODO Auto-generated method stub
		String topic = feedId +"/cmd";
		if (MqttConnectionManager.isConnected()) {
			
			mMqttConnectionManager.setPublishActionListener(al);
			mMqttConnectionManager.publish(topic, payload, qos, retained);
		}	
	}

	public static void connect(String clientId, String host, int port, boolean ssl, IMqttActionListener al, 
			MqttCallback ml, MqttTraceHandler tl)
	{
		mMqttConnectionManager.setConnectActionListener(al);
		mMqttConnectionManager.setMqttCallbackHandler(ml);
		mMqttConnectionManager.setMqttTraceCallback(tl);
		mMqttConnectionManager.createConnection(clientId, host, port, ssl,null);
	}
	
	public static void deInitMqttConnectionManager()
	{
   	if(mMqttConnectionManager!=null)
		{
			mMqttConnectionManager.unregisterResources();
			mMqttConnectionManager.setConnection(null);    	
		}		
	}
	public static void disconnect()
	{
	    	if(mMqttConnectionManager!=null)
			{
	    		mMqttConnectionManager.disconnect();
			}
	    	deInitMqttConnectionManager();	    	
	}

	@SuppressWarnings("unused")
	public static void disconnect(long timeout)
	{
		mMqttConnectionManager.disconnect(timeout);
	}
	
	public static void reconnect()
	{
		if(!mMqttConnectionManager.isConnected())
		{
		mMqttConnectionManager.reconnect();
		}
	}
	
	public static void InitMqttConnectionManager()
	{
		mMqttConnectionManager.registerResource();		
	}

	public static String getClientHandle() {
		return clientHandle;
	}

	public static void setClientHandle(String clientHandle) {
		IotManager.clientHandle = clientHandle;
	}

	public void connectionLost() {
		// TODO Auto-generated method stub
		Connection c = MqttConnectionManager.getConnection(clientHandle);
	 	if(c!=null){
	       c.addAction("Connection Lost");
	       c.changeConnectionStatus(Connection.DISCONNECTED);
	       c.setAllSubscribeStatus(Connection.UNSUB);
	 	}	
 	}

	public void unsubscribe(String topic, IMqttActionListener al) {
		// TODO Auto-generated method stub
		if(MqttConnectionManager.isConnected())
		{
			mMqttConnectionManager.setUnsubscribeActionListener(al);
			mMqttConnectionManager.unsubscribe(topic);
		}
	}

	public void subscribe(String topic, IMqttActionListener al, int qos) {
		// TODO Auto-generated method stub
		if(MqttConnectionManager.isConnected())
		{
			mMqttConnectionManager.setSubscribeActionListener(al);
			mMqttConnectionManager.subscribe(topic,qos);
		}
	}
}
