package com.supermap.gwfs.rainproduct.gridforcast;

import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.rainproduct.service.GridForcastService;

/**  
 * @Description: 入库gridForcast数据(单例)
 * @author zhoujian
 * @date 2016-10-30
 * @version V1.0 
 */
public class GridForcast
{
//	private  UniObject uniObject = null;
	private static GridForcast gridForcast = null;
//	private final String element_val = "ER03";
	private GridForcastService gridForcastService = null;
	private GridForcast()
	{
		gridForcastService = new GridForcastService();
//		uniObject = gridForcastService.findGribForcast(origin_val , element_val);
//		uniObject = gridForcastService.findGridForcastSequnce(uniObject, origin_val);
//		uniObject = gridForcastService.findGridForcastValid(uniObject, element_val , uniObject.getIntegerValue("origin_id"));
//		uniObject = gridForcastService.findGribForcasServer(uniObject);
	}
	
	public static GridForcast getInstance()
	{
		if(gridForcast == null)
		{
			gridForcast = new GridForcast();
		}
		return gridForcast;
	}
	
	public UniObject queryGridForecast(String origin_val , String element_val)
	{
		UniObject uniObject = new UniObject();
		
		uniObject = gridForcastService.findGribForcast(origin_val , element_val);
		uniObject = gridForcastService.findGridForcastSequnce(uniObject, origin_val);
		uniObject = gridForcastService.findGridForcastValid(uniObject, element_val , uniObject.getIntegerValue("origin_id"));
		uniObject = gridForcastService.findGribForcasServer(uniObject);
		
		return uniObject.clone();
	}
//	
//	public UniObject getUniObject()
//	{
//		return uniObject.clone();
//	}
	
}