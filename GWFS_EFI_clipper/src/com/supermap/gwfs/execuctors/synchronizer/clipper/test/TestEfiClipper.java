package com.supermap.gwfs.execuctors.synchronizer.clipper.test;

import com.supermap.gwfs.executors.synchronizer.clipper.efi.handler.EfiClipperThread;

/**  
 * @Description: TODO
 * @author zhoujian
 * @date 2016-11-16
 * @version V1.0 
 */
public class TestEfiClipper
{
	public static void main(String[] args)
	{
		long startTime = System.currentTimeMillis();
		new EfiClipperThread("C:\\Users\\LQ\\Desktop\\grib_simple\\EFI","20161120","00","240").run();
		long endTime = System.currentTimeMillis();
		System.out.println("耗时 : " + (endTime - startTime ) / 1000 + "秒");
	}
}
