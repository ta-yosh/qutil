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

public class QkanKyotakuData {

    private DngDBAccess dbm;
    private String dbUri;
    private String dbUser;
    private String dbPass;
    public String currentProvider=null;
    public String curProviderName=null;
    public int targetYear=0;
    public int targetMonth=0;
    public int targetDay=0;
    public String targetDate=null;
    private String data[][];
    private int ymdata[][];
    private int initCode[]= new int[7];
    private int ddata[];
    private double tunitRate;
    private double yunitRate;
    public int Rows;
    private DngGenericCombo cBox,ymBox,dBox;
    private JComboBox ymbox,dbox;
    private JScrollPane scp;
    private JPanel tPanel = new JPanel(new BorderLayout());

    private JPanel pn = new JPanel();
    private JPanel pn1 = new JPanel();
    private JPanel pn2 = new JPanel();
    private JPanel pn3 = new JPanel();
    private JPanel pnl = new JPanel(new BorderLayout()); 
    private JTable usrTbl;
    private boolean isSelectable=true;
    private DefaultTableModel dtm;
    private TableSorter2 sorter;

    //private Hashtable tValue = new Hashtable();
    //private Hashtable taUnit = new Hashtable();
    private Hashtable careRate = new Hashtable();
    //private Hashtable ratePlus = new Hashtable();

    public QkanKyotakuData(String dbUri,String dbUser,String dbPass) {
      this.dbUri = dbUri;
      this.dbUser = dbUser;
      this.dbPass = dbPass;
      initCode = new int[]  {0,1111,1111,1221,1223,1131,1241,1261};
      dbm = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
      StringBuffer buf = new StringBuffer();
      buf.append("select PROVIDER_ID,PROVIDER_NAME from PROVIDER ");
      buf.append("where PROVIDER_ID in (");
      buf.append("   select PROVIDER_ID from PROVIDER_SERVICE");
      buf.append("    where SYSTEM_SERVICE_KIND_DETAIL in (13111,13411)");
      buf.append(")");
      String sql = buf.toString(); 
      if (dbm.connect()) {
        dbm.execQuery(sql);
        dbm.Close();
        Rows = dbm.Rows;
        data = new String[Rows][3];
        for (int i=0;i<Rows;i++) {
          StringBuffer sb = new StringBuffer();
          data[i][0] = dbm.getData("PROVIDER_NAME",i).toString()
                      +" ( "
                      +dbm.getData("PROVIDER_ID",i).toString()
                      +" ) ";
          data[i][1] = dbm.getData("PROVIDER_ID",i).toString();
          data[i][2] = dbm.getData("PROVIDER_NAME",i).toString();
        }
        cBox = new DngGenericCombo(data);
        currentProvider = data[0][1];
        curProviderName = data[0][2];
      }
      else Rows=-1;
      careRate.put("1"," 非該当");
      careRate.put("11","経過的要介護");
      careRate.put("12"," 要支援1");
      careRate.put("13"," 要支援2");
      careRate.put("21"," 要介護1");
      careRate.put("22"," 要介護2");
      careRate.put("23"," 要介護3");
      careRate.put("24"," 要介護4");
      careRate.put("25"," 要介護5");
    }

    public JPanel searchCondition() {
      JComboBox cbx = cBox.getComboBox();
      ActionListener pChange = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JComboBox cb = (JComboBox)e.getSource();
          int pvInd = cb.getSelectedIndex();
          currentProvider = getData(pvInd,1);
          curProviderName = getData(pvInd,2);
          targetDay=0;
          YMCondition();
        }
      };
      cbx.addActionListener(pChange);
      pn.add(cbx);
      YMCondition();
      pn.add(pn1);
      tPanel.add(pn,BorderLayout.NORTH);
      tPanel.add(pnl,BorderLayout.CENTER);
      tPanel.add(pn2,BorderLayout.SOUTH);
      return tPanel;
    }
  
    public void YMCondition() {
      if (dbm.connect()) {
        pn1.setVisible(false);
        pn1.removeAll();
        StringBuffer buf = new StringBuffer();
        buf.append("select distinct extract(YEAR from SERVICE_DATE),");
        buf.append("extract(MONTH from SERVICE_DATE) from SERVICE ");
        buf.append("inner join PATIENT on (SERVICE.PATIENT_ID=");
        buf.append("PATIENT.PATIENT_ID and DELETE_FLAG=0) where PROVIDER_ID='");
        buf.append(currentProvider);
        buf.append("' and SYSTEM_SERVICE_KIND_DETAIL in (13111,13411) ");
        buf.append("and SERVICE_USE_TYPE in (4,6) ");
        buf.append(" and SERVICE_DATE is not NULL");
        //buf.append("and WEEK_DAY>0 ");
        buf.append(" order by SERVICE_DATE desc ");
        String sql = buf.toString(); 
        System.out.println(sql);
        dbm.execQuery(sql);
        dbm.Close();
        System.out.println("ROWS : "+dbm.Rows);
        if (dbm.Rows>0) {
          String ym[] = new String[dbm.Rows];
          ymdata = new int[dbm.Rows][2];
          for (int i=0;i<dbm.Rows;i++) {
            //String ymdata[] = dbm.getData("SERVICE_DATE",i).toString().split("-|/");
            String y = dbm.getData(0,i).toString();
            String m = dbm.getData(1,i).toString();
            ymdata[i][0] = Integer.parseInt(y);
            ymdata[i][1] = Integer.parseInt(m);
            ym[i] = "平成"+(new Integer(ymdata[i][0]-1988)).toString()+"年"+m+"月";
            System.out.println(ym[i]);
          }
          targetYear = ymdata[0][0];
          targetMonth= ymdata[0][1];
          ymBox = new DngGenericCombo(ym);
          ymbox = ymBox.getComboBox();
          ActionListener ymChange = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              JComboBox cb = (JComboBox)e.getSource();
              int tInd = cb.getSelectedIndex();
              System.out.println(ymdata[tInd][0]+"/"+ymdata[tInd][1]+" selected");
              if (ymdata[tInd][0]==targetYear && ymdata[tInd][1]==targetMonth) {
                return;
              }
              targetDay = 0;
              targetYear = ymdata[tInd][0];
              targetMonth= ymdata[tInd][1];
              targetDate = targetYear+"-"+targetMonth+"-"+targetDay;
              System.out.println(targetDate);
              dayCondition();
              setKyotakuPanel(targetYear,targetMonth,targetDay);
            }
          };
          ymbox.addActionListener(ymChange);
          pn1.add(ymbox);
          dayCondition();
          pn1.add(pn3);

          setKyotakuPanel(ymdata[0][0],ymdata[0][1],targetDay);
          
        } else {
          JLabel nodata = new JLabel("←該当するデータが有りません。");
          nodata.setFont(new Font("Dialog",Font.PLAIN,12));
          System.out.println("NODATATATATA");
          pn1.add(nodata);
          setKyotakuPanel(0,0,0);
        }
        pn1.setVisible(true);
      }
    }

    private void dayCondition() {
      if (!dbm.connect()) return;
      pn3.setVisible(false);
      pn3.removeAll();
      int nextMonth = targetMonth+1;
      int nextYear = targetYear;
      if (nextMonth==13) {
        nextMonth=1;
        nextYear++;
      }
      String nStart = (new Integer(targetYear).toString())+"-"+(new Integer(targetMonth).toString())+"-01";
      String nEnd = (new Integer(nextYear).toString())+"-"+(new Integer(nextMonth).toString())+"-01";
      StringBuffer buf = new StringBuffer();
      buf.append("select distinct extract(DAY from SERVICE_DATE) from SERVICE");
      buf.append(" inner join PATIENT on (PATIENT.PATIENT_ID");
      buf.append("=SERVICE.PATIENT_ID and DELETE_FLAG=0)"); 
      buf.append(" inner join PATIENT_NINTEI_HISTORY on ");
      buf.append("(PATIENT_NINTEI_HISTORY.PATIENT_ID=SERVICE.PATIENT_ID");
      buf.append(" and INSURE_VALID_END>=SERVICE.SERVICE_DATE");
      buf.append(" and NINTEI_HISTORY_ID in ");
      buf.append("(select NINTEI_HISTORY_ID from ");
      buf.append("PATIENT_NINTEI_HISTORY where PATIENT_ID=SERVICE.PATIENT_ID ");
      buf.append(" and INSURE_VALID_END>='");
      buf.append(nStart);
      buf.append("' and INSURE_VALID_START<'");
      buf.append(nEnd);
      buf.append("')) where SERVICE.PROVIDER_ID='");
      buf.append(currentProvider);
      buf.append("' and extract(YEAR from SERVICE_DATE)='");
      buf.append(targetYear);
      buf.append("' and extract(MONTH from SERVICE_DATE)='");
      buf.append(targetMonth);
      //buf.append("' and SYSTEM_SERVICE_KIND_DETAIL in (13111,13411) ");
      buf.append("' and ((SYSTEM_SERVICE_KIND_DETAIL=13111 and ");
      buf.append("substring(JOTAI_CODE from 1 for 1)=2) or ");
      buf.append("(SYSTEM_SERVICE_KIND_DETAIL=13411 and ");
      buf.append("substring(JOTAI_CODE from 1 for 1)=1)) ");
      buf.append("and SERVICE_USE_TYPE in (4,6) ");
      buf.append("order by SERVICE_DATE desc ");
      String sql = buf.toString(); 
      System.out.println(sql);
      dbm.execQuery(sql);
      dbm.Close();
      if (dbm.Rows>0) {
        String day[] = new String[dbm.Rows+1];
        day[0] = "月間";
        ddata = new int[dbm.Rows+1];
        ddata[0] = 0;
        for (int i=0;i<dbm.Rows;i++) {
          String d = dbm.getData(0,i).toString();
          ddata[i+1] = Integer.parseInt(d);
          day[i+1] = d+"日";
        }
        dBox = new DngGenericCombo(day);
        dbox = dBox.getComboBox();
        ActionListener dChange = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            JComboBox cb = (JComboBox)e.getSource();
            int tInd = cb.getSelectedIndex();
            if (targetDay == ddata[tInd]) return;
            targetDay = ddata[tInd];
            targetDate = targetYear+"-"+targetMonth+"-"+targetDay;
            System.out.println(targetDate);
            setKyotakuPanel(targetYear,targetMonth,targetDay);
          }
        };
        dbox.addActionListener(dChange);
        pn3.add(dbox);
      }
      dbm.connect();
      dbm.execQuery("select provider_area_type from provider where provider_id='"+currentProvider+"'");
      dbm.Close();
      buf.delete(0,buf.length());
      buf.append("select unit_price_value from m_area_unit_price ");
      buf.append("where unit_price_type=");
      buf.append(dbm.getData(0,0).toString());
      buf.append(" and system_service_kind_detail in (13111,13411) ");
      buf.append(" and unit_valid_start <='");
      buf.append(nStart);
      buf.append("' and unit_valid_end >='");
      buf.append(nStart);
      buf.append("' order by system_service_kind_detail");
      dbm.connect();
      dbm.execQuery(buf.toString());
      dbm.Close();
      tunitRate = Float.parseFloat(dbm.getData(0,0).toString());
      yunitRate = Float.parseFloat(dbm.getData(0,1).toString());
      System.out.println("trate = "+tunitRate+" yrate = "+yunitRate);
      pn3.setVisible(true);
    }

    public String getData(int row,int col) {
      return data[row][col];
    }
  
    public void setKyotakuPanel(int targetYear,int targetMonth,int targetDay) {
      pn2.setVisible(false);      
      pn2.removeAll();
      pnl.setVisible(false);      
      pnl.removeAll();
      int detYear = (targetMonth>3) ? targetYear:targetYear-1;
      int nextMonth = targetMonth+1;
      int nextYear = targetYear;
      if (nextMonth==13) {
        nextMonth=1;
        nextYear++;
      }
      String nStart = (new Integer(targetYear).toString())+"-"+(new Integer(targetMonth).toString())+"-01";
      String nEnd = (new Integer(nextYear).toString())+"-"+(new Integer(nextMonth).toString())+"-01";
      if (targetDay>0) 
        nStart = (new Integer(targetYear).toString())+"-"+(new Integer(targetMonth).toString())+"-"+(new Integer(targetDay).toString());
      if (dbm.connect()) {
        StringBuffer buf = new StringBuffer();

        buf.append("select PATIENT_FIRST_NAME,PATIENT_FAMILY_NAME,");
        buf.append("SERVICE.PATIENT_ID,max(SERVICE_ID) as SID,");
        buf.append("SYSTEM_SERVICE_KIND_DETAIL,INSURED_ID,PATIENT_BIRTHDAY,");
        buf.append("substring(JOTAI_CODE from 1 for 1),max(JOTAI_CODE) as JOTAI,");
        buf.append("count(SERVICE.SERVICE_ID),INSURE_RATE,");
        buf.append("min(INSURE_VALID_START) as INSURE_VALID_START,");
        buf.append("max(INSURE_VALID_END) as INSURE_VALID_END,");
        buf.append("max(BENEFIT_RATE) as BRATE");
        buf.append(" from SERVICE inner join PATIENT on ");
        buf.append("(PATIENT.PATIENT_ID=SERVICE.PATIENT_ID and DELETE_FLAG=0)");
        buf.append(" inner join PATIENT_NINTEI_HISTORY on ");
        buf.append("(PATIENT_NINTEI_HISTORY.PATIENT_ID=SERVICE.PATIENT_ID and");
        buf.append(" NINTEI_HISTORY_ID in ");
        buf.append("(select NINTEI_HISTORY_ID from ");
        buf.append("PATIENT_NINTEI_HISTORY where PATIENT_ID=SERVICE.PATIENT_ID ");
        //
        //buf.append(" and INSURE_VALID_END>=SERVICE.SERVICE_DATE");
        buf.append(" and INSURE_VALID_END>='");
        buf.append(nStart);
        if (targetDay>0) {
          buf.append("' and INSURE_VALID_START<='");
          buf.append(nStart);
          //buf.append("'");
        }
        if (targetDay==0) {
          buf.append("' and INSURE_VALID_START<'");
          buf.append(nEnd);
        }
        //
        //buf.append("' group by INSURED_ID)) left outer join PATIENT_KOHI on ");
        buf.append("')) left outer join PATIENT_KOHI on ");
        buf.append("(PATIENT_KOHI.PATIENT_ID=SERVICE.PATIENT_ID and ");
        //buf.append("INSURE_TYPE='1' and KOHI_VALID_END>=SERVICE_DATE");
        buf.append("INSURE_TYPE='1' and KOHI_VALID_END>='");
        buf.append(nStart);
        if (targetDay>0) {
          buf.append("' and KOHI_VALID_START<='");
          buf.append(nStart);
          //buf.append("'");
        }
        if (targetDay==0) {
          buf.append("' and KOHI_VALID_START<'");
          buf.append(nEnd);
        }
        buf.append("' and KOHI_LAW_NO not in (10,15,21,57,58,81))");
        buf.append(" where ((SYSTEM_SERVICE_KIND_DETAIL=13111 and ");
        buf.append("substring(JOTAI_CODE from 1 for 1)=2) or ");
        buf.append("(SYSTEM_SERVICE_KIND_DETAIL=13411 and ");
        buf.append("substring(JOTAI_CODE from 1 for 1)=1)) ");
        buf.append("and extract(YEAR from SERVICE_DATE)=");
        buf.append(targetYear);
        buf.append(" and extract(MONTH from SERVICE_DATE)=");
        buf.append(targetMonth);
        if (targetDay>0) {
          buf.append(" and extract(DAY from SERVICE_DATE)=");
          buf.append(targetDay);
        }
        buf.append(" and SERVICE_DATE>=INSURE_VALID_START");
        buf.append(" and SERVICE_DATE<=INSURE_VALID_END");
        buf.append(" and SERVICE.PROVIDER_ID='");
        buf.append(currentProvider);
        buf.append("' and SERVICE_USE_TYPE in (4,6)");
        //buf.append("' and INSURE_VALID_END>=SERVICE.SERVICE_DATE");
        buf.append(" group by SERVICE.PATIENT_ID,PATIENT_FIRST_NAME,");
        buf.append("PATIENT_FAMILY_NAME,PATIENT_BIRTHDAY,INSURED_ID,");
        buf.append("SYSTEM_SERVICE_KIND_DETAIL,");
        buf.append("substring(JOTAI_CODE from 1 for 1),INSURE_RATE");
        buf.append(" order by PATIENT_FAMILY_NAME,PATIENT_FIRST_NAME,INSURED_ID,SYSTEM_SERVICE_KIND_DETAIL");

        String sql = buf.toString();
        System.out.println(sql);
        dbm.execQuery(sql);
        dbm.Close();
        Vector pdata = new Vector();
        DngDBAccess dbm2 = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
        for (int i=0;i<dbm.Rows;i++){
          int pointCode=0;
          int sCount = Integer.parseInt(dbm.getData("COUNT",i).toString());
          int insRate = (dbm.getData("BRATE",i)==null) ? 
                   Integer.parseInt(dbm.getData("INSURE_RATE",i).toString()):
                   Integer.parseInt(dbm.getData("BRATE",i).toString());
          String insStart = dbm.getData("INSURE_VALID_START",i).toString();
          String insEnd = dbm.getData("INSURE_VALID_END",i).toString();
          String insId = dbm.getData("INSURED_ID",i).toString();

          Vector pline = new Vector();
          int pNo = new Integer(dbm.getData(2,i).toString()).intValue();
          //pline.addElement(dbm.getData(2,i).toString());
          int sNo = new Integer(dbm.getData(3,i).toString()).intValue();
          //pline.addElement(dbm.getData(3,i).toString());
          String nam1=(dbm.getData(1,i)!=null) ? dbm.getData(1,i).toString()+" ":"";
          String nam2=(dbm.getData(0,i)!=null) ? dbm.getData(0,i).toString():"";
          String nam =" "+nam1+nam2;
          pline.addElement(new Integer(i+1));
          pline.addElement(insId);
          pline.addElement(nam);
          if (dbm.getData("PATIENT_BIRTHDAY",i)!=null) {
            int age =patientAge(dbm.getData("PATIENT_BIRTHDAY",i).toString());
            pline.addElement(new Integer(age));
          } else {
            pline.addElement("");
          }
          String cR="1";
          int sbp = Integer.parseInt(dbm.getData(4,i).toString());
          String kind = (sbp==13111) ? 
                        "":"予防";
          if (targetDay>0) {
            if (dbm.getData("JOTAI",i)!=null) {
              cR = dbm.getData("JOTAI",i).toString();
              String cRate = (String)careRate.get(cR);
              pline.addElement(cRate);
            } else {
              pline.addElement("");
            }
            //pline.addElement(kind);
            buf.delete(0,buf.length());
            buf.append("select SERVICE_ID,SYSTEM_BIND_PATH,");
            buf.append("extract(HOUR from DETAIL_VALUE),");
            buf.append("extract(MINUTE from DETAIL_VALUE)");
            buf.append(" from SERVICE_DETAIL_DATE_");
            buf.append(detYear);
            buf.append(" where SERVICE_ID=");
            buf.append(sNo);
            buf.append(" and SYSTEM_BIND_PATH");
            buf.append(" in (3,4)");
            buf.append(" order by SYSTEM_BIND_PATH;");
            sql = buf.toString();
            System.out.println(sql);
            dbm2.connect();
            dbm2.execQuery(sql);
            dbm2.Close();
            String ti,hou,min;
            if (dbm2.getData(2,0)!=null) {
              hou = dbm2.getData(2,0).toString();
              min = dbm2.getData(3,0).toString();
              if (hou.length()==1) hou = "0"+hou;
              if (min.length()==1) min = "0"+min;
              ti = hou+":"+min;
            } else {
              ti = "";
            }
            pline.addElement(ti);
            if (dbm2.getData(2,1)!=null) {
              hou = dbm2.getData(2,1).toString();
              min = dbm2.getData(3,1).toString();
              if (hou.length()==1) hou = "0"+hou;
              if (min.length()==1) min = "0"+min;
              ti = hou+":"+min;
            } else {
              ti = "";
            }
            pline.addElement(ti);
            buf.delete(0,buf.length());
            buf.append("select SERVICE_ID,SYSTEM_BIND_PATH,");
            buf.append("DETAIL_VALUE");
            buf.append(" from SERVICE_DETAIL_INTEGER_");
            buf.append(detYear);
            buf.append(" where SERVICE_ID=");
            buf.append(sNo);
            buf.append(" and SYSTEM_BIND_PATH");
            buf.append(" in (14,1310103,1310104,1310105,1310107,1310108,1310109,1310110,1310111,1310112,1310113,1340101,1340102,1340103,1340104,1340105,1340106,1340107,1340108,1340109,1340110)");
            buf.append(" order by SYSTEM_BIND_PATH;");
            sql = buf.toString();
            System.out.println(sql);
            dbm2.connect();
            dbm2.execQuery(sql);
            dbm2.Close();
  
            System.out.println("dbm2:"+dbm2.Rows);
            int inic=0;
            int hId=0;
            double unitRate;
            boolean again = false;
            boolean cancer = false;
            int kaisei = 0;
            unitRate = (sbp==13111) ? tunitRate:yunitRate;
            for (int j=0;j<dbm2.Rows;j++){
              System.out.println("Rows start: "+j);
              int sbp0 = Integer.parseInt(dbm2.getData("SYSTEM_BIND_PATH",j).toString());
              if (sbp0==14) {
                kaisei = Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                System.out.println("kaisei = "+kaisei);
              }
              else if (sbp0==1310103 || sbp0==1340101 || sbp0==1310111 || sbp0==1340108) {
                hId = Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                System.out.println("hId = "+hId);
                inic += initCode[hId];
              } 
              else if (sbp0==1310104 || sbp0==1340102) {
                if (dbm2.getData("DETAIL_VALUE",j).toString().equals("2")) inic++;
              }
              else if (sbp0==1310105 || sbp0==1340103) {
                if (dbm2.getData("DETAIL_VALUE",j).toString().equals("2")) inic++;
              }
              else if (sbp0==1310107 || sbp0==1340104) {
                if (dbm2.getData("DETAIL_VALUE",j).toString().equals("2")) inic = inic-10;
              }
              else if (kaisei==0 && (sbp0==1310108 || sbp0==1340105) ) {
                if (dbm2.getData("DETAIL_VALUE",j).toString().equals("2")) inic = inic+30;
              }
              else if (kaisei==0 && (sbp0==1310109 || sbp0==1340106) ) {
                if (dbm2.getData("DETAIL_VALUE",j).toString().equals("2")) again=true;
              }
              else if (sbp0==1310110 || sbp0==1340107) {
                if (kaisei==0 && hId==4) {
                  if (dbm2.getData("DETAIL_VALUE",j).toString().equals("2") && again) inic = inic+30;
                  else if (dbm2.getData("DETAIL_VALUE",j).toString().equals("1") && again) inic = inic+2;
                }
                else if (kaisei==20090401) {
                  if (dbm2.getData("DETAIL_VALUE",j).toString().equals("2")) cancer=true;
                }
              }
              else if (hId==7 && (sbp0==1310112 || sbp0==1340109)) {
                if (dbm2.getData("DETAIL_VALUE",j).toString().equals("2")) inic = inic+1;
              }
              else if (kaisei==20090401 && hId>=3 && hId<=6 && (sbp0==1310113 || sbp0==1340110)) {
                if (dbm2.getData("DETAIL_VALUE",j).toString().equals("1")) {
                  if (hId==4 && cancer) inic += 32; 
                }
                else if (dbm2.getData("DETAIL_VALUE",j).toString().equals("2")) {
                  if (hId==3) inic += 30; 
                  if (hId==4) inic += (cancer) ? 30:2; 
                  if (hId==5) inic += 1;
                  if (hId==6) inic += (sbp0==1310113) ? 2:1;
                }
              }
            }
            System.out.println("inic : "+inic);
            pointCode = inic;
            buf.delete(0,buf.length());
            buf.append("select service_unit,service_name from m_service_code ");
            buf.append("where system_service_kind_detail='");
            buf.append(sbp);
            buf.append("' and  service_code_item='");
            buf.append(pointCode);
            buf.append("' and service_valid_end>='");
            buf.append(nStart);
            buf.append("' and service_valid_start<='");
            buf.append(nStart);
            buf.append("'");
            dbm2.connect();
            System.out.println(buf.toString());
            dbm2.execQuery(buf.toString());
            dbm2.Close();
            if (dbm2.Rows>0) {
              int p = Integer.parseInt(dbm2.getData(0,0).toString());
              String sName = dbm2.getData(1,0).toString();
              int hiyou =(int)((double) p * unitRate);
              int futan = hiyou - (int)((double)hiyou/100.0*(double)insRate);
              //if (hiyou%10>0) futan +=1;
              System.out.println("p = "+p+" hiyou = "+hiyou+" futan = "+futan);
              pline.addElement(sName);
              pline.addElement(new Integer(hiyou));
              pline.addElement(new Integer(futan));
            }
          } 
          else if (targetDay==0) {
            buf.delete(0,buf.length());
            buf.append("select JOTAI_CODE from PATIENT_NINTEI_HISTORY ");
            buf.append("where PATIENT_ID=");
            buf.append(pNo);
            buf.append(" and NINTEI_HISTORY_ID=(select");
            buf.append(" max(NINTEI_HISTORY_ID) from PATIENT_NINTEI_HISTORY ");
            buf.append("where INSURE_VALID_END='");
            buf.append(insEnd);
            buf.append("' and PATIENT_ID=");
            buf.append(pNo);
            buf.append(")");
            System.out.println(buf.toString());
            DngDBAccess dbm4 = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
            dbm4.connect();
            dbm4.execQuery(buf.toString());
            dbm4.Close();
            if (dbm4.Rows>0 && dbm4.getData("JOTAI_CODE",0)!=null) {
              cR = dbm4.getData("JOTAI_CODE",0).toString();
              String cRate = (String)careRate.get(cR);
              pline.addElement(cRate);
            } else {
              pline.addElement("");
            }
            pline.addElement(kind);

            buf.delete(0,buf.length());
            buf.append("select distinct extract(YEAR from CLAIM_DATE),");
            buf.append("extract(MONTH from CLAIM_DATE) from CLAIM where PATIENT_ID=");
            buf.append(pNo);
            buf.append(" and extract(YEAR from TARGET_DATE)=");
            buf.append(targetYear);
            buf.append(" and extract(MONTH from TARGET_DATE)=");
            buf.append(targetMonth);
            buf.append(" and CATEGORY_NO=7 and PROVIDER_ID='");
            buf.append(currentProvider);
            buf.append("'");
            DngDBAccess dbm5 = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
            dbm5.connect();
            dbm5.execQuery(buf.toString());
            dbm5.Close();
            int cRows;
            if (dbm5.Rows>0) {
              int cYear = Integer.parseInt(dbm5.getData(0,0).toString());
              if (Integer.parseInt(dbm5.getData(1,0).toString())<4) cYear--;

              buf.delete(0,buf.length());
              buf.append("select CLAIM_ID,SYSTEM_BIND_PATH,");
              buf.append("DETAIL_VALUE");
              buf.append(" from CLAIM_DETAIL_TEXT_");
              buf.append(cYear);
              //buf.append(" where CLAIM_ID = (select CLAIM_ID from CLAIM");

              buf.append(" where CLAIM_ID = (select CLAIM_ID from CLAIM_DETAIL_TEXT_");
              buf.append(cYear);
              buf.append(" where CLAIM_ID in (select CLAIM_ID from CLAIM");
  
              buf.append(" where PATIENT_ID=");
              buf.append(pNo);
              buf.append(" and INSURED_ID=");
              buf.append(insId);
              buf.append(" and extract(YEAR from TARGET_DATE)=");
              buf.append(targetYear);
              buf.append(" and extract(MONTH from TARGET_DATE)=");
              buf.append(targetMonth);
              buf.append(" and CATEGORY_NO=7 and PROVIDER_ID='");
              buf.append(currentProvider);
              buf.append("') and SYSTEM_BIND_PATH=701007 ");
              if (sbp==13111) buf.append("and DETAIL_VALUE='31')");
              else buf.append("and DETAIL_VALUE='34')");
              buf.append(" and SYSTEM_BIND_PATH");
              buf.append(" in (701008,701014,701015,701016,701017,701020)");
              buf.append(" order by SYSTEM_BIND_PATH;");
              sql = buf.toString();
              System.out.println(sql);
              dbm2.connect();
              dbm2.execQuery(sql);
              dbm2.Close();
              cRows = dbm2.Rows;
            }
            else cRows=0;
            DngDBAccess dbm3 = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
            if (dbm3.connect()) {
              buf.delete(0,buf.length());
              buf.append("select extract(DAY from SERVICE_DATE) from service");
              buf.append(" inner join PATIENT_NINTEI_HISTORY on ");
              buf.append("(PATIENT_NINTEI_HISTORY.PATIENT_ID=");
              buf.append(pNo);
              buf.append(" and NINTEI_HISTORY_ID in ");
              buf.append("(select NINTEI_HISTORY_ID from ");
              buf.append("PATIENT_NINTEI_HISTORY where PATIENT_ID=");
              buf.append(pNo);
              buf.append(" and INSURE_VALID_END>=SERVICE.SERVICE_DATE");
              buf.append(" and INSURE_VALID_START<=SERVICE.SERVICE_DATE");
              buf.append(")) where SERVICE.PATIENT_ID=");
              buf.append(pNo);
              buf.append(" and ((SYSTEM_SERVICE_KIND_DETAIL=13111 and ");
              buf.append("substring(JOTAI_CODE from 1 for 1)=2) or ");
              buf.append("(SYSTEM_SERVICE_KIND_DETAIL=13411 and ");
              buf.append("substring(JOTAI_CODE from 1 for 1)=1))");
              //buf.append(" and SYSTEM_SERVICE_KIND_DETAIL in (13111,13411)");
              buf.append(" and SERVICE_USE_TYPE in (4,6)");
              buf.append(" and extract(YEAR from SERVICE_DATE)=");
              buf.append(targetYear);
              buf.append(" and extract(MONTH from SERVICE_DATE)=");
              buf.append(targetMonth);
              buf.append(" and SERVICE_DATE<='");
              buf.append(insEnd);
              buf.append("' and SERVICE_DATE>='");
              buf.append(insStart);
              buf.append("' and SERVICE.PROVIDER_ID='");
              buf.append(currentProvider);
              buf.append("' group by extract(DAY from SERVICE_DATE)");
              System.out.println(buf.toString());
              dbm3.execQuery(buf.toString());
              dbm3.Close();
              if (dbm3.Rows>0) {
                if (cRows>0) pline.addElement(new Integer(dbm2.getData("DETAIL_VALUE",0).toString()));
                else pline.addElement(new Integer(dbm3.Rows));
                StringBuffer sb = new StringBuffer();
                for (int l=0;l<dbm3.Rows;l++) {
                  sb.append((l>0) ? ", ":" ");
                  if (Integer.parseInt(dbm3.getData(0,l).toString())<10) sb.append("0");
                  sb.append(dbm3.getData(0,l));
                  sb.append("日");
                }
                pline.addElement(sb.toString());
                if (cRows>0) {
                  int hiyou = (int)(Float.parseFloat(dbm2.getData("DETAIL_VALUE",1).toString())*Float.parseFloat(dbm2.getData("DETAIL_VALUE",2).toString()));
                  int futan = Integer.parseInt(dbm2.getData("DETAIL_VALUE",4).toString());
                  int self = (dbm2.getData("DETAIL_VALUE",5)!=null) ?
                    Integer.parseInt(dbm2.getData("DETAIL_VALUE",5).toString()):
                    0;
                  if (self>0) futan=self;
                  pline.addElement(new Integer(hiyou));
                  pline.addElement(new Integer(futan));
                }
              }
              else {
                pline.addElement(new Integer(sCount));
                pline.addElement("");
                pline.addElement("");
                pline.addElement("");
              }
            }
          }
          pdata.addElement(pline);
        }
        scp = getScrollList(pdata,targetDay);
        pn2.add(scp);
      }
      if (dbm.Rows>0) {
        if (targetDay>0) {
          JLabel lab1 = new JLabel("＊日単位での金額について：負担金額は端数処理の関係で月間金額とは異なる場合があります。 ");
          lab1.setFont(new Font("Dialog",Font.PLAIN,11));
          pnl.add(lab1,BorderLayout.NORTH);
        } else {
          JLabel lab1 = new JLabel("＊月間での金額は、実績確定分のみ表示されます。 ");
          lab1.setFont(new Font("Dialog",Font.PLAIN,11));
          JLabel lab2 = new JLabel("＊月途中で要介護度が変わっている場合、変更後の要介護度が表示されます。また、要支援←→要介護の場合は別行となります。");
          lab2.setFont(new Font("Dialog",Font.PLAIN,11));
          pnl.add(lab1,BorderLayout.NORTH);
          pnl.add(lab2,BorderLayout.CENTER);
        }
        pnl.setBorder(BorderFactory.createLineBorder(Color.black));
      } else {
        JLabel lab1 = new JLabel(" ");
        lab1.setFont(new Font("Dialog",Font.PLAIN,11));
        pnl.add(lab1,BorderLayout.CENTER);
        pnl.setBorder(null);
      }
      pnl.setVisible(true);
      pn2.setVisible(true);
    }

    public boolean isSelected() {
      //int sel = usrTbl.getSelectedRow();
      //return (sel!=-1) ? true:false;
      return true;
    }

    public void setSelectable(boolean selectable) {
      isSelectable = selectable;
    }
    public String getKyotakuDataCsv(int pno) {
      StringBuffer csvRecord;
      //for (int i=0;i<usrTbl.getRowCount();i++) {
        //if (pno==Integer.parseInt((usrTbl.getValueAt(i,2)).toString())) {
           csvRecord = new StringBuffer();
           for (int j=0;j<usrTbl.getColumnCount();j++) {
             Object value;
             csvRecord.append("\"");
             if (pno<0) value=(Object)usrTbl.getColumnName(j);
             else value=usrTbl.getValueAt(pno,j);
             if (value!=null) {
               csvRecord.append(value.toString().replaceAll("^ +","").replaceAll(" +$",""));
             }
             csvRecord.append("\"");
             if (j<usrTbl.getColumnCount()-1) csvRecord.append(",");
           }
          return csvRecord.toString();
        //}
      //}
      //return null;
    }

    public JScrollPane getScrollList(Vector data,int td) {
      Vector fieldName = new Vector();
      //fieldName.addElement("ID");
      //fieldName.addElement("サービスID");
      fieldName.addElement("No.");
      fieldName.addElement("被保険者番号");
      fieldName.addElement("氏名");
      fieldName.addElement("年齢");
      fieldName.addElement("要介護度");
      if (td>0) {
        fieldName.addElement("開始時刻");
        fieldName.addElement("終了時刻");
        fieldName.addElement("サービス名称");
      } 
      if (td==0) {
        fieldName.addElement("種類");
        fieldName.addElement("回数");
        fieldName.addElement("訪問日");
      }
      fieldName.addElement("費用");
      fieldName.addElement("負担額");
      dtm = new DefaultTableModel(data, fieldName);
      sorter = new TableSorter2(dtm);
      usrTbl = new JTable(sorter);
      sorter.setTableHeader(usrTbl.getTableHeader());
      sorter.setCellEditableAll(false);
      //sorter.setPrimaryKeyCol(0);
      //sorter.addMouseListenerToHeaderInTable(usrTbl);
      usrTbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      usrTbl.setRowSelectionAllowed(false);
      //usrTbl.setDefaultEditor(Object.class, null);
      usrTbl.setShowGrid(true);
      if (!isSelectable) usrTbl.setCellSelectionEnabled(isSelectable);
      int cid=0;
      DefaultTableCellRenderer ren = new DefaultTableCellRenderer();
      ren.setHorizontalAlignment(SwingConstants.RIGHT);
      sorter.setColumnClass(0,Integer.class);
      sorter.setColumnClass(3,Integer.class);
      if (td==0) {
        sorter.setColumnClass(6,Integer.class);
        sorter.setColumnClass(8,Integer.class);
        sorter.setColumnClass(9,Integer.class);
      } else {
        sorter.setColumnClass(8,Integer.class);
        sorter.setColumnClass(9,Integer.class);
      }
      usrTbl.getColumnModel().getColumn(0).setCellRenderer(ren);
      usrTbl.getColumnModel().getColumn(3).setCellRenderer(ren);

      //usrTbl.getColumnModel().getColumn(0).setMinWidth(0);
      //usrTbl.getColumnModel().getColumn(0).setMaxWidth(0);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(ren);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(88);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(110);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(60);
      if (td>0) {
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(60);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(60);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(160);
      } 
      if (td==0) {
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(30);
        usrTbl.getColumnModel().getColumn(cid).setCellRenderer(ren);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(30);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(140);
      }
      //sorter.setColumnClass(cid,Integer.class);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(ren);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(80);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(ren);
      usrTbl.getColumnModel().getColumn(cid).setPreferredWidth(80);
      //usrTbl.getTableHeader().setReorderingAllowed(false);
      JScrollPane scrPane = new JScrollPane();
      scrPane.getViewport().setView(usrTbl);
      scrPane.setFont(new Font("san-serif",Font.PLAIN,14));
      scrPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      scrPane.getHorizontalScrollBar();
      scrPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scrPane.getVerticalScrollBar();
      scrPane.setPreferredSize(new Dimension(740,410));
      return scrPane;
    }

    public TableSorter2 getSorter() {
        return sorter;
    }

    public Object[][] getSelectedPatients() {
      int rows[] = usrTbl.getSelectedRows();
      Object pdat[][] = new Object[rows.length][5];
      for (int i=0;i<rows.length;i++) {
        pdat[i][0] = new Integer(i);
        pdat[i][1] = usrTbl.getValueAt(rows[i],0);
        pdat[i][2] = usrTbl.getValueAt(rows[i],1);
        pdat[i][3] = usrTbl.getValueAt(rows[i],2);
        pdat[i][4] = usrTbl.getValueAt(rows[i],3);
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
      int num=0;
      float width[] = new float[10];
      int ctype[] = new int[10];
      Arrays.fill(ctype,0);
      ctype[cid] = 2; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = 4; //No.
      width[cid++] = 7; //被保険者番号
      width[cid++] = 16; //氏名
      ctype[cid] = 2; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = 4; //年齢
      width[cid++] = 6; //要介護度
      if (targetDay>0) {
        width[cid++] = 6; //開始時刻
        width[cid++] = 6; //終了時刻
        width[cid++] = 14; //サービス名称
      } 
      if (targetDay==0) {
        width[cid++] = 3; //種類
        ctype[cid] = 2; // 0 - normal 1 - add comma 2 - align right
        width[cid++] = 3; //回数
        width[cid++] = 14; //訪問日
      }
      ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = 7; //費用
      ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = 7; //負担額
      //Calendar cal = Calendar.getInstance();
      //String date=cal.get(Calendar.YEAR)+""+(cal.get(Calendar.MONTH) + 1)
      //            +""+cal.get(Calendar.DATE);
      //String fname = "TSUSYO"+date+".pdf";
      StringBuffer sb = new StringBuffer();
      sb.append("KYOTAKU-");
      sb.append(currentProvider);
      sb.append("_");
      sb.append(targetYear);
      if (targetMonth<10) sb.append("0");
      sb.append(targetMonth);
      if (targetDay>0) {
        if (targetDay<10) sb.append("0");
        sb.append(targetDay);
      } else {
        sb.append("M");
      }
      sb.append(".pdf");
      String fname = sb.toString();

      DngPdfTable pdf = new DngPdfTable(fname,1);
        sb.delete(0,sb.length());
        sb.append(curProviderName);
        sb.append(" ");
        sb.append(targetYear);
        sb.append("年");
        if (targetMonth<10) sb.append("0");
        sb.append(targetMonth);
        sb.append("月");
        if (targetDay>0) {
          if (targetDay<10) sb.append("0");
          sb.append(targetDay);
          sb.append("日");
        } 
        sb.append(" 提供分");
        pdf.setSubTitle(sb.toString());
      if (pdf.openPDF("居宅療養看護指導情報")) {
        pdf.setTable(usrTbl,width,ctype,0);
        pdf.flush();
        return fname;
      }
      else {
        return null;
      }
    }

    int patientAge(String birthday) {
       String bd[] = new String[3];
       if (birthday.matches("[0-9]+-[0-9]+-[0-9]+")) bd = birthday.split("-");
       if (birthday.matches("[0-9]+/[0-9]+/[0-9]+")) bd = birthday.split("/");
       int yy = Integer.parseInt(bd[0]);
       int mm = Integer.parseInt(bd[1]);
       int dd = Integer.parseInt(bd[2]);
       Calendar c = Calendar.getInstance();
       int age = c.get(c.YEAR)-yy;
       int mon = c.get(c.MONTH);
       if ((mm-mon) > 0) age--;
       else if (mm==mon && dd - c.get(c.DATE) > 0) age--;
       return age;
    }
}
