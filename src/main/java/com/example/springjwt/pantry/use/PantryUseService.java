package com.example.springjwt.pantry.use;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.ingredient.IngredientMaster;
import com.example.springjwt.ingredient.IngredientMasterRepository;
import com.example.springjwt.pantry.*;
import com.example.springjwt.pantry.history.HistoryAction;
import com.example.springjwt.pantry.history.PantryHistory;
import com.example.springjwt.pantry.history.PantryHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PantryUseService {

    private final PantryRepository pantryRepository;
    private final IngredientMasterRepository ingredientRepo;
    private final PantryStockRepository stockRepo;
    private final PantryHistoryRepository historyRepo;

    @Transactional
    public void useFromDefaultPantry(UserEntity user, List<UseIngredientRequest> items) {
        if (items == null || items.isEmpty()) return;

        // 기본 팬트리 선택 (없으면 첫 번째)
        Pantry pantry = pantryRepository.findFirstByUser_IdAndIsDefaultTrue(user.getId())
                .orElseGet(() -> pantryRepository.findAllByUser_IdOrderBySortOrderAscCreatedAtAsc(user.getId())
                        .stream().findFirst().orElseThrow(() -> new IllegalArgumentException("사용 가능한 냉장고가 없습니다.")));

        for (UseIngredientRequest it : items) {
            if (it.getName() == null || it.getName().isBlank()) continue;
            double amountD = it.getAmount() == null ? 0.0 : it.getAmount();
            if (amountD <= 0) continue;

            IngredientMaster ing = ingredientRepo.findByNameKoIgnoreCase(it.getName().trim())
                    .orElse(null);
            if (ing == null) continue;

            // 재고들: 유통기한 오름차순 → 생성일 오름차순
            List<PantryStock> stocks = stockRepo.findAllByPantry_IdAndIngredient_Id(pantry.getId(), ing.getId())
                    .stream()
                    .sorted(Comparator
                            .comparing(PantryStock::getExpiresAt, Comparator.nullsLast(Comparator.naturalOrder()))
                            .thenComparing(PantryStock::getCreatedAt))
                    .toList();

            BigDecimal remain = bd(amountD);
            for (PantryStock s : stocks) {
                if (remain.compareTo(BigDecimal.ZERO) <= 0) break;
                BigDecimal q = s.getQuantity() == null ? BigDecimal.ZERO : s.getQuantity();
                if (q.compareTo(BigDecimal.ZERO) <= 0) continue;

                BigDecimal consume = q.min(remain);     // 가능한 만큼 차감
                s.setQuantity(q.subtract(consume));     // 수량 감소

                // 히스토리: USE, changeQty는 양수로 기록
                historyRepo.save(
                        PantryHistory.builder()
                                .pantry(pantry)
                                .ingredient(ing)
                                .unit(s.getUnit())          // 기록용
                                .changeQty(consume)         // 양수
                                .action(HistoryAction.USE)
                                .stock(s)
                                .refType("RECIPE_USE")
                                .note("레시피 재료 사용")
                                .build()
                );

                remain = remain.subtract(consume);
            }

            // remain이 남아도 에러는 내지 않고 가능한 만큼만 차감 (원하면 여기서 예외 던져도 됨)
        }
    }

    private static BigDecimal bd(double v) { return BigDecimal.valueOf(v).setScale(3, java.math.RoundingMode.HALF_UP); }
}
