package com.dieu_phoi_xe.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dieu_phoi_xe.dto.sheet.SheetRequest;
import com.dieu_phoi_xe.util.Util;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/appsheet")
@CrossOrigin(origins = "https://pt6402.github.io/")
@Slf4j
public class GGSheetController {

    @GetMapping
    public ResponseEntity<?> firstTest() {
        String spreadSheet = "1_a3iRSlDI8vK64xaX2I8h1vgeXLz30FBJnmjFHnTa38";
        new Util().getInfo(spreadSheet);

        return ResponseEntity.ok("first test demo");
    }

    @PostMapping
    public ResponseEntity<?> registerForm(@RequestBody SheetRequest request) {
        String spreadSheet = "1_a3iRSlDI8vK64xaX2I8h1vgeXLz30FBJnmjFHnTa38";
        String range = "Trang tính1!A1";
        String valueInputOption = "RAW";
        List<List<Object>> rows = new ArrayList<>();
        List<Object> columns = new ArrayList<>();
        columns.add(request.getId());
        columns.add(request.getFirstName());
        columns.add(request.getLastName());
        columns.add(request.getEmail());
        columns.add(request.getCountry());
        rows.add(columns);
        try {
            new Util().appendValue(spreadSheet, range, valueInputOption, rows);
            return ResponseEntity.ok(request);
        } catch (IOException e) {
            log.info(e.getMessage());
            return new ResponseEntity<>("register fail", HttpStatus.BAD_REQUEST);
        }
    }

}
