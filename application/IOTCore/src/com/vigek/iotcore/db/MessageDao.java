package com.vigek.iotcore.db;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.vigek.iotcore.app.AppConfig;
import com.vigek.iotcore.common.Log;
import com.vigek.iotcore.bean.Deviceinfo;
import com.vigek.iotcore.bean.HMessage;

public class MessageDao
{
	private Context context;
	private Dao<HMessage, Integer> MessageDaoOpe;
	private DatabaseHelper helper;

	@SuppressWarnings("unchecked")
	public MessageDao(Context context)
	{
		this.context = context;
		try
		{
			helper = DatabaseHelper.getHelper(context);
			MessageDaoOpe = helper.getDao(HMessage.class);
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * add Message
	 * 
	 * @param device
	 */
	public void add(HMessage msg)
	{
		try
		{
			DeviceinfoDao deviceinfoDao = new DeviceinfoDao(context);
			MessageDaoOpe.create(msg);
			deviceinfoDao.update(msg.getDevice());
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	
	public void update(HMessage msg)
	{
		try
		{
			MessageDaoOpe.update(msg);
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public  List<HMessage> queryForAll()
	{
		try {
			return MessageDaoOpe.queryForAll();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public int delete(HMessage msg)
	{
		int ret = 0;
		try
		{
			ret =  MessageDaoOpe.delete(msg);
		
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		return ret;
	}
	

	/**
	 * 
	 * 
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public HMessage getMessageWithDevice(int id)
	{
		HMessage msg = null;
		try
		{
			msg = MessageDaoOpe.queryForId(id);
			helper.getDao(Deviceinfo.class).refresh(msg.getDevice());

		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		return msg;
	}
	
	public List<HMessage> getPicMessageList()
	{
		try {
			return MessageDaoOpe.queryBuilder().orderBy("time", false).where().eq("type", HMessage.HMESSAGE_TYPE_PICTURE).query();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * 
	 * @param id
	 * @return
	 */
	public HMessage get(int id)
	{
		HMessage msg = null;
		try
		{
			msg = MessageDaoOpe.queryForId(id);

		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		return msg;
	}
	
	public long getHMessageCount()
	{
		QueryBuilder<HMessage, ?> builder = MessageDaoOpe.queryBuilder();
		builder.setCountOf(true);
		long count = 0;
		try {
			count = MessageDaoOpe.countOf();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return count;
	}
	
	public long getHMessageCount(Deviceinfo device)
	{
//		QueryBuilder<HMessage, ?> builder = MessageDaoOpe.queryBuilder();
//		builder.setCountOf(true);
		String query = "select count(*) from tb_message where device_id = "+ device.getId();
		long count = 0;
		try {
			count = MessageDaoOpe.queryRawValue(query);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return count;
		
	}
	
	public long getHMessageCount(int msgType)
	{
//		QueryBuilder<HMessage, ?> builder = MessageDaoOpe.queryBuilder();
//		builder.setCountOf(true);
		String query = "select count(*) from tb_message where type = "+ msgType;
		long count = 0;
		try {
			count = MessageDaoOpe.queryRawValue(query);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return count;		
	}
	
	public  List<HMessage> getHMessages(long start, long pagesize)
	{
//		QueryBuilder<HMessage, ?> builder = MessageDaoOpe.queryBuilder();
//		builder.setCountOf(false);
//		
		try {
			return MessageDaoOpe.queryBuilder().offset(start).limit(pagesize).query();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public  List<HMessage> getHMessages(Deviceinfo device)
	{
		try {
			return MessageDaoOpe.queryBuilder().where().eq("device_id",device.getId()).query();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	
	public  List<HMessage> getLatestMessages(long pagesize)
	{
		try {
			return MessageDaoOpe.queryBuilder().orderBy("id",false).limit(pagesize).query();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public HMessage getLatestMessage()
	{
		try {
			return  MessageDaoOpe.queryBuilder().orderBy("id", false).queryForFirst();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public HMessage getOldestMessage()
	{
		try {
			return  MessageDaoOpe.queryBuilder().queryForFirst();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;		
	}
	
	public List<HMessage> listByMessageType(int type)
	{
		try
		{
			return MessageDaoOpe.queryBuilder().where().eq("type", type).query();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public List<HMessage> listByDeviceId(int deviceId)
	{
		try
		{
			/*QueryBuilder<Article, Integer> articleBuilder = articleDaoOpe
					.queryBuilder();
			QueryBuilder userBuilder = helper.getDao(User.class).queryBuilder();
			articleBuilder.join(userBuilder);
			
			
			Where<Article, Integer> where = queryBuilder.where();
			where.eq("user_id", 1);
			where.and();
			where.eq("name", "xxx");

			// 
			articleDaoOpe.queryBuilder().//
					where().//
					eq("user_id", 1).and().//
					eq("name", "xxx");
			//
			articleDaoOpe.updateBuilder().updateColumnValue("name","zzz").where().eq("user_id", 1);
			where.or(
					//
					where.and(//
							where.eq("user_id", 1), where.eq("name", "xxx")),
					where.and(//
							where.eq("user_id", 2), where.eq("name", "yyy")));*/

			return MessageDaoOpe.queryBuilder().where().eq("device_id", deviceId)
					.query();
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public int getUnreadMessagesCount(Deviceinfo device) {
		// TODO Auto-generated method stub
		String query = "select count(*) from tb_message where device_id = "+device.getId()+ "  and read = 0";
		int sum = 0;
		try {
			sum = (int)MessageDaoOpe.queryRawValue(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sum;
	}

	public boolean haveUnReadMessage() {
		// TODO Auto-generated method stub
		return getUnreadMessagesCount() > 0;
	}
	
	public boolean haveUnReadMessage(Deviceinfo device)
	{
		return getUnreadMessagesCount(device) > 0;
	}

	public int getUnreadMessagesCount() {
		// TODO Auto-generated method stub
		String query = "select count(*) from tb_message where read = 0";
		int sum = 0;
		try {
			sum = (int)MessageDaoOpe.queryRawValue(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sum;

	}
	
	@SuppressWarnings("deprecation")
	public void clearAllMessage()
	{
		MessageDaoOpe.deleteBuilder().clear();
	}
}
