package com.example.springjwt.ingredient;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "ingredient_master",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_ingredient_name_ko",
                columnNames = {"name_ko"}
        )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IngredientMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 한국어 이름 (예: 감자) */
    @Column(name = "name_ko", length = 100, nullable = false)
    private String nameKo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_ing_cat"))
    private IngredientCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_unit_id", foreignKey = @ForeignKey(name = "fk_ing_unit"))
    private UnitEntity defaultUnit;

    /** 재료 카드 썸네일(일러스트/아이콘) */
    @Column(name = "icon_url", length = 255)
    private String iconUrl;

}
