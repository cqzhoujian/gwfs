package com.supermap.gwfs.clipper.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.supermap.gwfs.clipper.entity.SizeParameter;

/**
 * 使用DOM解析XML文件
 * 
 * @author LQ
 *
 */

public class ReadXML {
	private Logger logger = Logger.getLogger(this.getClass());
	private static ReadXML readXML = null;
	private static SizeParameter sizeParameter = null;
	private ReadXML(){
		sizeParameter = this.getParameter();
	}
	
	public static ReadXML getInstance()
	{
		if(readXML == null)
			readXML = new ReadXML();
		
		return readXML;
	}
	private SizeParameter getParameter() {
		SizeParameter parameter = new SizeParameter();
		 FileInputStream fis = null;
		try {
			SAXReader reader = new SAXReader();
			fis = new FileInputStream("config/clipper/clipper.xml");
			Document doc = reader.read(fis);
			Element root = doc.getRootElement();
			Element feature = root.element("feature");
			Element plElement = feature.element("pl");
			String plFeature = plElement.getTextTrim();
			String sfc1Feature = feature.elementTextTrim("sfc1");
			String sfc2Feature = feature.elementTextTrim("sfc2");

			Element size = root.element("size");
			String plSize = size.elementTextTrim("pl");
			String sfc1Size = size.elementTextTrim("sfc1");
			String sfc2Size = size.elementTextTrim("sfc2");

			Element layer = root.element("layer");
			String plLayer = layer.elementTextTrim("pl");

			String time = root.elementTextTrim("time");
			String number = root.elementTextTrim("number");

			Element write = root.element("write");
			String rootpathLocal = write.elementTextTrim("rootpathLocal");
			String rootpath147 = write.elementTextTrim("rootpath147");

			parameter.setPlFeature(stringToArray(plFeature));
			parameter.setSfc1Feature(stringToArray(sfc1Feature));
			parameter.setSfc2Feature(stringToArray(sfc2Feature));

			parameter.setPlLayer(stringToArray(plLayer));

			parameter.setPlSize(plSize);
			parameter.setSfc1Size(sfc1Size);
			parameter.setSfc2Size(sfc2Size);

			parameter.setTime(time);
			parameter.setNumber(Integer.parseInt(number));
			parameter.setRootPath147(rootpath147);
			parameter.setRootpathLocal(rootpathLocal);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
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
				logger.error("关闭异常 , 异常 : " + e);
			}
		}
		return parameter;

	}

	private List<String> stringToArray(String str) {
		String[] strs = str.split(",");
		List<String> arrays = new ArrayList<String>(Arrays.asList(strs));
		return arrays;
	}

	public  SizeParameter getSizeParameter()
	{
		return sizeParameter;
	}
	
}
