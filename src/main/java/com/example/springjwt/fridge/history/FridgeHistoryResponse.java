package com.example.springjwt.fridge.history;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FridgeHistoryResponse {
    private String ingredientName;
    private double quantity;
    private String unit; // 예: 개, g, ml 등
    private String actionType; // ADD / USE
    private LocalDateTime actionDate;
}
