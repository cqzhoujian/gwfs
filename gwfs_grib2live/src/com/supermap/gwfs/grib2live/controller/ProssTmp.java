package com.supermap.gwfs.grib2live.controller;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.grib2live.service.WriteNCService;
import com.supermap.gwfs.grib2live.util.DateUtil;
import com.supermap.gwfs.grib2live.util.Logger;

/**  
 * @Description: 比较实况温度产生最高温和最低温
 * @author zhoujian
 * @date 2017-2-9
 * @version V1.0 
 */
public class ProssTmp
{
	private Logger logger = Logger.getLogger(this.getClass());
	private static ProssTmp prossTmp = null;
	private static float TmaxValue[] = new float[111 * 91];
	private static float TminValue[] = new float[111 * 91];
	private WriteNCService ncService = new WriteNCService();
	private ProssTmp()
	{
		Arrays.fill(TmaxValue, -50);
		Arrays.fill(TminValue, 50);
	}
	
	public static synchronized ProssTmp getInstance()
	{
		if (prossTmp == null)
		{
			prossTmp = new ProssTmp();
		}
		
		return prossTmp;
	}
	
	public void arrayInit(String element)
	{
		if ("TMAX".equals(element))
		{
			Arrays.fill(TmaxValue, -50);
		}
		else if ("TMIN".equals(element)) 
		{
			Arrays.fill(TminValue, 50);
		}
	}
	
	public void TmpComparison(UniObject uniObject)
	{
		try
		{
			logger.info("TMP Comparison : " + uniObject.getIntegerValue("hour") + "小时实况。");
			TmaxComparison("TMAX", uniObject.clone());
			TminComparison("TMIN", uniObject.clone());
			
		}
		catch (Exception e)
		{
			logger.error("ZJ:TMP comparison is error , error : " + e);
		}
		
	}
	
	private void TminComparison(String element , UniObject uniObject)
	{
		try
		{
			float[] data = (float[])uniObject.getValue("dValue");
			String filePath = uniObject.getStringValue("filePath").replace("TMP", "TMIN");
//			String date = uniObject.getStringValue("date");//2017-02-14
			
			String fileName = DateUtil.StringToString(uniObject.getStringValue("date"),"yyyy-MM-dd", "yyyyMMdd") +"_0.nc";
			filePath = filePath.substring(0, filePath.lastIndexOf("/")) ;
			File file = new File(filePath);
			if (!file.exists())
			{
				file.mkdirs();
			}
			
			TminValue = compare(data , TminValue ,"MIN");
			uniObject.setValue("filePath", filePath + "/" + fileName);
			Map<String, float[]> elementValue = new HashMap<String, float[]>();
			elementValue.put(element, TminValue);
			uniObject.setValue("elementValue", elementValue);
			
			
			boolean isWrite = writeNC(uniObject.clone());
			logger.info(filePath + "/" + fileName + " write of status : " + isWrite);
		}
		catch (Exception e)
		{
			logger.error("ZJ:Tmin live data is error , error : " + e);
		}
	}
	
	private void TmaxComparison(String element , UniObject uniObject)
	{
		try
		{
			float[] data = (float[])uniObject.getValue("dValue");
			String filePath = uniObject.getStringValue("filePath").replace("TMP", "TMAX");
			
			String fileName = DateUtil.StringToString(uniObject.getStringValue("date"),"yyyy-MM-dd", "yyyyMMdd") +"_0.nc";
			filePath = filePath.substring(0, filePath.lastIndexOf("/")) ;
			File file = new File(filePath);
			if (!file.exists())
			{
				file.mkdirs();
			}
			
			TmaxValue = compare(data , TmaxValue ,"MAX");
			if (TmaxValue != null)
			{
				uniObject.setValue("filePath", filePath + "/" + fileName);
				Map<String, float[]> elementValue = new HashMap<String, float[]>();
				elementValue.put(element, TmaxValue);
				uniObject.setValue("elementValue", elementValue);
				boolean isWrite = writeNC(uniObject.clone());
				logger.info("ZJ:" + filePath + "/" + fileName + " write of status : " + isWrite);
			}
		}
		catch (Exception e)
		{
			logger.error("ZJ:Tmax live data is error , error : " + e);
		}
	}

	private boolean writeNC(UniObject uniObject) {
		int forecastDate = uniObject.getIntegerValue("forecast");
		String unit = uniObject.getStringValue("unit");
		int level = uniObject.getIntegerValue("level");
		int cols = uniObject.getIntegerValue("cols");
		int rows = uniObject.getIntegerValue("rows");
		float left = uniObject.getFloatValue("left");
		float bottom = uniObject.getFloatValue("bottom");
		float dx = uniObject.getFloatValue("deltaX");
		float dy = uniObject.getFloatValue("deltaY");
		// float[] dvalue = gridsData.getdValue();
		String path = uniObject.getStringValue("filePath");

		Map<String, float[]> elementValue = (Map<String, float[]>)uniObject.getValue("elementValue");
		//20160913
		return  ncService.writeData(path, elementValue, level, left, bottom, dx, dy, forecastDate, cols, rows, unit);
	}
	
	private float[] compare(float[] data, float[] tmpvalue , String comparisonRule )
	{
		int length = data.length; 	//行
		
		float[] result = null;
		try
		{
			result = new float[length];
			if ("MAX".equals(comparisonRule))
			{
				for (int i = 0; i < length; i++)
				{
					if (data[i] > tmpvalue[i])
					{
						result[i] = data[i];
					}
					else
					{
						result[i] = tmpvalue[i];
					}
				}
			}
			else if ("MIN".equals(comparisonRule))
			{
				for (int i = 0; i < length; i++)
				{
					if (data[i] < tmpvalue[i])
					{
						result[i] = data[i];
					}
					else
					{
						result[i] = tmpvalue[i];
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return result;
	}
}
