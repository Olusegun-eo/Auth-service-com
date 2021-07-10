package com.waya.wayaauthenticationservice.util;

import org.junit.Before;

import java.io.File;
import java.io.IOException;

public class ExcelHelperTest {

    private ExcelHelper excelPOIHelper;
    private static String FILE_NAME = "temp.xlsx";
    private String fileLocation;

    @Before
    public void generateExcelFile() throws IOException {
        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        fileLocation = path.substring(0, path.length() - 1) + FILE_NAME;
        ExcelHelper.createExcelSheet(ExcelHelper.PRIVATE_USER_HEADERS);
    }

}
