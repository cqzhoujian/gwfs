package com.supermap.gwfs.synchronizer.micaps4grid.specialized;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.synchronizer.micaps4grid.local.MicapsToLocal;
import com.supermap.gwfs.synchronizer.micaps4grid.netcdf.Netcdf;
import com.supermap.gwfs.synchronizer.micaps4grid.save.SaveHelper;

/**  
 * @Description: 相对湿度处理
 * @author zhoujian
 * @date 2017-3-2
 * @version V1.0 
 */
public class RelativeHumidity
{
	private Logger logger = LoggerFactory.getLogger("RelativeHumidity"); 
	private static RelativeHumidity relativeHumidity = null;
	private static String flag = null;	//标记  用来标记是否清除内存数据
	private static Map<Integer, UniObject> D2Map = null;
	private static Map<Integer, UniObject> T2Map = null;
	private static final String element = "ERH";
	
	private RelativeHumidity()
	{
		D2Map = new HashMap<Integer, UniObject>();
		T2Map = new HashMap<Integer, UniObject>();
	}
	
	public static synchronized RelativeHumidity getInstance(String forecastDate , String sequrence)
	{
		if (relativeHumidity == null)
		{
			flag = forecastDate + "-" + sequrence;
			relativeHumidity = new RelativeHumidity();
		}
		else
		{
			if (!(forecastDate + "-" + sequrence).equals(flag))
			{
				flag = forecastDate + "-" + sequrence;
				//清空内存数据
				D2Map.clear();
				T2Map.clear();
			}
		}
		return relativeHumidity;
	}
	
	public void put(UniObject uniObject)
	{
		if ("2T".equals(uniObject.getStringValue("element")))
		{
			T2Map.put(uniObject.getIntegerValue("valid_"), uniObject.clone());
		}
		else if ("2D".equals(uniObject.getStringValue("element")))
		{
			D2Map.put(uniObject.getIntegerValue("valid_"), uniObject.clone());
		}
		else {
			logger.error("ZJ: Not Legal Element , Element : " + uniObject.getStringValue("element"));
		}
		
		this.get();
	}

	private void get()
	{
		Iterator<Integer> it = T2Map.keySet().iterator();
		while (it.hasNext())
		{
			int valid = it.next();
			if (D2Map.containsKey(valid))
			{
				//处理数据
				doRelative(D2Map.get(valid).clone(), T2Map.get(valid).clone());
				//移除已经处理的时效数据
				it.remove();
				D2Map.remove(valid);
			}
		}
		
			
		
	}
	/**
	 * 
	 * @Description: 处理相对湿度
	 * @param uniObject_2d 露点温度
	 * @param uniObject_2t 温度
	 * @return void
	 * @throws
	 */
	private void doRelative(UniObject uniObject_2d , UniObject uniObject_2t)
	{
		// 2D露点温度
		double[][] data_2D = (double[][])uniObject_2d.getValue("Z_");
		// 实际水气压e
		double[][] pressure_e = this.pressure(data_2D);
		// 2T温度
		double[][] data_2T = (double[][])uniObject_2t.getValue("Z_");
		// 饱和水汽压E
		double[][] pressure_E = this.pressure(data_2T);
		//计算相对湿度
		double[][] data_ERH = this.humidity(pressure_e , pressure_E);
		
		//拼凑封装成UniObject数据
		UniObject uniObject = this.getUniObject(uniObject_2d , data_ERH);
		//设置文件名和文件路径
		this.setFilePathAndFileName(uniObject);
		//写文件
		boolean flag = Netcdf.getInstance().writeNetcdf(uniObject);
		if (flag)
		{
			//文件写成功之后入库
			SaveHelper.getInstance().saveData(uniObject);
			
			//市台
			MicapsToLocal.getInstance().doMicapsToLocal(uniObject.clone());
			
			//计算最大湿度和最低湿度
			RelativeHumidityTmaxOrTmin.getInstance(uniObject.getStringValue("_forcastDate"), uniObject.getStringValue("sequrence")).put(uniObject.clone());
		}
		
		
		
	}
	
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
	 * @Description: 把原始的数据对象重新封装成新的对象使用
	 * @return UniObject
	 * @throws
	 */
	private UniObject getUniObject(UniObject uniObject_2d , double[][] data)
	{
		//获取原始信息
		int lonCount = uniObject_2d.getIntegerValue("lonCount");
		int latCount = uniObject_2d.getIntegerValue("latCount");
		double[][] lons = (double[][])uniObject_2d.getValue("X");
		double[][] lats = (double[][])uniObject_2d.getValue("Y");
		String origin_val = uniObject_2d.getStringValue("origin_val");
		String forcastDate = uniObject_2d.getStringValue("_forcastDate");
		int time = uniObject_2d.getIntegerValue("time");
		int validTime = uniObject_2d.getIntegerValue("validtime");
		int valid = uniObject_2d.getIntegerValue("valid_");
		int level = uniObject_2d.getIntegerValue("level");
		String forecast_fileversion = uniObject_2d.getStringValue("forecast_fileversion");
		String unit = uniObject_2d.getStringValue("erh_unit");
		String rootpathLocal = uniObject_2d.getStringValue("rootpathLocal");
		String sequrence = uniObject_2d.getStringValue("sequrence");
		String date = uniObject_2d.getStringValue("forcastDate");
		System.out.println(uniObject_2d);
		
		//重新封装
		UniObject uniObject = new UniObject();
		uniObject.setValue("lonCount", lonCount);
		uniObject.setValue("latCount", latCount);
		uniObject.setValue("X", lons);
		uniObject.setValue("Y", lats);
		uniObject.setValue("origin_val", origin_val);
		uniObject.setValue("_forcastDate", forcastDate);
		uniObject.setValue("time", time);
		uniObject.setValue("validtime", validTime);
		uniObject.setValue("valid_", valid);
		uniObject.setValue("level", level);
		uniObject.setValue("forecast_fileversion", forecast_fileversion);
		uniObject.setValue("erh_unit", unit);
		uniObject.setValue("element", element);
		uniObject.setValue("Z_", data);
		uniObject.setValue("rootpathLocal", rootpathLocal);
		uniObject.setValue("sequrence", sequrence);
		uniObject.setValue("forcastDate", date);
		return uniObject.clone();
	}

	/**
	 * 
	 * @Description: 计算相对湿度
	 * @param pressure_e 实际水气压
	 * @param pressure_E 饱和水气压
	 * @return double[][]
	 * @throws
	 */
	private double[][] humidity(double[][] pressure_e, double[][] pressure_E)
	{
		double[][] result = null;
		if (pressure_e != null || pressure_E != null)
		{
			int rows = pressure_e.length;		//行
			int clos = pressure_e[0].length;	//列
			result = new double[rows][clos];
			for (int i = 0; i < rows; i++)
			{
				for (int j = 0; j < clos; j++)
				{
					//取整数
					result[i][j] = (int)Math.round(pressure_e[i][j] / pressure_E[i][j] * 100);
				}
			}
			
		}
		else
		{
			logger.error("ZJ: Relative Humidity Data Is Null.");
		}

		return result;
	}
	
	/**
	 * 
	 * @Description: 计算实际水气压或者饱和水气压
	 * @param 2D 露点温度--->实际水气压
	 * @param 2T 2m温度---> 饱和水气压
	 * @return double[][]
	 * @throws
	 */
	private double[][] pressure(double[][] data)
	{
		double[][] result = null;
		if (data != null)
		{
			int rows = data.length;		//行
			int clos = data[0].length;	//列
			result = new double[rows][clos];
			for (int i = 0; i < rows; i++)
			{
				for (int j = 0; j < clos; j++)
				{
					double t =  data[i][j];
					result[i][j] = 6.112 * Math.exp(17.67 * t / (t + 243.5));
				}
			}
		}
		else
		{
			logger.error("ZJ: Pressure Data Is Null .");
		}
		return result;
	}
	
}
