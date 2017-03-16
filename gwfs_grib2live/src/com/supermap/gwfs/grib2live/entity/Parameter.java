package com.supermap.gwfs.grib2live.entity;

import java.awt.geom.Rectangle2D;

public class Parameter {
	private String rootPath;
	private Rectangle2D chongqingBounds;

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public Rectangle2D getChongqingBounds() {
		return chongqingBounds;
	}

	public void setChongqingBounds(Rectangle2D chongqingBounds) {
		this.chongqingBounds = chongqingBounds;
	}

}
