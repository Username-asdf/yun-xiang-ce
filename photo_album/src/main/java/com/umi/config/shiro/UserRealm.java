package com.umi.config.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.umi.pojo.Users;
import com.umi.service.UsersService;

public class UserRealm extends AuthorizingRealm{

	@Autowired
	private UsersService tUserService;
	
	@Override
	public String getName() {
		return "userRealm";
	}

	//授权
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		// TODO Auto-generated method stub
		return null;
	}

	//认证
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		Object p = token.getPrincipal();
		if(p==null) {
			return null;
		}
		Users user = tUserService.findUserByUsername(p.toString());
		if(user==null) {
			return null;
		}
		
		return new SimpleAuthenticationInfo(user, 
				user.getPassword(), 
				ByteSource.Util.bytes(user.getUsername()),
				this.getName());
	}

}
