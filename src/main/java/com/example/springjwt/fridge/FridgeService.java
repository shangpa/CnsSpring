package com.example.springjwt.fridge;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
        // UserRepository에서 사용 중인 ID 타입이 Integer이므로 변환
        Integer intUserId = userId.intValue();
        // 전달받은 userId로 UserEntity 조회
        UserEntity user = userRepository.findById(intUserId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 조회된 UserEntity를 Fridge 엔티티의 user 필드에 설정
        fridge.setUser(user);
        fridge.setCreatedAt(LocalDateTime.now());
        fridge.setUpdatedAt(LocalDateTime.now());
        return fridgeRepository.save(fridge);
    }

    // 로그인한 사용자의 냉장고 항목 조회
    public List<Fridge> getFridgesByUserId(Long userId) {
        return fridgeRepository.findByUser_Id(userId);
    }

    // 개별 항목 조회
    public Fridge getFridgeById(Long id) {
        return fridgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("냉장고 항목을 찾을 수 없습니다."));
    }

    // 업데이트
    public Fridge updateFridge(Long id, Fridge updatedFridge, Long userId) {
        Fridge existing = fridgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("냉장고 항목을 찾을 수 없습니다."));
        // (선택 사항) user 정보 검증 로직 추가 가능
        existing.setIngredientName(updatedFridge.getIngredientName());
        existing.setStorageArea(updatedFridge.getStorageArea());
        existing.setFridgeDate(updatedFridge.getFridgeDate());
        existing.setDateOption(updatedFridge.getDateOption());
        existing.setQuantity(updatedFridge.getQuantity());
        existing.setPrice(updatedFridge.getPrice());
        existing.setUnitCategory(updatedFridge.getUnitCategory());
        existing.setUnitDetail(updatedFridge.getUnitDetail());
        existing.setUpdatedAt(LocalDateTime.now());
        return fridgeRepository.save(existing);
    }

    // 삭제
    public void deleteFridge(Long id) {
        fridgeRepository.deleteById(id);
    }
}
