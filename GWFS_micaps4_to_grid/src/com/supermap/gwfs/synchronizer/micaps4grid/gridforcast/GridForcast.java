package com.supermap.gwfs.synchronizer.micaps4grid.gridforcast;

import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.synchronizer.micaps4grid.service.GridForcastService;

/**  
 * @Description: 入库gridForcast数据(单例)
 * @author zhoujian
 * @date 2016-10-30
 * @version V1.0 
 */
public class GridForcast
{
	private  UniObject uniObject = null;
	private static GridForcast gridForcast = null;
//	private final String origin_val = "ecthin";
	GridForcastService gridForcastService = null;
	private GridForcast()
	{
		gridForcastService = new GridForcastService();
	}
	
	public static synchronized GridForcast getInstance()
	{
		if(gridForcast == null)
		{
			gridForcast = new GridForcast();
		}
		return gridForcast;
	}
	
	public UniObject getGridForcastData(String element_val , String origin_val)
	{
		uniObject = gridForcastService.findGribForcast(origin_val , element_val);
		uniObject = gridForcastService.findGridForcastSequnce(uniObject, origin_val);
		uniObject = gridForcastService.findGridForcastValid(uniObject, element_val,uniObject.getIntegerValue("origin_id"));
		uniObject = gridForcastService.findGridForcastLevel(uniObject, element_val,uniObject.getIntegerValue("origin_id"));
		uniObject = gridForcastService.findGribForcasServer(uniObject);
		
		return uniObject.clone();
	}
}
