package com.example.calculator.service;

import com.example.calculator.enums.Operation;
import com.example.calculator.enums.RoundingType;
import com.example.calculator.dto.CalculationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class CalculatorService {

    private static final BigDecimal MIN_VALUE = new BigDecimal("-1000000000000.000000");
    private static final BigDecimal MAX_VALUE = new BigDecimal("1000000000000.000000");
    private static final int MAX_INPUT_SCALE = 10;

    private final RoundingService roundingService;
    private static final Pattern EXPONENTIAL_PATTERN = Pattern.compile("[eE]");

    @Autowired
    public CalculatorService(RoundingService roundingService) {
        this.roundingService = roundingService;
    }

    public CalculationResult calculate(CalculationRequest request) {
        try {
            // Валидация и парсинг входных данных
            BigDecimal num1 = parseAndValidateNumber(request.getNumber1(), "Первое число");
            BigDecimal num2 = parseAndValidateNumber(request.getNumber2(), "Второе число");
            BigDecimal num3 = parseAndValidateNumber(request.getNumber3(), "Третье число");
            BigDecimal num4 = parseAndValidateNumber(request.getNumber4(), "Четвертое число");

            // Получаем операции (по умолчанию сложение)
            Operation op1 = request.getOp1() != null ? request.getOp1() : Operation.ADD;
            Operation op2 = request.getOp2() != null ? request.getOp2() : Operation.ADD;
            Operation op3 = request.getOp3() != null ? request.getOp3() : Operation.ADD;

            // Шаг 1: Вычисляем выражение в скобках (num2 op2 num3)
            BigDecimal inBrackets = calculateOperation(num2, num3, op2);
            inBrackets = roundingService.roundIntermediate(inBrackets);
            validateRange(inBrackets, "Промежуточный результат в скобках");

            BigDecimal result;

            // Определяем порядок вычислений в зависимости от приоритетов
            if (isHighPriority(op3) && !isHighPriority(op1)) {
                // Если op3 (* или /) и op1 (+ или -), то op3 выполняется раньше
                // Вычисляем: num1 op1 ((num2 op2 num3) op3 num4)
                BigDecimal rightPart = calculateOperation(inBrackets, num4, op3);
                rightPart = roundingService.roundIntermediate(rightPart);
                validateRange(rightPart, "Промежуточный результат (правая часть)");

                result = calculateOperation(num1, rightPart, op1);
            } else {
                // Иначе стандартный порядок: ((num1 op1 (num2 op2 num3)) op3 num4)
                BigDecimal leftPart = calculateOperation(num1, inBrackets, op1);
                leftPart = roundingService.roundIntermediate(leftPart);
                validateRange(leftPart, "Промежуточный результат (левая часть)");

                result = calculateOperation(leftPart, num4, op3);
            }

            result = roundingService.roundIntermediate(result);
            validateRange(result, "Финальный результат");

            // Округление до целых по выбранному методу
            BigDecimal roundedResult = roundingService.roundFinal(result,
                    request.getRoundingType() != null ? request.getRoundingType() : RoundingType.MATHEMATICAL);

            return new CalculationResult(
                    formatNumber(result),
                    formatNumber(roundedResult),
                    null
            );

        } catch (IllegalArgumentException e) {
            return new CalculationResult(null, null, e.getMessage());
        } catch (ArithmeticException e) {
            return new CalculationResult(null, null, "Арифметическая ошибка: " + e.getMessage());
        }
    }

    private boolean isHighPriority(Operation op) {
        return op == Operation.MULTIPLY || op == Operation.DIVIDE;
    }

    private BigDecimal parseAndValidateNumber(String numberStr, String fieldName) {
        if (numberStr == null || numberStr.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Удаляем все пробелы из числа (поддержка формата с пробелами: 986 282 584 876,635029)
        String cleanedNumber = numberStr.trim().replace(" ", "");

        String normalized = normalizeNumber(cleanedNumber);

        // Проверка на экспоненциальную форму
        if (EXPONENTIAL_PATTERN.matcher(normalized).find()) {
            throw new IllegalArgumentException(fieldName + ": экспоненциальная нотация не поддерживается");
        }

        try {
            BigDecimal number = new BigDecimal(normalized);

            // Проверка масштаба
            if (number.scale() > MAX_INPUT_SCALE) {
                throw new IllegalArgumentException(fieldName +
                        ": слишком много знаков после запятой (максимум " + MAX_INPUT_SCALE + ")");
            }

            // Проверка диапазона
            validateRange(number, fieldName);

            return number;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректный формат " + fieldName.toLowerCase() + ": " + numberStr);
        }
    }

    private BigDecimal calculateOperation(BigDecimal a, BigDecimal b, Operation operation) {
        switch (operation) {
            case ADD:
                return a.add(b);
            case SUBTRACT:
                return a.subtract(b);
            case MULTIPLY:
                return a.multiply(b);
            case DIVIDE:
                if (b.compareTo(BigDecimal.ZERO) == 0) {
                    throw new ArithmeticException("Деление на ноль");
                }
                // Деление с округлением до 10 знаков для промежуточных вычислений
                return a.divide(b, 6, RoundingMode.HALF_UP);
            default:
                throw new IllegalArgumentException("Неизвестная операция");
        }
    }

    private String normalizeNumber(String number) {
        return number.replace(',', '.');
    }

    private void validateRange(BigDecimal number, String fieldName) {
        if (number.compareTo(MIN_VALUE) < 0 || number.compareTo(MAX_VALUE) > 0) {
            throw new IllegalArgumentException(fieldName +
                    " выходит за допустимый диапазон (±1,000,000,000,000.000000)");
        }
    }

    private String formatNumber(BigDecimal number) {
        if (number == null) return "0";

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator(' '); // Пробел как разделитель тысяч

        DecimalFormat df = new DecimalFormat();
        df.setDecimalFormatSymbols(symbols);
        df.setGroupingUsed(true); // Включаем разделители тысяч
        df.setGroupingSize(3);
        df.setMaximumFractionDigits(10);
        df.setMinimumFractionDigits(0);

        String formatted = df.format(number);
        if (EXPONENTIAL_PATTERN.matcher(formatted).find()) {
            // Если все же получили экспоненту, форматируем вручную
            String plain = number.toPlainString();
            // Добавляем пробелы как разделители тысяч
            return formatWithSpacesManual(plain);
        }

        return formatted;
    }

    // Ручное форматирование с пробелами
    private String formatWithSpacesManual(String numberStr) {
        if (numberStr == null || numberStr.isEmpty()) return "0";

        String[] parts = numberStr.split("\\.");
        String integerPart = parts[0];
        String decimalPart = parts.length > 1 ? "." + parts[1] : "";

        // Добавляем пробелы каждые 3 цифры справа налево
        StringBuilder formatted = new StringBuilder();
        int count = 0;

        for (int i = integerPart.length() - 1; i >= 0; i--) {
            char c = integerPart.charAt(i);
            if (c == '-') {
                formatted.append(c);
                break;
            }
            if (count > 0 && count % 3 == 0) {
                formatted.append(' ');
            }
            formatted.append(c);
            count++;
        }

        return formatted.reverse().toString() + decimalPart;
    }

    // Вложенный класс для результата
    public static class CalculationResult {
        private final String result;
        private final String roundedResult;
        private final String error;

        public CalculationResult(String result, String roundedResult, String error) {
            this.result = result;
            this.roundedResult = roundedResult;
            this.error = error;
        }

        public String getResult() { return result; }
        public String getRoundedResult() { return roundedResult; }
        public String getError() { return error; }
        public boolean hasError() { return error != null; }
    }
}