package org.example.device.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * excel文件解析类，用于解析数据规范的excel表格数据
 *
 * @author ydl
 */
public class ExcelParser {
    /**
     * @return
     * @throws UnsupportedEncodingException
     */
    public static Map<String, String> parseData(InputStream inputStream, String fileName, String sheetName, int sheetIndex) throws UnsupportedEncodingException {
        // 获得Workbook工作薄对象
        Workbook workbook = getWorkBook(inputStream, fileName);
        if (workbook == null)
            return null;
        Sheet sheet = null;
        try {
            sheet = workbook.getSheet(sheetName);
        } catch (Exception ignored) {
        }
        if (sheet == null)
            try {
                sheet = workbook.getSheetAt(sheetIndex);
            } catch (Exception ignored) {
            }
        if (sheet == null)
            return null;
        int firstRowNum = sheet.getFirstRowNum();
        int lastRowNum = sheet.getLastRowNum();
        Map<String, String> dataMap = new HashMap<>();
        for (int rowNum = firstRowNum; rowNum <= lastRowNum; rowNum++) {
            Row row = sheet.getRow(rowNum);
            String data = getCellValue(row.getCell(0));
            if (data.startsWith("LN_DOI_NAME")) {
                data = StringUtils.removeStart(data, "LN_DOI_NAME_");
                String value = getCellValue(row.getCell(1));
                dataMap.put(data, value);
            }
        }
        return dataMap;
    }

    public static Workbook getWorkBook(InputStream inputStream, String fileName) {

        // 创建Workbook工作薄对象，表示整个excel
        Workbook workbook = null;
        try {

            // 根据文件后缀名不同(xls和xlsx)获得不同的Workbook实现类对象
            if (fileName.endsWith("xls")) {
                // 2003
                workbook = new HSSFWorkbook(inputStream);
            } else if (fileName.endsWith("xlsx")) {
                // 2007 及2007以上
                workbook = new XSSFWorkbook(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return workbook;
    }

    /*
     * 获得单元格的数据
     */
//    public static String getCellValue(Cell cell) {
//        String cellValue = "";
//        if (cell == null) {
//            return cellValue;
//        }
//        // 判断数据的类型
//        switch (cell.getCellType()) {
//            case Cell.CELL_TYPE_NUMERIC: // 数字
//                cellValue = stringDateProcess(cell);
//                break;
//            case Cell.CELL_TYPE_STRING: // 字符串
//                cellValue = String.valueOf(cell.getStringCellValue());
//                break;
//            case Cell.CELL_TYPE_BOOLEAN: // Boolean
//                cellValue = String.valueOf(cell.getBooleanCellValue());
//                break;
//            case Cell.CELL_TYPE_FORMULA: // 公式
//                cellValue = String.valueOf(cell.getCellFormula());
//                break;
//            case Cell.CELL_TYPE_BLANK: // 空值
//                cellValue = "";
//                break;
//            case Cell.CELL_TYPE_ERROR: // 故障
//                cellValue = "非法字符";
//                break;
//            default:
//                cellValue = "未知类型";
//                break;
//        }
//        return cellValue;
//    }

    /*
     * 获得单元格的数据
     */
    public static String getCellValue(Cell cell) {
        String cellValue = "";
        if (cell == null) {
            return cellValue;
        }
        // 判断数据的类型
        switch (cell.getCellType()) {
            case NUMERIC: // 数字
                cellValue = stringDateProcess(cell);
                break;
            case STRING: // 字符串
                cellValue = String.valueOf(cell.getStringCellValue());
                break;
            case BOOLEAN: // Boolean
                cellValue = String.valueOf(cell.getBooleanCellValue());
                break;
            case FORMULA: // 公式
                cellValue = String.valueOf(cell.getCellFormula());
                break;
            case BLANK: // 空值
                cellValue = "";
                break;
            case ERROR: // 故障
                cellValue = "非法字符";
                break;
            default:
                cellValue = "未知类型";
                break;
        }
        return cellValue;
    }

    /**
     * 时间格式处理
     *
     * @return
     * @author
     * @data 2017年11月27日
     */
    public static String stringDateProcess(Cell cell) {
        String result;
        if (HSSFDateUtil.isCellDateFormatted(cell)) {// 处理日期格式、时间格式
            SimpleDateFormat sdf;
            if (cell.getCellStyle().getDataFormat() == HSSFDataFormat.getBuiltinFormat("h:mm")) {
                sdf = new SimpleDateFormat("HH:mm");
            } else { // 日期
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            }
            Date date = cell.getDateCellValue();
            result = sdf.format(date);
        } else if (cell.getCellStyle().getDataFormat() == 58) {
            // 处理自定义日期格式：m月d日(通过判断单元格的格式id解决，id的值是58)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            double value = cell.getNumericCellValue();
            Date date = DateUtil.getJavaDate(value);
            result = sdf.format(date);
        } else {
            double value = cell.getNumericCellValue();
            CellStyle style = cell.getCellStyle();
            DecimalFormat format = new DecimalFormat();
            String temp = style.getDataFormatString();
            // 单元格设置成常规
            if (temp.equals("General")) {
                format.applyPattern("#");
            }
            result = format.format(value);
        }

        return result;
    }

}
