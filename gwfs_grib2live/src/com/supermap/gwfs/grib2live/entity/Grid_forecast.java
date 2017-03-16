package com.supermap.gwfs.grib2live.entity;

import java.sql.Date;
import java.sql.Timestamp;

public class Grid_forecast {

	private Integer id;
	private Integer origin_id; // '所属初始场编号',
	private String origin_name; // '初始场名称',
	private String origin_value; // '初始场值',
	private Integer element_id; // '要素/天气现象编号',
	private String element_name;	//'要素名称'
	private String element_value;	//'要素值'
	private Date forecast_date; // '预报日期',
	private Integer forecast_sequence_id; // '预报时次编号',
	private String forecast_sequence_name; // '预报时次名称',
	private String forecast_sequence_value; // '预报时次值',
	private Integer forecast_level_id; // '预报层次编号',
	private String forecast_level_name; // '预报层次名称',
	private Integer forecast_level_value; // '预报层次值',
	private Integer forecast_valid_id; // '预报时效编号',
	private String forecast_valid_name; // '预报时效名称',
	private Integer forecast_valid_value; // '预报时效值',
	private Integer server_id; // '预报文件存放服务器编号',
	private String forecast_filename; // '预报文件名称',
	private String forecast_fileversion; // '预报文件版本',
	private String forecast_filepath; // '预报文件路径',
	private Timestamp create_time; // '预报文件路径',

	public Grid_forecast() {
	};

	public Grid_forecast(Integer id, Integer origin_id, String origin_name, String origin_value, Integer element_id, String element_name, String element_value, Date forecast_date, Integer forecast_sequence_id, String forecast_sequence_name, String forecast_sequence_value, Integer forecast_level_id, String forecast_level_name, Integer forecast_level_value, Integer forecast_valid_id, String forecast_valid_name, Integer forecast_valid_value, Integer server_id, String forecast_filename, String forecast_fileversion, String forecast_filepath, Timestamp create_time) {
		super();
		this.id = id;
		this.origin_id = origin_id;
		this.origin_name = origin_name;
		this.origin_value = origin_value;
		this.element_id = element_id;
		this.element_name = element_name;
		this.element_value = element_value;
		this.forecast_date = forecast_date;
		this.forecast_sequence_id = forecast_sequence_id;
		this.forecast_sequence_name = forecast_sequence_name;
		this.forecast_sequence_value = forecast_sequence_value;
		this.forecast_level_id = forecast_level_id;
		this.forecast_level_name = forecast_level_name;
		this.forecast_level_value = forecast_level_value;
		this.forecast_valid_id = forecast_valid_id;
		this.forecast_valid_name = forecast_valid_name;
		this.forecast_valid_value = forecast_valid_value;
		this.server_id = server_id;
		this.forecast_filename = forecast_filename;
		this.forecast_fileversion = forecast_fileversion;
		this.forecast_filepath = forecast_filepath;
		this.create_time = create_time;
	}

	public Timestamp getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Timestamp create_time) {
		this.create_time = create_time;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getOrigin_id() {
		return origin_id;
	}

	public void setOrigin_id(Integer origin_id) {
		this.origin_id = origin_id;
	}

	public String getOrigin_name() {
		return origin_name;
	}

	public void setOrigin_name(String origin_name) {
		this.origin_name = origin_name;
	}

	public String getOrigin_value() {
		return origin_value;
	}

	public void setOrigin_value(String origin_value) {
		this.origin_value = origin_value;
	}

	public Integer getElement_id() {
		return element_id;
	}

	public void setElement_id(Integer element_id) {
		this.element_id = element_id;
	}

	public String getElement_name() {
		return element_name;
	}

	public void setElement_name(String element_name) {
		this.element_name = element_name;
	}

	public String getElement_value() {
		return element_value;
	}

	public void setElement_value(String element_value) {
		this.element_value = element_value;
	}

	public Date getForecast_date() {
		return forecast_date;
	}

	public void setForecast_date(Date forecast_date) {
		this.forecast_date = forecast_date;
	}

	public Integer getForecast_sequence_id() {
		return forecast_sequence_id;
	}

	public void setForecast_sequence_id(Integer forecast_sequence_id) {
		this.forecast_sequence_id = forecast_sequence_id;
	}

	public String getForecast_sequence_name() {
		return forecast_sequence_name;
	}

	public void setForecast_sequence_name(String forecast_sequence_name) {
		this.forecast_sequence_name = forecast_sequence_name;
	}

	public String getForecast_sequence_value() {
		return forecast_sequence_value;
	}

	public void setForecast_sequence_value(String forecast_sequence_value) {
		this.forecast_sequence_value = forecast_sequence_value;
	}

	public Integer getForecast_level_id() {
		return forecast_level_id;
	}

	public void setForecast_level_id(Integer forecast_level_id) {
		this.forecast_level_id = forecast_level_id;
	}

	public String getForecast_level_name() {
		return forecast_level_name;
	}

	public void setForecast_level_name(String forecast_level_name) {
		this.forecast_level_name = forecast_level_name;
	}

	public Integer getForecast_level_value() {
		return forecast_level_value;
	}

	public void setForecast_level_value(Integer forecast_level_value) {
		this.forecast_level_value = forecast_level_value;
	}

	public Integer getForecast_valid_id() {
		return forecast_valid_id;
	}

	public void setForecast_valid_id(Integer forecast_valid_id) {
		this.forecast_valid_id = forecast_valid_id;
	}

	public String getForecast_valid_name() {
		return forecast_valid_name;
	}

	public void setForecast_valid_name(String forecast_valid_name) {
		this.forecast_valid_name = forecast_valid_name;
	}

	public Integer getForecast_valid_value() {
		return forecast_valid_value;
	}

	public void setForecast_valid_value(Integer forecast_valid_value) {
		this.forecast_valid_value = forecast_valid_value;
	}

	public Integer getServer_id() {
		return server_id;
	}

	public void setServer_id(Integer server_id) {
		this.server_id = server_id;
	}

	public String getForecast_filename() {
		return forecast_filename;
	}

	public void setForecast_filename(String forecast_filename) {
		this.forecast_filename = forecast_filename;
	}

	public String getForecast_fileversion() {
		return forecast_fileversion;
	}

	public void setForecast_fileversion(String forecast_fileversion) {
		this.forecast_fileversion = forecast_fileversion;
	}

	public String getForecast_filepath() {
		return forecast_filepath;
	}

	public void setForecast_filepath(String forecast_filepath) {
		this.forecast_filepath = forecast_filepath;
	}

	@Override
	public String toString() {
		return id + ", " + origin_value + ", " + element_value + ", " + forecast_sequence_value + ", " + forecast_level_value + ", " + forecast_valid_value + ", " + forecast_filename;
	}

}
