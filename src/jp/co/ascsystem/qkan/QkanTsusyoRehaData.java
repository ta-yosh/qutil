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

public class QkanTsusyoRehaData {

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
    private int ddata[];
    private double tunitRate;
    private double yunitRate;
    private double kaizenRate;
    private int kaizen1=0;
    private int kaizen2=0;
    private String taKaizenCode[];
    private String yaKaizenCode[];
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
    private Vector totalRow;
    private boolean isSelectable=true;
    private DefaultTableModel dtm;
    private TableSorter2 sorter;

    private Hashtable tValue = new Hashtable();
    private Hashtable yValue = new Hashtable();
    private Hashtable taUnit = new Hashtable();
    private Hashtable yaUnit = new Hashtable();
    private Hashtable careRate = new Hashtable();
    private Hashtable careCode = new Hashtable();

    public QkanTsusyoRehaData(String dbUri,String dbUser,String dbPass) {
      this.dbUri = dbUri;
      this.dbUser = dbUser;
      this.dbPass = dbPass;
      dbm = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
      StringBuffer buf = new StringBuffer();
      buf.append("select PROVIDER_ID,PROVIDER_NAME from PROVIDER ");
      buf.append("where PROVIDER_ID in (");
      buf.append("   select PROVIDER_ID from PROVIDER_SERVICE");
      buf.append("    where SYSTEM_SERVICE_KIND_DETAIL in (11611,16611)");
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

        //System.out.println("create test ="+dbm.execUpdate("create table test_t (id integer,test varchar(8))"));
      }
      else Rows=-1;
      careRate.put("1","非該当"); 
      careRate.put("11","経過的要介護");
      careRate.put("12","要支援1");
      careRate.put("13","要支援2");
      careRate.put("21","要介護1");
      careRate.put("22","要介護2");
      careRate.put("23","要介護3");
      careRate.put("24","要介護4");
      careRate.put("25","要介護5");
      careCode.put("12","3");
      careCode.put("13","4");
      careCode.put("21","5");
      careCode.put("22","6");
      careCode.put("23","7");
      careCode.put("24","8");
      careCode.put("25","9");
      pn.setOpaque(false);
      pn1.setOpaque(false);
      pn2.setOpaque(false);
      pn3.setOpaque(false);
      pnl.setOpaque(false);
      tPanel.setOpaque(false);
    }

    public void setUnit(String dat) {
      StringBuffer buf = new StringBuffer();
      buf.append("select service_code_item,service_unit,system_service_kind_detail,system_service_code_item from m_service_code ");
      buf.append(" where system_service_kind_detail in (11611,16611) ");
      buf.append(" and SUBSTRING(system_service_code_item from 1 for 1)='Z'");
      buf.append(" and service_valid_start<='");
      buf.append(dat);
      buf.append("' and service_valid_end>='");
      buf.append(dat);
      buf.append("' order by system_service_kind_detail,service_code_item");
      System.out.println(buf.toString());
      if (dbm.connect()) {
        dbm.execQuery(buf.toString());
        taUnit.put("1160105",(new int[] {0,0,Integer.parseInt(dbm.getData(1,0).toString())}));
        taUnit.put("1160107",(new int[] {0,0,Integer.parseInt(dbm.getData(1,1).toString())}));
        taUnit.put("1160111",(new int[] {0,0,Integer.parseInt(dbm.getData(1,2).toString())}));
        taUnit.put("1160112",(new int[] {0,0,Integer.parseInt(dbm.getData(1,3).toString()),Integer.parseInt(dbm.getData(1,4).toString()),0}));
        taUnit.put("1160114",(new int[] {0,0,Integer.parseInt(dbm.getData(1,5).toString())}));
        taUnit.put("1160115",(new int[] {0,0,Integer.parseInt(dbm.getData(1,6).toString())}));
        taUnit.put("1160118",(new int[] {0,0,Integer.parseInt(dbm.getData(1,15).toString())}));
        taUnit.put("1160119",(new int[] {0,0,Integer.parseInt(dbm.getData(1,17).toString())}));
        taUnit.put("1160120",(new int[] {0,0,Integer.parseInt(dbm.getData(1,18).toString())}));
        taUnit.put("1160121",(new int[] {0,0,Integer.parseInt(dbm.getData(1,14).toString())}));
        taUnit.put("1160122",(new int[] {0,0,Integer.parseInt(dbm.getData(1,9).toString()),Integer.parseInt(dbm.getData(1,10).toString())}));
        taUnit.put("1160123",(new int[] {0,0,Integer.parseInt(dbm.getData(1,7).toString())}));
        taUnit.put("12",(new int[] {0,0,Integer.parseInt(dbm.getData(1,19).toString())}));
        taUnit.put("16",(new int[] {0,0,Integer.parseInt(dbm.getData(1,8).toString())}));
        taUnit.put("KAIZEN",(new int[] {0,0,Integer.parseInt(dbm.getData(1,11).toString()),Integer.parseInt(dbm.getData(1,12).toString()),Integer.parseInt(dbm.getData(1,13).toString())}));
        taKaizenCode = new String[] {"","",dbm.getData(3,11).toString(),dbm.getData(3,12).toString(),dbm.getData(3,13).toString()};
        yaUnit.put("1660103",(new int[] {0,0,Integer.parseInt(dbm.getData(1,20).toString())}));
        yaUnit.put("1660104",(new int[] {0,0,Integer.parseInt(dbm.getData(1,21).toString())}));
        yaUnit.put("1660105",(new int[] {0,0,Integer.parseInt(dbm.getData(1,22).toString())}));
        yaUnit.put("1660106",(new int[] {0,0,Integer.parseInt(dbm.getData(1,23).toString())}));
        yaUnit.put("MULTI",(new int[] {0,0,0,Integer.parseInt(dbm.getData(1,24).toString()),0,Integer.parseInt(dbm.getData(1,25).toString()),Integer.parseInt(dbm.getData(1,26).toString()),Integer.parseInt(dbm.getData(1,27).toString())}));
        yaUnit.put("1660107",(new int[] {0,0,Integer.parseInt(dbm.getData(1,36).toString())}));
        yaUnit.put("1660108",(new int[] {0,0,Integer.parseInt(dbm.getData(1,28).toString()),Integer.parseInt(dbm.getData(1,30).toString()),0,Integer.parseInt(dbm.getData(1,29).toString()),Integer.parseInt(dbm.getData(1,31).toString())}));
        yaUnit.put("KAIZEN",(new int[] {0,0,Integer.parseInt(dbm.getData(1,37).toString()),Integer.parseInt(dbm.getData(1,38).toString()),Integer.parseInt(dbm.getData(1,39).toString())}));
        yaUnit.put("12",(new int[] {0,0,Integer.parseInt(dbm.getData(1,40).toString()),0,Integer.parseInt(dbm.getData(1,41).toString())}));
        yaUnit.put("16",(new int[] {0,0,Integer.parseInt(dbm.getData(1,32).toString()),0,Integer.parseInt(dbm.getData(1,34).toString()),0,Integer.parseInt(dbm.getData(1,33).toString()),0,Integer.parseInt(dbm.getData(1,35).toString())}));
        yaKaizenCode = new String[] {"","",dbm.getData(3,37).toString(),dbm.getData(3,38).toString(),dbm.getData(3,39).toString()};
        buf.delete(0,buf.length());
        buf.append("select provider_id,system_service_kind_detail,");
        buf.append("system_bind_path,detail_value from ");
        buf.append("PROVIDER_SERVICE_DETAIL_INTEGER,PROVIDER_SERVICE ");
        buf.append("where provider_service.provider_service_id = ");
        buf.append("provider_service_detail_integer.provider_service_id ");
        buf.append("and provider_id='");
        buf.append(currentProvider);
        buf.append("' and system_service_kind_detail in ('11611','16611') ");
        buf.append("and system_bind_path = 4 ");
        buf.append("order by system_service_kind_detail");
        dbm.connect();
        dbm.execQuery(buf.toString());
        dbm.Close();
        System.out.println(buf.toString());
        int mountFlg[] = new int[] {0,0,0,0};
        if (dbm.Rows==1) {
          if (Integer.parseInt(dbm.getData(1,0).toString())==11611) {
            kaizen1 = Integer.parseInt(dbm.getData(3,0).toString());
          } else {
            kaizen2 = Integer.parseInt(dbm.getData(3,0).toString());
          }
        }
        if (dbm.Rows==2) {
          kaizen1 = Integer.parseInt(dbm.getData(3,0).toString());
          kaizen2 = Integer.parseInt(dbm.getData(3,1).toString());
        }
      }
      String osn = System.getProperty("os.name").substring(0,3);
      tValue.put("1160104",(new String[] {"","1\uff5e2hr","2\uff5e3hr","3\uff5e4hr","4\uff5e6hr","6\uff5e8hr","8\uff5e9hr","9\uff5e10hr"}));
      tValue.put("1160105",(new String[] {"","無","有"}));
      tValue.put("1160107",(new String[] {"","無","有"}));
      tValue.put("1160109",(new String[] {"","無","超","欠"}));
      tValue.put("1160111",(new String[] {"","無","有"}));
      tValue.put("1160112",(new String[] {"","無","1月以内","1月越3月以内","3月越"}));
      tValue.put("1160113",(new String[] {"","無","有"}));
      tValue.put("1160114",(new String[] {"","無","有"}));
      tValue.put("1160115",(new String[] {"","無","有"}));
      tValue.put("1160116",(new String[] {"","通常","通常老","大I","大I老","大II","大II老"}));
      tValue.put("1160118",(new String[] {"","無","有"}));
      tValue.put("1160119",(new String[] {"","無","有"}));
      tValue.put("1160120",(new String[] {"","無","有"}));
      tValue.put("1160121",(new String[] {"","無","有"}));
      tValue.put("1160122",(new String[] {"","無","I","II"}));
      tValue.put("1160123",(new String[] {"","無","有"}));
      tValue.put("KAIZEN",(new String[] {"","無","I","II","III"}));
      tValue.put("12",(new String[] {"","無","有"}));
      tValue.put("16",(new String[] {"","無","有"}));
      yValue.put("1660101",(new String[] {"","無","超","欠"}));
      yValue.put("1660102",(new String[] {"","無","有"}));
      yValue.put("1660103",(new String[] {"","無","有"}));
      yValue.put("1660104",(new String[] {"","無","有"}));
      yValue.put("1660105",(new String[] {"","無","有"}));
      yValue.put("1660106",(new String[] {"","無","有"}));
      yValue.put("1660107",(new String[] {"","無","有"}));
      yValue.put("1660108",(new String[] {"","無","I","II","無","I","II"}));
      yValue.put("1660109",(new String[] {"","無","有"}));
      yValue.put("1660110",(new String[] {"","病/診","施設"}));
      yValue.put("KAIZEN",(new String[] {"","無","I","II","III"}));
      yValue.put("MULTI",(new String[] {"","無","無","I","無","I","I","II"}));
      yValue.put("12",(new String[] {"","無","有","無","有"}));
      yValue.put("16",(new String[] {"","無","有","無","有","無","有","無","有"}));

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
        buf.append("' and SYSTEM_SERVICE_KIND_DETAIL in (11611,16611) ");
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
              setTsusyoPanel(targetYear,targetMonth,targetDay);
            }
          };
          ymbox.addActionListener(ymChange);
          pn1.add(ymbox);
          dayCondition();
          pn1.add(pn3);

          setTsusyoPanel(ymdata[0][0],ymdata[0][1],targetDay);
          
        } else {
          JLabel nodata = new JLabel("←該当するデータが有りません他の事業所があれば選択しなおして下さい。");
          nodata.setFont(new Font("Dialog",Font.PLAIN,12));
          pn1.add(nodata);
          setTsusyoPanel(0,0,0);
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
      buf.append(" inner join PATIENT_NINTEI_HISTORY on");
      buf.append(" (PATIENT_NINTEI_HISTORY.PATIENT_ID=SERVICE.PATIENT_ID");
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
      //buf.append("' and SYSTEM_SERVICE_KIND_DETAIL in (11611,16611) ");
      buf.append("' and ((SYSTEM_SERVICE_KIND_DETAIL=11611 and ");
      buf.append("substring(JOTAI_CODE from 1 for 1)=2) or ");
      buf.append("(SYSTEM_SERVICE_KIND_DETAIL=16611 and ");
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
            setTsusyoPanel(targetYear,targetMonth,targetDay);
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
      buf.append(" and system_service_kind_detail in (11611,16611) ");
      buf.append(" and unit_valid_start <= '");
      buf.append(nStart);
      buf.append("' and unit_valid_end >= '");
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
  
    public void setTsusyoPanel(int targetYear,int targetMonth,int targetDay) {
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

      if (targetYear>0) setUnit(nStart);
      if (dbm.connect()) {
        StringBuffer buf = new StringBuffer();

        buf.append("select PATIENT_FIRST_NAME,PATIENT_FAMILY_NAME,");
        buf.append("SERVICE.PATIENT_ID,SERVICE_ID as SID,");
        buf.append("SYSTEM_SERVICE_KIND_DETAIL,max(JOTAI_CODE) as JOTAI_CODE,PATIENT_BIRTHDAY,");
        buf.append("count(SERVICE.SERVICE_ID),INSURE_RATE,");
        buf.append("min(INSURE_VALID_START) as INSURE_VALID_START,");
        buf.append("max(INSURE_VALID_END) as INSURE_VALID_END");
        buf.append(",min(extract(DAY from SERVICE_DATE)) as FIRST_DAY");
        buf.append(",SERVICE_USE_TYPE");
        buf.append(",substring(SERVICE.LAST_TIME from 1 for 16) as LAST");
        buf.append(",SERVICE_DATE");
        buf.append(" from SERVICE ");
        buf.append(" inner join PATIENT on ");
        buf.append("(PATIENT.PATIENT_ID=SERVICE.PATIENT_ID and DELETE_FLAG=0)");
        buf.append(" inner join PATIENT_NINTEI_HISTORY on ");
        buf.append("(PATIENT_NINTEI_HISTORY.PATIENT_ID=SERVICE.PATIENT_ID and");
        buf.append(" NINTEI_HISTORY_ID in ");
        buf.append("(select NINTEI_HISTORY_ID from ");
        buf.append("PATIENT_NINTEI_HISTORY where PATIENT_ID=SERVICE.PATIENT_ID");
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

        //buf.append("')) where SYSTEM_SERVICE_KIND_DETAIL in (11611,16611)");
        buf.append("')) where ((SYSTEM_SERVICE_KIND_DETAIL=11611 and ");
        buf.append("substring(JOTAI_CODE from 1 for 1)=2) or ");
        buf.append("(SYSTEM_SERVICE_KIND_DETAIL=16611 and ");
        buf.append("substring(JOTAI_CODE from 1 for 1)=1)) ");

        buf.append(" and SERVICE_USE_TYPE in (4,6) ");
        buf.append(" and extract(YEAR from SERVICE_DATE)=");
        buf.append(targetYear);
        buf.append(" and extract(MONTH from SERVICE_DATE)=");
        buf.append(targetMonth);
        if (targetDay>0) {
          buf.append(" and extract(DAY from SERVICE_DATE)=");
          buf.append(targetDay);
        }
        buf.append(" and SERVICE.PROVIDER_ID='");
        buf.append(currentProvider);
        buf.append("' group by SERVICE.PATIENT_ID,PATIENT_FIRST_NAME,");
        buf.append("PATIENT_FAMILY_NAME,PATIENT_BIRTHDAY,");
        buf.append("INSURE_RATE,SYSTEM_SERVICE_KIND_DETAIL");
        buf.append(",SERVICE.SERVICE_ID,LAST,SERVICE_USE_TYPE,SERVICE_DATE");
        buf.append(" order by SYSTEM_SERVICE_KIND_DETAIL,SERVICE.PATIENT_ID");
        buf.append(",LAST desc,SERVICE_DATE asc");

        String sql = buf.toString();
        System.out.println(sql);
        dbm.execQuery(sql);
        dbm.Close();
        Vector pdata = new Vector();
        totalRow = new Vector();
        DngDBAccess dbm2 = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
        int sbp=-1;
        int pNo=-1;
        int uTp=-1;
        int ln = 0;
        int totalCount=0;
        int totalKaizen=0;
        int totalFee1=0;
        int totalFee2=0;
        int totalFee3=0;
        int totalFee4=0;
        int totalFee5=0;
        int trCols=0;
        String sids = "";
        boolean monfin = false;
        for (int i=0;i<dbm.Rows;i++){
          int lastSbp = sbp;
          int lastP = pNo;
          sbp = Integer.parseInt(dbm.getData(4,i).toString());
          pNo = Integer.parseInt(dbm.getData(2,i).toString());
          if (pNo!=lastP || sbp!=lastSbp) {
            uTp = Integer.parseInt(dbm.getData("SERVICE_USE_TYPE",i).toString());
            if (targetDay==0) {
              if (!monfin && (lastP != -1 || lastSbp != -1) ) {
                pNo = lastP;
                sbp = lastSbp;
                i--;
                System.out.println("tbl Create start");
              } else {
                sids = dbm.getData("SID",i).toString();
                monfin=false;
                if (i<dbm.Rows-1) continue;
              }
            }
          }
          else {
            if (uTp!=Integer.parseInt(dbm.getData("SERVICE_USE_TYPE",i).toString())) {
              if (targetDay>0) continue;
              else if (monfin) continue;
              else i--;
              System.out.println("tbl create start");
            } else {
              if (targetDay==0) {
                sids += ","+dbm.getData("SID",i).toString();
                if (i<dbm.Rows-1) continue;
              }
            }
          }
          monfin = true;
          String pointCode="";
          double mountRate=0;
          int sCount = Integer.parseInt(dbm.getData("COUNT",i).toString());
          int insRate = Integer.parseInt(dbm.getData("INSURE_RATE",i).toString());
          String insStart = dbm.getData("INSURE_VALID_START",i).toString();
          String insEnd = dbm.getData("INSURE_VALID_END",i).toString();

          Vector pline = new Vector();
          //pline.addElement(dbm.getData(2,i).toString());
          int sNo = new Integer(dbm.getData(3,i).toString()).intValue();
          //pline.addElement(dbm.getData(3,i).toString());
          String nam1=(dbm.getData(1,i)!=null) ? dbm.getData(1,i).toString()+" ":"";
          String nam2=(dbm.getData(0,i)!=null) ? dbm.getData(0,i).toString():"";
          String nam =nam1+nam2;
          pline.addElement(new Integer(++ln));
          pline.addElement(nam);
          if (dbm.getData("PATIENT_BIRTHDAY",i)!=null) {
            int age =patientAge(dbm.getData("PATIENT_BIRTHDAY",i).toString());
            pline.addElement(new Integer(age));
          } else {
            pline.addElement("");
          }
          String kind = (sbp==11611) ? 
                        "介":"予";
          String cR="1";
          int kaizenVal = 0;
          if (targetDay==0) {
            DngDBAccess dbm4 = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
            //処遇改善取得----------------------------------------------
            if ( (sbp==11611 && kaizen1>1) || (sbp==16611 && kaizen2>1) ) {
              buf.delete(0,buf.length());
              buf.append("select distinct extract(YEAR from CLAIM_DATE),");
              buf.append("extract(MONTH from CLAIM_DATE) from CLAIM where PATIENT_ID=");
              buf.append(pNo);
              buf.append(" and extract(YEAR from TARGET_DATE)=");
              buf.append(targetYear);
              buf.append(" and extract(MONTH from TARGET_DATE)=");
              buf.append(targetMonth);
              buf.append(" and CATEGORY_NO=3 and PROVIDER_ID='");
              buf.append(currentProvider);
              buf.append("'");
              System.out.println(buf.toString());
             
              dbm4.connect();
              dbm4.execQuery(buf.toString());
              dbm4.Close();
              if (dbm4.Rows>0) {
                int cYear = Integer.parseInt(dbm4.getData(0,0).toString());
                if (Integer.parseInt(dbm4.getData(1,0).toString())<4) cYear--; 
                buf.delete(0,buf.length());
                buf.append("select DETAIL_VALUE from CLAIM_DETAIL_TEXT_"+cYear);
                buf.append(" where SYSTEM_BIND_PATH=301009 and CLAIM_ID = (");
                buf.append("select CLAIM_ID from CLAIM_DETAIL_TEXT_"+cYear);
                buf.append(" where CLAIM_ID in (select CLAIM_ID from ");
                buf.append("CLAIM where PATIENT_ID=");
                buf.append(pNo);
                buf.append(" and extract(YEAR from TARGET_DATE)=");
                buf.append(targetYear);
                buf.append(" and extract(MONTH from TARGET_DATE)=");
                buf.append(targetMonth);
                buf.append(" and CATEGORY_NO=3 and PROVIDER_ID='");
                buf.append(currentProvider);
                buf.append("') and detail_value='");
                if (sbp==11611)
                  buf.append(taKaizenCode[kaizen1]);
                else
                  buf.append(yaKaizenCode[kaizen2]);
                buf.append("');");
                System.out.println(buf.toString());
                dbm4.connect();
                dbm4.execQuery(buf.toString());
                dbm4.Close();
                if (dbm4.Rows>0) {
                  kaizenVal = Integer.parseInt(dbm4.getData(0,0).toString());
                }
              }
            }
            //処遇改善取得ここまで----------------------------------------

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
          }
          else if (targetDay>0) {
            if (dbm.getData("JOTAI_CODE",i)!=null) {
            cR = dbm.getData("JOTAI_CODE",i).toString();
            String cRate = (String)careRate.get(cR);
            pline.addElement(cRate);
            } else {
              pline.addElement("");
            }
            pline.addElement(kind);
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
          }
          buf.delete(0,buf.length());
          buf.append("select SYSTEM_BIND_PATH,");
          if (targetDay==0)
            buf.append("max(DETAIL_VALUE) as DETAIL_VALUE");
          else
            buf.append("DETAIL_VALUE");
          buf.append(" from SERVICE_DETAIL_INTEGER_");
          buf.append(detYear);
          buf.append(" where SERVICE_ID");
          if (targetDay==0)
            buf.append(" in ("+sids+")");
          else
            buf.append("="+sNo);
/*
          buf.append(" and SYSTEM_BIND_PATH");
          buf.append(" in (12,14,1160103,1160104,1160105,1160107,1160109,1160110,1160111,1160112,1160113,1160114,1160115,1160116,1160117,1160118,1160119,1160120,1160121,1160122,1660101,1660102,1660103,1660104,1660105,1660106,1660107,1660108)");
*/
          if (targetDay==0)
            buf.append(" group by SYSTEM_BIND_PATH ");
          buf.append(" order by SYSTEM_BIND_PATH desc;");
          sql = buf.toString();
          System.out.println(sql);
          dbm2.connect();
          dbm2.execQuery(sql);
          dbm2.Close();

          System.out.println("dbm2:"+dbm2.Rows);
          int addUnit=0;
          double unitRate;
          int kaizen = 0;
          boolean hiwari = false;
          if (sbp==11611) {
            unitRate = tunitRate;
            kaizen = kaizen1;
            Hashtable tVal = new Hashtable();
            tVal.put("1160105","");
            tVal.put("1160107","");
            tVal.put("1160109","");
            tVal.put("1160111","");
            tVal.put("1160112","");
            tVal.put("1160113","");
            tVal.put("1160114","");
            tVal.put("1160115","");
            tVal.put("1160116","");
            tVal.put("1160118","");
            tVal.put("1160119","");
            tVal.put("1160120","");
            tVal.put("1160121","");
            tVal.put("1160122","");
            tVal.put("12","");
            tVal.put("16","");
            String[] ssCode = new String[] {"0","0","0","0",(String)careCode.get(cR)};
            int ssCount = 0;
            for (int j=0;j<dbm2.Rows;j++){
              System.out.println("Rows start: "+j);
              String ssbp0 = dbm2.getData("SYSTEM_BIND_PATH",j).toString();
              int sbp0 = Integer.parseInt(ssbp0);
              String sval = dbm2.getData("DETAIL_VALUE",j).toString();
              int ival = Integer.parseInt(sval);
              String[] val;
              System.out.println("sbp = "+sbp0+" detailValu = "+ival);
              if (sbp0==15) continue;
              switch (sbp0) {
                case 1160116 : ssCode[0] = (new Integer(ival/2+ival%2)).toString();
                               ssCode[1] = (new Integer(2-ival%2)).toString();
                               break;
                case 1160104 : ssCode[2] = sval; break;
                case 1160109 : ssCode[3] = sval; break;
              }
              if (sbp0==14 || sbp0==9) {
              }
              else if (sbp0==1160116 || sbp0==1160104 || sbp0==1160109) {
                val = (String[])tValue.get(ssbp0);
                tVal.put(ssbp0,val[ival]);
              }
              else {
                int[] add;
                if (!tValue.containsKey(ssbp0)) continue;
                val = (String[])tValue.get(ssbp0);
                add = (int[]) taUnit.get(ssbp0);
                System.out.println("sbp = "+sbp0+" key = "+ival+" add= "+add[ival]);
                if (sbp0==12) mountRate = (double)add[ival]/100.0;
                else addUnit += add[ival];
                tVal.put(ssbp0,val[ival]);
              }
            }
            String[] val = (String[])tValue.get("KAIZEN");
            if (targetDay==0 && kaizenVal!=0) {
              tVal.put("KAIZEN","("+val[kaizen]+") "+Integer.toString(kaizenVal));
              totalKaizen = totalKaizen + kaizenVal;
            }
            else {
              tVal.put("KAIZEN",val[kaizen]);
            }
            pline.addElement((String)tVal.get("1160104")); //時間区分
            pline.addElement((String)tVal.get("1160116")); //施設区分
            pline.addElement((String)tVal.get("1160109")); //人員
            pline.addElement((String)tVal.get("1160119")); //人員
            pline.addElement((String)tVal.get("1160105")); //入浴
            pline.addElement((String)tVal.get("1160107")); //リハ指導
            pline.addElement((String)tVal.get("1160111")); //リハマネ
            pline.addElement((String)tVal.get("1160112")); //短期
            pline.addElement((String)tVal.get("1160121")); //若年
            pline.addElement((String)tVal.get("1160114")); //栄養
            pline.addElement((String)tVal.get("1160115")); //口腔
            pline.addElement((String)tVal.get("1160118")); //個別
            pline.addElement((String)tVal.get("1160120")); //認知症
            pline.addElement((String)tVal.get("1160123")); //重度
            pline.addElement((String)tVal.get("1160122")); //サービス
            pline.addElement((String)tVal.get("12"));      //中山間
            pline.addElement("");                          //運動
            pline.addElement("");                          //評価
            pline.addElement((String)tVal.get("16"));      //同住
            pline.addElement("");                          //日割
            //pline.addElement("");                          //選択複数
            pline.addElement((String)tVal.get("KAIZEN")); //処遇改善
            for (int k=0;k<5;k++) pointCode += ssCode[k];
            System.out.println("pointCode : "+pointCode);
            int[] add = (int[])taUnit.get("KAIZEN");
            kaizenRate = (double)add[kaizen]/1000.0; 
          }
          else {
            int jl=5;
            kaizen = kaizen2;
            unitRate = yunitRate;
            Hashtable yoVal = new Hashtable();
            yoVal.put("1660101","");
            yoVal.put("1660102","");
            yoVal.put("1660103","");
            yoVal.put("1660104","");
            yoVal.put("1660105","");
            yoVal.put("1660106","");
            yoVal.put("1660107","");
            yoVal.put("1660108","");
            yoVal.put("1660109","");
            yoVal.put("1660110","");
            yoVal.put("12","");
            yoVal.put("16","");
            String[] ssCode = new String[] {"0",(String)careCode.get(cR),"0","0"};
            int multi = 0;
            int multi_less = 0;
            for (int j=0;j<dbm2.Rows;j++) {
              System.out.println("Rows start: "+j);
              String ssbp0 = dbm2.getData("SYSTEM_BIND_PATH",j).toString();
              int sbp0 = Integer.parseInt(ssbp0);
              if (sbp0==15) continue;
              String sval = dbm2.getData("DETAIL_VALUE",j).toString();
              int ival = Integer.parseInt(sval);
              String[] val;
              switch (sbp0) {
                case 1660110 : ssCode[0] = sval; break;
                case 1660101 : ssCode[2] = sval; break;
                case 1660102 : ssCode[3] = sval; break;
                case 1660108 : ival += (cR.equals("13")) ? 3:0; break;
                case 16 : ival += (cR.equals("13")) ? 2:0; 
                          ival += (ssCode[0].equals("2")) ? 2:0; break;
                case 12 : ival += (ssCode[3].equals("2")) ? 2:0; break;
              }
              System.out.println("sbp = "+sbp0+" detailValu = "+ival);

              if (sbp0==14) {
              }
              else if (sbp0==1660110 || sbp0==1660102 || sbp0==1660101) {
                val = (String[])yValue.get(ssbp0);
                yoVal.put(ssbp0,val[ival]);
              }
              else {
                int[] add;
                if (!yValue.containsKey(ssbp0)) continue;
                val = (String[])yValue.get(ssbp0);
                add = (int[]) yaUnit.get(ssbp0);
                System.out.println("sbp = "+sbp0+" key = "+ival+" add= "+add[ival]);
                if (sbp0==12) mountRate = (double)add[ival]/100.0;
                else addUnit += add[ival];
                yoVal.put(ssbp0,val[ival]);
// multi-serv (value set)
                switch (sbp0) {
                  case 1660103 : if (ival==2) {
                                   multi += 1; 
                                   //multi_less += add[ival];
                                 }
                                 break;
                  case 1660104 : if (ival==2) {
                                   multi += 2;
                                   //multi_less += add[ival];
                                 }
                                 break;
                  case 1660105 : if (ival==2) {
                                   multi += 4;
                                   //multi_less += add[ival];
                                 }
                                 break;
                }
              }
            }
            String[] val = (String[])yValue.get("KAIZEN");
            if (targetDay==0 && kaizenVal!=0) {
              yoVal.put("KAIZEN","("+val[kaizen]+") "+Integer.toString(kaizenVal));
              totalKaizen = totalKaizen + kaizenVal;
            }
            else {
              yoVal.put("KAIZEN",val[kaizen]);
            }
            //val = (String[])yValue.get("MULTI");
            //yoVal.put("MULTI",val[multi]);
            pline.addElement("");                           //時間区分
            pline.addElement((String)yoVal.get("1660110")); //施設区分
            pline.addElement((String)yoVal.get("1660101")); //人員
            pline.addElement("");                           //理学
            pline.addElement("");                           //入浴
            pline.addElement("");                           //リハ指導
            pline.addElement("");                           //リハマネ
            pline.addElement("");                           //短期
            pline.addElement((String)yoVal.get("1660107")); //若年
            pline.addElement((String)yoVal.get("1660104")); //栄養
            pline.addElement((String)yoVal.get("1660105")); //口腔
            pline.addElement("");                           //個別
            pline.addElement("");                           //認知症
            pline.addElement("");                           //重度
            pline.addElement((String)yoVal.get("1660108")); //サービス
            pline.addElement((String)yoVal.get("12"));      //中山間
            pline.addElement((String)yoVal.get("1660103")); //運動
            pline.addElement((String)yoVal.get("1660106")); //評価
            pline.addElement((String)yoVal.get("16"));      //同住
            pline.addElement((String)yoVal.get("1660102")); //日割
            //pline.addElement((String)yoVal.get("MULTI"));   //選択複数
            pline.addElement((String)yoVal.get("KAIZEN")); //処遇改善
            for (int k=0;k<4;k++) pointCode += ssCode[k];
            System.out.println("pointCode : "+pointCode);
            int[] add = (int[])taUnit.get("KAIZEN");
            kaizenRate = (double)add[kaizen]/1000.0;
/*
            add = (int[])yaUnit.get("MULTI");
            addUnit += add[multi];
            if (add[multi] != 0) addUnit -= multi_less;
            else multi_less = 0;
            System.out.println(" multi_add = "+add[multi]+" multi_less = -"+multi_less);
*/
         }

         if (targetDay==0) {
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
           System.out.println(buf.toString());
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
              buf.append(" and extract(YEAR from TARGET_DATE)=");
              buf.append(targetYear);
              buf.append(" and extract(MONTH from TARGET_DATE)=");
              buf.append(targetMonth);
              buf.append(" and CATEGORY_NO=7 and PROVIDER_ID='");
              buf.append(currentProvider);
              buf.append("') and SYSTEM_BIND_PATH=701007 ");
              if (sbp==11611) buf.append("and DETAIL_VALUE='16')");
              else buf.append("and DETAIL_VALUE='66')");
              //buf.append(" and SYSTEM_BIND_PATH");
              //buf.append(" in (701008,701014,701015,701016,701017)");
              buf.append(" and (SYSTEM_BIND_PATH=701008 or ");
              buf.append("SYSTEM_BIND_PATH>=701014 and ");
              buf.append("SYSTEM_BIND_PATH<=701026)");
              buf.append(" order by SYSTEM_BIND_PATH;");
              sql = buf.toString();
              System.out.println(sql);
              dbm5.connect();
              dbm5.execQuery(sql);
              dbm5.Close();
              cRows = dbm5.Rows;
            }
            else cRows=0;
            if (cRows>0) {
              int other=0;
              int clid = Integer.parseInt(dbm5.getData("CLAIM_ID",0).toString())
;
              if (!hiwari) {
                pline.addElement(new Integer(dbm5.getData("DETAIL_VALUE",0).toString()));
                totalCount += Integer.parseInt(dbm5.getData("DETAIL_VALUE",0).toString());
              }
              else {
                DngDBAccess dbm3 = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
                if (dbm3.connect()) {
                  buf.delete(0,buf.length());
                  buf.append("select count(service_date),SERVICE_USE_TYPE from");
                  buf.append(" (select distinct service_date,SERVICE_USE_TYPE from service");
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
                  buf.append(" and ((SYSTEM_SERVICE_KIND_DETAIL=11611 and ");
                  buf.append("substring(JOTAI_CODE from 1 for 1)=2) or ");
                  buf.append("(SYSTEM_SERVICE_KIND_DETAIL=16611 and ");
                  buf.append("substring(JOTAI_CODE from 1 for 1)=1))");
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
                  buf.append("') group by SERVICE_USE_TYPE");
                  buf.append(" order by SERVICE_USE_TYPE desc");
                  System.out.println(buf.toString());
                  dbm3.execQuery(buf.toString());
                  dbm3.Close();
                  if (dbm3.getData(0,0)!=null) {
                    pline.addElement(new Integer(dbm3.getData(0,0).toString()));
                    totalCount += Integer.parseInt(dbm3.getData(0,0).toString());
                  }
                }
              }
              int hiyou = (int)(Float.parseFloat(dbm5.getData("DETAIL_VALUE",1).toString())*Float.parseFloat(dbm5.getData("DETAIL_VALUE",2).toString()));
              int futan = Integer.parseInt(dbm5.getData("DETAIL_VALUE",4).toString());
              int kouhiunit,kouhi,jikouhi;
              if (cRows>5) {
                kouhiunit = Integer.parseInt(dbm5.getData("DETAIL_VALUE",5).toString())
                       +Integer.parseInt(dbm5.getData("DETAIL_VALUE",8).toString())
                       +Integer.parseInt(dbm5.getData("DETAIL_VALUE",11).toString());
                kouhi = Integer.parseInt(dbm5.getData("DETAIL_VALUE",6).toString())
                       +Integer.parseInt(dbm5.getData("DETAIL_VALUE",9).toString())
                       +Integer.parseInt(dbm5.getData("DETAIL_VALUE",12).toString());
                jikouhi = Integer.parseInt(dbm5.getData("DETAIL_VALUE",7).toString())
                       +Integer.parseInt(dbm5.getData("DETAIL_VALUE",10).toString())
                       +Integer.parseInt(dbm5.getData("DETAIL_VALUE",13).toString());
              } else {
                kouhiunit = -1;
                kouhi = 0;
                jikouhi = 0;
              }

              buf.delete(0,buf.length());
              buf.append("select * from CLAIM_PATIENT_DETAIL where CLAIM_ID=");
              buf.append("(select CLAIM_ID from CLAIM where PATIENT_ID=");
              buf.append(pNo);
              buf.append(" and extract(YEAR from TARGET_DATE)=");
              buf.append(targetYear);
              buf.append(" and extract(MONTH from TARGET_DATE)=");
              buf.append(targetMonth);
              buf.append(" and CATEGORY_NO=16 and PROVIDER_ID='");
              buf.append(currentProvider);
              buf.append("' and CLAIM_DATE=(select claim_date from claim ");
              buf.append("where claim_id=");
              buf.append(clid);
              buf.append(") )");
              System.out.println(buf.toString());
              dbm2.connect();
              dbm2.execQuery(buf.toString());
              dbm2.Close();
              if (dbm2.Rows>0) {
                 for (int j=1;j<16;j=j+2) {
                   if (dbm2.getData(j,0)!=null) 
                     other += Integer.parseInt(dbm2.getData(j+1,0).toString());
                 }
                 if (dbm2.getData("OTHER_HIMOKU_NO6",0)!=null)
                   other += Integer.parseInt(dbm2.getData("OTHER_PAY_NO6",0).toString());
              }

              if (kouhiunit>0) futan = futan+ jikouhi;
              if (hiyou>0) totalFee1 += hiyou;
              if (futan>0) totalFee2 += futan;
              if (other>0) totalFee3 += other;
              if (other+futan>0) totalFee4 += other+futan;
              if (kouhi>0) totalFee5 += kouhi;
              pline.addElement(new Integer(hiyou));
              pline.addElement(new Integer(futan));
              pline.addElement(new Integer(other));
              pline.addElement(new Integer(other+futan));
              //if (kouhiunit>0) {
                pline.addElement(new Integer(kouhi));
              //} else {
              //  pline.addElement(new String(" -  "));
              //}
            }
            else { 
              DngDBAccess dbm3 = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
              if (dbm3.connect()) {
                buf.delete(0,buf.length());
                buf.append("select count(service_date),SERVICE_USE_TYPE from");
                buf.append(" (select distinct service_date,SERVICE_USE_TYPE from service");
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
                buf.append(" and ((SYSTEM_SERVICE_KIND_DETAIL=11611 and ");
                buf.append("substring(JOTAI_CODE from 1 for 1)=2) or ");
                buf.append("(SYSTEM_SERVICE_KIND_DETAIL=16611 and ");
                buf.append("substring(JOTAI_CODE from 1 for 1)=1))");
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
                buf.append("') group by SERVICE_USE_TYPE");
                buf.append(" order by SERVICE_USE_TYPE desc");
                System.out.println(buf.toString());
                dbm3.execQuery(buf.toString());
                System.out.println("dbm3:Rows="+dbm3.Rows+" sCount="+sCount);
                dbm3.Close();
                if (dbm3.Rows>0) {
                  if (dbm3.getData(0,0)!=null) {
                    pline.addElement(new Integer(dbm3.getData(0,0).toString()));
                    totalCount += Integer.parseInt(dbm3.getData(0,0).toString());
                  } else {
                    pline.addElement(new Integer(sCount));
                    totalCount += sCount;
                  }
                  pline.addElement(new String(""));
                  pline.addElement(new String(""));
                  pline.addElement(new String(""));
                  pline.addElement(new String(""));
                  pline.addElement(new String(""));
                }
                else {
                  pline.addElement(new String("-"));
                  pline.addElement(new String("算出不可"));
                  pline.addElement(new String(""));
                  pline.addElement(new String(""));
                  pline.addElement(new String(""));
                  pline.addElement(new String(""));
                }
              }
              else {
                pline.addElement(new String(""));
                pline.addElement(new String(""));
                pline.addElement(new String(""));
                pline.addElement(new String(""));
                pline.addElement(new String(""));
                pline.addElement(new String(""));
              }
            }
          }
          else if ( sbp==11611 && (cR.equals("12") || cR.equals("13"))) {
              int hiyou =(int)((double) addUnit * unitRate);
              int futan = hiyou - (int)((double)hiyou/100.0*(double)insRate);
              //if (hiyou%10>0) futan +=1;
              System.out.println("addUnit= "+addUnit+" uintRate = "+unitRate+" insRate = "+insRate+" hiyou = "+hiyou+" futan = "+futan);
              if (hiyou>0) totalFee1 += hiyou;
              if (futan>0) totalFee2 += futan;
              pline.addElement(new Integer(hiyou));
              pline.addElement(new Integer(futan));
          } 
          else {
            buf.delete(0,buf.length());
            buf.append("select service_unit from m_service_code ");
            buf.append("where system_service_kind_detail='");
            buf.append(sbp);
            buf.append("' and  system_service_code_item='");
            buf.append(pointCode);
            buf.append("' and service_valid_start <= '");
            buf.append(nStart);
            buf.append("' and service_valid_end >= '");
            buf.append(nStart);
            buf.append("'");
            dbm2.connect();
            System.out.println(buf.toString());
            dbm2.execQuery(buf.toString());
            dbm2.Close();
            if (dbm2.Rows>0) {
              int p = Integer.parseInt(dbm2.getData(0,0).toString());
              int mp = Math.round((float)((double) p * mountRate));
              System.out.println("p = "+p+" add = "+addUnit+" mountRate = "+mp+" unitRate = "+unitRate+" insRate = "+insRate);
              addUnit += mp;
              p += addUnit;
              int kp = Math.round((float)((double) p * kaizenRate));
              if (kaizen>2)
                kp = Math.round((float)((double)kp*(100-(kaizen-2)*10)/100.0));
              System.out.println(" kaizen = "+kp);
              p += kp;
              int hiyou =(int)((double) p * unitRate);
              int futan = hiyou - (int)((double)hiyou/100.0*(double)insRate);
              //if (hiyou%10>0) futan +=1;
              System.out.println("hiyou = "+hiyou+" futan = "+futan);
              if (hiyou>0) totalFee1 += hiyou;
              if (futan>0) totalFee2 += futan;
              pline.addElement(new Integer(hiyou));
              pline.addElement(new Integer(futan));
            }
          }
          pdata.addElement(pline);
          trCols = pline.size();
        }
        System.out.println(trCols+"fields  "+totalCount+":"+totalFee1+":"+totalFee2+":"+totalFee3+":"+totalFee4+":"+totalFee5);
        int cn1,cn2,cn3,cn4,cn5,cn6;
        cn6=  trCols-1;
        cn1=  cn6-5;
        cn2=  cn6-4;
        cn3=  cn6-3;
        cn4=  cn6-2;
        cn5=  cn6-1;
        for (int c=0;c<trCols;c++) {
          if (c==1) totalRow.addElement(new String("  合 計 値"));
          else if (targetDay>0) {
            if (c==cn5) totalRow.addElement(new Integer(totalFee1));
            else if (c==cn6) totalRow.addElement(new Integer(totalFee2));
            else totalRow.addElement(new String(""));
          }
          else {
            if (c==cn1) totalRow.addElement(new Integer(totalCount));
            else if (c==cn1-1) totalRow.addElement(new Integer(totalKaizen));
            else if (c==cn2) totalRow.addElement(new Integer(totalFee1));
            else if (c==cn3) totalRow.addElement(new Integer(totalFee2));
            else if (c==cn4) totalRow.addElement(new Integer(totalFee3));
            else if (c==cn5) totalRow.addElement(new Integer(totalFee4));
            else if (c==cn6) totalRow.addElement(new Integer(totalFee5));
            else totalRow.addElement(new String(""));
          }
        }
        scp = getScrollList(pdata,targetDay);
        pn2.add(scp);
      }
      if (dbm.Rows>0) {
        if (targetDay>0) {
          JLabel lab1 = new JLabel("＊日単位での金額について：負担金額は端数処理や月単位の加算を便宜的に算出しているため月間金額とは異なる場合があります。 ");
          lab1.setFont(new Font("Dialog",Font.PLAIN,11));
          JLabel lab2 = new JLabel("　　　　　　　　　　　　　予防サービスの場合は月間の金額を表示、日割の場合は基本単位数のみの金額を表示しています。");
          JLabel lab3 = new JLabel("　　　　　　　　　　　　　公費負担分は考慮しておりません。");
          lab3.setFont(new Font("Dialog",Font.PLAIN,11));
          lab2.setFont(new Font("Dialog",Font.PLAIN,11));
          pnl.add(lab1,BorderLayout.NORTH);
          pnl.add(lab2,BorderLayout.CENTER);
          pnl.add(lab3,BorderLayout.SOUTH);
        } else {
          JLabel lab1 = new JLabel("＊月間での金額について：実績確定分のみ表示されます。 ");
          lab1.setFont(new Font("Dialog",Font.PLAIN,11));
          JLabel lab2 = new JLabel("＊処遇改善加算有りの場合、実績確定後は点数が表示されます。");
          lab2.setFont(new Font("Dialog",Font.PLAIN,11));
          JLabel lab3 = new JLabel("＊月途中で要介護度が変わっている場合、変更後の要介護度が表示されます。また、要支援←→要介護の場合は別行となります。");
          lab3.setFont(new Font("Dialog",Font.PLAIN,11));
          pnl.add(lab1,BorderLayout.NORTH);
          pnl.add(lab2,BorderLayout.CENTER);
          pnl.add(lab3,BorderLayout.SOUTH);
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
/*
 select CLAIM_ID,SYSTEM_BIND_PATH,substring(DETAIL_VALUE from 1 for 10) from CLAIM_DETAIL_TEXT_2007 where CLAIM_ID = (select CLAIM_ID from CLAIM  where extract(YEAR from CLAIM_DATE)=2007 and extract(MONTH from CLAIM_DATE)=6 and CATEGORY_NO=7) and SYSTEM_BIND_PATH in (701008,701014,101015,701016,701017) order by SYSTEM_BIND_PATH;
*/
    }

    public boolean isSelected() {
      //int sel = usrTbl.getSelectedRow();
      //return (sel!=-1) ? true:false;
      return true;
    }

    public void setSelectable(boolean selectable) {
      isSelectable = selectable;
    }
    public String getTsusyoRehaCsv(int pno) {
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
      fieldName.addElement("氏名");
      fieldName.addElement("年齢");
      fieldName.addElement("要介護度");
      fieldName.addElement("種");
      if (td>0) {
        fieldName.addElement("開始時刻");
        fieldName.addElement("終了時刻");
      } 
      fieldName.addElement("時間区分");
      fieldName.addElement("施設");
      fieldName.addElement("人");
      fieldName.addElement("理学");
      fieldName.addElement("入浴");
      fieldName.addElement("指導");
      fieldName.addElement("マネ");
      fieldName.addElement("短期");
      fieldName.addElement("若年");
      fieldName.addElement("栄養");
      fieldName.addElement("口腔");
      fieldName.addElement("個別");
      fieldName.addElement("認知");
      fieldName.addElement("重度");
      fieldName.addElement("サー");
      fieldName.addElement("中山間");
      fieldName.addElement("運動");
      fieldName.addElement("評価");
      fieldName.addElement("同住");
      fieldName.addElement("日割");
      //fieldName.addElement("複数");
      fieldName.addElement("処遇改善");
      if (td==0) {
        fieldName.addElement("回");
      }
      fieldName.addElement("費用");
      fieldName.addElement("負担額");
      if (td==0) {
        fieldName.addElement("その他");
        fieldName.addElement("負担合計");
        fieldName.addElement("公費負担");
      }
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
      DefaultTableCellRenderer cen = new DefaultTableCellRenderer();
      cen.setHorizontalAlignment(SwingConstants.CENTER);
      sorter.setColumnClass(0,Integer.class);
      sorter.setColumnClass(2,Integer.class);
      int cn1,cn2,cn3,cn4,cn5,cn6;
      cn6 = usrTbl.getColumnCount()-1;
      cn1 = cn6-5;
      cn2 = cn6-4;
      cn3 = cn6-3;
      cn4 = cn6-2;
      cn5 = cn6-1;
      if (td==0) {
        sorter.setColumnClass(cn1,Integer.class);
        sorter.setColumnClass(cn2,Integer.class);
        sorter.setColumnClass(cn3,Integer.class);
        sorter.setColumnClass(cn4,Integer.class);
      }
      sorter.setColumnClass(cn5,Integer.class);
      sorter.setColumnClass(cn6,Integer.class);
      usrTbl.getColumnModel().getColumn(0).setCellRenderer(ren);
      usrTbl.getColumnModel().getColumn(2).setCellRenderer(ren);

      //usrTbl.getColumnModel().getColumn(0).setMinWidth(0);
      //usrTbl.getColumnModel().getColumn(0).setMaxWidth(0);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(90);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(60);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      if (td>0) {
        usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(60);
        usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(60);
      } 
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(65); //時区
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(40); //施設
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32); //人員
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32); //理学
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32); //入浴
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32); //指導
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32); //マネ
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(80); //短期
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32); //若年
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32); //栄養
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32); //口腔
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32); //個別
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32); //認知
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32); //重度
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32); //サー
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(40); //中山間
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32); //運動
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32); //評価
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32); //同住
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32); //日割
      //usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      //usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32); //複数
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(ren);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(60); //改善
      System.out.println("cid : "+cid);
      if (td==0) {
        usrTbl.getColumnModel().getColumn(cid).setCellRenderer(ren);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32); //回数
      }
      //sorter.setColumnClass(cid,Integer.class);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(ren);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(60); //費用
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(ren);
      usrTbl.getColumnModel().getColumn(cid).setPreferredWidth(60);   //負担額
      if (td==0) {
        usrTbl.getColumnModel().getColumn(++cid).setCellRenderer(ren);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(80); //その他
        usrTbl.getColumnModel().getColumn(cid).setCellRenderer(ren);  
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(63); //負担計
        usrTbl.getColumnModel().getColumn(cid).setCellRenderer(ren);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(60); //公費
      }
      //usrTbl.getTableHeader().setReorderingAllowed(false);
      JScrollPane scrPane = new JScrollPane();
      scrPane.getViewport().setView(usrTbl);
      scrPane.setBackground(new Color(250,230,210));
      scrPane.setFont(new Font("san-serif",Font.PLAIN,14));
      scrPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      scrPane.getHorizontalScrollBar();
      scrPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scrPane.getVerticalScrollBar();
      scrPane.setPreferredSize(new Dimension(810,410));
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
      num=usrTbl.getColumnCount();
      float width[] = new float[num];
      int ctype[] = new int[num];
      Arrays.fill(ctype,0);
      ctype[cid] = 2; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("2.5"); //No.
      width[cid++] = 9; //氏名
      ctype[cid] = 2; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.2"); //年齢
      ctype[cid] = 7;
      width[cid++] = Float.parseFloat("6.0"); //要介護度
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("2.5"); //種類
      if (targetDay>0) {
        ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
        width[cid++] = Float.parseFloat("6.0"); //開始時刻
        ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
        width[cid++] = Float.parseFloat("6.0"); //終了時刻
      } 
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("6.0"); //時間区分
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("5.2"); //施設区分
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("2.5"); //人員
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.0"); //理学
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.0"); //入浴
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.0"); //リハ指導
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.0"); //リハマネ
      ctype[cid] = 10; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("8.5"); //短期
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.0"); //若年
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.0"); //栄養
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.0"); //口腔
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.0"); //個別
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.0"); //認知
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.0"); //重度
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.0"); //サービス
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("4.5"); //中山間
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.0"); //運動
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.0"); //評価
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.0"); //同住
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.0"); //日割
      //ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      //width[cid++] = Float.parseFloat("3.0"); //複数
      ctype[cid] = 2; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("7.0"); //改善
      if (targetDay==0) {
        ctype[cid] = 2; // 0 - normal 1 - add comma 2 - align right
        width[cid++] = Float.parseFloat("3.0"); //回数
      }
      ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = 6; //費用
      ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = 5; //負担額
      if (targetDay==0) {
        ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
        width[cid++] = Float.parseFloat("5.0"); //その他負担額
        ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
        width[cid++] = Float.parseFloat("6.0"); //負担額合計
        ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
        width[cid++] = Float.parseFloat("5.5"); //公費負担額
      }
      //Calendar cal = Calendar.getInstance();
      //String date=cal.get(Calendar.YEAR)+""+(cal.get(Calendar.MONTH) + 1)
      //            +""+cal.get(Calendar.DATE);
      //String fname = "TSUSYO"+date+".pdf";
      StringBuffer sb = new StringBuffer();
      sb.append("TSUSYOREHA-");
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
      if (pdf.openPDF("通所リハ情報")) {
        pdf.setTable(usrTbl,width,ctype,0);
        pdf.setRow(totalRow,width,ctype,0);
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
