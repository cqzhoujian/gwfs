package com.supermap.gwfs.clipper.handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.supermap.gwfs.clipper.util.TimeUtil;

public class TestNcClipper {
	public static ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	public static void main(String[] args) {
		long lStart = System.currentTimeMillis();
		ClipperThread main1 = new ClipperThread("D:\\mnt\\simple\\ecmwf\\clipper", "20161222", "12", "228",1);
		main1.run();

		System.out.println(TimeUtil.formatDuring(lStart, System.currentTimeMillis()));
	}

}
