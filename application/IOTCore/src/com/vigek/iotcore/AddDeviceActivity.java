package com.vigek.iotcore;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.example.config.SCCtlOps;
import com.example.config.User_configuration;
import com.example.config.configurationListener;
import com.google.zxing.WriterException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity;

//import com.realtek.simpleconfig.SCCtlOps;
import com.realtek.simpleconfiglib.SCLibrary;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;
import com.vigek.iotcore.app.AppConfig;
import com.vigek.iotcore.bean.Deviceinfo;
import com.vigek.iotcore.common.AsyncHandler;
import com.vigek.iotcore.common.FileOps;
import com.vigek.iotcore.common.Log;
import com.vigek.iotcore.common.Notify;
import com.vigek.iotcore.common.StringUtils;
import com.vigek.iotcore.fragment.fragment_mydevice;
import com.vigek.iotcore.manager.DeviceListManager;
import com.vigek.iotcore.view.ChangingAwareEditText;
import com.wifi.connecter.Wifi;

public class AddDeviceActivity extends ActionBarActivity implements
		OnClickListener {
	private Context mContext;
	private DeviceListManager mDeviceListManager;
	private static final int QRCAPTURE_REQUEST_CODE = 1;
	private static final int SIMPLECONFIG_REQUEST_CODE = 2;
	private static final int POSITION_REQUEST_CODE = 3;
	private static final String TAG = "AddDeviceActivity";
	private static String WifiConnectStat = "";
	
	private LinearLayout ll_network_info;
	private LinearLayout ll_device_info;
	private TextView     txt_config_result;
	private EditText devIDEv, devNameEv;
	private ChangingAwareEditText pwdEv;
	private Spinner pwdSn;
	private MyScanResult choosedScanResult;
	private AlertDialog CfgResultDialog;
	// private TextView qrImgImage;
	private TextView scan_wifi;
	private TextView check_password;
	private Button start_config;
	private TextView configResult;
	private ProgressBar pgBar;
	private WifiManager wifiMngr;
	private WifiInfo wifiInfo;

	private BaseAdapter wifiListItemAdapter;

	private List<HashMap<String, Object>> InfoList = new ArrayList<HashMap<String, Object>>();

	private User_configuration con;
	private FileOps fileOps = new FileOps();
	private static List<MyScanResult> mScanResults = new ArrayList<MyScanResult>();
	private ProgressDialog cfgProgressDialog;
	private boolean ResultShowable = true;
	private boolean isChecked = false;

	private TextView wifi_security;
	private AddDeviceActivity mActivity;
	private boolean mReceiverRegistered = false;

	private static final int GET_IP_ERROR = 0x100;
	private static final int GET_IP_SUCCESS = 0x101;
	private static final int WIFI_DISCONNECTED = 0x102;
	private static final int PASSWORD_ERROR = 0x103;

	// WIFI氓鈥郝久р�扳��
	private static final int[] SIGNAL_LEVEL_LOCK = {
			R.drawable.ic_wifi_signal_1, R.drawable.ic_wifi_signal_2,
			R.drawable.ic_wifi_signal_3, R.drawable.ic_wifi_signal_4 };

	private static final int[] SIGNAL_LEVEL_OPEN = {
			R.drawable.ic_wifi_lock_signal_1, R.drawable.ic_wifi_lock_signal_2,
			R.drawable.ic_wifi_lock_signal_3, R.drawable.ic_wifi_lock_signal_4 };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.activity_adddevice);
		mContext = this.getApplicationContext();
		mActivity = this;
		
		mDeviceListManager = DeviceListManager.getInstance(mContext);
        //devIDEv拢篓device_Sn拢漏; devNameEv(device_name); pwsEv(WIFI脙脺脗毛拢漏拢禄pwdSn拢篓WIFI-Spinner)
		
		ll_network_info=(LinearLayout)findViewById(R.id.network_info);
		ll_device_info = (LinearLayout)findViewById(R.id.device_info);
		txt_config_result = (TextView)findViewById(R.id.config_result);
		
		devIDEv = (EditText) findViewById(R.id.input_serial_num);
		devNameEv = (EditText) findViewById(R.id.devNameInput);
		
		pwdEv = (ChangingAwareEditText) findViewById(R.id.input_wifi_code);
		pwdSn = (Spinner) findViewById(R.id.networkInput);
		// qrImgImage = (TextView) findViewById(R.id.bar_scan);
		scan_wifi = (TextView) findViewById(R.id.FiWi_scan);
		wifi_security = (TextView) findViewById(R.id.wifi_security);
		check_password = (TextView) findViewById(R.id.wifi_edit);
		start_config = (Button) findViewById(R.id.start_config);

		configResult = (TextView) findViewById(R.id.config_ing);
		pgBar = (ProgressBar) findViewById(R.id.progress);

		wifiListItemAdapter = new listBaseAdapter(this);
		cfgProgressDialog = new ProgressDialog(this);

		// SCLib.rtk_sc_init();
		// SCLib.TreadMsgHandler = new MsgHandler();
		// SCLib.WifiInit(this);

		con = new User_configuration(this, new configurationListener() {

			@Override
			public void onFailure() {
				// TODO Auto-generated method stub
				pgBar.setVisibility(View.INVISIBLE);
				pwdSn.setEnabled(true);
				pwdEv.setEnabled(true);
				configResult.setText(R.string.start_config_status);
				start_config.setText(R.string.start_config);
				txt_config_result.setText(R.string.step3);
				devIDEv.setText("");
				devNameEv.setText("");
				ll_device_info.setVisibility(View.VISIBLE);
				
				start_config.setOnClickListener(start);
			}

			@Override
			public void onSuccuss() {
				// TODO Auto-generated method stub
				InfoList = con.getInfo();
				String currentDeviceID = InfoList.get(0).get("Name").toString();
				start_config.setText(R.string.add_config);
				start_config.setOnClickListener(add);
				pgBar.setVisibility(View.INVISIBLE);
				devIDEv.setText(currentDeviceID);
				String name = mContext.getString(R.string.app_name) +" "+ new String(currentDeviceID).substring(currentDeviceID.length()-8);
				devNameEv.setText(name);
				configResult.setText(R.string.success_config_status);
				txt_config_result.setText(R.string.step1);
				ll_device_info.setVisibility(View.VISIBLE);
//				start_config.performClick();
			}

		});

		wifiMngr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiInfo = wifiMngr.getConnectionInfo();

		fileOps.SetKey(con.getMac());
		fileOps.UpgradeSsidPasswdFile();

		pwdSn.setAdapter(wifiListItemAdapter);
		pwdSn.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				Log.i(TAG, "OnItemSelectedListener" + position + "" + id);
				Log.i(TAG, "OnItemSelectedListener" + parent.getSelectedView());
				choosedScanResult = mScanResults.get(position);

				// launchWifiConnecter(AddDeviceActivity.this,
				// choosedScanResult.getS());

				if (choosedScanResult.getS() != null) {
					fileOps.ParseSsidPasswdFile(choosedScanResult.getS().SSID);
					String ScanResultSecurity = Wifi.ConfigSec
							.getScanResultSecurity(choosedScanResult.getS());
					boolean IsOpenNetwork = Wifi.ConfigSec
							.isOpenNetwork(ScanResultSecurity);

					final String rawSecurity = Wifi.ConfigSec
							.getDisplaySecirityString(choosedScanResult.getS());
					final String readableSecurity = IsOpenNetwork ? getString(R.string.wifi_security_open)
							: rawSecurity;
					wifi_security.setText(readableSecurity);

					if (IsOpenNetwork) {
						pwdEv.setEnabled(false);
						pwdEv.setText("");
						pwdEv.setHint(R.string.wifi_security_open);
						SCCtlOps.IsOpenNetwork = true;

					} else {
						pwdEv.setEnabled(true);
						pwdEv.setText(SCCtlOps.StoredPasswd);
						pwdEv.setHint(R.string.please_type_passphrase);
						SCCtlOps.IsOpenNetwork = false;
					}
					pwdEv.setChanged(false);

					SCCtlOps.ConnectedSSID = choosedScanResult.getS().SSID;
					SCCtlOps.ConnectedBSSID = choosedScanResult.getS().BSSID;
					Log.i(TAG, "SCCtlOps.ConnectedSSID = "
							+ SCCtlOps.ConnectedSSID);
					Log.i(TAG, "SCCtlOps.ConnectedBSSID = "
							+ SCCtlOps.ConnectedBSSID);
				}
				if (view != null) {
					view.findViewById(R.id.connected).setVisibility(View.GONE);
					view.findViewById(R.id.signal).setVisibility(View.GONE);
					view.setBackgroundColor(getResources().getColor(
							R.color.transparent));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				Log.i(TAG, "noting selected....");
			}

		});

		scan_wifi.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				wifiMngr.startScan();
				// SCLib.WifiStartScan();
				GetAllWifiList();
			}

		});

		start_config.setOnClickListener(prepared);
		check_password.setOnClickListener(this);

	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mContext.getString(R.string.add_device));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.v("onCreateOptionMenu");
		// Only show items in the action bar relevant to this screen
		// if the drawer is not showing. Otherwise, let the drawer
		// decide what to show in the action bar.
		getMenuInflater().inflate(R.menu.adddevice, menu);
		restoreActionBar();
		return true;
	}
   //脡篓脙猫露镁脦卢脗毛
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
		if (id == R.id.action_qrscan) {
			Intent openCameraIntent = new Intent(AddDeviceActivity.this,
					CaptureActivity.class);
			startActivityForResult(openCameraIntent, QRCAPTURE_REQUEST_CODE);
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("deprecation")
	public void StartConfigPopUp() {
		cfgProgressDialog.setTitle("Wi-Fi: " + con.getSSID());
		cfgProgressDialog.setMessage("    Configuring......");
		cfgProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		cfgProgressDialog.setCancelable(false);
		cfgProgressDialog.setButton("Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						con.StopDiscovery();
						ShowResultPopUp();
						dialog.cancel();
					}
				});
		cfgProgressDialog.show();
	}

	public void ShowResultPopUp() {
		final int num;
		final List<String> MacList = new ArrayList<String>();

		num = con.getDevNum();
		if (num == 0) {
			MacList.add("None");
		} else {
			InfoList = con.getInfo();
			for (int i = 0; i < num; i++) {
				if (InfoList.get(i).get("Name") == null) {
					MacList.add((String) InfoList.get(i).get("MAC"));
				} else {
					MacList.add((String) InfoList.get(i).get("Name"));
				}
			}
		}

		AlertDialog.Builder CfgBuilder = new AlertDialog.Builder(this);
		CfgBuilder.setTitle("Client list:")
				.setIcon(R.drawable.pick_from_camera).setCancelable(false)
				.setItems(MacList.toArray(new String[MacList.size()]), null);

		if (num > 0) {
			CfgBuilder.setPositiveButton("Rename",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							con.StopDiscovery();
							;
							String PINSet = devIDEv.getText().toString();
							if (PINSet == null) {
								Log.d(TAG, "Null PIN");
								Toast.makeText(AddDeviceActivity.this,
										"Null PIN", Toast.LENGTH_LONG).show();
								return;
							}
							if (PINSet.length() == 0) {
								Log.d(TAG, "No PIN, can not rename.");
								Toast.makeText(AddDeviceActivity.this,
										"No PIN, can not rename.",
										Toast.LENGTH_LONG).show();
								return;
							}

							List<HashMap<String, Object>> InfoReGet = new ArrayList<HashMap<String, Object>>();
							InfoReGet = con.getInfo();
							String ip = InfoReGet.get(0).get("IP").toString();
							String name = MacList.get(0).toString();

							dialog.cancel();
						}
					});
		}

		CfgBuilder.setNegativeButton("Finish",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ResultShowable = true;
						dialog.cancel();
					}
				});

		CfgResultDialog = CfgBuilder.show();
	}

	private String getIPAddress(int address) {
		StringBuilder sb = new StringBuilder();
		sb.append(address & 0x000000FF).append(".")
				.append((address & 0x0000FF00) >> 8).append(".")
				.append((address & 0x00FF0000) >> 16).append(".")
				.append((address & 0xFF000000L) >> 24);
		return sb.toString();
	}

	public int WifiGetIpInt() {

		int connect_count = 30;
		int wifiIP = con.getIp();
		while (connect_count > 0
				&& wifiIP == 0
				&& start_config.getText().toString()
						.equals(getString(R.string.stop_config))) {
			wifiIP = con.getIp();
			try {
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Log.v(TAG, "Allocating IP.....");
			connect_count--;
		}
		if ((wifiIP == 0) && (connect_count == 0)) {
			Log.e(TAG, "Failed to Allocated IP!");
			Toast.makeText(
					AddDeviceActivity.this,
					getString(R.string.password_error,
							choosedScanResult.getS().SSID), Toast.LENGTH_SHORT)
					.show();
		} else {
			Log.e(TAG, "Allocated IP : " + getIPAddress(wifiIP));
		}

		return wifiIP;

	}

	// 脜盲脰脙鹿媒鲁脤
	public void StartConfig() {
		Log.v(TAG, "1 - StartConfig ");
		boolean noSendSsid = false;
		SCLibrary.TotalConfigTimeMs = 120000;
		SCLibrary.OldModeConfigTimeMs = 120000;
		SCLibrary.ProfileSendRounds = 1;
		SCLibrary.ProfileSendTimeIntervalMs = 400;
		SCLibrary.PacketSendTimeIntervalMs = 0;
		// Exception action
		if ((Build.MANUFACTURER.equalsIgnoreCase("Samsung"))
				&& (Build.MODEL.equalsIgnoreCase("G9008"))) {
			SCLibrary.PacketSendTimeIntervalMs = 10; // 10ms
		}

		SCLibrary.EachPacketSendCounts = 1;
        //脜脨露脧脢脟鹿禄脫脨wife SSID麓忙脭脷
		if (SCCtlOps.ConnectedSSID == null) {
			Log.e(TAG, "Please select a Wi-Fi Network First");
			Toast.makeText(AddDeviceActivity.this,
					getString(R.string.please_select_wifi), Toast.LENGTH_SHORT)
					.show();
			return;
		}

		if (WifiGetIpInt() == 0) {
			return;
		}
		Log.v(TAG, "3 - StartConfig ");

		con.ReSet();
		;
		String PINSet = null;// devIDEv.getText().toString();
		if (PINSet == null)
			con.setDefaultPIN("57289961");
		con.setPIN(PINSet);
		
		con.setSSID(SCCtlOps.ConnectedSSID);
	
		con.setBSSID(SCCtlOps.ConnectedBSSID);

		Log.i(TAG, "4 -SCCtlOps.IsOpenNetwork=" + SCCtlOps.IsOpenNetwork);
		Log.i(TAG, "4 -SCCtlOps.ConnectedSSID=" + SCCtlOps.ConnectedSSID);
		Log.i(TAG, "4 -SCCtlOps.ConnectedBSSID=" + SCCtlOps.ConnectedBSSID);
		Log.i(TAG, "4 -SCCtlOps.ConnectedPasswd=" + SCCtlOps.ConnectedPasswd);
		//脠路卤拢脥酶脗莽脕卢脡脧拢卢驴陋脢录con.start拢篓拢漏脜盲脰脙碌脛
		if (!SCCtlOps.IsOpenNetwork) {
			if (SCCtlOps.ConnectedPasswd == null
					|| SCCtlOps.ConnectedPasswd.equals("")) {
				Log.e(TAG, "Please Enter Password");
				Toast.makeText(AddDeviceActivity.this, "Please Enter Password",
						Toast.LENGTH_SHORT).show();
				return;
			}
			con.setPASSWORD(SCCtlOps.ConnectedPasswd);
		} else {
			con.setPASSWORD("");
		}

		con.start();
		;

	}

	private Runnable simpleconfig = new Runnable() {

		@Override
		public void run() {
			if ((choosedScanResult != null)
					&& (choosedScanResult.getS() != null)) {
				if (Connect_to_newNet(choosedScanResult)) {
					StartConfig();
				} else {
					// Toast.makeText(AddDeviceActivity.this,
					// "password error, please check it!",
					// Toast.LENGTH_SHORT).show();
					Message msg = con.getMsgHandler().obtainMessage(
							GET_IP_ERROR);
					con.getMsgHandler().sendMessage(msg);
					return;
				}
			}

		}
	};
	
	private OnClickListener prepared = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			ll_network_info.setVisibility(View.VISIBLE);
			start_config.setText(getString(R.string.start_config));
			start_config.setOnClickListener(start);
		}
		
	};
	
	// start_config button 
	private OnClickListener start = new OnClickListener() {

		@Override
		public void onClick(View v) {
//
//			final String ID = devIDEv.getText().toString();
//			String name = devNameEv.getText().toString();
//			if (ID == null || ID.equals("")) {
//				Toast.makeText(AddDeviceActivity.this,
//						mContext.getString(R.string.product_key_check),
//						Toast.LENGTH_SHORT).show();
//				return;
//			}
//			if (name == null || name.equals("")) {
//				Toast.makeText(AddDeviceActivity.this,
//						mContext.getString(R.string.product_name_check),
//						Toast.LENGTH_SHORT).show();
//				return;
//			}
			// TODO Auto-generated method stub
			if (con.getWiFiStatus() != WifiManager.WIFI_STATE_ENABLED) {
				if (mReceiverRegistered == false) {
					mReceiverRegistered = true;
					final IntentFilter filter = new IntentFilter();
					filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
					filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
					filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
					mActivity.registerReceiver(mReceiver, filter);
				}
				// Toast.makeText(SCTest.this, "Wi-Fi is not enabled",
				// Toast.LENGTH_SHORT).show();
				mScanResults.clear();
				mScanResults.add(new MyScanResult(MyScanResult.NONE, null));
				wifiListItemAdapter.notifyDataSetChanged();
				OpenWifiPopUp();
			} else {
				if (mReceiverRegistered == true) {
					mReceiverRegistered = false;
					mActivity.unregisterReceiver(mReceiver);
				}
				// if(StringUtils.isEmpty(devIDEv.getText().toString()))
				// {
				// Toast.makeText(AddDeviceActivity.this,
				// "product key can not be empty!",
				// Toast.LENGTH_SHORT).show();
				// return ;
				// }
				// if(StringUtils.isEmpty(devNameEv.getText().toString()))
				// {
				// Toast.makeText(AddDeviceActivity.this,
				// "product key can not be empty!",
				// Toast.LENGTH_SHORT).show();
				// return ;
				// }
				// 脜脨露脧network脢脟路帽脭脷enadble
				String ScanResultSecurity = Wifi.ConfigSec
						.getScanResultSecurity(choosedScanResult.getS());
				boolean IsOpenNetwork = Wifi.ConfigSec
						.isOpenNetwork(ScanResultSecurity);
				if ((IsOpenNetwork == false)
						&& (StringUtils.isEmpty(pwdEv.getText().toString()))) {
					Toast.makeText(AddDeviceActivity.this,
							"please input the wifi password!",
							Toast.LENGTH_SHORT).show();
					return;
				}
				pgBar.setVisibility(View.VISIBLE);
				pwdSn.setEnabled(false);
				pwdEv.setEnabled(false);

				start_config.setText(R.string.stop_config);
				start_config.setOnClickListener(cancel);
				configResult.setText(R.string.cancel_config_status);
				// 脪矛虏陆脰麓脨脨simpleconfig
				AsyncHandler.post(simpleconfig);
			}

		}
	};

	// start_config button 碌茫禄梅隆卤cancle"碌脛戮脵露炉
	private OnClickListener cancel = new OnClickListener() {
		@Override
		public void onClick(View v) {

			// stop configuration
			con.StopDiscovery();
			start_config.setText(R.string.start_config);
			start_config.setOnClickListener(start);
			pgBar.setVisibility(View.INVISIBLE);
			pwdSn.setEnabled(true);
			pwdEv.setEnabled(true);
			configResult.setText(R.string.start_config_status);
		}
	};

	// 脜盲脰脙鲁脡鹿娄潞贸拢卢碌茫禄梅"add"脤铆录脫脡猫卤赂
	private OnClickListener add = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub

			final String ID = devIDEv.getText().toString();
			String name = devNameEv.getText().toString();
			if (ID == null || ID.equals("")) {
				Toast.makeText(AddDeviceActivity.this,
						mContext.getString(R.string.product_key_check),
						Toast.LENGTH_SHORT).show();
				return;
			}
			if (name == null || name.equals("")) {
				Toast.makeText(AddDeviceActivity.this,
						mContext.getString(R.string.product_name_check),
						Toast.LENGTH_SHORT).show();
				return;
			}

			Deviceinfo d = mDeviceListManager.getDeviceBySn(ID);
			if (d != null) {
				if (d.getDeviceName().equals(name)) {
					Notify.toast(mContext,
							mContext.getString(R.string.device_already_exist),
							2);
				} else {
					d.setDeviceName(name);
				}
			} else {
				d = new Deviceinfo(name, ID, name, ID,
						DeviceListManager.DEVICE_TYPE_SMOKE_ALARM);
			}
			// 掳脩device脥篓鹿媒intent麓芦碌脻碌陆fragment_mydevice
			Intent intent = new Intent(mContext, fragment_mydevice.class);
			intent.putExtra(AppConfig.config_device, d);
			setResult(RESULT_OK, intent);
			finish();
		}
	};

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.wifi_edit:
			togglePassword();
			break;
		default:
			break;
		}
	}

	private void togglePassword() {
		if (isChecked) {
			isChecked = false;
			// check_password.setIcon("fa-eye-slash");
		} else {
			isChecked = true;
			// check_password.setIcon("fa-eye");
		}
		pwdEv.setInputType(InputType.TYPE_CLASS_TEXT
				| (isChecked ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
						: InputType.TYPE_TEXT_VARIATION_PASSWORD));

	}

	private boolean Connect_to_newNet(MyScanResult ms) {
		boolean connResult = false;
		Log.v(TAG, "0 - Connect_to_newNet  start");

		String ScanResultSecurity = "";
		ScanResultSecurity = Wifi.ConfigSec.getScanResultSecurity(ms.getS());
		final WifiConfiguration config = Wifi.getWifiConfiguration(wifiMngr,
				ms.getS(), ScanResultSecurity);
		String passwd = pwdEv.getText().toString();

		if (config == null) {
			Log.i(TAG, "1- Connect_to_newNet  null");
			boolean open = Wifi.ConfigSec.isOpenNetwork(ScanResultSecurity);

			if (open) {
				SCCtlOps.IsOpenNetwork = true;
				connResult = Wifi.connectToNewNetwork(this, wifiMngr,
						ms.getS(), null, 10);
			} else {
				SCCtlOps.IsOpenNetwork = false;
				connResult = Wifi.connectToNewNetwork(this, wifiMngr,
						ms.getS(), passwd, 10);
			}
		} else {
			final boolean isCurrentNetwork_ConfigurationStatus = config.status == WifiConfiguration.Status.CURRENT;
			final WifiInfo info = wifiMngr.getConnectionInfo();
			final boolean isCurrentNetwork_WifiInfo = info != null
					&& ((android.text.TextUtils.equals(info.getSSID(),
							ms.getS().SSID)) || (android.text.TextUtils.equals(
							info.getSSID(), new String("\"" + ms.getS().SSID
									+ "\""))))	
					&& android.text.TextUtils.equals(info.getBSSID(),
							ms.getS().BSSID);
			if (isCurrentNetwork_ConfigurationStatus
					|| isCurrentNetwork_WifiInfo) {
				if (pwdEv.getChanged()) {
					// pwdEv.setChanged(false);
					Log.v(TAG,
							"2 - Connect_to_newNet changePasswordAndConnect to"
									+ config.SSID + " " + passwd);
					connResult = Wifi.changePasswordAndConnect(this, wifiMngr,
							config, passwd, 10);

				} else {
					Log.v(TAG, "2 - Connect_to_newNet password not changed"
							+ " " + passwd);
					connResult = true;
				}
			} else {
				connResult = wifiMngr.removeNetwork(config.networkId);

				Log.v("WifiConfiguration", " remove connResult=" + connResult);
				connResult = wifiMngr.saveConfiguration();

				ScanResultSecurity = Wifi.ConfigSec.getScanResultSecurity(ms
						.getS());
				boolean open = Wifi.ConfigSec.isOpenNetwork(ScanResultSecurity);

				WifiConfiguration _config = Wifi.getWifiConfiguration(wifiMngr,
						ms.getS(), ScanResultSecurity);
				if (_config == null) {
					Log.i("WifiConfiguration", "_config" + _config);
					if (open) {
						SCCtlOps.IsOpenNetwork = true;
						connResult = Wifi.connectToNewNetwork(this, wifiMngr,
								ms.getS(), null, 10);
					} else {
						SCCtlOps.IsOpenNetwork = false;
						connResult = Wifi.connectToNewNetwork(this, wifiMngr,
								ms.getS(), passwd, 10);
					}
				} else {
					Log.v(TAG,
							" 3 Connect_to_newNet connect to configured network ... "
									+ _config.SSID);
					connResult = Wifi.connectToConfiguredNetwork(this,
							wifiMngr, _config, false);
				}
				//
				// ScanResultSecurity =
				// Wifi.ConfigSec.getScanResultSecurity(ms.getS());
				// WifiConfiguration __config =
				// Wifi.getWifiConfiguration(wifiMngr, ms.getS(),
				// ScanResultSecurity);
				// if(__config==null)
				// {
				// connResult = false;
				// }
			}

		}

		if (WifiGetIpInt() == 0) {
			connResult = false;
		} else {
			final WifiInfo info = wifiMngr.getConnectionInfo();
			final boolean isCurrentNetwork_WifiInfo = info != null
					&& ((android.text.TextUtils.equals(info.getSSID(),
							ms.getS().SSID)) || (android.text.TextUtils.equals(
							info.getSSID(), new String("\"" + ms.getS().SSID
									+ "\""))))
					&& android.text.TextUtils.equals(info.getBSSID(),
							ms.getS().BSSID);
			if (isCurrentNetwork_WifiInfo) {
				Log.v(TAG, " 4 Connect_to_newNet WifiGetIpInt success ... ");
				connResult = true;
				SCCtlOps.ConnectedSSID = ms.getS().SSID;
				SCCtlOps.ConnectedBSSID = ms.getS().BSSID;
				SCCtlOps.ConnectedPasswd = new String(passwd); // Store password
				fileOps.UpdateSsidPasswdFile(); // connect successful, update
												// file
				Message msg = con.getMsgHandler().obtainMessage(GET_IP_SUCCESS);
				con.getMsgHandler().sendMessage(msg);
			} else {
				connResult = false;
				Log.v(TAG, " 4 Connect_to_newNet WifiGetIpInt failed ... ");
				Message msg = con.getMsgHandler().obtainMessage(GET_IP_ERROR);
				con.getMsgHandler().sendMessage(msg);
			}
		}

		return connResult;

	}

	private WifiConfiguration IsExsits(String SSID) {
		// wifiMngr = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		List<WifiConfiguration> existingConfigs = wifiMngr
				.getConfiguredNetworks();
		for (WifiConfiguration existingConfig : existingConfigs) {
			if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
				return existingConfig;
			}
		}
		return null;
	}

	public enum WifiCipherType {
		WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
	}

	private WifiConfiguration CreateWifiInfo(String SSID, String Password,
			WifiCipherType Type) {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";
		if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
			config.wepKeys[0] = "";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (Type == WifiCipherType.WIFICIPHER_WEP) {
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (Type == WifiCipherType.WIFICIPHER_WPA) {
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.status = WifiConfiguration.Status.ENABLED;
		} else {
			return null;
		}
		return config;
	}

	/** Handler class to receive send/receive message */
//	private class MsgHandler extends Handler {
//		byte ret;
//
//		@Override
//		public void handleMessage(Message msg) {
//			Log.d(TAG, "msg.what: " + msg.what);
//			switch (msg.what) {
//			case PASSWORD_ERROR:
//				Toast.makeText(
//						AddDeviceActivity.this,
//						getString(R.string.password_error,
//								choosedScanResult.getS().SSID),
//						Toast.LENGTH_SHORT).show();
//				con.StopDiscovery();
//				pgBar.setVisibility(View.INVISIBLE);
//				pwdSn.setEnabled(true);
//				pwdEv.setEnabled(true);
//				configResult.setText(R.string.start_config_status);
//				start_config.setText(R.string.start_config);
//				start_config.setOnClickListener(start);
//				break;
//			case GET_IP_ERROR:
//				Toast.makeText(
//						AddDeviceActivity.this,
//						getString(R.string.failed_connect_to,
//								choosedScanResult.getS().SSID),
//						Toast.LENGTH_SHORT).show();
//				con.StopDiscovery();
//				pgBar.setVisibility(View.INVISIBLE);
//				pwdSn.setEnabled(true);
//				pwdEv.setEnabled(true);
//				configResult.setText(R.string.start_config_status);
//				start_config.setText(R.string.start_config);
//				start_config.setOnClickListener(start);
//				break;
//			case GET_IP_SUCCESS:
//				Toast.makeText(
//						AddDeviceActivity.this,
//						getString(R.string.wifi_connect_to,
//								choosedScanResult.getS().SSID),
//						Toast.LENGTH_SHORT).show();
//				break;
//			case WIFI_DISCONNECTED:
//				con.StopDiscovery();
//				pgBar.setVisibility(View.INVISIBLE);
//				pwdSn.setEnabled(true);
//				pwdEv.setEnabled(true);
//				configResult.setText(R.string.start_config_status);
//				start_config.setText(R.string.start_config);
//				start_config.setOnClickListener(start);
//				break;
//			case ~SCCtlOps.Flag.CfgSuccessACK:
//				// if(cfgProgressDialog.isShowing()) {
//				// Toast.makeText(AddDeviceActivity.this, "Config Timeout",
//				// Toast.LENGTH_LONG).show();
//				// cfgProgressDialog.dismiss(); // re-operable
//				// }
//				Toast.makeText(
//						AddDeviceActivity.this,
//						getString(R.string.failed_connect_to,
//								choosedScanResult.getS().SSID),
//						Toast.LENGTH_SHORT).show();
//				con.StopDiscovery();
//				pgBar.setVisibility(View.INVISIBLE);
//				pwdSn.setEnabled(true);
//				pwdEv.setEnabled(true);
//				configResult.setText(R.string.start_config_status);
//				start_config.setText(R.string.start_config);
//				start_config.setOnClickListener(start);
//				break;
//			case SCCtlOps.Flag.CfgSuccessACK:
//				// if(!ResultShowable) {
//				// Log.d(TAG, "Not Showable");
//				// break;
//				// }
//				// if(cfgProgressDialog.isShowing()) {
//				// cfgProgressDialog.dismiss();
//				// }
//				con.StopDiscovery();
//				InfoList = con.getInfo();
//				start_config.setText(R.string.add_config);
//				start_config.setOnClickListener(add);
//				pgBar.setVisibility(View.INVISIBLE);
//				configResult.setText(R.string.success_config_status);
////				start_config.performClick();
//				// if(isActivityAlive) {
//				// ShowResultPopUp();//
//				// Toast.makeText(AddDeviceActivity.this,
//				// "simple config succussful",
//				// Toast.LENGTH_SHORT).show();
//				// }
//
//				break;
//			default:
//				break;
//			}
//		}
//	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onRestart() {
		super.onRestart();
	}

	private void checkWifi() {

		if (con.getWiFiStatus() == WifiManager.WIFI_STATE_DISABLED) {
			// Toast.makeText(SCTest.this, "Wi-Fi is not enabled",
			// Toast.LENGTH_SHORT).show();
			if (mReceiverRegistered == false) {
				mReceiverRegistered = true;
				final IntentFilter filter = new IntentFilter();
				filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
				filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
				filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
				mActivity.registerReceiver(mReceiver, filter);
			}

			mScanResults.clear();
			mScanResults.add(new MyScanResult(MyScanResult.NONE, null));
			wifiListItemAdapter.notifyDataSetChanged();
			OpenWifiPopUp(); //
		} else {
			wifiMngr.startScan();
			GetAllWifiList();// mScanResults = mWifiManager.getScanResults();
		}
	}

	@Override
	public void onResume() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();

		wifiInfo = wifiMngr.getConnectionInfo();
		if (info != null) {
			SCCtlOps.ConnectedSSID = wifiInfo.getSSID();
			SCCtlOps.ConnectedBSSID = wifiInfo.getBSSID();
			Log.i(TAG, "onResume().ConnectedSSID=" + SCCtlOps.ConnectedSSID);
		}

		checkWifi();

		super.onResume();
	}

	@Override
	public void onPause() {
		// SCCtlOps.ConnectedSSID = null;
		// SCCtlOps.ConnectedBSSID = null;
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		AsyncHandler.remove(simpleconfig);
		con.exit();
		SCCtlOps.ConnectedSSID = null;
		SCCtlOps.ConnectedBSSID = null;
		SCCtlOps.ConnectedPasswd = null;
		if (mReceiverRegistered == true) {
			mReceiverRegistered = false;
			mActivity.unregisterReceiver(mReceiver);
		}
	}
  //鹿茫虏楼陆脫脢脮WIFImanager碌脛脨脜脧垄拢卢麓娄脌铆
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "intent.getAction(): " + intent.getAction());
			if (intent.getAction().equals(
					WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
				GetAllWifiList();// mScanResults =
									// mWifiManager.getScanResults();
			}
			if (intent.getAction()
					.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
				Log.d(TAG, "" + con.getWiFiStatus());
				if (con.getWiFiStatus() == WifiManager.WIFI_STATE_DISABLED) {
					pwdEv.setText("");
				} else {
					wifiMngr.startScan();
				}
				// GetAllWifiList();
			}
			if (intent.getAction().equals(
					WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
				boolean isCurrentConnectSSID = false;

				NetworkInfo info = intent
						.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				WifiConnectStat = info.getState().toString();
				if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
					/* Store SSID and password to file */
					wifiInfo = wifiMngr.getConnectionInfo();
					String getSSID = wifiInfo.getSSID();
					Log.d(TAG, "Connected SSID: " + getSSID);
					// SCCtlOps.ConnectedSSID = ssid;
					// SCCtlOps.ConnectedBSSID = wifiInfo.getBSSID();
					// WifiConnected = true;
					// Log.d(TAG, "Clicked SSID: " + SCCtlOps.ConnectedSSID);

					if (getSSID == null) {
						return;
					}

					/* Determine if it is the current clicked SSID */
					if (SCCtlOps.ConnectedSSID != null
							&& SCCtlOps.ConnectedSSID.length() != 0
							&& (getSSID.equals(new String("\""
									+ SCCtlOps.ConnectedSSID + "\"")) || getSSID
										.equals(new String(
												SCCtlOps.ConnectedSSID)))) {
						isCurrentConnectSSID = true;
					} else {
						isCurrentConnectSSID = false;
					}

					/* Network is connected */
					if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
						if (isCurrentConnectSSID) {
							Log.v(TAG, "Wi-Fi network is connected.");
							/* Store SSID and password to file */
							fileOps.UpdateSsidPasswdFile(); // connect
															// successful,
															// update file
						} else {
							WifiConnectStat = "DISCONNECTED";
						}
					}

					// pwdSn.setAdapter(wifiListItemAdapter);
					// wifiListItemAdapter.notifyDataSetChanged();
					// GetAllWifiList();//mScanResults =
					// mWifiManager.getScanResults();
				} else if (info.getState().equals(
						NetworkInfo.State.DISCONNECTED)
						|| !isCurrentConnectSSID) {
					// WifiConnected = false;
					// SCCtlOps.ConnectedSSID = null;
					// SCCtlOps.ConnectedBSSID = null;
					Message msg = con.getMsgHandler().obtainMessage(
							WIFI_DISCONNECTED);
					con.getMsgHandler().sendMessage(msg);
					Log.i(TAG, "WifiConnected" + WifiConnectStat);
				}

				wifiMngr.startScan();
				GetAllWifiList();
			}

		}
	};

	public void GetAllWifiList() {
		scan_wifi.setEnabled(false);
		//
		List<ScanResult> getScanResults = con.getScanResult();
		if (getScanResults == null) {
			Log.e(TAG, "Get scan results error.");
			scan_wifi.setEnabled(true);
			return;
		}
		//
		int getSize = getScanResults.size();
		mScanResults.clear();

		// Log.d("msg", "get allSCCtlOps.ConnectedSSID " +
		// SCCtlOps.ConnectedSSID);
		// List<ScanResult> subScanResults = new ArrayList<ScanResult>();
		int subSize = 0;
		ScanResult selectResult = null;
		// ScanResult tmpResult = null;

		if (getScanResults != null) {
			for (int i = 0; i < getSize; i++) {
				if (getScanResults.get(i).level <= -100) {
					// Log.d(TAG, "Ignored extra poor signal(" +
					// getScanResults.get(i).level + "dBm) Wi-Fi.\n");
					continue;
				}
				if (getScanResults.get(i).SSID == null
						|| getScanResults.get(i).SSID.length() == 0) {
					continue; //
				}
				// Log.i(TAG, "SCCtlOps.ConnectedSSID="+SCCtlOps.ConnectedSSID);
				if ((SCCtlOps.ConnectedSSID != null)
						&& (SCCtlOps.ConnectedSSID.length() > 0)
						&& ((SCCtlOps.ConnectedSSID.equals(new String(
								getScanResults.get(i).SSID))) || (SCCtlOps.ConnectedSSID
								.equals(new String("\""
										+ getScanResults.get(i).SSID + "\""))))) {

					// Log.d(TAG, "WifiConnectStat: " + WifiConnectStat);
					Log.i(TAG, "getScanResults.get(i).SSID=" + i + " "
							+ getScanResults.get(i).SSID);

					selectResult = getScanResults.get(i); // Find out the
															// selected SSID

					if (WifiConnectStat.equals("CONNECTED")) {
						Log.d(TAG, "Find out the connected SSID");
						mScanResults.add(new MyScanResult(
								MyScanResult.CONNECTED, selectResult));
					} else if (WifiConnectStat.equals("CONNECTING")) {
						mScanResults.add(new MyScanResult(
								MyScanResult.CONNECTING, selectResult));
					} else {
						mScanResults.add(new MyScanResult(
								MyScanResult.DISCONNECTED, selectResult));
					}
					Log.i("msg", "Find out the selected SSID");
				} else {
					mScanResults.add(new MyScanResult(MyScanResult.UNKOWN,
							getScanResults.get(i))); // Store the other SSIDs
					subSize++;
				}
			}
			// Log.d(TAG, "subSize: " + subSize);

			//
			Collections.sort(mScanResults, new Comparator<MyScanResult>() {
				@Override
				public int compare(MyScanResult a, MyScanResult b) {
					if (a.getStatus() != b.getStatus()) {
						return (a.getStatus() - b.getStatus());
					} else {
						return (String.format("%d", a.getS().level))
								.compareTo(String.format("%d", b.getS().level));
						// return (String.format("%d",
						// b.level)).compareTo(String.format("%d", a.level));
					}
				}
			});

			//
			// mScanResults = new ArrayList<ScanResult>();
			// HashMap<String, Object> hmap;
			// wifiArrayList.clear();
			// if(WifiConnected || WifiConnecting || WifiDisconnected) {
			// // Set the connected/connecting SSID to the top
			// mScanResults.add(selectResult);
			// hmap = new HashMap<String, Object>();
			//
			// hmap.put("list_item_upper", selectResult.SSID);
			// if(WifiConnected) {
			// hmap.put("list_item_below", selectResult.level + "dBm" +
			// "  Connected");
			// } else if (WifiConnecting) {
			// hmap.put("list_item_below", selectResult.level + "dBm" +
			// "  Connecting");
			// } else {
			// hmap.put("list_item_below", selectResult.level + "dBm" +
			// "  Disconnected");
			// }
			// wifiArrayList.add(hmap);
			//
			// for(int i=0; i<subSize; i++){ // Set the other SSIDs to bellow
			// tmpResult = subScanResults.get(i);
			// mScanResults.add(tmpResult);
			// hmap = new HashMap<String, Object>();
			// hmap.put("list_item_upper", tmpResult.SSID);
			// hmap.put("list_item_below", tmpResult.level + "dBm");
			// wifiArrayList.add(hmap);
			// }
			//
			// hmap = new HashMap<String, Object>();
			// hmap.put("list_item_upper", "Add more network......");
			// wifiArrayList.add(hmap);
			// } else {
			// for(int i=0; i<subSize; i++){ // Set the whole SSIDs
			// tmpResult = subScanResults.get(i);
			// mScanResults.add(tmpResult);
			// hmap = new HashMap<String, Object>();
			// hmap.put("list_item_upper", tmpResult.SSID);
			// hmap.put("list_item_below", tmpResult.level + "dBm");
			// wifiArrayList.add(hmap);
			// }
			//
			// hmap = new HashMap<String, Object>();
			// hmap.put("list_item_upper", "Add more network......");
			// wifiArrayList.add(hmap);
			// }
		}

		pwdSn.setAdapter(wifiListItemAdapter);
		// wifiListItemAdapter.notifyDataSetChanged();
		// pwdSn.setSelection(0);

		scan_wifi.setEnabled(true);
	}

	public final class ViewHolder {
		public TextView titleText;
		public TextView infoText;
		public ImageView signal;
	}

	public class listBaseAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public listBaseAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			return mScanResults.size();
		}

		public Object getItem(int arg0) {
			return mScanResults.get(arg0);
		}

		public long getItemId(int arg0) {
			return arg0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.sc_list_items, null);
				holder.titleText = (TextView) convertView
						.findViewById(R.id.list_item_upper);
				holder.infoText = (TextView) convertView
						.findViewById(R.id.connected);
				holder.signal = (ImageView) convertView
						.findViewById(R.id.signal);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			MyScanResult ms = mScanResults.get(position);
			if (ms.getStatus() != MyScanResult.NONE) {
				holder.titleText
						.setText(mScanResults.get(position).getS().SSID);
				if (ms.getStatus() == MyScanResult.CONNECTED) {
					holder.infoText.setText("connected");
				} else if (ms.getStatus() == MyScanResult.CONNECTING) {
					holder.infoText.setText("connecting");
				}
				String ScanResultSecurity = Wifi.ConfigSec
						.getScanResultSecurity(ms.getS());
				boolean IsOpenNetwork = Wifi.ConfigSec
						.isOpenNetwork(ScanResultSecurity);
				if (IsOpenNetwork) {
					holder.signal
							.setImageResource(SIGNAL_LEVEL_OPEN[WifiManager
									.calculateSignalLevel(ms.getS().level,
											SIGNAL_LEVEL_OPEN.length)]);
				} else {
					holder.signal
							.setImageResource(SIGNAL_LEVEL_LOCK[WifiManager
									.calculateSignalLevel(ms.getS().level,
											SIGNAL_LEVEL_LOCK.length)]);
				}
			} else {
				holder.titleText.setText("Wifi is closed!");
			}

			return convertView;
		}
	}

	public void OpenWifiPopUp() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle("Warning: Wi-Fi Disabled!")
				.setIcon(R.drawable.pick_from_camera)
				.setCancelable(false)
				.setPositiveButton("Turn on Wi-Fi",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// startActivity(new
								// Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
								wifiMngr.setWifiEnabled(true);
								// SCLib.WifiOpen();
								wifiMngr.startScan();
								// SCLib.WifiStartScan();
								GetAllWifiList();
							}
						})
				.setNegativeButton("Exit",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								AddDeviceActivity.this.finish();
							}
						});
		builder.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == QRCAPTURE_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Bundle bundle = data.getExtras();
				String scanResult = bundle.getString("result");
				devIDEv.setText(scanResult);
			}
		} else if (requestCode == SIMPLECONFIG_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Bundle bundle = data
						.getBundleExtra(AppConfig.config_simpleconfig);
				String ip = bundle.getString(AppConfig.config_ipaddress);
				String mac = bundle.getString(AppConfig.config_macaddress);

			} //
			else if (resultCode == RESULT_CANCELED) {
				Bundle bundle = data
						.getBundleExtra(AppConfig.config_simpleconfig);
				String ip = bundle.getString(AppConfig.config_ipaddress);
				String mac = bundle.getString(AppConfig.config_macaddress);

			}

		} else if (requestCode == POSITION_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Bundle bundle = data.getBundleExtra(AppConfig.config_position);
				String position = "unknown";
				if (bundle != null) {
					position = bundle
							.getString(AppConfig.config_position_label);
					String latitude = bundle
							.getString(AppConfig.config_latitude);
					String longitude = bundle
							.getString(AppConfig.config_longitude);
				}

			} //
		}

	}

	public class MyScanResult {

		public static final int CONNECTED = 1;
		public static final int CONNECTING = 2;
		public static final int DISCONNECTED = 3;
		public static final int UNKOWN = 4;
		public static final int NONE = 5;
		private int status;
		private ScanResult s;

		public MyScanResult(int st, ScanResult sr) {
			setStatus(st);
			setS(sr);
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public ScanResult getS() {
			return s;
		}

		public void setS(ScanResult s) {
			this.s = s;
		}

	}

	/**
	 * Try to launch Wifi Connecter with {@link #hostspot}. Prompt user to
	 * download if Wifi Connecter is not installed.
	 * 
	 * @param activity
	 * @param hotspot
	 */
	private static void launchWifiConnecter(final Activity activity,
			final ScanResult hotspot) {
		final Intent intent = new Intent("com.wifi.connecter.CONNECT_OR_EDIT");
		intent.putExtra("com.wifi.connecter.HOTSPOT", hotspot);
		try {
			activity.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			// Wifi Connecter Library is not installed.
			Toast.makeText(activity, "Wifi Connecter is not installed.",
					Toast.LENGTH_LONG).show();

		}
	}

}
