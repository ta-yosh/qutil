package jp.co.ascsystem.qkan;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.regex.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.event.*;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import jp.co.ascsystem.util.*;
import jp.co.ascsystem.lib.*;

public class QkanProviderData {

    private DngDBAccess dbm;
    private String dbUri;
    private String dbUser;
    private String dbPass;
    private Vector data = new Vector();
    private String pdata[][];
    public int Rows;
    private JScrollPane scp;
    private JPanel tPanel = new JPanel(new BorderLayout());

    private JPanel pn = new JPanel();
    private JPanel pnl = new JPanel(new BorderLayout()); 
    private JTable usrTbl;
    private boolean isSelectable=true;
    private DefaultTableModel dtm;
    private TableSorter2 sorter;
    private int idCol;
    Hashtable sbLabel = new Hashtable();
    Hashtable vServ = new Hashtable();
    String jigyoKubun[] = new String[] {"","指定","基準該当","","","地域密着"};
    String chiikiKubun[] = new String[] {"","特別区","特甲地","甲地","乙地",
                                         "その他","特別地域"};
    public QkanProviderData(String dbUri,String dbUser,String dbPass) {
      this.dbUri = dbUri;
      this.dbUser = dbUser;
      this.dbPass = dbPass;
      dbm = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
      StringBuffer buf = new StringBuffer();
      buf.append("select system_service_kind_detail,service_calendar_abbreviation from m_service order by system_service_kind_detail");
      String sql = buf.toString(); 
      if (dbm.connect()) {
        dbm.execQuery(sql);
        for (int i=0;i<dbm.Rows;i++) {
          String key = dbm.getData("SYSTEM_SERVICE_KIND_DETAIL",i).toString();
          String val = dbm.getData("SERVICE_CALENDAR_ABBREVIATION",i).toString();
          System.out.println(key+" = 「"+val+"」");
          sbLabel.put(key,val);
        }
        buf.delete(0,buf.length());
        buf.append("select * from PROVIDER"); //order by PROVIDER_NAME");
        dbm.execQuery(buf.toString());
        Rows = dbm.Rows;
        dbm.Close();
        pdata = new String[Rows][12];
        for (int i=0;i<Rows;i++) {
          String tanto = "";
          String services = "";
          String services2 = "";
          String proid = dbm.getData("PROVIDER_ID",i).toString();
          System.out.println(i+" PROID = "+proid);
          buf.delete(0,buf.length());
          buf.append("select STAFF_FAMILY_NAME,STAFF_FIRST_NAME from STAFF");
          buf.append(" where PROVIDER_ID='");
          buf.append(proid);
          buf.append("'"); 
          DngDBAccess dbm2 = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
          if (dbm2.connect()) {
            dbm2.execQuery(buf.toString());
            if (dbm2.Rows>0) {
              tanto = "";
              for (int j=0;j<dbm2.Rows;j++) {
                if (j>0) tanto += ",";
                tanto += ((dbm2.getData("STAFF_FAMILY_NAME",j)!=null) ?
                        dbm2.getData("STAFF_FAMILY_NAME",j).toString():"")
                       +((dbm2.getData("STAFF_FIRST_NAME",j)!=null) ?
                        dbm2.getData("STAFF_FIRST_NAME",j).toString():"");
              }
            }
            System.out.println("tanto = "+tanto);
            buf.delete(0,buf.length());
            buf.append("select SYSTEM_SERVICE_KIND_DETAIL ");
            buf.append("from PROVIDER_SERVICE where PROVIDER_ID='");
            buf.append(proid);
            buf.append("' order by SYSTEM_SERVICE_KIND_DETAIL"); 
            dbm2.execQuery(buf.toString());
            dbm2.Close();
            buf.delete(0,buf.length());
            StringBuffer buf2 = new StringBuffer();
            int tanryoCnt=0;
            int yotanryoCnt=0;
            int ryoyoCnt=0;
            int Cnt=0;
            for (int j=0;j<dbm2.Rows;j++) {
              String sskd = dbm2.getData("SYSTEM_SERVICE_KIND_DETAIL",j).toString();
              int sskdn = Integer.parseInt(sskd);
              if (sskdn>12210 && sskdn<12315) {
                if (tanryoCnt>0) continue; 
                tanryoCnt++;
              }
              if (sskdn>12510 && sskdn<12615) {
                if (yotanryoCnt>0) continue; 
                yotanryoCnt++;
              }
              if (sskdn>15310 && sskdn<15314) {
                if (ryoyoCnt>0) continue; 
                ryoyoCnt++;
              }
              Cnt++;
              if (Cnt>10) {
                if (Cnt==11) buf.append("、...他");
              }
              else {
                if (j>0) buf.append("、");
                buf.append(sbLabel.get(dbm2.getData("SYSTEM_SERVICE_KIND_DETAIL",j).toString()));
              }
              if (j>0) buf2.append("、");
              buf2.append(sbLabel.get(dbm2.getData("SYSTEM_SERVICE_KIND_DETAIL",j).toString()));
            }
            services = buf.toString();
            services2 = buf2.toString();
          }
          pdata[i][0] = (new Integer(i)).toString();
          pdata[i][1] = proid;
          pdata[i][2] = (dbm.getData("PROVIDER_NAME",i)!=null) ?
                        dbm.getData("PROVIDER_NAME",i).toString():"";
          pdata[i][3] = ((dbm.getData("PROVIDER_ZIP_FIRST",i)!=null) ? 
                         dbm.getData("PROVIDER_ZIP_FIRST",i).toString():"")
                       +((dbm.getData("PROVIDER_ZIP_SECOND",i)!=null) ?
                       "-"+dbm.getData("PROVIDER_ZIP_SECOND",i).toString():"");
          pdata[i][4] = (dbm.getData("PROVIDER_ADDRESS",i)!=null) ?
                        dbm.getData("PROVIDER_ADDRESS",i).toString():"";
          pdata[i][5] = ((dbm.getData("PROVIDER_TEL_FIRST",i)!=null) ?
                        dbm.getData("PROVIDER_TEL_FIRST",i).toString()+"-":"")
                       +((dbm.getData("PROVIDER_TEL_SECOND",i)!=null) ?
                        dbm.getData("PROVIDER_TEL_SECOND",i).toString()+"-":"")
                       +((dbm.getData("PROVIDER_TEL_THIRD",i)!=null) ?
                        dbm.getData("PROVIDER_TEL_THIRD",i).toString():"");
          pdata[i][6] = ((dbm.getData("PROVIDER_FAX_FIRST",i)!=null) ?
                        dbm.getData("PROVIDER_FAX_FIRST",i).toString()+"-":"")
                       +((dbm.getData("PROVIDER_FAX_SECOND",i)!=null) ?
                        dbm.getData("PROVIDER_FAX_SECOND",i).toString()+"-":"")
                       +((dbm.getData("PROVIDER_FAX_THIRD",i)!=null) ?
                        dbm.getData("PROVIDER_FAX_THIRD",i).toString():"");
          System.out.println("jugyo_type="+dbm.getData("PROVIDER_JIGYOU_TYPE",i)+" area_type="+dbm.getData("PROVIDER_AREA_TYPE",i));
          pdata[i][7] = (dbm.getData("PROVIDER_JIGYOU_TYPE",i)!=null) ?
                        jigyoKubun[Integer.parseInt(dbm.getData("PROVIDER_JIGYOU_TYPE",i).toString())]:"";
          pdata[i][8] = (dbm.getData("PROVIDER_AREA_TYPE",i)!=null) ?
                        chiikiKubun[Integer.parseInt(dbm.getData("PROVIDER_AREA_TYPE",i).toString())]:"";
          pdata[i][9] = tanto;
          pdata[i][10] = services;
          vServ.put(pdata[i][1],services2);
          Vector pdat = new Vector();
          for (int k=1;k<11;k++) {
            String elem = (pdata[i][k]!=null) ? pdata[i][k]:"";
            if (k==4) elem = halfnum.convert(elem);
            pdat.addElement(elem);
          }
          data.addElement(pdat);
        }
      }
      else Rows=-1;
      System.out.println("Provider list "+Rows+"recs prepared OK");
    }

    public String getData(int row,int col) {
      return pdata[row][col];
    }
  
    public boolean isSelected() {
      //int sel = usrTbl.getSelectedRow();
      //return (sel!=-1) ? true:false;
      return true;
    }

    public void setSelectable(boolean selectable) {
      isSelectable = selectable;
    }
    public String getProviderDataCsv(int pno) {
      StringBuffer csvRecord;
           csvRecord = new StringBuffer();
           for (int j=0;j<usrTbl.getColumnCount();j++) {
             Object value;
             csvRecord.append("\"");
             if (pno<0) {
               value=(Object)usrTbl.getColumnName(j);
               if (value.equals("事業所番号")) idCol = j;
             }
             else {
               value=usrTbl.getValueAt(pno,j);
               if (usrTbl.getColumnName(j).equals("提供サービス")) 
                 value = vServ.get(usrTbl.getValueAt(pno,idCol));
             }
             if (value!=null) {
               csvRecord.append(value.toString().replaceAll("^ +","").replaceAll(" +$",""));
             }
             csvRecord.append("\"");
             if (j<usrTbl.getColumnCount()-1) csvRecord.append(",");
           }
          return csvRecord.toString();
    }

    public JScrollPane getScrollList() {
      Vector fieldName = new Vector();
      fieldName.addElement("事業所番号");
      fieldName.addElement("事業所名称");
      fieldName.addElement("郵便番号");
      fieldName.addElement("所在地");
      fieldName.addElement("電話番号");
      fieldName.addElement("FAX番号");
      fieldName.addElement("事業区分");
      fieldName.addElement("地域区分");
      fieldName.addElement("担当者");
      fieldName.addElement("提供サービス");
      dtm = new DefaultTableModel(data, fieldName);
      sorter = new TableSorter2(dtm);
      usrTbl = new JTable(sorter);
      sorter.setTableHeader(usrTbl.getTableHeader());
      sorter.setCellEditableAll(false);
      usrTbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      usrTbl.setRowSelectionAllowed(false);
      usrTbl.setShowGrid(true);
      if (!isSelectable) usrTbl.setCellSelectionEnabled(isSelectable);
      int cid=0;
      DefaultTableCellRenderer ren = new DefaultTableCellRenderer();
      ren.setHorizontalAlignment(SwingConstants.RIGHT);
      //sorter.setColumnClass(0,Integer.class);
      //usrTbl.getColumnModel().getColumn(0).setCellRenderer(ren);
      //usrTbl.getColumnModel().getColumn(cid).setMinWidth(0);
      //usrTbl.getColumnModel().getColumn(cid++).setMaxWidth(0);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(85);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(115);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(70);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(150);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(98);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(98);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(53);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(52);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(80);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(300);
      //usrTbl.getColumnModel().getColumn(cid).setMinWidth(0);
      //usrTbl.getColumnModel().getColumn(cid).setMaxWidth(0);
      //usrTbl.getTableHeader().setReorderingAllowed(false);
      JScrollPane scrPane = new JScrollPane();
      scrPane.getViewport().setView(usrTbl);
      scrPane.setFont(new Font("san-serif",Font.PLAIN,14));
      scrPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      scrPane.getHorizontalScrollBar();
      scrPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scrPane.getVerticalScrollBar();
      scrPane.setPreferredSize(new Dimension(795,410));
      return scrPane;
    }

    public TableSorter2 getSorter() {
        return sorter;
    }

    public Object[][] getSelectedProviders() {
      int rows[] = usrTbl.getSelectedRows();
      Object pdat[][] = new Object[rows.length][3];
      for (int i=0;i<rows.length;i++) {
        pdat[i][0] = new Integer(i);
        pdat[i][1] = usrTbl.getValueAt(rows[i],0);
        pdat[i][2] = usrTbl.getValueAt(rows[i],1);
      }
      return pdat;
    }

    public void removeRows(int pno[]) {
      for (int j=0;j<pno.length;j++) {
        for (int i=0;i<usrTbl.getRowCount();i++) {
          if (pno[j]==Integer.parseInt((usrTbl.getValueAt(i,0)).toString())) {
             dtm.removeRow(sorter.modelIndex(i));
             usrTbl.repaint();
             break;
          }
        }
      }
      usrTbl.repaint();
    }

    public void selectAll() {
      usrTbl.selectAll();
    }
    public void addRow(Vector dat) {
      dtm.insertRow(0,dat);
      usrTbl.repaint();
    }

    public String PDFout() {
      int cid=0;
      float width[] = new float[10];
      int ctype[] = new int[10];
      Arrays.fill(ctype,0);
      width[cid++] = 10;
      ctype[cid] = 22;
      width[cid++] = 18;
      width[cid++] = 8;
      ctype[cid] = 30;
      width[cid++] = 20;
      width[cid++] = 10;
      width[cid++] = 10;
      ctype[cid] = 7;
      width[cid++] = 8;
      ctype[cid] = 7;
      width[cid++] = 8;
      width[cid++] = 13;
      ctype[cid] = 38;
      width[cid] = 30;
      Calendar cal = Calendar.getInstance();
      StringBuffer sb = new StringBuffer();
      sb.append("JIGYOSYO");
      sb.append(".pdf");
      String fname = sb.toString();

      DngPdfTable pdf = new DngPdfTable(fname,25);
        sb.delete(0,sb.length());
        sb.append(cal.get(Calendar.YEAR));
        sb.append("年");
        sb.append(cal.get(Calendar.MONTH)+1);
        sb.append("月");
        sb.append(cal.get(Calendar.DATE));
        sb.append("日");
        pdf.setSubTitle(sb.toString());
      if (pdf.openPDF("事業所一覧")) {
        pdf.setTable(usrTbl,width,ctype,0);
        pdf.flush();
        return fname;
      }
      else {
        return null;
      }
    }
}
