package com.vigek.iot;

import java.util.Arrays;
import java.util.ArrayList;
import java.nio.*;

import android.content.Context;


public class CommandManager {

	public static final int MODE_NONE = 0;
	public static final int GPIO_MODE = 0x0001;
	public static final int PHOTO_MODE = 0x0002;

	public static final int GPIO_COMMAND_HIGH = 1;
	public static final int GPIO_COMMAND_LOW  = 2;
	public static final int GPIO_COMMAND_PWM  = 3;
    
	private ByteBuffer mByteBuf = null;
	private static CommandManager    mInstance = null;
	
	
	private int mWorkType;
	
	private Context mContext;
	
	private CommandManager(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
		mByteBuf = ByteBuffer.allocate(1024);
		mByteBuf.order(ByteOrder.LITTLE_ENDIAN);
		mWorkType = MODE_NONE;
//		mRealTimeCfg = new real_time_config();
	}


	
	public static CommandManager getCommandManager(Context context)
	{
		if(mInstance == null)
		{
			mInstance = new CommandManager(context);
		}

		return mInstance;
			
	}
	
	public void setWorkType(int mode)
	{
		mWorkType = mode;
	}
	
	public int getWorkType()
	{
		return mWorkType;
	}
//	
//	public void setRealTimeSwitch(short sw)
//	{
//		mRealTimeCfg.switches = sw;
//	}
//	
//	public short getRealTimeSwitch()
//	{
//		return mRealTimeCfg.switches;
//	}
//	
//	public void setRealTimeWhiteLedBrightness(short wb)
//	{
//		mRealTimeCfg.switches |= RT_CTRL_WHITE_LED;
//		mRealTimeCfg.white_led_brightness = wb; 
//	}
//	
//	public short getRealTimeWhiteLedBrightness()
//	{
//		return mRealTimeCfg.white_led_brightness;
//	}
//	
//	public void setRealTimeColor( int color)
//	{
//		mRealTimeCfg.switches |= RT_CTRL_RGB_LED;
////		short l = (short)((color & 0x00ff0000)>>8);
////		l = (short)(color & 0x0000ff00);
////		l= (short)((color & 0x000000ff) <<8);
//		mRealTimeCfg.reg_led_r_pwm =  (short)((color & 0x00ff0000)>>14);
//		mRealTimeCfg.reg_led_g_pwm = (short)((color & 0x0000ff00)>>6);
//		mRealTimeCfg.reg_led_b_pwm = (short)((color & 0x000000ff) <<2);
//	}

	public byte[] toByteArray(int mode, int gpioNum, int gpioType)
	{
		byte[] tempByteArray = null;
		int position = 0;
		switch(mWorkType){
		case GPIO_MODE:
			{
				mByteBuf.clear();
				mByteBuf.putShort((short)0xBB); // BB 00 
				mByteBuf.put((byte)0x0c);       //CMD: c
				mByteBuf.put((byte)0);          // status: default 0
				if(gpioType==GPIO_COMMAND_HIGH)
				{
					mByteBuf.putShort((short)0x23);  // cmd length: 
					mByteBuf.put((byte)gpioNum);     // pin num
					mByteBuf.put((byte)0x03);			// output
					mByteBuf.put((byte)0x0);			// default 0
					mByteBuf.put((byte)0x01);			// polarity 
					mByteBuf.put((byte)0x0);			// no pull
					mByteBuf.putInt((int)0x00000001); // high level
					mByteBuf.putShort((short)0);       // 
					mByteBuf.putShort((short)0);
					mByteBuf.putInt((int)0xffffffff);
					mByteBuf.putInt((int)0x0);
					mByteBuf.putInt((int)0);
					mByteBuf.putInt((int)0);
					mByteBuf.putInt((int)0);					
					mByteBuf.put((byte)0x01);
					mByteBuf.put((byte)0x0);
				}
				else if(gpioType == GPIO_COMMAND_LOW)
				{
					mByteBuf.putShort((short)0x23);  // cmd length: 
					mByteBuf.put((byte)gpioNum);     // pin num
					mByteBuf.put((byte)0x03);			// output
					mByteBuf.put((byte)0x0);			// default 0
					mByteBuf.put((byte)0x01);			// output polarity
					mByteBuf.put((byte)0x0);			// no pull
					mByteBuf.putInt((int)0x00000000); // output level
					mByteBuf.putShort((short)0);       // input high level
					mByteBuf.putShort((short)0);       // input low level 
					mByteBuf.putInt((int)0xffffffff);   //start time. 0xffffffff:current
					mByteBuf.putInt((int)0x0);			//delay time. 0: forever
					mByteBuf.putInt((int)0);			// event period. 0: no period
					mByteBuf.putInt((int)0);			//	period count 0: no period
					mByteBuf.putInt((int)0);			// next time     0: no period		
					mByteBuf.put((byte)0x01);           // event_pended_flag
					mByteBuf.put((byte)0x0);			// related gpio
				}
				else if(gpioType == GPIO_COMMAND_PWM)
				{
					mByteBuf.putShort((short)0x23);  // cmd length: 
					mByteBuf.put((byte)gpioNum);     // pin num
					mByteBuf.put((byte)0x04);			// pwm
					mByteBuf.put((byte)0x0);			// default 0
					mByteBuf.put((byte)0x01);			// polarity
					mByteBuf.put((byte)0x0);			// no pull
					mByteBuf.putInt((int)0x00320032); // pwm : duty:50% period: 50Hz
					mByteBuf.putShort((short)0);       // input high level
					mByteBuf.putShort((short)0);       // input low level 
					mByteBuf.putInt((int)0xffffffff);   //start time. 0xffffffff:current
					mByteBuf.putInt((int)0x0);			//delay time.  0: forever
					mByteBuf.putInt((int)0);			// event period. 0: no period
					mByteBuf.putInt((int)0);			//	period count 0: no period
					mByteBuf.putInt((int)0);			// next time     0: no period		
					mByteBuf.put((byte)0x01);           // event_pended_flag
					mByteBuf.put((byte)0x0);			// related gpio
					
				}
				position = mByteBuf.position();
				tempByteArray = new byte[position];
				mByteBuf.clear();
				mByteBuf.get(tempByteArray, 0, position);
				
			}
		break;
		case PHOTO_MODE:
			mByteBuf.clear();
			mByteBuf.putShort((short)0xBB); // BB 00 
			mByteBuf.put((byte)0x0c);       //CMD: c
			mByteBuf.put((byte)0);          // status: default 0
			{
				mByteBuf.putShort((short)0x23);  // cmd length: 
				mByteBuf.put((byte)0x09);     // camera
				mByteBuf.put((byte)0x00);			// default 0
				mByteBuf.put((byte)0x00);			// default 0
				mByteBuf.put((byte)0x00);			// none
				mByteBuf.put((byte)0x00);			// none
				mByteBuf.putInt((int)0x00000000); // none
				mByteBuf.putShort((short)0);       // none
				mByteBuf.putShort((short)0);       // none 
				mByteBuf.putInt((int)0xffffffff);   //start time. 0xffffffff:current
				mByteBuf.putInt((int)0x0);			//delay time. 0: forever
				mByteBuf.putInt((int)0);			// event period. 0: no period
				mByteBuf.putInt((int)1);			//	period count 1: one time
				mByteBuf.putInt((int)0);			// next time     0: no period		
				mByteBuf.put((byte)0x01);           // event_pended_flag
				mByteBuf.put((byte)0x0);			// related gpio
			}
			position = mByteBuf.position();
			tempByteArray = new byte[position];
			mByteBuf.clear();
			mByteBuf.get(tempByteArray, 0, position);
			break;
		default:
			tempByteArray = new byte[3];
			tempByteArray[0] = 1;
			break;
		}
		return tempByteArray;
	}
	
//	public class real_time_config{
//		public short switches;
//		public short white_led_brightness;
//		public short reg_led_r_pwm;
//		public short reg_led_g_pwm;
//		public short reg_led_b_pwm;
//		
//		public void real_time_config()
//		{
//			switches = 0;
//			white_led_brightness = 0;
//			reg_led_r_pwm = 0;
//			reg_led_g_pwm = 0;
//			reg_led_b_pwm = 0;
//		}
//		
//	
//		public short checkCRC()
//		{
//			int sum = 0;
//			sum += 0x00BB;
//			sum += 0x0009;
//			sum += 0x000a;
//			sum += switches;
//			sum += white_led_brightness;
//			sum += reg_led_r_pwm;
//			sum += reg_led_g_pwm;
//			sum += reg_led_b_pwm;
//			sum = (sum >> 16) + (sum & 0xffff);
//			sum += sum>>16;
//			sum = ~sum;
//			
//			return (short)sum;
//		}
//	}
	
//	public class night_lamp{
//		
//	}
	
//	public class anti_thief{
//			
//	}
	
//	public class music_lamp{
//		
//	}

//	public class voice_mail{
//		
//	}
	
//	public class sleep_assistant{
//		
//	}
	
//	public class photo{
//		
//	}
	
//	public class charge{
//		
//	}
}
