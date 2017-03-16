package com.supermap.gwfs.clipper;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayFloat.D5;
import ucar.ma2.ArrayInt;
import ucar.ma2.ArrayShort;
import ucar.ma2.ArrayShort.D2;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import com.supermap.gwfs.clipper.entity.NetcdfData;
import com.supermap.gwfs.clipper.entity.ClipperEntity;
import com.supermap.gwfs.clipper.entity.SizeParameter;
import com.supermap.gwfs.clipper.util.Logger;

public class WriteNetcdf {
	private Logger logger = Logger.getLogger(WriteNetcdf.class);

	public boolean writePlFile(ClipperEntity clipperEntity, SizeParameter sizeParameter, NetcdfData clipperData) {
		List<String> features = sizeParameter.getPlFeature();
		List<String> layers = sizeParameter.getPlLayer();
		NetcdfFileWriter outNetcdf = clipperEntity.getPlOutncfile();
		Rectangle2D cellBounds = clipperEntity.getPlCellBounds();
		Rectangle2D geoBounds = sizeParameter.getPlBounds();
		Point2D sizeCell = clipperEntity.getPlsizeCell();
		int numberNum = sizeParameter.getNumber();
		int dIM_time = clipperData.getTime();
		Map<String, Double[]> factor = clipperData.getPlfactors();
		Map<String, Array[][]> dataMap = clipperData.getPlDataMap();
		Map<String, String> uintMap = clipperData.getUnit();
		// 定义维度，变量
		return this.writeData(features, layers, outNetcdf, geoBounds, cellBounds, sizeCell, numberNum, dIM_time, dataMap, factor, uintMap);

	}

	public boolean writeSfc1File(ClipperEntity clipperEntity, SizeParameter sizeParameter, NetcdfData clipperData) {
		List<String> features = sizeParameter.getSfc1Feature();
		List<String> layers = sizeParameter.getSfc1Layer();
		NetcdfFileWriter outNetcdf = clipperEntity.getSfc1Outncfile();
		Rectangle2D cellBounds = clipperEntity.getSfc1CellBounds();
		Rectangle2D geoBounds = sizeParameter.getSfc1Bounds();
		Point2D sizeCell = clipperEntity.getSfc1sizeCell();
		int numberNum = sizeParameter.getNumber();
		int dIM_time = clipperData.getTime();
		Map<String, Double[]> factor = clipperData.getSfc1factors();
		Map<String, Array[][]> dataMap = clipperData.getSfc1DataMap();
		Map<String, String> uintMap = clipperData.getUnit();
		// 定义维度，变量
		return this.writeData(features, layers, outNetcdf, geoBounds, cellBounds, sizeCell, numberNum, dIM_time, dataMap, factor, uintMap);
	}

	public boolean writeSfc2File(ClipperEntity clipperEntity, SizeParameter sizeParameter, NetcdfData clipperData) {
		List<String> features = sizeParameter.getSfc2Feature();
		List<String> layers = sizeParameter.getSfc2Layer();
		NetcdfFileWriter outNetcdf = clipperEntity.getSfc2Outncfile();
		Rectangle2D cellBounds = clipperEntity.getSfc2CellBounds();
		Rectangle2D geoBounds = sizeParameter.getSfc2Bounds();
		Point2D sizeCell = clipperEntity.getSfc2sizeCell();
		int numberNum = sizeParameter.getNumber();
		int dIM_time = clipperData.getTime();
		Map<String, Double[]> factor = clipperData.getSfc2factors();
		Map<String, Array[][]> dataMap = clipperData.getSfc2DataMap();
		Map<String, String> uintMap = clipperData.getUnit();
		// 定义维度，变量
		return this.writeData(features, layers, outNetcdf, geoBounds, cellBounds, sizeCell, numberNum, dIM_time, dataMap, factor, uintMap);

	}

	private boolean writeData(List<String> features, List<String> layers, NetcdfFileWriter outNetcdf, Rectangle2D geoBounds, Rectangle2D cellBounds, Point2D sizeCell, int numberNum, int dIM_time, Map<String, Array[][]> dataMap, Map<String, Double[]> factor, Map<String, String> uintMap) {
		int w = (int) cellBounds.getWidth(), h = (int) cellBounds.getHeight();
		int x = (int) geoBounds.getX(), y = (int) geoBounds.getY();
		float dx = (float) sizeCell.getX(), dy = (float) sizeCell.getY();
		try {
			// 定义维度
			Dimension numberDim = outNetcdf.addDimension(null, "number", numberNum + 1);
			Dimension timeDim = outNetcdf.addDimension(null, "time", 1);
			Dimension levelDim = outNetcdf.addDimension(null, "level", layers.size());
			Dimension latDim = outNetcdf.addDimension(null, "latitude", h);
			Dimension lonDim = outNetcdf.addDimension(null, "longitude", w);
			// 定义变量
			Variable numberVar = outNetcdf.addVariable(null, "number", DataType.INT, "number");
			Variable timeVar = outNetcdf.addVariable(null, "time", DataType.INT, "time");
			Variable levelVar = outNetcdf.addVariable(null, "level", DataType.INT, "level");
			Variable latVar = outNetcdf.addVariable(null, "latitude", DataType.FLOAT, "latitude");
			Variable lonVar = outNetcdf.addVariable(null, "longitude", DataType.FLOAT, "longitude");
			// 设变量属性
			outNetcdf.addVariableAttribute(numberVar, new Attribute("long_name", "ensemble_member"));
			outNetcdf.addVariableAttribute(timeVar, new Attribute("units", "hours since 1900-01-01 00:00:0.0"));
			outNetcdf.addVariableAttribute(timeVar, new Attribute("long_name", "time"));
			outNetcdf.addVariableAttribute(levelVar, new Attribute("units", "millibars"));
			outNetcdf.addVariableAttribute(levelVar, new Attribute("long_name", "pressure_leve"));
			outNetcdf.addVariableAttribute(latVar, new Attribute("units", "degrees_north"));
			outNetcdf.addVariableAttribute(lonVar, new Attribute("units", "degrees_east"));

			ArrayInt.D1 numbers = new ArrayInt.D1(numberDim.getLength());
			ArrayInt.D1 times = new ArrayInt.D1(timeDim.getLength());
			ArrayInt.D1 levels = new ArrayInt.D1(levelDim.getLength());
			ArrayFloat.D1 lats = new ArrayFloat.D1(latDim.getLength());
			ArrayFloat.D1 lons = new ArrayFloat.D1(lonDim.getLength());

			int i;
			for (i = 0; i < numberDim.getLength(); i++) {
				numbers.set(i, i);
			}
			for (i = 0; i < timeDim.getLength(); i++) {
				times.set(i, dIM_time);
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
			String dims = " number time level latitude longitude";
			Map<Variable, ArrayFloat.D5> varsMap = new LinkedHashMap<Variable, D5>();
			for (String feature : features) {
				Variable var = outNetcdf.addVariable(null, feature, DataType.FLOAT, dims);
				outNetcdf.addVariableAttribute(var, new Attribute("units", uintMap.get(feature)));
				ArrayFloat.D5 arr = new ArrayFloat.D5(numberNum + 1, 1, layers.size(), h, w);
				varsMap.put(var, arr);
			}

			outNetcdf.create();
			outNetcdf.write(levelVar, levels);
			outNetcdf.write(numberVar, numbers);
			outNetcdf.write(timeVar, times);
			outNetcdf.write(latVar, lats);
			outNetcdf.write(lonVar, lons);
			outNetcdf.flush();
			String varName;
			Array[][] arrays;
			ArrayShort.D2 values;
			double scale_factor;
			double add_offset;
			short value;
			double v;
			for (int num = 0; num < numberNum + 1; num++) {
				for (int time = 0; time < 1; time++) {
					for (int lvl = 0; lvl < layers.size(); lvl++) {
						for (int lat = 0; lat < h; lat++) {
							for (int lon = 0; lon < w; lon++) {
								for (Entry<Variable, D5> var : varsMap.entrySet()) {
									varName = var.getKey().getFullName();
									arrays = dataMap.get(varName);
									if (arrays == null) {
										continue;
									}
									values = (D2) arrays[num][lvl];
									if (values == null) {
										continue;
									}									
									value = values.get(h - 1 - lat, lon);
									if (num == 0) {
										scale_factor = factor.get(varName)[0];
										add_offset = factor.get(varName)[1];
									} else {
										scale_factor = factor.get(varName)[2];
										add_offset = factor.get(varName)[3];
									}
									v = value * scale_factor + add_offset;
									var.getValue().set(num, time, lvl, lat, lon, (float) v);
									// TODO TEST
									// if (num == 1 && lvl == 0 && varName.equalsIgnoreCase("d")) {
									// System.out.println(factor.get(varName)[0] + " " + factor.get(varName)[1]);
									// if (lon == 0) {
									// System.out.println();
									// }
									// System.out.print(" (" + (30 + lat) + " " + (60 + lon) + "):");
									// System.out.print(String.format("%-18s", (float) v));
									// }
									// System.out.print("number:" + num + " level:" + layers.get(lvl) + " (" + lat + " " + lon + ")" + varName);
									// System.out.print(":" + v);
								}
							}
						}
					}
				}
			}

			for (Entry<Variable, D5> var : varsMap.entrySet()) {
				outNetcdf.write(var.getKey(), var.getValue());
				outNetcdf.flush();
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return false;
		} catch (InvalidRangeException e) {
			logger.error(e.getMessage(), e);
			return false;
		} finally {
			if (outNetcdf != null)
				try {
					outNetcdf.close();
				} catch (IOException ioe) {
					logger.error(ioe.getMessage(), ioe);
					return false;
				}
		}
		return true;
	}
}