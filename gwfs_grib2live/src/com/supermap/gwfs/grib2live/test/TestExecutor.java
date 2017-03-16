package com.supermap.gwfs.grib2live.test;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import com.supermap.gcpp.core.executor.AbstractExecutor;
import com.supermap.gwfs.grib2live.handler.Grib2LiveThread;
import com.supermap.gwfs.grib2live.util.Logger;

public class TestExecutor extends AbstractExecutor {
	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void execute() {
		logger.info("开始执行girb2转换工作=============================");
		// TODO
		File[] gribFiles = searchFile(new File("/mnt/data/local/central/origin/"));
		logger.info(gribFiles.length + " 个文件待转换");
		for (File file : gribFiles) {
			new Grib2LiveThread(file.getAbsolutePath()).run();
		}
		logger.info("==============================转换完成===================================");
		logger.info("================================END====================================");
	}

	/**
	 * searchAllFiles
	 * 
	 * @param folder
	 * @return
	 */
	public static File[] searchFile(File folder) {
		File[] subFolders = folder.listFiles(new FileFilter() {// 运用内部匿名类获得文件
					public boolean accept(File pathname) {// 实现FileFilter类的accept方法
						if (pathname.isDirectory() || pathname.isFile())// 目录
							return true;
						return false;
					}
				});
		List<File> result = new ArrayList<File>();// 声明一个集合
		for (int i = 0; i < subFolders.length; i++) {// 循环显示文件夹或文件
			if (subFolders[i].isFile()) {// 如果是文件则将文件添加到结果列表中
				if (subFolders[i].getAbsolutePath().contains("00000.GRB2") || subFolders[i].getAbsolutePath().contains(".tmp")) {
					continue;
				}
				result.add(subFolders[i]);
			} else {// 如果是文件夹，则递归调用本方法，然后把所有的文件加到结果列表中
				File[] foldResult = searchFile(subFolders[i]);
				for (int j = 0; j < foldResult.length; j++) {// 循环显示文件
					result.add(foldResult[j]);// 文件保存到集合中
				}
			}
		}

		File files[] = new File[result.size()];// 声明文件数组，长度为集合的长度
		result.toArray(files);// 集合数组化
		return files;

	}
}
