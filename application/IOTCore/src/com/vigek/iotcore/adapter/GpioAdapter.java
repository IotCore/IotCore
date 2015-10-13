package com.vigek.iotcore.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import com.vigek.iotcore.common.Log;
import com.vigek.iot.CommandManager;
import com.vigek.iotcore.R;
import com.vigek.iotcore.app.AppConfig;
import com.vigek.iotcore.app.AppContext;
import com.vigek.iotcore.bean.Deviceinfo;
import com.vigek.iotcore.bean.gpio;
import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;



public class GpioAdapter extends BaseAdapter{

	private Context context;
	private static List<gpio> debugGpoiList = new ArrayList<gpio>();
    private Deviceinfo mCurrentDevice;
    
	public GpioAdapter(Context context, Deviceinfo d) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.mCurrentDevice = d;
		debugGpoiList.clear();
		debugGpoiList.add(new gpio("GPIO1", 1));
		debugGpoiList.add(new gpio("GPIO2", 2));
		debugGpoiList.add(new gpio("GPIO3", 3));
		debugGpoiList.add(new gpio("GPIO4", 4));
		debugGpoiList.add(new gpio("GPIO5", 5));
		debugGpoiList.add(new gpio("GPIO6", 6));
		debugGpoiList.add(new gpio("GPIO7", 7));
		debugGpoiList.add(new gpio("GPIO8", 8));
	}
	public static GpioAdapter createPositionAdapter(Context context, Deviceinfo d) {
			return new GpioAdapter(context,d);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		int count=0;
		count = debugGpoiList.size();
		return count;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		
		if(debugGpoiList.size() == 0)
			return null;
		else
			return debugGpoiList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public  void removeItem( int arg0)
	{
		debugGpoiList.remove(arg0);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		// TODO Auto-generated method stub
		View v = null;
		TextView gpioV;
		Button gpioH;
		Button gpioL;
		Button gpioPWM;

		final String gpioName;
		final int    gpioNum;

		
		DeviceHolder holder;

		if( convertView == null ) {
			v = LayoutInflater.from(context).inflate(R.layout.gpio_list_view, null);
			holder = new DeviceHolder(v);
			v.setTag(holder);
		}
		else {
			v = convertView;
			holder = (DeviceHolder) v.getTag();
		}

		gpio g = debugGpoiList.get(position);
		
		gpioName = g.getGpio_name();
		gpioNum = g.getGpio_number();
		
		gpioV = holder.getGpioNameView();
		gpioV.setText(gpioName);
		
		gpioH = holder.getGpioHView();
		gpioH.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.v("set gpio"+ gpioNum + "of" + mCurrentDevice.getDeviceName()+ " high");
				AppContext.SendGPIOCommand(mCurrentDevice, gpioNum,CommandManager.GPIO_COMMAND_HIGH);
			}});
		
		gpioL = holder.getGpioLView();
		gpioL.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.v("set gpio"+ gpioNum + "of" + mCurrentDevice.getDeviceName()+ " low");
				AppContext.SendGPIOCommand(mCurrentDevice, gpioNum, CommandManager.GPIO_COMMAND_LOW);
				
			}});

		gpioPWM = holder.getGpioPWMView();
		gpioPWM.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.v("set gpio"+ gpioNum + "of" + mCurrentDevice.getDeviceName()+ " pwm");
				AppContext.SendGPIOCommand(mCurrentDevice, gpioNum, CommandManager.GPIO_COMMAND_PWM);			
				
			}});
		
		
		return v;
	}
	
	class DeviceHolder {
		View v;
		TextView gpioNameV;
		Button gpioHV;
		Button gpioLV;
		Button gpioPWMV;

		DeviceHolder(View v){
			this.v = v;
		}

		TextView getGpioNameView(){
			if (gpioNameV == null) {
				gpioNameV = (TextView)v.findViewById(R.id.gpio_name);
			}
			return gpioNameV;
		}
		
		Button getGpioHView(){
			if (gpioHV == null) {
				gpioHV = (Button)v.findViewById(R.id.gpio_high);
			}
			return gpioHV;
		}
		
		Button getGpioLView(){
			if (gpioLV == null) {
				gpioLV = (Button)v.findViewById(R.id.gpio_low);
			}
			return gpioLV;
		}
		
		Button getGpioPWMView(){
			if (gpioPWMV == null) {
				gpioPWMV = (Button)v.findViewById(R.id.gpio_pwm);
			}
			return gpioPWMV;
		}	

	}

}