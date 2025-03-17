package com.example.springjwt.fridge;

import com.example.springjwt.User.UserEntity;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fridge")
public class Fridge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ingredientName;

    @Column(nullable = false)
    private String storageArea;

    private LocalDate fridgeDate;
    private String dateOption;
    private Double quantity;
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnitCategory unitCategory;

    @Column(nullable = false)
    private String unitDetail;

    // UserEntity와의 다대일 관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 기본 생성자
    public Fridge() {}

    public void setUnitDetail(String unitDetail) {
        if (this.unitCategory != null && !this.unitCategory.isValidDetail(unitDetail)) {
            throw new IllegalArgumentException("유효하지 않은 단위 세부 항목입니다: " + unitDetail);
        }
        this.unitDetail = unitDetail;
    }

    public String getUnitDetail() {
        return unitDetail;
    }

    // 나머지 Getters & Setters
    public Long getId() {
        return id;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

}
