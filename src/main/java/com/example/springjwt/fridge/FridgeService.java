package com.example.springjwt.fridge;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FridgeService {

    @Autowired
    private FridgeRepository fridgeRepository;

    @Autowired
    private UserRepository userRepository; // UserEntity 조회를 위한 Repository

    // 냉장고 항목 추가 (userId를 추가 인자로 받음)
    public Fridge createFridge(Fridge fridge, Long userId) {
        Integer intUserId = userId.intValue();
        UserEntity user = userRepository.findById(intUserId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        fridge.setUser(user);
        fridge.setCreatedAt(LocalDateTime.now());
        fridge.setUpdatedAt(LocalDateTime.now());
        return fridgeRepository.save(fridge);
    }

    // 로그인한 사용자의 냉장고 항목 조회
    public List<Fridge> getFridgesByUserId(Long userId) {
        return fridgeRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    // 개별 항목 조회
    public Fridge getFridgeById(Long id) {
        return fridgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("냉장고 항목을 찾을 수 없습니다."));
    }

    // 업데이트
    public void updateFridge(Long id, FridgeRequestDTO request, String username) {
        Fridge fridge = fridgeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fridge not found"));

        if (!fridge.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("수정 권한 없음");
        }

        // request의 값으로 fridge 업데이트
        fridge.setIngredientName(request.getIngredientName());
        fridge.setStorageArea(request.getStorageArea());
        fridge.setFridgeDate(request.getFridgeDate());
        fridge.setDateOption(request.getDateOption());
        fridge.setQuantity(request.getQuantity());
        fridge.setPrice(request.getPrice());
        fridge.setUnitCategory(request.getUnitCategory());
        fridge.setUnitDetail(request.getUnitDetail());
        fridge.setUpdatedAt(LocalDateTime.now());

        fridgeRepository.save(fridge);
    }


    // 삭제
    public void deleteFridge(Long id, UserEntity user) {
        Fridge fridge = fridgeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fridge not found"));

        if (fridge.getUser().getId() != user.getId()) {
            throw new AccessDeniedException("해당 냉장고 항목에 대한 삭제 권한이 없습니다.");
        }
        fridgeRepository.delete(fridge);
    }
}
