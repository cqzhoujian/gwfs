package com.supermap.gwfs.produce.test;

import java.io.File;

import com.supermap.gwfs.produce.satellite.SatelliteThread;

/**  
 * @Description: TODO
 * @author zhoujian
 * @date 2017-3-15
 * @version V1.0 
 */
public class SatelliteTest
{
	public static void main(String[] args)
	{
		//Z_OTHE_RADA2DCR_201703090042.bin文件
		String path = "F:/test/卫星云图";
		File files = new File(path);
		if (files.exists())
		{
			File[] file = files.listFiles();
			for (File f : file)
			{
				String filePath = f.getAbsolutePath();
				if (filePath.contains(".AWX"))
				{
					new SatelliteThread(filePath).start();
				}
			}
		}
		System.out.println("卫星产品测试完成");
	}
}
