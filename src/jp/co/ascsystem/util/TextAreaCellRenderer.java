package jp.co.ascsystem.util;

import java.awt.Component;

import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;
import javax.swing.JTable;

public class TextAreaCellRenderer extends JTextArea implements TableCellRenderer {
  public TextAreaCellRenderer() {
    super();
    setLineWrap(true);
  }
  public Component getTableCellRendererComponent(
      JTable table, Object value,
      boolean isSelected, boolean hasFocus,
      int row, int column) {
    if(isSelected) {
      setForeground(table.getSelectionForeground());
      setBackground(table.getSelectionBackground());
    }else{
      setForeground(table.getForeground());
      setBackground(table.getBackground());
    }
    setText((value == null) ? "" : value.toString());
    return this;
  }
}
