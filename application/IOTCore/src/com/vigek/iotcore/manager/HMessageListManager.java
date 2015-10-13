package com.vigek.iotcore.manager;

//import java.util.Vector;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;  
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import java.util.concurrent.ExecutorService;  
import java.util.concurrent.Executors;  
import java.util.concurrent.TimeUnit;  
import java.util.concurrent.locks.Lock;  
import java.util.concurrent.locks.ReentrantLock;

import com.vigek.iotcore.app.AppConfig;
import com.vigek.iotcore.bean.Deviceinfo;
import com.vigek.iotcore.bean.HMessage;
import com.vigek.iotcore.common.FileOps;
import com.vigek.iotcore.common.Log;
import com.vigek.iotcore.db.MessageDao;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

public class HMessageListManager{
	private String TAG = "HMessageListManager";
	private MessageDao mHMessageDao;
	
	private static HMessageListManager __instance;
	private Context context;

	private static CountDownLatch handlerInitLatch;
	private HashSet<Integer> mSelectedHMessage = new HashSet<Integer>();

	private static ArrayList<DataSetObserver> mObserver = new ArrayList<DataSetObserver>();
	private static ArrayList<IUpdateListener> mListener = new ArrayList<IUpdateListener>();
	
	private List<HMessage> HMessageList = new ArrayList<HMessage>();

	private static long  messageCount = 0;

	private ReentrantLock mListLock = new ReentrantLock();	
	
	public static HMessageListManager getInstance(Context appContext) {
		if (__instance == null) {
			synchronized (HMessageListManager.class)
			{	
			__instance = new HMessageListManager(appContext);
			}
		}
		return __instance;
	}
	
	public HMessageListManager(Context ctx)
	{
		this.context=ctx;
		this.mHMessageDao = new MessageDao(context);
		handlerInitLatch = new CountDownLatch(1);
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

	public List<HMessage> getHMessageList()
	{
		return HMessageList;
	}
	
	public List<HMessage> getHMessages(Deviceinfo device)
	{
		if(device!=null)
		{
			return mHMessageDao.getHMessages(device);
		}
		else
		{
			return null;
		}
	}
	
	public HMessage getHMessageById(int msgId)
	{
		if(mHMessageDao!=null)
		{
			return mHMessageDao.get(msgId);
		}
		else
		{	
			return null;
		}
	}
	
	public  void removeHMessage(final HMessage msg)
	{
		Log.v("remove msg" + msg.getId());
		if(mListLock.tryLock()){
			if(HMessageList.size()> 0){
				HMessageList.remove(msg);

			messageCount -= 1;
//			mCurrentRow -= 1;
			if(messageCount <0)
			{
				messageCount = 0;
			}		


			if(mObserver != null)
			{
				for(DataSetObserver d: mObserver)
				{
				d.onInvalidated();
				}
			}				
		}
			mListLock.unlock();
		}
			if(mHMessageDao!=null)
			{
				mHMessageDao.delete(msg);
			}
	
			if((msg.getType() == HMessage.HMESSAGE_TYPE_PICTURE )&&(AppConfig.getAppConfig(context).getDelMessageWithPic()))
			{
					new Thread(new Runnable(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							try {
								FileOps.deleteFile(new String(msg.getPayload()));
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}}).start();
			}		

	}
	
	public void removeHMessage(int position)
	{
		removeHMessage(getHMessageList().get(position));
	}
	
	public void updateHMessage(HMessage msg)
	{
		if(mHMessageDao!=null)
		{
			mHMessageDao.update(msg);
		}
		if(mObserver != null)
		{
			for(DataSetObserver d: mObserver)
			{
				d.onInvalidated();
			}
		}
	}

	public void createMessage(HMessage msg)
	{
		if((msg!=null)&&(mHMessageDao!=null))
		{
			mHMessageDao.add(msg);
		}
	}

	public  void addMessage(HMessage msg){
		Log.v("0 HMessageListManager addMessage" + msg.getId());
//		
//	    if((AppManager.getAppManager().isForeground(context,"ui.activity.MainActivity") == false)||(AppManager.getAppManager().isScreenOn(context)== false)){
//	    	Notify.alarm(context, AppConfig.getAppConfig(context).getClientId() , msg.getTopic(), msg.getPayload(),msg.getId());
//			Log.v("Notify.alarm of msgId:" + msg.getId());
//	    }		
//	    
		
		if(mListLock.tryLock()){
	    if(msg!=null){
//			
//			HMessage msg = new HMessage(topic, clientId, payload, device);
//			msg.setTime(new Date(System.currentTimeMillis()));
//			msg.setType(topic);

			Log.v("1 HMessageListManager addMessage of " + msg.getTopic());


		 		HMessageList.add(msg);
	 			messageCount += 1;


	 		if(HMessageList.size()> 10*AppConfig.config_default_pagesize)
	 		{
	 			HMessageList.remove(0);
	 		}

			Log.v("2 HMessageListManager addMessage of " + msg.getTopic());
			
			if(mObserver != null)
			{
				for(DataSetObserver d:mObserver)
				{
					d.onChanged();
				}
			}
			Log.v("3 HMessageListManager addMessage of " + msg.getTopic());


		 }
	    
	    	mListLock.unlock();
		}
		return;
		
	}

	public void removeSelectHMessage(int _HMessage)
	{
		if(mSelectedHMessage.size() > 0 ){

		mSelectedHMessage.remove(_HMessage);
		}
	}
	
	public  HashSet<Integer> getSelectHMessageId()
	{
		return mSelectedHMessage;
	}
	
	public  int getSelectedHMessageCount()
	{
		return mSelectedHMessage.size();
	}

	public int getHMessageCount()
	{
		if(HMessageList==null)
		{
			return 0;
		}
		else
		{
		return HMessageList.size();
		}
	}
	
	public long getHMessageCounts()
	{
		return messageCount;
	}

	
	public  boolean isUpdated()
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

	@SuppressWarnings("unchecked")
	public  void restoreHMessageList(String clientID)
	{
		Log.v("0 HMessageListManager restoreHMessageList of " + clientID);
		if(mListLock.tryLock()){
			messageCount = mHMessageDao.getHMessageCount();
	
			HMessageList = mHMessageDao.getLatestMessages(AppConfig.config_default_pagesize);
			ComparatorHMessage comparator = new ComparatorHMessage();
			Collections.sort(HMessageList, comparator);
			mListLock.unlock();
		}

			Log.v("1 HMessageListManager restoreHMessageList of " + clientID);


		handlerInitLatch.countDown();
		Log.v("2 HMessageListManager restoreHMessageList of " + clientID);

		Log.v("3 HMessageListManager restoreHMessageList of " + clientID);
		if(mListener!=null){
			for(IUpdateListener i: mListener){
				i.update(IUpdateListener.MESSAGES_UPDATED, null);
			}
		}
		Log.v("4 HMessageListManager restoreHMessageList of " + clientID);
 }


	public void clearSelectedMessage() {
		// TODO Auto-generated method stub
		mSelectedHMessage.clear();
		
	}

	public List<HMessage> getSelectedMessage() {
		// TODO Auto-generated method stub
		List<HMessage> l = new ArrayList<HMessage>();
		List<HMessage> m = getHMessageList();
		if(m!=null){
			for(int i: mSelectedHMessage)
			{
				l.add(m.get(i));
			}
			return l;
		}
		else
		{
			return null;
		}
	}

	public void setSelectMessage(int position) {
		// TODO Auto-generated method stub
		mSelectedHMessage.add(position);
	}

	public void selectAllMessages() {
		// TODO Auto-generated method stub
		mSelectedHMessage.clear();
			for(int i=0; i< HMessageList.size();i++)
			{
				mSelectedHMessage.add(i);
			}

		
	};	

	public class ComparatorHMessage implements Comparator{

		@Override
		public int compare(Object lhs, Object rhs) {
			// TODO Auto-generated method stub
			HMessage l = (HMessage)lhs;
			HMessage r = (HMessage)rhs;
			int flag = l.getTime().compareTo(r.getTime());
			return flag;
		}
		
	}

	public boolean haveUnReadMessage() {
		return mHMessageDao.haveUnReadMessage();
	}

	public int getUnreadMessagesCount() {
		return mHMessageDao.getUnreadMessagesCount();
	}

	public int getUnreadMessagesCount(Deviceinfo device) {
		return mHMessageDao.getUnreadMessagesCount(device);
	}


	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return (messageCount == 0);
	}

	public HMessage getOldestMessage() {
		// TODO Auto-generated method stub
		return mHMessageDao.getOldestMessage();
	}
	
	public HMessage getLatestMessage()
	{
		return mHMessageDao.getLatestMessage();
	}

}




