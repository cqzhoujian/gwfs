package com.supermap.gwfs.synchronizer.micaps4grid.interpolation;

import javax.sound.midi.MidiDevice.Info;

import bilinear.Bilinear;

import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;

/**  
 * @Description: 插值成0.025分辨率
 * @author zhoujian
 * @date 2016-11-29
 * @version V1.0 
 */
public class Interpolation
{
	private static Logger logger = LoggerFactory.getLogger("Interpolation");
	private static Interpolation interpolation = null;
	private static Bilinear bilinear = null;
	private static Object object = new Object();
	
	private Interpolation()
	{
		try
		{
			bilinear = new Bilinear();
		}
		catch (MWException e)
		{
			logger.error("创建Bilinear对象异常 , 异常 : " + e);
		}
	}
	
	public static synchronized Interpolation getInstance()
	{
		try
		{
			if (interpolation == null)
			{
				interpolation = new Interpolation();
			}
		}
		catch (Exception e)
		{
			logger.error("异常 , e : "+ e);
		}
		return interpolation;
	}
	/**
	 * 
	 * @Description: 插值
	 * @param resolution 插值的分辨率
	 * @return UniObject
	 * @throws
	 */
	public  UniObject bilinearInterpolation(UniObject uniObject , double resolution)
	{
		Object[] out = null;
		MWNumericArray xi = null;
		MWNumericArray yi = null;
		MWNumericArray zi = null;
		try
		{
			synchronized (object)
			{
				double startlon = uniObject.getDoubleValue("startLon");
				double endlon = uniObject.getDoubleValue("endLon");
				double startlat = uniObject.getDoubleValue("startLat");
				double endlat = uniObject.getDoubleValue("endLat");
				double dx = uniObject.getDoubleValue("dx");
				double dy = uniObject.getDoubleValue("dy");
				double[][] data = (double[][])uniObject.getValue("data");
				out = bilinear.bilinear(3,startlon,dx,endlon,startlat,dy,endlat,resolution, data);
				xi = (MWNumericArray)out[0];
				yi = (MWNumericArray)out[1];
				zi = (MWNumericArray)out[2];
				int ww = (int)((endlon - startlon) / resolution) + 1;//宽度(经度格点数)
				int hh = (int)((endlat - startlat) / resolution) + 1;//高度(维度格点数)
				uniObject.setIntegerValue("lonCount", ww);//651
				uniObject.setIntegerValue("latCount", hh);//401
				double[][] X = getBilinearData(xi,ww,hh);
				double[][] Y = getBilinearData(yi,ww,hh);
				double[][] Z = getBilinearData(zi,ww,hh);
				uniObject.setValue("X", X);
				uniObject.setValue("Y", Y);
				uniObject.setValue("Z", Z);
				uniObject.setValue("Z_", Z);
			}
		}
		catch (Exception e)
		{
			logger.error("ZJ:Interpolation data error , error : " + e);
		}
		
		return uniObject;
	}

	private double[][] getBilinearData(MWNumericArray xyz, int ww, int hh)
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
			logger.error("ZJ:MathLab return data Convert error , error : " + e);
		}
		return xxx;
	}
}
