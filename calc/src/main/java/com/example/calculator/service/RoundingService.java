package com.example.calculator.service;

import com.example.calculator.enums.RoundingType;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class RoundingService {

    private static final int INTERMEDIATE_SCALE = 10;
    private static final int FINAL_SCALE = 0; // Округление до целых

    public BigDecimal roundIntermediate(BigDecimal value) {
        if (value == null) return BigDecimal.ZERO;
        return value.setScale(INTERMEDIATE_SCALE, RoundingMode.HALF_UP);
    }

    public BigDecimal roundFinal(BigDecimal value, RoundingType roundingType) {
        if (value == null) return BigDecimal.ZERO;

        switch (roundingType) {
            case MATHEMATICAL:
                return value.setScale(FINAL_SCALE, RoundingMode.HALF_UP);
            case BANKERS:
                return bankersRounding(value);
            case TRUNCATE:
                return truncate(value);
            default:
                return value.setScale(FINAL_SCALE, RoundingMode.HALF_UP);
        }
    }

    private BigDecimal bankersRounding(BigDecimal value) {
        // Банковское округление (Round half to even)
        BigDecimal[] divRem = value.divideAndRemainder(BigDecimal.ONE);
        BigDecimal integerPart = divRem[0];
        BigDecimal fractionalPart = divRem[1].abs();

        // Если дробная часть точно 0.5
        if (fractionalPart.compareTo(new BigDecimal("0.5")) == 0) {
            // Проверяем четность целой части
            if (integerPart.remainder(new BigDecimal("2")).compareTo(BigDecimal.ZERO) == 0) {
                return integerPart; // К четному
            } else {
                // Если целая часть отрицательная
                if (value.compareTo(BigDecimal.ZERO) < 0) {
                    return integerPart.subtract(BigDecimal.ONE); // К ближайшему четному
                } else {
                    return integerPart.add(BigDecimal.ONE); // К ближайшему четному
                }
            }
        } else {
            // Обычное математическое округление
            return value.setScale(FINAL_SCALE, RoundingMode.HALF_UP);
        }
    }

    private BigDecimal truncate(BigDecimal value) {
        // Просто убираем дробную часть
        if (value.compareTo(BigDecimal.ZERO) >= 0) {
            return value.setScale(FINAL_SCALE, RoundingMode.DOWN);
        } else {
            return value.setScale(FINAL_SCALE, RoundingMode.UP);
        }
    }
}