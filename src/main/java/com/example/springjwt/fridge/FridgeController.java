package com.example.springjwt.fridge;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fridges")
public class FridgeController {

    @Autowired
    private FridgeService fridgeService;
    @Autowired
    private UserRepository userRepository;
    // 냉장고 항목 추가 (POST)
    // 요청 본문에서 FridgeRequest DTO를 받아, 내부의 userId를 이용해 Fridge 엔티티 생성 후 저장
    @PostMapping
    public ResponseEntity<Fridge> createFridge(@AuthenticationPrincipal UserDetails userDetails,@RequestBody FridgeRequestDTO fridgeRequest) {
        // 현재 로그인한 사용자의 username 가져오기
        String username = userDetails.getUsername();

        // username을 이용해 userId 조회 (UserRepository 활용)
        UserEntity user = userRepository.findByUsername(username);

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
    public ResponseEntity<List<Fridge>> getMyFridges(@RequestParam Long userId) {
        List<Fridge> myFridges = fridgeService.getFridgesByUserId(userId);
        return ResponseEntity.ok(myFridges);
    }
}
