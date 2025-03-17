package com.example.springjwt.fridge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fridges")
public class FridgeController {

    @Autowired
    private FridgeService fridgeService;

    // 냉장고 항목 추가 (POST)
    @PostMapping
    public ResponseEntity<Fridge> createFridge(@RequestBody Fridge fridge) {
        Fridge createdFridge = fridgeService.createFridge(fridge);
        return new ResponseEntity<>(createdFridge, HttpStatus.CREATED);
    }

    // 로그인한 사용자의 냉장고 항목 조회 (GET)
    @GetMapping("/my")
    public ResponseEntity<List<Fridge>> getMyFridges(@RequestParam Long userId) {
        List<Fridge> myFridges = fridgeService.getFridgesByUserId(userId);
        return ResponseEntity.ok(myFridges);
    }

}
