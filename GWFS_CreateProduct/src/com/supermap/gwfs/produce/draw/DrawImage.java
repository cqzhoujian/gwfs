package com.supermap.gwfs.produce.draw;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.codehaus.jettison.json.JSONObject;

import com.mg.objects.Dataset;
import com.mg.objects.DatasetVector;
import com.mg.objects.Datasource;
import com.mg.objects.GeoText;
import com.mg.objects.Geometry;
import com.mg.objects.Graphics;
import com.mg.objects.Image;
import com.mg.objects.Layer;
import com.mg.objects.Layout;
import com.mg.objects.Map;
import com.mg.objects.Recordset;
import com.mg.objects.Workspace;
import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;

/**  
 * @Description: 绘制图片
 * @author zhoujian
 * @date 2017-3-14
 * @version V1.0 
 */
public class DrawImage
{
	private Logger logger = LoggerFactory.getLogger("DrawImage");
	private static DrawImage drawImage = null;
	private DrawImage()
	{}
	
	public static synchronized DrawImage getInstance()
	{
		if (drawImage == null)
		{
			drawImage = new DrawImage();
		}
		return drawImage;
	}
	/**
	 * 
	 * @Description: 绘制图片
	 * @param: ws			工作空间
	 * @param: map			地图
	 * @param: ptViewport	地图大小
	 * @param: strJson		数据源字符串
	 * @param: outFilePath	输出文件路径
	 * @param: province		标红的省份（null或者"" 表示没有特殊标红的省份）
	 * @return boolean
	 * @throws
	 */
	public boolean drawImage(Workspace ws , Map map ,Point2D ptViewport ,String strJson , String outFilePath , String province)
	{
		boolean result = true;
		Recordset rs = null;
		try
		{
			//叠加省级行政区划
//			String str = "{\"Type\":\"ESRI Shapefile\",\"Alias\":\"China_Province_pl\",\"Server\":\"C:/Users/LQ/Desktop/shp/China_Province_pl.shp\"}";
	        Datasource ds = ws.OpenDatasource(strJson);
	        Dataset dataset = ds.GetDataset(0);
	        
	        //地图图层--底图
	        Layer layer = Layer.CreateInstance("Region", ws);
	        layer.SetDataset(dataset);
	        layer.SetPropertyValue("LineStyle", "{\"color\":\"RGB(0,0,0)\",\"Width\":1,\"SymbolID\":0}"); //如果线宽为0，不绘制线
	        layer.SetPropertyValue("FillStyle", "{\"ForeColor\":\"RGBA(128,255,0,0)\"}");
	        map.InsertLayer(layer, -1);
	        //创建突出省市地图图层
	        if (province != null && !"".equals(province))
			{
	        	layer = Layer.CreateInstance("Region", ws);
	        	layer.SetDataset(dataset);
	        	layer.SetPropertyValue("Filter", "[NAME]='"+ province +"'");
	        	layer.SetPropertyValue("LineStyle", "{\"color\":\"RGB(255,0,0)\",\"Width\":2}");//绘制线条粗细
	        	layer.SetPropertyValue("FillStyle", "{\"ForeColor\":\"RGBA(0,0,0,0)\"}");
	        	map.InsertLayer(layer, -1);
			}
	        
	        //-----------------------------------------------------------------------------------------------------------------------------
			//创建布局
			Layout layout = new Layout();
			ws.InsertLayout(layout, -1);
			//layout.SetBackgroundStyle("{\"BackColor\":\"RGBA(0,0,0,0)\",\"ForeColor\":\"RGBA(255,255,255,0)\",\"SymbolID\":0}");
			layout.SetName("test");
			Graphics.DotToMM(ptViewport);
			Rectangle2D rc = new Rectangle2D.Double(0.0, 0.0, ptViewport.getX(), -ptViewport.getY()); //高度为负表示y向下
			layout.SetBounds(rc);
			
			//添加地图到布局
			DatasetVector dv = (DatasetVector)layout.GetDatasource().GetDataset(0);
			rs = dv.Query(null, null);
			//创建几何对象实例
			Geometry g = Geometry.CreateInstance("GeoMap", ws);
			g.SetBounds(rc);
			g.SetPropertyValue("MapName", "test");
			rs.AddNew(g);
			rs.Update();
			
			//添加图例
			g = Geometry.CreateInstance("GeoLegend", ws);
			//Alignment(基准)可取值:TopLeft,TopCenter,TopRight,MiddleLeft,MiddleCenter,MiddleRight,BottomLeft,BottomCenter,BottomRight
			g.SetPropertyValue("Alignment", "BottomLeft");
			//设置图例的原点(以左下角为原点 ,x轴上 "+" 向右 ; "-" 向左,y轴上 "+" 向下;"-" 向上。)
			g.SetOrigin(rc.getX() + 1, rc.getY() + Math.abs(rc.getHeight()) - 1);
	        g.SetPropertyValue("LegendType", "Range"); //Unique,Range,默认Unique
	
	        g.SetPropertyValue("Title", "图例");
	        g.SetPropertyValue("TitleTextStyle", "{\"FontName\":\"微软雅黑\",\"ForeColor\":\"RGB(0,0,0)\",\"FontSize\":3.5}"); //尺寸单位为毫米
	        //设置图例显示的文字以及字体大小(不设置不显示文字，文字大小会影响图例框的大小)
	        g.SetPropertyValue("SubTitle", "单位:dBZ");
	        g.SetPropertyValue("SubTitleTextStyle", "{\"FontName\":\"宋体\",\"ForeColor\":\"RGB(0,0,0)\",\"FontSize\":2.5}"); //尺寸单位为毫米
	        //图例项宽高，单位毫米
	        g.SetPropertyValue("ItemSize", "10 5"); 
	        g.SetPropertyValue("BackgroundStyle", "{\"ForeColor\":\"RGBA(255,255,255,255)\"}");
	        g.SetPropertyValue("BorderStyle", "{\"color\":\"RGB(0,0,0)\",\"Width\":1}");//设置图例边框线条
	        g.SetPropertyValue("Margin", "{\"left\":2,\"right\":2,\"top\":2,\"bottom\":1}"); //图例边距，单位毫米 图例内容与边框之间的间距
	        g.SetPropertyValue("ItemSpace", "1 0"); //图例项列行间距，单位毫米 (第一个参数是行距 ，第二个参数是列距)
	        g.SetPropertyValue("ItemTextStyle", "{\"FontName\":\"微软雅黑\",\"ForeColor\":\"RGB(0,0,0)\",\"FontSize\":3.0}"); //尺寸单位为毫米
	        g.SetPropertyValue("MaxValueCaption", "");
			rs.AddNew(g);
			rs.Update();
			//--------------------------------------------------------------------
	        //添加图例项
	        for (int i = 0; i < map.GetLayerCount(); i++)
	        {
	        	layer = map.GetLayer(i);
	            if (layer.GetType().equals("RasterRange"))
	            {
	                int nCount = layer.GetPropertyValueCount("RasterRangeItem");
	                for (int j = 0; j < nCount; j++)
	                {
	                	strJson = layer.GetPropertyValue("RasterRangeItem", j);
	                    
	                    JSONObject jsonObj = new JSONObject(strJson);
	        			
	                    strJson = String.format("{\"Type\":\"Region\",\"Caption\":\"%g\",\"FillStyle\":{\"ForeColor\":\"%s\"}}",
	        					jsonObj.getDouble("Value"), jsonObj.getString("Color"));
	                        
	                    g.AddPropertyValue("LegendItem", strJson);
	                }
	                break;
	            }
	        }
	        String strTitle = "";
	        //--------------------------------------------------------------------
			//增加标题
			if (!strTitle.isEmpty())
			{
				GeoText gt = new GeoText(strTitle);
				gt.SetOrigin(rc.getCenterX(), 5);
				gt.SetPropertyValue("TextStyle", "{\"FontName\":\"宋体\",\"Alignment\":\"TopCenter\",\"FontSize\":8}"); //尺寸单位为毫米
				rs.AddNew(gt);
				rs.Update();
			}
			//输出
			layout.Draw();
			Image image = layout.GetGraphics().GetImage();
			image.Save(outFilePath);
			
//			Runtime.geRuntime().exec("mspaint.exe " + outFilePath);
		
		}
		catch (Exception ex)
		{
			logger.error("ZJ: Draw product image exception , exception : " + ex);
			result = false;
		}
		finally
		{
			if (rs != null)
			{
				rs.Destroy();
			}
			if (ws != null)
			{
				ws.Destroy();
			}
		}
		
		return result;
	}
}
