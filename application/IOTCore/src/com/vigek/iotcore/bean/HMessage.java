package com.vigek.iotcore.bean;
import java.util.Collection;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.vigek.iotcore.app.AppConfig;
import com.vigek.iotcore.manager.DeviceListManager;

@DatabaseTable(tableName = "tb_message")
public class HMessage implements Parcelable {
	
	public static final int HMESSAGE_TYPE_RAW = 0x100;
	public static final int HMESSAGE_TYPE_STATE = 0x101;
	public static final int HMESSAGE_TYPE_PICTURE = 0x102;
	public static final int HMESSAGE_TYPE_AUDIO = 0x103;
	public static final int HMESSAGE_TYPE_VIDEO = 0x104;
	
	@DatabaseField(generatedId = true)
	private int id;
	
	
	@DatabaseField
	private int type;
	
	@DatabaseField
	private String topic;
	
	@DatabaseField
	private String clientId;
	
	@DatabaseField(dataType = DataType.BYTE_ARRAY)
	private byte[] payload;
	
	@DatabaseField(dataType = DataType.DATE_LONG, columnName="time")
	private Date  time;
	
	@DatabaseField(canBeNull = true, foreign = true, columnName = "device_id", foreignAutoRefresh = true, foreignAutoCreate = true)
	private Deviceinfo device;	
	
	@DatabaseField(columnName="read")
	private boolean read;
	
	public HMessage()
	{
		
	}
	
	public HMessage(String topic, String clientId, byte[]payload, Deviceinfo device)
	{
		this.topic = topic;
		this.clientId = clientId;
		this.payload = payload;
		this.device = device;
		this.type = HMESSAGE_TYPE_RAW;
		
	}
	
	public HMessage(Parcel p)
	{
		setId(p.readInt());
		setTopic(p.readString());
		setClientId(p.readString());
		payload = new byte[p.readInt()];
		p.readByteArray(payload);
		time = (Date)p.readSerializable();
		setDevice((Deviceinfo)p.readParcelable(Deviceinfo.class.getClassLoader()));
	}

    //////////////////////////////
    // Parcelable apis
    //////////////////////////////
    public static final Parcelable.Creator<HMessage> CREATOR
            = new Parcelable.Creator<HMessage>() {
                public HMessage createFromParcel(Parcel p) {
                    return new HMessage(p);
                }

                public HMessage[] newArray(int size) {
                    return new HMessage[size];
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
		dest.writeString(topic);
		dest.writeString(clientId);
		dest.writeInt(payload.length);
		dest.writeByteArray(payload);
		dest.writeSerializable(time);
		dest.writeParcelable(device, flags);
	}

	public Deviceinfo getDevice() {
		return device;
	}

	public void setDevice(Deviceinfo device) {
		this.device = device;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public byte[] getPayload() {
		return payload;
	}

	public void setPayload(byte[] payload) {
		this.payload = payload;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public int getType() {
		return type;
	}
	
	public void setType(String topic)
	{
		String  topics[] = topic.split("\\"+AppConfig.config_topic_split);
		String  type = topics[1];
		if(type.equals(DeviceListManager.DeviceSubTopic[2]))
		{
			this.type = HMESSAGE_TYPE_AUDIO;
		}
		else if(type.equals(DeviceListManager.DeviceSubTopic[1]))
		{
			this.type = HMESSAGE_TYPE_PICTURE;
		}
		else if(type.equals(DeviceListManager.DeviceSubTopic[0]))
		{
			this.type = HMESSAGE_TYPE_STATE;
		}
		else
		{
			this.type = HMESSAGE_TYPE_RAW;
		}
		
	}

	public void setType(int type) {
		this.type = type;
	}
		
	

}
