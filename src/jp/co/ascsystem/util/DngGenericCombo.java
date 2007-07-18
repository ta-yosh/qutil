package jp.co.ascsystem.util;

import java.awt.*;
import java.util.*;
import javax.swing.*;

public class DngGenericCombo {

  private JComboBox cBox;
  private String[][] values; 

  public DngGenericCombo(String[] obj) {
    this.values = new String[obj.length][1];
    for (int i=0;i<obj.length;i++) {
      this.values[i][0] = obj[i];
    }
    cBox = new JComboBox(obj);
    cBox.setMaximumRowCount(10);
  }

  public DngGenericCombo(String[][] obj) {
    this.values = new String[obj.length][obj[0].length];
    String[] keys = new String[obj.length];
    for (int i=0;i<obj.length;i++) {
      System.out.println(obj[i][0]);
      keys[i] = obj[i][0];
      for (int j=0;j<obj[i].length;j++) {
        this.values[i][j] = obj[i][j];
      }
    }
    cBox = new JComboBox(keys);
    cBox.setMaximumRowCount(10);
  }

  public JComboBox getComboBox() {
    return cBox;
  }

  public Object[] getSelctedValues() {
    return values[ cBox.getSelectedIndex() ];
  }

}
