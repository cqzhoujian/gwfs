package com.supermap.gwfs.execuctors.synchronizer.clipper;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayFloat.D3;
import ucar.ma2.ArrayFloat.D4;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayShort;
import ucar.ma2.ArrayShort.D2;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.executors.synchronizer.clipper.util.ClipperUtil;

/**  
 * @Description: 写nc文件
 * @author zhoujian
 * @date 2016-10-11
 * @version V1.0 
 */
public class WriteNetcdf
{
	private Logger logger = LoggerFactory.getLogger("EfiClipper");
	private static WriteNetcdf writeNetcdf = null;
	
	private WriteNetcdf(){}
	
	public synchronized static WriteNetcdf getInstance()
	{
		if (writeNetcdf == null)
		{
			writeNetcdf = new WriteNetcdf();
		}
		return writeNetcdf;
	}
	/**
	 * 
	 * @Description: 写EFI文件
	 * @return boolean
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	public boolean startWrite(UniObject uniObject,String fileName)
	{
		try
		{
			List<String> features = new ArrayList<String>();
			Map<String,double[]> map = (Map<String,double[]>)uniObject.getValue("elementValue");
			for (String feature : map.keySet())
			{
				features.add(feature);
			}
			
			NetcdfFileWriter outNetcdf = (NetcdfFileWriter)uniObject.getValue("ncOutFile");
			List<Integer> time = (List<Integer>)uniObject.getValue("time"); 
			
			Map<String, Array[][]> dataMap = (Map<String, Array[][]>)uniObject.getValue("elementValue");
			Map<String, String> unitMap = (Map<String, String>)uniObject.getValue("elementUnit");
			Map<String, double[]> factors = (Map<String, double[]>)uniObject.getValue("factors");
			//
			if (fileName.contains("efi"))
			{
				Rectangle2D cellBounds = (Rectangle2D)uniObject.getValue("cellBounds");
				Rectangle2D geoBounds = ClipperUtil.getBounds(uniObject.getStringValue("efi_sfcSize"));
				Point2D sizeCell = (Point2D)uniObject.getValue("sizeCell");
				return this.writeFile(features,outNetcdf,geoBounds,cellBounds,sizeCell,time,dataMap,unitMap,factors);
			}
			else if(fileName.contains("ep"))
			{
				Rectangle2D cellBounds = (Rectangle2D)uniObject.getValue("cellBounds");
				Rectangle2D geoBounds = ClipperUtil.getBounds(uniObject.getStringValue("ep_plSize"));
				Point2D sizeCell = (Point2D)uniObject.getValue("sizeCell");
				return this.writeFile(features,outNetcdf,geoBounds,cellBounds,sizeCell,time,dataMap,unitMap,factors);
			}
			else if(fileName.contains("es"))
			{
				Rectangle2D cellBounds = (Rectangle2D)uniObject.getValue("cellBounds");
				Rectangle2D geoBounds =  ClipperUtil.getBounds(uniObject.getStringValue("es_plSize"));
				Point2D sizeCell = (Point2D)uniObject.getValue("sizeCell");
				List<String> layers = (List<String>)uniObject.getValue("es_plLevel");
				return this.writeFile(features,outNetcdf,geoBounds,cellBounds,sizeCell,time,layers,dataMap,unitMap,factors);
			}
			else if(fileName.contains("sot"))
			{
				Rectangle2D cellBounds = (Rectangle2D)uniObject.getValue("cellBounds");
				Rectangle2D geoBounds =  ClipperUtil.getBounds(uniObject.getStringValue("sot_sfcSize"));
				Point2D sizeCell = (Point2D)uniObject.getValue("sizeCell");
				int number = uniObject.getIntegerValue("sot_sfcNumber");
				return this.writeFile(features,outNetcdf,geoBounds,cellBounds,sizeCell,time,number,dataMap,unitMap,factors);
			}
			else
			{
				this.logger.error("文件名非法, 文件名 ：" + fileName);
				return false;
			}
		}
		catch (Exception e)
		{
			logger.error("写nc文件异常 ， 异常 ： " + e);
			return false;
		}
	}

	private boolean writeFile(List<String> features, NetcdfFileWriter outNetcdf, Rectangle2D geoBounds, Rectangle2D cellBounds, Point2D sizeCell, List<Integer> T_time, int number, Map<String, Array[][]> dataMap, Map<String, String> unitMap, Map<String, double[]> factors)
	{
		int w = (int) cellBounds.getWidth(), h = (int) cellBounds.getHeight();
		int x = (int) geoBounds.getX(), y = (int) geoBounds.getY();
		float dx = (float) sizeCell.getX(), dy = (float) sizeCell.getY();
		int timeNum = T_time.size();
		try {
			// 定义维度
			Dimension numberDim = outNetcdf.addDimension(null, "number", number);
			Dimension timeDim = outNetcdf.addDimension(null, "time", timeNum);
			Dimension latDim = outNetcdf.addDimension(null, "latitude", h);
			Dimension lonDim = outNetcdf.addDimension(null, "longitude", w);
			// 定义变量
			Variable numberVar = outNetcdf.addVariable(null, "number", DataType.INT, "number");
			Variable timeVar = outNetcdf.addVariable(null, "time", DataType.INT, "time");
			Variable latVar = outNetcdf.addVariable(null, "latitude", DataType.FLOAT, "latitude");
			Variable lonVar = outNetcdf.addVariable(null, "longitude", DataType.FLOAT, "longitude");
			// 设变量属性
			outNetcdf.addVariableAttribute(numberVar, new Attribute("long_name", "ensemble_member"));
			outNetcdf.addVariableAttribute(timeVar, new Attribute("units", "hours since 1900-01-01 00:00:0.0"));
			outNetcdf.addVariableAttribute(timeVar, new Attribute("long_name", "time"));
			outNetcdf.addVariableAttribute(latVar, new Attribute("units", "degrees_north"));
			outNetcdf.addVariableAttribute(lonVar, new Attribute("units", "degrees_east"));

			ArrayInt.D1 numbers = new ArrayInt.D1(numberDim.getLength());
			ArrayInt.D1 times = new ArrayInt.D1(timeDim.getLength());
			ArrayFloat.D1 lats = new ArrayFloat.D1(latDim.getLength());
			ArrayFloat.D1 lons = new ArrayFloat.D1(lonDim.getLength());

			int i;
			for (i = 0; i < numberDim.getLength(); i++) {
				numbers.set(i, i);
			}
			for (i = 0; i < timeDim.getLength(); i++) {
				times.set(i, T_time.get(i));
			}
			for (i = 0; i < latDim.getLength(); i++) {
				lats.set(i, y + dy * i);
			}
			for (i = 0; i < lonDim.getLength(); i++) {
				lons.set(i, x + dx * i);
			}
			// int w = (int) cellBounds.getWidth() + 1, h = (int) cellBounds.getHeight() + 1;
			String dims = " number time latitude longitude";
			Map<Variable, ArrayFloat.D4> varsMap = new LinkedHashMap<Variable, D4>();
			for (String feature : features) {
				Variable var = outNetcdf.addVariable(null, feature, DataType.FLOAT, dims);
				outNetcdf.addVariableAttribute(var, new Attribute("units", unitMap.get(feature)));
				ArrayFloat.D4 arr = new ArrayFloat.D4(number, timeNum, h, w);
				varsMap.put(var, arr);
			}
			outNetcdf.create();
			outNetcdf.write(numberVar, numbers);
			outNetcdf.write(timeVar, times);
			outNetcdf.write(latVar, lats);
			outNetcdf.write(lonVar, lons);
			outNetcdf.flush();
			String varName;
			Array[][] arrays;
			ArrayShort.D2 values;
			short value;
			double v;
			for (int num = 0; num < number ; num++) {
				for (int time = 0; time < timeNum; time++) {
					for (int lat = 0; lat < h; lat++) {
						for (int lon = 0; lon < w; lon++) {
							for (Entry<Variable, D4> var : varsMap.entrySet()) {
								varName = var.getKey().getFullName();
								arrays = dataMap.get(varName);
								if (arrays == null) {
									continue;
								}
								values = (D2) arrays[num+1][0];
								if (values == null) {
									continue;
								}									
								value = values.get(h - 1 - lat, lon);
								v = value * factors.get(varName)[1] + factors.get(varName)[0];
								var.getValue().set(num, time, lat, lon, (float) v);
							}
						}
					}
				}
			}

			for (Entry<Variable, D4> var : varsMap.entrySet()) {
				outNetcdf.write(var.getKey(), var.getValue());
				outNetcdf.flush();
			}
		} catch (Exception e) {
			logger.error("写nc文件异常 , 异常 ： " + e);
			return false;
		} finally {
			if (outNetcdf != null)
				try {
					outNetcdf.close();
				} catch (IOException ioe) {
					logger.error("关闭 NetcdfFileWriter对象异常 ， 异常 ：" + ioe);
					return false;
				}
		}
		return true;
		
	}

	private boolean writeFile(List<String> features, NetcdfFileWriter outNetcdf, Rectangle2D geoBounds, Rectangle2D cellBounds, Point2D sizeCell, List<Integer> T_time, List<String> layers, Map<String, Array[][]> dataMap, Map<String, String> unitMap, Map<String, double[]> factors)
	{
		int w = (int) cellBounds.getWidth(), h = (int) cellBounds.getHeight();
		int x = (int) geoBounds.getX(), y = (int) geoBounds.getY();
		float dx = (float) sizeCell.getX(), dy = (float) sizeCell.getY();
		int timeNum = T_time.size();
		try {
			// 定义维度
			Dimension timeDim = outNetcdf.addDimension(null, "time", timeNum);
			Dimension levelDim = outNetcdf.addDimension(null, "level", layers.size());
			Dimension latDim = outNetcdf.addDimension(null, "latitude", h);
			Dimension lonDim = outNetcdf.addDimension(null, "longitude", w);
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
				times.set(i, T_time.get(i));
			}
			for (i = 0; i < levelDim.getLength(); i++) {
				levels.set(i, Integer.parseInt(layers.get(i)));
			}
			for (i = 0; i < latDim.getLength(); i++) {
				lats.set(i, y + dy * i);
			}
			for (i = 0; i < lonDim.getLength(); i++) {
				lons.set(i, x + dx * i);
			}
			// int w = (int) cellBounds.getWidth() + 1, h = (int) cellBounds.getHeight() + 1;
			String dims = "time level latitude longitude";
			Map<Variable , ArrayFloat.D4> varsMap = new LinkedHashMap<Variable, ArrayFloat.D4>();
			for (String feature : features) {
				Variable var = outNetcdf.addVariable(null, feature, DataType.FLOAT, dims);
				outNetcdf.addVariableAttribute(var, new Attribute("units", unitMap.get(feature)));
				ArrayFloat.D4 arr = new ArrayFloat.D4( timeNum, layers.size(), h, w);
				varsMap.put(var, arr);
			}

			outNetcdf.create();
			outNetcdf.write(levelVar, levels);
			outNetcdf.write(timeVar, times);
			outNetcdf.write(latVar, lats);
			outNetcdf.write(lonVar, lons);
			outNetcdf.flush();
			String varName;
			Array[][] arrays;
			ArrayShort.D2 values;
			short value;
			double v;
				for (int time = 0; time < timeNum; time++) {
					for (int lvl = 0; lvl < layers.size(); lvl++) {
						for (int lat = 0; lat < h; lat++) {
							for (int lon = 0; lon < w; lon++) {
								for (Entry<Variable, D4> var : varsMap.entrySet()) {
									varName = var.getKey().getFullName();
									arrays = dataMap.get(varName);
									if (arrays == null) {
										continue;
									}
									values = (D2) arrays[0][lvl];
									if (values == null) {
										continue;
									}									
									value = values.get(h - 1 - lat, lon);
									v = value * factors.get(varName)[1] + factors.get(varName)[0];
									var.getValue().set(time, lvl, lat, lon, (float) v);
								}
							}
						}
					}
				}

			for (Entry<Variable, D4> var : varsMap.entrySet()) {
				outNetcdf.write(var.getKey(), var.getValue());
				outNetcdf.flush();
			}
		} catch (Exception e) {
			logger.error("写 nc 文件异常 ， 异常 ： " + e);
			return false;
		} finally {
			if (outNetcdf != null)
				try {
					outNetcdf.close();
				} catch (IOException ioe) {
					logger.error(" 关闭 NetcdfFileWriter 对象异常 ， 异常 ：" + ioe);
					return false;
				}
		}
		return true;
		
	}

	private boolean writeFile(List<String> features, NetcdfFileWriter outNetcdf, Rectangle2D geoBounds, Rectangle2D cellBounds, Point2D sizeCell, List<Integer> T_time, Map<String, Array[][]> dataMap, Map<String, String> unitMap, Map<String, double[]> factors)
	{
		int w = Math.abs((int) cellBounds.getWidth()), h = (int) cellBounds.getHeight();
		int x = (int) geoBounds.getX(), y = (int) geoBounds.getY();
		float dx = (float) sizeCell.getX(), dy = (float) sizeCell.getY();
		int timeNum = T_time.size();
		try {
			// 定义维度
			Dimension timeDim = outNetcdf.addDimension(null, "time", timeNum);
			Dimension latDim = outNetcdf.addDimension(null, "latitude", h);
			Dimension lonDim = outNetcdf.addDimension(null, "longitude", w);
//			Dimension levelDim = outNetcdf.addDimension(null, "level", 1);
			// 定义变量
			Variable timeVar = outNetcdf.addVariable(null, "time", DataType.INT, "time");
//			Variable levelVar = outNetcdf.addVariable(null, "level", DataType.INT, "level");
			Variable latVar = outNetcdf.addVariable(null, "latitude", DataType.FLOAT, "latitude");
			Variable lonVar = outNetcdf.addVariable(null, "longitude", DataType.FLOAT, "longitude");
			// 设变量属性
			outNetcdf.addVariableAttribute(timeVar, new Attribute("units", "hours since 1900-01-01 00:00:0.0"));
			outNetcdf.addVariableAttribute(timeVar, new Attribute("long_name", "time"));
			outNetcdf.addVariableAttribute(latVar, new Attribute("units", "degrees_north"));
			outNetcdf.addVariableAttribute(lonVar, new Attribute("units", "degrees_east"));
//			outNetcdf.addVariableAttribute(levelVar, new Attribute("units", "millibars"));
//			outNetcdf.addVariableAttribute(levelVar, new Attribute("long_name", "pressure_leve"));
			

			ArrayInt.D1 times = new ArrayInt.D1(timeDim.getLength());
//			ArrayInt.D1 levels = new ArrayInt.D1(levelDim.getLength());
			ArrayFloat.D1 lats = new ArrayFloat.D1(latDim.getLength());
			ArrayFloat.D1 lons = new ArrayFloat.D1(lonDim.getLength());

			int i;
			for (i = 0; i < timeDim.getLength(); i++) {
				times.set(i, T_time.get(i));
			}
			for (i = 0; i < latDim.getLength(); i++) {
				lats.set(i, y + dy * i);
			}
//			for (i = 0; i < levelDim.getLength(); i++) {
//				levels.set(i, 0);
//			}
			for (i = 0; i < lonDim.getLength(); i++) {
				lons.set(i, x + dx * i);
			}
			String dims = "time latitude longitude";
			Map<Variable, ArrayFloat.D3> varsMap = new LinkedHashMap<Variable,ArrayFloat.D3>();
			for (String feature : features) {
				Variable var = outNetcdf.addVariable(null, feature, DataType.FLOAT, dims);
				outNetcdf.addVariableAttribute(var, new Attribute("units", unitMap.get(feature)));
				ArrayFloat.D3 arr = new ArrayFloat.D3(timeNum, h, w);
				varsMap.put(var, arr);
			}

			outNetcdf.create();
			outNetcdf.write(timeVar, times);
//			outNetcdf.write(levelVar, levels);
			outNetcdf.write(latVar, lats);
			outNetcdf.write(lonVar, lons);
			outNetcdf.flush();
			String varName;
			Array[][] arrays;
			ArrayShort.D2 values;
			short value;
			double v;
			for (int time = 0; time < timeNum; time++) {
//				for (int lvl = 0; lvl < 1; lvl++) {
					for (int lat = 0; lat < h; lat++) {
						for (int lon = 0; lon < w; lon++) {
							for (Entry<Variable, D3> var : varsMap.entrySet()) {
								varName = var.getKey().getFullName();
								arrays = dataMap.get(varName);
								if (arrays == null) {
									continue;
								}
								values = (D2) arrays[0][0];
								if (values == null) {
									continue;
								}									
								value = values.get(h - 1 - lat, lon);
								v = value * factors.get(varName)[1] + factors.get(varName)[0];
								var.getValue().set(time, lat, lon, (float) v);
							}
						}
					}
//				}
			}
			for (Entry<Variable, D3> var : varsMap.entrySet()) {
				outNetcdf.write(var.getKey(), var.getValue());
				outNetcdf.flush();
			}
		} catch (Exception e) {
			logger.error("写nc文件异常 ， 异常 ： " + e);
			return false;
		} finally {
			if (outNetcdf != null)
				try {
					outNetcdf.close();
				} catch (IOException ioe) {
					logger.error("关闭  NetcdfFileWriter 对象异常 ， 异常 ： " + ioe);
					return false;
				}
		}
		return true;
		
	}
}
