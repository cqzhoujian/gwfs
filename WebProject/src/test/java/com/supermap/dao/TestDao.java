package com.supermap.dao;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Resource;


import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.supermap.entity.UserInfo;
import com.supermap.service.UserService;

/**  
 * @Description: TODO
 * @author zhoujian
 * @date 2017-3-3
 * @version V1.0 
 */
@RunWith(SpringJUnit4ClassRunner.class) // 表示继承了SpringJUnit4ClassRunner类
@ContextConfiguration(locations = { "classpath:spring-mybatis.xml" })
public class TestDao
{
	@Resource
	private UserService userService ; 
//	SqlSessionFactory sqlSessionFactory;
    /* @Before
     public void initFactory() throws IOException
     {
         String resource = "MapConfig.xml";
 
         InputStream inputStream = Resources.getResourceAsStream(resource);
         sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
     }*/
     
    /* @Test
     public void testQueryOne()
     {
         SqlSession session=sqlSessionFactory.openSession();
         UserInfo user=session.selectOne("com.supermap.mapping.selectOne", 1);
         System.out.println(user.toString());
     }*/
	
	@Test
	public void testSelect()
	{
		UserInfo user = userService.select(5);
		System.out.println(user.toString());
	}
     
}
