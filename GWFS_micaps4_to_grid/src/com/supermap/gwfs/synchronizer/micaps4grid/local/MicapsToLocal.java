package com.supermap.gwfs.synchronizer.micaps4grid.local;

import java.io.File;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.synchronizer.micaps4grid.netcdf.Netcdf;
import com.supermap.gwfs.synchronizer.micaps4grid.save.SaveHelper;

/**  
 * @Description: ecthin初始场处理之后生成市台（local）数据
 * @author zhoujian
 * @date 2017-2-27
 * @version V1.0 
 */
public class MicapsToLocal
{
	private Logger logger = LoggerFactory.getLogger("MicapsToLocal");
	private final String origin_val = "local";
	private static MicapsToLocal micapsToLocal = null;
	private final String forecast_fileversion = "bilinear";
	private final String localElements = "ECT VIS EDA10 ERH ERHI ERHA";
	
	private MicapsToLocal()
	{}
	
	public static synchronized MicapsToLocal getInstance()
	{
		if (micapsToLocal == null)
		{
			micapsToLocal = new MicapsToLocal();
		}
		
		return micapsToLocal;
	}
	
	/**
	 * 
	 * @Description: netcdf文件中只有一个要素变量，如ECT ERH...
	 * @return void
	 * @throws
	 */
	public void doMicapsToLocal(UniObject uniObject)
	{
		try
		{
			if (uniObject != null)
			{
				//满足条件的要素才能写成市台数据和插入数据库
				if (localElements.contains(uniObject.getStringValue("element")))
				{
					this.setFilePathAndFileName(uniObject);
					boolean flag = Netcdf.getInstance().writeNetcdf(uniObject);
					if (flag)
					{
						/**
						 * 保存到数据库
						 */
//						System.out.println(uniObject);
						SaveHelper.getInstance().saveData(uniObject);
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.error("ZJ: " + uniObject.getStringValue("element") + " To Local Is Error , Error : " + e);
		}
	}
	
	/**
	 * 
	 * @Description: netcdf文件中有多个要素变量，如 风有UEDA10 VEDA10
	 * @return void
	 * @throws
	 */
	public void doMicapsToLocal(UniObject uniObject_v , UniObject uniObject_u)
	{
		try
		{
			if (uniObject_u != null && uniObject_v != null)
			{
				//只有 level = 0 的风要素才能写成市台数据和插入数据库(其他层次数据都是高层风，只能作为天气订正的参考)
				if (uniObject_v.getIntegerValue("level") == 0)
				{
					setFilePathAndFileName(uniObject_v);
					//uniObject_v中的element在此之前被替换成了EDA10插入数据库，所以现在重新写文件时把它还原
					uniObject_v.setValue("element", "10V");
					System.out.println(uniObject_v);
					boolean flag = Netcdf.getInstance().writeNetcdf(uniObject_v , uniObject_u);
					if (flag)
					{
						/**
						 * 保存到数据库
						 */
						System.out.println(uniObject_v);
						uniObject_v.setValue("element", "EDA10");
						SaveHelper.getInstance().saveData(uniObject_v);
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.error("ZJ: " + uniObject_v.getStringValue("element") + " To Local Is Error , Error : " + e);
		}
	}
	
	/**
	 * 
	 * @Description: netcdf文件名和文件路径
	 * @return String
	 * @throws
	 */
	private void setFilePathAndFileName(UniObject uniObject)
	{
		try
		{
			String filePath1 = uniObject.getStringValue("rootpathLocal") + "/" + origin_val + "/" + uniObject.getStringValue("element") + "/"  + uniObject.getStringValue("forcastDate").substring(0, 8) + "/" + uniObject.getStringValue("sequrence") + "/" +  uniObject.getStringValue("level") + "/" + forecast_fileversion;
			File file2 = new File(filePath1);
			if (!file2.exists())
			{
				file2.mkdirs();
			}
			String fileName =  uniObject.getStringValue("forcastDate").substring(0, 10) + "_" + uniObject.getStringValue("valid_")+ ".nc";
			uniObject.setValue("fileName", fileName);
			uniObject.setStringValue("filePath1", filePath1 + "/" + fileName);
			uniObject.setStringValue("element", uniObject.getStringValue("element"));
			uniObject.setStringValue("origin_val", origin_val);
			uniObject.setStringValue("forecast_fileversion", forecast_fileversion);
		}
		catch (Exception e)
		{
			logger.error("ZJ:getFileName error , error : " + e);
		}
	}
}
