package com.supermap.gwfs.rainproduct.probability;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.rainproduct.bestnumber.SimilarData;
import com.supermap.gwfs.rainproduct.util.CollectionUtil;

/**  
 * @Description: 概率匹配平均
 * @author zhoujian
 * @date 2016-10-27
 * @version V1.0 
 */
public class Probability
{
	private Logger logger = LoggerFactory.getLogger("ProbabilityAvg");
	private String size = "100,114,25,35";
//	private String size = "105,110.5,28,32.5";
	private SimilarData similarData = null;
	public Probability()
	{
		similarData = new SimilarData();
	}
	
	public synchronized UniObject getProbabilityAvg(LinkedHashMap<Integer, Double> bestNumberMap, String[] localFilePath)
	{
		UniObject uniObject = new UniObject();
		try
		{
			//最优成员数据
			Map<Integer, Map<Point2D, Double>> rainTempMap = null;
			if (localFilePath[3] != null)
			{
				rainTempMap = similarData.getNumberRainData(uniObject,localFilePath[3],"tp", size,"0",bestNumberMap);
			}
			Map<Integer, Map<Point2D, Double>> rainSumMap = similarData.getNumberRainData(uniObject,localFilePath[2],"tp", size,"0",bestNumberMap);
//			else {
//				rainTempMap = rainSumMap;
//				for (Integer integer : rainTempMap.keySet())
//				{
//					 Map<Point2D, Double> oMap = new HashMap<Point2D, Double>();
//					for (Point2D point : rainTempMap.get(integer).keySet())
//					{
//						oMap.put(point, 0.0);
//					}
//					rainTempMap.put(integer, oMap);
//				}
//			}
			Map<Integer, Map<Point2D, Double>> bestNumberRainMap = similarData.get3hRainData(rainSumMap,rainTempMap);
			//获取最优成员降水平均场
			Map<Point2D, Double> avgMap = similarData.getAvgData(bestNumberRainMap,bestNumberMap);
			/*
			 * 排序
			 */
			List<Double> tmpList = new ArrayList<Double>();
			for (Integer integer : bestNumberRainMap.keySet())
			{
				for (Point2D point : bestNumberRainMap.get(integer).keySet())
				{
					tmpList.add(bestNumberRainMap.get(integer).get(point));
				}
			}
			
			//1 原始数据排序
			Collections.sort(tmpList);

			//2 平均场排序  小 ---> 大
			LinkedHashMap<Point2D, Double> avgLinkedMap = CollectionUtil.sortByValue(avgMap);
			/*
			 * 取 row * col * Number 中的row * col
			 * 小 --> 大
			 */
			List<Double> list = new ArrayList<Double>();
			for (int i = 0; i < tmpList.size()/30; i++)
			{
				list.add(tmpList.get(14 + i * 30));
			}
			
			//代替（返回到平均场）
			int index = 0;
			LinkedHashMap<Point2D, Double> rainProductMap = new LinkedHashMap<Point2D, Double>();
			for (Point2D point : avgLinkedMap.keySet())
			{
				rainProductMap.put(point, list.get(index));
				index++ ;
			}
			uniObject.setValue("rainProductMap", rainProductMap);
//			/**
//			 * 预报时间
//			 */
//			int forcastTime  = similarData.getForcastTime(localFilePath[2]);
//			logger.info("-------------文件名 : " + localFilePath[2] + " 预报时间 ： " + forcastTime + "---------");
//			uniObject.setIntegerValue("time", forcastTime);
			
		}
		catch (Exception e)
		{
			logger.error(localFilePath[2] + " 计算概率匹配平均异常 , 异常 :" + e);
		}
		return uniObject.clone();
	}
}
