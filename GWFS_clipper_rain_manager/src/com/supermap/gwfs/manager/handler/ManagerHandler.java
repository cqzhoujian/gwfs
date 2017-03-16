package com.supermap.gwfs.manager.handler;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.disruptor.event.UniEvent;
import com.supermap.disruptor.handler.IHandler;
import com.supermap.gcpp.core.common.UniObject;

/**  
 * @Description: 管理simple裁切之后调用降水QPF程序
 * @author zhoujian
 * @date 2016-11-4
 * @version V1.0 
 */
public class ManagerHandler implements IHandler
{
	private Logger logger = LoggerFactory.getLogger("ManagerHandler");
	@Override
	public void onEvent(UniEvent event, long sequence, boolean endOfBatch) throws Exception
	{
		Object value = event.getValue();
		try
		{
			if (value instanceof UniObject)
			{
				UniObject parameter = (UniObject)value;
				String time = parameter.getStringValue("time");
				String timeSequrence = parameter.getStringValue("timeSequrence");
				String timeValid = parameter.getStringValue("timeValid");
//				String sfc1AbsolutePath = parameter.getStringValue("absolutePath");
//				int type = parameter.getIntegerValue("type");
				logger.info("调用降水客观订正管理者参数 : " + parameter);
				NewManager.getInstance(time, timeSequrence).manager(time, timeSequrence,timeValid);
//				pool.execute(managerThread);
			}
		}catch (Exception e) {
			logger.error("调用管理者Handler异常 ， 异常 : " + e);
		}
	}

}
