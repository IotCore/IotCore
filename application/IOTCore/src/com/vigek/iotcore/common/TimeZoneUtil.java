package com.vigek.iotcore.common;

import java.util.Date;
import java.util.TimeZone;

/**
 * 不同时区对应的时间处理工具类
 * 
 * @author HuangWenwei
 * 
 * @date 2014�?7�?31�?
 */
public class TimeZoneUtil {

	/**
	 * 判断用户的设备时区是否为东八区（中国�? 2014�?7�?31�?
	 * 
	 * @return
	 */
	public static boolean isInEasternEightZones() {
		boolean defaultVaule = true;
		if (TimeZone.getDefault() == TimeZone.getTimeZone("GMT+08"))
			defaultVaule = true;
		else
			defaultVaule = false;
		return defaultVaule;
	}

	/**
	 * 根据不同时区，转换时�? 2014�?7�?31�?
	 * 
	 * @param time
	 * @return
	 */
	public static Date transformTime(Date date, TimeZone oldZone,
			TimeZone newZone) {
		Date finalDate = null;
		if (date != null) {
			int timeOffset = oldZone.getOffset(date.getTime())
					- newZone.getOffset(date.getTime());
			finalDate = new Date(date.getTime() - timeOffset);
		}
		return finalDate;

	}
}
