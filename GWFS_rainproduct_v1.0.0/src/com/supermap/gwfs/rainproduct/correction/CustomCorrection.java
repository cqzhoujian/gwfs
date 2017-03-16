package com.supermap.gwfs.rainproduct.correction;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.Date;
import java.util.LinkedHashMap;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.rainproduct.bestnumber.BestNumber;
import com.supermap.gwfs.rainproduct.gridforcast.GridForcast;
import com.supermap.gwfs.rainproduct.netcdf.Netcdf;
import com.supermap.gwfs.rainproduct.probability.Interpolation;
import com.supermap.gwfs.rainproduct.probability.Probability;
import com.supermap.gwfs.rainproduct.service.GridForcastService;
import com.supermap.gwfs.rainproduct.util.ConvertDate;

/**  
 * @Description: 降水客观订正FQP
 * @author zhoujian
 * @date 2016-10-30
 * @version V1.0 
 */
public class CustomCorrection extends Thread
{
	private Logger logger = LoggerFactory.getLogger("Custom");
	private String time = null;			//预报时间
	private String timeSequence = null;	//时次
	private String timeValid = null;	//时效
	private static BestNumber bestNumber = null;
	private static Probability probability = null;
	private static Interpolation interpolation = null;
	private static Netcdf netcdf = null;
	private static GridForcastService gridForcastService = null;
	private static final String fileversion = "qpf";	//文件版本
	private static String element = "ER03";				//要素名	
	private static final String origin_val = "local";	//初始场
	
	
	public CustomCorrection( String time,String timeSequence,String timeValid)
	{
		this.time = time;
		this.timeSequence = timeSequence;
		this.timeValid = timeValid;
		getInstance();
	}
	public void getInstance()
	{
		if(bestNumber == null)
			bestNumber = new BestNumber();
		if(probability == null)
			probability = new Probability();
		if(interpolation == null)
			interpolation = Interpolation.getInstance();
		if(netcdf == null)
			netcdf = Netcdf.getInstance();
		if (gridForcastService == null)
			gridForcastService = new GridForcastService();
	}
	
	@Override
	public void run()
	{
		rainProcess();
	}
	@SuppressWarnings("unchecked")
	public synchronized void rainProcess()
	{
		try
		{
			logger.info("预报日期 :" + time + " 时次 :" + timeSequence + " 时效 : " + timeValid + " 开始降水订正...");
			/**
			 * 最优成员选取
			 */
			UniObject uniObject = bestNumber.findBestNumber( time, timeSequence, timeValid,origin_val,element);
			String rootLocalPath = uniObject.getStringValue("rootpathLocal");
			LinkedHashMap<Integer, Double> bestNumberMap = (LinkedHashMap<Integer, Double>)uniObject.getValue("bestNumber");
			String[] localFilePath = (String[]) uniObject.getValue("localFilePath");
			logger.info("最优成员选取完毕！" + bestNumberMap +" 时效：" + timeValid );
			//获取预报时间 -- 线程安全问题
			int forcastTime = uniObject.getIntegerValue("time");
			
			/**
			 * 概率匹配平均
			 */
			uniObject = probability.getProbabilityAvg(bestNumberMap, localFilePath);
			LinkedHashMap<Point2D, Double> rainProductMap = (LinkedHashMap<Point2D, Double>)uniObject.getValue("rainProductMap");
			logger.info("概率匹配平均完成！ " + " 时效 : " + timeValid );
			uniObject.setIntegerValue("time", forcastTime);
			/**
			 * 双线性插值
			 */
			uniObject = interpolation.bilinearInter(uniObject, rainProductMap);
//			double[][] X = (double[][])uniObject.getValue("X");
//			double[][] Y = (double[][])uniObject.getValue("Y");
//			double[][] Z = (double[][])uniObject.getValue("Z");
			logger.info("双线性插值完成！   时效：" + timeValid);
			
			/**
			 * 写nc文件
			 */
			/** 对时次进行特殊处理  20170116 08时次----处理---> 20170116 20时次产品*/
			/** 对时次进行特殊处理  20170116 20时次----处理---> 20170117 08时次产品*/
			//产生产品时间
			Date forecastDate = ConvertDate.getDate(time+timeSequence+"0000", "yyyyMMddHHmmss", 12);
			String forecastDateString = ConvertDate.dateToString(forecastDate, "yyyyMMddHHmmss");
			String filePth = rootLocalPath + "/" + origin_val + "/" + element + "/" + forecastDateString.substring(0, 8) + "/" + forecastDateString.substring(8,10) + "/" + "0" + "/" + fileversion;
			File file = new File(filePth);
			if (!file.exists())
			{
				file.mkdirs();
			}
			int valid = Integer.valueOf(timeValid) - 12;
			
			if (valid  > 0)
			{
				String fileName =  forecastDateString.substring(0, 8) + forecastDateString.substring(8,10) + "_" + valid +".nc" ;
//				System.out.println(uniObject);
				uniObject.setValue("element", element);
				boolean flag = netcdf.WriteNetcdf(uniObject , filePth + "/" + fileName);
				logger.info(fileName + " 文件，写状态 ： " + flag);
				if (flag)
				{
					/**
					 * 保存到数据库
					 */
					UniObject u = GridForcast.getInstance().queryGridForecast(origin_val , element);
					u.setStringValue("fileName", fileName);
					u.setStringValue("filePath", filePth + "/" + fileName);
					u.setStringValue("fileversion",fileversion);
					String forcast_date = ConvertDate.stringToString(forecastDateString.substring(0 , 8), "yyyyMMdd", "yyyy-MM-dd");
					u.setStringValue("forcast_date",forcast_date);
					
//					int result = gridForcastService.savaGridForcast(u, forecastDateString.substring(8,10), String.valueOf(valid));
//					if (result > 0)
//					{
//						logger.info("入库成功 ！ 影响行数 ： " + result);
//					}
//					else {
//						logger.info("入库失败 ！ 影响行数 : " + result);
//					}
					
					
					//调用ER12 ER24处理累积降水
					uniObject.setValue("fileversion", fileversion);
					uniObject.setValue("sequrence", forecastDateString.substring(8,10));
					uniObject.setValue("valid", valid);
					uniObject.setValue("forcast_date", forcast_date);
					uniObject.setValue("origin_val", origin_val);
					uniObject.setValue("rootLocalPath", rootLocalPath);
//					AccumulativeRainfull.getInstance(forcast_date, forecastDateString.substring(8,10)).put(uniObject.clone());
				}
			}
			
		}
		catch (Exception e)
		{
			logger.error("降水订正异常 , 异常 : " + e);
		}
	}
}
