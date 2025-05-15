package com.example.springjwt.board;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class BoardDetailResponseDTO {
    private Long id;
    private String content;
    private String writer;
    private List<String> imageUrls;
    private String boardType;
    private String createdAt;
    private int likeCount;
}