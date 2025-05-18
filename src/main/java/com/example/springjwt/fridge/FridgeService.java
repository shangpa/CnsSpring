package com.example.springjwt.fridge;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import com.example.springjwt.point.PointActionType;
import com.example.springjwt.point.PointService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
public class FridgeService {

    @Autowired
    private FridgeRepository fridgeRepository;

    @Autowired
    private UserRepository userRepository; // UserEntity 조회를 위한 Repository
    @Autowired
    private  PointService pointService;

    // 냉장고 항목 추가 (userId를 추가 인자로 받음)
    public Fridge createFridge(Fridge fridge, Long userId) {
        Integer intUserId = userId.intValue();
        UserEntity user = userRepository.findById(intUserId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        fridge.setUser(user);
        fridge.setCreatedAt(LocalDateTime.now());
        fridge.setUpdatedAt(LocalDateTime.now());
        fridgeRepository.save(fridge);

        // 포인트 적립 체크
        long totalCount = fridgeRepository.countByUser(user);
        int newStep = (int) (totalCount / 10);
        int prevStep = user.getFridgePointStep();

        if (newStep > prevStep) {
            int diff = newStep - prevStep;
            pointService.addPoint(
                    user,
                    PointActionType.FRIDGE_INPUT,
                    diff * 10,
                    "냉장고 재료 누적 " + totalCount + "개 등록"
            );

            user.setFridgePointStep(newStep);
            userRepository.save(user);
        }
        return fridge;
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

    //유통기한 찾기
    public List<Fridge> getExpiringFridgeItems(int daysLeft) {
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today.plusDays(daysLeft);
        return fridgeRepository.findAll().stream()
                .filter(f -> f.getFridgeDate() != null)
                .filter(f -> !f.getFridgeDate().isBefore(today)) // 오늘 이후
                .filter(f -> !f.getFridgeDate().isAfter(targetDate)) // targetDate 이전
                .collect(Collectors.toList());
    }

    //냉장고 차감
    @Transactional
    public void useIngredients(List<UsedIngredientDTO> ingredients, UserEntity user) {
        for (UsedIngredientDTO dto : ingredients) {
            Fridge fridge = fridgeRepository.findByUserAndIngredientName(user, dto.getName())
                    .orElseThrow(() -> new IllegalArgumentException("재료를 찾을 수 없습니다: " + dto.getName()));

            double remaining = fridge.getQuantity() - dto.getAmount();
            if (remaining < 0) {
                throw new IllegalStateException("재료 수량이 부족합니다: " + dto.getName());
            }

            fridge.setQuantity(remaining);
        }
    }
}
