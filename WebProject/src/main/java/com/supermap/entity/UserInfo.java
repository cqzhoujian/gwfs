package com.supermap.entity;
/**  
 * @Description: TODO
 * @author zhoujian
 * @date 2017-3-3
 * @version V1.0 
 */
public class UserInfo
{
	private Integer userId = null;
	private String userName = null;
	private String password = null;
	private Integer userAge = null;
	
	
	
//	public UserInfo(Integer userId, String userName, String password, Integer userAge)
//	{
//		this.userId = userId;
//		this.userName = userName;
//		this.password = password;
//		this.userAge = userAge;
//	}
	public Integer getUserId()
	{
		return userId;
	}
	public String getUserName()
	{
		return userName;
	}
	public String getPassword()
	{
		return password;
	}
	public Integer getUserAge()
	{
		return userAge;
	}
	public void setUserId(Integer userId)
	{
		this.userId = userId;
	}
	public void setUserName(String userName)
	{
		this.userName = userName;
	}
	public void setPassword(String password)
	{
		this.password = password;
	}
	public void setUserAge(Integer userAge)
	{
		this.userAge = userAge;
	}
	
	@Override
	public String toString()
	{
		return userId + " " + userName + " " + password + " " + userAge;
	}
}
