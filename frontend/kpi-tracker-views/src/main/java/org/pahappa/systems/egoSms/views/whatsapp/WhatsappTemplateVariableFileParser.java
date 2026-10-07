package org.pahappa.systems.egoSms.views.whatsapp;

import org.apache.poi.ss.usermodel.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.*;

final class WhatsappTemplateVariableFileParser {
    static final class ParsedFile {
        private final List<String> headers;
        private final List<Map<String,String>> rows;
        ParsedFile(List<String> headers,List<Map<String,String>> rows){this.headers=headers;this.rows=rows;}
        List<String> getHeaders(){return headers;}
        List<Map<String,String>> getRows(){return rows;}
    }

    private WhatsappTemplateVariableFileParser(){}

    static ParsedFile parse(String fileName,byte[] content)throws Exception{
        String lower=fileName==null?"":fileName.toLowerCase(Locale.ENGLISH);
        if(lower.endsWith(".csv"))return csv(content);
        if(lower.endsWith(".xlsx")||lower.endsWith(".xls"))return excel(content);
        throw new IllegalArgumentException("Upload a CSV, XLSX, or XLS file");
    }

    private static ParsedFile excel(byte[] content)throws Exception{
        Workbook workbook=WorkbookFactory.create(new ByteArrayInputStream(content));
        if(workbook.getNumberOfSheets()==0)throw new IllegalArgumentException("The workbook has no worksheets");
        Sheet sheet=workbook.getSheetAt(0);
        DataFormatter formatter=new DataFormatter();
        FormulaEvaluator evaluator=workbook.getCreationHelper().createFormulaEvaluator();
        List<List<String>> values=new ArrayList<List<String>>();
        int lastColumn=0;
        for(Row row:sheet){
            lastColumn=Math.max(lastColumn,row.getLastCellNum());
            List<String> cells=new ArrayList<String>();
            for(int column=0;column<lastColumn;column++){
                Cell cell=row.getCell(column,Row.RETURN_BLANK_AS_NULL);
                cells.add(cell==null?"":formatter.formatCellValue(cell,evaluator).trim());
            }
            values.add(cells);
        }
        return rows(values);
    }

    private static ParsedFile csv(byte[] content){
        String text=new String(content,Charset.forName("UTF-8"));
        if(text.startsWith("\uFEFF"))text=text.substring(1);
        List<List<String>> values=new ArrayList<List<String>>();
        List<String> row=new ArrayList<String>();StringBuilder value=new StringBuilder();boolean quoted=false;
        for(int i=0;i<text.length();i++){
            char character=text.charAt(i);
            if(character=='"'){
                if(quoted&&i+1<text.length()&&text.charAt(i+1)=='"'){value.append('"');i++;}
                else quoted=!quoted;
            }else if(character==','&&!quoted){row.add(value.toString().trim());value.setLength(0);}
            else if((character=='\n'||character=='\r')&&!quoted){
                if(character=='\r'&&i+1<text.length()&&text.charAt(i+1)=='\n')i++;
                row.add(value.toString().trim());value.setLength(0);values.add(row);row=new ArrayList<String>();
            }else value.append(character);
        }
        if(value.length()>0||!row.isEmpty()){row.add(value.toString().trim());values.add(row);}
        return rows(values);
    }

    private static ParsedFile rows(List<List<String>> values){
        while(!values.isEmpty()&&blank(values.get(0)))values.remove(0);
        if(values.isEmpty())throw new IllegalArgumentException("The file is empty");
        List<String> headers=new ArrayList<String>();
        for(String value:values.get(0))headers.add(normalize(value));
        List<Map<String,String>> rows=new ArrayList<Map<String,String>>();
        for(int index=1;index<values.size();index++){
            List<String> source=values.get(index);if(blank(source))continue;
            Map<String,String> mapped=new LinkedHashMap<String,String>();
            for(int column=0;column<headers.size();column++)mapped.put(headers.get(column),column<source.size()?source.get(column).trim():"");
            rows.add(mapped);
        }
        return new ParsedFile(headers,rows);
    }

    private static String normalize(String value){return value==null?"":value.trim().toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9]+","_").replaceAll("^_+|_+$","");}
    private static boolean blank(List<String> row){for(String value:row)if(value!=null&&!value.trim().isEmpty())return false;return true;}
}
