package com.example.springjwt.pantry.history;

import com.example.springjwt.User.UserEntity;
import com.example.springjwt.pantry.Pantry;
import com.example.springjwt.pantry.PantryRepository;
import com.example.springjwt.pantry.dto.PantryHistoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PantryHistoryService {

    private final PantryRepository pantryRepository;
    private final PantryHistoryRepository historyRepository;

    private static final DateTimeFormatter DF_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DF_DT   = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional(readOnly = true)
    public List<PantryHistoryDto> list(UserEntity user, Long pantryId, String ingredientName) {
        Pantry pantry = pantryRepository.findByIdAndUser_Id(pantryId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 냉장고가 없습니다. id=" + pantryId));

        var rows = (ingredientName == null || ingredientName.isBlank())
                ? historyRepository.findByPantry_IdOrderByCreatedAtDesc(pantry.getId())
                : historyRepository.findByPantry_IdAndIngredient_NameKoContainingIgnoreCaseOrderByCreatedAtDesc(
                pantry.getId(), ingredientName.trim()
        );

        return rows.stream().map(h -> {
            var ing  = h.getIngredient();
            var unit = h.getUnit();
            var stock = h.getStock();
            String purchasedAt = null, expiresAt = null;
            if (stock != null) {
                if (stock.getPurchasedAt() != null) purchasedAt = DF_DATE.format(stock.getPurchasedAt());
                if (stock.getExpiresAt()   != null) expiresAt   = DF_DATE.format(stock.getExpiresAt());
            }

            return PantryHistoryDto.builder()
                    .id(h.getId())
                    .action(h.getAction().name())
                    .ingredientId(ing.getId())
                    .ingredientName(ing.getNameKo())
                    .category(ing.getCategory() != null ? ing.getCategory().name() : null)
                    .quantity(h.getChangeQty().toPlainString())
                    .unitId(unit.getId())
                    .unitName(unit.getName())
                    .purchasedAt(purchasedAt)
                    .expiresAt(expiresAt)
                    .createdAt(DF_DT.format(h.getCreatedAt()))
                    .iconUrl(ing.getIconUrl())
                    .build();
        }).toList();
    }
}
