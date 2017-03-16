package com.supermap.gwfs.rainproduct.bestnumber;

import java.io.File;
import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.tools.ftp.client.factory.FTPClientFactory;
import com.supermap.gwfs.tools.ftp.client.helper.FTPClientHelper;

/**  
 * @Description: 下载数据文件
 * @author zhoujian
 * @date 2016-11-7
 * @version V1.0 
 */
public class LoadFiles
{
	private Logger logger = LoggerFactory.getLogger("BestNumber");
	private String[] localFilePath = new String[4];
	private static LoadFiles loadFiles = null;
//	private static FTPClient ftpClient = null;
	private LoadFiles(UniObject uniObject_ftp)
	{
//		ftpClient = FTPClientFactory.getInstance().createFTPClient(uniObject_ftp);
	}
	public synchronized static LoadFiles getInstance(UniObject uniObject_ftp)
	{
		if (loadFiles == null)
		{
			loadFiles = new LoadFiles(uniObject_ftp);
		}
		return loadFiles;
	}
	/**
	 * 
	 * @Description: 下载文件
	 * @return boolean
	 * @throws
	 */
	public synchronized boolean loadFile(UniObject uniObject, UniObject uniObject_ftp , String time , String timeSequence , String timeValid , String origin_val , String element)
	{
		FTPClient ftpClient = null;
		boolean result = true;
		try
		{
			//本地文件目录/mnt/data/local/origin/ecmwf/ER03/
			String localPath = uniObject.getStringValue("rootpathLocal")  + "/" + origin_val +"/origin/ecmwf/" + element + "/" + time + "/" + timeSequence + "/0/" + timeValid;
			File localFile = new File(localPath);
			if(!localFile.exists())
			{
				localFile.mkdirs();
				logger.info(localPath + " 目录创建成功 ！");
			}
			//删除前一天的文件
			delFiles(uniObject.getStringValue("rootpathLocal") + "/" + origin_val +"/origin/ecmwf/"+element+"/",time);
			//远程文件目录
			String remotePath = uniObject.getStringValue("rootpath147") + "/" + time + "/" + timeSequence ;
			ftpClient = FTPClientFactory.getInstance().createFTPClient(uniObject_ftp);
			logger.info("ftp连接对象， " + ftpClient);
			if (!ftpClient.changeWorkingDirectory(remotePath))
			{
				logger.error("远程文件夹不存在，文件夹 ： " + remotePath);
				return false;
			}
			// 需要下载的文件 ECWMF_pl_20161019_00_27.nc
			String[] fileName = new String[]{ "ECWMF_pl_" + time + "_" + timeSequence + "_" + timeValid + ".nc","ECWMF_sfc_1_" + time + "_" + timeSequence + "_" + timeValid + ".nc","ECWMF_sfc_2_" + time + "_" + timeSequence + "_" + timeValid + ".nc",null};
			logger.info("需要下载的文件 : " + fileName);
//			fileName[3] = getFileName(ftpClient,remotePath, time, timeSequence, timeValid);
			int tmpValid = Integer.parseInt(timeValid);
			if (tmpValid <= 72)
			{
				fileName[3] = "ECWMF_sfc_2_" + time + "_" + timeSequence + "_" + (tmpValid - 3) + ".nc";
			}
			else {
				fileName[3] = "ECWMF_sfc_2_" + time + "_" + timeSequence + "_" + (tmpValid - 6) + ".nc";
			}
			
			for (int i = 0; i < fileName.length; i++)
			{
				String remotefilePath = remotePath + "/" + fileName[i];
				File file = new File(localPath + "/" + fileName[i]);
				boolean flag = FTPClientHelper.retrieveFile(remotefilePath, file, uniObject_ftp);
				if (!flag)
				{
					logger.debug( remotefilePath + " 文件下载失败!");
				}
				else
				{
					localFilePath[i] = localPath + "/" + fileName[i];
					logger.debug( remotefilePath + " 文件下载成功!");
				}
			}
		}
		catch (IOException e)
		{
			logger.error("文件下载异常，异常 ： " + e);
			result = false;
		}
		finally
		{
			if(ftpClient != null )
				FTPClientHelper.closeFTPClient(ftpClient);
		}
		return result;
	}
	/**
	 * 
	 * @Description: 获取文件名
	 * @return String
	 * @throws
	 */
	private String getFileName(FTPClient ftpClient,String remotePath, String time, String timeSequence, String timeValid)
	{
		int tmpValid = Integer.parseInt(timeValid) - 3 ;
		if(tmpValid >= 0)
		{
			String name = "ECWMF_sfc_2_" + time + "_" + timeSequence + "_" + tmpValid + ".nc" ; 
			String filePath = remotePath + "/" +  name; 
			try
			{
				FTPFile[] files = ftpClient.listFiles(filePath); //  filePath);
				if(files.length > 0 && files[0].toString().contains(name))
				{
					logger.info("文件路径 ： " + filePath + "存在");
					return name;
				}
				else
				{
					return getFileName(ftpClient,remotePath, time, timeSequence, String.valueOf(tmpValid));
				}
			}
			catch (Exception e)
			{
				logger.error("获取文件名异常 ： 异常 : " + e);
			}
			finally
			{
//				if (ftpClient != null)
//				{
//					FTPClientHelper.closeFTPClient(ftpClient);
//				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @Description: 删除前一天的文件
	 * @return void
	 * @throws
	 */
	private void delFiles(String rootPathWrite, String time)
	{
		try
		{
			File root = new File(rootPathWrite);
			File[] rootFiles = root.listFiles();
			if (rootFiles != null)
			{
				for (File file : rootFiles) {
					int dataTime = Integer.parseInt(file.getName());
					int currentTime = Integer.parseInt(time);
					if (dataTime < currentTime - 1) {
						logger.info("删除 " + dataTime + " 时间的数据");
						deleteAllFilesOfDir(file);
					}
				}
			}
		}
		catch (NumberFormatException e)
		{ 
			logger.error("删除 " + time + " 文件异常  , 异常 : " + e);
		}
	}
	/**
	 * 
	 * @Description: 删除文件
	 * @return void
	 * @throws
	 */
	private void deleteAllFilesOfDir(File path)
	{
		if (!path.exists())
			return;
		if (path.isFile()) {
			path.delete();
			return;
		}
		File[] files = path.listFiles();
		for (int i = 0; i < files.length; i++) {
			deleteAllFilesOfDir(files[i]);
		}
		path.delete();
	}
	public String[] getLocalFilePath()
	{
		return localFilePath;
	}
	
}
