package com.vigek.iotcore.manager;

import java.util.List;
import java.util.Stack;

import com.vigek.iotcore.common.StringUtils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.os.PowerManager;

/**
 * AppManager Manager all Activities and application
 * @author : Dustin
 * @version: 1.0
 * @created: 2015-3-4
 */
public class AppManager {
	
	private static Stack<Activity> activityStack;
	private static AppManager instance;
	
	private AppManager(){
		if(activityStack==null){
			synchronized (AppManager.class)
			{
				activityStack=new Stack<Activity>();
			}
		}		
	}

	/*
	 *  get the single instance of AppManager
	 */
	public static AppManager getAppManager(){
		if(instance==null){
			instance=new AppManager();
		}
		return instance;
	}
	
	/**
	 * add Activity to stack
	 */
	public void addActivity(Activity activity){
		if(activityStack==null){
			activityStack=new Stack<Activity>();
		}
		activityStack.add(activity);
	}
	
	/**
	 * get current Activity  the latest activity push to the stack 
	 */
	public Activity currentActivity(){
		Activity activity=activityStack.lastElement();
		return activity;
	}
	
	
	public boolean isForeground(Context context, String className) {  
	       if (context == null || StringUtils.isEmpty(className)) {  
	           return false;  
	       }  
	  
	       ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);  
	       List<RunningTaskInfo> list = am.getRunningTasks(1);  
	       if (list != null && list.size() > 0) {  
	           ComponentName cpn = list.get(0).topActivity;  
//        	   Log.v("isForeground "+cpn.getShortClassName());
	           if ((("."+className).equals(cpn.getShortClassName()))||(className.equals(cpn.getShortClassName()))) {  
	               return true;  
	           }  
	       }  
	  
	       return false;  
	}  	
	
	public boolean isExist(String activityname)
	{
		boolean ret = false;
		for(Activity a: activityStack)
		{
			if(a.getLocalClassName().equals(activityname))
			{
				ret = true;
				break;
			}
		}

		return ret;
	}
	
	/**
	 * finish the latest Activity in the stack
	 */
	public void finishActivity(){
		Activity activity=activityStack.lastElement();
		finishActivity(activity);
	}
	
	/**
	 * finish the appointed Activity
	 */
	public void finishActivity(Activity activity){
		if(activity!=null){
			activityStack.remove(activity);
			activity.finish();
			activity=null;
		}
	}
	
	/**
	 * finish the Activity with appointed class name
	 */
	public void finishActivity(Class<?> cls){
		for (Activity activity : activityStack) {
			if(activity.getClass().equals(cls) ){
				finishActivity(activity);
			}
		}
	}
	
	/**
	 * finish all the Activities
	 */
	public void finishAllActivity(){
		for (int i = 0, size = activityStack.size(); i < size; i++){
            if (null != activityStack.get(i)){
            	activityStack.get(i).finish();
            }
	    }
		activityStack.clear();
	}
	
	/**
	 * exit the application
	 */
	public void AppExit(Context context) {
		try {
			finishAllActivity();
			ActivityManager activityMgr= (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			activityMgr.restartPackage(context.getPackageName());
			System.exit(0);
		} catch (Exception e) {	}
	}

	public boolean isScreenOn(Context context) {
		// TODO Auto-generated method stub
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);  
		return pm.isScreenOn();		
	}
}