package com.umi.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.umi.config.shiro.UserRealm;

@Configuration
public class ShiroConfig {

	//配置filter工厂
	@Bean
	public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager) {
		ShiroFilterFactoryBean filter = new ShiroFilterFactoryBean();
		filter.setSecurityManager(securityManager);
		Map<String, String> filterChainDefinitionMap = new HashMap<String, String>();
		
		filterChainDefinitionMap.put("/sign-in", "anon");
		filterChainDefinitionMap.put("/sign-up", "anon");
		filterChainDefinitionMap.put("/register", "anon");
		filterChainDefinitionMap.put("/login", "anon");
		filterChainDefinitionMap.put("/css/**", "anon");
		filterChainDefinitionMap.put("/js/**", "anon");
		filterChainDefinitionMap.put("/layui/**", "anon");
		filterChainDefinitionMap.put("/fonts/**", "anon");
		filterChainDefinitionMap.put("/images/**", "anon");
		filterChainDefinitionMap.put("/index", "authc");
		filterChainDefinitionMap.put("/**", "authc");
//		filterChainDefinitionMap.put("/*","anon");
		
		filter.setFilterChainDefinitionMap(filterChainDefinitionMap );
		filter.setLoginUrl("/sign-in");
		return filter;
	}
	
	//配置SecurityManager
	@Bean
	public DefaultWebSecurityManager defaultWebSecurityManager(UserRealm userRealm) {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
		securityManager.setRealm(userRealm);
		return securityManager;
	}
	
	//配置realm
	@Bean
	public UserRealm userRealm() {
		UserRealm realm = new UserRealm();
		//设置MD5加密
		HashedCredentialsMatcher credentialsMatcher = new HashedCredentialsMatcher();
		credentialsMatcher.setHashAlgorithmName("md5");
		realm.setCredentialsMatcher(credentialsMatcher);
		return realm;
	}
}
