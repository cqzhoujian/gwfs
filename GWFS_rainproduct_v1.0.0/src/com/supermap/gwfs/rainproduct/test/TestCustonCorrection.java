package com.supermap.gwfs.rainproduct.test;

import com.supermap.gwfs.rainproduct.correction.CustomCorrection;

/**  
 * @Description: TODO
 * @author zhoujian
 * @date 2016-11-9
 * @version V1.0 
 */
public class TestCustonCorrection
{

	/** 
	 * @Description: TODO
	 * @return void
	 * @throws 
	 */
	public static void main(String[] args)
	{
		for (int i = 12; i < 72; )
		{
			i = i + 3;
			new CustomCorrection("20170305", "20", String.valueOf(i)).rainProcess();
		}
		for (int i = 72; i < 240; )
		{
			i = i + 6;
			new CustomCorrection("20170305", "20", String.valueOf(i)).rainProcess();
		}
//		new CustomCorrection("20170222", "20", String.valueOf(63)).rainProcess();
//		new CustomCorrection("20170222", "20", String.valueOf(69)).rainProcess();
	}

}
