package com.umi.service;

import com.umi.pojo.RespResult;
import com.umi.pojo.Users;

public interface UsersService {

	/**
	 * 通过用户名查询用户
	 * @param username
	 * @return
	 */
	Users findUserByUsername(String username);
	/**
	 * 新增用户
	 * @param tUser
	 * @return
	 */
	int insUser(Users tUser);
	/**
	 * 修改用户 通过id
	 * @param user
	 * @return
	 */
	int updUser(Users user);
	/**
	 * 修改用户密码
	 * @param user
	 * @return
	 */
	RespResult updPassword(String oldPassword, String password, String repassword);
}
