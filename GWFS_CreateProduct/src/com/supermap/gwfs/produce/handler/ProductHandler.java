package com.supermap.gwfs.produce.handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.disruptor.event.UniEvent;
import com.supermap.disruptor.handler.IHandler;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.produce.radar.RadarThread;
import com.supermap.gwfs.produce.satellite.SatelliteThread;

/**  
 * @Description: 产品Handler
 * @author zhoujian
 * @date 2017-3-14
 * @version V1.0 
 */
public class ProductHandler implements IHandler
{
	private Logger logger = LoggerFactory.getLogger("ProductHandler");
	private RadarThread radarThread = null;
	private SatelliteThread satelliteThread = null;
	private ExecutorService pool = Executors.newFixedThreadPool(2);

	@Override
	public void onEvent(UniEvent event, long sequence, boolean endOfBatch) throws Exception
	{
		try
		{
			Object value = event.getValue();
			if (value instanceof UniObject)
			{
				UniObject param = (UniObject)value;
				//TODO 获取参数根据实际需求获取
				String path = param.getStringValue("absolutePath");
				int type = param.getIntegerValue("type");
				//雷达数据(类型待确定)
				//TODO 类型中的 1 2的值根据实际情况调整
				if (type == 1)
				{
					radarThread = new RadarThread(path);
					pool.execute(radarThread);
				}
				//卫星数据
				else if (type == 2 )
				{
					satelliteThread = new SatelliteThread(path);
					pool.execute(satelliteThread);
				}
				//其他数据
				else
				{
					logger.error("ZJ: Data type is not satellite or radar data.");
				}
				
			}
		}
		catch (Exception e) 
		{
			logger.error("ZJ:　call ProductHandler exception , exception : " + e);
		}
	}
}
