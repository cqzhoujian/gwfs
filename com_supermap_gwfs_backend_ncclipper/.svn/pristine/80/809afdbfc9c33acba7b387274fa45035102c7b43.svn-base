package com.supermap.gwfs.clipper.handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.supermap.disruptor.event.UniEvent;
import com.supermap.disruptor.handler.IHandler;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.clipper.ClipData;
import com.supermap.gwfs.clipper.util.Logger;

public class ClipperHandler implements IHandler {
	private Logger logger = Logger.getLogger(this.getClass());
	ClipData clip2Nc;
	ClipData upLoad;
	ClipData writeCtl;
	private ClipperThread thread;
	private ExecutorService pool = Executors.newFixedThreadPool(3);
	

	@Override
	public void onEvent(UniEvent event, long sequence, boolean endOfBatch) throws Exception {
		Object value = event.getValue();
		try {
			if (value instanceof UniObject) {
				UniObject params = (UniObject) value;
				String rootPath = params.getStringValue("rootPath");
				String forecastDate = params.getStringValue("forecastDate");
				String forecastSequence = params.getStringValue("forecastSequence");
				String forecastValid = params.getStringValue("forecastValid");
				int type = params.getIntegerValue("type");
				logger.info("The Clipper params is: \n" + params);
				thread = new ClipperThread(rootPath, forecastDate, forecastSequence, forecastValid ,type);
				pool.execute(thread);
			}
		} catch (Exception e) {
			logger.error("ClipperHandler Error:" + e.getMessage(), e);
		}

	}

}
