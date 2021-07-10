package com.waya.wayaauthenticationservice.util;

import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.pojo.BaseUserPojo;
import com.waya.wayaauthenticationservice.pojo.BulkCorporateUserCreationDTO;
import com.waya.wayaauthenticationservice.pojo.BulkPrivateUserCreationDTO;
import com.waya.wayaauthenticationservice.pojo.CorporateUserPojo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ExcelHelper {

    private static DataFormatter dataFormatter = new DataFormatter();

    public static String[] TYPE = {"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel"};
    public static List<String> PRIVATE_USER_HEADERS = Arrays.asList("FIRSTNAME", "SURNAME", "PHONE_NUMBER", "EMAIL" );
    public static List<String> CORPORATE_USER_HEADERS = Arrays.asList("FIRSTNAME", "SURNAME", "PHONE_NUMBER", "EMAIL",
            "OFFICE_ADDRESS", "CITY", "STATE", "ORG_NAME", "ORG_EMAIL",
            "ORG_PHONE", "ORG_TYPE", "BUSINESS_TYPE");

    static String SHEET = "Users";
    static Pattern alphabetsPattern = Pattern.compile("^[a-zA-Z]*$");
    static Pattern numericPattern = Pattern.compile("^[0-9]*$");
    static Pattern emailPattern = Pattern.compile("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\." + "[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
            + "(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?");

    public static boolean hasExcelFormat(MultipartFile file) {
        if (!Arrays.asList(TYPE).contains(file.getContentType())) {
            return false;
        }
        return true;
    }

    public static BulkPrivateUserCreationDTO excelToPrivateUserPojo(InputStream is, String fileName){

        try(Workbook workbook = getWorkBook(is, fileName)) {
            if(workbook == null){
                throw new CustomException("Invalid Excel File Check Extension", HttpStatus.BAD_REQUEST);
            }
            Set<BaseUserPojo> models = new HashSet<>();

            Sheet sheet = workbook.getSheet(SHEET);
            if(sheet == null) throw new CustomException("Invalid Excel File Format Passed, Check Sheet Name", HttpStatus.BAD_REQUEST);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            while (rows.hasNext()){
                Row currentRow = rows.next();
                Iterator<Cell> cellsInRow = currentRow.iterator();
                CorporateUserPojo pojo = new CorporateUserPojo();

                // If First Cell is empty break from loop
                if (currentRow == null || isCellEmpty(currentRow.getCell(0))) {
                    break;
                }

                // Skip header After Check of Header Formats
                if (rowNumber == 0) {
                    List<String> excelColNames = new ArrayList<>();
                    int i = 0;
                    while (cellsInRow.hasNext()) {
                        Cell cell = cellsInRow.next();
                        String cellValue = dataFormatter.formatCellValue(cell).trim().toUpperCase();
                        excelColNames.add(cellValue);
                        i++;
                        if (i == PRIVATE_USER_HEADERS.size()) {
                            break;
                        }
                    }
                    boolean value = checkExcelFileValidity(PRIVATE_USER_HEADERS, excelColNames);
                    if (!value) {
                        String errorMessage = "Failure, Incorrect File Format";
                        throw new CustomException(errorMessage, HttpStatus.BAD_REQUEST);
                    }
                    rowNumber++;
                    continue;
                }

                int cellIdx = 0;
                while (cellsInRow.hasNext()){
                    Cell cell = cellsInRow.next();
                    switch (cellIdx) {
                        case 0:
                            pojo.setFirstName(validateAndPassStringValue(cell, cellIdx, rowNumber));
                            break;
                        case 1:
                            pojo.setSurname(validateAndPassStringValue(cell, cellIdx, rowNumber));
                            break;
                        case 2:
                            pojo.setPhoneNumber(validateStringNumericOnly(cell, cellIdx, rowNumber));
                            break;
                        case 3:
                            pojo.setEmail(validateStringIsEmail(cell, cellIdx, rowNumber));
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                models.add(pojo);
                rowNumber++;
            }
            return new BulkPrivateUserCreationDTO(models);
        }catch (Exception ex){
            throw new CustomException(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public static BulkCorporateUserCreationDTO excelToCorporatePojo(InputStream is, String fileName){

        try(Workbook workbook = getWorkBook(is, fileName)) {
            if(workbook == null){
                throw new CustomException("Invalid Excel File Format Passed, Check Extension", HttpStatus.BAD_REQUEST);
            }
            Set<CorporateUserPojo> models = new HashSet<>();

            Sheet sheet = workbook.getSheet(SHEET);
            if(sheet == null) throw new CustomException("Invalid Excel File Format Passed, Check Sheet Name", HttpStatus.BAD_REQUEST);
            Iterator<Row> rows = sheet.iterator();
            int rowNumber = 0;
            while (rows.hasNext()){
                Row currentRow = rows.next();
                Iterator<Cell> cellsInRow = currentRow.iterator();
                CorporateUserPojo pojo = new CorporateUserPojo();

                // If First Cell is empty break from loop
                if (currentRow == null || isCellEmpty(currentRow.getCell(0))) {
                    break;
                }

                // skip header After Check of Header Formats
                if (rowNumber == 0) {
                    List<String> excelColNames = new ArrayList<>();
                    int i = 0;
                    while (cellsInRow.hasNext()) {
                        Cell cell = cellsInRow.next();
                        String cellValue = dataFormatter.formatCellValue(cell).trim().toUpperCase();
                        excelColNames.add(cellValue);
                        i++;
                        if (i == CORPORATE_USER_HEADERS.size()) {
                            break;
                        }
                    }
                    boolean value = checkExcelFileValidity(CORPORATE_USER_HEADERS, excelColNames);
                    if (!value) {
                        String errorMessage = "Failure, Incorrect File Format";
                        throw new CustomException(errorMessage, HttpStatus.BAD_REQUEST);
                    }
                    rowNumber++;
                    continue;
                }

                int cellIdx = 0;
                while (cellsInRow.hasNext()){
                    Cell cell = cellsInRow.next();
                    switch (cellIdx) {
                        case 0:
                            pojo.setFirstName(validateAndPassStringValue(cell, cellIdx, rowNumber));
                            break;
                        case 1:
                            pojo.setSurname(validateAndPassStringValue(cell, cellIdx, rowNumber));
                            break;
                        case 2:
                            pojo.setPhoneNumber(validateStringNumericOnly(cell, cellIdx, rowNumber));
                            break;
                        case 3:
                            pojo.setEmail(validateStringIsEmail(cell, cellIdx, rowNumber));
                            break;
                        case 4:
                            pojo.setOfficeAddress(defaultStringCell(cell));
                            break;
                        case 5:
                            pojo.setCity(defaultStringCell(cell));
                            break;
                        case 6:
                            pojo.setState(defaultStringCell(cell));
                            break;
                        case 7:
                            pojo.setOrgName(defaultStringCell(cell));
                            break;
                        case 8:
                            pojo.setOrgEmail(defaultStringCell(cell));
                            break;
                        case 9:
                            pojo.setOrgPhone(defaultStringCell(cell));
                            break;
                        case 10:
                           pojo.setOrgType(defaultStringCell(cell));
                            break;
                        case 11:
                            pojo.setBusinessType(validateAndPassStringValue(cell, cellIdx, rowNumber));
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                models.add(pojo);
                rowNumber++;
            }
            workbook.close();
            return new BulkCorporateUserCreationDTO(models);
        }catch (Exception ex){
            throw new CustomException(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public static ByteArrayInputStream createExcelSheet(List<String> HEADERS){
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            Sheet sheet = workbook.createSheet(SHEET);
            // Create Header
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillPattern(FillPatternType.NO_FILL);

            XSSFFont font = ((XSSFWorkbook) workbook).createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 13);
            font.setBold(true);
            headerStyle.setFont(font);

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERS.size(); col++) {
                sheet.autoSizeColumn(col);
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERS.get(col));
                cell.setCellStyle(headerStyle);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
           throw new CustomException("Error in Forming Excel: " + e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }

    private static Workbook getWorkBook(InputStream is, String fileName) {
        Workbook workbook = null;
        try {
            String extension = fileName.substring(fileName.lastIndexOf("."));
            if(extension.equalsIgnoreCase(".xls")){
                workbook = new HSSFWorkbook(is);
            }
            else if(extension.equalsIgnoreCase(".xlsx")){
                workbook = new XSSFWorkbook(is);
            }
        }
        catch(Exception ex) {
            log.error("An Error has Occurred while Getting WorkBook File: {}", ex.getMessage());
        }
        return workbook;
    }

    private static boolean isCellEmpty(final Cell cell) {
        String cellValue = dataFormatter.formatCellValue(cell).trim();
        return cellValue.isEmpty();
    }

    private static boolean checkExcelFileValidity(List<String> one, List<String> two) {
        if (one == null && two == null)
            return true;

        if ((one == null && two != null) || (one != null && two == null) || (one.size() != two.size())) {
            return false;
        }
        one = new ArrayList<>(one);
        two = new ArrayList<>(two);

        return one.equals(two);
    }

    private static String defaultStringCell(final Cell cell) {
        return dataFormatter.formatCellValue(cell).trim();
    }

    private static String validateAndPassStringValue(Cell cell, int cellNumber, int rowNumber){
        String cellValue =  dataFormatter.formatCellValue(cell).trim();
        boolean val = alphabetsPattern.matcher(cellValue).find();
        if(!cellValue.isBlank() && val && cellValue.length() >= 2){
            return cellValue;
        }
        String errorMessage = String.format("Invalid Cell Value Passed in row %s, cell %s", rowNumber + 1, cellNumber + 1);
        throw new CustomException(errorMessage, HttpStatus.EXPECTATION_FAILED);
    }

    private static String validateStringIsEmail(Cell cell, int cellNumber, int rowNumber) {
        String cellValue =  dataFormatter.formatCellValue(cell).trim();
        Matcher matcher = emailPattern.matcher(cellValue);
        if(!matcher.matches()){
            String errorMessage = String.format("Invalid Email Cell Value Passed in row %s, cell %s", rowNumber + 1, cellNumber + 1);
            throw new CustomException(errorMessage, HttpStatus.EXPECTATION_FAILED);
        }
        return cellValue;
    }

    private static String validateStringNumericOnly(Cell cell, int cellNumber, int rowNumber) {
        String cellValue =  dataFormatter.formatCellValue(cell).trim();
        boolean val = numericPattern.matcher(cellValue).find();
        if(!val) {
            String errorMessage = String.format("Invalid Numeric Cell Value Passed in row %s, cell %s", rowNumber + 1, cellNumber + 1);
            throw new CustomException(errorMessage, HttpStatus.EXPECTATION_FAILED);
        }
        return cellValue;
    }


}
