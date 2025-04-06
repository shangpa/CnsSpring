package com.example.springjwt.search;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchKeywordService searchKeywordService;

    //  인기 검색어 조회
    @GetMapping("/popular-keywords")
    public ResponseEntity<List<String>> getPopularKeywords() {
        List<String> topKeywords = searchKeywordService.getTop10Keywords();
        return ResponseEntity.ok(topKeywords);
    }

    //  검색어 저장 (필요할 경우 사용)
    @PostMapping("/save")
    public ResponseEntity<Void> saveKeyword(@RequestBody String keyword) {
        searchKeywordService.saveKeyword(keyword);
        return ResponseEntity.ok().build();
    }
}
