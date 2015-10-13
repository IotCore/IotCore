package com.vigek.iotcore.fragment;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;
import com.vigek.iotcore.MainActivity;
import com.vigek.iotcore.R;
import com.vigek.iotcore.app.AppConfig;
import com.vigek.iotcore.app.AppContext;
import com.vigek.iotcore.bean.Deviceinfo;
import com.vigek.iotcore.bean.HMessage;
import com.vigek.iotcore.common.Log;
import com.vigek.iotcore.manager.HMessageListManager;
import com.vigek.iotcore.view.HackyViewPager;
import android.app.Activity;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import uk.co.senab.photoview.PhotoView;
import android.widget.Button;

public class fragment_camera extends Fragment  implements OnPageChangeListener{
	private Context mContext;
	private Deviceinfo mCurrentDevice;
	private TextView mDeviceNameView;
    private static HMessageListManager mHMessageListManager;
	private ViewPager mViewPager;
	private static int mCurrentMsgId;
	private static int mCurrentItem;
	private SamplePagerAdapter mPagerAdapter;
    PageIndicator mIndicator;
    private static final int APP_UPDATE_CONTENT = 0x1003;
    
	private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            
            switch (msg.what) {
            	case APP_UPDATE_CONTENT:
        			mPagerAdapter.notifyDataSetChanged();	
        			mViewPager.setCurrentItem(mPagerAdapter.getCount()-1,true);
        			break;
            	default:
            		break;
            }
            
        }
    };
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContext = getActivity().getApplicationContext();
		int sectionNumber = getArguments().getInt(MainActivity.ARG_SECTION_NUMBER);
		MainActivity.mCurrentFragment = sectionNumber;

		View rootView = (LinearLayout) inflater.inflate(R.layout.fragment_camera , container, false);
		mCurrentDevice = (Deviceinfo)getArguments().getParcelable(MainActivity.ARG_DEVICE);
		mHMessageListManager = HMessageListManager.getInstance(mContext);
        mHMessageListManager.RegisterObserverListener(mHMessageListDataSetObserver);
        
        mViewPager = (HackyViewPager)rootView.findViewById(R.id.view_pager);
//		setContentView(mViewPager);
        mPagerAdapter = new SamplePagerAdapter();
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(this);        

        mIndicator = (CirclePageIndicator)rootView.findViewById(R.id.indicator);
        mIndicator.setViewPager(mViewPager);
        
		mDeviceNameView = (TextView)rootView.findViewById(R.id.camera_device_name);
		
		if(mCurrentDevice!=null)
		{
			mDeviceNameView.setText(mCurrentDevice.getDeviceName());
		}	
		
		setHasOptionsMenu(true);
		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((MainActivity) activity).onSectionAttached(getArguments().getInt(MainActivity.ARG_SECTION_NUMBER));
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	        inflater.inflate(R.menu.camera, menu);
	        super.onCreateOptionsMenu(menu,inflater);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch(item.getItemId()){   
	    case  R.id.action_capture:
	    	Log.v("capture  ");
	    	AppContext.SendCaptureCommand(mCurrentDevice);
	        break;  

	    }
	    return super.onOptionsItemSelected(item);
	}
	
	private DataSetObserver mHMessageListDataSetObserver = new DataSetObserver() {

		@Override
		public void onChanged() {
			mHandler.sendEmptyMessageDelayed(APP_UPDATE_CONTENT, 0);
			super.onChanged();
			
		}

		@Override
		public void onInvalidated() {
			mHandler.sendEmptyMessageDelayed(APP_UPDATE_CONTENT, 0);
			super.onInvalidated();
		}

	};	    
	

	static class SamplePagerAdapter extends PagerAdapter {
	//	private static final int[] sDrawables = { R.drawable.wallpaper, R.drawable.wallpaper, R.drawable.wallpaper,
	//			R.drawable.wallpaper, R.drawable.wallpaper, R.drawable.wallpaper };
	
		@Override
		public int getCount() {
			int n = mHMessageListManager.getHMessageList().size();
	//        Log.v("ReviewActivity"," getCount" + n);
	        return n;
		}
	
		@Override
		public View instantiateItem(ViewGroup container, int position) {
			HMessage msg = mHMessageListManager.getHMessageList().get(position);
			String ImageUri = "file://"+ new String(msg.getPayload());
		
			PhotoView photoView = new PhotoView(container.getContext());
	//		photoView.setImageURI(Uri.parse(ImageUri));
			
			ImageLoader.getInstance()
			.displayImage(ImageUri, photoView, AppContext.getDisplayImageOptions(), new SimpleImageLoadingListener() {
				@Override
				public void onLoadingStarted(String imageUri, View view) {
				//	holder.progressBar.setProgress(0);
				//	holder.progressBar.setVisibility(View.VISIBLE);
				}
	
				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				//	holder.progressBar.setVisibility(View.GONE);
				}
	
				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				//	holder.progressBar.setVisibility(View.GONE);
				}
			}, new ImageLoadingProgressListener() {
				@Override
				public void onProgressUpdate(String imageUri, View view, int current, int total) {
			//		holder.progressBar.setProgress(Math.round(100.0f * current / total));
				}
			});				
	
			// Now just add PhotoView to ViewPager and return it
			container.addView(photoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			mCurrentItem = position;
			return photoView;
		}
		
	
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}
	
		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
	}


	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		
	}

}
