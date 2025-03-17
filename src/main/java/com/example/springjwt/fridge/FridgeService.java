package com.example.springjwt.fridge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FridgeService {

    @Autowired
    private FridgeRepository fridgeRepository;

    // 냉장고 항목 추가
    public Fridge createFridge(Fridge fridge) {
        fridge.setCreatedAt(LocalDateTime.now());
        fridge.setUpdatedAt(LocalDateTime.now());
        return fridgeRepository.save(fridge);
    }

    public List<Fridge> getFridgesByUserId(Long userId) {
        return fridgeRepository.findByUser_Id(userId);
    }


    // 개별 항목 조회
    public Fridge getFridgeById(Long id) {
        return fridgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("냉장고 항목을 찾을 수 없습니다."));
    }

    // 업데이트
    public Fridge updateFridge(Long id, Fridge updatedFridge) {
        Fridge existing = fridgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("냉장고 항목을 찾을 수 없습니다."));
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