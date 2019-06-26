package com.bytehonor.server.zuul.gateway.filter;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import com.bytehonor.sdk.center.user.constant.UserHeaderKey;
import com.bytehonor.sdk.center.user.service.AccessTokenCacheService;
import com.bytehonor.sdk.center.user.util.AccessTokenUtils;
import com.bytehonor.sdk.center.user.util.UserPassportUtils;
import com.bytehonor.sdk.protocol.common.constant.HeaderKey;
import com.bytehonor.sdk.protocol.common.result.JsonResponse;
import com.bytehonor.sdk.server.spring.util.TerminalUtils;
import com.bytehonor.server.zuul.gateway.constant.PassUrl;
import com.google.gson.Gson;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

public class AccessZuulFilter extends ZuulFilter {

    private static final Logger LOG = LoggerFactory.getLogger(AccessZuulFilter.class);

    private static final Gson GSON = new Gson();

    @Autowired
    private AccessTokenCacheService accessTokenCacheService;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String serverPort;

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String fromUuid = TerminalUtils.getFromUuid(request);
        String fromTerminal = TerminalUtils.getFromTerminal(request);
        String fromIp = TerminalUtils.getFromIp(request);
        // 三个key透传
        ctx.addZuulRequestHeader(HeaderKey.X_FROM_TERMINAL, fromTerminal);
        ctx.addZuulRequestHeader(HeaderKey.X_REAL_IP, fromIp);
        ctx.addZuulRequestHeader(HeaderKey.X_FROM_UUID, fromUuid);
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method) || PassUrl.isPass(requestURI)) {
            LOG.info("L1. {} {} {} {}", fromTerminal, fromIp, method, requestURI);
            return null;
        }

        if ("true".equals(request.getParameter("debug"))) {
            LOG.info("L2. {} {} {} {}", fromTerminal, fromIp, method, requestURI);
            return null;
        }

        String token = AccessTokenUtils.prase(request, fromTerminal);
        if (StringUtils.isEmpty(token)) {
            processAuthFail(ctx, "token invalid!");
            return null;
        }

        String up = UserPassportUtils.toString(fromUuid, fromTerminal, fromIp);
        ctx.addZuulRequestHeader(UserHeaderKey.X_USER_PASSPORT, up);

        if (LOG.isInfoEnabled()) {
            LOG.info("L3. {} {} {} {}, USER:{}", fromTerminal, fromIp, method, requestURI, fromUuid);
        }

        boolean isOk = false;
        try {
            isOk = accessTokenCacheService.isEffective(fromTerminal, token);
        } catch (Exception e) {
            isOk = false;
            LOG.error("check failed, error:{}", e.getMessage());
        }

        if (isOk == false) {
            processAuthFail(ctx, "token expired!");
        }

        return null;
    }

    private void processAuthFail(RequestContext ctx, String message) {
        ctx.setSendZuulResponse(false);
        ctx.setResponseStatusCode(200);
        JsonResponse<String> body = new JsonResponse<String>();
        body.setCode(1001);
        body.setMessage(message);
        body.setData(message);
        ctx.setResponseBody(GSON.toJson(body));
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public String filterType() {
        return "pre";
    }

}
