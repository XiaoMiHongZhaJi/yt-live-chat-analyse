package com.lwf.ytlivechatanalyse.auth.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthPrincipal {
    private String userId;
    private String userName;
}
