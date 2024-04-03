package com.dieu_phoi_xe.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.InsertDimensionRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Util {

    private static final String APPLICATION_NAME = "ApiAppsheet";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    // ===================================
    private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    // =====================================
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = Util.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    private static Sheets getService() {
        try {
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
            return service;
        } catch (GeneralSecurityException | IOException e) {
            log.info(e.getMessage());
            return null;
        }
    }
// =======================

    public ValueRange readValue(String spreadsheetId, String range) throws IOException {
        Sheets service = getService();
        ValueRange result = null;
        try {
            result = service.spreadsheets().values().get(spreadsheetId, range).execute();
            int numRows = result.getValues() != null ? result.getValues().size() : 0;
            System.out.printf("%d rows retrieved.", numRows);
        } catch (GoogleJsonResponseException e) {
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 404) {
                System.out.printf("Spreadsheet not found with id '%s'.\n", spreadsheetId);
            } else {
                throw e;
            }
        }
        return result;
    }

    public String createSpreadsheet(String title_spreadSheet, String title_sheet) throws IOException {

        Sheets service = getService();
        Spreadsheet spreadsheet = new Spreadsheet()
                .setProperties(new SpreadsheetProperties().setTitle(title_sheet))
                .setSheets(Arrays.asList(new Sheet().setProperties(new SheetProperties().setTitle(title_sheet))));

        try {
            if (service != null) {
                spreadsheet = service.spreadsheets().create(spreadsheet).execute();
                return "ID: " + spreadsheet.getSpreadsheetId() + "-" + "URL: " + spreadsheet.getSpreadsheetUrl();
            }
            return null;
        } catch (IOException e) {
            log.info(e.getMessage());
            return null;
        }
    }

    public UpdateValuesResponse updateValue(String sheetId, String range, String valueInputOption, List<List<Object>> values) throws IOException {
        Sheets service = getService();
        UpdateValuesResponse result = null;
        try {
            ValueRange body = new ValueRange()
                    .setValues(values);
            result = service.spreadsheets().values().update(sheetId, range, body)
                    .setValueInputOption(valueInputOption)
                    .execute();
            System.out.printf("%d cells updated.", result.getUpdatedCells());
        } catch (GoogleJsonResponseException e) {
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 404) {
                System.out.printf("Spreadsheet not found with id '%s'.\n", sheetId);
            } else {
                throw e;
            }
        }
        return result;
    }

    public AppendValuesResponse appendValue(String sheetId, String range, String valueInputOption, List<List<Object>> values) throws IOException {
        Sheets service = getService();
        AppendValuesResponse result = null;
        try {
            ValueRange body = new ValueRange()
                    .setValues(values);
            result = service.spreadsheets().values().append(sheetId, range, body)
                    .setValueInputOption(valueInputOption)
                    .execute();
            System.out.printf("%d cells updated.", result.getUpdates().getUpdatedCells());
        } catch (GoogleJsonResponseException e) {
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 404) {
                System.out.printf("Spreadsheet not found with id '%s'.\n", sheetId);
            } else {
                throw e;
            }
        }
        return result;
    }

    public void DeleteDimension(String spreadSheetId, int sheetId, String DimensionRange) {
        Sheets service = getService();
        DeleteDimensionRequest deleteRequest = new DeleteDimensionRequest()
                .setRange(
                        new DimensionRange()
                                .setSheetId(sheetId)
                                .setDimension(DimensionRange)
                                .setStartIndex(0)
                                .setEndIndex(2));

        List<Request> requests = new ArrayList<>();
        requests.add(new Request().setDeleteDimension(deleteRequest));

        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
        try {
            service.spreadsheets().batchUpdate(spreadSheetId, body).execute();
        } catch (IOException e) {
            // TODO Auto-generated catch block

        }
    }

    public void AddDimension(String spreadSheetId, int sheetId, String dimensionRange) {
        Sheets service = getService();
        InsertDimensionRequest insertRequest = new InsertDimensionRequest()
                .setRange(
                        new DimensionRange()
                                .setSheetId(sheetId)
                                .setDimension(dimensionRange)
                                .setStartIndex(0)
                                .setEndIndex(1));

        List<Request> requests = new ArrayList<>();
        requests.add(new Request().setInsertDimension(insertRequest));

        BatchUpdateSpreadsheetRequest body = new BatchUpdateSpreadsheetRequest().setRequests(requests);
        try {
            service.spreadsheets().batchUpdate(spreadSheetId, body).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getInfo(String spreadSheetId) {
        Sheets service = getService();
        Spreadsheet spreadsheet = null;
        try {
            spreadsheet = service.spreadsheets().get(spreadSheetId).execute();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Hiển thị thông tin tổng quan của bảng tính
        System.out.println("Spreadsheet ID: " + spreadsheet.getSpreadsheetId());
        System.out.println("Spreadsheet URL: " + spreadsheet.getSpreadsheetUrl());
        System.out.println("Number of sheets: " + spreadsheet.getSheets().size());

        // Hiển thị thông tin từng tờ tính
        List<Sheet> sheets = spreadsheet.getSheets();
        for (Sheet sheet : sheets) {
            System.out.println("Sheet Title: " + sheet.getProperties().getTitle());
            System.out.println("Sheet ID: " + sheet.getProperties().getSheetId());
            System.out.println("Grid Properties: ");
            System.out.println("\tRow count: " + sheet.getProperties().getGridProperties().getRowCount());
            System.out.println("\tColumn count: " + sheet.getProperties().getGridProperties().getColumnCount());
            System.out.println("------------------------------------------------");
            ValueRange response = null;
            try {
                response = service.spreadsheets().values()
                        .get(spreadSheetId, sheet.getProperties().getTitle())
                        .execute();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            List<List<Object>> values = response.getValues();
            if (values != null && !values.isEmpty()) {
                System.out.println("Data:");
                int rowCount = 0;
                int columnCount = values.get(0).size();
                for (List<Object> row : values) {
                    rowCount++;
                    for (Object cell : row) {
                        System.out.print(cell + "\t");
                    }
                    System.out.println();
                }
                System.out.println("Number of data rows: " + rowCount);
                int nonEmptyColumnCount = 0;
                for (int i = 0; i < columnCount; i++) {
                    if (!isColumnEmpty(values, i)) {
                        nonEmptyColumnCount++;
                    }
                }
                System.out.println("Number of data columns: " + (columnCount - (columnCount - nonEmptyColumnCount)));
                // System.out.println("Number of data columns: " + );
            } else {
                System.out.println("No data found.");
            }

            System.out.println("------------------------------------------------");
        }

    }

    private boolean isColumnEmpty(List<List<Object>> values, int columnIndex) {
        for (List<Object> row : values) {
            if (row.size() <= columnIndex || row.get(columnIndex) == null
                    || row.get(columnIndex).toString().isEmpty()) {
                return true;
            }
        }
        return false;
    }

}
