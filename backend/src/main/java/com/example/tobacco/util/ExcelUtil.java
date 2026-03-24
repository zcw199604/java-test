package com.example.tobacco.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExcelUtil {

    public byte[] exportWorkbook(String sheetName, String[] headers, List<Map<String, Object>> rows) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet(sheetName);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            for (int i = 0; i < rows.size(); i++) {
                Row row = sheet.createRow(i + 1);
                int col = 0;
                for (String header : headers) {
                    Object value = rows.get(i).get(header);
                    row.createCell(col++).setCellValue(value == null ? "" : String.valueOf(value));
                }
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Excel导出失败: " + ex.getMessage());
        }
    }

    public List<Map<String, String>> importWorkbook(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        try {
            InputStream inputStream = file.getInputStream();
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                workbook.close();
                throw new IllegalArgumentException("Excel模板缺少表头");
            }
            List<String> headers = new ArrayList<String>();
            for (Cell cell : headerRow) {
                headers.add(readCell(cell));
            }
            List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                Map<String, String> item = new LinkedHashMap<String, String>();
                boolean empty = true;
                for (int j = 0; j < headers.size(); j++) {
                    String value = readCell(row.getCell(j));
                    if (value != null && value.trim().length() > 0) {
                        empty = false;
                    }
                    item.put(headers.get(j), value);
                }
                if (!empty) {
                    rows.add(item);
                }
            }
            workbook.close();
            return rows;
        } catch (IOException ex) {
            throw new IllegalArgumentException("Excel导入失败: " + ex.getMessage());
        }
    }

    public Integer toInteger(Map<String, String> row, String key) {
        return Integer.valueOf(require(row, key));
    }

    public Long toLong(Map<String, String> row, String key) {
        return Long.valueOf(require(row, key));
    }

    public BigDecimal toBigDecimal(Map<String, String> row, String key) {
        return new BigDecimal(require(row, key));
    }

    public String require(Map<String, String> row, String key) {
        String value = row.get(key);
        if (value == null || value.trim().length() == 0) {
            throw new IllegalArgumentException("Excel字段缺失: " + key);
        }
        return value.trim();
    }

    private String readCell(Cell cell) {
        if (cell == null) {
            return "";
        }
        CellType cellType = cell.getCellType();
        if (cellType == CellType.NUMERIC) {
            double value = cell.getNumericCellValue();
            if (Math.floor(value) == value) {
                return String.valueOf((long) value);
            }
            return String.valueOf(value);
        }
        if (cellType == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        return cell.toString().trim();
    }
}
