package com.supermap.gwfs.grib2live.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/** 
 * IP工具类 
 */
public class PublicUtil {

	public static void main(String[] args) throws UnknownHostException {
		// System.out.println("本机的ip=" + PublicUtil.getIp());
		// System.out.println("工程地址：" + PublicUtil.getPorjectPath());
	}
	/** 
	 * 日志 
	 */
	private static final Logger logger = Logger.getLogger(PublicUtil.class);

	public static String getPorjectPath() {
		String nowpath = "";
		nowpath = System.getProperty("user.dir") + "/";
		return nowpath;
	}

	/**
	 * 获取本机ip
	 * @return
	 */
	public static String getIp() {

		String ip = "";
		try {
			InetAddress inet = InetAddress.getLocalHost();
			ip = inet.getHostAddress();
		} catch (UnknownHostException e) {
			logger.error(e.getMessage(), e);
		}
		return ip;

	}

}
