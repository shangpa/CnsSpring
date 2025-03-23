package com.example.springjwt.fridge;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import com.example.springjwt.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

@RestController
@RequestMapping("/api/fridges")
public class FridgeController {

    @Autowired
    private FridgeService fridgeService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JWTUtil jwtUtil;

    // 냉장고 항목 추가 (POST)
    // 요청 본문에서 FridgeRequest DTO를 받아, 내부의 userId를 이용해 Fridge 엔티티 생성 후 저장
    @PostMapping
    public ResponseEntity<Fridge> createFridge(@AuthenticationPrincipal UserDetails userDetails,@RequestBody FridgeRequestDTO fridgeRequest) {
        // 현재 로그인한 사용자의 username 가져오기
        String username = userDetails.getUsername();

        UserEntity user = userRepository.findOptionalByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // FridgeRequest DTO의 데이터를 Fridge 엔티티에 매핑
        Fridge fridge = new Fridge();
        fridge.setIngredientName(fridgeRequest.getIngredientName());
        fridge.setStorageArea(fridgeRequest.getStorageArea());
        fridge.setFridgeDate(fridgeRequest.getFridgeDate());
        fridge.setDateOption(fridgeRequest.getDateOption());
        fridge.setQuantity(fridgeRequest.getQuantity());
        fridge.setPrice(fridgeRequest.getPrice());
        fridge.setUnitCategory(fridgeRequest.getUnitCategory());
        fridge.setUnitDetail(fridgeRequest.getUnitDetail());
        long userId = (long) user.getId();
        // FridgeRequest에 포함된 userId를 사용해 서비스를 호출합니다.
        Fridge createdFridge = fridgeService.createFridge(fridge, userId);
        return new ResponseEntity<>(createdFridge, HttpStatus.CREATED);
    }

    // 로그인한 사용자의 냉장고 항목 조회 (GET)
    @GetMapping("/my")
    public ResponseEntity<List<Fridge>> getMyFridges(@AuthenticationPrincipal UserDetails userDetails) {
        // 현재 로그인한 사용자명 가져오기
        String username = userDetails.getUsername();

        UserEntity user = userRepository.findOptionalByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // userId를 이용해서 냉장고 재료 가져오기
        List<Fridge> myFridges = fridgeService.getFridgesByUserId((long) user.getId());
        return ResponseEntity.ok(myFridges);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFridge(
            @PathVariable Long id,
            @RequestBody FridgeRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {

        fridgeService.updateFridge(id, request, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFridge(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        System.out.println("authorization now");

        String token = authHeader.substring(7); // "Bearer " 제거
        String username = jwtUtil.getUsername(token);
        UserEntity user = userRepository.findOptionalByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        fridgeService.deleteFridge(id, user);
        return ResponseEntity.ok().build();
    }


}
