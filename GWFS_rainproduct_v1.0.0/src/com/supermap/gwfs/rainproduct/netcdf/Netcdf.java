package com.supermap.gwfs.rainproduct.netcdf;

import java.io.IOException;

import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;

/**  
 * @Description: 处理Netcdf
 * @author zhoujian
 * @date 2016-10-25
 * @version V1.0 
 */
public class Netcdf
{
	private Logger logger = LoggerFactory.getLogger("Netcdf_Rain");
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
	
	
	public boolean WriteNetcdf(UniObject uniObject,String filePath)
	{
			int clos = uniObject.getIntegerValue("w");
			int rows = uniObject.getIntegerValue("h");
			int time = uniObject.getIntegerValue("time");
			String element = uniObject.getStringValue("element");
			double[][] X = (double[][])uniObject.getValue("X");
			double[][] Y = (double[][])uniObject.getValue("Y");
			double[][] Z = (double[][])uniObject.getValue("Z");
		
			return write(rows, clos, element, time, X, Y, Z, filePath); 

	}
	
	
	private boolean write(int rows, int clos,String element, int time,double[][] x, double[][] y, double[][] z ,String filePath)
	{
		boolean flag = true;
		NetcdfFileWriter outNetcdf = null;
		try {
		
			outNetcdf = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, filePath);
			// 定义维度
			Dimension timeDim = outNetcdf.addDimension(null, "time", 1);
			Dimension levelDim = outNetcdf.addDimension(null, "level", 1);
			Dimension latDim = outNetcdf.addDimension(null, "latitude", rows);
			Dimension lonDim = outNetcdf.addDimension(null, "longitude", clos);
			// 定义变量
			Variable timeVar = outNetcdf.addVariable(null, "time", DataType.INT, "time");
			Variable levelVar = outNetcdf.addVariable(null, "level", DataType.INT, "level");
			Variable latVar = outNetcdf.addVariable(null, "latitude", DataType.FLOAT, "latitude");
			Variable lonVar = outNetcdf.addVariable(null, "longitude", DataType.FLOAT, "longitude");
			// 设变量属性
			outNetcdf.addVariableAttribute(timeVar, new Attribute("units", "hours since 1900-01-01 00:00:0.0"));
			outNetcdf.addVariableAttribute(timeVar, new Attribute("long_name", "time"));
			outNetcdf.addVariableAttribute(levelVar, new Attribute("units", "millibars"));
			outNetcdf.addVariableAttribute(levelVar, new Attribute("long_name", "pressure_leve"));
			outNetcdf.addVariableAttribute(latVar, new Attribute("units", "degrees_north"));
			outNetcdf.addVariableAttribute(lonVar, new Attribute("units", "degrees_east"));

			ArrayInt.D1 times = new ArrayInt.D1(timeDim.getLength());
			ArrayInt.D1 levels = new ArrayInt.D1(levelDim.getLength());
			ArrayFloat.D1 lats = new ArrayFloat.D1(latDim.getLength());
			ArrayFloat.D1 lons = new ArrayFloat.D1(lonDim.getLength());

			int i;
			for (i = 0; i < timeDim.getLength(); i++) {
				times.set(i,time);
			}
			for (i = 0; i < levelDim.getLength(); i++) {
				levels.set(i, 0);
			}
			for (i = 0; i < latDim.getLength(); i++) {
				lats.set(i, (float)y[i][0]);
			}
			for (i = 0; i < lonDim.getLength(); i++) {
				lons.set(i, (float)x[0][i]);
			}
			String dims = "time level latitude longitude";
			Variable var = outNetcdf.addVariable(null, element, DataType.FLOAT, dims);
			outNetcdf.addVariableAttribute(var, new Attribute("units", "m"));
			ArrayFloat.D4 arr = new ArrayFloat.D4( 1, 1, rows, clos);

			outNetcdf.create();
			outNetcdf.write(levelVar, levels);
			outNetcdf.write(timeVar, times);
			outNetcdf.write(latVar, lats);
			outNetcdf.write(lonVar, lons);
			outNetcdf.flush();
			double v;
				for (int t = 0; t < 1; t++)
				{
					for (int lvl = 0; lvl < 1; lvl++) 
					{
						for (int lat = 0; lat < rows; lat++) //行 401
						{
							for (int lon = 0; lon < clos; lon++)//列 651
							{
								v = z[lat][lon];
								if(v < 0)
									v = 0;
								arr.set( t, lvl, lat, lon, (float) v);
									
							}
						}
					}
				}
			outNetcdf.write(var, arr);
			outNetcdf.flush();
		} catch (Exception e) 
		{
			flag = false;
			logger.error("写nc数据异常 , 异常 ：" + e);
		}
		finally
		{
			try
			{
				if (outNetcdf != null)
					outNetcdf.close();
			}
			catch (IOException e)
			{
				logger.error("关闭NetcdfFileWriter对象异常 ,异常  : " + e);
			}
		}
		
		return flag;
	}
	
}
