package com.orkes.spreadsheet.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.orkes.spreadsheet.util.Utils.isInteger;

@Service
public class SpreadsheetService {
    private final Map<String, Integer> cellValues = new ConcurrentHashMap<>();

    public void setCellValue(String id, String value) {
        if (id == null || value == null || id.isEmpty() || value.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
        if (isInteger(value)) {
            cellValues.put(id, Integer.parseInt(value));
        } else {
            cellValues.put(id, FormulaEvaluator.evaluateFormula(value, this));
        }
    }

    public int getCellValue(String id) {
        if (id == null || id.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request");
        }
        if (cellValues.containsKey(id)) {
            return cellValues.get(id);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cell not found");
    }
}
