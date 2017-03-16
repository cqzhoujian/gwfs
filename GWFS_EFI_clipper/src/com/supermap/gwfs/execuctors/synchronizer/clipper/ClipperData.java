package com.supermap.gwfs.execuctors.synchronizer.clipper;

import java.io.File;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.executors.synchronizer.clipper.util.ClipperUtil;
import com.supermap.gwfs.executors.synchronizer.clipper.util.FileUtil;
import com.supermap.gwfs.executors.synchronizer.clipper.util.ReadXML;

/**  
 * @Description: TODO
 * @author zhoujian
 * @date 2016-10-11
 * @version V1.0 
 */
public class ClipperData
{
	private Logger logger = LoggerFactory.getLogger("EfiClipper");
	private UniObject uniObject ;
	
	private static ClipperData clipperData = null;
		
	private ClipperData(){}
	
	public synchronized static ClipperData getInstance()
	{
		if(clipperData == null)
			clipperData = new ClipperData();
		return clipperData;
			
	}
	/**
	 * 
	 * @Description: EFI类型数据
	 * @return UniObject
	 * @throws
	 */
	public UniObject getData(String rootPath, String time, String timeSequrence, String timeValid,File file)
	{
		try
		{
			// 1. 获取XML中参数
			uniObject = getParameterData();
			
			uniObject.setValue("originpath", file.getAbsoluteFile());
			String fileNameTemp = file.getName();//filePath.substring(filePath.lastIndexOf("\\")+1, filePath.length());
			String fileName = fileNameTemp.substring(0, fileNameTemp.indexOf(".")) + ".nc";
			uniObject.setValue("outFileName", fileName);
			
			//创建输出nc文件路径
			String rootPathWrite = uniObject.getStringValue("rootpathLocal");
			String outncPath = rootPathWrite + "/" + time + "/" + timeSequrence;
			File folderWrite = new File(outncPath);
			if(!folderWrite.exists())
			{
				if(!this.createOutNCPath(folderWrite))
				{
					this.logger.error("创建输出nc文件路径失败 , " + outncPath);
					return null;
				}
			}
			//删除前一天的数据文件
			delFile(time , rootPathWrite);
			//获取裁切的数据(裁切范围，格距，原始nc，输出nc..)
 			ClipperUtil.getClipperEntity(uniObject , outncPath);
			//获取裁切的nc文件数据
 			ClipperUtil.getClipperData(uniObject,fileName);
			
		}
		catch (Exception e)
		{
			this.logger.error("获取原始nc文件数据异常  , 异常：" + e);
			return null;
		}
		return uniObject;
	}

	/**
	 * 
	 * @Description: 删除之前1天的文件
	 * @return void
	 * @throws
	 */
	private void delFile(String time, String rootPathWrite)
	{
		int dataTime  = 0;
		try
		{
			File root = new File(rootPathWrite);
			File[] rootFiles = root.listFiles();
			for (File file : rootFiles)
			{
				if (file.getName().equals("ctl_file"))
				{
					continue;
				}
				dataTime = Integer.parseInt(file.getName());
				int currentime = Integer.parseInt(time);
				if(dataTime < currentime - 1)
				{
					this.logger.info("删除文件 , " + dataTime);
					FileUtil.deleteAllFilesOfDir(file);
				}
			}
		}
		catch (Exception e)
		{
			this.logger.error(dataTime + "删除文件异常 ，异常 ：" + e);
		}
	}

	/**
	 * 
	 * @Description: 创建输出NC文件夹
	 * @return boolean
	 * @throws
	 */
	private boolean createOutNCPath(File folderWrite)
	{
		boolean isMk = true;
		isMk = folderWrite.mkdirs();
		return isMk;
	}
	/**
	 * 
	 * @Description: 解析对应XML
	 * @return UniObject
	 * @throws
	 */
	private UniObject getParameterData()
	{
		UniObject uniObject = ReadXML.getInstance().getParameter();
		return uniObject;
	}
}
