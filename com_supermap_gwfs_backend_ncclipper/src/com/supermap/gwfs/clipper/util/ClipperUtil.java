package com.supermap.gwfs.clipper.util;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import com.supermap.gwfs.clipper.entity.NetcdfData;
import com.supermap.gwfs.clipper.entity.ClipperEntity;
import com.supermap.gwfs.clipper.entity.FileParameter;
import com.supermap.gwfs.clipper.entity.SizeParameter;

public class ClipperUtil {
	public static Logger logger = Logger.getLogger(ClipperUtil.class);

	/**
	 * 
	 * @param files 文件集合
	 * @return 文件参数实体集合
	 */
	public static FileParameter getFileParameter(File[] files) {
		FileParameter fileParameter = new FileParameter();
		String[] plFilePaths = new String[2];
		String[] sfc1FilePaths = new String[2];
		String[] sfc2FilePaths = new String[2];
		String plFileOutName = null;
		String sfc1FileOutName = null;
		String sfc2FileOutName = null;
		for (int i = 0; i < files.length; i++) {
			String fileName = files[i].getName();
			String outNames[] = fileName.split("_");
			outNames[3] = outNames[3].substring(0, outNames[3].indexOf("."));
			String filePath = files[i].getAbsolutePath();
			filePath = filePath.replace("\\", "/");
			String[] filePaths = filePath.split("/");
			String timeSequrence = "00".equals(filePaths[filePaths.length - 3]) ? "08" : "20";
			if (fileName.contains("pl")) {
				if (fileName.contains("cf")) {
					plFilePaths[0] = filePath;
					plFileOutName = "ECWMF_" + "pl_" + outNames[2] + "_" + timeSequrence + "_" + outNames[3] + ".nc";
				}
				if (fileName.contains("pf")) {
					plFilePaths[1] = filePath;
				}
			}
			if (fileName.contains("sfc") && fileName.contains("grib1")) {
				if (fileName.contains("cf")) {
					sfc1FilePaths[0] = filePath;
					sfc1FileOutName = "ECWMF_" + "sfc_1_" + outNames[2] + "_" + timeSequrence + "_" + outNames[3] + ".nc";
				}
				if (fileName.contains("pf")) {
					sfc1FilePaths[1] = filePath;
				}
			}
			if (fileName.contains("sfc") && fileName.contains("grib2")) {
				if (fileName.contains("cf")) {
					sfc2FilePaths[0] = filePath;
					sfc2FileOutName = "ECWMF_" + "sfc_2_" + outNames[2] + "_" + timeSequrence + "_" + outNames[3] + ".nc";
				}
				if (fileName.contains("pf")) {
					sfc2FilePaths[1] = filePath;
				}
			}
		}
		fileParameter.setPlFileName(plFilePaths);
		fileParameter.setSfc1FileName(sfc1FilePaths);
		fileParameter.setSfc2FileName(sfc2FilePaths);
		fileParameter.setPlFileOutName(plFileOutName);
		fileParameter.setSfc1FileOutName(sfc1FileOutName);
		fileParameter.setSfc2FileOutName(sfc2FileOutName);

		return fileParameter;

	}

	public static ClipperEntity getClipperEntity(FileParameter fileParameter, SizeParameter sizeParameter, String outncPath) {

		ClipperEntity clipperEntity = new ClipperEntity();

		String plOut = outncPath + "/" + fileParameter.getPlFileOutName();
		String sfc1Out = outncPath + "/" + fileParameter.getSfc1FileOutName();
		String sfc2Out = outncPath + "/" + fileParameter.getSfc2FileOutName();
		NetcdfFile plncFile = null;
		NetcdfFile sfc1ncFile = null;
		NetcdfFile sfc2ncFile = null;
		try {
			clipperEntity.setPlOutncfile(NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, plOut));
			clipperEntity.setSfc1Outncfile(NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, sfc1Out));
			clipperEntity.setSfc2Outncfile(NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, sfc2Out));
			
			plncFile = NetcdfFile.open(fileParameter.getPlFileName()[0]);
			sfc1ncFile = NetcdfFile.open(fileParameter.getSfc1FileName()[0]);
			sfc2ncFile = NetcdfFile.open(fileParameter.getSfc2FileName()[0]);
			
			clipperEntity.setPlCellBounds(NetCDFAllUtil.getCellBounds(plncFile, sizeParameter.getPlBounds()));
			clipperEntity.setSfc1CellBounds(NetCDFAllUtil.getCellBounds(sfc1ncFile, sizeParameter.getSfc1Bounds()));
			clipperEntity.setSfc2CellBounds(NetCDFAllUtil.getCellBounds(sfc2ncFile, sizeParameter.getSfc2Bounds()));
			
			clipperEntity.setPlsizeCell(NetCDFAllUtil.getCellSize(plncFile));
			clipperEntity.setSfc1sizeCell(NetCDFAllUtil.getCellSize(sfc1ncFile));
			clipperEntity.setSfc2sizeCell(NetCDFAllUtil.getCellSize(sfc2ncFile));
			//设置NoDataValue -32767  -32767;
			clipperEntity.setPlNoDataValue(-32767);
			clipperEntity.setSfc1NoDataValue(-32767);
			clipperEntity.setSfc2NoDataValue(-32767);
		}catch (Exception e) {
			logger.error("nc类型数据操作异常 , 异常  : " + e);
		}
		finally
		{
			try
			{
				if (plncFile != null)
				{
					plncFile.close();
				}
				if (sfc1ncFile != null)
				{
					sfc1ncFile.close();
				}
				if (sfc2ncFile != null)
				{
					sfc2ncFile.close();
				}
//				if (clipperEntity.getPlOutncfile() != null)
//					clipperEntity.getPlOutncfile().close();
//				if (clipperEntity.getSfc1Outncfile() != null)
//					clipperEntity.getSfc1Outncfile().close();
//				if (clipperEntity.getSfc2Outncfile() != null)
//					clipperEntity.getSfc2Outncfile().close();
			}
			catch (IOException e)
			{
				logger.error("关闭异常 ，异常 : " + e);
			}
		}
		
		
		
		return clipperEntity;

	}

	public static NetcdfData getClipperData(FileParameter fileParameter,ClipperEntity clipperEntity, SizeParameter sizeParameter) {

		NetcdfData clipperData = new NetcdfData();
		NetcdfFile plcfFile = null;
		NetcdfFile plpfFile = null;
		NetcdfFile sfc1cfFile = null;
		NetcdfFile sfc1pfFile = null;
		NetcdfFile sfc2cfFile1 = null;
		NetcdfFile sfc2pfFile1 = null;
		NetcdfFile sfc2cfFile = null;
		NetcdfFile sfc2pfFile = null;
		try {
			Map<String, String> unitMap = new HashMap<String, String>();
			/*
			 * _pl===》大气层
			 */
			// 控制预报
//			// 4维,[longitude = 181;, latitude = 111;, level = 11;, time = 1;]
			plcfFile = NetcdfFile.open(fileParameter.getPlFileName()[0]);
			List<Integer> cfncLvls = NetCDFAllUtil.getLevelIndex(plcfFile, sizeParameter.getPlLayer());
			// 成员预报
 			// 5维,[longitude = 181;, latitude = 111;, level = 11;, number = 50;, time = 1;]
			plpfFile = NetcdfFile.open(fileParameter.getPlFileName()[1]);
			List<String> plLayers = sizeParameter.getPlLayer();
			List<Integer> pfncLvls = NetCDFAllUtil.getLevelIndex(plpfFile, plLayers);
			List<Integer> pfncNumbers = NetCDFAllUtil.getNumberIndex(plpfFile);
			List<String> plFeature = sizeParameter.getPlFeature();
			Rectangle2D plcellBounds = clipperEntity.getPlCellBounds();
			//世界时 --> 北京时
			int time = NetCDFAllUtil.getDimTime(plpfFile) + 8;

			clipperData.setTime(time);
			Map<String, Array[][]> pldataMap = new HashMap<String, Array[][]>();// Array[number][lvl]
			Map<String, Double[]> plfactors = new HashMap<String, Double[]>();
			for (String feature : plFeature) {
				Array[][] datas = new Array[sizeParameter.getNumber() + 1][plLayers.size()];
				Variable cfvariable = plcfFile.findVariable(feature.toLowerCase());
				unitMap.put(feature, NetCDFAllUtil.getUnit(feature, cfvariable));// 设置单位
				double cfadd_offset = 0;
				double cfscale_factor = 0;
				if (cfvariable == null) {
					logger.error(plcfFile.getLocation() + "\n   	Variable: " + feature + "要素 不存在");
				} else {
					cfadd_offset = NetCDFAllUtil.getAdd_offset(cfvariable);
					cfscale_factor = NetCDFAllUtil.getScale_factor(cfvariable);
					// 裁切
					for (int i = 0; i < cfncLvls.size(); i++) {// [longitude = 181;, latitude = 111;, level = 11;, time = 1;]
						int lvlIndex = cfncLvls.get(i);
						if (lvlIndex < 0) {
							logger.error(plcfFile.getLocation() + ":" + feature + " 要素 " + plLayers.get(i) + " 层不存在");
							continue;
						}
						int[] origin = new int[] { 0, lvlIndex, (int) plcellBounds.getY(), (int) plcellBounds.getX() };
						int[] size = new int[] { 1, 1, (int) plcellBounds.getHeight(), (int) plcellBounds.getWidth() };
						// 裁切数据
						Array cfdata2D;

						cfdata2D = cfvariable.read(origin, size).reduce();

						datas[0][i] = cfdata2D;
					}
				}

				// -----------------------------------------------------------------
				Variable pfvariable = plpfFile.findVariable(feature.toLowerCase());
				double pfadd_offset = 0;
				double pfscale_factor = 0;
				if (pfvariable == null) {
					logger.error(plcfFile.getLocation() + "\n   	Variable: " + feature + "要素 不存在!");
				} else {
					pfadd_offset = NetCDFAllUtil.getAdd_offset(pfvariable);
					pfscale_factor = NetCDFAllUtil.getScale_factor(pfvariable);
					// 裁切
					for (int n = 0; n < sizeParameter.getNumber(); n++) {
						for (int i = 0; i < pfncLvls.size(); i++) {
							int lvlIndex = pfncLvls.get(i);
							if (lvlIndex < 0) {
								logger.error(plpfFile.getLocation() + ":" + feature + " 要素 " + plLayers.get(i) + " 层不存在");
								continue;
							}
							int[] origin = new int[] { 0, pfncNumbers.get(n), lvlIndex, (int) plcellBounds.getY(), (int) plcellBounds.getX() };
							int[] size = new int[] { 1, 1, 1, (int) plcellBounds.getHeight(), (int) plcellBounds.getWidth() };
							// 裁切数据
							Array pfdata2D;

							pfdata2D = pfvariable.read(origin, size).reduce();

							datas[n + 1][i] = pfdata2D;
						}
					}
				}

				pldataMap.put(feature, datas);
				Double[] factor = new Double[4];
				factor[0] = cfscale_factor;
				factor[1] = cfadd_offset;
				factor[2] = pfscale_factor;
				factor[3] = pfadd_offset;
				plfactors.put(feature, factor);
			}
			clipperData.setPlDataMap(pldataMap);
			clipperData.setPlfactors(plfactors);

			// cf_sfc===》控制预报，海平面
			// 控制预报
//			sfc1cfFile = clipperEntity.getSfc1ncfiles()[0];// 3维,[longitude = 281;, latitude = 161;, time = 1;]
			sfc1cfFile = NetcdfFile.open(fileParameter.getSfc1FileName()[0]);
			// 成员预报 pf_sfc===》
//			sfc1pfFile = clipperEntity.getSfc1ncfiles()[1];// 4维,[longitude = 281;, latitude = 161;, number = 50;, time = 1;]
			sfc1pfFile = NetcdfFile.open(fileParameter.getSfc1FileName()[1]);
			List<String> sfc1Feature = sizeParameter.getSfc1Feature();
			Rectangle2D sfc1CellBounds = clipperEntity.getSfc1CellBounds();

			List<Integer> sfc1ncNumbers = NetCDFAllUtil.getNumberIndex(sfc1pfFile);
			Map<String, Array[][]> sfc1dataMap = new HashMap<String, Array[][]>();
			Map<String, Double[]> sfc1factors = new HashMap<String, Double[]>();

			for (String feature : sfc1Feature) {
				Array[][] datas = new Array[sizeParameter.getNumber() + 1][1];
				Variable cfvariable = sfc1cfFile.findVariable(feature.toLowerCase());
				unitMap.put(feature, NetCDFAllUtil.getUnit(feature, cfvariable));// 设置单位
				double cfadd_offset = 0;
				double cfscale_factor = 0;
				if (cfvariable == null) {
					logger.error(sfc1cfFile.getLocation() + "\n   Variable: " + feature + "要素 不存在！");
				} else {
					cfadd_offset = NetCDFAllUtil.getAdd_offset(cfvariable);
					cfscale_factor = NetCDFAllUtil.getScale_factor(cfvariable);
					int[] origin = new int[] { 0, (int) sfc1CellBounds.getY(), (int) sfc1CellBounds.getX() };
					int[] size = new int[] { 1, (int) sfc1CellBounds.getHeight(), (int) sfc1CellBounds.getWidth() };
					// 裁切数据
					Array cfdata2D;
					cfdata2D = cfvariable.read(origin, size).reduce();
					datas[0][0] = cfdata2D;
				}
				Variable pfvariable = sfc1pfFile.findVariable(feature.toLowerCase());
				double pfadd_offset = 0;
				double pfscale_factor = 0;
				if (pfvariable == null) {
					logger.error(sfc1pfFile.getLocation() + "\n   Variable: " + feature + "要素 不存在!");
				} else {
					pfadd_offset = NetCDFAllUtil.getAdd_offset(pfvariable);
					pfscale_factor = NetCDFAllUtil.getScale_factor(pfvariable);
					// 裁切
					for (int n = 0; n < sizeParameter.getNumber(); n++) {
						int[] pforigin = new int[] { 0, sfc1ncNumbers.get(n), (int) sfc1CellBounds.getY(), (int) sfc1CellBounds.getX() };
						int[] pfsize = new int[] { 1, 1, (int) sfc1CellBounds.getHeight(), (int) sfc1CellBounds.getWidth() };
						// 裁切数据
						Array pfdata2D = pfvariable.read(pforigin, pfsize).reduce();
						datas[n + 1][0] = pfdata2D;
					}
				}
				sfc1dataMap.put(feature, datas);
				Double[] factor = new Double[4];
				factor[0] = cfscale_factor;
				factor[1] = cfadd_offset;
				factor[2] = pfscale_factor;
				factor[3] = pfadd_offset;
				sfc1factors.put(feature, factor);
			}
			clipperData.setSfc1DataMap(sfc1dataMap);
			clipperData.setSfc1factors(sfc1factors);

			// ===》海平面
			// 控制预报
			// 3维,[longitude = 281;, latitude = 161;, time = 1;] // 成员预报
			// 4维,[longitude = 281;, latitude = 161;, number = 50;, time = 1;]
			sfc2cfFile1 = NetcdfFile.open(fileParameter.getSfc2FileName()[0]);
			sfc2pfFile1 = NetcdfFile.open(fileParameter.getSfc2FileName()[1]);
			
			List<String> sfc2Feature = sizeParameter.getSfc2Feature();
			Rectangle2D sfc2CellBounds = clipperEntity.getSfc2CellBounds();
			Map<String, Array[][]> sfc2dataMap = new HashMap<String, Array[][]>();
			Map<String, Double[]> sfc2factors = new HashMap<String, Double[]>();
			List<Integer> sfc2ncNumbers1 = NetCDFAllUtil.getNumberIndex(sfc2pfFile1);
			List<Integer> sfc2ncNumbers;
			for (String feature : sfc2Feature) {
				if (feature.equalsIgnoreCase("kx")) {
					sfc2cfFile = sfc2cfFile1;
					sfc2pfFile = sfc2pfFile1;
					sfc2ncNumbers = sfc2ncNumbers1;
				} else {
					sfc2cfFile = sfc1cfFile;
					sfc2pfFile = sfc1pfFile;
					sfc2ncNumbers = sfc1ncNumbers;
				}
				Array[][] datas = new Array[sizeParameter.getNumber() + 1][1];
				Variable cfvariable = sfc2cfFile.findVariable(feature.toLowerCase());
				unitMap.put(feature, NetCDFAllUtil.getUnit(feature, cfvariable));// 设置单位
				double cfadd_offset = 0;
				double cfscale_factor = 0;
				if (cfvariable == null) {
					logger.error(sfc2cfFile.getLocation() + "\n   Variable: " + feature + "要素 不存在!");
				} else {
					cfadd_offset = NetCDFAllUtil.getAdd_offset(cfvariable);
					cfscale_factor = NetCDFAllUtil.getScale_factor(cfvariable);

					int[] origin = new int[] { 0, (int) sfc2CellBounds.getY(), (int) sfc2CellBounds.getX() };
					int[] size = new int[] { 1, (int) sfc2CellBounds.getHeight(), (int) sfc2CellBounds.getWidth() };
					// 裁切数据
					Array cfdata2D = cfvariable.read(origin, size).reduce();

					datas[0][0] = cfdata2D;

				}
				Variable pfvariable = sfc2pfFile.findVariable(feature.toLowerCase());
				double pfadd_offset = 0;
				double pfscale_factor = 0;
				if (pfvariable == null) {
					logger.error(sfc2pfFile.getLocation() + "\n   Variable: " + feature + "要素 不存在!");
				} else {
					pfadd_offset = NetCDFAllUtil.getAdd_offset(pfvariable);
					pfscale_factor = NetCDFAllUtil.getScale_factor(pfvariable);
					// 裁切
					for (int n = 0; n < sizeParameter.getNumber(); n++) {
						int[] pforigin = new int[] { 0, sfc2ncNumbers.get(n), (int) sfc2CellBounds.getY(), (int) sfc2CellBounds.getX() };
						int[] pfsize = new int[] { 1, 1, (int) sfc2CellBounds.getHeight(), (int) sfc2CellBounds.getWidth() };
						// 裁切数据
						Array pfdata2D = pfvariable.read(pforigin, pfsize).reduce();
						datas[n + 1][0] = pfdata2D;
					}
				}

				sfc2dataMap.put(feature, datas);
				Double[] factor = new Double[4];
				factor[0] = cfscale_factor;
				factor[1] = cfadd_offset;
				factor[2] = pfscale_factor;
				factor[3] = pfadd_offset;
				sfc2factors.put(feature, factor);
			}
			clipperData.setSfc2DataMap(sfc2dataMap);
			clipperData.setSfc2factors(sfc2factors);

			clipperData.setUnit(unitMap);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (InvalidRangeException e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				if (plcfFile != null)
				{
					plcfFile.close();
				}
				if (plpfFile != null)
				{
					plpfFile.close();
				}
				if (sfc1cfFile != null)
				{
					sfc1cfFile.close();
				}
				if (sfc1pfFile != null)
				{
					sfc1pfFile.close();
				}
				if (sfc2cfFile1 != null)
				{
					sfc2cfFile1.close();
				}
				if (sfc2pfFile1 != null)
				{
					sfc2pfFile1.close();
				}
				if (sfc2cfFile != null)
				{
					sfc2cfFile.close();
				}
				if (sfc2pfFile != null)
				{
					sfc2pfFile.close();
				}
				
			} catch (IOException e) {
				logger.error("获取原始nc数据异常 ， 异常 : " + e);
			}
		}
		return clipperData;
	}
}
