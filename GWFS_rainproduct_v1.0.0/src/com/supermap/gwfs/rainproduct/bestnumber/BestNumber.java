package com.supermap.gwfs.rainproduct.bestnumber;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.rainproduct.util.CollectionUtil;
import com.supermap.gwfs.rainproduct.util.ReadConfig;

/**  
 * @Description: 选取最优成员
 * @author zhoujian
 * @date 2016-10-20
 * @version V1.0 
 */
public class BestNumber
{
	private Logger logger = LoggerFactory.getLogger("BestNumber");
	private String[] localFilePath = new String[4];
	private SimilarData similarData = null;
	public BestNumber()
	{
		similarData = new SimilarData();
	}
	
	public synchronized UniObject  findBestNumber(String time, String timeSequence, String timeValid ,  String origin_val ,String element) throws IOException
	{
		UniObject uniObject = null;
		try
		{
			/*************【读取配置文件】******************/
			uniObject = ReadConfig.getInstance().getUniObject();
			UniObject uniObject_ftp = ReadConfig.getInstance().getUniObject_ftp();
			logger.info("读取配置文件完成！");
			/*************【下载数据文件到本地】***************/
			boolean flag = LoadFiles.getInstance(uniObject_ftp).loadFile(uniObject , uniObject_ftp , time , timeSequence , timeValid , origin_val , element);
			if (flag)
			{
				localFilePath = LoadFiles.getInstance(uniObject_ftp).getLocalFilePath();
				/*************【标准化各个层次和成员数据】***********/
				Map<Integer, Double> pl500Map = similarData.getPl500SimilaryData(uniObject , "500" , localFilePath);
				Map<Integer, Double> pl700Map = similarData.getPl750SimilaryData(uniObject , "700" , localFilePath);;
				Map<Integer, Double> pl850Map = similarData.getPl800SimilaryData(uniObject , "850" , localFilePath);;
				Map<Integer, Double> sfcMap   = similarData.getSfcSimilaryData(uniObject , "0" , localFilePath);;
				
				/**************【各个层次标准化求和】**************/
				Map<Integer, Double> sumMap = getSum(pl500Map,pl700Map,pl850Map,sfcMap);
				
				/**************【对求和集合排序(升序)】************/
				LinkedHashMap< Integer, Double> sortMap = CollectionUtil.sortByValue(sumMap);
				
				/**************【选取最优成员30个】***************/
				LinkedHashMap<Integer, Double> bestNumberMap =  subMap(sortMap,uniObject.getIntegerValue("number"));
				uniObject.setValue("bestNumber", bestNumberMap);
				uniObject.setValue("localFilePath", localFilePath);
				
				logger.info("最优成员选取完毕！");
				
				
				/**
				 * 获取时间
				 */
				int forcastTime  = similarData.getForcastTime(localFilePath[0]);
				uniObject.setIntegerValue("time", forcastTime);
			}
			else {
				uniObject = null;
			}
		}
		catch (Exception e)
		{
			logger.error("获取最优成员异常 , 异常 : " + e);
		}
		return uniObject.clone();
	}


	/**
	 * @Description: 裁剪最优成员
	 * @return LinkedHashMap<Integer,Double>
	 * @throws
	 */
	private LinkedHashMap<Integer, Double> subMap(LinkedHashMap<Integer, Double> sortMap, int number)
	{
		LinkedHashMap<Integer, Double> resultMap = new LinkedHashMap<Integer, Double>();
		try
		{
			int index = 0;
			for (Integer integer : sortMap.keySet())
			{
				if (index < number)
				{
					resultMap.put(integer, sortMap.get(integer));
				}
				index++ ;
			}
		}
		catch (Exception e)
		{
			logger.error("筛选最优成员异常 , 异常 : " + e);
		}
		
		return resultMap;
	}

	/**
	 * 
	 * @Description: 各层次标准化求和
	 * @return Map<Integer,Double>
	 * @throws
	 */
	private Map<Integer, Double> getSum(Map<Integer, Double> pl500Map, Map<Integer, Double> pl700Map, Map<Integer, Double> pl850Map, Map<Integer, Double> sfcMap)
	{
		Map<Integer, Double> resultMap = new HashMap<Integer, Double>();
		try
		{
			for (Integer integer : pl500Map.keySet())
			{
				resultMap.put(integer, pl500Map.get(integer) + pl700Map.get(integer) + pl850Map.get(integer) + sfcMap.get(integer));
			}
		}
		catch (Exception e)
		{
			logger.error("各层次求和异常 , 异常 ： " + e);
		}
		
		return resultMap;
	}
}
