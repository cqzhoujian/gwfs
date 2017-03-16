package com.supermap.gwfs.rainproduct.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**  
 * @Description: 日期转换
 * @author zhoujian
 * @date 2016-11-25
 * @version V1.0 
 */
public class ConvertDate
{
	/**
	 * 
	 * @Description: 指定格式字符串 转换成的 日期
	 * @param dateString 被转换的日期字符串
	 * @param template 格式
	 * @return Date
	 * @throws
	 */
	public static Date stringToDate(String dateString , String template)
	{
		Date date = null;
		try
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat(template);
			date = dateFormat.parse(dateString);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return date;
	}
	/**
	 * 
	 * @Description: 日期转换成指定的字符串
	 * @param date 日期
	 * @param template 格式
	 * @return String
	 * @throws
	 */
	public static String dateToString(Date date , String template)
	{
		String dateString = null;
		try
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat(template);
			dateString = dateFormat.format(date);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return dateString;
	}
	
	/**
	 * 
	 * @Description: 目标日期字符串加上valid小时数之后重新生成的日期
	 * @param dateString  日期字符串
	 * @param template	根式
	 * @param valid	时效
	 * @return Date
	 * @throws
	 */
	public static Date getDate(String dateString , String template , int valid)
	{
		Date date = null;
		try
		{
			Calendar afterTime = Calendar.getInstance(); 
		    Date nowDate = stringToDate(dateString,template);
		    afterTime.setTime(nowDate);
		    
		    afterTime.add(Calendar.HOUR, valid);
		    
		    date = (Date) afterTime.getTime();
		    
//		    System.out.println(dateToString(date, "yyyy-MM-dd HH:mm:ss"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return date;
	}
	/**
	 * 
	 * @Description: 目标日期加某小时后 于 原始的日期 相差小时数
	 * @param datasString 目标日期字符串
	 * @param template 日期格式
	 * @param valid 小时数
	 * @param origindateString 原始日期字符串
	 * @return long
	 * @throws
	 */
	public static long getDateToHours(String datasString , String template , int valid , String origindateString)
	{
		long hours = 0 ; 
		try
		{
			Date startDate = getDate(datasString, template, valid);
			Date endDate = stringToDate(origindateString, template);
			hours = getDateToHours(startDate , endDate);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return hours;
	}
	
	/**
	 * 
	 * @Description: 目标日期加某小时后 于 原始的日期 相差小时数
	 * @param datasString 目标日期字符串
	 * @param template 日期格式
	 * @param origindateString 原始日期字符串
	 * @return long
	 * @throws
	 */
	public static long getDateToHours(String datasString , String template , String origindateString)
	{
		long hours = 0 ; 
		try
		{
			Date startDate = stringToDate(datasString, template);;
			Date endDate = stringToDate(origindateString, template);
			hours = getDateToHours(startDate , endDate);
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return hours;
	}
	/**
	 * 
	 * @Description: 两个日期相差小时数
	 * @return long
	 * @throws
	 */
	private static long getDateToHours(Date startDate, Date endDate)
	{
		long hours = 0 ; 
		try
		{
			long millisecond = getDateToMillisecond(startDate , endDate);
			hours = millisecond / 1000 / 60 /60;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return hours;
	}
	
	/**
	 * 
	 * @Description: 两个日期之间相差毫秒数
	 * @return long
	 * @throws
	 */
	private static long getDateToMillisecond(Date startDate, Date endDate)
	{
		return startDate.getTime() - endDate.getTime();
	}
	
	/**
	 * 
	 * @Description: 把原始格式的日期型字符串   转换成    目标格式的日期字符串
	 * @param forcastDate	日期字符串
	 * @param originFormat	原始格式
	 * @param targetFormat	目标格式
	 * @return Sting		转换后的目标日期字符串
	 * @throws
	 */
	public static String stringToString(String forcastDate, String originFormat, String targetFormat)
	{
		String targetDate = null;
		try
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat(originFormat);
			Date date = dateFormat.parse(forcastDate);
			SimpleDateFormat dateFormat2 = new SimpleDateFormat(targetFormat);
			targetDate = dateFormat2.format(date);
			
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		return targetDate;
	}
	
//	public static void main(String[] args)
//	{
//		String ss = stringToString("20161123", "yyyyMMdd", "yyyy-MM-dd");
//		System.out.println(ss);
//	}
}
