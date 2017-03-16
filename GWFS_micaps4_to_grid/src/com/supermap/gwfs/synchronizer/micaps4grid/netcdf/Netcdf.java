package com.supermap.gwfs.synchronizer.micaps4grid.netcdf;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.NetcdfFileWriter.Version;
import ucar.nc2.Variable;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;

/**  
 * @Description: netcdf数据处理
 * @author zhoujian
 * @date 2016-11-29
 * @version V1.0 
 */
public class Netcdf
{
	private Logger logger = LoggerFactory.getLogger("NetcdfLog");
	private static Netcdf netcdf = null;
	private Netcdf()
	{
	}
	public static synchronized Netcdf getInstance()
	{
		if (netcdf == null)
		{
			netcdf = new Netcdf();
		}
		return netcdf;
	}
	
	public boolean writeNetcdf(UniObject uniObject)
	{
		boolean flag = false;
		try
		{
			String element = uniObject.getStringValue("element");
			String unit = uniObject.getStringValue(element.toLowerCase()+"_unit");
			int ww = uniObject.getIntegerValue("lonCount");
			int hh = uniObject.getIntegerValue("latCount");
			int time = uniObject.getIntegerValue("time");
			int level = uniObject.getIntegerValue("level");
			double[][] X = (double[][])uniObject.getValue("X");
			double[][] Y = (double[][])uniObject.getValue("Y");
			double[][] Z = (double[][])uniObject.getValue("Z_");
			String filePath = uniObject.getStringValue("filePath1");
			int valid = uniObject.getIntegerValue("validtime");
			
			//2016-12-09
			//最高温最低温出现的时效值
			double[][] dataTimes = null;
			if ("TEM_Max".equals(element) || "TEM_Min".equals(element))
			{
				dataTimes = (double[][])uniObject.getValue("dataTimes");
			}
			
			double[] xx = new double[X[0].length];
			double[] yy = new double[Y.length];
			
			for (int i = 0; i < ww; i++)
			{
				xx[i] = X[0][i];
			}
			
			for (int i = 0; i < hh; i++)
			{
				yy[i] = Y[i][0];
			}
			Map<String, double[][]> elementValue = new HashMap<String, double[][]>();
			elementValue.put(element, Z);
//			 flag = Write(element , unit , ww , hh , time , level , xx , yy , Z , filePath , dataTimes , valid);
			flag = Write(elementValue, unit, ww, hh, time, level, xx, yy, filePath, dataTimes, valid);
			
			
		}
		catch (Exception e)
		{
			logger.error("ZJ:get Netcdf data to write error , error : " + e);
		}
		return flag;
	}
	/*private boolean Write(String element ,String unit , int lonCount, int latCount, int time, int level, double[] xx, double[] yy, double[][] data, String filePath , double[][] dataTimes , int valid)
	{
		boolean flag = true;
		NetcdfFileWriter ncfile = null;
		try
		{
			ncfile = NetcdfFileWriter.createNew(Version.netcdf3, filePath);
			
			
			//定义维度
			Dimension timeDim = ncfile.addDimension(null, "time", 1);
			Dimension validDim = ncfile.addDimension(null, "validtime", 1);
			Dimension levelDim = ncfile.addDimension(null, "level", 1);
			Dimension latDim = ncfile.addDimension(null, "latitude", latCount);
			Dimension lonDim = ncfile.addDimension(null, "longitude", lonCount);
			
			//定义变量
			Variable timeVar = ncfile.addVariable(null, "time", DataType.INT, "time");
			Variable validVar = ncfile.addVariable(null, "validtime", DataType.INT, "validtime");
			Variable levelVar = ncfile.addVariable(null, "level", DataType.INT,"level");
			Variable latVar = ncfile.addVariable(null, "latitude", DataType.FLOAT, "latitude");
			Variable lonVar = ncfile.addVariable(null, "longitude", DataType.FLOAT, "longitude");
			
			
			
			//定义属性
			ncfile.addVariableAttribute(timeVar, new Attribute("units", "hours since 1900-01-01 00:00:00"));
			ncfile.addVariableAttribute(timeVar, new Attribute("long_name", "time"));
			
			validVar.addAttribute(new Attribute("units", "hour"));
			validVar.addAttribute(new Attribute("long_name", validDim.getShortName()));
			ncfile.addVariableAttribute(levelVar, new Attribute("units", "millibars"));
			ncfile.addVariableAttribute(levelVar, new Attribute("long_name", "pressure_leve"));
			ncfile.addVariableAttribute(latVar, new Attribute("units", "degress_north"));
			ncfile.addVariableAttribute(lonVar, new Attribute("units", "degrees_east"));
		
			//定义数组
			ArrayInt.D1 times = new ArrayInt.D1(timeDim.getLength());
			ArrayInt.D1 valids = new ArrayInt.D1(validDim.getLength());
			ArrayInt.D1 levels = new ArrayInt.D1(levelDim.getLength());
			ArrayFloat.D1 lats = new ArrayFloat.D1(latDim.getLength());
			ArrayFloat.D1 lons = new ArrayFloat.D1(lonDim.getLength());
			
			int i;
			for (i = 0; i < timeDim.getLength(); i++)
			{
				times.set(i, time);
			}
			for (i = 0; i < validDim.getLength(); i++)
			{
				valids.set(i, valid);
			}
			for (i = 0; i < levelDim.getLength(); i++)
			{
				levels.set(i, level);
			}
			for (i = 0; i < latDim.getLength(); i++)
			{
				lats.set(i, (float)yy[i]);
			}
			for (i = 0; i < lonDim.getLength(); i++)
			{
				lons.set(i, (float)xx[i]);
			}
			
			//变量
			String dims = "time validtime level latitude longitude";
			Variable var = ncfile.addVariable(null, element, DataType.FLOAT, dims);
			ncfile.addVariableAttribute(var, new Attribute("units", unit));
			ArrayFloat.D5 arr = new ArrayFloat.D5( 1, 1, 1, latCount, lonCount);
			
			Variable var_TmaxOrTminTimes = null;
			ArrayFloat.D5 arr_times = null;
			if (dataTimes != null)
			{
				var_TmaxOrTminTimes = ncfile.addVariable(null, "dtimes", DataType.FLOAT, dims);
				ncfile.addVariableAttribute(var_TmaxOrTminTimes, new Attribute("remarks", "occurrence time"));
				arr_times = new ArrayFloat.D5(1, 1 ,1, latCount, lonCount);
			}
			
			
			ncfile.create();
			ncfile.write(levelVar, levels);
			ncfile.write(timeVar, times);
			ncfile.write(validVar, valids);
			ncfile.write(latVar, lats);
			ncfile.write(lonVar, lons);
			ncfile.flush();
			
			double v , dt;
			for (int t = 0; t < 1; t++)
			{
				for (int val = 0; val < validDim.getLength(); val++)
				{	
					for (int lvl = 0; lvl < 1; lvl++) 
					{
						for (int latude = 0; latude < latCount; latude++) //行 
						{
							for (int longidute = 0; longidute < lonCount; longidute++)//列 
							{
								v = data[latude][longidute];
								arr.set( t, val ,lvl, latude, longidute, (float) v);
								if (dataTimes != null)
								{
									dt = dataTimes[latude][longidute];
									arr_times.set(t, val, lvl, latude, longidute, (float)dt);	
								}
							}
						}
					}
				}
			}
		ncfile.write(var, arr);
		if (dataTimes != null)
		{
			ncfile.write(var_TmaxOrTminTimes, arr_times);
		}
		ncfile.flush();
			
		}
		catch (Exception e)
		{
			logger.error("ZJ:writed  " + filePath + " file error , error : " + e);
			flag = false;
		}
		
		return flag;
	}*/
	public boolean writeNetcdf(UniObject uniObject_v, UniObject uniObject_u)
	{
		boolean flag = false;
		String element_v = null; //变量V
		String element_u = null; //变量U
		try
		{
			if ("V".equals(uniObject_v.getStringValue("element").toUpperCase()) || "10V".equals(uniObject_v.getStringValue("element").toUpperCase()))
			{
				element_v = "VEDA10";
			}
			if ("U".equals(uniObject_u.getStringValue("element").toUpperCase()) || "10U".equals(uniObject_u.getStringValue("element").toUpperCase()))
			{
				element_u = "UEDA10";
			}
			String unit = uniObject_v.getStringValue(element_v.toLowerCase()+"_unit");
			int lonCount = uniObject_v.getIntegerValue("lonCount");
			int latCount = uniObject_v.getIntegerValue("latCount");
			int time = uniObject_v.getIntegerValue("time");
			int level = uniObject_v.getIntegerValue("level");
			double[][] X = (double[][])uniObject_v.getValue("X");
			double[][] Y = (double[][])uniObject_v.getValue("Y");
			//不用插值
			double[][] Z_V = (double[][])uniObject_v.getValue("Z_");
			String filePath = uniObject_v.getStringValue("filePath1");
			int valid = uniObject_v.getIntegerValue("validtime");
			
			double[] lons = new double[X[0].length];
			double[] lats = new double[Y.length];
			
			for (int i = 0; i < lonCount; i++)
			{
				lons[i] = X[0][i];
			}
			
			for (int i = 0; i < latCount; i++)
			{
				lats[i] = Y[i][0];
			}
			
			double[][] Z_U = (double[][])uniObject_u.getValue("Z_");
			
			Map<String, double[][]> elementValue = new HashMap<String, double[][]>();
			elementValue.put(element_u, Z_U);
			elementValue.put(element_v, Z_V);
//			float dx = uniObject_v.getFloatValue("dx");
//			float dy = uniObject_v.getFloatValue("dy");
//			String filePath = uniObject_v.getStringValue("filePath1");
//			int valid = uniObject_v.getIntegerValue("validtime");
//			
//			int lonCount =  (int) (((uniObject_v.getDoubleValue("endLon") - uniObject_v.getDoubleValue("startLon"))/dx) + 1);
//			int latCount =  (int) (((uniObject_v.getDoubleValue("endLat") - uniObject_v.getDoubleValue("startLat"))/dy) + 1);
//			int time = uniObject_v.getIntegerValue("time");
//			int level = uniObject_v.getIntegerValue("level");
//			double[] lons = new double[lonCount];
//			double startLon = uniObject_v.getDoubleValue("startLon");
//			for (int i = 0; i < lonCount; i++)
//			{
//				lons[i] = startLon;
//				startLon += dx;
//			}
//			
//			double[] lats = new double[lonCount];
//			double startLat = uniObject_v.getDoubleValue("startLat");
//			for (int i = 0; i < lonCount; i++)
//			{
//				lats[i] = startLat;
//				startLat += dx;
//			}
//			Map<String, double[][]> elementValue = new HashMap<String, double[][]>();
//			double[][] Z_U = (double[][])uniObject_u.getValue("Z");
//			double[][] Z_V = (double[][])uniObject_v.getValue("Z");
//			elementValue.put(element_u, Z_U);
//			elementValue.put(element_v, Z_V);
			
			
			flag = Write(elementValue , unit , lonCount , latCount , time , level , lons , lats , filePath , null, valid);
			
			
		}
		catch (Exception e)
		{
			logger.error("ZJ:get Netcdf data to write error , error : " + e);
		}
		return flag;
	}
	private boolean Write(Map<String, double[][]> elementValue, String unit, int lonCount, int latCount, int time, int level, double[] xx, double[] yy, String filePath, double[][] dataTimes , int valid)
	{
		boolean flag = true;
		NetcdfFileWriter ncfile = null;
		try
		{
			ncfile = NetcdfFileWriter.createNew(Version.netcdf3, filePath);
			
			//定义维度
			Dimension timeDim = ncfile.addDimension(null, "time", 1);
			Dimension validDim = ncfile.addDimension(null, "validtime", 1);
			Dimension levelDim = ncfile.addDimension(null, "level", 1);
			Dimension latDim = ncfile.addDimension(null, "latitude", latCount);
			Dimension lonDim = ncfile.addDimension(null, "longitude", lonCount);
			//定义变量
			Variable timeVar = ncfile.addVariable(null, "time", DataType.INT, "time");
			Variable validVar = ncfile.addVariable(null, "validtime", DataType.INT, "validtime");
			Variable levelVar = ncfile.addVariable(null, "level", DataType.INT,"level");
			Variable latVar = ncfile.addVariable(null, "latitude", DataType.FLOAT, "latitude");
			Variable lonVar = ncfile.addVariable(null, "longitude", DataType.FLOAT, "longitude");
			
			//定义属性
			ncfile.addVariableAttribute(timeVar, new Attribute("units", "hours since 1900-01-01 00:00:00"));
			ncfile.addVariableAttribute(timeVar, new Attribute("long_name", "time"));
			
			validVar.addAttribute(new Attribute("units", "hour"));
			validVar.addAttribute(new Attribute("long_name", validDim.getShortName()));
			ncfile.addVariableAttribute(levelVar, new Attribute("units", "millibars"));
			ncfile.addVariableAttribute(levelVar, new Attribute("long_name", "pressure_leve"));
			ncfile.addVariableAttribute(latVar, new Attribute("units", "degress_north"));
			ncfile.addVariableAttribute(lonVar, new Attribute("units", "degrees_east"));
			//定义数组
			ArrayInt.D1 times = new ArrayInt.D1(timeDim.getLength());
			ArrayInt.D1 valids = new ArrayInt.D1(validDim.getLength());
			ArrayInt.D1 levels = new ArrayInt.D1(levelDim.getLength());
			ArrayFloat.D1 lats = new ArrayFloat.D1(latDim.getLength());
			ArrayFloat.D1 lons = new ArrayFloat.D1(lonDim.getLength());
			int i;
			for (i = 0; i < timeDim.getLength(); i++)
			{
				times.set(i, time);
			}
			for (i = 0; i < validDim.getLength(); i++)
			{
				valids.set(i, valid);
			}
			for (i = 0; i < levelDim.getLength(); i++)
			{
				levels.set(i, level);
			}
			for (i = 0; i < latDim.getLength(); i++)
			{
				lats.set(i, (float)yy[i]);
			}
			for (i = 0; i < lonDim.getLength(); i++)
			{
				lons.set(i, (float)xx[i]);
			}
			
			//变量
			String dims = "time validtime level latitude longitude";
			
//			Variable var = ncfile.addVariable(null, element, DataType.FLOAT, dims);
//			ncfile.addVariableAttribute(var, new Attribute("units", unit));
//			ArrayFloat.D5 arr = new ArrayFloat.D5( 1, 1, 1, latCount, lonCount);
			Map<Variable, ArrayFloat.D5> varsMap = new LinkedHashMap<Variable, ArrayFloat.D5>();
			for (Entry<String, double[][]> ele : elementValue.entrySet()) {
				Variable var = ncfile.addVariable(null, ele.getKey(), DataType.FLOAT, dims);
				ncfile.addVariableAttribute(var, new Attribute("units", unit));
				ArrayFloat.D5 arr = new ArrayFloat.D5(1, 1, 1, latCount, lonCount);
				varsMap.put(var, arr);
			}
			
			Variable var_TmaxOrTminTimes = null;
			ArrayFloat.D5 arr_times = null;
			if (dataTimes != null)
			{
				var_TmaxOrTminTimes = ncfile.addVariable(null, "dtimes", DataType.FLOAT, dims);
				ncfile.addVariableAttribute(var_TmaxOrTminTimes, new Attribute("remarks", "occurrence time"));
				arr_times = new ArrayFloat.D5(1, 1 ,1, latCount, lonCount);
			}
			
			ncfile.create();
			ncfile.write(levelVar, levels);
			ncfile.write(timeVar, times);
			ncfile.write(validVar, valids);
			ncfile.write(latVar, lats);
			ncfile.write(lonVar, lons);
			ncfile.flush();
			
			double v , dt;
			for (int t = 0; t < 1; t++)
			{
				for (int val = 0; val < validDim.getLength(); val++)
				{	
					for (int lvl = 0; lvl < 1; lvl++) 
					{
						for (int latude = 0; latude < latCount; latude++) //行 
						{
							for (int longidute = 0; longidute < lonCount; longidute++)//列 
							{
								for (Entry<Variable, ArrayFloat.D5> var : varsMap.entrySet()) {
									v = elementValue.get(var.getKey().getFullName())[latude][longidute];
									var.getValue().set( t, val ,lvl, latude, longidute, (float) v);
								}
								if (dataTimes != null)
								{
									dt = dataTimes[latude][longidute];
									arr_times.set(t, val, lvl, latude, longidute, (float)dt);	
								}
								
								
//								v = data[latude][longidute];
//								arr.set( t, val ,lvl, latude, longidute, (float) v);
//								if (dataTimes != null)
//								{
//									dt = dataTimes[latude][longidute];
//									arr_times.set(t, val, lvl, latude, longidute, (float)dt);	
//								}
							}
						}
					}
				}
			}
		for (Entry<Variable, ArrayFloat.D5> var : varsMap.entrySet()) {
			ncfile.write(var.getKey(), var.getValue());
			ncfile.flush();
		}
		if (dataTimes != null)
		{
			ncfile.write(var_TmaxOrTminTimes, arr_times);
		}
		ncfile.flush();
			
		}
		catch (Exception e)
		{
			logger.error("ZJ:writed  " + filePath + " file error , error : " + e);
			flag = false;
		}
		finally
		{
			try
			{
				if (ncfile != null)
				{
					ncfile.close();
				}
			}
			catch (IOException e)
			{
				logger.error("ZJ:close ncfile is error , error : " + e);
			}
		}
		
		return flag;
	}
}
