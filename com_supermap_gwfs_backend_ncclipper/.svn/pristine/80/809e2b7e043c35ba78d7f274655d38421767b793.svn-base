package com.supermap.gwfs.clipper;

import java.io.File;

import com.supermap.gwfs.clipper.entity.SizeParameter;
import com.supermap.gwfs.clipper.util.FTPClientFactory;
import com.supermap.gwfs.clipper.util.FileUtil;
import com.supermap.gwfs.clipper.util.Logger;

public class UpLoadNcFile {
	String path147;
	String upLoadFileFolder;
	private Logger logger = Logger.getLogger(this.getClass());

	public boolean startUpLoad(SizeParameter sizeParameter, String time, String sequence, String valid) {

		String folder = sizeParameter.getRootpathLocal();
		upLoadFileFolder = folder + "/" + time + "/" + sequence;
		// 创建目录
		String rootPath147 = sizeParameter.getRootPath147();
		path147 = rootPath147 + "/" + time + "/" + sequence;

		return upLoad(time, sequence, valid);
	}

	private boolean upLoad(String time, String timeSequrence, String timeValid) {
		File[] files = getUpLoadFile(timeValid);
		if (files == null) {
			logger.error("查找上传文件为空-:" + time + "/" + timeSequrence + "/" + timeValid);
			return false;
		}
		boolean isUpLoad = FTPClientFactory.uploadFile(files, path147);
		return isUpLoad;
	}

	private File[] getUpLoadFile(String timeValid) {

		File fileRead = new File(upLoadFileFolder);// 目录
		if (!fileRead.exists()) {
			// 如果文件夹不存在
			logger.error("目录不存在：" + fileRead.getAbsolutePath());
			return null;
		}
		String keyWords = "_" + timeValid + ".";
		File[] files = FileUtil.searchFile(fileRead, keyWords);
		return files;

	}
}
