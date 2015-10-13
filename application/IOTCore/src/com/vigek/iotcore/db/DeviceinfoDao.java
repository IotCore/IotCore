package com.vigek.iotcore.db;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.vigek.iotcore.bean.Deviceinfo;

public class DeviceinfoDao
{
	private Context context;
	private Dao<Deviceinfo, Integer> DeviceinfoDaoOpe;
	private DatabaseHelper helper;

	@SuppressWarnings("unchecked")
	public DeviceinfoDao(Context context)
	{
		this.context = context;
		try
		{
			helper = DatabaseHelper.getHelper(context);
			DeviceinfoDaoOpe = helper.getDao(Deviceinfo.class);
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * add device
	 * 
	 * @param device
	 */
	public void add(Deviceinfo device)
	{
		try
		{
			DeviceinfoDaoOpe.create(device);
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
	

	public void refresh(Deviceinfo device)
	{
		try {
			DeviceinfoDaoOpe.refresh(device);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void update(Deviceinfo device)
	{
		try
		{
			DeviceinfoDaoOpe.update(device);
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public List<Deviceinfo> queryForAll() throws SQLException
	{
		return DeviceinfoDaoOpe.queryForAll();
	}
	
	public int delete(Deviceinfo device)
	{
		int ret = 0;
		try
		{
			ret =  DeviceinfoDaoOpe.delete(device);
		
		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		return ret;
	}
	
	public Deviceinfo getDeviceBySn(String sn)
	{
		Deviceinfo device = null;
		
		try {
			device = DeviceinfoDaoOpe.queryBuilder().where().eq("feed", sn).queryForFirst();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return device;
		
	}
	
	public Deviceinfo getDeviceByID(int id)
	{
		Deviceinfo device = null;
		
		try {
			device = DeviceinfoDaoOpe.queryForId(id);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return device;
	}

	/**
	 * 
	 * 
	 * @param id
	 * @return
	 */
	public Deviceinfo get(int id)
	{
		Deviceinfo article = null;
		try
		{
			article = DeviceinfoDaoOpe.queryForId(id);

		} catch (SQLException e)
		{
			e.printStackTrace();
		}
		return article;
	}
}
