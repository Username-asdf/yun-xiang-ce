package com.umi.controller;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.umi.pojo.LoginResult;
import com.umi.pojo.RespResult;
import com.umi.pojo.Users;
import com.umi.service.UsersService;

@Controller
public class UserController {
	
	@Autowired
	private UsersService tUserSericeImpl;
	
	/**
	 * 跳转到登录页面
	 * @return
	 */
	@GetMapping({"/sign-in", "/login"})
	public String toLogin() {
		return "sign-in";
	}
	/**
	 * 跳转到注册页面
	 * @return
	 */
	@GetMapping({"/sign-up", "/register"})
	public String toRegister() {
		return "sign-up";
	}
	/**
	 * 跳转到主页
	 * @param model
	 * @return
	 */
	@RequestMapping({"/index"})
	@RequiresAuthentication
	public String toIndex(Model model) {
		Object principal = SecurityUtils.getSubject().getPrincipal();
		if(principal==null) {
			SecurityUtils.getSubject().logout();
			return "sign-in";
		}
		model.addAttribute("user", principal);
		return "index";
	}
	/**
	 * 登录
	 * @param username
	 * @param password
	 * @return
	 */
	@PostMapping("/login")
	@ResponseBody
	public LoginResult login(String username,String password) {
		LoginResult result = new LoginResult();
		Subject subject = SecurityUtils.getSubject();
		subject.getSession().setTimeout(7*24*3600*1000);
		
		UsernamePasswordToken token = new UsernamePasswordToken(username,password);
		token.setRememberMe(true);
		try {
			subject.login(token);
			result.setCode(200);
			result.setMsg("登录成功");
		} catch (UnknownAccountException e) {
//            return "用户名不存在！";
            result.setCode(500);
			result.setMsg("账号或密码错误！");
        } catch (AuthenticationException e) {
//            return "账号或密码错误！";
            result.setCode(500);
			result.setMsg("账号或密码错误！");
        } catch (AuthorizationException e) {
//            return "没有权限";
            result.setCode(500);
			result.setMsg("账号或密码错误！");
        }
		return result;
	}
	
	/**
	 * 注册
	 * @param username
	 * @param password
	 * @param repassword
	 * @return
	 */
	@PostMapping("/register")
	@ResponseBody
	public LoginResult register(String username,String password,String repassword) {
		LoginResult result = new LoginResult();
		if(username==null||password==null||repassword==null
				||username.equals("")||password.equals("")||repassword.equals("")) {
			result.setCode(500);
			result.setMsg("请填写完整信息");
			return result;
		}
		
		if(!password.equals(repassword)) {
			result.setCode(500);
			result.setMsg("两次密码不一致");
			return result;
		}
		
		Users user = tUserSericeImpl.findUserByUsername(username);
		if(user!=null) {
			result.setCode(500);
			result.setMsg("用户名已被注册");
			return result;
		}
		
		user = new Users();
		user.setUsername(username);
		user.setPassword(password);
		
		Integer i = tUserSericeImpl.insUser(user);
		if(i>0) {
			result.setCode(200);
			result.setMsg("注册成功");
		}
		
		return result;
	}
	/**
	 * 退出登录
	 * @return
	 */
	@RequestMapping("/logout")
	public String logout() {
		SecurityUtils.getSubject().logout();
		return "sign-in";
	}
	
	/**
	 * 修改密码
	 * @param oldPassword
	 * @param password
	 * @param repassword
	 * @return
	 */
	@RequestMapping("/up")
	@ResponseBody
	public RespResult updatePassowrd(String oldPassword, String password, String repassword) {
		return this.tUserSericeImpl.updPassword(oldPassword, password, repassword);
	}
	
	/**
	 * 跳转到修改密码页面
	 * @return
	 */
	@RequestMapping("/updatePwd")
	public String toUpdatePwd() {
		return "update_pwd.html";
	}
}
