package com.vigek.iotcore.manager;

public interface IUpdateListener
{
	public int SHOW_DIALOG  = 0;
	public int REMOVE_DIALOG = 1;
	
	public int DEVICES_UPDATED = 2;
	public int MESSAGES_UPDATED = 3;
	public int POSITIONS_UPDATED = 4;
	public int SKINS_UPDATED = 5;
	
	
	void update(int status, String result);
}