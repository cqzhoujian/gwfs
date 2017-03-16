package com.supermap.gwfs.rainproduct.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.disruptor.event.UniEvent;
import com.supermap.disruptor.handler.IHandler;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.rainproduct.correction.CustomCorrection;

/**  
 * @Description: 降水客观订正Handler
 * @author zhoujian
 * @date 2016-10-31
 * @version V1.0 
 */
public class CustomHandler implements IHandler
{
	private Logger logger = LoggerFactory.getLogger("CustomHandler");
	private CustomCorrection corrThread;
	private ExecutorService pool = Executors.newFixedThreadPool(2);
	@Override
	public void onEvent(UniEvent event, long sequence, boolean endOfBatch) throws Exception
	{
		Object value = event.getValue();
		try
		{
			if (value instanceof UniObject)
			{
//				UniObject parameter = (UniObject)value;
//				String time = parameter.getStringValue("time");
//				String timeSequrence = parameter.getStringValue("timeSequnce");
//				List<String> timeValid = (List<String>)parameter.getValue("timeValids");
//				logger.info("调用降水客观订正参数 : " + parameter);
//				for (int i = 0; i < timeValid.size();i++)
//				{
//					logger.debug("准备创建 " + timeValid.get(i) +" 时效的降水对象！");
//					corrThread = new CustomCorrection(time, timeSequrence, timeValid.get(i));
//					logger.info(time + " " + timeSequrence + " " + timeValid.get(i) + " Thread: " + corrThread);
//					pool.execute(corrThread);
//				}
				//20161219
				UniObject parameter = (UniObject)value;
				String time = parameter.getStringValue("time");
				String timeSequrence = parameter.getStringValue("timeSequnce");
				String timeValid = parameter.getStringValue("timeValids");
				logger.info("调用降水客观订正参数 : " + parameter);
				logger.debug("准备创建 " + timeValid +" 时效的降水对象！");
				corrThread = new CustomCorrection(time, timeSequrence, timeValid);
				logger.info(time + " " + timeSequrence + " " + timeValid + " Thread: " + corrThread);
				pool.execute(corrThread);
				
			}
		}
		catch (Exception e)
		{
			logger.error("CorrectionHandler 异常 , 异常 : " + e);
		}
	}
	public static void main(String[] args) throws Exception
	{
		UniObject uniObject = new UniObject();
		uniObject.setStringValue("time", "20161114");
		uniObject.setStringValue("timeSequnce", "0");
		List<String> list = new ArrayList<String>();
		list.add("3");
		list.add("6");
		list.add("9");
		list.add("12");
		uniObject.setValue("timeValids", list);
		UniEvent uniEvent = new UniEvent();
		uniEvent.setValue(uniObject);
		new CustomHandler().onEvent(uniEvent, 1000, true);
	}
}
