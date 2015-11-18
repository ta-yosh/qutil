package jp.co.ascsystem.lib;

import java.io.*;
import java.util.regex.*;
import jp.co.ascsystem.util.DngXMLParse;

public class DngAppProperty {
  private DngXMLParse xml;

  public DngAppProperty(String uri) {
    xml = new DngXMLParse(uri);
  }
  public String getProperty(String key) {
    String[] keys = key.split("/");
    String val = xml.getValue(keys);
    //if (val=="" || val.matches(".*?<.*>.*")) val=null;
    val = Pattern.compile("_KAKKOL_").matcher(val).replaceAll("\\(");
    val = Pattern.compile("_KAKKOR_").matcher(val).replaceAll("\\)");
    val = Pattern.compile("_DOLLER_").matcher(val).replaceAll("\\$");
    return val;
  }
}
