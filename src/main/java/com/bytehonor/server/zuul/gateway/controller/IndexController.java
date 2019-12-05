package com.bytehonor.server.zuul.gateway.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bytehonor.sdk.basic.server.util.TerminalUtils;

@RestController
public class IndexController {
	
	private static final Logger LOG = LoggerFactory.getLogger(IndexController.class);
	
	@Value("${spring.application.name}")
	private String applicationName;
	
	@Value("${server.port}")
	private String serverPort;
	
    @RequestMapping("/")
    public String actionIndex(HttpServletRequest request) {
    	if (LOG.isDebugEnabled()) {
    		LOG.debug("hello {}, {}, from:{}", applicationName, serverPort, TerminalUtils.getFromTerminal(request));
    	} 
    	StringBuilder sb = new StringBuilder();
    	sb.append("hello world, ").append(applicationName).append(":").append(serverPort);
        return sb.toString();
    }

}