package com.supermap.gwfs.grib2live.handler;

import com.supermap.gwfs.grib2live.controller.Grib2Controller;

public class Grib2LiveThread extends Thread {

	private Grib2Controller controller;
	private String filePath;

	private Grib2LiveThread() {
		controller = new Grib2Controller();
	}

	public Grib2LiveThread(String filePath) {
		this();
		this.filePath = filePath;
	}

	@Override
	public void run() {
		controller.startConvert(filePath);
	}

}
