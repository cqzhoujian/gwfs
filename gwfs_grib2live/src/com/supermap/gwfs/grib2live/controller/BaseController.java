package com.supermap.gwfs.grib2live.controller;

import com.supermap.gwfs.grib2live.entity.Parameter;
import com.supermap.gwfs.grib2live.util.ReadXML;

public class BaseController {
	private static Parameter parameter;
	static {
		ReadXML readXML = new ReadXML();
		setParameter(readXML.getParameter());
	}

	public static Parameter getParameter() {
		return parameter;
	}

	public static void setParameter(Parameter parameter) {
		BaseController.parameter = parameter;
	}
}
