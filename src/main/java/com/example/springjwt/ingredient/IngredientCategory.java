package com.example.springjwt.ingredient;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "ingredient_category",
        uniqueConstraints = @UniqueConstraint(name = "uq_ing_category_name", columnNames = "name_ko")
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IngredientCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_ko", length = 50, nullable = false)
    private String nameKo;
}
