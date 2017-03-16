package com.supermap.gwfs.clipper.entity;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriter;

public class ClipperEntity {

	private int plTime;
	private int sfc1Time;
	private int sfc2Time;
	private Rectangle2D plCellBounds;
	private Rectangle2D sfc1CellBounds;
	private Rectangle2D sfc2CellBounds;
	private Point2D plsizeCell;
	private Point2D sfc1sizeCell;
	private Point2D sfc2sizeCell;
	private double plNoDataValue;
	private double sfc1NoDataValue;
	private double sfc2NoDataValue;

	private NetcdfFile[] plncfiles;
	private NetcdfFile[] sfc1ncfiles;
	private NetcdfFile[] sfc2ncfiles;
	private NetcdfFileWriter plOutncfile;
	private NetcdfFileWriter sfc1Outncfile;
	private NetcdfFileWriter sfc2Outncfile;

	public int getPlTime() {
		return plTime;
	}

	public int getSfc1Time() {
		return sfc1Time;
	}

	public int getSfc2Time() {
		return sfc2Time;
	}

	public Rectangle2D getPlCellBounds() {
		return plCellBounds;
	}

	public Rectangle2D getSfc1CellBounds() {
		return sfc1CellBounds;
	}

	public Rectangle2D getSfc2CellBounds() {
		return sfc2CellBounds;
	}

	public Point2D getPlsizeCell() {
		return plsizeCell;
	}

	public Point2D getSfc1sizeCell() {
		return sfc1sizeCell;
	}

	public Point2D getSfc2sizeCell() {
		return sfc2sizeCell;
	}

	public double getPlNoDataValue() {
		return plNoDataValue;
	}

	public double getSfc1NoDataValue() {
		return sfc1NoDataValue;
	}

	public double getSfc2NoDataValue() {
		return sfc2NoDataValue;
	}

	public NetcdfFile[] getPlncfiles() {
		return plncfiles;
	}

	public NetcdfFile[] getSfc1ncfiles() {
		return sfc1ncfiles;
	}

	public NetcdfFile[] getSfc2ncfiles() {
		return sfc2ncfiles;
	}

	public NetcdfFileWriter getPlOutncfile() {
		return plOutncfile;
	}

	public NetcdfFileWriter getSfc1Outncfile() {
		return sfc1Outncfile;
	}

	public NetcdfFileWriter getSfc2Outncfile() {
		return sfc2Outncfile;
	}

	public void setPlTime(int plTime) {
		this.plTime = plTime;
	}

	public void setSfc1Time(int sfc1Time) {
		this.sfc1Time = sfc1Time;
	}

	public void setSfc2Time(int sfc2Time) {
		this.sfc2Time = sfc2Time;
	}

	public void setPlCellBounds(Rectangle2D plCellBounds) {
		this.plCellBounds = plCellBounds;
	}

	public void setSfc1CellBounds(Rectangle2D sfc1CellBounds) {
		this.sfc1CellBounds = sfc1CellBounds;
	}

	public void setSfc2CellBounds(Rectangle2D sfc2CellBounds) {
		this.sfc2CellBounds = sfc2CellBounds;
	}

	public void setPlsizeCell(Point2D plsizeCell) {
		this.plsizeCell = plsizeCell;
	}

	public void setSfc1sizeCell(Point2D sfc1sizeCell) {
		this.sfc1sizeCell = sfc1sizeCell;
	}

	public void setSfc2sizeCell(Point2D sfc2sizeCell) {
		this.sfc2sizeCell = sfc2sizeCell;
	}

	public void setPlNoDataValue(double plNoDataValue) {
		this.plNoDataValue = plNoDataValue;
	}

	public void setSfc1NoDataValue(double sfc1NoDataValue) {
		this.sfc1NoDataValue = sfc1NoDataValue;
	}

	public void setSfc2NoDataValue(double sfc2NoDataValue) {
		this.sfc2NoDataValue = sfc2NoDataValue;
	}

	public void setPlncfiles(NetcdfFile[] plncfiles) {
		this.plncfiles = plncfiles;
	}

	public void setSfc1ncfiles(NetcdfFile[] sfc1ncfiles) {
		this.sfc1ncfiles = sfc1ncfiles;
	}

	public void setSfc2ncfiles(NetcdfFile[] sfc2ncfiles) {
		this.sfc2ncfiles = sfc2ncfiles;
	}

	public void setPlOutncfile(NetcdfFileWriter plOutncfile) {
		this.plOutncfile = plOutncfile;
	}

	public void setSfc1Outncfile(NetcdfFileWriter sfc1Outncfile) {
		this.sfc1Outncfile = sfc1Outncfile;
	}

	public void setSfc2Outncfile(NetcdfFileWriter sfc2Outncfile) {
		this.sfc2Outncfile = sfc2Outncfile;
	}

	@Override
	public String toString() {
		return "ClipperEntity [plTime=" + plTime + ", plCellBounds=" + plCellBounds + ", plsizeCell=" + plsizeCell + ", plNoDataValue=" + plNoDataValue + "]";
	}

}
