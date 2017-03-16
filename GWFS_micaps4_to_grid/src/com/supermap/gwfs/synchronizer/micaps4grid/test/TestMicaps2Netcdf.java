package com.supermap.gwfs.synchronizer.micaps4grid.test;

import java.io.File;

import com.supermap.gwfs.synchronizer.micaps4grid.Micaps4gridThread;

/**  
 * @Description: TODO
 * @author zhoujian
 * @date 2016-11-29
 * @version V1.0 
 */
public class TestMicaps2Netcdf
{
	public static void main(String[] args)
	{
		
//		String dir = "D:/test/micaps/2T/999";
		String dir = "D:/test/micaps/10u/999";
		File dirs = new File(dir);
		if (dirs.isDirectory())
		{
			File[] files = dirs.listFiles();
			for (File file : files)
			{
				String filePath = file.getAbsolutePath();
				System.out.println(filePath);
				//测试整个线程
				testMicaps2NetcdfThread(filePath);
			}
		}
		String dir1 = "D:/test/micaps/10v/999";
		File dirs2 = new File(dir1);
		if (dirs.isDirectory())
		{
			File[] files = dirs2.listFiles();
			for (File file : files)
			{
				String filePath = file.getAbsolutePath();
				System.out.println(filePath);
				//测试整个线程
				testMicaps2NetcdfThread(filePath);
			}
		}
		
		System.out.println("完成 ");
		
//		testMicaps2NetcdfThread("D:\\test\\16121520.078");
		
//		String filePath  = "D:\\test\\17010908.024";
//		Micaps4gridThread thread = new Micaps4gridThread(filePath,"TP");
//		thread.run();
	}
	
	/**
	 * 
	 * @Description: 测试Micapse4 解析
	 * @return void
	 * @throws
	 */
	public static void testAnalytical(String filePath)
	{
//		MicapsAnalytical.getInstance().Analytical(filePath);
	}
	
	public static void testMicaps2NetcdfThread(String filePath)
	{
		String element = filePath.replace("\\", "/").split("/")[3];
		Micaps4gridThread thread = new Micaps4gridThread(filePath,element);
		thread.run();
		
//		String element = "R";
//		Micaps4gridThread thread = new Micaps4gridThread(filePath,element);
//		thread.run();
	}
}
