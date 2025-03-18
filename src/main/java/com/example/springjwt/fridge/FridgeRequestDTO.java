package com.example.springjwt.fridge;

import java.time.LocalDate;

public class FridgeRequestDTO {
    private String ingredientName;
    private String storageArea;
    private LocalDate fridgeDate;
    private String dateOption;
    private Double quantity;
    private Double price;
    private UnitCategory unitCategory;
    private String unitDetail;
    private Long userId;  // 로그인한 사용자의 ID

    // 기본 생성자
    public FridgeRequestDTO() {}

    // 모든 필드를 위한 생성자 (선택사항)
    public FridgeRequestDTO(String ingredientName, String storageArea, LocalDate fridgeDate, String dateOption, Double quantity, Double price, UnitCategory unitCategory, String unitDetail, Long userId) {
        this.ingredientName = ingredientName;
        this.storageArea = storageArea;
        this.fridgeDate = fridgeDate;
        this.dateOption = dateOption;
        this.quantity = quantity;
        this.price = price;
        this.unitCategory = unitCategory;
        this.unitDetail = unitDetail;
        this.userId = userId;
    }

    // Getters & Setters
    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public String getStorageArea() {
        return storageArea;
    }

    public void setStorageArea(String storageArea) {
        this.storageArea = storageArea;
    }

    public LocalDate getFridgeDate() {
        return fridgeDate;
    }

    public void setFridgeDate(LocalDate fridgeDate) {
        this.fridgeDate = fridgeDate;
    }

    public String getDateOption() {
        return dateOption;
    }

    public void setDateOption(String dateOption) {
        this.dateOption = dateOption;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public UnitCategory getUnitCategory() {
        return unitCategory;
    }

    public void setUnitCategory(UnitCategory unitCategory) {
        this.unitCategory = unitCategory;
    }

    public String getUnitDetail() {
        return unitDetail;
    }

    public void setUnitDetail(String unitDetail) {
        this.unitDetail = unitDetail;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
