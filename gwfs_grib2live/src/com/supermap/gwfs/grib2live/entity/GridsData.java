package com.supermap.gwfs.grib2live.entity;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Map;

public class GridsData {

	private String unit;
	private int rows;
	private int cols;
	private int dimTime;
	private Date date;
	private String element;
	private int hour;
	private int level;
	private Map<String, float[]> elementValue;
	private float[] dValue;
	private float left;
	private float bottom;
	private float deltaX;
	private float deltaY;
	private String fileName;
	private String filePath;
	private Timestamp create_time;
	//20160913
	private int forecast;

	public GridsData() {
		super();
	}

	public GridsData(String unit, int rows, int cols, int dimTime, Date date, String element, int hour, int level, float[] dValue, float left, float bottom, float deltaX, float deltaY, String fileName, String filePath, Timestamp create_time) {
		super();
		this.unit = unit;
		this.rows = rows;
		this.cols = cols;
		this.dimTime = dimTime;
		this.date = date;
		this.element = element;
		this.hour = hour;
		this.level = level;
		this.dValue = dValue;
		this.left = left;
		this.bottom = bottom;
		this.deltaX = deltaX;
		this.deltaY = deltaY;
		this.fileName = fileName;
		this.filePath = filePath;
		this.create_time = create_time;
	}
	

	public GridsData(String unit, int rows, int cols, int dimTime, Date date, String element, int hour, int level, float[] dValue, float left, float bottom, float deltaX, float deltaY, String fileName, String filePath, Timestamp create_time,int forecastDate) {
		super();
		this.unit = unit;
		this.rows = rows;
		this.cols = cols;
		this.dimTime = dimTime;
		this.date = date;
		this.element = element;
		this.hour = hour;
		this.level = level;
		this.dValue = dValue;
		this.left = left;
		this.bottom = bottom;
		this.deltaX = deltaX;
		this.deltaY = deltaY;
		this.fileName = fileName;
		this.filePath = filePath;
		this.create_time = create_time;
		this.forecast = forecastDate;
	}

	public String getElement() {
		return element;
	}

	public float[] getdValue() {
		return dValue;
	}

	public void setElement(String element) {
		this.element = element;
	}

	public void setdValue(float[] dValue) {
		this.dValue = dValue;
	}

	public String getUnit() {
		return unit;
	}

	public int getRows() {
		return rows;
	}

	public int getCols() {
		return cols;
	}

	public int getDimTime() {
		return dimTime;
	}

	public Date getDate() {
		return date;
	}

	

	public int getLevel() {
		return level;
	}

	public Map<String, float[]> getElementValue() {
		return elementValue;
	}

	public float getLeft() {
		return left;
	}

	public float getBottom() {
		return bottom;
	}

	public float getDeltaX() {
		return deltaX;
	}

	public float getDeltaY() {
		return deltaY;
	}

	public String getFileName() {
		return fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public Timestamp getCreate_time() {
		return create_time;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public void setCols(int cols) {
		this.cols = cols;
	}

	public void setDimTime(int dimTime) {
		this.dimTime = dimTime;
	}

	public void setDate(Date date) {
		this.date = date;
	}


	public void setLevel(int level) {
		this.level = level;
	}

	public void setElementValue(Map<String, float[]> elementValue) {
		this.elementValue = elementValue;
	}

	public void setLeft(float left) {
		this.left = left;
	}

	public void setBottom(float bottom) {
		this.bottom = bottom;
	}

	public void setDeltaX(float deltaX) {
		this.deltaX = deltaX;
	}

	public void setDeltaY(float deltaY) {
		this.deltaY = deltaY;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public void setCreate_time(Timestamp create_time) {
		this.create_time = create_time;
	}


	public int getForecast()
	{
		return forecast;
	}

	public void setForecast(int forecast)
	{
		this.forecast = forecast;
	}
	

	public int getHour()
	{
		return hour;
	}

	public void setHour(int hour)
	{
		this.hour = hour;
	}

	@Override
	public String toString() {
		return unit + ", " + rows + ", " + cols + ", " + dimTime + ", " + date + ", " + ", " + hour + " , " + level + ", " + left + ", " + bottom + ", " + deltaX + ", " + deltaY + ", " + fileName + ", " + filePath + ", " + create_time;
	}

}
