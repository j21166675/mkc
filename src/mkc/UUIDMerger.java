UUIDMerger.java
Earlier this week
Mon 10:42 PM
J
You uploaded an item
Java
UUIDMerger.java
import static java.nio.charset.Charset.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.io.*;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class UUIDMerger {
    private static Map<String, String> excludeDataMap = new HashMap<String, String>();

    public Map<String, String> readSheet(String filename) throws IOException, InvalidFormatException {
      System.out.println( "*** Excell sheet to Map Conversion has been initiated ***");
      Map<String, String> map= new HashMap<String, String>();

      FileInputStream fis = new FileInputStream(new File(filename));
      XSSFWorkbook wb = new XSSFWorkbook(fis);
      XSSFSheet sh = wb.getSheetAt(0);
      for(Row row : sh) {
        String key = "";
        String value = "";
        for(Cell cell : row) {
          String cellVal = cell.getStringCellValue();

          if(cell.getColumnIndex() == 0){
            key = validateMethodName(cellVal);
         }
          else if(cell.getColumnIndex() == 1){
            value = cellVal;
          }
        }
        map.put(key,value);
      }
      return map;
    }

    public String validateMethodName(String cellName){
      if(cellName == null){
        cellName = "skip";
      }
      else if(cellName.indexOf("Corpora") == -1){
        cellName = "skip";
      }
      else if(cellName.indexOf("Hermetic") != -1) {
        if(cellName.startsWith("Corpora")) {
          cellName = cellName
              .substring((cellName.indexOf("Corpora") + 8), cellName.indexOf("Hermetic") - 1);
        }
        else{
          cellName = cellName
              .substring((cellName.indexOf("Corpora") + 8), cellName.length());
        }
      }
      else if(cellName.indexOf("Corpora") != -1){
        cellName = cellName.substring((cellName.indexOf("Corpora") + 8));
      }
      return cellName;
    }

    private String setUUID(Map<String, String> map, String javaFileName)
        throws IOException {
      return addUUID(removeExistingUUID(javaFileName), map);
    }

  private String addUUID(String fileStr, Map<String, String> map) {
      int count =0;
    for (Map.Entry<String,String> entry : map.entrySet()) {
      String key = entry.getKey();
      String value = entry.getValue();

      if(key != null & key.length() !=0 & fileStr.contains(key.trim()) &  fileStr.charAt(fileStr.indexOf(key) + key.length()) == '('){
        fileStr = insertAt(fileStr,fileStr.lastIndexOf("@Test", fileStr.indexOf(key)) + 6,"  @TestTrackerUuid("+"\"" + value + "\")");

        if(fileStr.charAt(fileStr.lastIndexOf("@TestTrackerUuid", fileStr.indexOf(key)) + 58) != '\n'){
          fileStr = insertAt(fileStr,fileStr.lastIndexOf("@TestTrackerUuid", fileStr.indexOf(key)) + 58,"\n  ");
        }
      }
      else{
        excludeDataMap.put(key,value);
      }
    }
    return fileStr;
  }

  public static String insertAt(final String target, final int position, final String insert) {
    final int targetLen = target.length();
    if (position < 0 || position > targetLen) {
      throw new IllegalArgumentException("position=" + position);
    }
    if (insert.isEmpty()) {
      return target;
    }
    if (position == 0) {
      return insert.concat(target);
    } else if (position == targetLen) {
      return target.concat(insert);
    }
    final int insertLen = insert.length();
    final char[] buffer = new char[targetLen + insertLen];
    target.getChars(0, position, buffer, 0);
    insert.getChars(0, insertLen, buffer, position);
    target.getChars(position, targetLen, buffer, position + insertLen);
    return new String(buffer);
  }

  private String removeExistingUUID(String fileName) throws IOException {
    System.out.println("***Removing Existing UUID is initiated***");
    String fileContent = fileToString(fileName);
    int count = 1;
    while(fileContent.indexOf("@TestTrackerUuid") != -1){
        String subString = fileContent.substring(fileContent.indexOf("@TestTrackerUuid"),fileContent.indexOf("@TestTrackerUuid")+56);
        fileContent = fileContent.replace(subString,"");
        count ++;
     }
    System.out.println("Total UUID removed : " + count);
    return fileContent;
  }

  public String fileToString(String fileName) throws IOException {
    final String EoL = System.getProperty("line.separator");
    List<String> lines = Files.readAllLines(Paths.get(fileName),
        defaultCharset());
    StringBuilder sb = new StringBuilder();
    for (String line : lines) {
      sb.append(line).append(EoL);
    }
    return (sb.toString());
  }

  public void convertMap(String fileName) throws IOException, InvalidFormatException {
      Map<String, String> map= new HashMap<String, String>();
      readSheet(fileName);
  }

  private Path writeToFile(String fileContent) throws IOException {
    String path = "/usr/local/google/home/jasokan/Documents/work/UUID/data/Drive/UUIDUpdatedFile.java";
    return(Files.write( Paths.get(path), fileContent.getBytes(), StandardOpenOption.CREATE));
  }
  public String lint(String fileContent){
      int i=0;
      while(fileContent.indexOf("public void",i) != -1){
        if(Character.isWhitespace(fileContent.charAt(fileContent.indexOf("public void",i)-1))){
          insertAt(fileContent, fileContent.indexOf("public void",i)-1, "\n");
        }
      }
      return fileContent;
  }

    public static void main(String[] args) throws IOException, InvalidFormatException {
       String fileName = "/usr/local/google/home/jasokan/Documents/work/UUID/data/Drive/search2.xlsx";
       String javaFileName = "/usr/local/google/home/jasokan/Documents/work/UUID/data/Drive/driveExports.java";
       UUIDMerger objUUIDMerger = new UUIDMerger();
       Map<String, String> map= objUUIDMerger.readSheet(fileName);
       System.out.println( "*** Map Conversion is done successfully ***"+map.size());

       String finalFileContent = objUUIDMerger.setUUID(map, javaFileName);
       System.out.println( "*** UUID insertion is done successfully ***");

      System.out.println( "*** File has created with UUID. PATH - "+ objUUIDMerger.writeToFile(finalFileContent));
      System.out.println("*** Excluded data Count - "+ excludeDataMap.size());
      System.out.println("*** Excluded data - "+ excludeDataMap);
    }
}
