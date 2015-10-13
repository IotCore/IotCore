package com.vigek.iotcore.app;

import android.app.AlarmManager;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.vigek.iotcore.R;
import com.vigek.iotcore.common.AlarmAlertWakeLock;
import com.vigek.iotcore.common.AsyncHandler;
import com.vigek.iotcore.common.Log;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.BroadcastReceiver.PendingResult;
import android.os.PowerManager.WakeLock;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class AppInitReceiver  extends BroadcastReceiver {
	private static final String TAG = "AppTimerService";
	private Context mContext;	
	public static final String ACTION_BOOT = 
			"android.intent.action.BOOT_COMPLETED";
	public static final String ACTION_NETWORK_CHANGED = 
			"android.net.conn.CONNECTIVITY_CHANGE";
	public static final String ACTION_MQTT_SERVICE = 
			"com.vigek.smokealarm.action.MQTT_SERVICE";
	
	private PendingIntent pendingIntent;


	@Override
	public void onReceive(Context context, final Intent intent) {
		mContext = context;
        final String action = intent.getAction();
        SimpleDateFormat    formatter    =   new  SimpleDateFormat(mContext.getString(R.string.dateFormat)); 
        String timeString = formatter.format(new Date(System.currentTimeMillis()));
        
        Log.v(TAG,"AppInitReceiver " + action +"" + timeString);
        final ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        final PendingResult result = goAsync();
//        final WakeLock wl = AlarmAlertWakeLock.createPartialWakeLock(context);
//        final int SelectedDevice = 0;
//        wl.acquire();
        AlarmAlertWakeLock.acquireCpuWakeLock(context);
        AsyncHandler.post(new Runnable() {
            @Override public void run() {
                // Remove the snooze alarm after a boot.
                if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
//                    Alarms.saveSnoozeAlert(context, Alarms.INVALID_ALARM_ID, -1);
//                    Alarms.disableExpiredAlarms(context);
            		Intent intent = new Intent(mContext, AppTimerService.class);
//            		intent.putExtra(DeviceListManager.SELECT_DEVICE, SelectedDevice);
                    mContext.startService(intent);
                    // Clear stopwatch and timers data
//                    SharedPreferences prefs =
//                            PreferenceManager.getDefaultSharedPreferences(context);
                    Log.v("","AlarmInitReceiver - Reset timers and clear stopwatch data");
//                    TimerObj.resetTimersInSharedPrefs(prefs);
//                    Utils.clearSwSharedPref(prefs);
//
//                    if (!prefs.getBoolean(PREF_VOLUME_DEF_DONE, false)) {
//                        // Fix the default
//                        Log.v("AlarmInitReceiver - resetting volume button default");
//                        switchVolumeButtonDefault(prefs);
//                    }
                }
                else if(action.equals(ACTION_NETWORK_CHANGED))
                {
                	Bundle bundle = intent.getExtras();
                	if(bundle!=null)
                	{
                		final NetworkInfo info = (NetworkInfo)bundle.get(ConnectivityManager.EXTRA_NETWORK_INFO);
                		int NetworkType = info.getType();
                     	Log.v(TAG,"NetWork Type = "+ NetworkType);
                     	if(NetworkType == ConnectivityManager.TYPE_WIFI)
                     	{
                     		AlarmAlertWakeLock.acquireWifiLock(mContext);
                     	}
                     	else
                     	{
                     		AlarmAlertWakeLock.releaseWifiLock();
                     	}
                	}
                	
//                	if(wifi.isConnected())
//                	{
//                		Intent intent = new Intent(mContext, AppTimerService.class);
//                        mContext.startService(intent);
//                	}
                }
                else if(action.equals(ACTION_MQTT_SERVICE))
                {
            		 AppContext.getAppContext().StartAppTimerService();
                     AppContext.scheduleNextAlarm(AppConfig.config_defaultInterval);
               }
                
//                Alarms.setNextAlert(context);
                result.finish();
                Log.v("","AlarmInitReceiver finished");
                AlarmAlertWakeLock.releaseCpuLock();
                
            }
        });
    }
	
}
