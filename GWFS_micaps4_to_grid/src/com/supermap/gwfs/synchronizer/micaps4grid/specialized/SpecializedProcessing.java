package com.supermap.gwfs.synchronizer.micaps4grid.specialized;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.synchronizer.micaps4grid.netcdf.Netcdf;
import com.supermap.gwfs.synchronizer.micaps4grid.save.SaveHelper;

/**  
 * @Description: 对温度和降水要素进行特殊的处理
 * @author zhoujian
 * @date 2016-12-8
 * @version V1.0 
 */
public class SpecializedProcessing
{
	private Logger logger = LoggerFactory.getLogger("Specialized");
	private static Map<Integer,double[][]> TMaxMap = new HashMap<Integer, double[][]>();
	private static Map<Integer, double[][]> TMinMap = new HashMap<Integer, double[][]>();
	private static Map<Integer,UniObject> ER12Map = new HashMap<Integer, UniObject>();
	private static Map<Integer,UniObject> ER24Map = new HashMap<Integer, UniObject>();
	private static Map<Integer,UniObject > ER03Map = new HashMap<Integer, UniObject>();
	private static SpecializedProcessing specializedProcessing = null;
	private static int[] status = new int[53];
	private static String flag = null;
	//
	private static List<Integer> valids = new  ArrayList<Integer>();
	private static List<Integer> valids_12 = new  ArrayList<Integer>();
	private static List<Integer> valids_24 = new  ArrayList<Integer>();
	
	private SpecializedProcessing()
	{}
	
	public static synchronized SpecializedProcessing getInstance(String forecastDate ,String sequrence)
	{
		if (specializedProcessing == null)
		{
			flag = forecastDate + "_" + sequrence;
			specializedProcessing = new SpecializedProcessing();
		}
		else
		{
			if (!(forecastDate + "_" + sequrence).equals(flag))
			{
				flag = forecastDate + "_" + sequrence;
				Arrays.fill(status, 0);
				TMaxMap.clear();
				TMinMap.clear();
				ER03Map.clear();
				valids.clear();
				valids_12.clear();
				valids_24.clear();
			}
		}
		return specializedProcessing;
	}
	
	/**
	 * 
	 * @Description: 根据不同的要数返回对象
	 * @return Map<Integer,float[]>
	 * @throws
	 */
	public Map<Integer,double[][]> getMap(String element)
	{
		try
		{
			if ("TEM_Max".equals(element))
			{
				return TMaxMap;
			}
			else if ("TEM_Min".equals(element))
			{
				return TMinMap;
			}
			else
			{
				return null;
			}
		}
		catch (Exception e)
		{
			logger.error("ZJ: "+ element +" get Map error , error : " + e);
		}
		return null;
	}




	/**
	 * 
	 * @Description: 根据要素获取Map，并放入key  value
	 * @return void
	 * @throws
	 */
	public void put(String element, int valid, double[][] data)
	{

		if (this.getMap(element) != null)
		{
			this.getMap(element).put(valid, data);
		}
	}




	/**
	 * 
	 * @Description: 处理最高温 最低温
	 * @return int
	 * @throws
	 */
	public synchronized int doTmaxorTmin(UniObject uniObject, String element)
	{
		int val = 0;
		try
		{
			for (Integer valid:this.getMap(element).keySet())
			{
				if (valid % 24 == 0 && valid != 0)
				{
					if (this.getMap(element).containsKey(valid)&& this.getMap(element).containsKey(valid-3) 
							&& this.getMap(element).containsKey(valid-6) && this.getMap(element).containsKey(valid-9)
							&& this.getMap(element).containsKey(valid-12)&& this.getMap(element).containsKey(valid-15)
							&& this.getMap(element).containsKey(valid-18)&& this.getMap(element).containsKey(valid-21))
					{
						val = valid;
						List<double[][]> list = getMaxorMin(this.getMap(element).get(valid),this.getMap(element).get(valid-3),this.getMap(element).get(valid-6),this.getMap(element).get(valid-9),this.getMap(element).get(valid-12),this.getMap(element).get(valid-15),this.getMap(element).get(valid-18),this.getMap(element).get(valid-21),element,valid);
						if (list != null)
						{
							uniObject.setValue("Z_", list.get(0));
							uniObject.setValue("dataTimes", list.get(1));
							uniObject.setValue("valid_", val);
							uniObject.setValue("validtime", val);
						}
						
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.error("ZJ:"+ element +" Specialized Processing error , error :" + e);
		}
		return val;
		
	}



	/**
	 * 
	 * @Description: 获取最高温最低温数组
	 * @return double[][]
	 * @throws
	 */
	private List<double[][]> getMaxorMin(double[][] d8, double[][] d7, double[][] d6, double[][] d5, double[][] d4, double[][] d3, double[][] d2, double[][] d1,String element,int valid)
	{
		List<double[][]> list = null;
		
		try
		{
			double[][] data = new double[d8.length][d8[0].length];
			//记录最高最低温出现的时效
			double[][] dataTime = new double[d8.length][d8[0].length];
			//把出现最高温最低温的时间默认为第一个时效
			for (int i = 0; i < dataTime.length; i++)
			{
				Arrays.fill(dataTime[i], valid-21 - 1);//3  -1使得出现的时间不在时效上，在时效中间
			}
			
			
			data = getMaxorMinValue(dataTime , d1   , d2 , element , valid - 18);//3 6
			data = getMaxorMinValue(dataTime , data , d3 , element , valid - 15);//9
			data = getMaxorMinValue(dataTime , data , d4 , element , valid - 12);//12
			data = getMaxorMinValue(dataTime , data , d5 , element , valid - 9);//15
			data = getMaxorMinValue(dataTime , data , d6 , element , valid - 6);//18
			data = getMaxorMinValue(dataTime , data , d7 , element , valid - 3);//21
			data = getMaxorMinValue(dataTime , data , d8 , element , valid);//24
			list = new ArrayList<double[][]>();
			
			list.add(data);
			list.add(dataTime);
		}
		catch (Exception e)
		{
			logger.error("ZJ: get "+ element +" data error , error : " + e);
		}
		
		return list;
	}


	/**
	 * 
	 * @Description: 比较产生最大最小值
	 * @return double[][]
	 * @throws
	 */
	private double[][] getMaxorMinValue(double[][] dataTime , double[][] d1, double[][] d2,String element , int valid)
	{
		
		double[][] data = null;
		try
		{
			int rows = d1.length;
			int clos = d1[0].length;
			data = new double[rows][clos];
			if ("TEM_Max".equals(element))			//最高温
			{
				for (int i = 0; i < rows; i++)
				{
					for (int j = 0; j < clos; j++)
					{
						if (d1[i][j] > d2[i][j]) //前一个时效值大  记录前一个时效
						{
							data[i][j] = d1[i][j];
						}
						else					//后一个时效值大	 记录后一个时效
						{
							data[i][j] = d2[i][j];
							dataTime[i][j] = valid - 1;
						}
					}
				}
			}
			else if(("TEM_Min".equals(element))) 								//最低温
			{
				for (int i = 0; i < rows; i++)
				{
					for (int j = 0; j < clos; j++)
					{
						if (d1[i][j] > d2[i][j]) // 前一个时效大  记录后一个时效值
						{
							data[i][j] = d2[i][j];
							dataTime[i][j] = valid - 1;
						}
						else					// 后一个时效大  记录前一个时效
						{
							data[i][j] = d1[i][j];
						}
					}
				}
			}
			else
			{
				return null;
			}
		}
		catch (Exception e)
		{
			logger.error("ZJ: Max Or Min data error , error : " + e);
		}
		return data;
	}


	/**
	 * 
	 * @Description: 移除已经处理之后的对象
	 * @return void
	 * @throws
	 */
	public void remove(String element, int key)
	{
		if (this.getMap(element) != null)
		{
			if (this.getMap(element).containsKey(key))
			{
				this.getMap(element).remove(key);
			}
		}
		
	}




	public synchronized void  putER(int key, UniObject uniObject)
	{
	   try
	{
		   String element = uniObject.getStringValue("element");
		   if ("ER03".equals(element))
		   {
			   //放入集合
			   ER03Map.put(key, uniObject);
			   valids.add(key);
			   //设置状态数组
				if (key <= 60)
				{
					status[key / 3] = 1;
				}
				else
				{
					status[key / 3 - (key - 60 ) /6] = 1;
				}
				//处理降水数据
			   getER03();
		   }
		   else if ("ER12".equals(element))
		   {
			   //放入集合
			   ER12Map.put(key, uniObject);
			   valids_12.add(key);
			 //设置状态数组
				if (key <= 60)
				{
					status[key / 3] = 1;
				}
				else
				{
					status[key / 3 - (key - 60 ) /6] = 1;
				}
				//处理降水数据
			   getER12();
		   }
		   else 
		   {
			   ER24Map.put(key, uniObject);
			   valids_24.add(key);
			 //设置状态数组
				if (key <= 60)
				{
					status[key / 3] = 1;
				}
				else
				{
					status[key / 3 - (key - 60 ) /6] = 1;
				}
				//处理降水数据
			   getER24();
		   }
	}
	catch (Exception e)
	{
		logger.error("ZJ: valid = " + key + "  ---- "  + " error . error : " + e);
	}
	}

	private void getER24()
	{
		Iterator<Integer> iterator = valids_24.iterator();
		while (iterator.hasNext())
		{
			int valid = iterator.next();
			if (valid == 0 )
			{
				iterator.remove();
				continue;
			}
			if (valid <= 60)
			{
				if (status[valid / 3 - 8] == 1)
				{
					if (ER03Map.containsKey(valid) && ER03Map.containsKey(valid - 24))
					{
						doER03(valid , ER24Map.get(valid) , ER24Map.get(valid - 24));
						iterator.remove();
					}
				}
			}
			else
			{
				if (status[valid / 3 - (valid - 60) / 6 - 4 ] == 1)
				{
					if (ER03Map.containsKey(valid) && ER03Map.containsKey(valid - 24))
					{
						doER03(valid , ER24Map.get(valid) , ER24Map.get(valid - 24));
						iterator.remove();
					}
				}
			}
		}
		
	}

	private void getER12()
	{
		Iterator<Integer> iterator = valids_12.iterator();
		while (iterator.hasNext())
		{
			int valid = iterator.next();
			if (valid == 0 )
			{
				iterator.remove();
				continue;
			}
			if (valid <= 60)
			{
				if (status[valid / 3 - 4] == 1)
				{
					if (ER03Map.containsKey(valid) && ER03Map.containsKey(valid - 12))
					{
						doER03(valid , ER12Map.get(valid) , ER12Map.get(valid - 12));
						iterator.remove();
					}
				}
			}
			else
			{
				if (status[valid / 3 - (valid - 60) / 6 - 2] == 1)
				{
					if (ER03Map.containsKey(valid) && ER03Map.containsKey(valid - 12))
					{
						doER03(valid , ER12Map.get(valid) , ER12Map.get(valid - 12));
						iterator.remove();
					}
				}
			}
			
		}
	}

	private void getER03()
	{
		try
		{
			Iterator<Integer> iterator = valids.iterator();
			
			while (iterator.hasNext())
			{
				int valid = iterator.next();
				if (valid == 0 )
				{
					iterator.remove();
					continue;
				}
				if (valid <= 60)
				{	
					if (status[valid / 3 - 1] == 1)
					{
						if (ER03Map.containsKey(valid) && ER03Map.containsKey(valid - 3))
						{
							doER03(valid , ER03Map.get(valid) , ER03Map.get(valid - 3));
							iterator.remove();
						}
					}
				}
				else
				{
					if (status[valid / 3 - (valid - 60) / 6 ] == 1)
					{
						if (ER03Map.containsKey(valid) && ER03Map.containsKey(valid - 6))
						{
							doER03(valid , ER03Map.get(valid) , ER03Map.get(valid - 6));
							iterator.remove();
						}
					}
				}
				
			}
		}
		catch (Exception e)
		{
			logger.error(" error : " + e.getMessage());
		}
		
	}
	
	/**
	 * 
	 * @Description: uniObject_after（之后的时效数据-6时效）    uniObject_before（之前的时效数据-3时效）
	 * @return void
	 * @throws
	 */
	private void doER03(int valid , UniObject uniObject_after, UniObject uniObject_before)
	{
		double[][] data = getDateER03((double[][])uniObject_after.getValue("Z") , (double[][])uniObject_before.getValue("Z"));
		uniObject_after.setValue("Z_", data);
		uniObject_after.setValue("valid_", valid);
		/**
		 * 文件名  写nc数据
		 */
		String fileName = getFileName(uniObject_after);
		boolean flag = Netcdf.getInstance().writeNetcdf(uniObject_after);
		/**
		 * 保存到数据库 
		 */
		if (flag)
		{
//			saveData(uniObject_after, fileName, uniObject_after.getStringValue("_forcastDate"));
			SaveHelper.getInstance().saveData(uniObject_after.clone());
		}
		else
		{
			logger.error("ZJ:write Netcdf fail , fileName = " + fileName);
		}
	}
	
	/**
	 * 
	 * @Description: 获取nc文件输出路径和文件名
	 * @return String
	 * @throws
	 */
	private String getFileName(UniObject uniObject)
	{
		String fileName = null;
		try
		{
			String filePath1 = uniObject.getStringValue("rootpathLocal") +  "/" + uniObject.getStringValue("origin_val") + "/" + uniObject.getStringValue("element") + "/"  + uniObject.getStringValue("forcastDate").substring(0, 8) + "/" + uniObject.getStringValue("sequrence") + "/" +  uniObject.getStringValue("level") + "/" + uniObject.getStringValue("forecast_fileversion");
			File file2 = new File(filePath1);
			if (!file2.exists())
			{
				file2.mkdirs();
			}
			fileName =  uniObject.getStringValue("forcastDate").substring(0, 10) + "_" + uniObject.getStringValue("valid_")+ ".nc";
			uniObject.setStringValue("filePath1", filePath1 + "/" + fileName);
			uniObject.setValue("fileName", fileName);
		}
		catch (Exception e)
		{
			logger.error("ZJ:getFileName error , error : " + e);
		}
		return fileName;
	}

	/**
	 * 
	 * @Description: 保存数据
	 * @return int
	 * @throws
	 */
//	private void saveData(UniObject uniObject,String fileName , String forcastDate)
//	{
//		int res = 0;
//		try
//		{
//			UniObject uniObject1 = GridForcast.getInstance().getGridForcastData(uniObject.getStringValue("element") , uniObject.getStringValue("origin_val"));
////			System.out.println(uniObject1);
//			uniObject1.setStringValue("filePath", uniObject.getStringValue("filePath1"));
//			uniObject1.setStringValue("fileName", fileName);
//			uniObject1.setStringValue("forcastDate", forcastDate);
//			uniObject1.setStringValue("fileversion", "ecthin");
//			uniObject1.setStringValue("forecast_date", forcastDate);
////			System.out.println(uniObject1);
//			res = new GridForcastService().savaGridForcast(uniObject1, uniObject.getStringValue("sequrence"), uniObject.getStringValue("valid_"),uniObject.getStringValue("level"));
////			System.out.println("插入影响行数 ： " + res);
//			logger1.info("ZJ:==========element : "+ uniObject.getStringValue("element") +" , forecast_date : "+forcastDate+" sequrence : " + uniObject.getStringValue("sequrence") + " , valid : " + uniObject.getStringValue("valid_") + " , level : " + uniObject.getStringValue("level") + " save Number of affected rows : " + res);		}
//		catch (Exception e)
//		{
//			logger.error("ZJ:save data is error , error : " + e);
//		}
//	}
	
	/**
	 * 
	 * @Description: value_aftre后一个时效的数据    value_before前一个时效的数据
	 * @return double[][]
	 * @throws
	 */
	private double[][] getDateER03(double[][] value_aftre, double[][] value_before)
	{
		double[][] data = null;
		try
		{
			int cols = 0 , rows = 0;
			if (value_aftre != null && value_before != null)
			{
				cols = value_aftre[0].length;
				rows = value_aftre.length;
			}
			data = new double[rows][cols];
			for (int i = 0; i < rows; i++)
			{
				for (int j = 0; j < cols; j++)
				{
					data[i][j] = value_aftre[i][j] - value_before[i][j];
				}
			}
		}
		catch (Exception e)
		{
			logger.error("get ER03 data error , error :" + e);
		}
		return data;
	}
	
	
	
}
