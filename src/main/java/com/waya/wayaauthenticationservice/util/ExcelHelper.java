package com.waya.wayaauthenticationservice.util;

import com.waya.wayaauthenticationservice.exception.CustomException;
import com.waya.wayaauthenticationservice.pojo.others.BonusTransferExcelPojo;
import com.waya.wayaauthenticationservice.pojo.others.BulkBonusTransferDTO;
import com.waya.wayaauthenticationservice.pojo.userDTO.BaseUserPojo;
import com.waya.wayaauthenticationservice.pojo.userDTO.BulkCorporateUserCreationDTO;
import com.waya.wayaauthenticationservice.pojo.userDTO.BulkPrivateUserCreationDTO;
import com.waya.wayaauthenticationservice.pojo.userDTO.CorporateUserPojo;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
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

import static com.waya.wayaauthenticationservice.util.HelperUtils.isEmailOrPhoneNumber;

@Slf4j
public class ExcelHelper {

    private static DataFormatter dataFormatter = new DataFormatter();

    public static String[] TYPE = {"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel"};
    public static List<String> PRIVATE_USER_HEADERS = Arrays.asList("FIRSTNAME", "SURNAME", "PHONE_NUMBER", "EMAIL", "REFERENCE_CODE" );
    public static List<String> CORPORATE_USER_HEADERS = Arrays.asList("FIRSTNAME", "SURNAME", "PHONE_NUMBER", "EMAIL",
            "OFFICE_ADDRESS", "CITY", "STATE", "ORG_NAME", "ORG_EMAIL",
            "ORG_PHONE", "ORG_TYPE", "BUSINESS_TYPE", "REFERENCE_CODE");
    public static List<String> ACTIVATION_LIST = Arrays.asList("FIRSTNAME", "SURNAME", "PHONE_NUMBER OR EMAIL");
    public static List<String> USER_BONUS_HEADERS = Arrays.asList("AMOUNT", "BENEF_ACCOUNT_NUMBER", "DEBIT_ACCOUNT_NUMBER", "TRAN_C_RNCY", "TRAN_NARRATION" );
    static String SHEET = "Users";
    static String BONUS_SHEET = "UserBonus";

    static Pattern alphabetsPattern = Pattern.compile("^([^0-9]*)$");
    static Pattern numericPattern = Pattern.compile("^[0-9]*$");
    static Pattern emailPattern = Pattern.compile("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\." + "[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@"
            + "(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?");

    static Pattern formatter = Pattern.compile("\\d+\\.\\d+");

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
                BaseUserPojo pojo = new BaseUserPojo();

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
                    String colName = CellReference.convertNumToColString(cell.getColumnIndex()).toUpperCase();
                    switch (colName) {
                        case "A":
                            pojo.setFirstName(validateAndPassStringValue(cell, cellIdx, rowNumber));
                            break;
                        case "B":
                            pojo.setSurname(validateAndPassStringValue(cell, cellIdx, rowNumber));
                            break;
                        case "C":
                            pojo.setPhoneNumber(validateStringNumericOnly(cell, cellIdx, rowNumber));
                            break;
                        case "D":
                            pojo.setEmail(validateStringIsEmail(cell, cellIdx, rowNumber));
                            break;
                        case "E":
                            pojo.setReferenceCode(defaultStringCell(cell));
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

    public static Set<String> excelActivation(InputStream is, String fileName){

        try(Workbook workbook = getWorkBook(is, fileName)) {
            if(workbook == null){
                throw new CustomException("Invalid Excel File Check Extension", HttpStatus.BAD_REQUEST);
            }
            Set<String> userList = new HashSet<>();

            Sheet sheet = workbook.getSheet(SHEET);
            if(sheet == null) throw new CustomException("Invalid Excel File Format Passed, Check Sheet Name", HttpStatus.BAD_REQUEST);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            while (rows.hasNext()){
                Row currentRow = rows.next();
                Iterator<Cell> cellsInRow = currentRow.iterator();

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
                        if (i == ACTIVATION_LIST.size()) {
                            break;
                        }
                    }
                    boolean value = checkExcelFileValidity(ACTIVATION_LIST, excelColNames);
                    if (!value) {
                        String errorMessage = "Failure, Incorrect File Header Format";
                        throw new CustomException(errorMessage, HttpStatus.BAD_REQUEST);
                    }
                    rowNumber++;
                    continue;
                }

                int cellIdx = 0;
                String validId = null;
                while (cellsInRow.hasNext()){
                    Cell cell = cellsInRow.next();
                    String colName = CellReference.convertNumToColString(cell.getColumnIndex()).toUpperCase();
                    switch (colName) {
                        case "C":
                            validId = validateActivationId(cell, cellIdx, rowNumber);
                            break;
                        default:
                            break;
                    }
                    cellIdx++;
                }
                if(validId != null)
                    userList.add(validId);
            }
            return userList;
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
                    String colName = CellReference.convertNumToColString(cell.getColumnIndex()).toUpperCase();
                    switch (colName) {
                        case "A":
                            pojo.setFirstName(validateAndPassStringValue(cell, cellIdx, rowNumber));
                            break;
                        case "B":
                            pojo.setSurname(validateAndPassStringValue(cell, cellIdx, rowNumber));
                            break;
                        case "C":
                            pojo.setPhoneNumber(validateStringNumericOnly(cell, cellIdx, rowNumber));
                            break;
                        case "D":
                            pojo.setEmail(validateStringIsEmail(cell, cellIdx, rowNumber));
                            break;
                        case "E":
                            pojo.setOfficeAddress(defaultStringCell(cell));
                            break;
                        case "F":
                            pojo.setCity(defaultStringCell(cell));
                            break;
                        case "G":
                            pojo.setState(defaultStringCell(cell));
                            break;
                        case "H":
                            pojo.setOrgName(defaultStringCell(cell));
                            break;
                        case "I":
                            pojo.setOrgEmail(defaultStringCell(cell));
                            break;
                        case "J":
                            pojo.setOrgPhone(defaultStringCell(cell));
                            break;
                        case "K":
                            pojo.setOrgType(defaultStringCell(cell));
                            break;
                        case "L":
                            pojo.setBusinessType(validateAndPassStringValue(cell, cellIdx, rowNumber));
                            break;
                        case "M":
                            pojo.setReferenceCode(defaultStringCell(cell));
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


    public static BulkBonusTransferDTO excelToBonusTransferPojo(InputStream is, String fileName){

        try(Workbook workbook = getWorkBook(is, fileName)) {
            if(workbook == null){
                throw new CustomException("Invalid Excel File Format Passed, Check Extension", HttpStatus.BAD_REQUEST);
            }
            Set<BonusTransferExcelPojo> models = new HashSet<>();

            Sheet sheet = workbook.getSheet(BONUS_SHEET);
            if(sheet == null) throw new CustomException("Invalid Excel File Format Passed, Check Sheet Name", HttpStatus.BAD_REQUEST);
            Iterator<Row> rows = sheet.iterator();
            int rowNumber = 0;
            while (rows.hasNext()){
                Row currentRow = rows.next();
                Iterator<Cell> cellsInRow = currentRow.iterator();
                BonusTransferExcelPojo pojo = new BonusTransferExcelPojo();

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
                        if (i == USER_BONUS_HEADERS.size()) {
                            break;
                        }
                    }
                    boolean value = checkExcelFileValidity(USER_BONUS_HEADERS, excelColNames);
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
                    String colName = CellReference.convertNumToColString(cell.getColumnIndex()).toUpperCase();
                    switch (colName) {
                        case "A":
                            pojo.setAmount(Double.parseDouble(validateStringDoubleOnly(cell, cellIdx, rowNumber)));
                            break;
                        case "B":
                            pojo.setBenefAccountNumber(defaultStringCell(cell));
                            break;
                        case "C":
                            pojo.setDebitAccountNumber(defaultStringCell(cell));
                            break;
                        case "D":
                            pojo.setTranCrncy(defaultStringCell(cell));
                            break;
                        case "E":
                            pojo.setTranNarration(defaultStringCell(cell));
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
            return new BulkBonusTransferDTO(models);
        }catch (Exception ex){
            throw new CustomException(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    public static ByteArrayInputStream createExcelSheet(List<String> HEADERS){
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            DataFormat fmt = workbook.createDataFormat();
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setDataFormat(fmt.getFormat("@"));
            
            Sheet sheet = workbook.createSheet(SHEET);
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERS.size(); col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERS.get(col));
                cell.setCellStyle(cellStyle);
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
        log.info("val :" + val);
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
    	String cellValue;
    	try {
    		double d = cell.getNumericCellValue();
    		cellValue = String.format("%.0f", d);
    	}catch(IllegalStateException | NumberFormatException ex) {
    		cellValue = dataFormatter.formatCellValue(cell).trim();
    	}
        boolean val = numericPattern.matcher(cellValue).find();
        if(!val) {
            String errorMessage = String.format("Invalid Numeric Cell Value Passed in row %s, cell %s", rowNumber + 1, cellNumber + 1);
            throw new CustomException(errorMessage, HttpStatus.EXPECTATION_FAILED);
        }
        return cellValue;
    }

    private static String validateActivationId(Cell cell, int cellNumber, int rowNumber) {
        String cellValue;
        try {
            double d = cell.getNumericCellValue();
            cellValue = String.format("%.0f", d);
        }catch(IllegalStateException | NumberFormatException ex) {
            cellValue = dataFormatter.formatCellValue(cell).trim();
        }
        boolean val = isEmailOrPhoneNumber(cellValue);
        if(!val) {
            String errorMessage = String.format("Invalid Numeric Cell Value Passed in row %s, cell %s", rowNumber + 1, cellNumber + 1);
            log.error("Invalid Numeric Cell entered :: {}", errorMessage);
            return null;
        }
        return cellValue;
    }

    private static String validateStringDoubleOnly(Cell cell, int cellNumber, int rowNumber) throws CustomException {
        String cellValue =  dataFormatter.formatCellValue(cell).trim();
        boolean val = formatter.matcher(cellValue).find();
        if(!val) {
            String errorMessage = String.format("Invalid Numeric Cell Value Passed in row %s, cell %s", rowNumber + 1, cellNumber + 1);
            throw new CustomException(errorMessage,HttpStatus.EXPECTATION_FAILED);
        }
        return cellValue;
    }


}
