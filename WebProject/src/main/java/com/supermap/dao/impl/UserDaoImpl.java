package com.supermap.dao.impl;

import java.util.List;

import javax.annotation.Resource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.supermap.dao.UserDao;
import com.supermap.entity.UserInfo;

/**  
 * @Description: TODO
 * @author zhoujian
 * @date 2017-3-3
 * @version V1.0 
 */
@Repository
public class UserDaoImpl implements UserDao
{
	@Resource(name = "sqlSessionTemplate")
	SqlSessionTemplate sqlSessionTemplate;
//	@Resource
//	SqlSessionFactory sessionFactory;
	@Override
	public UserInfo select(Integer id)
	{
		return sqlSessionTemplate.selectOne("com.supermap.mapping.selectOne", id);
//		return sessionFactory.openSession().selectOne("com.supermap.mapping.selectOne", id);
	}
	@Override
	public int insert(UserInfo userInfo)
	{
		return sqlSessionTemplate.insert("com.supermap.mapping.insert", userInfo);
	}
	@Override
	public List<UserInfo> selectAll()
	{
		return sqlSessionTemplate.selectList("com.supermap.mapping.selectAll");
	}
	@Override
	public int addUserInfoBatch(List<UserInfo> userList)
	{
		return sqlSessionTemplate.insert("com.supermap.mapping.addUserBatch", userList);
	}
	@Override
	public int updateUserBatch(List<UserInfo> userList)
	{
		return sqlSessionTemplate.update("com.supermap.mapping.updateUserBatch", userList);
	}
	@Override
	public int update(UserInfo userInfo)
	{
		return sqlSessionTemplate.update("com.supermap.mapping.updateUser", userInfo);
	}
	
	

}
