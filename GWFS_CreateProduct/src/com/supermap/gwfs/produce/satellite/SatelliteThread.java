package com.supermap.gwfs.produce.satellite;

import java.awt.geom.Point2D;
import java.util.List;

import com.mg.objects.Map;
import com.mg.objects.Workspace;
import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.produce.ProductSuper;
import com.supermap.gwfs.produce.draw.DrawImage;
import com.supermap.gwfs.produce.radar.analyze.RadarAnalyze;
import com.supermap.gwfs.produce.satellite.analyze.SatelliteAnalyze;

/**  
 * @Description: TODO
 * @author zhoujian
 * @date 2017-3-16
 * @version V1.0 
 */
public class SatelliteThread extends ProductSuper
{
	private String path = null;
	private Logger logger = null;
	
	public SatelliteThread(String filePath)
	{
		this.path = filePath;
		logger = LoggerFactory.getLogger("SatelliteThread");
		super.setType("satellite");
		super.setLogger(logger);
	}
	
	/**
	 * 获取卫星产品参数
	 */
	@Override
	public UniObject getProductParam(UniObject productParam)
	{
		UniObject satellite = null;
		if (productParam != null)
		{
			List<UniObject> satellites = productParam.getObjectsByName("satellite");
			if (satellites.size() > 0)
			{
				satellite = satellites.get(0);
			}
		}
		return satellite;
	}
	
	/**
	 * 解析产品数据
	 */
	@Override
	public boolean analyzeData(Workspace ws, Map map, UniObject param)
	{
		//数据源字符串
		String strJson = this.getDatasourceParameter("AWX", "AWX", path);
		/**
		 * 解析卫星数据
		 */
		boolean flag = SatelliteAnalyze.getInstance().satelliteAnalyze(ws, map, strJson);
		return flag;
	}
	
	/**
	 * 产品名称
	 * 根据实际需求设置文件名
	 */
	@Override
	public String getFileName(UniObject param)
	{
		String fileName = path.substring(path.replace("\\", "/").lastIndexOf("/") + 1, path.lastIndexOf(".")) + param.getStringValue("out_type");
		return fileName;
	}
	/**
	 * 绘制产品图片
	 */
	@Override
	public boolean draw(Workspace ws, Map map, Point2D ptViewport, UniObject param, String filePath)
	{
		//数据源字符串
		String strJson = this.getDatasourceParameter("ESRI Shapefile", "China_Province_pl", param.getStringValue("shp_path"));
		boolean flag = DrawImage.getInstance().drawImage(ws, map, ptViewport, strJson , filePath , null);
		return flag;
	}

	

}
