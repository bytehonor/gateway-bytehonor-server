package com.bytehonor.server.zuul.gateway.config;

import java.io.Serializable;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import com.bytehonor.sdk.center.user.service.AccessTokenValidateService;
import com.bytehonor.sdk.center.user.service.impl.AccessTokenValidateServiceImpl;

@Configuration
@AutoConfigureAfter(RedisCacheAutoConfiguration.class)
public class UserCenterSdkConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AccessTokenValidateService userTokenCheckService(RedisTemplate<String, Serializable> redisTemplate) {
        AccessTokenValidateServiceImpl impl = new AccessTokenValidateServiceImpl(redisTemplate);
        return impl;
    }
}
