package com.bytehonor.server.zuul.gateway.constant;

import java.util.HashSet;
import java.util.Set;

public class PassUrl {
	
	private static final Set<String> URLS = new HashSet<String>(1024);
	
	static {
		URLS.add("/cb1");
		URLS.add("/uc1/admin/tokens");
		URLS.add("/user-proxy/wechat/tokens");
		URLS.add("/user-proxy/merchant/tokens");
		URLS.add("/user-proxy/merchant/users/password");
	}
	
	public static boolean isPass(String url) {
		for (String s : URLS) {
			if (url.startsWith(s)) {
				return true;
			}
		}
		return false;
	}

}
