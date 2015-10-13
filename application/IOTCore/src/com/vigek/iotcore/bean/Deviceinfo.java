package com.vigek.iotcore.bean;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Collection;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "tb_deviceinfo")

public class Deviceinfo  implements Parcelable {
	@DatabaseField(generatedId = true)
	private int id;

	@DatabaseField
	String m_pName;
	
	@DatabaseField (columnName="feed")
	String m_pFeedId;
	
	@DatabaseField
	String m_pProductName;
	
	@DatabaseField
	String m_pActivationCode;
	
	@DatabaseField(columnName="mac")
	private	String m_Mac;
	
	@DatabaseField
	int    m_Type;
	
	@DatabaseField
	int    m_Icon;


	public Deviceinfo()
	{
	}
	
	public Deviceinfo(String _name, String _feedId, String _productName, String _activationCode, int type)
	{
		this.m_pName = _name;
		this.m_pFeedId = _feedId;
		this.m_pActivationCode = _activationCode;
		this.m_pProductName = _productName;
		this.m_Type = type;
		this.m_pProductName = "";
	}
	
	public Deviceinfo(Parcel p)
	{
		id = p.readInt();
		m_pName= p.readString();
		m_pFeedId = p.readString();
		m_pProductName = p.readString();
		m_pActivationCode = p.readString();
		m_Mac = p.readString();
		m_Type = p.readInt();
		m_Icon = p.readInt();
	}
	
    //////////////////////////////
    // Parcelable apis
    //////////////////////////////
    public static final Parcelable.Creator<Deviceinfo> CREATOR
            = new Parcelable.Creator<Deviceinfo>() {
                public Deviceinfo createFromParcel(Parcel p) {
                    return new Deviceinfo(p);
                }

                public Deviceinfo[] newArray(int size) {
                    return new Deviceinfo[size];
                }
            };		

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(id);
		dest.writeString(m_pName);
		dest.writeString(m_pFeedId);
		dest.writeString(m_pProductName);
		dest.writeString(m_pActivationCode);
		dest.writeString(m_Mac);
		dest.writeInt(m_Type);
		dest.writeInt(m_Icon);
	}


	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}
	
	public String getDeviceName()
	{
		return this.m_pName;
	}
	
	public void setDeviceName(String name)
	{
		m_pName = name;
	}
	
	public String getFeedId()
	{
		return this.m_pFeedId;
	}
	
	public String getActivationCode()
	{
		return this.m_pActivationCode;
	}
	
	public String getProductname()
	{
		return this.m_pProductName;
	}
	
	public void setProductName(String name)
	{
		m_pProductName = name;
	}
	
	public void setDeviceType(int type)
	{
		this.m_Type = type;
	}

	public int getDeviceType()
	{
		return this.m_Type;
	}
	
	public void setDeviceIcon(int icon)
	{
		this.m_Icon = icon;
	}
	
	public int getDeviceIcon()
	{
		return this.m_Icon;
	}

	public String getM_Mac() {
		return m_Mac;
	}

	public void setM_Mac(String m_Mac) {
		this.m_Mac = m_Mac;
	}

}
