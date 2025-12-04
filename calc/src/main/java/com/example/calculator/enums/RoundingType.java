package com.example.calculator.enums;

public enum RoundingType {
    MATHEMATICAL("Математическое округление"),
    BANKERS("Бухгалтерское (банковское) округление"),
    TRUNCATE("Усечение (убрать дробную часть)");

    private final String description;

    RoundingType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}