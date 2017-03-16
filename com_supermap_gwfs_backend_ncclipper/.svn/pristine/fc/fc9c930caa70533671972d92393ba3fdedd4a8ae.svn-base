package com.supermap.gwfs.clipper.entity;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class SizeParameter {
	private List<String> plFeature;
	private List<String> sfc1Feature;
	private List<String> sfc2Feature;
	private String plSize;
	private String sfc1Size;
	private String sfc2Size;
	private List<String> plLayer;
	private List<String> sfc1Layer;
	private List<String> sfc2Layer;
	private String time;
	private int number;
	private String rootpathLocal;
	private String rootPath147;

	public void setSfc1Layer(List<String> sfc1Layer) {
		this.sfc1Layer = sfc1Layer;
	}

	public void setSfc2Layer(List<String> sfc2Layer) {
		this.sfc2Layer = sfc2Layer;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getTime() {
		return Integer.parseInt(time);
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getRootpathLocal() {
		return rootpathLocal;
	}

	public String getRootPath147() {
		return rootPath147;
	}

	public void setRootpathLocal(String rootpathLocal) {
		this.rootpathLocal = rootpathLocal;
	}

	public void setRootPath147(String rootPath147) {
		this.rootPath147 = rootPath147;
	}

	public List<String> getPlFeature() {
		return plFeature;
	}

	public List<String> getSfc1Feature() {
		return sfc1Feature;
	}

	public List<String> getSfc2Feature() {
		return sfc2Feature;
	}

	public String getPlSize() {
		return plSize;
	}

	public String getSfc1Size() {
		return sfc1Size;
	}

	public String getSfc2Size() {
		return sfc2Size;
	}

	public List<String> getPlLayer() {
		return plLayer;
	}

	public List<String> getSfc1Layer() {
		return sfc1Layer;
	}

	public List<String> getSfc2Layer() {
		return sfc2Layer;
	}

	public void setPlFeature(List<String> plFeature) {
		this.plFeature = plFeature;
	}

	public void setSfc1Feature(List<String> sfc1Feature) {
		this.sfc1Feature = sfc1Feature;
	}

	public void setSfc2Feature(List<String> sfc2Feature) {
		this.sfc2Feature = sfc2Feature;
	}

	public void setPlSize(String plSize) {
		this.plSize = plSize;
	}

	public void setSfc1Size(String sfc1Size) {
		this.sfc1Size = sfc1Size;
	}

	public void setSfc2Size(String sfc2Size) {
		this.sfc2Size = sfc2Size;
	}

	public void setPlLayer(List<String> plLayer) {
		this.plLayer = plLayer;
	}

	public Rectangle2D getPlBounds() {
		String plSize = getPlSize();
		return getBounds(plSize);
	}

	public Rectangle2D getSfc1Bounds() {
		return getBounds(getSfc1Size());
	}

	public Rectangle2D getSfc2Bounds() {
		return getBounds(getSfc2Size());
	}

	/**
	 * 获取Bounds
	 * @param size
	 * @return
	 */
	private Rectangle2D getBounds(String size) {
		Rectangle2D bounds = new Rectangle2D.Float();
		String[] str = size.split(",");
		List<Float> flos = new ArrayList<Float>();
		for (int i = 0; i < str.length; i++) {
			flos.add(Float.parseFloat(str[i]));
		}
		bounds.setRect(flos.get(0), flos.get(2), flos.get(1) - flos.get(0), flos.get(3) - flos.get(2));
		return bounds;
	}

	@Override
	public String toString() {
		return "SizeParameter: \n [plFeature=" + plFeature + ", plSize=" + plSize + ", plLayer=" + plLayer + ", time=" + time + ", rootpathLocal=" + rootpathLocal + ", rootPath147=" + rootPath147 + "]";
	}

}
