package com.supermap.gwfs.grib2live.service;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.beanutils.BeanUtils;

import ucar.nc2.grib.grib2.Grib2Gds;
import ucar.nc2.grib.grib2.Grib2Gds.LatLon;
import ucar.nc2.grib.grib2.Grib2Parameter;
import ucar.nc2.grib.grib2.Grib2Pds;
import ucar.nc2.grib.grib2.Grib2Record;
import ucar.nc2.grib.grib2.Grib2RecordScanner;
import ucar.nc2.grib.grib2.Grib2SectionDataRepresentation;
import ucar.nc2.grib.grib2.Grib2SectionGridDefinition;
import ucar.nc2.grib.grib2.Grib2SectionIdentification;
import ucar.nc2.grib.grib2.Grib2SectionIndicator;
import ucar.nc2.grib.grib2.Grib2SectionProductDefinition;
import ucar.nc2.grib.grib2.table.NcepLocalParams;
import ucar.unidata.io.RandomAccessFile;

import com.supermap.gwfs.grib2live.controller.BaseController;
import com.supermap.gwfs.grib2live.entity.GridsData;
import com.supermap.gwfs.grib2live.util.DateUtil;
import com.supermap.gwfs.grib2live.util.Logger;

public class GribService {

	private Logger logger = Logger.getLogger(this.getClass());

	public synchronized List<GridsData> listAllGrib(String path) {
		List<GridsData> gridsDatas = new ArrayList<GridsData>();
		RandomAccessFile raf = null;
		try {
			// 重庆
			Rectangle2D chqBounds = BaseController.getParameter().getChongqingBounds();
			String unit = "";
			String element = null;
			String elementVal;
			int level;
			int rows = 0;
			int cols = 0;
			float left = 0;// 起始经纬度
			float bottom = 0;
			float deltaX = 0;
			float deltaY = 0;
			Date date;
			int dimTime;
	
			float[] rangedata;
			GridsData data;
			String fileName;
			String filePath;
			Timestamp create_time;
			// =============
			int hour;
			LatLon ll;
			float la1;
			float lo1;
			String time = null;
			String rootPath = BaseController.getParameter().getRootPath();
			int nxRaw;
			int startX = 0;
			int startY = 0;
			float[] dValue;
			int forecastDate = 0;
			// ============
			// 获取要素 创建时间---------------
			String s1 = path.substring(path.lastIndexOf("-") + 1);
	
			String[] ss = s1.split("_");
			element = ss[0];
			elementVal = element;
			String referenceString = ss[1];
			path = path.replace("\\", "/");
			String create = path.substring(path.lastIndexOf("/")).split("_")[4];
			create_time = new Timestamp(DateUtil.StringToDate(create, "yyyyMMddHHmmss").getTime() + TimeUnit.HOURS.toMillis(8));
			// ---------------
		
			raf = new RandomAccessFile(path, "r");
			Grib2RecordScanner scan = new Grib2RecordScanner(raf);
			logger.info("ZJ:being read Data!");
			while (scan.hasNext()) {
				Grib2Record gr2 = scan.next();
				Grib2SectionIdentification ids = gr2.getId();
				// 处理时间
				date = new Date(DateUtil.StringToDate(referenceString, "yyyyMMddHHmm").getTime());
				hour = ids.getHour();
				Grib2SectionGridDefinition gds = gr2.getGDSsection();
				Grib2Gds tempGds = gds.getGDS();
				nxRaw = tempGds.getNxRaw();
				if (tempGds.isLatLon()) {
					// 经纬度范围
					ll = (LatLon) tempGds;
					la1 = ll.la1;
					lo1 = ll.lo1;
					bottom = (float) chqBounds.getY();
					left = (float) chqBounds.getX();
					deltaY = ll.deltaLat;
					deltaX = ll.deltaLon;
					rows = (int) ((float) chqBounds.getHeight() / deltaY) + 1;
					cols = (int) ((float) chqBounds.getWidth() / deltaX) + 1;
					startX = (int) ((float) (chqBounds.getX() - lo1) / deltaX);
					startY = (int) ((float) (chqBounds.getY() - la1) / deltaY);
				}

				Grib2SectionProductDefinition pds = gr2.getPDSsection();
				Grib2Pds tempPds = pds.getPDS();
				
				dimTime = getDimTime(date, hour, 0);
				//20160913
				forecastDate = getDimTime(date, hour);
				//
				level = 0;

				Grib2SectionIndicator iss = gr2.getIs();
				int d = iss.getDiscipline();
				int c = tempPds.getParameterCategory();
				int n = tempPds.getParameterNumber();
				if ("EDA10".equalsIgnoreCase(element)) {
					if (d == 0 && c == 2 && n == 2) {
						// 风速U风量
						elementVal = "UEDA10";
					}
					if (d == 0 && c == 2 && n == 3) {
						// 风速V风量
						elementVal = "VEDA10";
					}
				}

				Grib2Parameter param = NcepLocalParams.getParameter(d, c, n);
				if (param != null) {
					unit = param.getUnit();
				}

				Grib2SectionDataRepresentation drs = gr2.getDataRepresentationSection();
				dValue = gr2.readData(raf, drs.getStartingPosition());
				// 获取一定范围
				rangedata = getRangeData(dValue, rows, cols, startX, startY, nxRaw);

				// 处理文件名，文件路径START------------------------------------
				time = DateUtil.DateToString(date, "yyyyMMdd");
				filePath = rootPath+ "/" + "central/live/" + element + "/" + time + "/" + level + "/live";
				fileName = time + "_" + hour + ".nc";
				data = new GridsData(unit, rows, cols, dimTime, date, elementVal, hour , level, rangedata, left, bottom, deltaX, deltaY, fileName, filePath, create_time, forecastDate);
				// /mnt/data/local/ec_thin/TEM_Max/20160708/08/0/curvefitting/20160708_48.nc
				if (createFolder(filePath)) {
					filePath = filePath + "/" + fileName;
				} else {
					logger.error("dirs create is fail," + filePath);
				}
				data = new GridsData(unit, rows, cols, dimTime, date, elementVal, hour, level, rangedata, left, bottom, deltaX, deltaY, fileName, filePath, create_time, forecastDate);
				gridsDatas.add(data);
				
			}

			gridsDatas = processingData(element, unit, gridsDatas, rootPath, time);
		} catch (Exception e) {
			logger.error("ZJ:read grid2 is error , error : " + e.getMessage(), e);
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					logger.error("ZJ:closed is error!" + e.getMessage(), e);
				}
			}
		}
		return gridsDatas;
	}

	private List<GridsData> processingData(String element, String unit, List<GridsData> gridsDatas, String rootPath, String time) {
		for (GridsData gridsData : gridsDatas) {
			Map<String, float[]> elementValue = new HashMap<String, float[]>();
			elementValue.put(gridsData.getElement(), gridsData.getdValue());
			gridsData.setElementValue(elementValue);
		}
		if ("ER03".equalsIgnoreCase(element)) {
			// 处理降水 START------------------------------------
//			gridsDatas = doER03(gridsDatas, element, time, rootPath);
//			gridsDatas = doER03To24(gridsDatas, element, time, rootPath);
			// 处理降水 END------------------------------------
		}

		if ("EDA10".equalsIgnoreCase(element)) {
//			gridsDatas = doEDA10(gridsDatas, element);
		}
		// 单位
		if ("K".equalsIgnoreCase(unit)) {
			float[] value;
			for (int i = 0; i < gridsDatas.size(); i++) {
				value = gridsDatas.get(i).getdValue();
				for (int j = 0; j < value.length; j++) {
					value[j] = value[j] - 273.15f;
				}
				gridsDatas.get(i).setUnit("C");
			}
		}

		return gridsDatas;

	}

//	private List<GridsData> doEDA10(List<GridsData> gridsDatas, String element) {
//		List<GridsData> gds = new ArrayList<GridsData>();
//		// 按时效分类
//		Map<Integer, List<GridsData>> gridsDataMap = classifyByValid(gridsDatas);
//		for (Map.Entry<Integer, List<GridsData>> map : gridsDataMap.entrySet()) {
//			List<GridsData> gDatas = map.getValue();
//			Map<String, float[]> elementValue = new HashMap<String, float[]>();
//			elementValue.put(gDatas.get(0).getElement(), gDatas.get(0).getdValue());
//			elementValue.put(gDatas.get(1).getElement(), gDatas.get(1).getdValue());
//
//			GridsData gridsData = gDatas.get(0);
//			gridsData.setElementValue(elementValue);
//			gridsData.setElement(element);
//			gds.add(gridsData);
//		}
//		return gds;
//
//	}

//	private Map<Integer, List<GridsData>> classifyByValid(List<GridsData> gridsDatas) {
//		List<Integer> validList = new ArrayList<Integer>();
//
//		int valid;
//		for (GridsData gd : gridsDatas) {
//			valid = gd.getValid();
//			if (!validList.contains(valid)) {
//				validList.add(valid);
//			}
//		}
//		Map<Integer, List<GridsData>> validAndGridsDatas = new HashMap<Integer, List<GridsData>>();
//		for (Integer v : validList) {
//			List<GridsData> gDatas = new ArrayList<GridsData>();
//			for (GridsData gd : gridsDatas) {
//				if (v == gd.getValid()) {
//					gDatas.add(gd);
//				}
//			}
//			validAndGridsDatas.put(v, gDatas);
//		}
//		return validAndGridsDatas;
//	}

//	private List<GridsData> doER03(List<GridsData> gridsDatas, String element, String time, String rootPath) {
//
//		int valid;
//		String fileName;
//		String filePath;
//
//		element = "ER12";
//		Map<Integer, GridsData> gridsDataMap = new HashMap<Integer, GridsData>();
//		for (GridsData gridsData : gridsDatas) {
//			gridsDataMap.put(gridsData.getValid(), gridsData);
//		}
//		for (Map.Entry<Integer, GridsData> map : gridsDataMap.entrySet()) {
//			int ER03Valid = map.getKey();
//			//20161107
//			if (ER03Valid % 12 == 0) {
//				try {
//					valid = ER03Valid;
//
//					GridsData gridsData12 = new GridsData();
//					GridsData gridsData9 = new GridsData();
//					GridsData gridsData6 = new GridsData();
//					GridsData gridsData3 = new GridsData();
//
//					BeanUtils.copyProperties(gridsData12, gridsDataMap.get(ER03Valid));
//					BeanUtils.copyProperties(gridsData9, gridsDataMap.get(ER03Valid - 3));
//					BeanUtils.copyProperties(gridsData6, gridsDataMap.get(ER03Valid - 6));
//					BeanUtils.copyProperties(gridsData3, gridsDataMap.get(ER03Valid - 9));
//
//					float[] sumValue = getSumValue(gridsData12.getdValue(), gridsData9.getdValue(), gridsData6.getdValue(), gridsData3.getdValue());
//					
//					// 路径
//					filePath = rootPath + "/" + "central/" + element + "/" + time + "/" + gridsData12.getSequence() + "/" + gridsData12.getLevel() + "/central";
//					fileName = time + gridsData12.getSequence() + "_" + valid + ".nc";
//					// /mnt/data/local/ec_thin/TEM_Max/20160708/08/0/curvefitting/20160708_48.nc
//					if (createFolder(filePath)) {
//						filePath = filePath + "/" + fileName;
//					} else {
//						logger.error("ZJ:create dris is fail , " + filePath);
//					}
//					Map<String, float[]> elementValue = new HashMap<String, float[]>();
//					elementValue.put(element, sumValue);
//					gridsData12.setElementValue(elementValue);
//
//					gridsData12.setdValue(sumValue);
//					gridsData12.setElement(element);
//					gridsData12.setFilePath(filePath);
//					gridsDatas.add(gridsData12);
//					
//				} catch (Exception e) {
//					logger.error("ZJ:ER12 is error ! error : " + e.getMessage(), e);
//				} 
//			}
//		}
//		return gridsDatas;
//	}
	
	
//	private List<GridsData> doER03To24(List<GridsData> gridsDatas, String element, String time, String rootPath) {
//
//		int valid;
//		String fileName;
//		String filePath;
//
//		element = "ER24";
//		Map<Integer, GridsData> gridsDataMap = new HashMap<Integer, GridsData>();
//		for (GridsData gridsData : gridsDatas) {
//			if (gridsData.getFilePath().contains("ER03"))
//			{
//				gridsDataMap.put(gridsData.getValid(), gridsData);
//			}
//		}
//		for (Map.Entry<Integer, GridsData> map : gridsDataMap.entrySet()) {
//			int ER03Valid = map.getKey();
//			//20161107
//			if (ER03Valid % 24 == 0 && ER03Valid != 0) {
//				try {
//					valid = ER03Valid;
//
//					GridsData gridsData24 = new GridsData();
//					GridsData gridsData21 = new GridsData();
//					GridsData gridsData18 = new GridsData();
//					GridsData gridsData15 = new GridsData();
//					GridsData gridsData12 = new GridsData();
//					GridsData gridsData9 = new GridsData();
//					GridsData gridsData6 = new GridsData();
//					GridsData gridsData3 = new GridsData();
//
//					BeanUtils.copyProperties(gridsData24, gridsDataMap.get(ER03Valid));
//					BeanUtils.copyProperties(gridsData21, gridsDataMap.get(ER03Valid - 3));
//					BeanUtils.copyProperties(gridsData18, gridsDataMap.get(ER03Valid - 6));
//					BeanUtils.copyProperties(gridsData15, gridsDataMap.get(ER03Valid - 9));
//					BeanUtils.copyProperties(gridsData12, gridsDataMap.get(ER03Valid - 12));
//					BeanUtils.copyProperties(gridsData9, gridsDataMap.get(ER03Valid - 15));
//					BeanUtils.copyProperties(gridsData6, gridsDataMap.get(ER03Valid - 18));
//					BeanUtils.copyProperties(gridsData3, gridsDataMap.get(ER03Valid - 21));
//					
//					float[] sumValue1 = getSumValue(gridsData24.getdValue(), gridsData21.getdValue(), gridsData18.getdValue(), gridsData15.getdValue());
//					float[] sumValue2 = getSumValue(gridsData12.getdValue(), gridsData9.getdValue(), gridsData6.getdValue(), gridsData3.getdValue());
//					float[] sumValue = getSumValue(sumValue1 ,sumValue2);
//					
//					
//					// 路径
//					filePath = rootPath + "/" + "central/" + element + "/" + time + "/" + gridsData24.getSequence() + "/" + gridsData24.getLevel() + "/central";
//					fileName = time + gridsData24.getSequence() + "_" + valid + ".nc";
//					// /mnt/data/local/ec_thin/TEM_Max/20160708/08/0/curvefitting/20160708_48.nc
//					if (createFolder(filePath)) {
//						filePath = filePath + "/" + fileName;
//					} else {
//						logger.error("ZJ:create dris is fail , " + filePath);
//					}
//					Map<String, float[]> elementValue = new HashMap<String, float[]>();
//					elementValue.put(element, sumValue);
//					gridsData24.setElementValue(elementValue);
//
//					gridsData24.setdValue(sumValue);
//					gridsData24.setElement(element);
//					gridsData24.setFilePath(filePath);
//					gridsDatas.add(gridsData24);
//					
//				} catch (Exception e) {
//					logger.error("ZJ:ER12 is error ! error : " + e.getMessage(), e);
//				} 
//			}
//		}
//		return gridsDatas;
//	}



//	private float[] getSumValue(float[] sumValue1, float[] sumValue2)
//	{
//		float[] sumValue = new float[sumValue1.length];
//		for (int i = 0; i < sumValue.length; i++) {
//			sumValue[i] = sumValue1[i] + sumValue2[i];
//		}
//		return sumValue;
//	}
//
//	private float[] getSumValue(float[] value1, float[] value2, float[] value3, float[] value4) {
//		float[] sumValue = new float[value1.length];
//		for (int i = 0; i < sumValue.length; i++) {
//			sumValue[i] = value1[i] + value2[i] + value3[i] + value4[i];
//		}
//		return sumValue;
//	}

	private int getDimTime(Date date, int hour, int valid) {
		long since = DateUtil.StringToDate("1900-01-01", "yyyy-MM-dd").getTime();
		long forecastTime = date.getTime() + TimeUnit.HOURS.toMillis(hour + valid);
		Long dimeTime = TimeUnit.MILLISECONDS.toHours(forecastTime - since);
		return dimeTime.intValue();
	}
	private int getDimTime(Date date, int hour) {
		long since = DateUtil.StringToDate("1970-01-01", "yyyy-MM-dd").getTime();
		long forecastTime = date.getTime() + TimeUnit.HOURS.toMillis(hour);
		Long dimeTime = TimeUnit.MILLISECONDS.toHours(forecastTime - since);
		return dimeTime.intValue();
	}

	/**
	 * 如果路径存在，返回true
	 * 如果路径不存在，创建路径
	 * @param path
	 * @return
	 */
	private synchronized boolean createFolder(String path) {
		boolean flag = false;
		File folder = new File(path);
		if (folder.exists()) {
			flag = true;
		} else {
			//20160928
			String[] paths = path.split("/");
			StringBuffer dirs = new StringBuffer();
			for (String string : paths) {
				dirs.append(string + "/");
				File file = new File(dirs.toString());
				if(!file.isDirectory())
				{
					flag = file.mkdir();
					file.setReadable(true,false);
					file.setWritable(true, false);
				}
						
				System.out.println(flag);
				
			}
			/*flag = folder.mkdirs();
			//20160928
			folder.setReadable(true,false);
			folder.setWritable(true,false);*/
		}
		logger.info("ZJ:create " + path + " dirs is : " + flag);
		return flag;
	}

	/**
	 * 
	 * @param dataFloat
	 * @param rows 目标范围数据的行数
	 * @param cols 目标范围数据的列数
	 * @param startX 
	 * @param startY
	 * @param nxRaw 
	 * @return
	 */
	private static int count = 0;
	private float[] getRangeData(float[] dataFloat, int rows, int cols, int startX, int startY, int nxRaw) {
//		System.out.println(count);
		count++;
		float[] rangeData = new float[rows * cols];
		for (int i = 0; i < rows; i++) {
			// 源数组，源数组开始位置，目标数组，目标数组开始位置，长度
			
			
			System.arraycopy(dataFloat, nxRaw * startY + startX + i * nxRaw, rangeData, i * cols, cols);
		}
		return rangeData;
	}
}
