package jp.co.ascsystem.qkan;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import jp.co.ascsystem.util.DngGenericCombo;
import jp.co.ascsystem.util.DngDBAccess;

public class QkanSearchCondition {

  private JComboBox cBox;
  private Object[][] values; 
  private Object[] keys;

  public void DngGenericCombo(Object[] obj) {
    for (int i=0;i<obj.length;i++) {
      values[i][0] = obj[i];
    }
    cBox = new JComboBox(obj);
    cBox.setMaximumRowCount(1);
  }

  public void DngGenericCombo(Object[][] obj) {
    for (int i=0;i<obj.length;i++) {
      keys[i] = obj[i][0];
      for (int j=0;j<obj[i].length;j++) {
        values[i][j] = obj[i][j];
      }
    }
    cBox = new JComboBox(keys);
    cBox.setMaximumRowCount(1);
  }

  public Object[] getSelctedValues() {
    return values[ cBox.getSelectedIndex() ];
  }

}
