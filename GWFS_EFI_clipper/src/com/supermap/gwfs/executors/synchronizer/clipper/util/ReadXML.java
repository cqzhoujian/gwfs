package com.supermap.gwfs.executors.synchronizer.clipper.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;

/**  
 * @Description: 解析XML文件
 * @author zhoujian
 * @date 2016-10-11
 * @version V1.0 
 */
public class ReadXML
{
	private Logger logger = LoggerFactory.getLogger("ReadXML");
	private static ReadXML readXML = null;
	private ReadXML(){}
	
	public synchronized static ReadXML getInstance()
	{
		if(readXML == null)
			readXML = new ReadXML();
		return readXML;
	}
	
	public synchronized UniObject getParameter()
	{
		UniObject uniObject = new UniObject();
			ReadXML_EFI(uniObject);
		return uniObject;
	}
	
	/**
	 * 
	 * @Description: 解析clipper_efi.xml
	 * @return void
	 * @throws
	 */
	private void ReadXML_EFI(UniObject uniObject)
	{
		try
		{
			SAXReader reader = new SAXReader();
			File xmlFile = new File("config/clipper/clipper_efi.xml");
			Document doc = reader.read(xmlFile);
			Element root = doc.getRootElement();
			//获取要素
			Element feature = root.element("feature");
			String efi_sfcFeature = feature.elementTextTrim("efi_sfc");
			String ep_plFeature = feature.elementTextTrim("ep_pl");
			String es_plFeature = feature.elementTextTrim("es_pl");
			String sot_sfcFeature = feature.elementTextTrim("sot_sfc");
			//获取范围
			Element size = root.element("size");
			String efi_sfcSize = size.elementTextTrim("sfc");
			String ep_plSize = size.elementTextTrim("pl");
			String es_plSize = size.elementTextTrim("pl");
			String sot_sfcSize = size.elementTextTrim("sfc");
			//层次
			Element level = root.element("level");
			String efi_sfcLevel = level.elementTextTrim("efi_sfc");
			String ep_plLevel = level.elementTextTrim("ep_pl");
			String es_plLevel = level.elementTextTrim("es_pl");
			String sot_sfcLevel = level.elementTextTrim("sot_sfc");
			//数量
			Element number = root.element("number");
			String efi_sfcNumber = number.elementTextTrim("efi_sfc");
			String ep_plNumber = number.elementTextTrim("ep_pl");
			String es_plNumber = number.elementTextTrim("es_pl");
			String sot_sfcNumber = number.elementTextTrim("sot_sfc");
			//文件目录
			Element write = root.element("write");
			String rootpathLocal = write.elementTextTrim("rootpathLocal");
			String rootpath147 = write.elementTextTrim("rootpath147");
			
			uniObject.setValue("efi_sfcFeature", stringToArray(efi_sfcFeature));
			uniObject.setValue("ep_plFeature", stringToArray(ep_plFeature));
			uniObject.setValue("es_plFeature", stringToArray(es_plFeature));
			uniObject.setValue("sot_sfcFeature", stringToArray(sot_sfcFeature));
			
			uniObject.setValue("efi_sfcSize", efi_sfcSize);
			uniObject.setValue("ep_plSize", ep_plSize);
			uniObject.setValue("es_plSize", es_plSize);
			uniObject.setValue("sot_sfcSize", sot_sfcSize);
			
			uniObject.setValue("efi_sfcLevel", stringToArray(efi_sfcLevel));
			uniObject.setValue("ep_plLevel", stringToArray(ep_plLevel));
			uniObject.setValue("es_plLevel", stringToArray(es_plLevel));
			uniObject.setValue("sot_sfcLevel", stringToArray(sot_sfcLevel));
			
			uniObject.setValue("efi_sfcNumber", efi_sfcNumber);
			uniObject.setValue("ep_plNumber", ep_plNumber);
			uniObject.setValue("es_plNumber", es_plNumber);
			uniObject.setValue("sot_sfcNumber", sot_sfcNumber);
			
			uniObject.setValue("rootpathLocal", rootpathLocal);
			uniObject.setValue("rootpath147", rootpath147);
//			this.logger.debug("xml文件参数 ：" + uniObject);
		}
		catch (Exception e) {
			this.logger.error("解析efi.xml文件异常 , 异常 ： " + e);
		}
		
	}
	/**
	 * 
	 * @Description: String转化成List
	 * @return List<String>
	 * @throws
	 */
	private List<String> stringToArray(String str) {
		String[] strs = str.split(",");
		List<String> arrays = new ArrayList<String>(Arrays.asList(strs));
		return arrays;
	}
	
	/***************************ftp***************************/
	/**
	 * 
	 * @Description: 解析ftp配置文件
	 * @return UniObject
	 * @throws
	 */
	public UniObject getFtpParameter()
	{
		UniObject uniObject = new UniObject();
		InputStream in = null;
		FileInputStream fis = null;
		try
		{
			Properties p = new Properties();//属性集合对象      
			fis = new FileInputStream("config/clipper/ftpconfig.properties");//属性文件流      
			p.load(fis);
			uniObject.setStringValue("userName", p.getProperty("username"));
			uniObject.setStringValue("passWord", p.getProperty("password"));
			uniObject.setStringValue("ipAddress", p.getProperty("ip"));
			uniObject.setStringValue("portName", p.getProperty("port"));
//			logger.debug("FTP参数 : " + uniObject);
		}
		catch (Exception e)
		{
			logger.error("解析FTP文件异常  ，异常  ： " + e);
			return null;
		}
		finally
		{
			try
			{
				if (fis != null)
					fis.close();
				if(in != null)
					in.close();
			}
			catch (IOException e)
			{
				logger.error("关闭文件流异常 ， 异常 ： " + e);
			}
		}
		return uniObject;
		
	}
	public static void main(String[] args)
	{
//		new ReadXML().getFtpParameter();
		new ReadXML().getParameter();
	}
	
}
