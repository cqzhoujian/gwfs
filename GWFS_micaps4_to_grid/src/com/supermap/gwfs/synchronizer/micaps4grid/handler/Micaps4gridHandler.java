package com.supermap.gwfs.synchronizer.micaps4grid.handler;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.disruptor.event.UniEvent;
import com.supermap.disruptor.handler.IHandler;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.synchronizer.micaps4grid.Micaps4gridThread;

/**  
 * @Description: TODO
 * @author zhoujian
 * @date 2016-11-29
 * @version V1.0 
 */
public class Micaps4gridHandler implements IHandler
{
	private Logger logger = LoggerFactory.getLogger("MicapsHandler");
	private Micaps4gridThread micapsThread = null;
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
				String path = param.getStringValue("absolutePath");
				boolean isDowload = param.getBooleanValue("isDownloaded");
				String element = param.getStringValue("element");
				if (isDowload)
				{
					logger.debug("Micaps2NetcdfHandler被调用 ： " + param);
					micapsThread = new Micaps4gridThread(path,element);
					pool.execute(micapsThread);
				}
			}
		}
		catch (Exception e) 
		{
			logger.error("调用Micaps4handler异常 ， 异常 ： " + e);
		}
	}
}
