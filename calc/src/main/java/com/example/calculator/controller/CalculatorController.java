package com.example.calculator.controller;

import com.example.calculator.service.CalculatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
public class CalculatorController {

    @Autowired
    private CalculatorService calculatorService;

    @GetMapping("/")
    public String showCalculator(Model model) {
        model.addAttribute("result", "");
        model.addAttribute("error", "");
        model.addAttribute("number1", "");
        model.addAttribute("number2", "");
        model.addAttribute("operation", "add");
        return "calculator";
    }

    @PostMapping("/calculate")
    public String calculate(
            @RequestParam String number1,
            @RequestParam String number2,
            @RequestParam String operation,
            Model model) {

        try {
            String result = calculatorService.calculate(number1, number2, operation);
            model.addAttribute("result", result);
            model.addAttribute("error", "");
        } catch (IllegalArgumentException e) {
            model.addAttribute("result", "");
            model.addAttribute("error", e.getMessage());
        }

        model.addAttribute("number1", number1);
        model.addAttribute("number2", number2);
        model.addAttribute("operation", operation);

        return "calculator";
    }
}