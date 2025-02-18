package com.example.springjwt.controller;

import com.example.springjwt.dto.CustomUserDetails;
import com.example.springjwt.dto.LoginInfoResponse;
import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    private final UserRepository userRepository;
    public MainController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/user/info")
    public LoginInfoResponse mainP() {
        // Authentication 객체에서 현재 사용자 정보 받아오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        // username 추출
        String userName = customUserDetails.getUsername();
        System.out.println("유저네임: " + userName);

        // username을 기반으로 name을 가져오는 방법 (DB에서 조회)
        UserEntity userEntity = userRepository.findByUsername(userName);  // UserRepository를 통해 DB에서 사용자 정보 가져오기
        String name = userEntity.getName();  // name을 가져옵니다.

        // LoginInfoResponse로 반환
        return new LoginInfoResponse(userName, name);
    }

}