package com.supermap.gwfs.grib2live.service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.NetcdfFileWriter.Version;
import ucar.nc2.Variable;

import com.supermap.gwfs.grib2live.util.Logger;

public class WriteNCService {
	private Logger logger = Logger.getLogger(this.getClass());


	/*public boolean writeData(String path, String element, int level, float left, float bottom, float dx, float dy, int dIM_time, float[] dvalue, int cols, int rows, String unit) {
		Map<String, float[]> elementValue = new HashMap<String, float[]>();
		elementValue.put(element, dvalue);
		return writeData(path, elementValue, level, left, bottom, dx, dy, dIM_time, cols, rows, unit);
	}*/
	//20160913
	public boolean writeData(String path, Map<String, float[]> elementValue, int level, float left, float bottom, float dx, float dy, int forecastDate, int cols, int rows, String unit)
	{
		boolean flag = true;
		float x = left, y = bottom;
		int w = cols, h = rows;
		// float dx = dx, dy = dy;
		NetcdfFileWriter outNetcdf = null;
		try {
			outNetcdf = NetcdfFileWriter.createNew(Version.netcdf3, path);
			// 定义维度
			Dimension timeDim = outNetcdf.addDimension(null, "time", 1);
			Dimension levelDim = outNetcdf.addDimension(null, "level", 1);
			Dimension latDim = outNetcdf.addDimension(null, "latitude", h);
			Dimension lonDim = outNetcdf.addDimension(null, "longitude", w);
			// 定义变量
			Variable timeVar = outNetcdf.addVariable(null, "time", DataType.INT, "time");
			//20160913
			Variable levelVar = outNetcdf.addVariable(null, "level", DataType.INT, "level");
			Variable latVar = outNetcdf.addVariable(null, "latitude", DataType.FLOAT, "latitude");
			Variable lonVar = outNetcdf.addVariable(null, "longitude", DataType.FLOAT, "longitude");
			// 设变量属性
			outNetcdf.addVariableAttribute(timeVar, new Attribute("units", "hours since 1970-01-01 00:00:0.0"));
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
				times.set(i, forecastDate);
			}
			for (i = 0; i < levelDim.getLength(); i++) {
				levels.set(i, level);
			}
			for (i = 0; i < latDim.getLength(); i++) {
				lats.set(i, y + dy * i);
			}
			for (i = 0; i < lonDim.getLength(); i++) {
				lons.set(i, x + dx * i);
			}
			String dims = "time level latitude longitude";

			Map<Variable, ArrayFloat.D4> varsMap = new LinkedHashMap<Variable, ArrayFloat.D4>();
			for (Entry<String, float[]> ele : elementValue.entrySet()) {
				Variable var = outNetcdf.addVariable(null, ele.getKey(), DataType.FLOAT, dims);
				outNetcdf.addVariableAttribute(var, new Attribute("units", unit));
				ArrayFloat.D4 arr = new ArrayFloat.D4(1, 1, h, w);
				varsMap.put(var, arr);
			}
			outNetcdf.create();
			outNetcdf.write(levelVar, levels);
			outNetcdf.write(timeVar, times);
			outNetcdf.write(latVar, lats);
			outNetcdf.write(lonVar, lons);
			outNetcdf.flush();
			float[] dvalue;
			for (int time = 0; time < 1; time++) {
				for (int lvl = 0; lvl < 1; lvl++) {
					for (int lat = 0; lat < h; lat++) {
						for (int lon = 0; lon < w; lon++) {
							for (Entry<Variable, ArrayFloat.D4> var : varsMap.entrySet()) {
								dvalue = elementValue.get(var.getKey().getFullName());
								var.getValue().set(time, lvl, lat, lon, dvalue[lat * w + lon]);
							}
						}
					}
				}
			}
			for (Entry<Variable, ArrayFloat.D4> var : varsMap.entrySet()) {
				outNetcdf.write(var.getKey(), var.getValue());
				outNetcdf.flush();
			}
		} catch (InvalidRangeException e) {
			logger.error("ZJ:" + e.getMessage(), e);
			flag = false;
		} catch (Exception e) {
			logger.error("ZJ:"+e.getMessage(), e);
			flag = false;
		} finally {
			if (outNetcdf != null)
				try {
					outNetcdf.close();
				} catch (IOException ioe) {
					logger.error("ZJ:"+ioe.getMessage(), ioe);
					flag = false;
				}
			
		}
		return flag;
	}
}
