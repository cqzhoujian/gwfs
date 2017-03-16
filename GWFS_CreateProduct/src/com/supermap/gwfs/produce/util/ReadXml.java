package com.supermap.gwfs.produce.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.supermap.gcpp.core.common.UniObject;

/**  
 * @Description: 读取配置文件
 * @author zhoujian
 * @date 2017-3-14
 * @version V1.0 
 */
public class ReadXml
{
	private static ReadXml readXml = null;
	private static UniObject uniObject = null;
	
	private ReadXml()
	{
		uniObject = this.readXml();
	}
	
	public static synchronized ReadXml getInstance()
	{
		if (readXml == null)
		{
			readXml = new ReadXml();
		}
		return readXml;
	}
	
	public UniObject getParameter()
	{
		return uniObject.clone();
	}
	
	private UniObject readXml()
	{
		UniObject uniObject = null;
		FileInputStream fis = null;
		try
		{
			SAXReader reader = new SAXReader();
			fis = new FileInputStream("config/parameter/ProductParameters.xml");
			Document doc = reader.read(fis);
			Element root = doc.getRootElement();
			Element radar = root.element("radar");
			String img_resolution = radar.elementTextTrim("img_resolution");
			String projection = radar.elementTextTrim("projection");
			Element xml_file = radar.element("xml_file");
			String xml_path = xml_file.elementText("xml_path");
			String xml_Encoding = xml_file.elementText("xml_Encoding");
			String out_path = radar.elementText("out_path");
			String out_type = radar.elementText("out_type");
			
			String xmlContent = this.getXmlContent(xml_path,xml_Encoding);
			
			String shp_path = radar.elementTextTrim("shp_path");
			UniObject u_radar = new UniObject("radar");
			u_radar.setValue("img_resolution", img_resolution);
			u_radar.setValue("projection", projection);
			u_radar.setValue("xmlContent", xmlContent);
			u_radar.setValue("shp_path", shp_path);
			u_radar.setValue("out_path", out_path);
			u_radar.setValue("out_type", out_type);
			
			Element satellite = root.element("satellite");
			String img_resolution2 = satellite.elementTextTrim("img_resolution");
			String projection2 = satellite.elementTextTrim("projection");
			String shp_path2 = satellite.elementTextTrim("shp_path");
			String out_path2 = satellite.elementText("out_path");
			String out_type2 = satellite.elementText("out_type");
			
			UniObject u_satellite = new UniObject("satellite");
			u_satellite.setValue("img_resolution", img_resolution2);
			u_satellite.setValue("projection", projection2);
			u_satellite.setValue("shp_path", shp_path2);
			u_satellite.setValue("out_path", out_path2);
			u_satellite.setValue("out_type", out_type2);
			uniObject = new UniObject();
			uniObject.addContainedObject(u_radar);
			uniObject.addContainedObject(u_satellite);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return uniObject;
	}
	/**
	 * 
	 * @Description: 读雷达产品配色文件
	 * @return String
	 * @throws
	 */
	private String getXmlContent(String xml_path , String xml_Encoding)
	{
		StringBuffer sb = null;
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(xml_path), xml_Encoding));
			sb = new StringBuffer(); 
			String str;
			while ((str = reader.readLine()) != null) 
			{
			    sb.append(str);
			}
			reader.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return sb.toString();
	}

	public static void main(String[] args)
	{
		UniObject uniObject = new ReadXml().readXml();
		List<UniObject> u = uniObject.getObjectsByName("radar");
		System.out.println(u.get(0));
		List<UniObject> u1 = uniObject.getObjectsByName("satellite");
		System.out.println(u1.get(0));
	}
}
