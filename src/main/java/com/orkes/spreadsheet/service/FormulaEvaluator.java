package com.orkes.spreadsheet.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Stack;

import static com.orkes.spreadsheet.util.Utils.isInteger;

public class FormulaEvaluator {
    protected static int evaluateFormula(String formula, SpreadsheetService service) {
        String[] tokens = formula.split("(?=[-+*/])|(?<=[-+*/])");
        Stack<String> operatorStack = new Stack<>();
        Stack<String> postfixStack = new Stack<>();

        for (String token : tokens) {
            token = token.trim();
            if (isOperator(token.charAt(0))) {
                while (!operatorStack.isEmpty() && hasHigherPrecedence(operatorStack.peek().charAt(0), token.charAt(0))) {
                    postfixStack.push(operatorStack.pop());
                }
                operatorStack.push(token);
            } else {
                postfixStack.push(token);
            }
        }

        while (!operatorStack.isEmpty()) {
            postfixStack.push(operatorStack.pop());
        }

        Stack<Integer> resultStack = new Stack<>();
        for (String token : postfixStack) {
            if (isOperator(token.charAt(0))) {
                // Need at least 2 operands for each operator
                if (resultStack.size() < 2) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid formula");
                }
                int operand2 = resultStack.pop();
                int operand1 = resultStack.pop();
                int result = applyOperator(operand1, operand2, token.charAt(0));
                resultStack.push(result);
            } else {
                if (isInteger(token)) {
                    resultStack.push(Integer.valueOf(token));
                } else {
                    resultStack.push(service.getCellValue(token));
                }
            }
        }
        return resultStack.pop();
    }

    private static boolean hasHigherPrecedence(char op1, char op2) {
        int precedence1 = getOperatorPrecedence(op1);
        int precedence2 = getOperatorPrecedence(op2);
        return precedence1 >= precedence2;
    }

    private static int getOperatorPrecedence(char operator) {
        return switch (operator) {
            case '+', '-' -> 1;
            case '*', '/' -> 2;
            default -> 0;
        };
    }

    private static int applyOperator(int operand1, int operand2, char operator) {
        switch (operator) {
            case '+' -> {
                return operand1 + operand2;
            }
            case '-' -> {
                return operand1 - operand2;
            }
            case '*' -> {
                return operand1 * operand2;
            }
            case '/' -> {
                if (operand2 == 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Division by zero");
                }
                return operand1 / operand2;
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid operator");
        }
    }

    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }
}
