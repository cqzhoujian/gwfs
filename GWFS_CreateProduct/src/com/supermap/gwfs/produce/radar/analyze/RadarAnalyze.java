package com.supermap.gwfs.produce.radar.analyze;

import java.awt.geom.Rectangle2D;

import com.mg.objects.Dataset;
import com.mg.objects.Datasource;
import com.mg.objects.Layer;
import com.mg.objects.Map;
import com.mg.objects.Projection;
import com.mg.objects.Workspace;
import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;

/**  
 * @Description: 雷达数据解析
 * @author zhoujian
 * @date 2017-3-14
 * @version V1.0 
 */
public class RadarAnalyze
{
	private Logger logger = LoggerFactory.getLogger("RadarAnalyze");
	public static RadarAnalyze radarAnalyze = null;
	
	private RadarAnalyze()
	{}
	
	public static synchronized RadarAnalyze getInstance()
	{
		if (radarAnalyze == null)
		{
			radarAnalyze = new RadarAnalyze();
		}
		return radarAnalyze;
	}
	/**
	 * 
	 * @Description: 解析雷达数据
	 * @param: ws 工作空间
	 * @param: map 地图
	 * @param: strJson 数据源字符串
	 * @param: xmlContent 配色方案xml文件内容
	 * @return boolean
	 * @throws
	 */
	public synchronized boolean  radarAnalyze(Workspace ws, Map map , String strJson , String xmlContent)
	{
		boolean result = true;
		Dataset dataset = null;
		try
		{
			//打开SWAN数据
//	        String strJson = "{\"Type\":\"SWAN\",\"Alias\":\"SWANTest\",\"Server\":\"C:/Users/LQ/Desktop/雷达组合放射率拼图/Z_OTHE_RADA2DCR_201703090054.bin\"}";
	        Datasource ds = ws.OpenDatasource(strJson);
	        dataset = ds.GetDataset(0);
	        
	        //创建图层
	        Layer layer = Layer.CreateInstance("RasterRange", ws);
	        layer.SetDataset(dataset);
	        //设置配色方案
	        layer.FromXML(xmlContent);
	        
	        map.InsertLayer(layer, -1);
	
	        Rectangle2D rc = dataset.GetBounds();
	        if (!dataset.GetProjection().equals(map.GetProjection()))
	        {
	            Projection.Transform(dataset.GetProjection(), map.GetProjection(), rc);
	        }
	        map.SetBounds(rc);
		}
	    catch (Exception e)
	    {
	    	logger.error("ZJ: Radar data analysis exception , exception : " + e);
	    	result = false;
	    }

		return result;
	}
	
}
