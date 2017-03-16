package com.supermap.controller;


import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;
import javax.enterprise.inject.New;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.supermap.entity.UserInfo;
import com.supermap.service.UserService;

/**  
 * @Description: TODO
 * @author zhoujian
 * @date 2017-3-3
 * @version V1.0 
 */
@Controller
@RequestMapping("/user")
public class UserController
{
	private Logger logger = Logger.getLogger(this.getClass());
	@Resource
	UserService userService ; 
	 @RequestMapping("/select")
	 @ResponseBody
	    public Object select(HttpServletRequest request, Model model) {
	        int userId = Integer.parseInt(request.getParameter("id"));
	        UserInfo user = this.userService.select(userId);
	        model.addAttribute("user", user);
	        return user;
	    }
	 @RequestMapping("/insert")
	 public String insert(HttpServletRequest request, Model model)
	 {
		 long start = System.currentTimeMillis();
		 int num = Integer.parseInt(request.getParameter("number"));
		 for (int i = 1; i < num; i++)
		{
			 UserInfo userInfo = new UserInfo();
			 
			 userInfo.setUserId((int)System.currentTimeMillis());
			 userInfo.setUserName("aaaa");
			 userInfo.setPassword("123456");
			 userInfo.setUserAge(i);
			 userService.insert(userInfo);
			 logger.info(userInfo.toString());
		}
		 long end = System.currentTimeMillis();
		 System.out.println(num + " 单条插入使用时间 ： " + (end - start ) / 1000.0 + " 秒");
		 logger.error(num + " 单条插入使用时间 ： " + (end - start ) / 1000.0 + " 秒");
		return "success";
	 }
	 
	 @RequestMapping("/selectAll")
	 @ResponseBody
	 public Object selectAll(HttpServletRequest request, Model model)
	 {
		 List<UserInfo> userList = userService.selectAll();
		 return userList;
	 }
	 
	 @RequestMapping("/addBacth")
	 public String addBacth(HttpServletRequest request, Model model)
	 {
		 
		 long start = System.currentTimeMillis();
		 int num = Integer.parseInt(request.getParameter("number"));
		List<UserInfo> userList = new LinkedList<UserInfo>();
		 for (int i = 1; i < num; i++)
		{
			UserInfo userInfo = new UserInfo();
			userInfo.setUserId(i);
			userInfo.setUserName("aaaa");
			userInfo.setPassword("123456");
			userInfo.setUserAge(i);
			userList.add(userInfo);
		}
		 userService.addUserInfoBatch(userList);
		 long end = System.currentTimeMillis();
		 System.out.println(num + " 批量插入使用时间 ： " + (end - start ) / 1000.0 + " 秒");
		 logger.error(num + " 单条插入使用时间 ： " + (end - start ) / 1000.0 + " 秒");
		 
		return "success";
	 }
	 
	 @RequestMapping("/updataBacth")
	 public String updataBacth(HttpServletRequest request, Model model)
	 {
		 long start = System.currentTimeMillis();
		 int num = Integer.parseInt(request.getParameter("number"));
		List<UserInfo> userList = new LinkedList<UserInfo>();
		 for (int i = 1; i < num; i++)
		{
			UserInfo userInfo = new UserInfo();
			userInfo.setUserId(i);
			userInfo.setUserName("bbbb");
			userInfo.setPassword("111111");
			userInfo.setUserAge(i);
			userList.add(userInfo);
		}
		 userService.updateUserBatch(userList);
		 long end = System.currentTimeMillis();
		 System.out.println(num + " 批量更新使用时间 ： " + (end - start ) / 1000.0 + " 秒");
		 logger.info(num + " 单条插入使用时间 ： " + (end - start ) / 1000.0 + " 秒");
		 
		return "success";
	 }
	 
	 @RequestMapping("/update")
	 public String update(HttpServletRequest request, Model model)
	 {
		 long start = System.currentTimeMillis();
		 int num = Integer.parseInt(request.getParameter("number"));
		 for (int i = 1; i < num; i++)
		{
			UserInfo userInfo = new UserInfo();
			userInfo.setUserId(i);
			userInfo.setUserName("hhhh");
			userInfo.setPassword("888888");
			userInfo.setUserAge(i);
			userService.update(userInfo);
		}
		 long end = System.currentTimeMillis();
		 System.out.println( num + " 单条更新使用时间 ： " + (end - start ) / 1000.0 + " 秒");
		 logger.info(num + " 单条插入使用时间 ： " + (end - start ) / 1000.0 + " 秒");
		 
		return "success";
	 }
}
