package com.supermap.gwfs.synchronizer.micaps4grid.specialized;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.synchronizer.micaps4grid.local.MicapsToLocal;
import com.supermap.gwfs.synchronizer.micaps4grid.netcdf.Netcdf;
import com.supermap.gwfs.synchronizer.micaps4grid.save.SaveHelper;
import com.supermap.gwfs.synchronizer.micaps4grid.util.ReadXML;

/**  
 * @Description: 最大湿度最小湿度处理
 * @author zhoujian
 * @date 2017-3-3
 * @version V1.0 
 */
public class RelativeHumidityTmaxOrTmin
{
	private Logger logger = LoggerFactory.getLogger("RelativeHumidityTmaxOrTmin");
	private static RelativeHumidityTmaxOrTmin relative = null;
	private static String flag = null;	//标记--数据是否属于一个时次，属于则不会去清空内存中的数据，不属于则清空内存数据，重新设置标记
	private static Map<Integer, UniObject> ERHMap = null;	//存储ERHMap<时效 , UniObject数据>
	private static UniObject uniObject_validParam = null;
	
	static
	{
		uniObject_validParam = ReadXML.getValidParameter();
	}
	
	private RelativeHumidityTmaxOrTmin()
	{
		ERHMap = new HashMap<Integer, UniObject>();
	}
	
	public static synchronized RelativeHumidityTmaxOrTmin getInstance(String forecastDate , String sequrence)
	{
		if (relative == null)	//开始启动程序
		{
			flag = forecastDate + "-" + sequrence;
			relative = new RelativeHumidityTmaxOrTmin();
		}
		else	
		{
			if (!(forecastDate + "-" + sequrence).equals(flag))//不同日期不同时次
			{
				flag = forecastDate + "-" + sequrence;
				//清空内存
				ERHMap.clear();
			}
		}
		return relative;
	}
	
	/**
	 * 
	 * @Description: put每个时效的降水数据
	 * @return void
	 * @throws
	 */
	public synchronized void put(UniObject uniObject)
	{
		int valid = uniObject.getIntegerValue("valid_");
		ERHMap.put(valid , uniObject.clone());
		logger.debug("ZJ:" + valid +" 时效的ERH数据放入Map!");
		doERH();
	}
	/**
	 * 
	 * @Description: 处理24小时累积降水
	 * @return void
	 * @throws
	 */
	private void doERH()
	{
		try
		{
			//遍历时效
			Iterator<Integer> it = ERHMap.keySet().iterator();
			//判断时效在那个step中，文件个数
			while (it.hasNext())
			{
				int valid = it.next();	
				if (valid % 24 == 0 && valid != 0)	//24小时累计降水的结束时效
				{ 
					int[] validSplit1 = splitValid(uniObject_validParam.getStringValue("ERHstep1"));
					int[] validSplit2 = splitValid(uniObject_validParam.getStringValue("ERHstep2"));
					
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
						//处理最小湿度
						boolean flag = false;
						flag = this.doERHIAndERHA(valids , validDataList , ERHMap.get(valid).clone() , "ERHI");
						//处理最大湿度
						flag = this.doERHIAndERHA(valids , validDataList , ERHMap.get(valid).clone() , "ERHA");
						if (flag)
						{
							it.remove();
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
	 * @Description: 
	 * @return boolean
	 * @throws
	 */
	private boolean doERHIAndERHA(List<Integer> valids, List<double[][]> validDataList , UniObject uniObject , String element)
	{
//		String element = "ERHI";
		boolean flag = false;
		for (Integer v : valids)
		{
			if (ERHMap.containsKey(v))
			{
				validDataList.add((double[][])ERHMap.get(v).getValue("Z_"));
			}
			else
			{
				logger.error("ZJ: missing in " + v + " !");
				return flag;
			}
		}
		double[][] data = getValidDataCompare(validDataList , element);
		
		uniObject.setValue("element", element);
		uniObject.setValue(element.toLowerCase()+"_unit", "%");
		uniObject.setValue("Z_", data);
		this.setFilePathAndFileName(uniObject);
		
		flag = Netcdf.getInstance().writeNetcdf(uniObject);
		if (flag)
		{
			SaveHelper.getInstance().saveData(uniObject.clone());
			//市台
			MicapsToLocal.getInstance().doMicapsToLocal(uniObject.clone());
		}
		return flag;
	}
	/**
	 * 
	 * @Description: 设置文件路径和文件名
	 * @return void
	 * @throws
	 */
	private void setFilePathAndFileName(UniObject uniObject)
	{
		try
		{
			//2017-03-03--> 20170303
			String forcastDate = uniObject.getStringValue("_forcastDate").replace("-", ""); 
			String sequrence = uniObject.getStringValue("sequrence");
			String fileName = forcastDate + sequrence + "_" + uniObject.getStringValue("valid_") + ".nc";
			String filePath = uniObject.getStringValue("rootpathLocal") + "/" + uniObject.getStringValue("origin_val") + "/" + uniObject.getStringValue("element") + "/" 
								+ forcastDate + "/" + sequrence + "/" + uniObject.getStringValue("level") + "/" 
								+ uniObject.getStringValue("forecast_fileversion");
			File dirs = new File(filePath);
			if (!dirs.exists() || !dirs.isDirectory())
			{
				dirs.mkdirs();
			}
			
			uniObject.setValue("fileName", fileName);
			uniObject.setValue("filePath1", filePath + "/" + fileName);
		}
		catch (Exception e)
		{
			logger.error("ZJ: Set ERH FilePath And FileName Is Error , Error : " + e);
		}
	}
	
	/**
	 * 
	 * @Description: 比较指定时效内的所有时效
	 * @return double[][]
	 * @throws
	 */
	private double[][] getValidDataCompare(List<double[][]> validDataList, String element)
	{
		double[][] result = null;
		try
		{
			if (validDataList.size() > 0)
			{
				int rows = validDataList.get(0).length;
				int clos = validDataList.get(0)[0].length;
				result = new double[rows][clos];
				if ("ERHI".equals(element))
				{
					// 初始化result的值要大
					for (int i = 0; i < rows; i++)
					{
						Arrays.fill(result[i], 100.0);
					}
				}
				
				for (double[][] data : validDataList)
				{
					result = this.validDataCompare(result , data , element);
				}
			}
		}
		catch (Exception e)
		{
			logger.error("ZJ: getValidDataCompare Is Error , Error : " + e);
		}
		return result;
	}
	
	/**
	 * 
	 * @Description: 比较两个数组
	 * @return double[][]
	 * @throws
	 */
	private double[][] validDataCompare(double[][] data1, double[][] data2 , String element)
	{
		double[][] result = null;
		try
		{
			if (data1 != null && data2 != null)
			{
				int rows = data1.length;
				int clos = data1[0].length;
				result = new double[rows][clos];
				for (int i = 0; i < rows; i++)
				{
					for (int j = 0; j < clos; j++)
					{
						if ("ERHI".equals(element))
						{
							//比较 取小
							if (data1[i][j] > data2[i][j])
							{
								result[i][j] = data2[i][j];
							}
							else
							{
								result[i][j] = data1[i][j];
							}
						}
						else
						{
							//比较 取大
							if (data1[i][j] > data2[i][j])
							{
								result[i][j] = data1[i][j];
							}
							else
							{
								result[i][j] = data2[i][j];
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.error("ZJ: Valid Data Compare Is Error , Error : " + e);
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
