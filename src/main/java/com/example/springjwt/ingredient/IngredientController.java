package com.example.springjwt.ingredient;

import com.example.springjwt.ingredient.dto.IngredientMasterResponse;
import com.example.springjwt.ingredient.dto.UnitResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientMasterRepository ingredientRepo;
    private final UnitRepository unitRepo;

    @GetMapping("/ingredients/categories")
    public ResponseEntity<List<String>> categories() {
        var list = Arrays.stream(IngredientCategory.values()).map(Enum::name).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/ingredients")
    public ResponseEntity<List<IngredientMasterResponse>> list(
            @RequestParam String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        var pageable = PageRequest.of(page, size);
        var cat = IngredientCategory.valueOf(category);
        var pageResult = (keyword == null || keyword.isBlank())
                ? ingredientRepo.findByCategory(cat, pageable)
                : ingredientRepo.findByCategoryAndNameKoContainingIgnoreCase(cat, keyword.trim(), pageable);

        var body = pageResult.getContent().stream().map(it ->
                IngredientMasterResponse.builder()
                        .id(it.getId())
                        .nameKo(it.getNameKo())
                        .category(it.getCategory().name())
                        .defaultUnitId(it.getDefaultUnit() == null ? null : it.getDefaultUnit().getId())
                        .iconUrl(it.getIconUrl())
                        .build()
        ).toList();

        return ResponseEntity.ok(body);
    }

    @GetMapping("/units")
    public ResponseEntity<List<UnitResponse>> units() {
        var body = unitRepo.findAll().stream().map(u ->
                UnitResponse.builder().id(u.getId()).name(u.getName()).kind(u.getKind().name()).build()
        ).toList();
        return ResponseEntity.ok(body);
    }
}
