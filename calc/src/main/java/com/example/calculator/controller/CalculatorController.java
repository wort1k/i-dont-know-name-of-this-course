package com.example.calculator.controller;

import com.example.calculator.dto.CalculationRequest;
import com.example.calculator.enums.Operation;
import com.example.calculator.enums.RoundingType;
import com.example.calculator.service.CalculatorService;
import com.example.calculator.service.CalculatorService.CalculationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
public class CalculatorController {

    private final CalculatorService calculatorService;

    @Autowired
    public CalculatorController(CalculatorService calculatorService) {
        this.calculatorService = calculatorService;
    }

    @GetMapping("/")
    public String showCalculator(Model model) {
        // Инициализация формы с значениями по умолчанию
        CalculationRequest request = new CalculationRequest();
        request.setNumber1("0");
        request.setNumber2("0");
        request.setNumber3("0");
        request.setNumber4("0");
        request.setOp1(Operation.ADD);
        request.setOp2(Operation.ADD);
        request.setOp3(Operation.ADD);
        request.setRoundingType(RoundingType.MATHEMATICAL);

        model.addAttribute("request", request);
        model.addAttribute("operations", Operation.values());
        model.addAttribute("roundingTypes", RoundingType.values());
        model.addAttribute("result", null);

        return "calculator";
    }

    @PostMapping("/calculate")
    public String calculate(
            @ModelAttribute CalculationRequest request,
            Model model) {

        CalculationResult result = calculatorService.calculate(request);

        if (result.hasError()) {
            model.addAttribute("error", result.getError());
        } else {
            model.addAttribute("result", result.getResult());
            model.addAttribute("roundedResult", result.getRoundedResult());
        }

        model.addAttribute("request", request);
        model.addAttribute("operations", Operation.values());
        model.addAttribute("roundingTypes", RoundingType.values());

        return "calculator";
    }
}