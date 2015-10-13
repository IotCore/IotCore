package com.vigek.iotcore.common;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;



import com.example.config.AESCrypt;
import com.example.config.SCCtlOps;
//import com.realtek.simpleconfig.AESCrypt;
//import com.realtek.simpleconfig.SCCtlOps;
import com.vigek.iotcore.app.AppConfig;

import android.os.Environment;
import android.util.Log;

public class FileOps {
	private static final String TAG = "FileOps";

//	private String SDPATH = AppConfig.DEFAULT_SAVE_IMAGE_PATH;
	private String CFGFOLDER = AppConfig.DEFAULT_SAVE_IMAGE_PATH;
	public static String OldSsidPasswdFile = "1.txt";
	public static String NewSsidPasswdFile = "1-1.txt";
	public static String CfgPinFile = "2.txt";
	public static String CtlPinFile = "3.txt";
	public static String NounceFile = "4.txt";
	public static String MAC;
	public static String PASSWD;

	public static boolean checkFileExists(String path) {
	    File file = new File(path);
	    return file.exists();
	}

	private boolean createDir(String dir) {
  		File dfile = new File(dir);
  		if(dfile.exists())
  			return true;
//		Log.d(TAG, "createDir: " + dfile);
  		return dfile.mkdir();
    }

	private boolean createFile(String file) throws Exception
	{
        File ffile = new File(file);
//		Log.d(TAG, "createFile: " + ffile);
    	return ffile.createNewFile();
	}

	public static boolean deleteFile(String file) throws Exception
	{
        File ffile = new File(file);
//		Log.d(TAG, "deleteFile: " + ffile);
    	return ffile.delete();
	}

	public void SetKey(String key)
	{
		MAC = key;
		CreateNounceFile();
		PASSWD = ParseNounceFile() + MAC;
//		Log.d(TAG, "PASSWD: " + PASSWD);
	}


	protected RandomAccessFile openFile(String filename) throws Exception
    {
        RandomAccessFile rf = null;
		String dir = CFGFOLDER;
		String file = dir + filename;
//		Log.d(TAG, "openFile: " + file);

		if(!checkFileExists(file)) {
			if(!createDir(dir)) {
				Log.e(TAG, "Create Dir Error");
				return null;
			}
			if(!createFile(file)) {
				Log.e(TAG, "Create File Error");
				return null;
			}
		}

        rf = new RandomAccessFile(file, "rw");

        return rf;
    }

    protected void writeFile(RandomAccessFile rf, String str, boolean encrypt) throws Exception
    {
    	if(rf==null) {
    		return;
    	}

    	if(encrypt) {
        	String estr = AESCrypt.encrypt(PASSWD, str);
//    		Log.d(TAG, "estr: " + estr);
            rf.writeBytes(estr);
    	} else {
            rf.writeBytes(str);
    	}
    }

    protected String readFile(RandomAccessFile rf, boolean encrypt) throws Exception
    {
        String str = null;
        byte[] strbuf = null;
        int len = 0 ;

    	if(rf==null) {
    		return null;
    	}

        len = (int)rf.length();
		if(len==0) {
			return null;
		}

        strbuf = new byte[len];
        rf.read(strbuf, 0, len);
//        for(int i=0; i<strbuf.length; i++)
//    		Log.d(TAG, "" + strbuf[i]);
        str = new String(strbuf);
//		Log.d(TAG, "read str: " + str);

        if(encrypt) {
        	String dstr = AESCrypt.decrypt(PASSWD, str);
//    		Log.d(TAG, "decrypt str: " + dstr);
            return dstr;
        } else {
        	return str;
        }
    }

    protected void closeFile(RandomAccessFile rf) throws Exception
    {
    	if(rf==null)
    		return;

        rf.close();
    }


	public void ParseSsidPasswdFile(String ssid)
	{
    	RandomAccessFile rf = null;
    	String str = null;

		try {
			rf = openFile(FileOps.NewSsidPasswdFile);
		} catch (Exception e1) {
			e1.printStackTrace();
			Log.e(TAG, "Open File Error");
			return;
		}

		try {
			str = readFile(rf, true);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Read File Error");
			try {
				rf.setLength(0); //clear file
			} catch (IOException e1) {
				e1.printStackTrace();
				Log.e(TAG, "Set Length Error");
				return;
			}
			return;
		}
//		Log.d(TAG, "Read: " + str);

    	SCCtlOps.StoredPasswd = new String();
		if(str==null) {
			Log.e(TAG, "Null File");
		} else {
		    String[] items = str.split("\\|");
		    for(int i=0; i<items.length; i++) {
//				System.out.printf("items[%d]: %s\n", i, items[i]);
			    String[] subitems = items[i].split("\\:");
//			    for(int j=0; j<subitems.length; j++) {
//					Log.d(TAG, String.format("subitems[%d]: %s\n", j, subitems[j]));
//			    }
//				Log.d(TAG, String.format("Selected SSID: %s\n", ssid));
			    if(subitems.length>1) { //make sure password area exists
				    if(ssid.equals(subitems[0]) && !subitems[1].equals("null")) {
				    	SCCtlOps.StoredPasswd += subitems[1];
//						Log.d(TAG, "Find already existed SSID");
				    	break;
				    }
			    }
		    }
		}

    	try {
    		closeFile(rf);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Close File Error");
		}
	}

	public void UpdateSsidPasswdFile()
	{
//		Log.d(TAG, "isOpenNetwork: " + isOpenNetwork);
		if(!SCCtlOps.IsOpenNetwork && (SCCtlOps.ConnectedPasswd==null||SCCtlOps.ConnectedPasswd.length()==0)) {
//			Log.d(TAG, "SCCtlOps.ConnectedPasswd: " + SCCtlOps.ConnectedPasswd);
			return;
		}

    	RandomAccessFile rf;
		try {
			rf = openFile(FileOps.NewSsidPasswdFile);
		} catch (Exception e2) {
			e2.printStackTrace();
			Log.e(TAG, "Open File Error");
    		return;
		}

    	long len=0;
    	boolean isOld = false;
		String getstr = new String();
		String setstr = new String();
    	try {
			len = rf.length();
		} catch (IOException e1) {
			e1.printStackTrace();
			Log.e(TAG, "Get Length Error");
    		return;
		}
		if(len>0) {
			try {
				getstr = readFile(rf, true);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "Read File Error");
	    		return;
			}
//			Log.d(TAG, "getstr: " + getstr);

		    String[] items = getstr.split("\\|");
		    for(int i=0; i<items.length; i++) {
//				System.out.printf("items[%d]: %s\n", i, items[i]);
			    String[] subitems = items[i].split("\\:");
//			    for(int j=0; j<subitems.length; j++) {
//					System.out.printf("subitems[%d]: %s\n", j, subitems[j]);
//			    }
			    if(SCCtlOps.ConnectedSSID.equals(subitems[0])) {
			    	isOld = true;
//					Log.d(TAG, "Refresh old");
			    	if(SCCtlOps.IsOpenNetwork)
			    		setstr += SCCtlOps.ConnectedSSID + ":null|";
			    	else
			    		setstr += SCCtlOps.ConnectedSSID + ":" + SCCtlOps.ConnectedPasswd + "|";
			    } else {
//					Log.d(TAG, "Re-Add existed");
					setstr += subitems[0] + ":" + subitems[1] + "|";
			    }
		    }
		}
		if(!isOld) {
//			Log.d(TAG, "Add new");
	    	if(SCCtlOps.IsOpenNetwork)
	    		setstr += SCCtlOps.ConnectedSSID + ":null|";
	    	else
	    		setstr += SCCtlOps.ConnectedSSID + ":" + SCCtlOps.ConnectedPasswd + "|";
		}

//		Log.d(TAG, "setstr: " + setstr);
		try {
			rf.setLength(0); //clear file
			rf.seek(0); //re-write all info
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Re-Seek Error");
    		return;
		}
    	try {
    		writeFile(rf, setstr, true);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Write File Error");
    		return;
		}

    	try {
    		closeFile(rf);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Close File Error");
    		return;
		}
	}

	public void UpgradeSsidPasswdFile()
	{
		String password = PASSWD; //backup it first

		String old_file = CFGFOLDER + FileOps.OldSsidPasswdFile;
//		Log.d(TAG, "old_file: " + old_file);
		if(!checkFileExists(old_file)) {
//			Log.d(TAG, "Old File does not exist.");
			return;
		}

    	RandomAccessFile rf_old;
		try {
			rf_old = openFile(FileOps.OldSsidPasswdFile);
		} catch (Exception e2) {
			e2.printStackTrace();
			Log.e(TAG, "Open Old File Error");
    		return;
		}

    	long len=0;
		String getstr = new String();
    	try {
			len = rf_old.length();
		} catch (IOException e1) {
			e1.printStackTrace();
			Log.e(TAG, "Get Length Error");
    		return;
		}
		if(len>0) {
			try {
				PASSWD = MAC; //for old encryption
//				Log.d(TAG, "Old key: " + PASSWD);
				getstr = readFile(rf_old, true);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "Read File Error");
	    		return;
			}
//			Log.d(TAG, "getstr: " + getstr);
		}

    	try {
    		closeFile(rf_old);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Close File Error");
    		return;
		}

    	PASSWD = password; //for new encryption
//		Log.d(TAG, "New key: " + PASSWD);
    	RandomAccessFile rf_new;
		try {
			rf_new = openFile(FileOps.NewSsidPasswdFile);
		} catch (Exception e2) {
			e2.printStackTrace();
			Log.e(TAG, "Open File Error");
    		return;
		}

		try {
			rf_new.setLength(0); //clear file
			rf_new.seek(0); //re-write all info
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Re-Seek Error");
    		return;
		}
    	try {
    		writeFile(rf_new, getstr, true);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Write File Error");
    		return;
		}
		Log.i(TAG, "Upgrade File Success.");

    	try {
    		closeFile(rf_new);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Close File Error");
    		return;
		}

		try {
			deleteFile(old_file);
		} catch (Exception e1) {
			Log.e(TAG, "Delete File Error");
			e1.printStackTrace();
		}
	}

	public String ParseCfgPinFile()
	{
    	RandomAccessFile rf = null;
    	String getstr = null;

		try {
			rf = openFile(FileOps.CfgPinFile);
		} catch (Exception e1) {
			e1.printStackTrace();
			Log.e(TAG, "Open File Error");
			return null;
		}

		try {
			getstr = readFile(rf, true);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Read File Error");
			try {
				rf.setLength(0); //clear file
			} catch (IOException e1) {
				e1.printStackTrace();
				Log.e(TAG, "Set Length Error");
				return null;
			}
			return null;
		}
//		Log.d(TAG, "Read: " + getstr);

    	try {
    		closeFile(rf);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Close File Error");
		}

		return getstr;
	}

	public void UpdateCfgPinFile(String pin)
	{
    	RandomAccessFile rf;
		try {
			rf = openFile(FileOps.CfgPinFile);
		} catch (Exception e2) {
			e2.printStackTrace();
			Log.e(TAG, "Open File Error");
    		return;
		}

		try {
			rf.setLength(0); //clear file
			rf.seek(0); //re-write all info
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Re-Seek Error");
    		return;
		}

    	try {
    		writeFile(rf, pin, true);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Write File Error");
    		return;
		}

    	try {
    		closeFile(rf);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Close File Error");
    		return;
		}
	}

	public String ParseCtlPinFile(String mac)
	{
    	RandomAccessFile rf = null;
    	String getstr = null;
    	String pin = null;

		try {
			rf = openFile(FileOps.CtlPinFile);
		} catch (Exception e1) {
			e1.printStackTrace();
			Log.e(TAG, "Open File Error");
			return null;
		}

		try {
			getstr = readFile(rf, true);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Read File Error");
			try {
				rf.setLength(0); //clear file
			} catch (IOException e1) {
				e1.printStackTrace();
				Log.e(TAG, "Set Length Error");
				return null;
			}
			return null;
		}
//		Log.d(TAG, "Read: " + getstr);

		if(getstr==null) {
			Log.e(TAG, "Null File");
		} else {
		    String[] items = getstr.split("\\;");
		    for(int i=0; i<items.length; i++) {
//				System.out.printf("items[%d]: %s\n", i, items[i]);
			    String[] subitems = items[i].split("\\|");
//			    for(int j=0; j<subitems.length; j++) {
//					Log.d(TAG, String.format("subitems[%d]: %s\n", j, subitems[j]));
//			    }
//				Log.d(TAG, String.format("Selected MAC: %s\n", mac));
			    if(subitems.length>1) { //make sure pin area exists
				    if(mac.equals(subitems[0]) && !subitems[1].equals("null")) {
				    	pin = subitems[1];
//						Log.d(TAG, "Find PIN");
				    	break;
				    }
			    }
		    }
		}

    	try {
    		closeFile(rf);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Close File Error");
		}

		return pin;
	}

	public void UpdateCtlPinFile(String mac, String pin)
	{
    	RandomAccessFile rf;
		try {
			rf = openFile(FileOps.CtlPinFile);
		} catch (Exception e2) {
			e2.printStackTrace();
			Log.e(TAG, "Open File Error");
    		return;
		}

    	long len=0;
    	boolean isOld = false;
		String getstr = new String();
		String setstr = new String();
    	try {
			len = rf.length();
		} catch (IOException e1) {
			e1.printStackTrace();
			Log.e(TAG, "Get Length Error");
    		return;
		}
		if(len>0) {
			try {
				getstr = readFile(rf, true);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "Read File Error");
	    		return;
			}
//			Log.d(TAG, "getstr: " + getstr);

		    String[] items = getstr.split("\\;");
		    for(int i=0; i<items.length; i++) {
//				System.out.printf("items[%d]: %s\n", i, items[i]);
			    String[] subitems = items[i].split("\\|");
//			    for(int j=0; j<subitems.length; j++) {
//					System.out.printf("subitems[%d]: %s\n", j, subitems[j]);
//			    }
			    if(mac.equals(subitems[0])) {
			    	isOld = true;
//					Log.d(TAG, "Refresh old");
				    setstr += subitems[0] + "|" + pin + ";";
			    } else {
//					Log.d(TAG, "Re-Add existed");
				    setstr += subitems[0] + "|" + subitems[1] + ";";
			    }
		    }
		}
		if(!isOld) {
//			Log.d(TAG, "Add new");
	    	setstr += mac + "|" + pin + ";";
		}

//		Log.d(TAG, "setstr: " + setstr);
		try {
			rf.setLength(0); //clear file
			rf.seek(0); //re-write all info
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Re-Seek Error");
    		return;
		}
    	try {
    		writeFile(rf, setstr, true);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Write File Error");
    		return;
		}

    	try {
    		closeFile(rf);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Close File Error");
    		return;
		}
	}

	private void CreateNounceFile()
	{
    	RandomAccessFile rf;
		try {
			rf = openFile(FileOps.NounceFile);
		} catch (Exception e2) {
			e2.printStackTrace();
			Log.e(TAG, "Open File Error");
    		return;
		}

		try {
			if(rf.length()==64) {
//				Log.d(TAG, "Nonce file existed.");
		    	try {
		    		closeFile(rf);
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "Close File Error");
		    		return;
				}
				return;
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}

		/** Nonce */
		byte[] Nonce = new byte[64];
		String NonceStr = new String();
		for(int i=0; i<64; i++) {
    		Random r = new Random();
    		Nonce[i] = (byte) (r.nextInt(94)+32);
//    		System.out.printf("%02x ", Nonce[i]);
    		NonceStr += String.format("%c", Nonce[i]);
		}
//		System.out.printf("\n");
//		Log.d(TAG, "NonceStr: " + NonceStr);

    	try {
			rf.setLength(0); //clear file
			rf.seek(0); //re-write all info
    		writeFile(rf, NonceStr, false);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Write File Error");
    		return;
		}

    	try {
    		closeFile(rf);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Close File Error");
    		return;
		}
	}

	private String ParseNounceFile()
	{
    	RandomAccessFile rf = null;
    	String getstr = null;

		try {
			rf = openFile(FileOps.NounceFile);
		} catch (Exception e1) {
			e1.printStackTrace();
			Log.e(TAG, "Open File Error");
			return null;
		}

		try {
			getstr = readFile(rf, false);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Read File Error");
			try {
				rf.setLength(0); //clear file
			} catch (IOException e1) {
				e1.printStackTrace();
				Log.e(TAG, "Set Length Error");
				return null;
			}
			return null;
		}
//		Log.d(TAG, "Read: " + getstr);

    	try {
    		closeFile(rf);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Close File Error");
		}

		return getstr;
	}
}
