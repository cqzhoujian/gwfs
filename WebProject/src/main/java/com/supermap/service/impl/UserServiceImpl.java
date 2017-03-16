package com.supermap.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.supermap.dao.UserDao;
import com.supermap.entity.UserInfo;
import com.supermap.service.UserService;

/**  
 * @Description: TODO
 * @author zhoujian
 * @date 2017-3-3
 * @version V1.0 
 */
@Service/*("userService")*/
public class UserServiceImpl implements UserService
{
	@Resource
	private UserDao userDao;
	@Override
	public UserInfo select(Integer id)
	{
		return userDao.select(id);
	}
	@Override
	public int insert(UserInfo userInfo)
	{
		return userDao.insert(userInfo);
	}
	@Override
	public List<UserInfo> selectAll()
	{
		return userDao.selectAll();
	}
	@Override
	public int addUserInfoBatch(List<UserInfo> userList)
	{
		return userDao.addUserInfoBatch(userList);
	}
	@Override
	public int updateUserBatch(List<UserInfo> userList)
	{
		return userDao.updateUserBatch(userList);
	}
	@Override
	public int update(UserInfo userInfo)
	{
		return userDao.update(userInfo);
	}

}
