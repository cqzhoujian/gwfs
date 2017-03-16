package com.supermap.gwfs.produce;

import java.awt.geom.Point2D;
import java.io.File;

import com.mg.objects.Map;
import com.mg.objects.Workspace;
import com.supermap.commons.logging.Logger;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.produce.util.ReadXml;

/**  
 * @Description: TODO
 * @author zhoujian
 * @date 2017-3-16
 * @version V1.0 
 */
public abstract class ProductSuper extends Thread
{
	//数据类型(卫星、雷达...)
	private String type = null;
	private Logger logger =  null;
	private byte[] lock = new byte[1];
	
	public void setType(String type)
	{
		this.type = type;
	}

	public void setLogger(Logger logger)
	{
		this.logger = logger;
	}

	protected static UniObject uniObject = null;
	static
	{
		//读取配置文件获取产品参数
		uniObject = ReadXml.getInstance().getParameter();
	}
	
	@Override
	public void run()
	{
		startProduct();
	}

	private void startProduct()
	{
		synchronized (lock)
		{
			//获取某一产品参数
			UniObject param = getProductParam(uniObject);
			if (param == null)
			{
				logger.error("ZJ: Get "+ type +" product parameters are null.");
				return;
			}
			//产品分辨率
			int[] img_resolution = null;
			
			try
			{
				img_resolution = this.getResolution(param);
			}
			catch (Exception e)
			{
				logger.error("ZJ: " + type +" product image resolution exception , exception : " + e);
			}
			/** 工作空间是用户在同一个工程中（或者是一个事务）工作环境的集合，包括数据源、地图、布局、符号等。
			 *  单个工作空间对象不支持多线程并发访问；
			 *  如果需要多线程并发访问，应该一个线程单独分配一个工作空间对象。
			 */
			
			Workspace ws = new Workspace();
			//创建地图
			Point2D ptViewport = new Point2D.Double(img_resolution[0], img_resolution[1]); 
			Map map = new Map();
			ws.InsertMap(map, -1);
			map.SetName("test");
			map.SetSize((int)ptViewport.getX(), (int)ptViewport.getY()); //设备尺寸
			//设置地图投影方式
			map.SetProjection(param.getStringValue("projection"));
			/**
			 * 解析数据
			 */
			boolean flag = analyzeData(ws, map , param);
			
			if (flag)
			{
				//得到文件名
				String fileName = this.getFileName(param);
				//创建产品输出目录
				String dirs = param.getStringValue("out_path").replace("\\", "/");
				try
				{
					flag = this.createDirs(dirs);
				}
				catch (Exception e)
				{
					logger.error("ZJ: Create " + type + " image product folder exception , exception : " + e);
				}
				if (flag)
				{
					//绘制图片
					flag = draw(ws, map, ptViewport , param , (dirs + "/" + fileName).replace("/", "\\\\"));
					String status = flag==true?"successful!":"failed!";
					logger.debug("ZJ: " + type + " product " + fileName + " , is " + status);
				}
			}
		}
		
	}
	
	/**
	 * 
	 * @Description: 获取某一数据的产品参数
	 * @return UniObject
	 * @throws
	 */
	public abstract UniObject getProductParam(UniObject productParam);

	/**
	 * 
	 * @Description: 解析产品数据
	 * @return boolean
	 * @throws
	 */
	public abstract boolean analyzeData(Workspace ws, Map map ,UniObject param);
	
	/**
	 * 
	 * @Description: 产品名称
	 * @return String
	 * @throws
	 */
	public abstract String getFileName(UniObject param);

	/**
	 * 
	 * @Description: 绘制产品图片
	 * @return boolean
	 * @throws
	 */
	public abstract boolean draw(Workspace ws, Map map, Point2D ptViewport, UniObject param, String filePath);



	/**
	 * 
	 * @Description: 根据创建文件夹
	 * @return boolean
	 * @throws
	 */
	protected boolean createDirs(String dirs) throws Exception
	{
		boolean flag = true;
		try
		{
			File dir = new File(dirs);
			if (!dir.exists())
			{
				flag = dir.mkdirs();
			}
		}
		catch (Exception e)
		{
			flag = false;
		}
		
		return flag;
	}
	/**
	 * 
	 * @Description: 从xml中获取到产品图片分辨率
	 * @return int[]
	 * @throws
	 */
	protected int[] getResolution(UniObject param) throws Exception
	{
		int [] resolution = new int[2];
		try
		{
			String [] resolu = param.getStringValue("img_resolution").split(",");
			resolution[0] = Integer.valueOf(resolu[0]);
			resolution[1] = Integer.valueOf(resolu[1]);
		}
		catch (Exception e)
		{
			//异常之后给产品图片默认大小
			resolution[0] = 1024;
			resolution[1] = 768;
		}
		
		return resolution;
	}
	
	/**
	 * 
	 * @Description: 获取数据源参数
	 * @return String
	 * @throws
	 */
	protected String getDatasourceParameter(String type, String alias, String server) {
		String string = String.format("{\"Type\":\"%s\",\"Alias\":\"%s\",\"Server\":\"%s\"}", type, alias, server.replace("\\", "/"));
		return string;
	}
}
