package com.example.springjwt.api.vision;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.User.UserRepository;
import com.example.springjwt.fridge.Fridge;
import com.example.springjwt.fridge.FridgeRepository;
import com.example.springjwt.fridge.FridgeService;
import com.example.springjwt.fridge.UnitCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VisionAnalyzeService {

    private final GcpVisionClient gcpVisionClient;
    private final FridgeRepository fridgeRepository;
    private final FridgeService fridgeService;

    // 단순한 식재료 키워드 필터 예시
    private static final Set<String> VALID_INGREDIENTS = Set.of(
            "apple", "milk", "onion", "cabbage", "egg", "carrot", "tomato", "rice", "cheese", "bread"
    );

    public List<String> analyzeAndSave(MultipartFile imageFile, UserEntity user) {
        List<String> labels = gcpVisionClient.detectLabels(imageFile);
        List<String> saved = new ArrayList<>();
        Long userId = (long) user.getId();
        for (String label : labels) {
            if (VALID_INGREDIENTS.contains(label)) {
                Fridge fridge = new Fridge();
                fridge.setIngredientName(label);
                fridge.setStorageArea("냉장");
                fridge.setQuantity(1.0);
                fridge.setUnitCategory(UnitCategory.COUNT);
                fridge.setUnitDetail("개");
                fridge.setFridgeDate(LocalDate.now());
                fridge.setCreatedAt(LocalDateTime.now());
                fridge.setUpdatedAt(LocalDateTime.now());
                fridge.setUser(user);
                fridgeService.createFridge(fridge, userId);
                saved.add(label);
            }
        }
        return saved;
    }
}
