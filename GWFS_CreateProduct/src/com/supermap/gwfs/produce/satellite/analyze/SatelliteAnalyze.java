package com.supermap.gwfs.produce.satellite.analyze;

import java.awt.geom.Rectangle2D;

import com.mg.objects.Dataset;
import com.mg.objects.DatasetRaster;
import com.mg.objects.Datasource;
import com.mg.objects.Layer;
import com.mg.objects.Map;
import com.mg.objects.Projection;
import com.mg.objects.Workspace;
import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;

/**  
 * @Description: TODO
 * @author zhoujian
 * @date 2017-3-14
 * @version V1.0 
 */
public class SatelliteAnalyze
{
	private Logger logger = LoggerFactory.getLogger("SatelliteAnalyze");
	
	private static SatelliteAnalyze satelliteAnalyze = null;
	
	private SatelliteAnalyze()
	{}
	
	public static synchronized SatelliteAnalyze getInstance()
	{
		if (satelliteAnalyze == null)
		{
			satelliteAnalyze = new SatelliteAnalyze();
		}
		return satelliteAnalyze;
	}
	
	public synchronized boolean satelliteAnalyze(Workspace ws , Map map , String strJson)
	{
		boolean result = true;
		Datasource pds = null;
		Dataset pDataset = null;
		DatasetRaster pdr = null;
		try
		{
			//打开AWX数据
//			String strJson = "{\"Type\":\"AWX\",\"Alias\":\"AWXTest\",\"Server\":\"C:/Users/LQ/Desktop/weixing/SATE_L2_F2E_VISSR_MWB_LBT_SEC-VIS-20170301-0300.AWX\"}";
			pds = ws.OpenDatasource(strJson);
			pDataset = pds.GetDataset(0);
		
			pdr = (DatasetRaster)pDataset;
			pdr.SetPropertyValue("ColorTableItem", "{\"Color\":\"RGBA(0,0,0,0)\"}"); //透明处理
			
			//创建图层
			Layer pLayer = Layer.CreateInstance("RasterUnique", ws);
			pLayer.SetDataset(pDataset);
	
			map.InsertLayer(pLayer, -1);
	
			Rectangle2D rc = pDataset.GetBounds();
			if (!pDataset.GetProjection().equals(map.GetProjection()))
			{
				Projection.Transform(pDataset.GetProjection(), map.GetProjection(), rc);
			}
			map.SetBounds(rc);
		}
		catch (Exception e)
		{
			logger.error("ZJ: Satellite data analysis exception , exception : " + e);
			result = false;
		}
		return result;
	}
}
