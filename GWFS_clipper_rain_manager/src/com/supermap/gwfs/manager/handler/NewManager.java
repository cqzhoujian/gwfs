package com.supermap.gwfs.manager.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.disruptor.UniDisruptor;
import com.supermap.disruptor.helper.DisruptorHelper;
import com.supermap.gcpp.core.common.UniObject;

/**  
 * @Description: 管理裁切之后的程序调用
 * @author zhoujian
 * @date 2016-12-19
 * @version V1.0 
 */
public class NewManager
{
	private Logger logger = LoggerFactory.getLogger("Manager");
	private static int[] status = new int[53];				//每个时效状态的数组
	private static List<Integer> valids = new ArrayList<Integer>();		//未处理的时效集合
	private static NewManager newManager = null;
	private static String flag = null;				//标识  是否为同一天的同一时次
	private NewManager()
	{
	}
	public static synchronized NewManager getInstance(String time , String sequrence)
	{
		if (newManager == null)
		{
			newManager = new NewManager();
		}
		if (flag != null)
		{
			if (flag.equals(time+sequrence))
			{
				return newManager;
			}
			else
			{
				flag = time + sequrence;
				Arrays.fill(status, 0);
				valids.clear();
				return newManager;
			}
		}
		else
		{
			flag = time + sequrence;
			return newManager;
		}
	}
	
	
	public synchronized void  manager(String time, String sequrence , String valid)
	{
		try
		{
			/**
			 * 20170116
			 * 对时次进行转换处理
			 * 20160116 08时次原始数据-----处理----> 20160116 20时次产品
			 * 20160116 20时次原始数据-----处理----> 20160117 08时次产品
			 * 0-9时效的数据舍弃，从12时效开始(产品的0时效)
			 */
//			Date forecastDate = ConvertDate.getDate(time+sequrence+"0000", "yyyyMMddHHmmss", 12);
//			time = ConvertDate.dateToString(forecastDate, "yyyyMMddHHmmss").substring(0, 8);
//			System.out.println(time);
//			sequrence = ConvertDate.dateToString(forecastDate, "yyyyMMddHHmmss").substring(8, 10);
//			System.out.println(sequrence);
//			int validTmp = 0;
//			if (Integer.parseInt(valid) - 12 < 0)
//			{
//				return;
//			}
//			else
//			{
//				validTmp = Integer.parseInt(valid) - 12 ;
//			}
			int validTmp = Integer.parseInt(valid);
			logger.info("ZJ:" + time + "  " + sequrence + " " + validTmp + "  be call.");
			
			if (validTmp <= 72)
			{
				if (validTmp == 0 )
				{
					status[validTmp/3] = 1;
					return ;
				}
				if (status[validTmp/3 - 1] ==1)
				{
					status[validTmp / 3] = 1;
					//调用后面程序的disrupter
					//调用降水handler
					UniDisruptor disruptor = DisruptorHelper.getDisruptor("CustomHandler");
					UniObject uniObject = new UniObject();
					uniObject.setStringValue("time", time);
					uniObject.setStringValue("timeSequnce", sequrence);
					uniObject.setValue("timeValids",validTmp);
					if (disruptor != null)
					{
						logger.info("ZJ:beings the call : " + uniObject);
						disruptor.publish(uniObject);
					}
				}
				else
				{
					status[validTmp/3] = 1;
					valids.add(validTmp);
				}
			}
			else
			{
				if (status[validTmp/3 - (validTmp - 72)/6 - 1] == 1)
				{
					status[validTmp/3 - (validTmp - 72)/6] = 1;
					//调用之后的程序
					//调用降水handler
					UniDisruptor disruptor = DisruptorHelper.getDisruptor("CustomHandler");
					UniObject uniObject = new UniObject();
					uniObject.setStringValue("time", time);
					uniObject.setStringValue("timeSequnce", sequrence);
					uniObject.setValue("timeValids",validTmp);
					if (disruptor != null)
					{
						logger.info("ZJ:beings the call : " + uniObject);
						disruptor.publish(uniObject);
					}
				}
				else
				{
					status[validTmp/3 - (validTmp - 72)/6] = 1;
					valids.add(validTmp);
				}
			}
			logger.info("ZJ:valids of waiting for treatment :" + valids);
			boolean flag1 = true;
			while (flag1)
			{
				for (int i = 0; i < valids.size(); i++)
				{
					int validTemp = valids.get(i);  
					if (validTemp <= 72)
					{
						if (status[validTemp / 3 - 1] == 1)
						{
							//调用后续程序
							//调用降水handler
							UniDisruptor disruptor = DisruptorHelper.getDisruptor("CustomHandler");
							UniObject uniObject = new UniObject();
							uniObject.setStringValue("time", time);
							uniObject.setStringValue("timeSequnce", sequrence);
							uniObject.setValue("timeValids",validTemp);
							if (disruptor != null)
							{
								valids.remove(i);
								logger.info("ZJ:beings the call : " + uniObject);
								disruptor.publish(uniObject);
							}
						}
						else
						{
							continue;
						}
					}
					else
					{
						if (status[validTemp / 3 - (validTemp - 72)/6 - 1] == 1)
						{
							//调用后续程序
							//调用降水handler
							UniDisruptor disruptor = DisruptorHelper.getDisruptor("CustomHandler");
							UniObject uniObject = new UniObject();
							uniObject.setStringValue("time", time);
							uniObject.setStringValue("timeSequnce", sequrence);
							uniObject.setValue("timeValids",validTemp);
							if (disruptor != null)
							{
								valids.remove(i);
								logger.info("ZJ:beings the call : " + uniObject);
								disruptor.publish(uniObject);
							}
						}
						else
						{
							continue;
						}
					}
				}
				flag1 = false;
			}
		}
		catch (Exception e)
		{
			logger.error("ZJ:manager() method handles exceptions  , e : " + e);
		}
	}
}
