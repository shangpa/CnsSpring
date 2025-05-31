package com.example.springjwt.admin.dto;

import lombok.Getter;

@Getter
public class DeleteRequestDTO {
    private String adminUsername;
    private String reason;
}
