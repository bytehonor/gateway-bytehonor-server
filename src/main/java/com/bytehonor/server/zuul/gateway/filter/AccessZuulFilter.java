package com.bytehonor.server.zuul.gateway.filter;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.bytehonor.sdk.center.user.constant.UserHeaderKey;
import com.bytehonor.sdk.center.user.model.AccessToken;
import com.bytehonor.sdk.center.user.service.AccessTokenValidateService;
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
    private AccessTokenValidateService accessTokenValidateService;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String serverPort;

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String fromTerminal = TerminalUtils.getFromTerminal(request);
        String fromIp = TerminalUtils.getFromIp(request);
        ctx.addZuulRequestHeader(HeaderKey.X_FROM_TERMINAL, fromTerminal);
        ctx.addZuulRequestHeader(HeaderKey.X_REAL_IP, fromIp);
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        if ("GET".equalsIgnoreCase(method) || PassUrl.isPass(requestURI)) {
            LOG.info("L1. {} {} {} {}", fromTerminal, fromIp, method, requestURI);
            return null;
        }
        AccessToken accessToken = null;
        try {
            accessToken = AccessTokenUtils.build(request, fromTerminal);
        } catch (Exception e) {
            processAuthFail(ctx, e.getMessage());
            return null;
        }

        if (accessToken.getDebug()) {
            LOG.info("L2. {} {} {} {}", fromTerminal, fromIp, method, requestURI);
            return null;
        }

        String up = UserPassportUtils.toString(accessToken.getRoleKey(), accessToken.getGuid(), fromTerminal, fromIp);
        ctx.addZuulRequestHeader(UserHeaderKey.X_USER_PASSPORT, up);

        if (LOG.isInfoEnabled()) {
            LOG.info("L3. {} {} {} {}, USER:{}-{}", fromTerminal, fromIp, method, requestURI, accessToken.getRoleKey(),
                    accessToken.getGuid());
        }

        boolean isOk = false;
        try {
            isOk = accessTokenValidateService.isEffective(accessToken);
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

    // private TokenExpireVO getTokenExpire(RequestContext ctx, UserToken userToken,
    // String fromTerminal) {
    // String user = userToken.getGuid();
    // String token = userToken.getToken();
    // Integer type = userToken.getRoleKey();
    //
    // if (UserTypeEnum.WECHAT.getCode() == type) {
    // ctx.addZuulRequestHeader(HeaderAuthKey.WECHAT_USER_OPENID, user);
    // JsonResponse<TokenExpireVO> result =
    // userCenterRemoteService.getWechatTokenExpire(user, token);
    // if (result.getCode() != 0) {
    // LOG.error("Wechat Expire result code:{}, message:{}", result.getCode(),
    // result.getMessage());
    // throw new RuntimeException("WechatToken invalid");
    // }
    // return result.getData();
    // } else if (UserTypeEnum.MERCHANT.getCode() == type) {
    // ctx.addZuulRequestHeader(HeaderAuthKey.MERCHANT_USER_UNID, user);
    // JsonResponse<TokenExpireVO> result =
    // userCenterRemoteService.getMerchantTokenExpire(user, token, fromTerminal);
    // if (result.getCode() != 0) {
    // LOG.error("Merchant Expire result from:{}, code:{}, message:{}",
    // fromTerminal, result.getCode(),
    // result.getMessage());
    // throw new RuntimeException("StoreToken invalid");
    // }
    // return result.getData();
    // } else if (UserTypeEnum.ADMIN.getCode() == type) {
    // ctx.addZuulRequestHeader(HeaderAuthKey.ADMIN_USER_UNID, user);
    // JsonResponse<TokenExpireVO> result =
    // userCenterRemoteService.getAdminTokenExpire(user, token, fromTerminal);
    // if (result.getCode() != 0) {
    // LOG.error("Admin Expire result from:{}, code:{}, message:{}", fromTerminal,
    // result.getCode(),
    // result.getMessage());
    // throw new RuntimeException("AdminToken invalid");
    // }
    // return result.getData();
    // } else {
    // throw new RuntimeException("Authentication type invalid");
    // }
    // }

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
