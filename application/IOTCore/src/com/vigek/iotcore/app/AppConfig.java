package com.vigek.iotcore.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

//import com.example.smarthome.android.mqttservice.Connection;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.vigek.iotcore.common.StringUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;     
import android.telephony.TelephonyManager;

@SuppressLint("NewApi")
public class AppConfig {

	private final static String APP_CONFIG = "config";
	  /** Application TAG for logs where class name is not used*/
	public  static final String TAG = "APP_CONFIG";
	
	public  static final int config_default_pagesize = 10;
	public  static final int config_default_picPageSize = 12;
	public static String config_authKey = "123456";
	  /*Default values **/
	  /** Default QOS value
	   *  0 : at most once
	   *  1:  at least once
	   *  2:  exactly once
	   * */
	public  static final int config_defaultQos = 2;
	public  static final int config_defaultInterval = 20000;
	  /** Default timeout*/
	public  static final int config_defaultTimeOut = 2000;
	  /** Default keep alive value*/
	public  static final int config_defaultKeepAlive = 10;
	  /** Default SSL enabled flag*/
	public  static final boolean config_defaultSsl = false;
	  /** Default message retained flag */
	public  static final boolean config_defaultRetained = false;
	  /** Default last will message*/
	public  static final MqttMessage config_defaultLastWill = null;
	
	public  static final String config_defaultServerURI = "iot.eclipse.org";
	  /** Default port*/
	public  static final int config_defaultPort = 1883;
	
	public static final String config_defaultAppURI = "www.vigek.com";
	public static final String config_app_uri = "application";

	  /** Connect Request Code */
	public  static final int config_connect = 0;
	  /** Advanced Connect Request Code  **/
	public  static final int config_advancedConnect = 1;
	  /** Last will Request Code  **/
	public  static final int config_lastWill = 2;
	  /** Show History Request Code  **/
	public  static final int config_showHistory = 3;

	  /* Bundle Keys */
	public static final String config_device = "device";

	  /** Server Bundle Key **/
	public  static final String config_server = "server";
	  /** Port Bundle Key **/
	public  static final String config_port = "port";
	  /** ClientID Bundle Key **/
	public  static final String config_clientId = "clientId";
	  /** Topic Bundle Key **/
	public  static final String config_topic = "topic";
	public  static final String config_topic_split = "/";
	  /** History Bundle Key **/
	public  static final String config_history = "history";
	  /** Message Bundle Key **/
	public  static final String config_alarm	= "alarm";
	public  static final String config_message = "message";
	public  static final String config_messageid = "messageid";
	public  static final String config_deviceid = "deviceid";
	  /** Retained Flag Bundle Key **/
	public static final String config_retained = "retained";
	  /** QOS Value Bundle Key **/
	  public  static final String config_qos = "qos";
	  /** User name Bundle Key **/
	  public  static final String config_username = "username";
	  /** Password Bundle Key **/
	  public  static final String config_password = "password";
	  /** Keep Alive value Bundle Key **/
	  public static final String config_keepalive = "keepalive";
	  /** Timeout Bundle Key **/
	  public  static final String config_timeout = "timeout";
	  /** SSL Enabled Flag Bundle Key **/
	  public  static final String config_ssl = "ssl";
	  /** SSL Key File Bundle Key **/
	  public  static final String config_ssl_key = "ssl_key";
	  /** Connections Bundle Key **/
	  public  static final String config_connections = "connections";
	  /** Clean Session Flag Bundle Key **/
	  public  static final String config_cleanSession = "cleanSession";
	  /** Action Bundle Key **/
	  public  static final String config_action = "action";
	  
	  public static final String ALARM_KILLED = "ALARM_KILLED";
	  public static final String ALARM_INTENT_EXTRA = "ALARM_INTENT_EXTRA";
	  public static final String ALARM_KILLED_TIMEOUT = "ALARM_KILLED_TIMEOUT";
	  public static final String ALARM_REPLACED = "ALARM_REPLACED";
	  public static final String DEFAULT_ALARM_TIMEOUT = "DEFAULT_ALARM_TIMEOUT";
	  
	  public static final String  ALARM_ALERT_ACTION = "com.vigek.smokealarm.common.ALARM_ALERT";       // Play the alarm alert and vibrate the device.
	  
	  // vibrate
	  public static final String config_vibrate = "vibrate";
	  public static boolean config_vibrate_default = true;
	  
	  public static final String config_sound = "sound";
	  public static final String config_picPath = "picpath";
	  
	  // message
	  public static boolean config_del_with_pic_default = true;
	  
	  public static final String config_del_with_pic = "delmessage";
	  
	  // simpleconfig
	  public static final String config_simpleconfig = "simpleconfig";
	  public static final String config_ipaddress = "ip";
	  public static final String config_macaddress = "mac";
	  
	  // position
	  public static final String config_position = "position";
	  public static final String config_position_label = "positionLabel";
	  public static final String config_latitude = "latitude";
	  public static final String config_longitude = "longitude";

	  /* Property names */

	  /** Property name for the history field in {@link Connection} object for use with {@link java.beans.PropertyChangeEvent} **/
	  public  static final String config_historyProperty = "history";

	  /** Property name for the connection status field in {@link Connection} object for use with {@link java.beans.PropertyChangeEvent} **/
	  public  static final String config_ConnectionStatusProperty = "connectionStatus";

	  /* Useful constants*/

	  /** Space String Literal **/
	  public  static final String space = " ";
	  /** Empty String for comparisons **/
	  public  static final String empty = new String();	

	public final static String CONF_APP_UNIQUEID = "APP_UNIQUEID";
	public final static String CONF_ACCESSTOKEN = "accessToken";
	public final static String CONF_ACCESSSECRET = "accessSecret";
	public final static String CONF_EXPIRESIN = "expiresIn";
	public final static String CONF_LOAD_IMAGE = "perf_loadimage";

	public final static String SAVE_IMAGE_PATH = "save_image_path";
	@SuppressLint("NewApi")
	public final static String DEFAULT_SAVE_IMAGE_PATH = Environment.getExternalStorageDirectory()+ File.separator+ "smarthome"+ File.separator;
			
	private Context mContext;

	private static AppConfig appConfig;

	public static AppConfig getAppConfig(Context context) {
		if (appConfig == null) {
			synchronized (AppConfig.class)
			{
				appConfig = new AppConfig();
				appConfig.mContext = context.getApplicationContext();
			}
		}
		return appConfig;
	}


	public static SharedPreferences getSharedPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}



	public static boolean isLoadImage(Context context) {
		return getSharedPreferences(context).getBoolean(CONF_LOAD_IMAGE, true);
	}

	public void setExpiresIn(long expiresIn) {
		set(CONF_EXPIRESIN, String.valueOf(expiresIn));
	}

	public long getExpiresIn() {
		return StringUtils.toLong(get(CONF_EXPIRESIN));
	}

	public String getClientId() {
		String uniqueID = get(AppConfig.CONF_APP_UNIQUEID);
		if(StringUtils.isEmpty(uniqueID)){
			  TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE); 
			  String imei = ""+tm.getDeviceId();
			  String serialNo = ""+Build.SERIAL;
			  UUID myUUID = new UUID(imei.hashCode(), serialNo.hashCode());
			  uniqueID = myUUID.toString();
			set(AppConfig.CONF_APP_UNIQUEID, uniqueID);
		}
		return uniqueID;
	}	

	public String getAppUri()
	{
		String Uri = get(config_app_uri);
		if(StringUtils.isEmpty(Uri))
		{
			Uri = config_defaultAppURI;
		}
		return Uri;
	}
	
	public String getDefaultAlarmTimeout(String timeout)
	{
		String defaultTimeOut = get(AppConfig.DEFAULT_ALARM_TIMEOUT);
		if(StringUtils.isEmpty(defaultTimeOut))
		{
			return timeout;
		}
		return defaultTimeOut;
		
	}
	
	public void setClientId(String clientId)
	{
		set(AppConfig.CONF_APP_UNIQUEID, clientId);
	}
	
	public void setServerURI(String server)
	{
		set(config_server, server);
	}
	
	public String getServerURI()
	{
		String serverUri = get(config_server);
		if(StringUtils.isEmpty(serverUri))
		{
			serverUri = config_defaultServerURI;
		}
		return serverUri;
	}
	
	public void setServerPort(int port)
	{
		set(config_port, String.valueOf(port));
	}
	
	public int getServerPort()
	{
		return StringUtils.toInt(get(config_port));
	}

	public String getPicPath()
	{
		String path = get(config_picPath);
		
		if(StringUtils.isEmpty(path))
		{
			path =  DEFAULT_SAVE_IMAGE_PATH;
			try {
				setPicPath(path);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return path;
		}
		return path;
	}
	
	public void setPicPath(String path) throws IOException
	{
		File picDir = new File(path);
		if(picDir!=null &&  !picDir.exists())
		{
			if(!picDir.mkdirs())
			{
				throw new FileNotFoundException("Cannot create folder: " + picDir.getAbsolutePath());
			}
			else
			{
				if (!new File(picDir, ".nomedia").createNewFile()) {
					throw new FileNotFoundException("Cannot create file .nomedia");
				}
			}
		}
		else
		{
			if (!new File(picDir, ".nomedia").createNewFile()) {
			//	throw new FileNotFoundException("Cannot create file .nomedia");
			}
		}
		set(config_picPath, path);
	}
	
	public String getSound()
	{
		return get(config_sound);
	}
	
	public void setSound(String path)
	{
		set(config_sound,path);
	}
	
	public void setVibrate(boolean v)
	{
		if(v)
			{
			set(config_vibrate, "true");
			}
		else
		{
			set(config_vibrate, "false");
		}
	}
	
	public boolean getVibrate()
	{
		String vibrate = get(config_vibrate);
		if(StringUtils.isEmpty(vibrate))
		{
			if(config_vibrate_default == true)
			{
				set(config_vibrate,"true");
			}
			else
			{
				set(config_vibrate, "false");
			}
			return config_vibrate_default;
		}
		else if(get(config_vibrate).equals("true"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean getDelMessageWithPic()
	{
		String check = get(config_del_with_pic);
		if(StringUtils.isEmpty(check))
		{
			if(config_del_with_pic_default == true)
			{
				set(config_del_with_pic,"true");
			}
			else
			{
				set(config_del_with_pic, "false");
			}
			return config_del_with_pic_default;
		}
		else if(get(config_del_with_pic).equals("true"))
		{
			return true;
		}
		else
		{
			return false;
		}		
	}
	
	public void setDelMessageWithPic(boolean check)
	{
		if(check)
		{
			set(config_del_with_pic, "true");
		}
		else
		{
			set(config_del_with_pic, "false");
		}		
	}

	public String get(String key) {
		Properties props = get();
		return (props != null) ? props.getProperty(key) : null;
	}

	public Properties get() {
		FileInputStream fis = null;
		Properties props = new Properties();
		try {
			File dirConf = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
			fis = new FileInputStream(dirConf.getPath() + File.separator
					+ APP_CONFIG);

			props.load(fis);
		} catch (Exception e) {
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
		return props;
	}

	private void setProps(Properties p) {
		FileOutputStream fos = null;
		try {
			File dirConf = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
			File conf = new File(dirConf, APP_CONFIG);
			fos = new FileOutputStream(conf);

			p.store(fos, null);
			fos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
			}
		}
	}

	public void set(Properties ps) {
		Properties props = get();
		props.putAll(ps);
		setProps(props);
	}

	public void set(String key, String value) {
		Properties props = get();
		props.setProperty(key, value);
		setProps(props);
	}

	public void remove(String... key) {
		Properties props = get();
		for (String k : key)
			props.remove(k);
		setProps(props);
	}
}
