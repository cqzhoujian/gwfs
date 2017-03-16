package com.supermap.gwfs.grib2live.handler;

import com.supermap.disruptor.event.UniEvent;
import com.supermap.disruptor.handler.IHandler;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.grib2live.util.Logger;

public class Grib2LiveHandler implements IHandler {
	private Logger logger = Logger.getLogger(this.getClass());
	private Grib2LiveThread thread;

	// private ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	@Override
	public void onEvent(UniEvent event, long sequence, boolean endOfBatch) throws Exception {
		Object value = event.getValue();
		try {
			if (value instanceof UniObject) {
				UniObject params = (UniObject) value;
				String filePath = params.getStringValue("absolutePath");
				if (!filePath.contains("00000.GRB2") || filePath.contains(".tmp")) {
					// 过滤部分没用的文件
				} else {
					boolean isDownloaded = params.getBooleanValue("isDownloaded");
					if (isDownloaded) {
						if (filePath.contains("TMP"))
						{
							logger.info("The Clipper params is: \n" + params);
							thread = new Grib2LiveThread(filePath);
							thread.run();
							// pool.execute(thread);
						}
					}
				}

			}

		} catch (Exception e) {
			logger.error("supermap::ClipperHandler Error:" + e.getMessage(), e);
		}
	}

}
