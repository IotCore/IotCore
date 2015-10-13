/*******************************************************************************
 * Copyright (c) 1999, 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 */
package com.vigek.iotcore.mqtt;

import com.vigek.iotcore.app.AppConfig;
import com.vigek.iotcore.app.AppContext;
import com.vigek.iotcore.bean.Deviceinfo;
import com.vigek.iotcore.bean.HMessage;
import com.vigek.iotcore.common.Log;
import com.vigek.iotcore.manager.DeviceListManager;
import com.vigek.iotcore.manager.HMessageListManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import android.content.Context;
import android.content.Intent;
/**
 * Handles call backs from the MQTT Client
 *
 */
public class MqttCallbackHandler implements MqttCallback {

  /** {@link Context} for the application used to format and import external strings**/
  private Context context;
  /** Client handle to reference the connection that this handler is attached to**/
  private String clientHandle;

  /**
   * Creates an <code>MqttCallbackHandler</code> object
   * @param context The application's context
   * @param clientHandle The handle to a {@link Connection} object
   */
  public MqttCallbackHandler(Context context, String clientHandle)
  {
    this.context = context;
    this.clientHandle = clientHandle;
  }

  /**
   * @see org.eclipse.paho.client.mqttv3.MqttCallback#connectionLost(java.lang.Throwable)
   */
  @Override
  public void connectionLost(Throwable cause) {
//	  cause.printStackTrace();
    if (cause != null) {
		AppContext.connectionLost();
    }
  }


	private File generateFile(String topic, Deviceinfo device) {
		String filename = AppConfig.getAppConfig(context).getPicPath();
		filename += "/"+device.getDeviceName();

		File dir = new File(filename);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				// failed to create dir
				return null; 
			}
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH);
		String currentDateandTime = sdf.format(new Date());
		File msgFile = null;
		String extName = ".jpg";

		msgFile = new File(dir, currentDateandTime+extName);

		return msgFile;
	}	
	 
	public synchronized HMessage saveMsgtoFile(String topic, MqttMessage msg, Deviceinfo device)
	{
		String resultfile = null;
		FileOutputStream out = null;
		HMessage message = null;
		File file = generateFile(topic, device);
		if(file!=null)
		{
			try{
				out = new FileOutputStream(file);
				out.write(msg.getPayload());
				resultfile = file.getAbsolutePath();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
	
			try {
				out.flush();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			message = new HMessage(topic, AppConfig.getAppConfig(context).getClientId(), resultfile.getBytes(), device);
		}

		if(message!=null){
			message.setTime(new Date(System.currentTimeMillis()));
			message.setType(topic);
		}
//		device.getMessages().add(msg); //it will invode dao.create()
//		out = null;
//		file = null;
//		msg = null;		
//		System.gc();	
		return message;
			
		
	}  
  /**
   * @see org.eclipse.paho.client.mqttv3.MqttCallback#messageArrived(java.lang.String, org.eclipse.paho.client.mqttv3.MqttMessage)
   */
  @Override
  public void messageArrived(final String topic, final MqttMessage message)/* throws Exception*/ {
    if(message == null)
    {
    	return;
    }
    
    final Deviceinfo currentDevice = AppContext.getCurrentDevice(context, topic);
    Log.v("arrived message from " + currentDevice);
    if(currentDevice!=null){
//	    final HMessage msg = saveMsgtoFile(topic, message, currentDevice);
//		HMessageListManager.getInstance(context).createMessage(msg);
//		
//	    AppContext.pool.execute(new Runnable() {
//
//			@Override
//			public void run() {
				// TODO Auto-generated method stub
			    final HMessage msg = saveMsgtoFile(topic, message, currentDevice);
				HMessageListManager.getInstance(context).createMessage(msg);
			    Log.v("addMessage messageid  " + msg.getId());
			    HMessageListManager.getInstance(context).addMessage(msg);
//		}
//
//		});		
    }
    else  // device is not in the current device list, so unsubscribe it's topics
    {
		AppContext.unsubscribeTopic(topic);
    }		    
	
  }

  /**
   * @see org.eclipse.paho.client.mqttv3.MqttCallback#deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken)
   */
  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    // Do nothing
  }

}
