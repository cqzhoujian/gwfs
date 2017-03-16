package com.supermap.gwfs.executors.synchronizer.clipper.efi.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.execuctors.synchronizer.clipper.ClipperData;
import com.supermap.gwfs.execuctors.synchronizer.clipper.UploadFile;
import com.supermap.gwfs.execuctors.synchronizer.clipper.WriteNetcdf;
import com.supermap.gwfs.executors.synchronizer.clipper.util.FileUtil;

/**  
 * @Description: TODO
 * @author zhoujian
 * @date 2016-11-15
 * @version V1.0 
 */
public class EfiClipperThread extends Thread
{
	private Logger logger = LoggerFactory.getLogger("EfiClipper");
	private String rootPath = null;
	private String time = null;
	private String timeSequrence = null;
	private String timeValid = null;
	private ClipperData clipperData = null;
	
	public EfiClipperThread(String rootPath, String time, String timeSequrence, String timeValid)
	{
		this.rootPath = rootPath;
		this.time = time;
		this.timeSequrence = timeSequrence;
		this.timeValid = timeValid;
		//获取一些对象
		clipperData = ClipperData.getInstance();
	}

	@Override
	public void run()
	{
		startClipper();
	}
	

	private void startClipper()
	{
		// 1. 得到目标目录下的所有文件
		String keyWord = ".grib1.nc";
		List<File> files = getFiles(keyWord,rootPath,time,timeSequrence,timeValid);
		if(files.size() == 0)
		{
			this.logger.error("文件夹为空 ," + time + "/" + timeSequrence+"/"+timeValid );
		}
	    for (File file : files)
		{
	    	String sequence = "00".equals(timeSequrence) ? "08" : "20" ;
	    	// 2. 遍历所有文件--得到裁切数据和nc数据
	    	UniObject uniObject = clipperData.getData(rootPath,time,sequence,timeValid,file);
		    if(uniObject != null)
		    {
		    	boolean flag = false;
			    // 3. 写nc文件
			    flag = WriteNetcdf.getInstance().startWrite(uniObject,file.getName());
			    this.logger.info( "写 " + file.getName() + " 文件 ，状态 ： " + flag);
			    if(flag)
			    {	// 4. 上传147服务器
			    	flag = UploadFile.getInstance().upload(uniObject, time, sequence, timeValid);
			    	this.logger.info(file.getName() + " 文件上传状态 ： " + flag);
			    }
			    if (flag)
			    {   // 5. 写ctl文件
//				    flag = WriteCtl147.getInstance().startWrite(uniObject,time,timeSequrence,timeValid);
//				    this.logger.info("write ctl file is " + flag);
//				    if (flag)
//					{
//						//使用disrupter调用之后的程序
//				    	
//				    	//TODO
//					}
			    }
		    }
		}
	}
	
	private List<File> getFiles(String keyWord, String rootPath, String time, String timeSequrence, String timeValid)
	{
		List<File> fileList = null;
		try
		{
			fileList = new ArrayList<File>();
			String pathRead = rootPath + "/" + time + "/" +timeSequrence + "/" + timeValid;
			File fileRead = new File(pathRead);
			if (!fileRead.exists())
			{
				this.logger.error("prigg: file is not exitsts , " + fileRead.getAbsolutePath());
				return null;
			}
			File[] files = FileUtil.searchFile(fileRead, keyWord); 
			for (File file : files)
			{
				if (file.getName().contains("efi_sfc") || file.getName().contains("ep_pl"))
				{
					fileList.add(file);
				}
			}
		}
		catch (Exception e)
		{
			logger.error("查找文件夹 " + rootPath + "/" + time + "/" +timeSequrence + " 下的 " + keyWord + "文件异常 ， 异常 : " + e );
		}
		
		return fileList;
	}
}
