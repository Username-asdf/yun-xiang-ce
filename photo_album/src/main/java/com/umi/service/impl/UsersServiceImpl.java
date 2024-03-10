package com.umi.service.impl;

import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.umi.mapper.UsersMapper;
import com.umi.pojo.Folder;
import com.umi.pojo.RespResult;
import com.umi.pojo.Users;
import com.umi.pojo.UsersExample;
import com.umi.service.FolderService;
import com.umi.service.UsersService;

@Service
@Transactional
public class UsersServiceImpl implements UsersService{

	@Autowired
	private UsersMapper usersMapper;
	
	@Override
	public Users findUserByUsername(String username) {
		UsersExample example = new UsersExample();
		example.or().andUsernameEqualTo(username);
		List<Users> list = usersMapper.selectByExample(example);
		if(null==list || list.size()<=0) {
			return null;
		}
		return list.get(0);
	}

	@Override
	public int insUser(Users user) {
		Md5Hash md5 = new Md5Hash(user.getPassword(),user.getUsername());
		user.setPassword(md5.toString());
		return usersMapper.insertSelective(user);
	}

	@Override
	public int updUser(Users user) {
		
		return usersMapper.updateByPrimaryKeySelective(user);
	}

	@Override
	public RespResult updPassword(String oldPassword, String password, String repassword) {
		RespResult result = new RespResult();
		result.setCode(500);
		result.setMsg("修改密码失败");
		
		Object principal = SecurityUtils.getSubject().getPrincipal();
		if(principal == null) {
			return result;
		}
		Users user = (Users) principal;
		
		if(oldPassword == null || password == null || repassword == null) {
			return result;
		}
		
		if(oldPassword.equals("") || password.equals("") || repassword.equals("")) {
			result.setMsg("请填写完整信息");
			return result;
		}
		
		if(!password.equals(repassword)) {
			result.setMsg("两次密码不一致");
			return result;
		}
		
		//将密码转为MD5
		Md5Hash md5 = new Md5Hash(oldPassword, user.getUsername());
		if(!md5.toString().equals(user.getPassword())) {
			result.setMsg("旧密码不正确");
			return result;
		}
		
		user.setPassword(new Md5Hash(password, user.getUsername()).toString());
		int i = this.updUser(user);
		if(i > 0) {
			result.setCode(200);
			result.setMsg("修改密码成功");
			//清除principal  退出登录
			SecurityUtils.getSubject().logout();
		}
		
		return result;
	}

}
