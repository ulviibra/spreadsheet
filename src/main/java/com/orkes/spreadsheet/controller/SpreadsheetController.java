package com.orkes.spreadsheet.controller;

import com.orkes.spreadsheet.service.SpreadsheetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/spreadsheet")
public class SpreadsheetController {
    private final SpreadsheetService spreadsheetService;

    @Autowired
    public SpreadsheetController(SpreadsheetService spreadsheetService) {
        this.spreadsheetService = spreadsheetService;
    }

    @PostMapping("/cell/{id}")
    public ResponseEntity<Void> setCellValue(@NonNull @PathVariable String id, @NonNull @RequestParam String value) {
        spreadsheetService.setCellValue(id, value);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/cell/{id}")
    public ResponseEntity<Integer> getCellValue(@NonNull @PathVariable String id) {
        int value = spreadsheetService.getCellValue(id);
        return ResponseEntity.ok(value);
    }
}
