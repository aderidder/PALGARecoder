/*
 * Copyright 2017 NKI/AvL
 *
 * This file is part of PALGARecoder.
 *
 * PALGARecoder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PALGARecoder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PALGARecoder. If not, see <http://www.gnu.org/licenses/>
 */

package recoder.utils;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Functions for apache poi usage
 */
public class ExcelUtils {
    private static final Logger logger = LogManager.getLogger(ExcelUtils.class.getName());

    /**
     * transform row to list
     * @param row    the row
     * @return  list representation of the row
     */
    public static List<String> getRowAsList(Row row){
        List list = new ArrayList();
        for(Cell cell:row){
            list.add(cell.getStringCellValue().toUpperCase());
        }
        return list;
    }

    /**
     * get value of a row based on an index
     * @param row the row
     * @param i   the index
     * @return string value
     */
    public static String getCellValue(Row row, int i){
        // retrieve the cell
        Cell cell = row.getCell(i, Row.CREATE_NULL_AS_BLANK);
        // convert the cell type to string to prevent lots of annoying cell type issues
        cell.setCellType(Cell.CELL_TYPE_STRING);
        // return the value
        return cell.getStringCellValue();
    }

    /**
     * create a new workbook
     * @return the workbook
     */
    public static Workbook createXLSXWorkbook(){
        return new XSSFWorkbook();
    }

    /**
     * write a workbook to file
     * @param workbook the workbook to write
     * @param fileName the filename of the output file
     */
    public static void writeXLSXWorkBook(Workbook workbook, String fileName){
        try (FileOutputStream fileOut = new FileOutputStream(fileName)){
            workbook.write(fileOut);
            fileOut.close();
        } catch (IOException e) {
            logger.error("Problem creating {}. The file has NOT been created.", fileName);
        }
    }

    /**
     * create a style with bold text and custom background
     * @param workbook     the workbook in which the style should be available
     * @param indexedColor the color which is to be used for the style
     * @return the cellstyle
     */
    public static CellStyle createHeaderStyle(Workbook workbook, IndexedColors indexedColor){
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(indexedColor.index);
        headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        return headerStyle;
    }

    /**
     * Creates a header in a sheet and applies a cellstyle to each of the header's cells
     * @param sheet       the sheet in which the header will be created
     * @param headerNames list of headernames
     * @param cellStyle   the cellstyle which should be applied to the header
     */
    public static void addHeader(Sheet sheet, List<String> headerNames, CellStyle cellStyle){
        Row row = sheet.createRow(0);
        for(int i=0; i<headerNames.size(); i++){
            row.getCell(i, Row.CREATE_NULL_AS_BLANK).setCellValue(headerNames.get(i));
            row.getCell(i).setCellStyle(cellStyle);
        }
    }

    /**
     * creates a one cell title in the sheet and applies a cellstyle to it
     * @param sheet       the sheet in which the title will be created
     * @param text        the title
     * @param cellStyle   the cellstyle which should be applied to the header
     */
    public static void addTitle(Sheet sheet, String text, CellStyle cellStyle){
        Row row = sheet.createRow(sheet.getLastRowNum()+1);
        row.getCell(0, Row.CREATE_NULL_AS_BLANK).setCellValue(text);
        row.getCell(0).setCellStyle(cellStyle);
    }

    /**
     * writes values to a sheet
     * @param sheet  the sheet in which the values will be created
     * @param values the values to be written
     */
    public static void writeValues(Sheet sheet, List<String> values){
        Row row = sheet.createRow(sheet.getLastRowNum()+1);
        for(int i=0; i<values.size(); i++){
            row.getCell(i, Row.CREATE_NULL_AS_BLANK).setCellValue(values.get(i));
        }
    }

    /**
     * skip a line in an Excel sheet
     * @param sheet    sheet in which to skip a line
     */
    public static void skipLine(Sheet sheet){
        sheet.createRow(sheet.getLastRowNum()+1);
    }

    /**
     * create a sheet in a workbook with header texts, which follow a certain cellstyle
     * @param workbook       the workbook
     * @param sheetName      sheetname in which to create the header
     * @param headerNames    names of the header items
     * @param headerStyle    style to use for the header
     */
    public static void createSheetWithHeader(Workbook workbook, String sheetName, List <String> headerNames, CellStyle headerStyle){
        Sheet sheet = workbook.createSheet(sheetName);
        Row row = sheet.createRow(0);

        for(int i=0; i<headerNames.size(); i++){
            row.getCell(i, Row.CREATE_NULL_AS_BLANK).setCellValue(headerNames.get(i));
            row.getCell(i).setCellStyle(headerStyle);
        }
    }
}
