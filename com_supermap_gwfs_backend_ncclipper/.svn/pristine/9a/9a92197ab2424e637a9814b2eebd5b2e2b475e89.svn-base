package com.supermap.gwfs.clipper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.supermap.gwfs.clipper.entity.ClipperEntity;
import com.supermap.gwfs.clipper.entity.FileParameter;
import com.supermap.gwfs.clipper.entity.NetcdfData;
import com.supermap.gwfs.clipper.entity.SizeParameter;
import com.supermap.gwfs.clipper.util.ClipperUtil;
import com.supermap.gwfs.clipper.util.FileUtil;
import com.supermap.gwfs.clipper.util.Logger;
import com.supermap.gwfs.clipper.util.ReadXML;

public class ClipData {
	private Logger logger = Logger.getLogger(ClipData.class);
	private SizeParameter sizeParameter;
	private FileParameter fileParameters;
	private ClipperEntity clipperEntity;
	private NetcdfData netcdfData;

	public boolean getData(String rootPath, String time, String timeSequrence, String timeValid) {
		/*
		 * 1.拿到参数
		 */
		sizeParameter = getSizeParameterData();

		if (Integer.parseInt(timeValid) > sizeParameter.getTime())
			return false;
		// 设置特殊件情况参数
		setSizeParemeter(timeValid, sizeParameter);
		/*
		 * 2.读文件
		 */
		File[] files = getFiles(rootPath, time, timeSequrence, timeValid);
		if (files == null) {
			logger.error("文件为空-:" + time + "_" + timeValid);
			return false;
		} else if (files.length != 6) {
			logger.error("在查找文件夹：" + rootPath + "/" + time + "/" + timeSequrence + " 关键字：" + "_" + timeValid + ".");
			logger.error("待待裁切的nc文件不全，文件个数为-:" + files.length);
			return false;
		}
		/*
		 * 3.遍历文件
		 */
		fileParameters = ClipperUtil.getFileParameter(files);
		logger.debug(fileParameters);

		// 创建输出nc文件路径
		String rootPathWrite = sizeParameter.getRootpathLocal();
		String sequrence = "00".equals(timeSequrence) ? "08" : "20" ;
		String outncPath = rootPathWrite + "/" + time + "/" + sequrence;
		File folderWrite = new File(outncPath);
		if (!folderWrite.exists()) {
			if (!this.createOutNCpath(folderWrite)) {
				logger.error("创建nc输出文件夹失败：-" + outncPath);
				return false;
			}
			// 删除1天之前的文件
			delFile(time, rootPathWrite);// /mnt/simple/ecmwf/clipper/ens
		}

		clipperEntity = ClipperUtil.getClipperEntity(fileParameters, sizeParameter, outncPath);
		logger.debug(clipperEntity);
		

		netcdfData = ClipperUtil.getClipperData(fileParameters,clipperEntity, sizeParameter);
		logger.debug(netcdfData);

		return true;
	}

	private void delFile(String time, String rootPathWrite) {
		int dataTime = 0;
		try
		{
			File root = new File(rootPathWrite);// /mnt/simple/ecmwf/clipper/ens
			File[] rootFiles = root.listFiles();
			for (File file : rootFiles) {
				logger.info("当前时间 : " + time + " 文件名 : " + file.getName());
				dataTime = Integer.parseInt(file.getName());
				int currentTime = Integer.parseInt(time);
				if (dataTime < currentTime - 1) {
					logger.info("删除 " + dataTime + " 文件的数据");
					FileUtil.deleteAllFilesOfDir(file);
				}
			}
		}
		catch (Exception e)
		{
			logger.error("删除 " + dataTime + "文件数据异常 , 异常  ： " + e);
		}
	}

	private File[] getFiles(String rootPath, String time, String timeSequrence, String timeValid) {
		String keyWords = "_" + timeValid + ".";
		String pathRead = rootPath + "/" + time + "/" + timeSequrence + "/" + timeValid;
		File fileRead = new File(pathRead);// 目录
		if (!fileRead.exists()) {// 如果文件夹不存在
			logger.info("目录不存在：" + fileRead.getAbsolutePath());
			return null;
		}
		File[] files = FileUtil.searchFile(fileRead, keyWords);
		return files;
	}

	private void setSizeParemeter(String timeValid, SizeParameter sizeParameter) {
		List<String> sfc1Layer = new ArrayList<String>();
		sfc1Layer.add("0");
		List<String> sfc2Layer = new ArrayList<String>();
		sfc2Layer.add("0");
		sizeParameter.setSfc1Layer(sfc1Layer);
		sizeParameter.setSfc2Layer(sfc2Layer);

		List<String> sfc2Feature = sizeParameter.getSfc2Feature();
		// 只有在时效为6的倍数的数据才有
		Integer t = Integer.parseInt(timeValid);
		if (t == 0 || t % 6 != 0) {
			sfc2Feature.remove("P10FG6");
			sfc2Feature.remove("MX2T6");
			sfc2Feature.remove("MN2T6");
		}
		sizeParameter.setSfc2Feature(sfc2Feature);
	}

	private static SizeParameter getSizeParameterData() {

		SizeParameter sizeParameter = ReadXML.getInstance().getSizeParameter();
		return sizeParameter;
	}

	private synchronized boolean createOutNCpath(File folderWrite) {
		boolean isMK = true;
		isMK = folderWrite.mkdirs();
		logger.info("创建本地目录:-" + folderWrite.getAbsolutePath() + "状态 ： " + isMK);
		return isMK;
	}

	public SizeParameter getSizeParameter() {
		return sizeParameter;
	}

	public FileParameter getFileParameters() {
		return fileParameters;
	}

	public ClipperEntity getClipperEntity() {
		return clipperEntity;
	}

	public NetcdfData getNetcdfData() {
		return netcdfData;
	}

}
