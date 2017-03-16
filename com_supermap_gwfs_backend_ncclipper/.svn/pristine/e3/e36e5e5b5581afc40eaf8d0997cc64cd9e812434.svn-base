package com.supermap.gwfs.clipper.util;

import java.util.Date;

public class TimeUtil {
	/**
	     *
	     * @param 要转换的毫秒数
	     * @return 该毫秒数转换为 * days * hours * minutes * seconds 后的格式
	     * @author LQ
	     */
	public static String formatDuring(long mss) {
		long days = mss / (1000 * 60 * 60 * 24);
		long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
		long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
		long seconds = (mss % (1000 * 60)) / 1000;
		return days + " days " + hours + " hours " + minutes + " minutes " + seconds + " seconds ";
	}

	/**
	*
	* @param begin 时间段的开始
	* @param end   时间段的结束
	* @return  输入的两个Date类型数据之间的时间间格用* days * hours * minutes * seconds的格式展示
	* @author fy.zhang
	*/
	public static String formatDuring(Date begin, Date end) {
		return formatDuring(end.getTime() - begin.getTime());
	}

	public static String formatDuring(Long begin, Long end) {
		return formatDuring(end - begin);
	}
}