package com.supermap.gwfs.rainproduct.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**  
 * @Description: 集合排序
 * @author zhoujian
 * @date 2016-10-20
 * @version V1.0 
 */
public class CollectionUtil
{
	/**
	 * 
	 * @Description: Map集合按照升序排序
	 * @return LinkedHashMap<K,V>
	 * @throws
	 */
	 public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortByValue( Map<K, V> map )  
	 {  
	     List<Map.Entry<K, V>> list =  new LinkedList<Map.Entry<K, V>>( map.entrySet() );  
	     Collections.sort( list, new Comparator<Map.Entry<K, V>>()  
	     {  
	         public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )  
	         {  
	             return (o1.getValue()).compareTo( o2.getValue() );  
	         }  
	     } );  
	
	     LinkedHashMap<K, V> result = new LinkedHashMap<K, V>();  
	     for (Map.Entry<K, V> entry : list)  
	     {  
	         result.put( entry.getKey(), entry.getValue() );  
	     }  
	     return result;  
	 }

//	 public static void main(String[] args)
//	{
//		 Map<Point2D, Float> map = new HashMap<Point2D, Float>();
//		 Point2D point1 = new Point2D.Float();
//		 point1.setLocation(1.0, 1.0);
//		 float value1 = 11.0f;
//		 map.put(point1, value1);
//		 
//		 Point2D point2 = new Point2D.Float();
//		 point2.setLocation(2.0, 1.0);
//		 float value2 = 10.0f;
//		 map.put(point2, value2);
//		 
//		 Point2D point3 = new Point2D.Float();
//		 point3.setLocation(11.0, 2.0);
//		 float value3 = 35.0f;
//		 map.put(point3, value3);
//		 
//		 Point2D point4 = new Point2D.Float();
//		 point4.setLocation(3.0,8.0);
//		 float value4 = 16.0f;
//		 map.put(point4, value4);
//		 
//		 Point2D point5 = new Point2D.Float();
//		 point5.setLocation(9.0, 5.0);
//		 float value5 = 51.0f;
//		 map.put(point5, value5);
//		 
//		 Point2D point6 = new Point2D.Float();
//		 point6.setLocation(7.5, 1.0);
//		 float value6 = 33.0f;
//		 map.put(point6, value6);
//		 System.out.println("排序之前：" + map);
//		 map = sortByValue(map);
//		 System.out.println("排序之后：" + map);
//	}
}
