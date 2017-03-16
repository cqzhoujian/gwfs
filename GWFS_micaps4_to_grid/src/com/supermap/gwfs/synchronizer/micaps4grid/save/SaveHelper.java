package com.supermap.gwfs.synchronizer.micaps4grid.save;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.synchronizer.micaps4grid.gridforcast.GridForcast;
import com.supermap.gwfs.synchronizer.micaps4grid.service.GridForcastService;

/**  
 * @Description: 保存辅助类
 * @author zhoujian
 * @date 2017-2-16
 * @version V1.0 
 */
public class SaveHelper
{
	private Logger logger = LoggerFactory.getLogger("DBMicaps");
	private static SaveHelper saveHelper = null;
	private  SaveHelper()
	{
	}
	public static SaveHelper getInstance()
	{
		if (saveHelper == null)
		{
			saveHelper = new SaveHelper();
		}
		return saveHelper;
	}
	
	public void saveData(UniObject uniObject)
	{
		int res = 0;
		try
		{
			UniObject uniObject1 = GridForcast.getInstance().getGridForcastData(uniObject.getStringValue("element") , uniObject.getStringValue("origin_val"));
//			System.out.println(uniObject1);
			uniObject1.setStringValue("filePath", uniObject.getStringValue("filePath1"));
			uniObject1.setStringValue("fileName", uniObject.getStringValue("fileName"));
			uniObject1.setStringValue("forcastDate", uniObject.getStringValue("_forcastDate"));
			uniObject1.setStringValue("fileversion", uniObject.getStringValue("forecast_fileversion"));
			uniObject1.setStringValue("forecast_date", uniObject.getStringValue("_forcastDate"));
//			System.out.println(uniObject1);
			res = new GridForcastService().savaGridForcast(uniObject1, uniObject.getStringValue("sequrence"), uniObject.getStringValue("valid_"),uniObject.getStringValue("level"));
//			System.out.println("插入影响行数 ： " + res);
			logger.info("ZJ:element : "+ uniObject.getStringValue("element") +" , forecast_date : "+uniObject.getStringValue("_forcastDate")+"sequrence : " + uniObject.getStringValue("sequrence") + " , valid : " + uniObject.getStringValue("valid_") + " , level : " + uniObject.getStringValue("level") + " save Number of affected rows : " + res);		}
		catch (Exception e)
		{
			logger.error("ZJ:save data is error , error : " + e);
		}
	}
}
