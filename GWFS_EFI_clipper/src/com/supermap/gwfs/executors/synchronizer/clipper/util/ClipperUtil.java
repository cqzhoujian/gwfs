package com.supermap.gwfs.executors.synchronizer.clipper.util;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;


/**  
 * @Description: 获取裁切的信息(范围  分辨率 数据...)
 * @author zhoujian
 * @date 2016-10-11
 * @version V1.0 
 */
public class ClipperUtil
{
	private static Logger logger = LoggerFactory.getLogger("EfiClipper");

	/**
	 * 
	 * @Description: 获取裁切数据(范围，格距，输入输出nc文件)
	 * @return void
	 * @throws
	 */
	public static void getClipperEntity(UniObject uniObject, String outncPaht)
	{
		NetcdfFile ncOriginFile = null;
		NetcdfFileWriter ncOutFile = null;
		Rectangle2D cellBounds = null;
		Point2D sizeCell = null;
		try
		{
			//根据源文件路径创建nc
			ncOriginFile = NetcdfFile.open(uniObject.getStringValue("originpath"));
			//输出nc完整路径
			String outFileName = uniObject.getStringValue("outFileName");
			String outFilePaht = outncPaht + "/" + outFileName;
			//创建输出nc文件
			ncOutFile = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, outFilePaht);
			
			if(outFileName.contains("_pl_"))
			{
				cellBounds = NetCDFAllUtil.getCellBounds(ncOriginFile, getBounds(uniObject.getStringValue("ep_plSize")));
			}
			else//"_sfc_"
			{
				cellBounds = NetCDFAllUtil.getCellBounds(ncOriginFile, getBounds(uniObject.getStringValue("efi_sfcSize")));
			}
			//获取范围
			//获取格距
			sizeCell = NetCDFAllUtil.getCellSize(ncOriginFile);
			long NoDataValue = -32767;
			uniObject.setValue("ncOriginFile", ncOriginFile);
			uniObject.setValue("ncOutFile", ncOutFile);
			uniObject.setValue("cellBounds", cellBounds);
			uniObject.setValue("sizeCell", sizeCell);
			uniObject.setValue("NoDataValue", NoDataValue);
			
		}
		catch (Exception e)
		{
			logger.error("获取原始nc文件的参数(格距  范围 输出文件名)异常, 异常 ： " + e);
		}
		finally
		{
			try
			{
				if (ncOriginFile != null)
				{
					ncOriginFile.close();
				}
			}
			catch (IOException e)
			{
				logger.error("关闭原始文件对象异常 , 异常 ： " + e);
			}
		}
	}

	/**
	 * @Description: 获取nc文件裁切数据
	 * @throws InvalidRangeException 
	 * @throws IOException 
	 * @return void
	 * @throws
	 */
	@SuppressWarnings("unchecked")
	public static void getClipperData(UniObject uniObject,String fileName) throws IOException, InvalidRangeException
	{
		Map<String, String> unitMap = new HashMap<String, String>();
		//获取原nc文件
		NetcdfFile netcdf = null;
		//获取层次
		List<Integer> lvls = null;
		List<String> features = null;
//		int time = NetCDFAllUtil.getDimTime(netcdf);
		int number = 0; 
		try
		{
			netcdf = NetcdfFile.open(uniObject.getStringValue("originpath"));//(NetcdfFile)uniObject.getValue("ncOriginFile");
			List<Integer> times = NetCDFAllUtil.getDimTime(netcdf);
			uniObject.setValue("time", times);
			for (int i = 0; i < times.size(); i++)
			{
				logger.info( uniObject.getStringValue("outFileName")+ "========================" + times.get(i));
			}
			List<Integer> Numbers = NetCDFAllUtil.getNumberIndex(netcdf,uniObject.getStringValue("type"));
			
			if(fileName.contains("efi_"))	//sfc
			{
				features = (List<String>)uniObject.getValue("efi_sfcFeature");
				number = uniObject.getIntegerValue("efi_sfcNumber");
				//范围
				Rectangle2D cellBounds = (Rectangle2D)uniObject.getValue("cellBounds");
				Map<String, Array[][]> dataMap = new HashMap<String, Array[][]>();
				Map<String, double[]> factors = new HashMap<String, double[]>();
				
				for (String feature : features)
				{
					Array[][] datas = new Array[number+1][1];
					Variable variable = netcdf.findVariable(feature.toLowerCase());
					double add_offset = 0;
					double cale_factor = 0;
					if(variable == null)
					{
						logger.error("efi文件:" + netcdf.getLocation() + "变量  :" + feature + "不存在!");
					}
					else {
						unitMap.put(feature, NetCDFAllUtil.getUnit(feature, variable));
						add_offset = NetCDFAllUtil.getAdd_offset(variable);
						cale_factor = NetCDFAllUtil.getScale_factor(variable);
						int[] origin = new int[] { 0, (int) cellBounds.getY(), (int) cellBounds.getX() };
						int[] size = new int[] { 1, (int) cellBounds.getHeight(), (int) cellBounds.getWidth() };
						// 裁切数据
						Array cfdata2D;
						cfdata2D = variable.read(origin, size).reduce();
						datas[0][0] = cfdata2D;
						dataMap.put(feature, datas);
						double[] factor = new double[2];
						factor[0] = add_offset;
						factor[1] = cale_factor;
						factors.put(feature, factor);
						uniObject.setValue("factors", factors);
						uniObject.setValue("elementUnit", unitMap);
						uniObject.setValue("elementValue", dataMap);
					}
				}
			}
			else if(fileName.contains("ep_")) 	//pl
			{
				features = (List<String>)uniObject.getValue("ep_plFeature");
				number = uniObject.getIntegerValue("ep_plNumber");
				//范围
				Rectangle2D cellBounds = (Rectangle2D)uniObject.getValue("cellBounds");
				Map<String, Array[][]> dataMap = new HashMap<String, Array[][]>();
				Map<String, double[]> factors = new HashMap<String, double[]>();
				for (String feature : features)
				{
					Array[][] datas = new Array[number+1][1];
					Variable variable = netcdf.findVariable(feature.toLowerCase());
					unitMap.put(feature, NetCDFAllUtil.getUnit(feature, variable));
					double add_offset = 0;
					double cale_factor = 0;
					if(variable == null)
					{
						logger.error("ep文件:" + netcdf.getLocation() + " 变量 :" + feature + " 不存在!");
					}
					else {
						add_offset = NetCDFAllUtil.getAdd_offset(variable);
						cale_factor = NetCDFAllUtil.getScale_factor(variable);
						int[] origin = new int[] { 0, (int) cellBounds.getY(), (int) cellBounds.getX() };
						int[] size = new int[] { 1, (int) cellBounds.getHeight(), (int) cellBounds.getWidth() };
						// 裁切数据
						Array cfdata2D;
						cfdata2D = variable.read(origin, size).reduce();
						datas[0][0] = cfdata2D;
						dataMap.put(feature, datas);
						double[] factor = new double[2];
						factor[0] = add_offset;
						factor[1] = cale_factor;
						factors.put(feature, factor);
						uniObject.setValue("factors", factors);
						uniObject.setValue("elementUnit", unitMap);
						uniObject.setValue("elementValue", dataMap);
					}
				}
			}	
			else if(fileName.contains("es_"))	//pl
			{
				lvls = NetCDFAllUtil.getLevelIndex(netcdf, (List<String>)uniObject.getValue("es_plLevel"));
				features = (List<String>)uniObject.getValue("es_plFeature");
				number = uniObject.getIntegerValue("es_plNumber");
				
				//范围
				Rectangle2D cellBounds = (Rectangle2D)uniObject.getValue("cellBounds");
				Map<String, Array[][]> dataMap = new HashMap<String, Array[][]>();
				Map<String, double[]> factors = new HashMap<String, double[]>();
				for (String feature : features)
				{
					Array[][] datas = new Array[number+1][lvls.size()];
					Variable variable = netcdf.findVariable(feature.toLowerCase());
					unitMap.put(feature, NetCDFAllUtil.getUnit(feature, variable));
					double add_offset = 0;
					double cale_factor = 0;
					if(variable == null)
					{
						logger.error("es文件 :" + netcdf.getLocation() + " 变量 :" + feature + " 不存在!");
					}
					else {
						add_offset = NetCDFAllUtil.getAdd_offset(variable);
						cale_factor = NetCDFAllUtil.getScale_factor(variable);
						// 裁切
						for (int i = 0; i < lvls.size(); i++) {// [longitude = 181;, latitude = 111;, level = 11;, time = 1;]
							int lvlIndex = lvls.get(i);
							if (lvlIndex < 0) {
								logger.error(netcdf.getLocation() + ":" + feature + " element " + lvls.get(i) + " level is not exits");
							}
							int[] origin = new int[] { 0, lvlIndex, (int) cellBounds.getY(), (int) cellBounds.getX() };
							int[] size = new int[] { 1, 1, (int) cellBounds.getHeight(), (int) cellBounds.getWidth() };
							// 裁切数据
							Array cfdata2D;

							cfdata2D = variable.read(origin, size).reduce();

							datas[0][i] = cfdata2D;
							dataMap.put(feature, datas);
							double[] factor = new double[2];
							factor[0] = add_offset;
							factor[1] = cale_factor;
							factors.put(feature, factor);
							uniObject.setValue("factors", factors);
							uniObject.setValue("elementUnit", unitMap);
							uniObject.setValue("elementValue", dataMap);
						}
					}
				}
			}
			else if(fileName.contains("sot_"))	//sfc
			{
//				lvls = NetCDFAllUtil.getLevelIndex(netcdf, (List<String>)uniObject.getValue("sot_sfcLevel"));
				features = (List<String>)uniObject.getValue("sot_sfcFeature");
				number = uniObject.getIntegerValue("sot_sfcNumber");
				//范围
				Rectangle2D cellBounds = (Rectangle2D)uniObject.getValue("cellBounds");
				Map<String, Array[][]> dataMap = new HashMap<String, Array[][]>();
				Map<String, double[]> factors = new HashMap<String, double[]>();
				for (String feature : features) {
					Array[][] datas = new Array[number + 1][1];
					Variable cfvariable = netcdf.findVariable(feature.toLowerCase());
					double add_offset = 0;
					double cale_factor = 0;
					if (cfvariable == null) {
						logger.error("文件 ：" + netcdf.getLocation() + "\n   	变量 : " + feature + " 不存在 ！");
					} else {
						unitMap.put(feature, NetCDFAllUtil.getUnit(feature, cfvariable));// 设置单位
						add_offset = NetCDFAllUtil.getAdd_offset(cfvariable);
						cale_factor = NetCDFAllUtil.getScale_factor(cfvariable);
						// 裁切
						for (int i = 0; i < Numbers.size(); i++) {// [longitude = 181;, latitude = 111;, level = 11;, time = 1;]
							int numberIndex = Numbers.get(i);
							if (numberIndex < 0) {
								logger.error("sot文件 ：" + netcdf.getLocation() + ":" + feature + " 要素： " + Numbers.get(i) + " 成员  不存在！");
							}
							int[] origin = new int[] { 0, i, (int) cellBounds.getY(), (int) cellBounds.getX() };
							int[] size = new int[] { 1, 1, (int) cellBounds.getHeight(), (int) cellBounds.getWidth() };
							// 裁切数据
							Array cfdata2D;

							cfdata2D = cfvariable.read(origin, size).reduce();

							datas[i+1][0] = cfdata2D;
							
							dataMap.put(feature, datas);
							double[] factor = new double[2];
							factor[0] = add_offset;
							factor[1] = cale_factor;
							factors.put(feature, factor);
							uniObject.setValue("factors", factors);
							uniObject.setValue("elementUnit", unitMap);
							uniObject.setValue("elementValue", dataMap);
						}
					}
				}
			}
			else 
			{
				logger.error("非法文件  , 文件名 ：" + fileName);
			}
		}
		catch (Exception e)
		{
			logger.error("获取原始nc文件数据异常 ， 异常 ： " + e);
		}
		finally
		{
			if (netcdf != null)
			{
				netcdf.close();
			}
		}
	}
	
	/**
	 * 获取Bounds
	 * @param size
	 * @return
	 */
	public static Rectangle2D getBounds(String size) {
		Rectangle2D bounds = new Rectangle2D.Float();
		String[] str = size.split(",");
		List<Float> flos = new ArrayList<Float>();
		for (int i = 0; i < str.length; i++) {
			flos.add(Float.parseFloat(str[i]));
		}
		bounds.setRect(flos.get(0), flos.get(2), flos.get(1) - flos.get(0), flos.get(3) - flos.get(2));
		return bounds;
	}
}