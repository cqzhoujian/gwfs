package com.supermap.gwfs.synchronizer.micaps4grid.dao;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.supermap.commons.logging.Logger;
import com.supermap.commons.logging.factory.LoggerFactory;
import com.supermap.gcpp.core.common.UniObject;
import com.supermap.gwfs.db.helper.JDBCHelper;
import com.supermap.gwfs.synchronizer.micaps4grid.util.ConvertDate;

/**  
 * @Description: 数据持久
 * @author zhoujian
 * @date 2016-10-30
 * @version V1.0 
 */
public class Grid_forcastDao
{
	private Logger logger = LoggerFactory.getLogger("DBMicaps");
	//查询要入库的字段
	private final String FIND_GRID_FORCAST_VARIABLES = " SELECT DISTINCT o.id AS origin_id,o.name AS origin_name,o.val AS  origin_value,"
													+"e.id AS element_id,e.name AS element_name,e.val AS  element_value, "
													+"l.id AS forecast_level_id,l.name AS forecast_level_name,l.val AS forecast_level_value " 
													+"FROM  t_forecast_origin o , t_forecast_element e ,t_forecast_level l "
													+"WHERE o.id=e.origin_id  "
													+"AND e.id=l.element_id  " 
													+"AND  o.val=? "
													+"AND e.val=?";
	//查询时次所有信息
	private final String FIND_GRID_FORCAST_SEQUNCE = "SELECT DISTINCT s.id AS sequnce_id ,s.name AS sequnce_name,s.val AS squnce_val "
													+"FROM t_forecast_origin o ,t_forecast_sequence s "
													+"WHERE o.id=s.origin_id "
													+"AND o.val=?";
	
	//查询时效所有信息
	private final String FIND_GRID_FORCAST_VALID = "SELECT DISTINCT v.id AS valid_id ,v.name AS valid_name,v.val AS valid_val "
												   +"FROM t_forecast_element e ,t_forecast_valid v "
												   +"WHERE e.id=v.element_id "
												   +"AND e.val=?"
												   +"AND e.origin_id = ?";
	
	//查询层次所有信息
	private final String FIND_GRID_FORCAST_LEVEL = "SELECT DISTINCT l.id AS valid_id ,l.name AS valid_name,l.val AS valid_val "
												   +"FROM t_forecast_element e ,t_forecast_level l "
												   +"WHERE e.id=l.element_id "
												   +"AND e.val=?"
												   +"AND e.origin_id = ?";
	
	

	private final String FIND_GRID_FORCAST_SERVERID	= "SELECT id from t_server where ip = ?";
	
	private final String INSERT = "INSERT INTO  t_grid_forecast(id,origin_id,origin_name,origin_value,element_id,element_name,element_value,forecast_date,forecast_sequence_id,forecast_sequence_name,forecast_sequence_value,forecast_level_id,forecast_level_name,forecast_level_value,forecast_valid_id,forecast_valid_name,forecast_valid_value,server_id,forecast_filename,forecast_fileversion,forecast_filepath,create_time) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)	";
	
	/**
	 * 
	 * @Description: 查询grid_forcast信息
	 * @return UniObject
	 * @throws
	 */
	public UniObject findGridForcast(String origin_value,String element_value)
	{
		Connection conn = null;
		PreparedStatement smt = null;
		ResultSet rs = null;
		UniObject uniObject = null;
		try
		{
			uniObject = new UniObject();
			conn = JDBCHelper.getConnection();
			smt = conn.prepareStatement(FIND_GRID_FORCAST_VARIABLES);
			smt.setString(1, origin_value);
			smt.setString(2, element_value);
			rs = smt.executeQuery();
			while(rs.next())
			{
				String origin_id = rs.getString("origin_id");
				String origin_name = rs.getString("origin_name");
				String origin_value1 = rs.getString("origin_value");
				String element_id = rs.getString("element_id");
				String element_name = rs.getString("element_name");
				String element_value1 = rs.getString("element_value");
//				String forecast_level_id = rs.getString("forecast_level_id");
//				String forecast_level_name = rs.getString("forecast_level_name");
//				String forecast_level_value = rs.getString("forecast_level_value");
				uniObject.setStringValue("origin_id", origin_id);
				uniObject.setStringValue("origin_name", origin_name);
				uniObject.setStringValue("origin_value", origin_value1);
				uniObject.setStringValue("element_id", element_id);
				uniObject.setStringValue("element_name", element_name);
				uniObject.setStringValue("element_value", element_value1);
//				uniObject.setStringValue("forecast_level_id", forecast_level_id);
//				uniObject.setStringValue("forecast_level_name", forecast_level_name);
//				uniObject.setStringValue("forecast_level_value", forecast_level_value);
				
//				System.out.print("o_id"+ "\t" + "o_name"+ "\t" + "o_value" + "\t" + "e_id\t"+"e_name\t"+"e_value\t"+"lvl_id\t"+"lvl_name\t"+"lvl_value");
//				System.out.println();
//				System.out.print(origin_id+ "\t" + origin_name+ "\t" + origin_value1 + "\t" + element_id+ "\t"+element_name  + "\t" +element_value1+ "\t"+forecast_level_id+ "\t"+forecast_level_name+ "\t"+forecast_level_value);
			}
		}
		catch (Exception e)
		{
			logger.error("ZJ:query grid_forcast data error , error : " + e);
		}
		finally
		{
			JDBCHelper.free(rs, smt, conn);
		}
		return uniObject;
	}
	/**
	 * 
	 * @Description: 获取初始场下所有时次信息 <id,<val,name>>
	 * @return UniObject
	 * @throws
	 */
	public UniObject findGridForcastSequnce(UniObject uniObject , String origin_val)
	{
		Connection conn = null;
		PreparedStatement smt = null;
		ResultSet rs = null;
		try
		{
			conn = JDBCHelper.getConnection();
			smt = conn.prepareStatement(FIND_GRID_FORCAST_SEQUNCE);
			smt.setString(1, origin_val);
			rs = smt.executeQuery();
			Map<String , Map<String, String>> sequnceMap = new HashMap<String, Map<String,String>>();
			while(rs.next())
			{
				Map< String, String> tmpMap = new HashMap<String, String>();
				String sequnce_id = rs.getString("sequnce_id");
				String sequnce_name = rs.getString("sequnce_name");
				String sequnce_val = rs.getString("squnce_val");
				tmpMap.put(sequnce_val, sequnce_name);
				sequnceMap.put(sequnce_id, tmpMap);
				
//				System.out.print("o_id"+ "\t" + "o_name"+ "\t" + "o_value" + "\t" + "e_id\t"+"e_name\t"+"e_value\t"+"lvl_id\t"+"lvl_name\t"+"lvl_value");
//				System.out.println();
//				System.out.print(origin_id+ "\t" + origin_name+ "\t" + origin_value1 + "\t" + element_id+ "\t"+element_name  + "\t" +element_value1+ "\t"+forecast_level_id+ "\t"+forecast_level_name+ "\t"+forecast_level_value);
			}
			uniObject.setValue("sequnce", sequnceMap);
		}
		catch (Exception e)
		{
			logger.error("ZJ:query grid_forcast of sequnce is error ， error : " + e);
		}
		finally
		{
			JDBCHelper.free(rs, smt, conn);
		}
		return uniObject;
	}
	/**
	 * 
	 * @Description: 获取要素下所有时效的信息
	 * @return UniObject
	 * @throws
	 */
	public UniObject findGridForcastValid(UniObject uniObject , String element_val , int origin_id)
	{
		Connection conn = null;
		PreparedStatement smt = null;
		ResultSet rs = null;
		try
		{
			conn = JDBCHelper.getConnection();
			smt = conn.prepareStatement(FIND_GRID_FORCAST_VALID);
			smt.setString(1, element_val);
			smt.setInt(2, origin_id);
			rs = smt.executeQuery();
			Map<String , Map<String, String>> validMap = new HashMap<String, Map<String,String>>();
			while(rs.next())
			{
				Map< String, String> tmpMap = new HashMap<String, String>();
				String valid_id = rs.getString("valid_id");
				String valid_name = rs.getString("valid_name");
				String valid_val = rs.getString("valid_val");
				tmpMap.put(valid_val, valid_name);
				validMap.put(valid_id, tmpMap);
				
//				System.out.print("o_id"+ "\t" + "o_name"+ "\t" + "o_value" + "\t" + "e_id\t"+"e_name\t"+"e_value\t"+"lvl_id\t"+"lvl_name\t"+"lvl_value");
//				System.out.println();
//				System.out.print(origin_id+ "\t" + origin_name+ "\t" + origin_value1 + "\t" + element_id+ "\t"+element_name  + "\t" +element_value1+ "\t"+forecast_level_id+ "\t"+forecast_level_name+ "\t"+forecast_level_value);
			}
			uniObject.setValue("valid", validMap);
		}
		catch (Exception e)
		{
			logger.error("ZJ:query grid_forcast of valid error ,error : " + e);
		}
		finally
		{
			JDBCHelper.free(rs, smt, conn);
		}
		return uniObject;
	}
	/**
	 * 
	 * @Description: 保存
	 * @return void
	 * @throws
	 */
	public int saveGridForcast(UniObject uniObject)
	{
		Connection conn = null;
		PreparedStatement smt = null;
		ResultSet rs = null;
		int result = 0;
		try
		{
			conn = JDBCHelper.getConnection();
			smt = conn.prepareStatement(INSERT);
			smt.setInt(1, 0);
			smt.setInt(2, uniObject.getIntegerValue("origin_id"));
			smt.setString(3, uniObject.getStringValue("origin_name"));
			smt.setString(4, uniObject.getStringValue("origin_value"));
			smt.setInt(5, uniObject.getIntegerValue("element_id"));
			smt.setString(6, uniObject.getStringValue("element_name"));
			smt.setString(7, uniObject.getStringValue("element_value"));
			SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd");
			smt.setDate(8, new java.sql.Date(simple.parse(uniObject.getStringValue("forecast_date")).getTime()));
			
			smt.setInt(9, uniObject.getIntegerValue("sequnce_id"));
			smt.setString(10,uniObject.getStringValue("sequnce_name"));
			smt.setString(11, uniObject.getStringValue("sequnce_value"));
			
			smt.setInt(12, uniObject.getIntegerValue("level_id"));
			smt.setString(13, uniObject.getStringValue("level_name"));
			smt.setString(14, uniObject.getStringValue("level_value"));
			
			smt.setInt(15, uniObject.getIntegerValue("valid_id"));
			smt.setString(16,uniObject.getStringValue("valid_name"));
			smt.setInt(17, uniObject.getIntegerValue("valid_value"));
			
			smt.setInt(18, uniObject.getIntegerValue("serverId"));
			smt.setString(19, uniObject.getStringValue("fileName"));
			smt.setString(20, uniObject.getStringValue("fileversion"));
			smt.setString(21, uniObject.getStringValue("filePath"));
			
			smt.setString(22, ConvertDate.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss"));
			logger.info("ZJ:executeUpdate \n" + smt.toString().substring(smt.toString().indexOf(":") + 1));
			result = smt.executeUpdate();
			logger.info("ZJ:save status ： " + result);
		}
		catch (Exception e)
		{
			logger.error("ZJ:save grid_forcast data error , error : " + e);
		}
		finally
		{
			JDBCHelper.free(rs, smt, conn);
		}
		return result;
	}
	/**
	 * 
	 * @Description: 查询serverId
	 * @return UniObject
	 * @throws
	 */
	public UniObject findGridForcastServer(UniObject uniObject)
	{
		Connection conn = null;
		PreparedStatement smt = null;
		ResultSet rs = null;
		try
		{
			InetAddress inet = InetAddress.getLocalHost();
			String ip = inet.getHostAddress();
			if ("127.0.0.1".equals(ip)) {
				ip = "172.24.176.169";
			}
			conn = JDBCHelper.getConnection();
			smt = conn.prepareStatement(FIND_GRID_FORCAST_SERVERID);
			smt.setString(1, ip);
			rs = smt.executeQuery();
			int id = -1;
			while (rs.next())
			{
				id = rs.getInt("id");
			}
			//默认主机
			if(id == -1)
				id = 1;
			uniObject.setIntegerValue("serverId", id);
		}
		catch (UnknownHostException e)
		{
			logger.error("ZJ:get hostlocal IP error , error : " + e);
		}
		catch (SQLException e)
		{
			logger.error("ZJ:query serverId error , error : " + e);
		}
		finally
		{
			JDBCHelper.free(rs, smt, conn);
		}
		return uniObject;
	}
	/**
	 * 
	 * @Description: 获取初始场下要素的层次信息
	 * @return UniObject
	 * @throws
	 */
	public UniObject findGridForcastLevel(UniObject uniObject, String element_val, int origin_id)
	{
		Connection conn = null;
		PreparedStatement smt = null;
		ResultSet rs = null;
		try
		{
			conn = JDBCHelper.getConnection();
			smt = conn.prepareStatement(FIND_GRID_FORCAST_LEVEL);
			smt.setString(1, element_val);
			smt.setInt(2, origin_id);
			rs = smt.executeQuery();
			//<层次Id , <层次value , 层次 Name>>
			Map<String , Map<String, String>> levelMap = new HashMap<String, Map<String,String>>();
			while(rs.next())
			{
				Map< String, String> tmpMap = new HashMap<String, String>();
				String valid_id = rs.getString("valid_id");
				String valid_name = rs.getString("valid_name");
				String valid_val = rs.getString("valid_val");
				tmpMap.put(valid_val, valid_name);
				levelMap.put(valid_id, tmpMap);
				
			}
			uniObject.setValue("level", levelMap);
		}
		catch (Exception e)
		{
			logger.error("ZJ:query grid_forcast of level error , error : " + e);
		}
		finally
		{
			JDBCHelper.free(rs, smt, conn);
		}
		return uniObject;
	}
}
