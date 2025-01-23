package com.example.springjwt.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginInfoResponse {

    private String userName;
    private String name;


    // 매개변수 생성자
    public LoginInfoResponse(String userName, String name) {
        this.userName = userName;
        this.name = name;
    }

}
