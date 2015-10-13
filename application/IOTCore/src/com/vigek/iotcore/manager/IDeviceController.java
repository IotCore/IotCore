package com.vigek.iotcore.manager;

import com.vigek.iotcore.bean.Deviceinfo;

public interface IDeviceController {
    
    public static final int CONTROLLER_TYPE_GPIO =  1;
    public static final int CONTROLLER_TYPE_CAMERA =2;
    public static final int CONTROLLER_TYPE_ALL = 3;
    
    void onDeviceSelcted(int type, Deviceinfo d);
}
