package com.supermap.service;

import java.util.List;

import com.supermap.entity.UserInfo;

/**  
 * @Description: TODO
 * @author zhoujian
 * @date 2017-3-3
 * @version V1.0 
 */
public interface UserService
{
	public UserInfo select(Integer id);
	
	public List<UserInfo> selectAll();
	
	public int insert(UserInfo userInfo);
	
	public int addUserInfoBatch(List<UserInfo> userList);
	
	public int updateUserBatch(List<UserInfo> userList);
	
	public int update(UserInfo userInfo);
}
