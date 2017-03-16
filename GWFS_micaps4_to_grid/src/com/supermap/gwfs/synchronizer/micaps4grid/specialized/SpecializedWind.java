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
 * @Description: 处理要素风--U V两个变量
 * @author zhoujian
 * @date 2017-1-18
 * @version V1.0 
 */
public class SpecializedWind
{
	private Logger logger = LoggerFactory.getLogger("SpecializedWind");
	private static SpecializedWind specializedWind  = null;
	//用于存放U变量
	private static Map<String , UniObject> uMap = new HashMap<String, UniObject>();
	//用于存放V变量
	private static Map<String , UniObject> vMap = new HashMap<String, UniObject>();
	//标记 标记是否属于同一天同一个时次的数据，如果不是同一天同一个时次的数据则对之前遗留在内存中的数据进行清空
	private static String flag = null;
	private SpecializedWind(){};
	
	public static synchronized SpecializedWind getInstance(String forecastDate ,String sequrence)
	{
		if (specializedWind == null)
		{
			flag = forecastDate + "_" + sequrence;
			specializedWind = new SpecializedWind();
			uMap.clear();
			vMap.clear();
		}
		else
		{
			if (!(forecastDate+"_"+sequrence).equals(flag))
			{
				flag = forecastDate + "_" + sequrence;
				vMap.clear();
				uMap.clear();
			}
		}
		return specializedWind;
	}
	/**
	 * 
	 * @Description: 根据不同的要素返回不同的Map
	 * @return Map<String,UniObject>
	 * @throws
	 */
	private Map<String , UniObject> getMap(String element)
	{
		if ("V".equals(element)||"10V".equals(element))
		{
			return vMap;
		}
		else if ("U".equals(element)||"10U".equals(element))
		{
			return uMap;
		}
		else
		{
			return null;
		}
	}
	/**
	 * 
	 * @Description: 根据不同要素把数据放在不同的Map中
	 * @return void
	 * @throws
	 */
	public void putWind(String element ,String forcastDate, String valid, String level,UniObject uniObject)
	{
		this.getMap(element).put(forcastDate + uniObject.getStringValue("sequrence") + valid + level, uniObject);
		getWind();
	}
	/**
	 * 
	 * @Description: 处理数据（同时向文件中写U V变量）
	 * @return void
	 * @throws
	 */
	private void getWind()
	{
		//遍历vMap
		Iterator<String> it = vMap.keySet().iterator();
		while(it.hasNext())
		{
			String key = it.next();
			if(uMap.containsKey(key))
			{
				//文件名
				String fileName = this.getFileName(vMap.get(key));
				//写文件
				//1.写U、V变量
				boolean flag = Netcdf.getInstance().writeNetcdf(vMap.get(key) , uMap.get(key));
				if (flag)
				{
					logger.info(fileName +" write is successful.");
					System.out.println(fileName +" write is successful.");
					
					//DB
					String element = "EDA10";
					vMap.get(key).setValue("element", element);
					SaveHelper.getInstance().saveData(vMap.get(key).clone());
					
					//ecthin数据处理之后把地面风数据作为市台风数据
					MicapsToLocal.getInstance().doMicapsToLocal(vMap.get(key) , uMap.get(key));
					
					//移除处理过后的时效   以免重复处理
					uMap.remove(key);
					it.remove();
				}
				else
				{
					logger.error(fileName + " write is failed");
				}
				
			}
		}
	}
	
	private String getFileName(UniObject uniObject)
	{
		String fileName = null;
		try
		{
			String filePath1 = uniObject.getStringValue("rootpathLocal") + "/" + uniObject.getStringValue("origin_val") +  "/" + "EDA10" + "/"  + uniObject.getStringValue("forcastDate").substring(0, 8) + "/" + uniObject.getStringValue("sequrence") + "/" +  uniObject.getStringValue("level") + "/" + uniObject.getStringValue("forecast_fileversion");
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
}
