package com.supermap.gwfs.clipper;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.net.ftp.FTPClient;

import com.supermap.gwfs.clipper.entity.ClipperEntity;
import com.supermap.gwfs.clipper.entity.FileParameter;
import com.supermap.gwfs.clipper.entity.SizeParameter;
import com.supermap.gwfs.clipper.util.FTPClientFactory;
import com.supermap.gwfs.clipper.util.Logger;

public class WriteCtlTo147 extends ClipData {
	String ctlPath;
	FTPClient ftpClient;
	ClipperEntity clipperEntity;
	SizeParameter sizeParameter;
	FileParameter fileParameter;
	private Logger logger = Logger.getLogger(this.getClass());

	public WriteCtlTo147() {
		ftpClient = FTPClientFactory.getFtpClient();
	}

	public boolean startWrite(ClipperEntity clipperEntity, SizeParameter sizeParameter, FileParameter fileParameter, String time, String sequence, String valid) {
		this.clipperEntity = clipperEntity;
		this.sizeParameter = sizeParameter;
		this.fileParameter = fileParameter;
		// 输出文件夹路径
		String rootPathWrite = sizeParameter.getRootPath147();
		ctlPath = rootPathWrite + "/ctl_file";
		
		boolean flag = ftpServer();
		if (!flag) {
			return false;
		}
		
		writePlCtl(time, sequence, valid);
		writeSfc1Ctl(time, sequence, valid);
		writeSfc2Ctl(time, sequence, valid);
		//20161008
		FTPClientFactory.closeConnect();
		return true;
	}

	private boolean ftpServer() {
		try {
			if (FTPClientFactory.connectServer(ftpClient)) {
				logger.debug("服务器连接成功");
				logger.debug("服务器ip:" + ftpClient.getRemoteAddress());
			} else {
				logger.error("服务器连接失败");
			}
			if (ftpClient.changeWorkingDirectory(ctlPath)) {
				// 文件夹存在
			} else {
				// 文件夹不存在
				logger.info("创建文件夹：-" + ctlPath);
				if (!FTPClientFactory.createDirecroty(ctlPath, ftpClient)) {
					logger.error("创建目录" + ctlPath + "失败");
					return false;
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	private void writePlCtl(String time, String timeSequrence, String timeValid) {

		Rectangle2D geoBounds = sizeParameter.getPlBounds();
		Point2D sizeCell = clipperEntity.getPlsizeCell();
		List<String> features = sizeParameter.getPlFeature();
		List<String> layer = sizeParameter.getPlLayer();
		double noDataValue = clipperEntity.getPlNoDataValue();
		String ctlName = "ECWMF_pl_" + timeValid + ".ctl";
		String fileName = fileParameter.getPlFileOutName();
		writeData(geoBounds, sizeCell, features, layer, noDataValue, ctlName, time, timeSequrence, timeValid, fileName);
		logger.info("...ctl文件:-" + ctlPath + "/" + ctlName + " 写完");
	}

	private void writeSfc1Ctl(String time, String timeSequrence, String timeValid) {

		Rectangle2D geoBounds = sizeParameter.getSfc1Bounds();
		Point2D sizeCell = clipperEntity.getSfc1sizeCell();
		List<String> features = sizeParameter.getSfc1Feature();
		List<String> layer = sizeParameter.getSfc1Layer();
		double noDataValue = clipperEntity.getSfc1NoDataValue();
		String ctlName = "ECWMF_sfc_1_" + timeValid + ".ctl";
		String fileName = fileParameter.getSfc1FileOutName();
		writeData(geoBounds, sizeCell, features, layer, noDataValue, ctlName, time, timeSequrence, timeValid, fileName);
		logger.info("...ctl文件:-" + ctlPath + "/" + ctlName + " 写完");
	}

	private void writeSfc2Ctl(String time, String timeSequrence, String timeValid) {
		Rectangle2D geoBounds = sizeParameter.getSfc2Bounds();
		Point2D sizeCell = clipperEntity.getSfc2sizeCell();
		List<String> features = sizeParameter.getSfc2Feature();
		List<String> layer = sizeParameter.getSfc2Layer();
		double noDataValue = clipperEntity.getSfc2NoDataValue();
		String ctlName = "ECWMF_sfc_2_" + timeValid + ".ctl";
		String fileName = fileParameter.getSfc2FileOutName();
		writeData(geoBounds, sizeCell, features, layer, noDataValue, ctlName, time, timeSequrence, timeValid, fileName);
		logger.info("...ctl文件:-" + ctlPath + "/" + ctlName + " 写完");
	}

	private void writeData(Rectangle2D geoBounds, Point2D sizeCell, List<String> features, List<String> layer, double noDataValue, String ctlName, String time, String timeSequrence, String timeValid, String fileName) {
		int j, h = (int) geoBounds.getHeight(), w = (int) geoBounds.getWidth(), x = (int) geoBounds.getX(), y = (int) geoBounds.getY();
		double dx = sizeCell.getX(), dy = sizeCell.getY();
		int numberNum = sizeParameter.getNumber();
		BufferedWriter fw = null;
		try {
			//20161008
			OutputStreamWriter outctl = new OutputStreamWriter(ftpClient.storeFileStream(ctlPath + "/" + ctlName));
			
			/*File file = new File(ctlPath);
				if(!file.isDirectory())
					file.mkdirs();
			
			OutputStreamWriter outctl = new OutputStreamWriter(new FileOutputStream(new File(file.getAbsolutePath()+"/"+ctlName)));*/
			fw = new BufferedWriter(outctl);
			// -------------------------------------------------------------------------------------
			fw.write("DSET E:/JiHeYuBao/Product" + sizeParameter.getRootPath147() + "/" + time + "/" + timeSequrence + "/" + fileName + "\n");

			fw.write("DTYPE NETCDF " + "\n");
			fw.write("UNDEF " + noDataValue + "\n");
			fw.write("XDEF " + (int) (w / dx + 1) + " LINEAR " + x + " " + dx + "\n");
			fw.write("YDEF " + (int) (h / dy + 1) + " LINEAR " + y + " " + dy + "\n");

			StringBuffer numberStr = new StringBuffer();
			for (int i = 0; i <= numberNum; i++) {
				numberStr.append("0" + i + " ");
			}
			int levelNum = layer.size();
			StringBuffer levelStr = new StringBuffer();
			for (j = 0; j < levelNum; j++) {
				if (layer.get(j).equals("surcafe")) {
					levelStr.append(0);
				} else {
					levelStr.append(layer.get(j) + " ");
				}
			}

			// -----------------------------------------------------------------------------------------
			fw.write("ZDEF " + levelNum + " LEVELS " + levelStr + "\n");

			// TIME-----------------------------------------------------------------------------------------
			Locale l = new Locale("en");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			Date date = new Date();
			date = sdf.parse(time);
			String day = String.format("%td", date);

			String month = String.format(l, "%tb", date);
			String year = String.format("%tY", date);
			// String hour = String.format("%tH", date);
//			String hour = null;
//			if (timeSequrence.equals("00")) {
//				hour = "08";
//			}
//			if (timeSequrence.equals("12")) {
//				hour = "20";
//			}
			String dateStr = timeSequrence + "Z" + day + month + year;
			int t = Integer.parseInt(timeValid);
			if (t <= 78) {
				t = 3;
			}
			if (78 < t && t <= 240) {
				t = 6;
			}
			if (240 < t) {
				t = 12;
			}

			fw.write("TDEF 1 LINEAR " + dateStr + " " + t + "hr" + "\n");
			// -----------------------------------------------------------------------------------------

			fw.write("EDEF " + (numberNum + 1) + " NAMES " + numberStr + "\n");
			fw.write("VARS " + features.size() + "\n");
			for (String feature : features) {
				fw.write(feature + " " + levelNum + " e,t,z,y,x " + feature + "\n");
			}
			fw.write("ENDVARS");
			fw.flush();

			// -------------------------------------------------------------------------------------
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				if (fw != null) {
					fw.close();
				}
				//20161008
				ftpClient.completePendingCommand();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}

		}

	}
}
