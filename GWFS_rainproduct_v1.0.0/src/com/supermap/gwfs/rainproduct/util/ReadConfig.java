package com.supermap.gwfs.rainproduct.util;

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
 * @Description: 读取配置文件
 * @author zhoujian
 * @date 2016-10-20
 * @version V1.0 
 */
public class ReadConfig
{
	
	private Logger logger = LoggerFactory.getLogger("ReadXml");
	private static ReadConfig readConfig = null;
	private static UniObject uniObject = null;
	private static UniObject uniObject_ftp = null;
	private static UniObject uniObject_valid = null;

	private ReadConfig(){
		uniObject = this.getParameter();
		uniObject_ftp = this.getFTPParameter();
		uniObject_valid = this.getValidParameter();
	}
	
	public static synchronized ReadConfig getInstance()
	{
		if(readConfig == null)
			readConfig = new ReadConfig();
		return readConfig ; 
	}
	/**
	 * 
	 * @Description: DOM解析XML文件
	 * @return UniObject
	 * @throws
	 */
	private UniObject getParameter()
	{
		UniObject uniObject = new UniObject();
		FileInputStream fis = null;
		try {
			SAXReader reader = new SAXReader();
			fis = new FileInputStream("config/rainproduct/bestnumber/bestnumber.xml");
			Document doc = reader.read(fis);
			Element root = doc.getRootElement();
			Element feature = root.element("feature");
			String sfcFeature =feature.elementTextTrim("sfc");
			String pl500Feature = feature.elementTextTrim("pl500");
			String pl700Feature = feature.elementTextTrim("pl700");
			String pl850Feature = feature.elementTextTrim("pl850");

			Element size = root.element("size");
			String sfcSize = size.elementTextTrim("sfc");
			String pl500Size = size.elementTextTrim("pl500");
			String pl700Size = size.elementTextTrim("pl700");
			String pl850Size = size.elementTextTrim("pl850");

			Element exception = root.element("exception");
			String sfcException = exception.elementTextTrim("sfc");
			String pl500Exception = exception.elementTextTrim("pl500");

			Element bestNumber = root.element("bestNumber");
			String number = bestNumber.elementTextTrim("number");

			Element write = root.element("write");
			String rootpathLocal = write.elementTextTrim("rootpathLocal");
			String rootpath147 = write.elementTextTrim("rootpath147");
			
			uniObject.setValue("sfcFeature", stringToArray(sfcFeature));
			uniObject.setValue("pl500Feature", stringToArray(pl500Feature));
			uniObject.setValue("pl700Feature", stringToArray(pl700Feature));
			uniObject.setValue("pl850Feature", stringToArray(pl850Feature));
			
			uniObject.setStringValue("sfcSize", sfcSize);
			uniObject.setStringValue("pl500Size", pl500Size);
			uniObject.setStringValue("pl700Size", pl700Size);
			uniObject.setStringValue("pl850Size", pl850Size);
						
			uniObject.setValue("sfcException", stringToArray(sfcException));
			uniObject.setValue("pl500Exception",stringToArray(pl500Exception));
			
			uniObject.setStringValue("number", number);
			uniObject.setStringValue("rootpathLocal", rootpathLocal);
			uniObject.setStringValue("rootpath147", rootpath147);
		}
		catch (Exception e) {
			this.logger.error("解析XML文件异常，异常：" + e);
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
				this.logger.error("关闭异常，异常：" + e);
			}
		}
		return uniObject;
	}
	
	/**
	 * 
	 * @Description: 解析时效分割配置文件
	 * 说明：
	 * 1.集合预报在72小时之后时间间隔是6小时（未插值之前）
	 * 2.当日08时次的数据处理之后的产品是当日20时次的数据 ，当日20时次数据处理出来之后是明日08时次的数据（时次整体向后推12）
	 * 3.当进行时间上的插值之后是不会出现把0-240分成几段来处理的情况
	 * @return UniObject
	 * @throws
	 */
	private UniObject getValidParameter()
	{
		UniObject uniObject = new UniObject();
		FileInputStream fis = null;
		try {
			SAXReader reader = new SAXReader();
			fis = new FileInputStream("config/rainproduct/valid/validsplit.xml");
			Document doc = reader.read(fis);
			Element root = doc.getRootElement();
			
			Element ER12 = root.element("ER12");	
			String ER12step1 = ER12.elementText("step1");
			String ER12step2 = ER12.elementText("step2");
			
			uniObject.setValue("ER12step1", ER12step1);
			uniObject.setValue("ER12step2", ER12step2);
			
			
			Element ER24 = root.element("ER24");	
			String ER24step1 = ER24.elementText("step1");
			String ER24step2 = ER24.elementText("step2");
			
			uniObject.setValue("ER24step1", ER24step1);
			uniObject.setValue("ER24step2", ER24step2);
			
		}
		catch (Exception e) {
			this.logger.error("解析XML文件异常，异常：" + e);
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
				this.logger.error("关闭异常，异常：" + e);
			}
		}
		return uniObject;
	}
	
	
	
	/**
	 * 
	 * @Description: String 转化成 List<String>
	 * @return List<String>
	 * @throws
	 */
	private List<String> stringToArray(String str) {
		try
		{
			String[] strs = str.split(",");
			List<String> arrays = new ArrayList<String>(Arrays.asList(strs));
			return arrays;
		}
		catch (Exception e)
		{
			logger.error("String 转化成 Array 异常, 异常 ： " + e);
		}
		return null;
	}
	
	/**
	 * 
	 * @Description: 加载FTP配置文件
	 * @return UniObject
	 * @throws
	 */
	private UniObject getFTPParameter()
	{
		UniObject uniObject = new UniObject();
		InputStream in = null;
		try
		{
			Properties p = new Properties();//属性集合对象      
			FileInputStream fis = new FileInputStream("config/rainproduct/ftp/ftpconfig.properties");//属性文件流      
			p.load(fis);
			uniObject.setStringValue("userName", p.getProperty("username"));
			uniObject.setStringValue("passWord", p.getProperty("password"));
			uniObject.setStringValue("ipAddress", p.getProperty("ip"));
			uniObject.setStringValue("portName", p.getProperty("port"));

		}
		catch (Exception e)
		{
			logger.error("加载FTP配置文件异常，异常 ： " + e);
			return null;
		}
		finally
		{
			try
			{
				if(in != null)
					in.close();
			}
			catch (IOException e)
			{
				logger.error("关闭InputStream异常 ， 异常 ： " + e);
			}
		}
		return uniObject;
	}
	
	
	public  UniObject getUniObject()
	{
		return uniObject.clone();
	}

	public  UniObject getUniObject_ftp()
	{
		return uniObject_ftp.clone();
	}

	public static UniObject getUniObject_valid()
	{
		return uniObject_valid.clone();
	}
	
	public static void main(String[] args)
	{
		UniObject uni = ReadConfig.getInstance().getUniObject();
		UniObject uuuObject =ReadConfig.getInstance().getFTPParameter();
		UniObject uObjectvalid =ReadConfig.getInstance().getValidParameter();
		System.out.println(uni);
		System.out.println(uuuObject);
		System.out.println(uObjectvalid);
	}
	
}
