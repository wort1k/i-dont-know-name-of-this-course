package com.example.calculator.dto;

import com.example.calculator.enums.Operation;
import com.example.calculator.enums.RoundingType;

public class CalculationRequest {
    private String number1;
    private String number2;
    private String number3;
    private String number4;

    private Operation op1;  // между number1 и (number2 op2 number3)
    private Operation op2;  // между number2 и number3
    private Operation op3;  // между (number2 op2 number3) и number4

    private RoundingType roundingType;

    // Геттеры и сеттеры
    public String getNumber1() { return number1; }
    public void setNumber1(String number1) { this.number1 = number1; }

    public String getNumber2() { return number2; }
    public void setNumber2(String number2) { this.number2 = number2; }

    public String getNumber3() { return number3; }
    public void setNumber3(String number3) { this.number3 = number3; }

    public String getNumber4() { return number4; }
    public void setNumber4(String number4) { this.number4 = number4; }

    public Operation getOp1() { return op1; }
    public void setOp1(Operation op1) { this.op1 = op1; }

    public Operation getOp2() { return op2; }
    public void setOp2(Operation op2) { this.op2 = op2; }

    public Operation getOp3() { return op3; }
    public void setOp3(Operation op3) { this.op3 = op3; }

    public RoundingType getRoundingType() { return roundingType; }
    public void setRoundingType(RoundingType roundingType) {
        this.roundingType = roundingType;
    }

    // Для удобства
    public String getOp1Symbol() { return op1 != null ? op1.getSymbol() : "+"; }
    public String getOp2Symbol() { return op2 != null ? op2.getSymbol() : "+"; }
    public String getOp3Symbol() { return op3 != null ? op3.getSymbol() : "+"; }
}