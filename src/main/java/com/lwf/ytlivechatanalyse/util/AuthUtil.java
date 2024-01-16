package com.lwf.ytlivechatanalyse.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

@Component
public class AuthUtil {

    private static final Logger logger = LoggerFactory.getLogger(AuthUtil.class);

    public static String manageAuth;

    @Value("${manageAuth}")
    public void setManageAuth(String manageAuth) {
        AuthUtil.manageAuth = manageAuth;
    }

    public static boolean auth(HttpServletRequest request){
        if(StringUtils.isBlank(manageAuth)){
            return true;
        }
        try {
            request.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("认证失败，请求解析异常", e);
            return false;
        }
        String authorization = request.getHeader("Authorization");
        if (authorization == null) {
            logger.error("认证失败，authorization为空");
            return false;
        }
        String basic = authorization.substring("Basic ".length());
        byte[] bytes;
        try {
            bytes = basic.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error("认证失败，认证解析异常", e);
            return false;
        }
        if(manageAuth.equals(new String(Base64Utils.decode(bytes)))){
            logger.info("认证成功");
            return true;
        }
        logger.error("认证失败");
        return false;
    }
}
