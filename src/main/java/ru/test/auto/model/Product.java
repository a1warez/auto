package ru.test.auto.model;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity // Помечаем класс как JPA-сущность
@Table(name = "products") // Соответствует таблице "products" в БД
@Getter // Lombok: генерирует геттеры
@Setter // Lombok: генерирует сеттеры
public class Product {

    @Id // Первичный ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Автоматическое генерирование ID
    private Long id;

    private String name;
    private String description;
    private double price;
    private int stockQuantity;
    private String imageUrl; // URL картинки товара

    // Дополнительные поля для поиска/фильтрации
    private String brand;
    private String partNumber; // Артикул
    private String compatibleCars; // Простая строка для совместимых авто

    // Конструкторы (можно генерировать Lombok'ом или писать вручную)
    public Product() {}

    public Product(String name, String description, double price, int stockQuantity, String imageUrl, String brand, String partNumber, String compatibleCars) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
        this.brand = brand;
        this.partNumber = partNumber;
        this.compatibleCars = compatibleCars;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getCompatibleCars() {
        return compatibleCars;
    }

    public void setCompatibleCars(String compatibleCars) {
        this.compatibleCars = compatibleCars;
    }
}