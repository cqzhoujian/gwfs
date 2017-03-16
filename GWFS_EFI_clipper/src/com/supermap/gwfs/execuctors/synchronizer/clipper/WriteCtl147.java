package com.supermap.gwfs.execuctors.synchronizer.clipper;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.executors.synchronizer.clipper.util.ClipperUtil;

/**  
 * @Description: 写Ctl文件
 * @author zhoujian
 * @date 2016-10-12
 * @version V1.0 
 */
public class WriteCtl147
{
	private Logger logger = LoggerFactory.getLogger("Clipper");
	private String localCltPath = null;
	private static WriteCtl147 writeCtl147 = null;
	private WriteCtl147()
	{
	}
	public static WriteCtl147 getInstance()
	{
		if (writeCtl147 == null)
		{
			writeCtl147 = new WriteCtl147();			
		}
		return writeCtl147;
	}
	public boolean startWrite(UniObject uniObject, String time, String timeSequrence, String timeValid)
	{
		String type = uniObject.getStringValue("type");
		
		//输入ctl文件目录
		String rootPath = uniObject.getStringValue("rootpathLocal");
		localCltPath = rootPath + "/"  + "ctl_file";
		File file = new File(localCltPath);
		if (!file.exists())
		{
			file.mkdirs();
		}
		uniObject.setStringValue("localCltPath", localCltPath);
		if("EFI".equals(type))
		{
			String fileName = uniObject.getStringValue("outFileName");
			if (fileName.contains("efi_"))
			{
				//写本地ctl文件
				this.write_efi_Ctl(uniObject,time,timeSequrence,timeValid);
			}
			else if(fileName.contains("ep_")) {
				this.write_ep_Ctl(uniObject,time,timeSequrence,timeValid);
			}
			else if(fileName.contains("es_"))
			{
				this.write_es_Ctl(uniObject,time,timeSequrence,timeValid);
			}
			else if(fileName.contains("sot_"))
			{
				this.write_sot_Ctl(uniObject,time,timeSequrence,timeValid);
			}
			else {
				this.logger.error("prigg:write " + fileName + "ctl file is error !");
				return false;
			}
		}
		return true;
	}
	@SuppressWarnings({ "static-access", "unchecked" })
	private void write_sot_Ctl(UniObject uniObject, String time, String timeSequrence, String timeValid)
	{
		String ctlName = null;
		try
		{
			Rectangle2D geoBounds = new ClipperUtil().getBounds(uniObject.getStringValue("sot_sfcSize"));
			Point2D sizeCell = (Point2D)uniObject.getValue("sizeCell");
			List<String> features = (List<String>)uniObject.getValue("sot_sfcFeature");
			double noDataValue = uniObject.getDoubleValue("NoDataValue");
			String fileName = uniObject.getStringValue("outFileName");
			int vaildRead = Integer.parseInt((String) fileName.split("_")[3].subSequence(0, fileName.split("_")[3].indexOf(".")));
			String time_space = null; 
			if(vaildRead < 230)
			{
				time_space = "24";
			}
			else {
				time_space = "12";
			}
			String[] Name = fileName.substring(0,fileName.lastIndexOf(".")).split("_");
			ctlName = Name[0] + "_" + Name[1] + "_" + Name[3] + ".ctl";
			List<String> lvls = null;
			int number = uniObject.getIntegerValue("sot_sfcNumber");
			boolean flag = writeData(geoBounds,sizeCell,features,noDataValue,fileName ,ctlName,time,timeSequrence,timeValid,lvls,number,time_space);
			if (flag)
			{
				//上传
				UploadFile.getInstance().uploadCtl(uniObject, uniObject.getStringValue("localCltPath"), ctlName);
			}
			logger.info("...ctl文件:-" + localCltPath + "/" + ctlName + " 写完");
		}
		catch (Exception e)
		{
			logger.error("写" + ctlName + " 文件异常 ： 异常 ：" + e);
		}
	}
	@SuppressWarnings({ "static-access", "unchecked" })
	private void write_es_Ctl(UniObject uniObject, String time, String timeSequrence, String timeValid)
	{
		String ctlName =null;
		try
		{
			Rectangle2D geoBounds = new ClipperUtil().getBounds(uniObject.getStringValue("es_plSize"));
			Point2D sizeCell = (Point2D)uniObject.getValue("sizeCell");
			List<String> features = (List<String>)uniObject.getValue("es_plFeature");
			double noDataValue = uniObject.getDoubleValue("NoDataValue");
			String fileName = uniObject.getStringValue("outFileName");
			String time_space = null; 
			time_space = "6";
			String[] Name = fileName.substring(0,fileName.lastIndexOf(".")).split("_");
			ctlName = Name[0] + "_" + Name[1] + "_" + Name[3] + ".ctl";
			List<String> lvls = (List<String>)uniObject.getValue("es_plLevel");
			int number = uniObject.getIntegerValue("es_plNumber");
			boolean flag = writeData(geoBounds,sizeCell,features,noDataValue,fileName ,ctlName,time,timeSequrence,timeValid,lvls,number,time_space);
			if (flag)
			{
				//上传
				UploadFile.getInstance().uploadCtl(uniObject, uniObject.getStringValue("localCltPath"), ctlName);
			}
			logger.info("...ctl文件:-" + localCltPath + "/" + ctlName + " 写完");
		}
		catch (Exception e)
		{
			logger.error("写" + ctlName + " 文件异常 ： 异常 ：" + e);
		}
	}
	@SuppressWarnings({ "static-access", "unchecked" })
	private void write_ep_Ctl(UniObject uniObject, String time, String timeSequrence, String timeValid)
	{
		String ctlName =null;
		try
		{
			Rectangle2D geoBounds = new ClipperUtil().getBounds(uniObject.getStringValue("ep_plSize"));
			Point2D sizeCell = (Point2D)uniObject.getValue("sizeCell");
			List<String> features = (List<String>)uniObject.getValue("ep_plFeature");
			double noDataValue = uniObject.getDoubleValue("NoDataValue");
			String fileName = uniObject.getStringValue("outFileName");
			String time_space = "24"; 
			String[] Name = fileName.substring(0,fileName.lastIndexOf(".")).split("_");
			ctlName = Name[0] + "_" + Name[1] + "_" + Name[3] + ".ctl";
			List<String> lvls = null;
			int number = uniObject.getIntegerValue("ep_plNumber");
			boolean flag = writeData(geoBounds,sizeCell,features,noDataValue,fileName ,ctlName,time,timeSequrence,timeValid,lvls,number,time_space);
			if (flag)
			{
				//上传
				UploadFile.getInstance().uploadCtl(uniObject, uniObject.getStringValue("localCltPath"), ctlName);
			}
			logger.info("...ctl文件:-" + localCltPath + "/" + ctlName + " 写完");
		}
		catch (Exception e)
		{
			logger.error("写" + ctlName + " 文件异常 ： 异常 ：" + e);
		}
	}
	@SuppressWarnings({ "static-access", "unchecked" })
	private void write_efi_Ctl(UniObject uniObject, String time, String timeSequrence, String timeValid)
	{
		String ctlName= null;
		try
		{
			Rectangle2D geoBounds = new ClipperUtil().getBounds(uniObject.getStringValue("efi_sfcSize"));
			Point2D sizeCell = (Point2D)uniObject.getValue("sizeCell");
			List<String> features = (List<String>)uniObject.getValue("efi_sfcFeature");
			double noDataValue = uniObject.getDoubleValue("NoDataValue");
			String fileName = uniObject.getStringValue("outFileName");
			int vaildRead = Integer.parseInt((String) fileName.split("_")[3].subSequence(0, fileName.split("_")[3].indexOf(".")));
			String time_space = null; 
			if(vaildRead < 230)
			{
				time_space = "24";
			}
			else {
				time_space = "12";
			}
			String[] Name = fileName.substring(0,fileName.lastIndexOf(".")).split("_");
			ctlName = Name[0] + "_" + Name[1] + "_" + Name[3] + ".ctl";
			List<String> lvls = null;
			int number = uniObject.getIntegerValue("efi_sfcNumber");
			boolean flag = writeData(geoBounds,sizeCell,features,noDataValue,fileName ,ctlName,time,timeSequrence,timeValid,lvls,number,time_space);
			if (flag)
			{
				//上传
				UploadFile.getInstance().uploadCtl(uniObject, uniObject.getStringValue("localCltPath"), ctlName);
			}
			logger.info("...ctl文件:-" + localCltPath + "/" + ctlName + " 写完");
		}
		catch (Exception e)
		{
			logger.error("写" + ctlName + " 文件异常 ： 异常 ：" + e);
		}
	}
	/**
	 * 
	 * @Description: 写EFI数据类型的ctl
	 * @return boolean
	 * @throws
	 */
	private boolean writeData(Rectangle2D geoBounds, Point2D sizeCell, List<String> features, double noDataValue, String fileName, String ctlName, String time, String timeSequrence, String timeValid, List<String> lvls, int number,String time_spase)
	{
		int j, h = (int) geoBounds.getHeight(), w = (int) geoBounds.getWidth(), x = (int) geoBounds.getX(), y = (int) geoBounds.getY();
		double dx = sizeCell.getX(), dy = sizeCell.getY();
		BufferedWriter fw = null;
		OutputStreamWriter outctl = null;
		boolean result = true;
		try
		{
			outctl = new OutputStreamWriter(new FileOutputStream(new File(localCltPath + "/" + ctlName)));
			fw = new BufferedWriter(outctl);
			/**************** hander ***********************/
			fw.write("DSET E:/JiHeYuBao/Product/daily/efi " + "/" +time + "/"+ timeSequrence + "/" + fileName + "\n");
			fw.write("DTYPE NETCDF " + "\n");
			fw.write("UNDEF " + noDataValue + "\n");
			fw.write("XDEF " + (int)(w / dx + 1) + " LINEAR " + x + " " + dx + "\n");
			fw.write("YDEF " + (int)(h / dy + 1) + " LINEAR " + y + " " + dy + "\n");
			
			/****************** level *******************/
			if(lvls != null)
			{
				int lvlNum = lvls.size();
				StringBuffer lvlBuffer = new StringBuffer();
				for (int i = 0; i < lvlNum ; i++)
				{
					lvlBuffer.append(lvls.get(i) + " ");
				}
				fw.write("ZDEF " + lvlNum + " LEVELS " + lvlBuffer + "\n");
			}
			else {
				fw.write("ZDEF " + 1 + " LEVELS " + 0 + "\n");
			}
			/********************* TIME *************************/
			Locale l = new Locale("en");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			Date date = new Date();
			date = sdf.parse(time);
			String day = String.format("%td", date);

			String month = String.format(l, "%tb", date);
			String year = String.format("%tY", date);
			String hour = null;
			if (timeSequrence.equals("00")) {
				hour = "08";
			}
			if (timeSequrence.equals("12")) {
				hour = "20";
			}
			String dateStr = hour + "Z" + day + month + year;

			fw.write("TDEF 1 LINEAR " + dateStr + " " + time_spase + "hr" + "\n");
			
			/******************* Number *******************/
			StringBuffer numbBuffer = new StringBuffer();
			for (int i = 0; i < number; i++)
			{
				numbBuffer.append("0" + i +" ");
			}
			if(number != 0)
			{
				fw.write("EDEF " + number + " NAMES " + numbBuffer + "\n");
			}
			fw.write("VARS " + features.size() + "\n");
			for (String feature : features) {
				if(lvls != null)
				{
					fw.write(feature + " " + lvls.size() + " e,t,z,y,x " + feature + "\n");
				}
				else {
					fw.write(feature + " 1 e,t,z,y,x " + feature + "\n");
				}
			}
			fw.write("ENDVARS");
			fw.flush();
			this.logger.info("写 " + ctlName + " 文件成功！");
		}
		catch (Exception e)
		{
			this.logger.error("写  " + ctlName + " 文件异常  , 异常 : " + e);
			result = false;
		}
		finally
		{
			try
			{
				if (outctl != null)
				{
					outctl.close();
				}
			}
			catch (IOException e)
			{
				this.logger.error("文件流关闭异常 ， 异常 ：  " + e);
			}
		}
		return result;
	}
}
