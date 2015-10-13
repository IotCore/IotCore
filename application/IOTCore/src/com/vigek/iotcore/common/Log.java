/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * package-level logging flag
 */

package com.vigek.iotcore.common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {
    public final static String LOGTAG = "com.vigek.smokealarm";

    /** This must be false for production.  If true, turns on logging,
     test code, etc. */
    static final boolean LOGV = true;

    public static void v(String logMe) {
    	if(LOGV == true){
    		android.util.Log.v(LOGTAG, /* SystemClock.uptimeMillis() + " " + */ logMe);
    	}
    }
    
    public static void v(String Tag, String logMe)
    {
    	if(LOGV == true){
    		android.util.Log.v(Tag, logMe);
    	}
    }

    public static void d(String logMe) {
    	if(LOGV == true){
    		android.util.Log.d(LOGTAG, /* SystemClock.uptimeMillis() + " " + */ logMe);
    	}
    }
    
    public static void d(String Tag, String logMe)
    {
    	if(LOGV == true){
    		android.util.Log.d(Tag, logMe);
    	}
    }    
    
    public static void i(String logMe) {
    	if(LOGV == true){
    		android.util.Log.i(LOGTAG, logMe);
    	}
    }

    public static void i(String Tag, String logMe) {
    	if(LOGV == true){
    		android.util.Log.i(Tag, logMe);
    	}
    }   
    
    public static void e(String logMe) {
    	if(LOGV == true){
    		android.util.Log.e(LOGTAG, logMe);
    	}
    }

    public static void e(String Tag, String logMe) {
    	if(LOGV == true){
    		android.util.Log.e(Tag, logMe);
    	}
    }
    
    public static void e(String logMe, Exception ex) {
    	if(LOGV == true){
    		android.util.Log.e(LOGTAG, logMe, ex);
    	}
    }
    
    public static void e(String Tag, String logMe, Exception ex) {
    	if(LOGV == true){
    		android.util.Log.e(Tag, logMe, ex);
    	}
    }

    public static void w(String logMe) {
    	if(LOGV == true){
    		android.util.Log.w(LOGTAG, logMe);
    	}
    }

    public static void w(String Tag, String logMe) {
    	if(LOGV == true){
    		android.util.Log.w(Tag, logMe);
    	}
    }
    
    
    public static void wtf(String logMe) {
    	if(LOGV == true){
    		android.util.Log.wtf(LOGTAG, logMe);
    	}
    }

    public static void wtf(String Tag, String logMe) {
    	if(LOGV == true){
    		android.util.Log.wtf(Tag, logMe);
    	}
    }
    
    public static String formatTime(long millis) {
        return new SimpleDateFormat("HH:mm:ss.SSS/E").format(new Date(millis));
    }
}
