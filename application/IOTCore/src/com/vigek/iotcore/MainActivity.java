package com.vigek.iotcore;
import android.os.Parcel;
import android.os.Parcelable;
import com.vigek.iotcore.bean.Deviceinfo;
import com.vigek.iotcore.common.Log;
import com.vigek.iotcore.common.Notify;
import com.vigek.iotcore.fragment.NavigationDrawerFragment;
import com.vigek.iotcore.fragment.fragment_camera;
import com.vigek.iotcore.fragment.fragment_controller;
import com.vigek.iotcore.fragment.fragment_gpio;
import com.vigek.iotcore.fragment.fragment_mydevice;
import com.vigek.iotcore.fragment.fragment_settings;
import com.vigek.iotcore.manager.DeviceListManager;
import com.vigek.iotcore.manager.IDeviceController;
import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, 
	IDeviceController{

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;
    public static final String MyDeviceFragmentTag = "device";
    public static final String GpioFragmentTag = "gpio";
    public static final String ControllerFragmentTag = "controller";
    public static final String CameraFragmentTag = "camera";
    public static final String SettingFragmentTag = "setting";
    public static int mCurrentFragment = 0;
	public static final String ARG_SECTION_NUMBER = "section_number";
	public static final String ARG_DEVICE = "device";
	
	private Context mContext;
	private DeviceListManager mDeviceListManager = null;
	
    private boolean isExit = false;	
    private static final int  APP_IS_EXIT		= 0x1000;
    private static final int  APP_EXIT_CHANGED 	= 0x1002;

	private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            
            switch (msg.what) {
            	case APP_EXIT_CHANGED:
            		isExit=false;
            		break;
            	default:
            		break;
            }
            
        }
    };    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("onCreate");
		mContext = this.getApplicationContext();
		setContentView(R.layout.activity_main);
		mDeviceListManager = DeviceListManager.getInstance(mContext);
		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		Log.v("onNavigationDrawerItemSelected");
		// update the main content by replacing fragments
//		FragmentManager fragmentManager = getSupportFragmentManager();
//		fragmentManager.beginTransaction().replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
//				.commit();
		setCurrentFragment(position+1, null);
	}

	public void onSectionAttached(int number) {
		Log.v("onSectionAttached");
		switch (number) {
		case 1:
			mTitle = getString(R.string.title_section_device);
			break;
		case 4:
			mTitle = getString(R.string.title_section_controller);
			break;
		case 3:
			mTitle = getString(R.string.title_section_camera);
			break;
		case 2:
			mTitle = getString(R.string.title_section_settings);
			break;
		case 5:
			mTitle = getString(R.string.title_section_gpio);
			break;		
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.v("onCreateOptionMenu");
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.global, menu);
			onSectionAttached(mCurrentFragment);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.v("onOptionsItemSelected");
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    private void changeFragment(Fragment targetFragment, String tag){
//      resideMenu.clearIgnoredViewList();
	  	FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	  	ft.setCustomAnimations(R.anim.push_right_in,R.anim.push_right_out); 
	    ft.replace(R.id.container, targetFragment, tag);
	    ft.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
	    ft.addToBackStack(null);
	    ft.commit();
  }   
    
    public void setCurrentFragment(int num, Parcelable p)
    {	
		Bundle args = new Bundle();
		Fragment f = null;
    	switch(num)
    	{
    	case 5:
    		f = new fragment_gpio();
			args.putInt(ARG_SECTION_NUMBER, num);
			args.putParcelable(ARG_DEVICE, p);
			f.setArguments(args);    		
    		changeFragment(f, GpioFragmentTag);
    	    mCurrentFragment = 5;
    		break;

    	case 4:
    		f = new fragment_controller();
			args.putInt(ARG_SECTION_NUMBER, num);
			args.putParcelable(ARG_DEVICE, p);
			f.setArguments(args);    		
    		changeFragment(f, ControllerFragmentTag);
    	    mCurrentFragment = 4;
    		break;

    	case 3:
    		f = new fragment_camera();
			args.putInt(ARG_SECTION_NUMBER, num);
			args.putParcelable(ARG_DEVICE, p);
			f.setArguments(args);    		
    		changeFragment(f, CameraFragmentTag);
    	    mCurrentFragment = 3;

    		break;
    	case 2:
    		f = new fragment_settings();
			args.putInt(ARG_SECTION_NUMBER, num);
			args.putParcelable(ARG_DEVICE, p);
			f.setArguments(args);    		
    		changeFragment(f, CameraFragmentTag);
    	    mCurrentFragment = 2;
    		break;
    		
    	case 1:
    	default:
    		f = new fragment_mydevice();
			args.putInt(ARG_SECTION_NUMBER, num);
			args.putParcelable(ARG_DEVICE, p);
			f.setArguments(args);    		
    		changeFragment(f, MyDeviceFragmentTag);
    	    mCurrentFragment = 1;
    		break;
    	}
    }
//    /**
//	 * A placeholder fragment containing a simple view.
//	 */
//	public static class PlaceholderFragment extends Fragment {
//		/**
//		 * The fragment argument representing the section number for this
//		 * fragment.
//		 */
//		private static final String ARG_SECTION_NUMBER = "section_number";
//
//		/**
//		 * Returns a new instance of this fragment for the given section number.
//		 */
//		public static PlaceholderFragment newInstance(int sectionNumber) {
//			PlaceholderFragment fragment = new PlaceholderFragment();
//			Bundle args = new Bundle();
//			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//			fragment.setArguments(args);
//			return fragment;
//		}
//
//		public PlaceholderFragment() {
//		}
//
//		@Override
//		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//			int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
//			View rootView = createSectionView(inflater, container, savedInstanceState, sectionNumber);
//			return rootView;
//		}
//
//		@Override
//		public void onAttach(Activity activity) {
//			super.onAttach(activity);
//			((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
//		}
//		
//		public View CreateMyDeviceSectionView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
//		{
//			View rootView  = (LinearLayout) inflater.inflate(R.layout.fragment_mydevice , container, false);
//			return rootView;
//			
//		}
//		
//		public View CreateGpioControllerView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
//		{
//			View rootView  = (LinearLayout) inflater.inflate(R.layout.fragment_gpiocontroller , container, false);
//			return rootView;
//		}
//		
//		public View CreateCameraControllerView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
//		{
//			View rootView  = (LinearLayout) inflater.inflate(R.layout.fragment_camera , container, false);
//			return rootView;
//			
//		}
//		
//		public View createSectionView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState, int number)
//		{
//			View rootView  = null;
//			switch(number)
//			{
//			case 3:
//				rootView = CreateCameraControllerView(inflater, container, savedInstanceState);
//				break;
//			case 2:
//				rootView = CreateGpioControllerView(inflater, container, savedInstanceState);
//				break;
//			case 1:
//			default:
//				rootView = CreateMyDeviceSectionView(inflater, container, savedInstanceState);
//				break;
//			}
//			
//			return rootView;
//		}
//	}

	@Override
	public void onDeviceSelcted(int type, Deviceinfo d) {
		// TODO Auto-generated method stub
		switch(type)
		{
		case IDeviceController.CONTROLLER_TYPE_ALL:
			setCurrentFragment(4,d);
			break;		
		case IDeviceController.CONTROLLER_TYPE_GPIO:
			setCurrentFragment(5,d);
			break;
		case IDeviceController.CONTROLLER_TYPE_CAMERA:		
			setCurrentFragment(3,d);
			break;
		default:
			break;

		}
	}
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
		super.onActivityResult(requestCode, resultCode, intent);
   	
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {  
        if(keyCode == KeyEvent.KEYCODE_BACK){
        	if(mCurrentFragment<=1){
	            if(isExit==false){
	                isExit=true;
	                Resources resource = (Resources) getBaseContext().getResources();
	                String exit=resource.getString(R.string.again_exit);                
	                Notify.toast(mContext, exit, Toast.LENGTH_SHORT);
	                mHandler.sendEmptyMessageDelayed(APP_EXIT_CHANGED, 3000);
	                return true;
	        	}	
	            else
	            {
	            	finish();
	            	return false;
	        	}
        	}
        	
        }

       return super.onKeyDown(keyCode, event);

    }
}
