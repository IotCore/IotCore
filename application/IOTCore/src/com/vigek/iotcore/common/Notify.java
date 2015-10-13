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
package com.vigek.iotcore.common;

import java.util.Calendar;

import com.vigek.iotcore.R;
import com.vigek.iotcore.app.AppConfig;
import com.vigek.iotcore.manager.AppManager;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.BroadcastReceiver.PendingResult;
import android.os.Bundle;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat.Builder;
import android.widget.Toast;
/**
 * Provides static methods for creating and showing notifications to the user.
 *
 */
public class Notify {

  /** Message ID Counter **/
  private static int MessageNum = 0;
  public static final String MessageAlarmTag = "alarm";
  public static final int MessageAlarmActivity = 1;
  public static final int MessageNotification = 2;
  
  
  public int getMessageNum()
  {
	  return MessageNum;
  }
  
	public static void CrashReport(final Context cont,
			final String crashReport) {
		AlertDialog.Builder builder = new AlertDialog.Builder(cont);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setTitle("ERROR");
		builder.setMessage(crashReport);
		builder.setPositiveButton("Submit",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 发�?�异常报�?
						Intent i = new Intent(Intent.ACTION_SEND);
						// i.setType("text/plain"); //模拟�?
						i.setType("message/rfc822"); // 真机
						i.putExtra(Intent.EXTRA_EMAIL,
								new String[] { "dustin.guo.sz@qq.com" });
						i.putExtra(Intent.EXTRA_SUBJECT,
								"Vigek 智能烟雾报警�? - 错误报告");
						i.putExtra(Intent.EXTRA_TEXT, crashReport);
						cont.startActivity(Intent.createChooser(i, "发�?�错误报�?"));
						// �?�?
						AppManager.getAppManager().AppExit(cont);
					}
				});
		builder.setNegativeButton("OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// �?�?
						AppManager.getAppManager().AppExit(cont);
					}
				});
		builder.show();
	}  


  /**
   * Displays a notification in the notification area of the UI
   * @param context Context from which to create the notification
   * @param messageString The string to display to the user as a message
   * @param intent The intent which will start the activity when the user clicks the notification
   * @param notificationTitle The resource reference to the notification title
   */
  public static void notifcation(Context context, String messageString, Intent intent, int notificationTitle) {

    //Get the notification manage which we will use to display the notification
    String ns = Context.NOTIFICATION_SERVICE;
    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(ns);

    Calendar.getInstance().getTime().toString();

    long when = System.currentTimeMillis();

    //get the notification title from the application's strings.xml file
    CharSequence contentTitle = context.getString(notificationTitle);

    //the message that will be displayed as the ticker
    String ticker = contentTitle + " " + messageString;

    //build the pending intent that will start the appropriate activity
    PendingIntent pendingIntent = PendingIntent.getActivity(context,
        AppConfig.config_showHistory, intent, 0);

    //build the notification
    Builder notificationCompat = new Builder(context);
    notificationCompat.setAutoCancel(true)
        .setContentTitle(contentTitle)
        .setContentIntent(pendingIntent)
        .setContentText(messageString)
        .setTicker(ticker)
        .setWhen(when)
        .setSmallIcon(R.drawable.pick_from_camera);

    Notification notification = notificationCompat.build();
    //display the notification
    mNotificationManager.notify(MessageNotification, notification);
    MessageNum++;

  }

  /**
   * Display a toast notification to the user
   * @param context Context from which to create a notification
   * @param text The text the toast should display
   * @param duration The amount of time for the toast to appear to the user
   */
  public static void toast(Context context, CharSequence text, int duration) {
    Toast toast = Toast.makeText(context, text, duration);
    toast.show();
  }

}
