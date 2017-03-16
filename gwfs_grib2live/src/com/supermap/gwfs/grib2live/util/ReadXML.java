package com.supermap.gwfs.grib2live.util;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.supermap.gwfs.grib2live.entity.Parameter;

/**
 * 使用DOM解析XML文件
 * 
 * @author LQ
 *
 */

public class ReadXML {
	private Logger logger = Logger.getLogger(this.getClass());

	public Parameter getParameter() {
		Parameter parameter = new Parameter();
		try {
			SAXReader reader = new SAXReader();
			File xmlFile = new File("config/grib2convert/grib2convert.xml");
			Document doc = reader.read(xmlFile);
			Element root = doc.getRootElement();

			Element size = root.element("size");
			String cqSize = size.elementTextTrim("chongqing");

			Element write = root.element("write");
			String rootPath = write.elementTextTrim("rootpath");

			Rectangle2D bounds = getBounds(stringToArray(cqSize));

			parameter.setChongqingBounds(bounds);
			parameter.setRootPath(rootPath);

		} catch (DocumentException e) {
			logger.error("supermap::"+e.getMessage(), e);
		}
		return parameter;

	}

	private List<String> stringToArray(String str) {
		String[] strs = str.split(",");
		List<String> arrays = new ArrayList<String>(Arrays.asList(strs));
		return arrays;
	}

	private Rectangle2D getBounds(List<String> b) {
		Rectangle2D bounds = new Rectangle2D.Float();
		float x = Float.parseFloat(b.get(0));
		float y = Float.parseFloat(b.get(1));
		float w = Float.parseFloat(b.get(2));
		float h = Float.parseFloat(b.get(3));
		bounds.setRect(x, y, w, h);
		return bounds;
	}
}