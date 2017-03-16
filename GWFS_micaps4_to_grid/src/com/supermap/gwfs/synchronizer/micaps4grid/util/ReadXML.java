package com.supermap.gwfs.synchronizer.micaps4grid.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;

/**  
 * @Description: 解析XML文件
 * @author zhoujian
 * @date 2016-11-30
 * @version V1.0 
 */
public class ReadXML
{
	private static Logger logger = LoggerFactory.getLogger("MicapsAnalytical");
	private static UniObject uniObject = null;
	
	private ReadXML()
	{
		
	}
	
	public static synchronized UniObject getParamterUniObject()
	{
		if (uniObject == null)
		{
			ReadXML readXML = new ReadXML();
			uniObject = readXML.getParamter();
		}
		return uniObject;
	}
	
	private  UniObject getParamter()
	{
		UniObject uniObject = new UniObject();
		FileInputStream fis = null;
		try {
			SAXReader reader = new SAXReader();
			fis = new FileInputStream("config/clipper/micaps_clipper.xml");
			Document doc = reader.read(fis);
			Element root = doc.getRootElement();
			
			String size = root.elementTextTrim("size");
			Element unit = root.element("unit");
			String windSize = root.elementText("windsize");
			String T = unit.elementTextTrim("t");
			String VEDA10 = unit.elementTextTrim("v");
			String msl = unit.elementTextTrim("msl");
			String mx2t3 = unit.elementTextTrim("TEM_Max");
			String mn2t3 = unit.elementTextTrim("TEM_Min");
			String r = unit.elementTextTrim("ERH");
			String vis = unit.elementTextTrim("vis");
			String tp = unit.elementTextTrim("ER03");
			String tcc = unit.elementTextTrim("ECT");
			String ER12 = unit.elementTextTrim("ER12");
			String ER24 = unit.elementTextTrim("ER24");
			String rootpathLocal = root.elementTextTrim("rootpathLocal");
			List<Float> sizeList = getList(size);
			float startLon = 0;
			float endLon = 0;
			float startLat = 0;
			float endLat = 0;
			if (sizeList.size() != 0)
			{
				startLon = sizeList.get(0);
				endLon = sizeList.get(1);
				startLat = sizeList.get(2);
				endLat = sizeList.get(3);
			}
			
			List<Float> windSizeList = getList(windSize);
			float windStartLon = 0;
			float windEndLon = 0;
			float windStartLat = 0;
			float windEndLat = 0;
			if (windSizeList.size() != 0)
			{
				windStartLon = windSizeList.get(0);
				windEndLon = windSizeList.get(1);
				windStartLat = windSizeList.get(2);
				windEndLat = windSizeList.get(3);
			}
			
			
			uniObject.setStringValue("2t_unit", T);
			uniObject.setStringValue("tem_max_unit", mx2t3);
			uniObject.setStringValue("tem_min_unit", mn2t3);
			uniObject.setStringValue("erh_unit", r);
			uniObject.setStringValue("vis_unit", vis);
			uniObject.setStringValue("er03_unit", tp);
			uniObject.setStringValue("er12_unit", ER12);
			uniObject.setStringValue("er24_unit", ER24);
			uniObject.setStringValue("ect_unit", tcc);
			uniObject.setStringValue("msl_unit", msl);
			uniObject.setStringValue("ueda10_unit", VEDA10);
			uniObject.setStringValue("veda10_unit", VEDA10);
			uniObject.setFloatValue("startLon", startLon);
			uniObject.setFloatValue("endLon", endLon);
			uniObject.setFloatValue("startLat", startLat);
			uniObject.setFloatValue("endLat", endLat);
			
			uniObject.setValue("windStartLon", windStartLon);
			uniObject.setValue("windEndLon", windEndLon);
			uniObject.setValue("windStartLat", windStartLat);
			uniObject.setValue("windEndLat", windEndLat);
			
			uniObject.setStringValue("rootpathLocal", rootpathLocal);
		}
		catch (Exception e) {
			logger.error("ZJ:Analytical XML file error , error : " + e);
		}
		finally
		{
			try
			{
				if (fis != null)
				{
					fis.close();
				}
			}
			catch (IOException e)
			{
				logger.error("ZJ: IO closed error , error : " + e);
			}
		}
		return uniObject;
	}
	
	/**
	 * 
	 * @Description: String --> List
	 * @return List<Float>
	 * @throws
	 */
	private static List<Float> getList(String size)
	{
		List<Float> list = null;
		try
		{
			list = new ArrayList<Float>();
			String[] tmp = size.split(",");
			for (int i = 0; i < tmp.length; i++)
			{
				list.add(Float.valueOf(tmp[i]));
			}
 		}
		catch (Exception e)
		{
			logger.error("String Convert To  List error , error : " + e);
		}
		return list;
	}

	/**
	 * 
	 * @Description: 解析时效配置文件--时效之间的间隔
	 * @return UniObject
	 * @throws
	 */
	public static UniObject getValidParameter()
	{
		UniObject uniObject = new UniObject();
		FileInputStream fis = null;
		try {
			SAXReader reader = new SAXReader();
			fis = new FileInputStream("config/valid/validsplit.xml");
			Document doc = reader.read(fis);
			Element root = doc.getRootElement();
			
			Element ER12 = root.element("ERH");	
			String ER12step1 = ER12.elementText("step1");
			String ER12step2 = ER12.elementText("step2");
			
			uniObject.setValue("ERHstep1", ER12step1);
			uniObject.setValue("ERHstep2", ER12step2);
			
		}
		catch (Exception e) {
			logger.error("解析XML文件异常，异常：" + e);
		}
		finally
		{
			try
			{
				if (fis != null )
				{
					fis.close();
				}
			}
			catch (IOException e)
			{
				logger.error("关闭异常，异常：" + e);
			}
		}
		return uniObject;
	}
	
	public static void main(String[] args)
	{
		UniObject uniObject = new ReadXML().getParamter();
		System.out.println(uniObject);
	}
}
