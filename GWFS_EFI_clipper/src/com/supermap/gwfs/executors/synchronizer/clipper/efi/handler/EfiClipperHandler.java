package com.supermap.gwfs.executors.synchronizer.clipper.efi.handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.disruptor.event.UniEvent;
import com.supermap.disruptor.handler.IHandler;
import com.supermap.gcpp.core.common.UniObject;

/**  
 * @Description: EFI数据裁切
 * @author zhoujian
 * @date 2016-11-15
 * @version V1.0 
 */
public class EfiClipperHandler implements IHandler
{
	private Logger logger = LoggerFactory.getLogger("EfiClipperHandler");
	private ExecutorService pool = Executors.newFixedThreadPool(2);
	private EfiClipperThread thread = null;
	@Override
	public void onEvent(UniEvent event, long sequence, boolean endOfBatch) throws Exception
	{
		Object value = event.getValue();
		try {
			if (value instanceof UniObject) 
			{
				UniObject params = (UniObject) value;
				String rootPath = params.getStringValue("rootPath"); // /mnt/simple/netcdf/efi
				String forecastDate = params.getStringValue("forecastDate"); // 20161011
				String forecastSequence = params.getStringValue("forecastSequence");//00
				String forecastValid = params.getStringValue("forecastValid");	//240

				logger.info("调用EFI类型的clipper程序参数 : \n" + params);
				thread = new EfiClipperThread(rootPath, forecastDate, forecastSequence, forecastValid);
				pool.execute(thread);
			}
		} 
		catch (Exception e)
		{
			logger.error("调用EFI类型数据clipperHandler异常  ， 异常 :" + e);
		}
	}
}
