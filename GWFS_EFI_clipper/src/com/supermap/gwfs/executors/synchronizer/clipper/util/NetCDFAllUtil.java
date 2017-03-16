package com.supermap.gwfs.executors.synchronizer.clipper.util;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;

public class NetCDFAllUtil {
	public static Logger logger = LoggerFactory.getLogger("Cilpper");

	public static void main(String[] args) {
		try {
			NetcdfFile ncfFile = NetcdfFile.open("E:/data/EN_NETCDF_data/20160409/12/cf_sfc_20160409_0.grib1.nc");
			Point2D cellsize = getCellSize(ncfFile);
			System.out.println(cellsize);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<Integer> asList(int[] arr) {
		List<Integer> list = new ArrayList<Integer>();
		for (int i : arr) {
			list.add(i);
		}
		return list;
	}

	public static List<Float> asList(float[] arr) {
		List<Float> list = new ArrayList<Float>();
		for (float f : arr) {
			list.add(f);
		}
		return list;
	}

	public static Variable getLat(NetcdfFile ncfile) {
		Variable latVar = ncfile.findVariable("latitude");
		return latVar;
	}

	public static Variable getLon(NetcdfFile ncfile) {
		Variable lonVar = ncfile.findVariable("longitude");
		return lonVar;
	}

	public static int getIndex(Variable var, int i) {
		int index = 0;
		try {
			Array array = var.read();
			List<Integer> list = asIntList(array);
			return list.indexOf(i);
		} catch (IOException e) {
			index = -1;
			logger.error(e.getMessage());
		}
		return index;
	}

	public static int getIndex(Variable var, float i) {
		int index = 0;
		try {
			Array array = var.read();
			List<Float> list = asFloatList(array);
			return list.indexOf(i);
		} catch (IOException e) {
			index = -1;
			logger.error(e.getMessage());
		}
		return index;
	}

	private static List<Float> asFloatList(Array array) {
		float[] arrjava = (float[]) array.copyTo1DJavaArray();
		return asList(arrjava);
	}

	private static List<Integer> asIntList(Array array) {
		int[] arrjava = (int[]) array.copyTo1DJavaArray();
		return asList(arrjava);
	}

//	public static int getDimTime(NetcdfFile netcdfFile) {
//		int t = 0;
//		try {
//			/*Variable time = netcdfFile.findVariable("time");
//			Array tArray = time.read();
//			t = tArray.getInt(0);*/
//			
//			List<Long> timeList = new ArrayList<Long>();
//			Variable timeVar = netcdfFile.findVariable("time");
//			Array timeValues =  timeVar.read();
//			long[] times = (long[])timeValues.copyTo1DJavaArray();
//			long[] datavalues = new long[times.length];
//			for (int i = 0; i < datavalues.length; i++)
//			{
//				timeList.add(times[i]);
//			}
//			
//		} catch (IOException e) {
//			logger.error(e.getMessage());
//		}
//		return t;
//	}
	public static List<Integer> getDimTime(NetcdfFile netcdfFile) {
		List<Integer> timeList = new ArrayList<Integer>();
		try {
			Variable timeVar = netcdfFile.findVariable("time");
			Array timeValues =  timeVar.read();
			int[] times = (int[])timeValues.copyTo1DJavaArray();
//			int[] datavalues = new int[times.length];
			for (int i = 0; i < times.length; i++)
			{	
				timeList.add(times[i]);
			}
			
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return timeList;
	}

	public static double getAdd_offset(Variable variable) {
		Attribute add_offset = variable.findAttribute("add_offset");
		return add_offset.getNumericValue().doubleValue();
	}

	public static double getScale_factor(Variable variable) {
		Attribute scale_factor = variable.findAttribute("scale_factor");
		return scale_factor.getNumericValue().doubleValue();
	}

	public static Point2D getCellSize(NetcdfFile netcdfFile) {
		Variable latVar = getLat(netcdfFile);
		Variable lonVar = getLon(netcdfFile);
		Point2D cellSize = new Point2D.Float();
		try {
			Array lonArray = lonVar.read();
			float lonlen = lonArray.getFloat((int) lonArray.getSize() - 1) - lonArray.getFloat(0);
			long lonsize = lonArray.getSize() - 1;
			float latSize = lonlen / lonsize;

			Array latArray = latVar.read();
			float latlen = latArray.getFloat(0) - latArray.getFloat((int) latArray.getSize() - 1);
			long size = latArray.getSize() - 1;
			float lonSize = latlen / size;
			cellSize.setLocation(lonSize, latSize);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cellSize;

	}

	/**
	 *  地理范围-->栅格范围
	 * @param netcdfFile
	 * @param geoBounds
	 * @return
	 */
	public static Rectangle2D getCellBounds(NetcdfFile netcdfFile, Rectangle2D geoBounds) {
		Rectangle2D cellBounds = new Rectangle2D.Float();
		Variable latVar = getLat(netcdfFile);
		Variable lonVar = getLon(netcdfFile);
		float startX = (float) geoBounds.getX(), startY = (float) geoBounds.getY(), endX = (float) (startX + geoBounds.getWidth()), endY = (float) (startY + geoBounds.getHeight());
		//裁切lon后的startx坐标是在原始坐标集合的下标，裁切结束的endx坐标是在原始坐标集合的下标  CellEndX - cellStartX + 1 = lon开始到结束的格子数
		int cellStartX = getIndex(lonVar, startX), CellEndX = getIndex(lonVar, endX);
		//裁切lat后的starty在原始坐标集合的下标，endy在原始坐标集合中的下标 cellStartY - CellEndY + 1 = lat开始到结束的格子数
		int cellStartY = getIndex(latVar, startY), CellEndY = getIndex(latVar, endY);
		
		cellBounds.setRect(cellStartX, CellEndY, CellEndX - cellStartX + 1, cellStartY - CellEndY + 1);
		return cellBounds;
	}

	/**
	 * 
	 * @Title: getLevelIndex 
	 * @Description: TODO
	 * @param @param cfFile
	 * @param @param list  裁切的层list
	 * @param @return
	 * @return List<Integer>
	 * @throws
	 */
	public static List<Integer> getLevelIndex(NetcdfFile cfFile, List<String> list) {
		List<Integer> indexsList = new ArrayList<Integer>();
		Variable lvlVar = cfFile.findVariable("level");
		if(lvlVar == null)
			return null;
		for (String lvl : list) {
			int index = getIndex(lvlVar, Integer.parseInt(lvl));
			indexsList.add(index);
		}
		return indexsList;
	}

	public static List<Integer> getNumberIndex(NetcdfFile ncFile,String type) {
		List<Integer> indexsList = new ArrayList<Integer>();
		Variable numVar = ncFile.findVariable("number");
		if(numVar == null)
			return null;
		if("EFI".equals(type))
		{
			for (int i = 1; i <= 2; i++) {
				int index = -1;
				if (i == 1)
				{
					index = getIndex(numVar, 10);
				}
				if (i == 2)
				{
					index = getIndex(numVar, 90);
				}
				
				indexsList.add(index);
			}
		}
		else
		{
			for (int i = 1; i <= 50; i++) {
				int index = getIndex(numVar, i);
				indexsList.add(index);
			}
		}

		return indexsList;
	}

	public static String getUnit(String feature, NetcdfFile ncfile) {
		Variable variable = ncfile.findVariable(feature);
		return getUnit(feature, variable);
	}

	public static String getUnit(String feature, Variable variable) {
		Attribute units = variable.findAttribute("units");
		return units.getStringValue();
	}

	public static double getNoDataValue(NetcdfFile netcdfFile) {
		// TODO Auto-generated method stub
		return 0;
	}
}
