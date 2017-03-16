package com.supermap.gwfs.execuctors.synchronizer.clipper;

import java.io.File;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.executors.synchronizer.clipper.util.ReadXML;
import com.supermap.gwfs.tools.ftp.client.helper.FTPClientHelper;

/**  
 * @Description: FTP文件上传
 * @author zhoujian
 * @date 2016-10-13
 * @version V1.0 
 */
public class UploadFile
{
	private Logger logger = LoggerFactory.getLogger("Upload");
	private static UniObject ftpConfig = null;
	private static UploadFile uploadFile = null;
	private  UploadFile()
	{
		ftpConfig = ReadXML.getInstance().getFtpParameter();
	}
	public synchronized static UploadFile getInstance()
	{
		if (uploadFile == null)
		{
			uploadFile = new UploadFile();
		}
		return uploadFile;
	}
	/**
	 * 
	 * @Description: 上传nc文件
	 * @return boolean
	 * @throws
	 */
	public boolean upload(UniObject uniObject,String time,String timeSequence,String timeValid)
	{
		boolean isUploaded = false;
		String filePath = null;
		try
		{
			//获取本地文件
			String rootPath = uniObject.getStringValue("rootpathLocal");
			filePath = rootPath + "/" + time + "/" +timeSequence + "/" + uniObject.getStringValue("outFileName");
			//服务器目录
			String remotePath = uniObject.getStringValue("rootpath147") ;
			String remoteDirectory = remotePath + "/" + time + "/" + timeSequence ;
			//创建远程文件夹
			boolean isCreated = FTPClientHelper.createDirectorys(remoteDirectory, ftpConfig);
			if (isCreated)
			{
				//判断上传文件是否存在
				File uploadFile = new File(filePath);
				if(uploadFile.exists()&&uploadFile.isFile())
				{
					String fileName =  uploadFile.getName();
					String[] name = fileName.split("_");
					isUploaded  = FTPClientHelper.storeFile(remoteDirectory+"/"+fileName, uploadFile, ftpConfig);
					
					boolean isUploaded2  = FTPClientHelper.storeFile(remotePath +"/"+name[0]+"_"+name[1]+"_"+name[3], uploadFile, ftpConfig);
					if(isUploaded && isUploaded2){
						isUploaded = true;
					}else{
						isUploaded = false;
					}
				}
				else {
					logger.error("需要上传的文件不存在  , " + filePath);
					isUploaded = false;
				}
			}
		}
		catch (Exception e)
		{
			this.logger.error("文件上传异常 , 异常 ：" + e);
			isUploaded = false;
		}	
		return isUploaded;
	}
	/**
	 * 
	 * @Description: 上传nc文件描述信息ctl
	 * @return void
	 * @throws
	 */
	public void uploadCtl(UniObject uniObject , String localCtlPath , String fileName)
	{
		try
		{
			//远程文件夹
			String remotePath = uniObject.getStringValue("rootpath147") + "/" + "ctl_file";
			logger.debug("ctl文件路径 ： " + remotePath);
			//创建远程文件夹
			boolean isCreated = FTPClientHelper.createDirectorys(remotePath, ftpConfig);
			if (!isCreated)
			{
				logger.error("创建ctl文件夹失败！ ");
			}
			String localFilePath = localCtlPath + "/" + fileName;
			File file = new File(localFilePath);
			if(!file.exists())
			{
				logger.error("本地ctl文件不存在 , 文件 ： " + localFilePath);
			}
			boolean flag = FTPClientHelper.storeFile(remotePath + "/" + fileName, file, ftpConfig);
			if (flag)
			{
				logger.debug(localFilePath + " 文件上传成功！");
			}
			else
			{
				logger.debug(localFilePath + " 文件上传失败！");
			}
		}
		catch (Exception e)
		{
			logger.error(localCtlPath + "/" + fileName + " 文件上传异常  ， 异常 ： " + e);
		}
	}
}
