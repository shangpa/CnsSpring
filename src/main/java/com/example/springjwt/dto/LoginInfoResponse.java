package com.example.springjwt.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor  // 기본 생성자 추가
public class LoginInfoResponse {
    private String userName;
    private String name;

    public LoginInfoResponse(String userName, String name) {
        this.userName = userName;
        this.name = name;
    }
}

