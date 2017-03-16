package com.supermap.gwfs.rainproduct.probability;

import java.awt.geom.Point2D;
import java.util.LinkedHashMap;

import bilinear.Bilinear;

import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;

/**  
 * @Description: 插值(分辨率为0.5--->分辨率0.025)
 * @author zhoujian
 * @date 2016-10-28
 * @version V1.0 
 */
public class Interpolation
{
	private Object object = new Object();
	private Logger logger = LoggerFactory.getLogger("InterPolation");
	private Bilinear bilinear = null;
	private static Interpolation interpolation = null;
	
	private Interpolation()
	{
		try
		{
			bilinear = new Bilinear();
		}
		catch (MWException e)
		{
			logger.error("创建Bilinear对象异常 ， 异常 : " + e);
		}
	}
	
	public static Interpolation getInstance()
	{
		if (interpolation == null)
		{
			interpolation = new Interpolation();
		}
		return interpolation;
		
	}
	
	
	public UniObject bilinearInter(UniObject uniObject, LinkedHashMap<Point2D, Double> rainProductMap)
	{
		Object[] out = null;
		MWNumericArray xi = null;
		MWNumericArray yi = null;
		MWNumericArray zi = null;
		try
		{
			synchronized (object)
			{
				//拼接数据
				String[] size = uniObject.getStringValue("size").split(",");
				float startlon = Float.parseFloat(size[0]),endlon = Float.parseFloat(size[1]);
				float startlat = Float.parseFloat(size[2]),endlat = Float.parseFloat(size[3]);
				float dx = uniObject.getFloatValue("dx"), dy = uniObject.getFloatValue("dy");
				int w = (int)((endlon - startlon) / dx ) + 1 , h = (int)((endlat - startlat) / dy ) + 1;
				double[][] data = new double[h][w];
				for (int lat = 0; lat < h; lat++)
				{
					for (int lon = 0; lon < w; lon++)
					{
						Point2D point = new Point2D.Float(lon,lat);
						data[lat][lon] = rainProductMap.get(point);
					}
				}
				
				bilinear = new Bilinear();
				out = bilinear.bilinear(3,startlon,dx,endlon,startlat,dy,endlat,0.025, data);
				xi = (MWNumericArray)out[0];
				yi = (MWNumericArray)out[1];
				zi = (MWNumericArray)out[2];
				int ww = (int)((endlon - startlon) / 0.025) + 1;//宽度
				int hh = (int)((endlat - startlat) / 0.025) + 1;//高度
				uniObject.setIntegerValue("w", ww);//651
				uniObject.setIntegerValue("h", hh);//401
				double[][] X = getBilinearData(xi,ww,hh);
				double[][] Y = getBilinearData(yi,ww,hh);
				double[][] Z = getBilinearData(zi,ww,hh);
				uniObject.setValue("X", X);
				uniObject.setValue("Y", Y);
				uniObject.setValue("Z", Z);
				
			}
		}
		catch (Exception e)
		{
			logger.error("双线性插值异常 , 异常 ： " + e);
		}
		finally
		{
			
		}
		return uniObject.clone();
	}
	/**
	 * 
	 * @Description: matlab返回数据
	 * @return double[][]
	 * @throws
	 */
	private double[][] getBilinearData(MWNumericArray xyz , int ww ,int hh)
	{
		double[][] xxx =  new double[hh][ww];
		try
		{
			double[] xx =  xyz.getDoubleData();
			for (int j = 0; j < ww; j++)
			{
				for (int i = 0; i < hh; i++)
				{
					xxx[i][j] = xx[i + j * hh];
				}
			}
		}
		catch (Exception e)
		{
			logger.error("数据转换异常 , 异常 : " + e);
		}
		return xxx;
	}

}
