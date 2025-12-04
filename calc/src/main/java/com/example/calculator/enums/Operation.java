package com.example.calculator.enums;

public enum Operation {
    ADD("+", "Сложение"),
    SUBTRACT("-", "Вычитание"),
    MULTIPLY("*", "Умножение"),
    DIVIDE("/", "Деление");

    private final String symbol;
    private final String description;

    Operation(String symbol, String description) {
        this.symbol = symbol;
        this.description = description;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDescription() {
        return description;
    }

    public static Operation fromSymbol(String symbol) {
        for (Operation op : values()) {
            if (op.getSymbol().equals(symbol)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Неизвестная операция: " + symbol);
    }
}
