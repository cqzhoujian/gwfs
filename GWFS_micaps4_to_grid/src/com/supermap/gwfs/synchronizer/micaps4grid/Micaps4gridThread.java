package com.supermap.gwfs.synchronizer.micaps4grid;

import java.io.File;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.disruptor.UniDisruptor;
import com.supermap.disruptor.helper.DisruptorHelper;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.synchronizer.micaps4grid.interpolation.Interpolation;
import com.supermap.gwfs.synchronizer.micaps4grid.local.MicapsToLocal;
import com.supermap.gwfs.synchronizer.micaps4grid.micapsanalytical.MicapsAnalytical;
import com.supermap.gwfs.synchronizer.micaps4grid.netcdf.Netcdf;
import com.supermap.gwfs.synchronizer.micaps4grid.save.SaveHelper;
import com.supermap.gwfs.synchronizer.micaps4grid.specialized.RelativeHumidity;
import com.supermap.gwfs.synchronizer.micaps4grid.specialized.SpecializedProcessing;
import com.supermap.gwfs.synchronizer.micaps4grid.specialized.SpecializedWind;
import com.supermap.gwfs.synchronizer.micaps4grid.util.ConvertDate;
import com.supermap.gwfs.synchronizer.micaps4grid.util.ReadXML;

/**  
 * 说明：
 * 1.当日原始文件的08时次解析处理出来是作为当日20时次的预报数据（如：20170113 08 时次数据----处理----> 20170113 20 数据）
 * 2.当日原始文件的20时次解析处理出来是作为次日08时次的预报数据（如：20170113 20 时次数据----处理----> 20170114 08 数据）
 * @Description: 转化线程
 * @author zhoujian
 * @date 2016-11-29
 * @version V1.0 
 */
public class Micaps4gridThread extends Thread
{
	private Logger logger = LoggerFactory.getLogger("Micaps2NetcdfThread");
	private String filePath = null;
	private String element = null;
	private final String  forecast_fileversion = "ecthin";
	private final String  origin_val = "ecthin";
	private static byte[] lock = new byte[0];
	
	public Micaps4gridThread(String filePath,String element)
	{
		this.filePath = filePath;
		this.element = element;
	}
	
	@Override
	public void run()
	{
		doMicapsToGrid();
		
	}

	private void doMicapsToGrid()
	{	
		synchronized (lock)
		{
			try
			{
				/**
				 * 根据文件名裁切产生要素
				 */
				this.element = getElement(element);
				if (element != null)
				{
					UniObject uniObject = new UniObject();
					/**
					 * 解析XML文件
					 */
					getXmlParamter(uniObject , element);
					uniObject.setStringValue("filePath", filePath.replace("\\", "/"));
					uniObject.setValue("forecast_fileversion", forecast_fileversion);
					uniObject.setValue("element", element);
					uniObject.setValue("origin_val", origin_val);
					/**
					 * 解析Micaps4数据
					 */
					uniObject = MicapsAnalytical.getInstance().Analytical(uniObject);
					if (uniObject == null)
					{
						return ;
					}
					String forcastDate_tmp = uniObject.getStringValue("forcastDate").substring(0, 8);
					String forcastDate = ConvertDate.stringToString(forcastDate_tmp,"yyyyMMdd","yyyy-MM-dd");
					uniObject.setValue("_forcastDate", forcastDate);
					/**
					 * 对解析的数据集进行插值
					 */
					if ("V".equals(element) || "U".equals(element) || "MSL".equals(element))
					{
						//对风数据进行特殊的插值处理（文件的原始分辨率是0.25）
						//对于10m的风(地面风)进行0.025分辨率的插值
						//对于100~1000的风(高层风)进行0.25分辨率的插值
						uniObject = Interpolation.getInstance().bilinearInterpolation(uniObject , 0.25);
					}
					else if ("10U".equals(element) || "10V".equals(element))
					{
						uniObject = Interpolation.getInstance().bilinearInterpolation(uniObject , 0.025);
					}
					else 
					{
						uniObject = Interpolation.getInstance().bilinearInterpolation(uniObject , 0.025);
					}
					boolean flag = false;
					String fileName = null;
					/**
					 * 最高温 最低温 特殊处理 TEM_Max TEM_Min
					 */
					if (element.equals("TEM_Max") || element.equals("TEM_Min"))	//特殊处理日最高温和日最低温
					{
						//特殊处理
						SpecializedProcessing specialized = SpecializedProcessing.getInstance(forcastDate , uniObject.getStringValue("sequrence"));
						specialized.put(element , uniObject.getIntegerValue("valid_"),(double[][])uniObject.getValue("Z"));
						int index = specialized.doTmaxorTmin(uniObject , element);
						if (index != 0)
						{
							specialized.remove(element,index);
							specialized.remove(element,index - 3);
							specialized.remove(element,index - 6);
							specialized.remove(element,index - 9);
							specialized.remove(element,index - 12);
							specialized.remove(element,index - 15);
							specialized.remove(element,index - 18);
							specialized.remove(element,index - 21);
							/**
							 * 文件名  写nc数据
							 */
							fileName = getFileName(uniObject);
							flag = Netcdf.getInstance().writeNetcdf(uniObject);
							/**
							 * 保存到数据库
							 */
							if (flag)
							{
								SaveHelper.getInstance().saveData(uniObject.clone());
								
								//调用市台最高温  最低温的程序
								UniObject params = new UniObject();
								//调用市台最高温  最低温的程序
								params.setValue("type", 5);
								params.setValue("element", this.element);
								params.setValue("absolutePath", uniObject.getStringValue("filePath1"));
								UniDisruptor disruptor = DisruptorHelper.getDisruptor("ProcessorHandler");
								if(disruptor != null){
									disruptor.publish(params);
								}
							}
							else
							{
								logger.error("ZJ:write Netcdf fail , fileName = " + fileName);
							}
							return ;
						}
						else {
							return;
						}
					}
					else if ("ER03".equals(element))	//特殊处理降水数据
					{
						SpecializedProcessing specialized = SpecializedProcessing.getInstance(forcastDate , uniObject.getStringValue("sequrence"));
						element = "ER03";
						uniObject.setValue("element", element);
						uniObject.setValue("_forcastDate", forcastDate);
						
						specialized = SpecializedProcessing.getInstance(forcastDate , uniObject.getStringValue("sequrence"));
						specialized.putER(uniObject.getIntegerValue("valid_"),uniObject.clone());
						
						int _valid = uniObject.getIntegerValue("valid_");
						//时效为12时直接处理成12小时累计降水
						if (_valid % 12 == 0 )
						{
							element = "ER12";
							uniObject.setValue("element", element);
							uniObject.setValue("_forcastDate", forcastDate);
							specialized.putER(uniObject.getIntegerValue("valid_"),uniObject.clone());
						}
						if (_valid % 24 == 0)
						{
							//时效为24时直接处理成24小时累计降水
							element = "ER24";
							uniObject.setValue("element", element);
							uniObject.setValue("_forcastDate", forcastDate);
							specialized.putER(uniObject.getIntegerValue("valid_"),uniObject.clone());
						}
					}	//特殊处理风要素
					else if("V".equals(element) || "U".equals(element) || "10V".equals(element) || "10U".equals(element))
					{
//						uniObject.setValue("element", element);
						SpecializedWind wind = SpecializedWind.getInstance(forcastDate, uniObject.getStringValue("sequrence"));
						wind.putWind(element , forcastDate, uniObject.getStringValue("valid_") , uniObject.getStringValue("level"), uniObject.clone());
					}
					else {
						//其他要素直接写
						
						/**
						 * 文件名  写nc数据
						 */
						if (!"2D".equals(element))
						{
							fileName = getFileName(uniObject);
							//写文件
							flag = Netcdf.getInstance().writeNetcdf(uniObject);
							if (flag)
							{
								/**
								 * 保存到数据库
								 */
								SaveHelper.getInstance().saveData(uniObject);
								//把指定要素数据处理成市台数据
								MicapsToLocal.getInstance().doMicapsToLocal(uniObject.clone());
							}
							else
							{
								logger.error("ZJ:write Netcdf fail , fileName = " + fileName);
							}
						}
						
						if ("2D".equals(element) || "2T".equals(element))
						{
							/**
							 * 处理相对湿度（2T 2d）
							 */
							RelativeHumidity.getInstance(forcastDate, uniObject.getStringValue("sequrence")).put(uniObject.clone());
						}
					}
				}
			}
			catch (Exception e)
			{
				logger.error("ZJ:---- lement : " + element+ " filePath : "+ filePath +" Micaps Thread error , error : " + e);
			}
		}
	}
	/**
	 * 
	 * @Description: 获取到XML中的参数（高层风特殊处理）
	 * @return void
	 * @throws
	 */
	private void getXmlParamter(UniObject uniObject , String element)
	{
		UniObject xmlParams = ReadXML.getParamterUniObject();
		uniObject.setStringValue("2t_unit", xmlParams.getStringValue("2t_unit"));
		uniObject.setStringValue("tem_max_unit", xmlParams.getStringValue("tem_max_unit"));
		uniObject.setStringValue("tem_min_unit", xmlParams.getStringValue("tem_min_unit"));
		uniObject.setStringValue("erh_unit", xmlParams.getStringValue("erh_unit"));
		uniObject.setStringValue("vis_unit", xmlParams.getStringValue("vis_unit"));
		uniObject.setStringValue("ueda10_unit", xmlParams.getStringValue("ueda10_unit"));
		uniObject.setStringValue("veda10_unit", xmlParams.getStringValue("ueda10_unit"));
		uniObject.setStringValue("er03_unit", xmlParams.getStringValue("er03_unit"));
		uniObject.setStringValue("er12_unit", xmlParams.getStringValue("er12_unit"));
		uniObject.setStringValue("er24_unit", xmlParams.getStringValue("er24_unit"));
		uniObject.setStringValue("ect_unit", xmlParams.getStringValue("ect_unit"));
		uniObject.setStringValue("msl_unit", xmlParams.getStringValue("msl_unit"));
		uniObject.setStringValue("rootpathLocal", xmlParams.getStringValue("rootpathLocal"));
		//	对高层风的裁切范围进行特殊处理
		if ("V".equals(element.toUpperCase()) || "U".equals(element.toUpperCase()))
		{
			uniObject.setFloatValue("startLon", xmlParams.getFloatValue("windStartLon"));
			uniObject.setFloatValue("endLon", xmlParams.getFloatValue("windEndLon"));
			uniObject.setFloatValue("startLat", xmlParams.getFloatValue("windStartLat"));
			uniObject.setFloatValue("endLat", xmlParams.getFloatValue("windEndLat"));
		}
		else	// 非高层风要素
		{
			uniObject.setFloatValue("startLon", xmlParams.getFloatValue("startLon"));
			uniObject.setFloatValue("endLon", xmlParams.getFloatValue("endLon"));
			uniObject.setFloatValue("startLat", xmlParams.getFloatValue("startLat"));
			uniObject.setFloatValue("endLat", xmlParams.getFloatValue("endLat"));
		}
	}

	/**
	 * 
	 * @Description: 获取nc文件输出路径和文件名
	 * @return String
	 * @throws
	 */
	private String getFileName(UniObject uniObject)
	{
		String fileName = null;
		try
		{
			String filePath1 = uniObject.getStringValue("rootpathLocal") + "/" + origin_val + "/" + element + "/"  + uniObject.getStringValue("forcastDate").substring(0, 8) + "/" + uniObject.getStringValue("sequrence") + "/" +  uniObject.getStringValue("level") + "/" + forecast_fileversion;
			File file2 = new File(filePath1);
			if (!file2.exists())
			{
				file2.mkdirs();
			}
			fileName =  uniObject.getStringValue("forcastDate").substring(0, 10) + "_" + uniObject.getStringValue("valid_")+ ".nc";
			uniObject.setValue("fileName", fileName);
			uniObject.setStringValue("filePath1", filePath1 + "/" + fileName);
			uniObject.setStringValue("element", element);
		}
		catch (Exception e)
		{
			logger.error("ZJ:getFileName error , error : " + e);
		}
		return fileName;
	}

	/**
	 * 
	 * @Description: 更改要素名
	 * @return String
	 * @throws
	 */
	private String getElement(String ele)
	{
		String elementName = null;
		try
		{
			if ("MN2T3".equals(ele.toUpperCase()))
			{
				elementName = "TEM_Min";
			}
			else if("MX2T3".equals(ele.toUpperCase()))
			{
				elementName = "TEM_Max";
			}
			else if("TP".equals(ele.toUpperCase()))
			{
				elementName = "ER03";
			}
			else if("TCC".equals(ele.toUpperCase()))
			{
				elementName = "ECT";
			}
			else if("VIS".equals(ele.toUpperCase()))
			{
				elementName = "VIS";
			}
			else if("2T".equals(ele.toUpperCase()))
			{
				elementName = "2T";
			}
			else if("R".equals(ele.toUpperCase()))
			{
				elementName = "ERH";
			}
			else if("V".equals(ele.toUpperCase()))
			{
				elementName = "V";
			}
			else if("U".equals(ele.toUpperCase()))
			{
				elementName = "U";
			}
			else {
				elementName = ele.toUpperCase();
			}
		}
		catch (Exception e)
		{
			logger.error("ZJ:element name changed error , error : " + e);
		}
		return elementName;
	}
}
