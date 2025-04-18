package com.example.springjwt.User;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;
    private String username;
    private String password;

    private String role;
    @Column(nullable = false)
    private int point = 0; // 기본값 0
    @Column(nullable = false)
    private int fridgePointStep = 0;
}