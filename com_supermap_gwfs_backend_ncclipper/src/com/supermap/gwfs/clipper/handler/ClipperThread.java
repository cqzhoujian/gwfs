package com.supermap.gwfs.clipper.handler;

import java.io.File;

import com.supermap.disruptor.UniDisruptor;
import com.supermap.disruptor.helper.DisruptorHelper;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.clipper.Clip2Netcdf;
import com.supermap.gwfs.clipper.ClipData;
import com.supermap.gwfs.clipper.UpLoadNcFile;
import com.supermap.gwfs.clipper.WriteCtlTo147;
import com.supermap.gwfs.clipper.entity.ClipperEntity;
import com.supermap.gwfs.clipper.entity.FileParameter;
import com.supermap.gwfs.clipper.entity.NetcdfData;
import com.supermap.gwfs.clipper.entity.SizeParameter;
import com.supermap.gwfs.clipper.util.FileUtil;
import com.supermap.gwfs.clipper.util.Logger;

public class ClipperThread extends Thread {
	private Logger logger = Logger.getLogger(ClipperThread.class);
	private Clip2Netcdf clip2Nc;
	private UpLoadNcFile upLoad;
	private WriteCtlTo147 writeCtl;
	private ClipData clipData;

	private String rootPath;
	private String time;
	private String timeSequrence;
	private String timeValid;
	private int type ;

	public ClipperThread() {
		clip2Nc = new Clip2Netcdf();
		upLoad = new UpLoadNcFile();
		writeCtl = new WriteCtlTo147();
		clipData = new ClipData();
	}

	public ClipperThread(String rootPath, String time, String timeSequrence, String timeValid , int type) {
		this();
		this.setRootPath(rootPath);
		this.setTime(time);
		this.setTimeSequrence(timeSequrence);
		this.setTimeValid(timeValid);
		this.setType(type);
	}

	@Override
	public void run() {
		startClip();
	}

	private void startClip() {
		boolean flag;
		String sequrence = "00".equals(timeSequrence) ? "08" : "20" ;
		flag = clipData.getData(rootPath, time, timeSequrence, timeValid);
		ClipperEntity clipperEntity = clipData.getClipperEntity();
		SizeParameter sizeParameter = clipData.getSizeParameter();
		FileParameter fileParameter = clipData.getFileParameters();
		NetcdfData netcdfData = clipData.getNetcdfData();
		
		if (flag) {
			flag = clip2Nc.startClip(clipperEntity, sizeParameter, netcdfData, fileParameter);
//			//删除裁切完成之后的大nc文件
			String filePath = rootPath + File.separator + time + File.separator + timeSequrence + File.separator + timeValid;
			File file = new File(filePath);
			FileUtil.deleteAllFilesOfDir(file);
		}
		if (flag) {
			//20161008
//			flag = upLoad.startUpLoad(sizeParameter, time, sequrence, timeValid);
		}
		if (flag) {
//			flag = writeCtl.startWrite(clipperEntity, sizeParameter, fileParameter, time, sequrence, timeValid);
		}
		if (flag)
		{
			/**
			 * 
			 */
			String rootPathWrite = sizeParameter.getRootpathLocal();
			String outncPath = rootPathWrite + "/" + time + "/" + sequrence;
			String sfc1AbsolutePath = outncPath + "/" + fileParameter.getSfc1FileOutName();
			//20161031
			int tmpValid = Integer.parseInt(timeValid);
			if (tmpValid <= 240)
			{
				//disruptor调用降水客观订正管理者
				UniDisruptor disruptor = DisruptorHelper.getDisruptor("ManagerHandler");
				UniObject uniObject = new UniObject();
				uniObject.setStringValue("time", time);
				uniObject.setStringValue("timeSequrence", sequrence);
				uniObject.setStringValue("timeValid", timeValid);
//				uniObject.setStringValue("absolutePath", sfc1AbsolutePath);
//				uniObject.setIntegerValue("type", type);
				logger.info("----时效：" + timeValid +" 调用ManagerHandler ： " + disruptor);
				if (disruptor != null)
				{
					disruptor.publish(uniObject);
				}
				
//				UniDisruptor disruptor_wl = DisruptorHelper.getDisruptor("ProcessorHandler");
//				UniObject uniObject_wl = new UniObject();
//				uniObject_wl.setStringValue("absolutePath", sfc1AbsolutePath);
//				uniObject_wl.setIntegerValue("type", type);
//				if (disruptor_wl != null)
//				{
//					logger.info("调用开始wl程序开始" + uniObject_wl);
//					disruptor_wl.publish(uniObject_wl);
//				}
			}
		}
	}

	private void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	private void setTime(String time) {
		this.time = time;
	}

	private void setTimeSequrence(String timeSequrence) {
		this.timeSequrence = timeSequrence;
	}

	private void setTimeValid(String timeValid) {
		this.timeValid = timeValid;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}
	

}
