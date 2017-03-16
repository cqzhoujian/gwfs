package com.supermap.gwfs.synchronizer.micaps4grid.micapsanalytical;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jettison.json.JSONObject;

import com.mg.objects.DatasetRaster;
import com.mg.objects.Datasource;
import com.mg.objects.Scanline;
import com.mg.objects.Workspace;
import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.synchronizer.micaps4grid.util.ConvertDate;
/**  
 * @Description: 解析micapse数据 (单例)
 * @author zhoujian
 * @date 2016-11-29
 * @version V1.0 
 */
public class MicapsAnalytical
{
	private Logger logger = LoggerFactory.getLogger("MicapsAnalytical");
	private static MicapsAnalytical micapsAnalytical = null;
	private MicapsAnalytical()
	{
	}
	
	public static synchronized MicapsAnalytical getInstance()
	{
		if (micapsAnalytical == null)
		{
			micapsAnalytical = new MicapsAnalytical();
		}
		return micapsAnalytical;
	}
	
	/**
	 * 
	 * @Description: 解析micaps数据
	 * @return UniObject
	 * @throws
	 */
	public synchronized UniObject Analytical(UniObject uniObject)
	{
		Workspace pws = null;
		Datasource pds = null;
		DatasetRaster pdr = null;
		try
		{
			pws = new Workspace();
			String strJson = "{\"Type\":\"Micaps\",\"Alias\":\"m07121120\",\"Server\":\""+uniObject.getStringValue("filePath")+"\"}";
			pds = pws.OpenDatasource(strJson);
			if (pds == null)
			{
				logger.error(uniObject.getStringValue("filePath") + " Datasource is null .");
				return null;
			}
			pdr = (DatasetRaster)pds.GetDataset(0);
			if (pdr == null)
			{
				logger.error(uniObject.getStringValue("filePath") + " DatasetRaster is null.");
				return null;
			}
			String strMateData = pdr.GetMetadata();
			JSONObject jsonObject = new JSONObject(strMateData);
			System.out.println(jsonObject);
			String year = jsonObject.getString("年");
			String month = jsonObject.getString("月");
			String day = jsonObject.getString("日");
			String sequrence = jsonObject.getString("时次");
			String valid = jsonObject.getString("时效");
			String level = jsonObject.getString("层次");
			
			if (Integer.valueOf(valid) - 12 < 0)
			{
				return null;
			}
			
			//经度和维度方向正好相反
			double dx = jsonObject.getDouble("经度格距");
			double dy = jsonObject.getDouble("纬度格距");
			
			String forcastDate = year + month + day + sequrence + "0000";
			Date forcastDate1 = ConvertDate.getDate(forcastDate, "yyyyMMddHHmmss", 12);
			String forcastDate2 = ConvertDate.dateToString(forcastDate1, "yyyyMMddHHmmss");
			
//			long time = ConvertDate.getDateToHours(forcastDate, "yyyyMMddHHmmss", Integer.parseInt(valid), "19000101000000");
			long time = ConvertDate.getDateToHours(forcastDate2, "yyyyMMddHHmmss", Integer.parseInt(valid), "19000101000000");
			
			double startLon = jsonObject.getDouble("起始经度");
//			double endLon = jsonObject.getDouble("终止经度");
//			double startLat = jsonObject.getDouble("起始纬度");
			double endLat = jsonObject.getDouble("终止纬度");
			
			String sequrence_tmp = "08".equals(sequrence) ? "20" :"08"; 
 //			uniObject.setStringValue("forcastDate", forcastDate);
			uniObject.setStringValue("forcastDate", forcastDate2);
			uniObject.setLongValue("time", time);
			uniObject.setStringValue("level", level);
			uniObject.setStringValue("sequrence", sequrence_tmp);
			uniObject.setIntegerValue("valid_", Integer.parseInt(valid)-12);
			uniObject.setDoubleValue("dx", dx);
			uniObject.setDoubleValue("dy", Math.abs(dy));
			uniObject.setValue("validtime", Integer.parseInt(valid)-12);
			

			int latCount = jsonObject.getInt("经向格点数");
			int lonCount = jsonObject.getInt("纬向格点数");
			
			List<Double> lats = new ArrayList<Double>();
			double tmpStartLat = endLat;
//			double tmpStartLat = startLat;
			for (int i = 0; i < latCount; i++)
			{
				lats.add(tmpStartLat);
				tmpStartLat = tmpStartLat + Math.abs(dy);
			}
			
			List<Double> lons = new ArrayList<Double>();
			double tmpStartLon = startLon;
			for (int i = 0; i < lonCount; i++)
			{
				lons.add(tmpStartLon);
				tmpStartLon = tmpStartLon + dx;
			}
			/**
			 * 配置文件
			 */
			int latStartIndex = getIndex(lats , uniObject.getDoubleValue("startLat"));
			int latEndIndex = getIndex(lats , uniObject.getDoubleValue("endLat"));
			int lonStartIndex = getIndex(lons , uniObject.getDoubleValue("startLon"));
			int lonEndIndex = getIndex(lons , uniObject.getDoubleValue("endLon"));
			
			/**
			 * 裁切之后的经纬度格点数
			 */
			int lonLength = (int) (((uniObject.getDoubleValue("endLon") - uniObject.getDoubleValue("startLon"))/dx) + 1);
			int latLength = (int) (((uniObject.getDoubleValue("endLat") - uniObject.getDoubleValue("startLat"))/Math.abs(dy)) + 1);
			double[][] data = new double[latLength][lonLength];
			
			Scanline sl = new Scanline(pdr.GetValueType(), pdr.GetHeight());
			int lat = 0 ;
			for (int i = 0; i < pdr.GetHeight(); i++)
			{
				if (i<= latEndIndex && i>= latStartIndex)
				{
					int lon = 0;
					pdr.GetScanline(0, i, sl);//根据行列获取扫描线
					for (int j = 0; j < pdr.GetWidth(); j++)
					{
						if(j >= lonStartIndex && j <= lonEndIndex)//判断是否在范围
						{
							double v = sl.GetValue(j);
							data[lat][lon] = v;
							lon++ ;
						}
					}
					lat++;
				}
			}
			uniObject.setValue("data", data);
		}
		catch (Exception e)
		{
			this.logger.error("ZJ:Analytical " + uniObject.getStringValue("filePath") + " Micapse ERROR,ERROR : " + e);
		}
		finally
		{
			if (pdr != null)
			{
				pdr.Close();
			}
			if (pws != null)
			{
				pws.Destroy();
			}
		}
		return uniObject;
	}
	
//	private int getDimTime(Date date, int hour, int valid) {
//		long since = ConvertDate.stringToDate("1900-01-01", "yyyy-MM-dd").getTime();
//		long forecastTime = date.getTime() + TimeUnit.HOURS.toMillis(hour + valid);
//		Long dimeTime = TimeUnit.MILLISECONDS.toHours(forecastTime - since);
//		return dimeTime.intValue();
//	}
	
	/**
	 * 
	 * @Description: 根据目标值返回下标
	 * @return int
	 * @throws
	 */
	private static int getIndex(List<Double> list, double d)
	{
		int index = 0;
		for (int i = 0; i < list.size(); i++)
		{
			if (d == list.get(i))
			{
				index = i ;
			}
		}
		return index;
	}
	
}
