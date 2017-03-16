package com.supermap.gwfs.rainproduct.correction;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.rainproduct.gridforcast.GridForcast;
import com.supermap.gwfs.rainproduct.netcdf.Netcdf;
import com.supermap.gwfs.rainproduct.service.GridForcastService;
import com.supermap.gwfs.rainproduct.util.ConvertDate;
import com.supermap.gwfs.rainproduct.util.ReadConfig;


/**  
 * @Description: 计算累积降水
 * @author zhoujian
 * @date 2017-2-22
 * @version V1.0 
 */
@SuppressWarnings("static-access")
public class AccumulativeRainfull
{
	private Logger logger = LoggerFactory.getLogger("AccumulativeRainfull");
	private static AccumulativeRainfull accumulative = null;
	private static String flag = null;	//标记--数据是否属于一个时次，属于则不会去清空内存中的数据，不属于则清空内存数据，重新设置标记
	private static Map<Integer, UniObject> ER12Map = null;	//12小时累积降水Map<时效 , UniObject数据>
	private static Map<Integer, UniObject> ER24Map = null;	//24小时累积降水Map<时效 , UniObject数据>
	private static UniObject uniObject_validParam = null;
	
	static
	{
		uniObject_validParam = ReadConfig.getInstance().getUniObject_valid();
	}
	
	private AccumulativeRainfull()
	{
		ER12Map = new HashMap<Integer, UniObject>();
		ER24Map = new HashMap<Integer, UniObject>();
	}
	
	public static synchronized AccumulativeRainfull getInstance(String forecastDate , String sequrence)
	{
		if (accumulative == null)	//开始启动程序
		{
			flag = forecastDate + "_" + sequrence;
			accumulative = new AccumulativeRainfull();
		}
		else	
		{
			if (!(forecastDate + "_" + sequrence).equals(flag))//不同日期不同时次
			{
				flag = forecastDate + "_" + sequrence;
				//清空内存
				ER12Map.clear();
				ER24Map.clear();
			}
		}
		return accumulative;
	}
	
	/**
	 * 
	 * @Description: put每个时效的降水数据
	 * @return void
	 * @throws
	 */
	public synchronized void put(UniObject uniObject)
	{
		int valid = uniObject.getIntegerValue("valid");
		ER12Map.put(valid , uniObject.clone());
		ER24Map.put(valid , uniObject.clone());
		logger.debug("ZJ:" + valid +" 时效的降水数据放入Map!");
		doER03();
	}
	
	private void doER03()
	{
		doER12();
		doER24();
	}
	/**
	 * 
	 * @Description: 处理24小时累积降水
	 * @return void
	 * @throws
	 */
	private void doER24()
	{
		try
		{
			String element = "ER24";
			//遍历时效
			Iterator<Integer> it = ER24Map.keySet().iterator();
			//判断时效在那个step中，文件个数
			while (it.hasNext())
			{
				int valid = it.next();	
				if (valid % 24 == 0)	//24小时累计降水的结束时效
				{
					int[] validSplit1 = splitValid(uniObject_validParam.getStringValue("ER24step1"));
					int[] validSplit2 = splitValid(uniObject_validParam.getStringValue("ER24step2"));
					
					int startValid = valid - 24;//24小时累计降水的开始时效
					int interval1 = 0; // 第一段的时效间隔
					int interval2 = 0; // 第二段的时效间隔 
					
					
					//0-60-3 60-240-6
					//判断开始和结束时效分别在哪一个分段,得到时效之间的间隔
					if (startValid >= validSplit1[0] && startValid <= validSplit1[1] && valid > validSplit1[0] && valid <= validSplit1[1])
					{
						//开始时效和结束时效都落在第一段 时效间隔3小时
						interval1 = validSplit1[2];
						interval2 = 0;
					}
					else if(startValid > validSplit2[0] && startValid <= validSplit2[1] && valid > validSplit2[0] && valid <= validSplit2[1])
					{
						//开始时效和结束时效都落在第二段，时效间隔6小时
						interval1 = 0;
						interval2 = validSplit2[2];
					}
					else
					{
						//开始时效落在第一段，时效间隔3小时。结束时效落在第二段，时效间隔3小时
						interval1 = validSplit1[2];
						interval2 = validSplit2[2];
					}
					
					//动态的获取到该24小时内的时效List
					List<Integer> valids = new ArrayList<Integer>();
					if (interval1 != 0 && interval2 ==0) //第一段 时效间隔3小时
					{
						for (int i = 0; i < 24 / interval1; i++)
						{
							valids.add(valid - i * interval1);
						}
					}
					else if(interval1 == 0 && interval2 != 0)//第二段 时效间隔6小时
					{
						for (int i = 0; i < 24 / interval2; i++)
						{
							valids.add(valid - i * interval2);
						}
					}
					else if (interval1 != 0 && interval2 != 0)
					{
						for (int i = 0; i < (validSplit1[1] - startValid) / interval1 ; i++)
						{
							valids.add(validSplit1[1] - i * interval1);
						}
						for (int i = 0; i < (valid - validSplit1[1]) / interval2 ; i++)
						{
							valids.add(valid - i * interval2);
						}
					}
					else
					{
						logger.error("ZJ: Valid Out Of Range !");
						return ;
					}
					
					//判断ER24集合中是否存在该24小时内的所有时效，存在则获取数据数组，不存在则跳出方法
					List<double[][]> validDataList = new ArrayList<double[][]>();
					if (valids.size() > 0)
					{
						for (Integer v : valids)
						{
							if (ER24Map.containsKey(v))
							{
								validDataList.add((double[][])ER24Map.get(v).getValue("Z"));
							}
							else
							{
								logger.error("ZJ: " + valid +" hours missing in " + v + " !");
								return ;
							}
						}
						double[][] data = getValidDataSum(validDataList);
						
						//写nc
						UniObject u = ER24Map.get(valid);
						u.setValue("element", element);
						u.setValue("Z", data);
						String forecast_tmp = ConvertDate.stringToString(u.getStringValue("forcast_date"), "yyyy-MM-dd", "yyyyMMdd");
						String fileName = forecast_tmp + u.getStringValue("sequrence")+"_" + valid + ".nc";
						String filePath = u.getStringValue("rootLocalPath") + "/" + u.getStringValue("origin_val") + "/" + element + "/" + forecast_tmp + "/" + u.getStringValue("sequrence") + "/0/" +u.getStringValue("fileversion"); 
						File file = new File(filePath);
						if (!file.exists())
						{
							file.mkdirs();
						}
						//写netcdf文件
						boolean flag = Netcdf.getInstance().WriteNetcdf(u, filePath + "/" + fileName);
						if (flag)
						{
							System.out.println(filePath+"/"+fileName +" 写文件成功！");
							it.remove();
							//DB
							this.saveData(element , fileName , filePath , valid, u.clone());
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.error("ZJ: 24 Hours Cumulative Precipitation Treatment Is Exception , Exception : " + e);
		}
	}
	/**
	 * 
	 * @Description: 处理12小时降水数据
	 * @return void
	 * @throws
	 */
	private void doER12()
	{
		try
		{
			String element = "ER12";
			//遍历时效
			Iterator<Integer> it = ER12Map.keySet().iterator();
			//判断时效在那个step中，文件个数
			while (it.hasNext())
			{
				int valid = it.next();
				if (valid % 12 == 0)	
				{
					int[] validSplit1 = splitValid(uniObject_validParam.getStringValue("ER12step1"));
					int[] validSplit2 = splitValid(uniObject_validParam.getStringValue("ER12step2"));
					//获取时效间隔
					int interval = 0;
					if (valid > validSplit1[0] && valid <= validSplit1[1])
					{
						interval = validSplit1[2];	//时效之间的间隔
						
					}
					else if (valid > validSplit2[0] && valid <= validSplit2[1])
					{
						interval = validSplit2[2];	//时效之间的间隔
					}
					else
					{
						logger.error("ZJ: Valid Out Of Range!");
						return ;
					}
					
					//当前的12小时内的所有时效的list
					List<Integer> valids = new ArrayList<Integer>();
					for (int i = 0; i < 12 / interval; i++)
					{
						valids.add(valid - i * interval);
					}
					//判断ER12集合中是否存在所需时效，存在则把所有时效的数据取出放在数据list中，不存在则退出方法
					List<double[][]> validDataList = new ArrayList<double[][]>();
					for (Integer v : valids)
					{
						if (ER12Map.containsKey(v))
						{
							validDataList.add((double[][]) ER12Map.get(v).getValue("Z"));
						}
						else 
						{
							logger.error("ZJ: " + valid +" hours missing in " + v + " !");
							return ;
						}
					}
					//程序运行就具备了累积12小时降水的条件了
					//data为所有时效数据的累和
					double[][] data = getValidDataSum(validDataList); 
					UniObject u = ER12Map.get(valid);
					u.setValue("element", element);
					u.setValue("Z", data);
					String forecast_tmp = ConvertDate.stringToString(u.getStringValue("forcast_date"), "yyyy-MM-dd", "yyyyMMdd");
					String fileName = forecast_tmp + u.getStringValue("sequrence")+"_" + valid + ".nc";
					String filePath = u.getStringValue("rootLocalPath") + "/" + u.getStringValue("origin_val") + "/" + element + "/" + forecast_tmp + "/" + u.getStringValue("sequrence") + "/0/" +u.getStringValue("fileversion"); 
					File file = new File(filePath);
					if (!file.exists())
					{
						file.mkdirs();
					}
					
					//写netcdf文件
					boolean flag = Netcdf.getInstance().WriteNetcdf(u, filePath + "/" + fileName);
					if (flag)
					{
						System.out.println(filePath+"/"+fileName +" 写文件成功！");
						it.remove();
						//DB
						this.saveData(element, fileName, filePath, valid, u.clone());
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.error("ZJ: 12 Hours Cumulative Precipitation Treatment Is Exception , Exception : " + e);
		}
	}
	
	/**
	 * 
	 * @Description: 数据保存
	 * @return void
	 * @throws
	 */
	private void saveData(String element , String fileName , String filePath ,int valid , UniObject u)
	{
		
		try
		{
			UniObject ui = GridForcast.getInstance().queryGridForecast(u.getStringValue("origin_val") , element);
			ui.setStringValue("fileName", fileName);
			ui.setStringValue("filePath", filePath + "/" + fileName);
			ui.setValue("forcast_date", u.getStringValue("forcast_date"));
			ui.setValue("fileversion", u.getStringValue("fileversion"));
			System.out.println(ui);
			int result =new GridForcastService().savaGridForcast(ui, u.getStringValue("sequrence"), String.valueOf(valid));
			System.out.println(result);
		}
		catch (Exception e)
		{
			logger.error("ZJ: Save Data Is Exception , Exception : " + e);
		}
	}
	
	/**
	 * 
	 * @Description: 所有时效数据累和
	 * @return double[][]
	 * @throws
	 */
	private double[][] getValidDataSum(List<double[][]> validDataList)
	{
		double[][] result = null;
		int rows = 0;
		int clos = 0;
		if (validDataList.size() > 0)
		{
			rows = validDataList.get(0).length;
			clos = validDataList.get(0)[0].length;
		}
		try
		{
			result = new double[rows][clos];
			for (int i = 0; i < validDataList.size(); i++)
			{
				result = this.sum(result , validDataList.get(i));
			}
			
		}
		catch (Exception e)
		{
			logger.error("ZJ: All Valids Data Sum Exception , Exception : " + e);
		}
		return result;
	}
	/**
	 * 
	 * @Description: 两个数组累和
	 * @return double[][]
	 * @throws
	 */
	private double[][] sum(double[][] d1, double[][] d2)
	{
		double[][] result = null;
		if (d1 == null || d2 == null)
		{
			return null;
		}
		int rows = d1.length;
		int clos = d1[0].length;
		try
		{
			result = new double[rows][clos];
			for (int i = 0; i < rows; i++)
			{
				for (int j = 0; j < clos; j++)
				{
					result[i][j] = d1[i][j] + d2[i][j];
				}
			}
			
		}
		catch (Exception e)
		{
			logger.error("ZJ: Two Array Sum Exceptions , Exception : " + e);
		}
		return result;
		
	}

	/**
	 * 
	 * @Description: 分割时效参数
	 * @param validParam  0-60-3
	 * @return int[]
	 * @throws
	 */
	private int[] splitValid(String validParam)
	{
		int[] validSplit = null;
		try
		{
			String[] validSplitTmp = validParam.split("-");
			validSplit = new int[validSplitTmp.length];
			for (int i = 0; i < validSplitTmp.length; i++)
			{
				validSplit[i] = Integer.valueOf(validSplitTmp[i]);
			}
		}
		catch (Exception e)
		{
			logger.error("ZJ: Split Valid Exception , Exception : " + e);
		}
		return validSplit;
	}
}
