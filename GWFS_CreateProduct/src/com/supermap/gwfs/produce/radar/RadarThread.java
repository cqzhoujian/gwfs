package com.supermap.gwfs.produce.radar;

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

/**  
 * @Description: TODO
 * @author zhoujian
 * @date 2017-3-16
 * @version V1.0 
 */
public class RadarThread extends ProductSuper
{
	private String path = null;
	private Logger logger = null;
	public RadarThread(String filePath)
	{
		this.path = filePath;
		logger = LoggerFactory.getLogger("RadarThread");
		super.setType("radar");
		super.setLogger(logger);
	}
	
	/**
	 * 获取雷达产品参数
	 */
	@Override
	public UniObject getProductParam(UniObject productParam)
	{
		UniObject radar = null;
		if (productParam != null)
		{
			List<UniObject> radars = productParam.getObjectsByName("radar");
			if (radars.size() > 0)
			{
				radar = radars.get(0);
			}
		}
		return radar;
	}
	
	/**
	 * 解析雷达数据
	 */
	@Override
	public boolean analyzeData(Workspace ws, Map map , UniObject param)
	{
		//获取数据源字符串
		String strJson = this.getDatasourceParameter("SWAN", "SWAN", path);
		/**
		 * 解析雷达数据
		 */
		boolean flag = RadarAnalyze.getInstance().radarAnalyze(ws, map, strJson, param.getStringValue("xmlContent"));
		return flag;
	}
	/**
	 * 产品名称
	 * 根据实际需求设置产品名称
	 */
	@Override
	public String getFileName(UniObject param)
	{
		String fileName = path.substring(path.replace("\\", "/").lastIndexOf("/") + 1, path.lastIndexOf(".")) + param.getStringValue("out_type");
		return fileName;
	}
	/**
	 * 绘制雷达产品图片
	 */
	@Override
	public boolean draw(Workspace ws, Map map, Point2D ptViewport, UniObject param, String filePath)
	{
		String strJson = this.getDatasourceParameter("ESRI Shapefile", "China_Province_pl", param.getStringValue("shp_path"));
		boolean flag = DrawImage.getInstance().drawImage(ws, map, ptViewport, strJson , filePath , "湖北省");
		return flag;
	}

	public static void main(String[] args)
	{
		new RadarThread("F:/test/雷达组合放射率拼图/Z_OTHE_RADA2DCR_201703090042.bin").start();
	}
}
