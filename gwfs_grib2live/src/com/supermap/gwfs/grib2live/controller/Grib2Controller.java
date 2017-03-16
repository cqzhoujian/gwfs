package com.supermap.gwfs.grib2live.controller;

import java.util.List;
import java.util.Map;

import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.grib2live.entity.GridsData;
import com.supermap.gwfs.grib2live.service.GribService;
import com.supermap.gwfs.grib2live.service.WriteNCService;
import com.supermap.gwfs.grib2live.util.Logger;

public class Grib2Controller{
	private Logger logger = Logger.getLogger(this.getClass());
	private GribService gribService = new GribService();
	WriteNCService ncService = new WriteNCService();

	public void startConvert(String filePath){
		try{
			logger.info("ZJ:filePath : " + filePath);
			List<GridsData> gridsDatas = gribService.listAllGrib(filePath);
			logger.info("ZJ:data size : " + gridsDatas.size());
			if(gridsDatas.size() > 0)
			{
				// 写数据
				for(GridsData gridsData : gridsDatas)
				{
					boolean isWritten = writeNC(gridsData);
					if(isWritten)
					{
						if (gridsData.getHour() == 8)
						{
							ProssTmp.getInstance().arrayInit("TMAX");
						}
						if (gridsData.getHour() == 20)
						{
							ProssTmp.getInstance().arrayInit("TMIN");
						}
						ProssTmp.getInstance().TmpComparison(getGridForecastToUni(gridsData));
					}
				}
			}
			else
			{
				logger.error("ZJ:" + filePath + " file is wrong!");
			}	
		}catch(Exception e){
			logger.error("ZJ: Grib2 Covert is error ! " + e);
		}
	}

	private UniObject getGridForecastToUni(GridsData gridsData)
	{
		UniObject uniObject = new UniObject();
		uniObject.setValue("unit", gridsData.getUnit());
		uniObject.setValue("rows", gridsData.getRows());
		uniObject.setValue("cols", gridsData.getCols());
		uniObject.setValue("dimTime", gridsData.getDimTime());
		uniObject.setValue("date", gridsData.getDate());
		uniObject.setValue("element", gridsData.getElement());
		uniObject.setValue("hour", gridsData.getHour());
		uniObject.setValue("level", gridsData.getLevel());
		uniObject.setValue("dValue", gridsData.getdValue());
		uniObject.setValue("left", gridsData.getLeft());
		uniObject.setValue("bottom", gridsData.getBottom());
		uniObject.setValue("deltaX", gridsData.getDeltaX());
		uniObject.setValue("deltaY", gridsData.getDeltaY());
		uniObject.setValue("filePath", gridsData.getFilePath());
		uniObject.setValue("forecast", gridsData.getForecast());
		return uniObject.clone();
	}

	public boolean writeNC(GridsData gridsData) {
		int forecastDate = gridsData.getForecast();
		String unit = gridsData.getUnit();
		int level = (int) gridsData.getLevel();
		int cols = gridsData.getCols();
		int rows = gridsData.getRows();
		float left = gridsData.getLeft();
		float bottom = gridsData.getBottom();
		float dx = gridsData.getDeltaX();
		float dy = gridsData.getDeltaY();
		// float[] dvalue = gridsData.getdValue();
		String path = gridsData.getFilePath();

		Map<String, float[]> elementValue = gridsData.getElementValue();
		//20160913
		return ncService.writeData(path, elementValue, level, left, bottom, dx, dy, forecastDate, cols, rows, unit);
	}
}
