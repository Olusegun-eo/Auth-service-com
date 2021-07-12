package com.waya.wayaauthenticationservice.util;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.jupiter.api.Test;

import java.io.*;

public class ExcelHelperTest {

    private static String FILE_NAME = "temp.xlsx";
    private String fileLocation;

    @Before
    public void generateExcelFile() throws IOException {
        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        fileLocation = path.substring(0, path.length() - 1) + FILE_NAME;
        ByteArrayInputStream in = ExcelHelper.createExcelSheet(ExcelHelper.PRIVATE_USER_HEADERS);
        // Copy File
        IOUtils.copy(in, new FileOutputStream(fileLocation));
//
//        OutputStream out = new FileOutputStream(fileLocation);
//
//        byte[] buf = new byte[1024];
//        int len;
//        while ((len = in.read(buf)) > 0) {
//            out.write(buf, 0, len);
//        }
//        in.close();
//        out.close();
    }

    @Test
    void streamPayload() {

    }
}
