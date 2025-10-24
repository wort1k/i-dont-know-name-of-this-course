package com.example.calculator.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class CalculatorService {

    private static final BigDecimal MIN_VALUE = new BigDecimal("-1000000000000.000000");
    private static final BigDecimal MAX_VALUE = new BigDecimal("1000000000000.000000");
    private static final int MAX_SCALE = 6;

    // Регулярное выражение для проверки на экспоненциальную форму
    private static final Pattern EXPONENTIAL_PATTERN = Pattern.compile("[eE]");
    // Регулярное выражение для валидации допустимого формата числа
    private static final Pattern VALID_NUMBER_PATTERN =
            Pattern.compile("^-?\\d{1,15}([.,]\\d{0,6})?$");

    public String calculate(String num1Str, String num2Str, String operation) {
        // Проверка на пустые значения
        if (num1Str == null || num1Str.trim().isEmpty()) {
            throw new IllegalArgumentException("Первое число не может быть пустым");
        }
        if (num2Str == null || num2Str.trim().isEmpty()) {
            throw new IllegalArgumentException("Второе число не может быть пустым");
        }

        // Нормализация разделителей (запятая -> точка)
        String normalizedNum1 = normalizeNumber(num1Str.trim());
        String normalizedNum2 = normalizeNumber(num2Str.trim());

        // Проверка на экспоненциальную форму
        validateNoExponentialNotation(normalizedNum1, "первое число");
        validateNoExponentialNotation(normalizedNum2, "второе число");

        // Проверка формата числа
        validateNumberFormat(normalizedNum1, "первое число");
        validateNumberFormat(normalizedNum2, "второе число");

        BigDecimal num1 = parseNumber(normalizedNum1, "первое число");
        BigDecimal num2 = parseNumber(normalizedNum2, "второе число");

        validateRange(num1, "первое число");
        validateRange(num2, "второе число");

        BigDecimal result;
        switch (operation) {
            case "add":
                result = num1.add(num2);
                break;
            case "subtract":
                result = num1.subtract(num2);
                break;
            default:
                throw new IllegalArgumentException("Неизвестная операция: " + operation);
        }

        validateRange(result, "результат");
        return formatResult(result);
    }

    private String normalizeNumber(String number) {
        // Заменяем запятую на точку
        return number.replace(',', '.');
    }

    private void validateNoExponentialNotation(String number, String fieldName) {
        if (EXPONENTIAL_PATTERN.matcher(number).find()) {
            throw new IllegalArgumentException(
                    fieldName + ": экспоненциальная нотация (например, 123e+2) не поддерживается. " +
                            "Используйте обычный формат числа."
            );
        }
    }

    private void validateNumberFormat(String number, String fieldName) {
        if (!VALID_NUMBER_PATTERN.matcher(number).matches()) {
            throw new IllegalArgumentException(
                    fieldName + ": недопустимый формат числа. " +
                            "Допустимы только цифры, знак минуса и разделитель дробной части (точка или запятая). " +
                            "Максимум 15 цифр в целой части и 6 цифр в дробной."
            );
        }
    }

    private BigDecimal parseNumber(String numberStr, String fieldName) {
        try {
            return new BigDecimal(numberStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректный формат " + fieldName + ": " + numberStr);
        }
    }

    private void validateRange(BigDecimal number, String fieldName) {
        if (number.compareTo(MIN_VALUE) < 0 || number.compareTo(MAX_VALUE) > 0) {
            throw new IllegalArgumentException(
                    fieldName + " выходит за допустимый диапазон. " +
                            "Допустимый диапазон: от -1,000,000,000,000.000000 до +1,000,000,000,000.000000"
            );
        }

        // Проверка масштаба (количества знаков после запятой)
        if (number.scale() > MAX_SCALE) {
            throw new IllegalArgumentException(
                    fieldName + " имеет слишком много знаков после запятой. " +
                            "Максимум 6 знаков после запятой."
            );
        }
    }

    private String formatResult(BigDecimal result) {
        // Используем форматирование с точкой как разделителем дробной части
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');

        DecimalFormat df = new DecimalFormat();
        df.setDecimalFormatSymbols(symbols);
        df.setGroupingUsed(false); // Без разделителей тысяч
        df.setMaximumFractionDigits(MAX_SCALE);
        df.setMinimumFractionDigits(0);
        df.setGroupingUsed(false);

        // Убеждаемся, что результат не в экспоненциальной форме
        String formatted = df.format(result);

        // Дополнительная проверка на случай, если DecimalFormat все же вернет экспоненциальную форму
        if (EXPONENTIAL_PATTERN.matcher(formatted).find()) {
            // Используем toPlainString() для гарантированного получения числа без экспоненты
            formatted = result.toPlainString();
        }

        return formatted;
    }
}