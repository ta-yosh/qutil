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

public class QkanTsusyoData {

    private DngDBAccess dbm;
    private String dbUri;
    private String dbUser;
    private String dbPass;
    public String currentProvider=null;
    public String curProviderName=null;
    public int targetYear=0;
    public int targetMonth=0;
    public int targetDay=0;
    public boolean KAI16 = false;
    public String targetDate=null;
    private String data[][];
    private int ymdata[][];
    private int ddata[];
    private double tunitRate;
    private double yunitRate;
    private double spRate;
    private double smRate;
    private double kaizenRate;
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
    private boolean spArea=false;
    private boolean smProv1=false;
    private boolean smProv2=false;
    private DefaultTableModel dtm;
    private TableSorter2 sorter;
    private int kaizen1 =0;
    private int kaizen2 =0;

    private Hashtable tValue = new Hashtable();
    private Hashtable yValue = new Hashtable();
    private Hashtable taUnit = new Hashtable();
    private Hashtable yaUnit = new Hashtable();
    private Hashtable careRate = new Hashtable();
    private Hashtable careCode = new Hashtable();
    private Hashtable firstDate= new Hashtable();
    private String timeCode[];
    private String taKaizenCode[];
    private String yaKaizenCode[];

    public QkanTsusyoData(String dbUri,String dbUser,String dbPass) {
      this.dbUri = dbUri;
      this.dbUser = dbUser;
      this.dbPass = dbPass;
      dbm = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
      StringBuffer buf = new StringBuffer();
      buf.append("select PROVIDER_ID,PROVIDER_NAME,SPECIAL_AREA_FLAG from PROVIDER ");
      buf.append("where PROVIDER_ID in (");
      buf.append("   select PROVIDER_ID from PROVIDER_SERVICE");
      buf.append("    where SYSTEM_SERVICE_KIND_DETAIL in (11511,16511)");
      buf.append(")");
      String sql = buf.toString(); 
      if (dbm.connect()) {
        dbm.execQuery(sql);
        dbm.Close();
        Rows = dbm.Rows;
        data = new String[Rows][4];
        for (int i=0;i<Rows;i++) {
          StringBuffer sb = new StringBuffer();
          data[i][0] = dbm.getData("PROVIDER_NAME",i).toString()
                      +" ( "
                      +dbm.getData("PROVIDER_ID",i).toString()
                      +" ) ";
          data[i][1] = dbm.getData("PROVIDER_ID",i).toString();
          data[i][2] = dbm.getData("PROVIDER_NAME",i).toString();
          data[i][3] = dbm.getData("SPECIAL_AREA_FLAG",i).toString();
        }
        cBox = new DngGenericCombo(data);
        currentProvider = data[0][1];
        curProviderName = data[0][2];
        if (Integer.parseInt(data[0][3])==2) spArea = true;
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
      pnl.setOpaque(false);
      pn1.setOpaque(false);
      pn2.setOpaque(false);
      pn3.setOpaque(false);
      tPanel.setOpaque(false);
    }

    public void setUnit(String dat) {
      int timeCode;
      String osn = System.getProperty("os.name").substring(0,3);
      //if (osn.equals("Win")) {
        tValue.put("1150104",(new String[] {"","2\uff5e3hr","3\uff5e5hr","5\uff5e7hr","7\uff5e9hr","9\uff5e10hr","10\uff5e11hr","11\uff5e12hr","12\uff5e13hr","13\uff5e14hr"}));
        tValue.put("1150104R",(new String[] {"","3\uff5e6hr","6\uff5e8hr"}));
/*
      }
      else {
        tValue.put("1150104",(new String[] {"","2〜3hr","3〜5hr","5〜7hr","7〜9hr","9〜10hr","10〜11hr","11〜12hr"}));
        tValue.put("1150104R",(new String[] {"","3〜6hr","6〜8hr"}));
      }
*/
      tValue.put("1150106",(new String[] {"","無","有","有"}));
      tValue.put("1150108",(new String[] {"","無","超","欠"}));
      tValue.put("1150112",(new String[] {"","無","有"}));
      tValue.put("1150113",(new String[] {"","小規模","通常","大規模I","大規模II","療養通所"}));
      tValue.put("1150115",(new String[] {"","無","有"}));
      tValue.put("1150116",(new String[] {"","無","有"}));
      tValue.put("1150117",(new String[] {"","無","Iロ","II","III","Iイ"}));
      tValue.put("1150118",(new String[] {"","無","有"}));
      tValue.put("1150119",(new String[] {"","無","有"}));
      tValue.put("1150120",(new String[] {"","無","有"}));
      tValue.put("1150121",(new String[] {"","無","有"}));
      tValue.put("1150122",(new String[] {"","無","有"}));
      tValue.put("1150123",(new String[] {"","無","有"}));
      tValue.put("1150124",(new String[] {"","無","有"}));
      tValue.put("12",(new String[] {"","無","有"}));
      tValue.put("16",(new String[] {"","無","有"}));
      tValue.put("18",(new String[] {"","無","片","複"}));
      tValue.put("KAIZEN",(new String[] {"","無","II","III","IV","I"}));

      yValue.put("1650101",(new String[] {"","無","超","欠"}));
      yValue.put("1650102",(new String[] {"","無","有"}));
      yValue.put("1650103",(new String[] {"","無","有"}));
      yValue.put("1650104",(new String[] {"","無","有"}));
      yValue.put("1650105",(new String[] {"","無","有"}));
      yValue.put("1650106",(new String[] {"","無","有"}));
      yValue.put("1650107",(new String[] {"","無","有"}));
      yValue.put("1650108",(new String[] {"","無","有"}));
      yValue.put("1650109",(new String[] {"","無","Iロ","II","Iイ","無","Iロ","II","Iイ"}));
      yValue.put("12",(new String[] {"","無","有","無","有"}));
      yValue.put("16",(new String[] {"","無","有","無","有"}));
      yValue.put("KAIZEN",(new String[] {"","無","II","III","IV","I"}));
      yValue.put("MULTI",(new String[] {"","無","無","I1","無","I2","I3","II"}))
;

      StringBuffer buf = new StringBuffer();
      buf.append("select service_code_item,service_unit,system_service_kind_detail,system_service_code_item from m_service_code ");
      buf.append(" where system_service_kind_detail in (11511,16511) ");
      buf.append(" and SUBSTRING(system_service_code_item from 1 for 1)='Z'");
      buf.append(" and service_valid_start<='");
      buf.append(dat);
      buf.append("' and service_valid_end>='");
      buf.append(dat);
      buf.append("' order by system_service_kind_detail,service_code_item");
      System.out.println("KAI16="+KAI16+"\n"+buf.toString());
      if (dbm.connect()) {
        int COLU = 0;
        dbm.execQuery(buf.toString());
        dbm.Close();
        taUnit.put("1150119", (new int[] {0,0,
          Integer.parseInt(dbm.getData(1,COLU++).toString())})); //個別１
        taUnit.put("1150120", (new int[] {0,0,
          Integer.parseInt(dbm.getData(1,COLU++).toString())})); //個別２
        taUnit.put("1150106", (new int[] {0,0,
          Integer.parseInt(dbm.getData(1,COLU++).toString())})); //入浴
        taUnit.put("1150121", (new int[] {0,0,
          Integer.parseInt(dbm.getData(1,COLU++).toString())})); //認知症
        taUnit.put("1150122", (new int[] {0,0,
          Integer.parseInt(dbm.getData(1,COLU++).toString())})); //中重度
        taUnit.put("1150116", (new int[] {0,0,
          Integer.parseInt(dbm.getData(1,COLU++).toString())})); //栄養
        taUnit.put("1150112", (new int[] {0,0,
          Integer.parseInt(dbm.getData(1,COLU++).toString())})); //口腔
        taUnit.put("16", (new int[] {0,0,
          Integer.parseInt(dbm.getData(1,COLU++).toString())})); //同一住居
        taUnit.put("18", (new int[] {0,0,
          Integer.parseInt(dbm.getData(1,COLU++).toString()),    //送迎減算片道
          Integer.parseInt(dbm.getData(1,COLU++).toString())*2})); //送迎減算往復
        if (!KAI16) {
          System.out.println("KAI16="+KAI16+"\n");
          taUnit.put("1150123", (new int[] {0,0,
            Integer.parseInt(dbm.getData(1,COLU++).toString())})); //個別送迎強化
          taUnit.put("1150124", (new int[] {0,0,
            Integer.parseInt(dbm.getData(1,COLU++).toString())})); //入浴強化
        }
        taUnit.put("1150117", (new int[] {0,0,
          Integer.parseInt(dbm.getData(1,COLU+1).toString()),    //サービスI2(ロ)
          Integer.parseInt(dbm.getData(1,COLU+2).toString()),    //サービスII
          (!KAI16) ? Integer.parseInt(dbm.getData(1,COLU+3).toString()):0,    //サービスIII
          Integer.parseInt(dbm.getData(1,COLU).toString())})); //サービスI1(イ)
        COLU += (!KAI16) ? 4:3;
        taUnit.put("KAIZEN", (new int[] {0,0,
          Integer.parseInt(dbm.getData(1,COLU++).toString()),     //処遇改善II
          Integer.parseInt(dbm.getData(1,COLU++).toString()),    //処遇改善III
          Integer.parseInt(dbm.getData(1,COLU++).toString()),    //処遇改善IV
          Integer.parseInt(dbm.getData(1,COLU++).toString())})); //処遇改善I
        taUnit.put("1150115", (new int[] {0,0,
          Integer.parseInt(dbm.getData(1,COLU++).toString())})); //若年
        taUnit.put("12",(new int[] {0,0,
          Integer.parseInt(dbm.getData(1,COLU++).toString())})); //中山間
        COLU = (!KAI16) ? 16:13;
        taKaizenCode = new String[] {"","",
          dbm.getData(3,COLU++).toString(),     //処遇改善II
          dbm.getData(3,COLU++).toString(),     //処遇改善III
          dbm.getData(3,COLU++).toString(),    //処遇改善IV
          dbm.getData(3,COLU++).toString() //処遇改善I
        };
        COLU = (!KAI16) ? 22:19;

        yaUnit.put("1650104", (new int[] {0,0,
          Integer.parseInt(dbm.getData(1,COLU++).toString())})); //予運動
        yaUnit.put("1650105", (new int[] {0,0,
          Integer.parseInt(dbm.getData(1,COLU++).toString())})); //予栄養
        yaUnit.put("1650106", (new int[] {0,0,
          Integer.parseInt(dbm.getData(1,COLU++).toString())})); //予口腔
        yaUnit.put("1650107", (new int[] {0,0,
          Integer.parseInt(dbm.getData(1,COLU++).toString())})); //予評価
        yaUnit.put("MULTI", (new int[] {0,0,0,
          Integer.parseInt(dbm.getData(1,COLU++).toString()),0,  //予複数I1
          Integer.parseInt(dbm.getData(1,COLU++).toString()),    //予複数I2
          Integer.parseInt(dbm.getData(1,COLU++).toString()),    //予複数I3
          Integer.parseInt(dbm.getData(1,COLU++).toString())})); //予複数II
        yaUnit.put("1650103", (new int[] {0,0,
          Integer.parseInt(dbm.getData(1,COLU++).toString())})); //予向上
        yaUnit.put("1650109", (new int[] {0,0,
          Integer.parseInt(dbm.getData(1,COLU++).toString()),    //予サービスI21(ロ)
          Integer.parseInt(dbm.getData(1,COLU+1).toString()),    //予サービスII1
          Integer.parseInt(dbm.getData(1,COLU+5).toString()),0,  //予サービスI11(イ)
          Integer.parseInt(dbm.getData(1,COLU).toString()),    //予サービスI22(ロ)
          Integer.parseInt(dbm.getData(1,COLU+2).toString()),    //予サービスII2
          Integer.parseInt(dbm.getData(1,COLU+6).toString())})); //予サービスI12(イ)
        yaUnit.put("16", (new int[] {0,0,
          Integer.parseInt(dbm.getData(1,COLU+3).toString()),0,  //予同一住所
          Integer.parseInt(dbm.getData(1,COLU+4).toString())})); //予同一住所
        COLU += 7;
        yaUnit.put("1650108", (new int[] {0,0,
          Integer.parseInt(dbm.getData(1,COLU++).toString())})); //予若年
        yaUnit.put("KAIZEN", (new int[] {0,0,
          Integer.parseInt(dbm.getData(1,COLU+1).toString()),     //処遇改善II
          Integer.parseInt(dbm.getData(1,COLU+2).toString()),    //処遇改善III
          Integer.parseInt(dbm.getData(1,COLU+3).toString()),    //処遇改善IV
          Integer.parseInt(dbm.getData(1,COLU).toString())})); //処遇改善I
        COLU += 4;
        yaUnit.put("12", (new int[] {0,0,
          Integer.parseInt(dbm.getData(1,COLU++).toString()),    //予中山間
          Integer.parseInt(dbm.getData(1,COLU).toString())})); //予中山間日割
        COLU -= 4;
        yaKaizenCode = new String[] {"","",
          dbm.getData(3,COLU++).toString(),     //処遇改善II
          dbm.getData(3,COLU++).toString(),    //処遇改善III
          dbm.getData(3,COLU).toString(),    //処遇改善IV 
          dbm.getData(3,COLU-3).toString() //処遇改善I
        };
        buf.delete(0,buf.length());
        buf.append("select provider_id,system_service_kind_detail,");
        buf.append("system_bind_path,detail_value from ");
        buf.append("PROVIDER_SERVICE_DETAIL_INTEGER,PROVIDER_SERVICE ");
        buf.append("where provider_service.provider_service_id = ");
        buf.append("provider_service_detail_integer.provider_service_id ");
        buf.append("and provider_id='");
        buf.append(currentProvider);
        buf.append("' and system_service_kind_detail in ('11511','16511') ");
        buf.append("and system_bind_path = 4 ");
        buf.append("order by system_service_kind_detail");
        dbm.connect();
        dbm.execQuery(buf.toString());
        dbm.Close();
        System.out.println(buf.toString());
        int mountFlg[] = new int[] {0,0,0,0};
        if (dbm.Rows==1) {
          if (Integer.parseInt(dbm.getData(1,0).toString())==11511) {
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
    }

    public JPanel searchCondition() {
      JComboBox cbx = cBox.getComboBox();
      ActionListener pChange = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          JComboBox cb = (JComboBox)e.getSource();
          int pvInd = cb.getSelectedIndex();
          currentProvider = getData(pvInd,1);
          curProviderName = getData(pvInd,2);
          if (Integer.parseInt(getData(pvInd,3))==2) spArea = true;
          else spArea = false;
        System.out.println(currentProvider+" Special = "+spArea);
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
        buf.append("' and SYSTEM_SERVICE_KIND_DETAIL in (11511,16511) ");
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
      buf.append("' and ((SYSTEM_SERVICE_KIND_DETAIL=11511 and ");
      buf.append("substring(JOTAI_CODE from 1 for 1)=2) or ");
      buf.append("(SYSTEM_SERVICE_KIND_DETAIL=16511 and ");
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
      buf.append(" and system_service_kind_detail in (11511,16511) ");
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
      Hashtable firsted= new Hashtable();
      Long diffTime;
      double difft;
      KAI16 = ((targetYear==2016 && targetMonth>=4) || (targetYear>2016) ) ? true : false;
      Calendar cal1 = Calendar.getInstance();
      String date="Panel set start at "+cal1.get(Calendar.YEAR)+"."+(cal1.get(Calendar.MONTH) + 1) +"."+cal1.get(Calendar.DATE) +" "+cal1.get(Calendar.HOUR) + ":"+cal1.get(Calendar.MINUTE)+":"+cal1.get(Calendar.SECOND)+"."+cal1.get(Calendar.MILLISECOND);
      System.out.println(date);
      pn2.setVisible(false);      
      pn2.removeAll();
      pnl.setVisible(false);      
      pnl.removeAll();
      int detYear = (targetMonth>3) ? targetYear:targetYear-1;
      int nextMonth = targetMonth+1;
      int nextYear = targetYear;
      int kaisei = 0;
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
        buf.append("SERVICE.PATIENT_ID,SERVICE.SERVICE_ID as SID,");
        buf.append("SYSTEM_SERVICE_KIND_DETAIL,JOTAI_CODE,PATIENT_BIRTHDAY,");
        buf.append("count(SERVICE.SERVICE_ID),INSURE_RATE,");
        buf.append("min(INSURE_VALID_START) as INSURE_VALID_START,");
        buf.append("max(INSURE_VALID_END) as INSURE_VALID_END,");
        buf.append("min(extract(DAY from SERVICE_DATE)) as FIRST_DAY");
        buf.append(",SERVICE_USE_TYPE");
        buf.append(",substring(SERVICE.LAST_TIME from 1 for 16) as LAST,");
        buf.append("SERVICE_DATE,");
        buf.append("SERVICE_DETAIL_DATE_");
        buf.append(detYear+".DETAIL_VALUE as STARTT");
        buf.append(" from SERVICE ");
        buf.append(" inner join SERVICE_DETAIL_DATE_"+detYear+" on ");
        buf.append("SERVICE.SERVICE_ID=SERVICE_DETAIL_DATE_"+detYear);
        buf.append(".SERVICE_ID and SYSTEM_BIND_PATH=3");
        buf.append(" inner join PATIENT on ");
        buf.append("(PATIENT.PATIENT_ID=SERVICE.PATIENT_ID and DELETE_FLAG=0)");
        buf.append(" inner join PATIENT_NINTEI_HISTORY on ");
        buf.append("(PATIENT_NINTEI_HISTORY.PATIENT_ID=SERVICE.PATIENT_ID and");
        buf.append(" NINTEI_HISTORY_ID in ");
        buf.append("(select max(NINTEI_HISTORY_ID) from ");
        buf.append("PATIENT_NINTEI_HISTORY where PATIENT_ID=SERVICE.PATIENT_ID");
        buf.append(" and INSURE_VALID_END>='");
        buf.append(nStart);
        buf.append("' and SYSTEM_INSURE_VALID_END>='");
        buf.append(nStart);
        if (targetDay>0) {
          buf.append("' and INSURE_VALID_START<='");
          buf.append(nStart);
          buf.append("' and SYSTEM_INSURE_VALID_START<='");
          buf.append(nStart);
          //buf.append("'");
        }
        if (targetDay==0) {
          buf.append("' and INSURE_VALID_START<'");
          buf.append(nEnd);
          buf.append("' and SYSTEM_INSURE_VALID_START<'");
          buf.append(nEnd);
        }

        buf.append("')) where ((SYSTEM_SERVICE_KIND_DETAIL=11511 and ");
        buf.append("substring(JOTAI_CODE from 1 for 1)=2) or ");
        buf.append("(SYSTEM_SERVICE_KIND_DETAIL=16511 and ");
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
        buf.append(",SERVICE.SERVICE_ID,LAST");
        buf.append(",STARTT,SERVICE_USE_TYPE,SERVICE_DATE,JOTAI_CODE");
        buf.append(" order by SYSTEM_SERVICE_KIND_DETAIL,SERVICE.PATIENT_ID");
        buf.append(",LAST desc,service_date asc,SID asc,STARTT asc");

        String sql = buf.toString();
        System.out.println(sql);
        dbm.execQuery(sql);
        dbm.Close();
        Vector pdata = new Vector();
        totalRow = new Vector();
        int pNo=-1;
        int sbp=-1;
        int uTp=-1;
        int ln = 0;
        int totalKaizen=0;
        int totalFee1=0;
        int totalFee2=0;
        int totalFee3=0;
        int totalFee4=0;
        int totalFee5=0;
        int totalCount=0;
        int eiyoCount=0;
        int koukCount=0;
        int trCols=0;
        double totalTime1=0.0;
        double totalTime2=0.0;
        
        String sids = "";
        boolean monfin = false;
        boolean second = false;
        String ItemCode="";
        for (int i=0;i<dbm.Rows;i++){
          int lastP = pNo;
          int lastSbp = sbp;
          String lastCode = ItemCode;
          pNo = Integer.parseInt(dbm.getData(2,i).toString());
          sbp = Integer.parseInt(dbm.getData(4,i).toString());
          if (pNo!=lastP || sbp!=lastSbp) {
            uTp = Integer.parseInt(dbm.getData("SERVICE_USE_TYPE",i).toString());
            second = false;
            eiyoCount=0;
            koukCount=0;
            if (targetDay==0) {
              firstDate.put(dbm.getData("PATIENT_ID",i).toString(),dbm.getData("FIRST_DAY",i).toString());
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
            second = true;
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
          double mountRate=0;
          int sCount = Integer.parseInt(dbm.getData("COUNT",i).toString());
          int insRate = Integer.parseInt(dbm.getData("INSURE_RATE",i).toString());
          String insStart = dbm.getData("INSURE_VALID_START",i).toString();
          String insEnd = dbm.getData("INSURE_VALID_END",i).toString();

          Vector pline = new Vector();
          if (!firsted.containsKey(new Integer(pNo)))
            firsted.put(new Integer(pNo), "false");
          
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
          String kind = (sbp==11511) ? 
                        "介":"予";
          Calendar cal21 = Calendar.getInstance();
          String cR="1";
          if (dbm.getData("JOTAI_CODE",i)!=null) {
            cR = dbm.getData("JOTAI_CODE",i).toString();
            String cRate = (String)careRate.get(cR);
            pline.addElement(cRate);
          } else {
            pline.addElement("");
          }
          pline.addElement(kind);
          int vt=0;
          int vt2=0;
          double sti = 0.0;
          boolean stio = true;
          DngDBAccess dbm2 = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
          int kaizenVal = 0;
          if (targetDay==0) {
            //処遇改善取得----------------------------------------------
            if ( (sbp==11511 && kaizen1>1) || (sbp==16511 && kaizen2>1) ) {
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

              dbm2.connect();
              dbm2.execQuery(buf.toString());
              dbm2.Close();
              if (dbm2.Rows>0) {
                int cYear = Integer.parseInt(dbm2.getData(0,0).toString());
                if (Integer.parseInt(dbm2.getData(1,0).toString())<4) cYear--;

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
                if (sbp==11511) 
                  buf.append(taKaizenCode[kaizen1]);
                else
                  buf.append(yaKaizenCode[kaizen2]);
                buf.append("');");
                System.out.println(buf.toString());
                dbm2.connect();
                dbm2.execQuery(buf.toString());
                dbm2.Close();
                if (dbm2.Rows>0) {
                  kaizenVal = Integer.parseInt(dbm2.getData(0,0).toString());
                }
              }
            }
            //処遇改善取得----------------------------------------------

              buf.delete(0,buf.length());
              buf.append("select SERVICE_ID,SYSTEM_BIND_PATH,");
              buf.append("extract(HOUR from DETAIL_VALUE),");
              buf.append("extract(MINUTE from DETAIL_VALUE)");
              buf.append(" from SERVICE_DETAIL_DATE_");
              buf.append(detYear);
              buf.append(" where SERVICE_ID in ("+sids+")");
              buf.append(" and SYSTEM_BIND_PATH");
              buf.append(" in (3,4)");
              buf.append(" order by SERVICE_ID,SYSTEM_BIND_PATH;");
              sql = buf.toString();
              System.out.println(sql);
              dbm2.connect();
              dbm2.execQuery(sql);
              dbm2.Close();
          }
          else if (targetDay>0) {
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
            int s_hou=0;
            int s_min=0;
            if (dbm2.getData(2,0)!=null) {
              hou = dbm2.getData(2,0).toString();
              min = dbm2.getData(3,0).toString();
              s_hou = Integer.parseInt(hou);
              s_min = Integer.parseInt(min);
              if (hou.length()==1) hou = "0"+hou;
              if (min.length()==1) min = "0"+min;
              ti = hou+":"+min;
            } else {
              stio = false;
              ti = "";
            }
            pline.addElement(ti);
            if (dbm2.getData(2,1)!=null) {
              hou = dbm2.getData(2,1).toString();
              min = dbm2.getData(3,1).toString();
              if (hou.length()==1) hou = "0"+hou;
              if (min.length()==1) min = "0"+min;
              ti = hou+":"+min;
              s_hou = Integer.parseInt(hou) - s_hou;
              s_min = Integer.parseInt(min) - s_min;
              vt = s_hou; 
              if (s_min>0) vt2 = 1;
              if (s_min<0) vt2 = -1;
            } else {
              ti = "";
            }
            pline.addElement(ti);
          }
          buf.delete(0,buf.length());
          buf.append("select SYSTEM_BIND_PATH,");
          if (targetDay==0) {
            buf.append("max(DETAIL_VALUE) as DETAIL_VALUE");
            buf.append(",sum(DETAIL_VALUE-1) as DETAIL_VALUE2");
          }
          else
            buf.append("DETAIL_VALUE");
          buf.append(" from SERVICE_DETAIL_INTEGER_");
          buf.append(detYear);
          buf.append(" where SERVICE_ID");
          if (targetDay==0) 
            buf.append(" in ("+sids+")");
          else
            buf.append("="+sNo);
          if (targetDay==0) 
            buf.append(" group by SYSTEM_BIND_PATH ");
          buf.append(" order by SYSTEM_BIND_PATH desc;");
          sql = buf.toString();
          System.out.println(sql);
          dbm2.connect();
          dbm2.execQuery(sql);
          dbm2.Close();

          System.out.println("dbm2:"+dbm2.Rows);
          int inic=0;
          int hId=0;
          int jimuId=0;
          int timeId=0;
          int jPlus=0;
          int tPlus=0;
          int mPlus=0;
          int addUnit=0;
          int vtime=0;
          double unitRate;
          boolean hiwari = false;
          boolean smProv = false;
          int kaizen = 0;
          ItemCode = "";
          if (sbp==11511) {
            smProv = smProv1;
            kaizen = kaizen1;
            unitRate = tunitRate;
            Hashtable tVal = new Hashtable();
            tVal.put("1150104","");
            tVal.put("1150106","");
            tVal.put("1150108","");
            tVal.put("1150112","");
            tVal.put("1150113","");
            tVal.put("1150115","");
            tVal.put("1150116","");
            tVal.put("1150117","");
            tVal.put("1150119","");
            tVal.put("1150120","");
            tVal.put("1150121","");
            tVal.put("1150122","");
            tVal.put("1150123","");
            tVal.put("1150124","");
            tVal.put("12","");
            tVal.put("16","");
            tVal.put("18","");
            String[] ssCode = new String[] {"0","0","0","0",(String)careCode.get(cR)};
            int ssCount = 0; 
            int kiboId = 0;
            for (int j=0;j<dbm2.Rows;j++){
              int sbp0 = Integer.parseInt(dbm2.getData("SYSTEM_BIND_PATH",j).toString());
              System.out.println("Rows start: "+j+" sbp= "+sbp0);
              if (sbp0==15) continue;
              if (sbp0==14) {
                kaisei=Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                System.out.println("kaisei: "+kaisei);
              }
              else if (sbp0==1150104) {
                int key = Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                String[] val;
                if (kiboId==5) {
                  ssCode[1]="1";
                  ssCode[2]=Integer.toString(key);
                  val = (String[])tValue.get(Integer.toString(sbp0)+"R"); 
                  ssCode[4]="1";
                }
                else {
                  ssCode[1]=Integer.toString(key);
                  ssCode[2]="1";
                  val = (String[])tValue.get(Integer.toString(sbp0)); 
                }
                tVal.put(Integer.toString(sbp0),val[key]);
              }
              else if (sbp0==1150108) {
                int key = Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                String[] val = (String[])tValue.get(Integer.toString(sbp0)); 
                tVal.put(Integer.toString(sbp0),val[key]);
                ssCode[3] = Integer.toString(key);
              }
              else if (sbp0==1150113) {
                ssCode[0] = dbm2.getData("DETAIL_VALUE",j).toString();
                kiboId = Integer.parseInt(ssCode[0]);
                String[] val = (String[])tValue.get(Integer.toString(sbp0)); 
                tVal.put(Integer.toString(sbp0),val[kiboId]);
                System.out.println("kiboID: "+kiboId);
              }
              else if (sbp0==12 || sbp0==1150112 || sbp0==1150116) {
                int key = Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                String[] val = (String[])tValue.get(Integer.toString(sbp0)); 
                //pline.addElement(val[key]);
                tVal.put(Integer.toString(sbp0),val[key]);
                int[] add = (int[]) taUnit.get(dbm2.getData("SYSTEM_BIND_PATH",j).toString());
                System.out.println("sbp = "+sbp0+" key = "+key+" add= "+add[key]);
                if (sbp0==12) mountRate = (double)add[key]/100.0; 
                else {
                  if (targetDay==0) {
                    int kcn = Integer.parseInt(dbm2.getData("DETAIL_VALUE2",j).toString());
                    if (kcn==1) { 
                      addUnit += add[key]; 
                      tVal.put(Integer.toString(sbp0),"1回");
                    }
                    if (kcn>1)  {
                      addUnit += add[key] * 2; 
                      tVal.put(Integer.toString(sbp0),"2回");
                    }
                  }
                  else {
                    if (key==2) {
                       if (sbp0==1150112) {
                         koukCount++;
                         if (koukCount<=2) addUnit += add[key];
                         else tVal.put(Integer.toString(sbp0),"無");
                       }
                       if (sbp0==1150116) {
                         eiyoCount++;
                         if (eiyoCount<=2)  addUnit += add[key];
                         else tVal.put(Integer.toString(sbp0),"無");
                       }
                    }
                  }
                }
              }
/*
              else if (sbp0==16 || sbp0==1150106 || sbp0==1150117 || sbp0==1150119 || sbp0==1150120 ) {
                int key = Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                String[] val = (String[])tValue.get(Integer.toString(sbp0)); 
                tVal.put(Integer.toString(sbp0),val[key]);
                int[] add = (int[]) taUnit.get(dbm2.getData("SYSTEM_BIND_PATH",j).toString());
                addUnit += add[key]; 
                System.out.println("sbp = "+sbp0+" key = "+key+" add= "+add[key]);
              }
*/
              else {
                if (!tValue.containsKey(Integer.toString(sbp0))) continue;
                int key = Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                String[] val = (String[])tValue.get(Integer.toString(sbp0)); 
                //pline.addElement(val[key]);
                tVal.put(Integer.toString(sbp0),val[key]);
                int[] add = (int[]) taUnit.get(dbm2.getData("SYSTEM_BIND_PATH",j).toString());
                addUnit += add[key]; 
                System.out.println("sbp = "+sbp0+" key = "+key+" add= "+add[key]);
              }
              System.out.println("num"+sbp0+"num = "+dbm2.getData("DETAIL_VALUE",j));
            }
            String[] val = (String[])tValue.get("KAIZEN");
            if (targetDay==0 && kaizenVal!=0) {
              tVal.put("KAIZEN","("+val[kaizen]+") "+Integer.toString(kaizenVal));
              totalKaizen = totalKaizen + kaizenVal;
            }
            else {
              tVal.put("KAIZEN",val[kaizen]);
            }
            pline.addElement((String)tVal.get("1150104"));
            pline.addElement((String)tVal.get("1150113"));
            pline.addElement((String)tVal.get("1150108"));
            pline.addElement((String)tVal.get("12"));
            pline.addElement((String)tVal.get("1150106"));
            pline.addElement((String)tVal.get("1150119"));
            pline.addElement((String)tVal.get("1150120"));
            pline.addElement((String)tVal.get("1150115"));
            pline.addElement((String)tVal.get("1150116"));
            pline.addElement((String)tVal.get("1150112"));
            pline.addElement((String)tVal.get("16"));
            pline.addElement((String)tVal.get("1150117"));
            pline.addElement((String)tVal.get("1150121"));
            pline.addElement((String)tVal.get("1150122"));
            pline.addElement((String)tVal.get("18"));
            if (!KAI16) {
              pline.addElement((String)tVal.get("1150123"));
              pline.addElement((String)tVal.get("1150124"));
            }
            pline.addElement("");
            pline.addElement("");
            pline.addElement("");
            pline.addElement((String)tVal.get("KAIZEN"));
            pline.addElement("");
            if (targetDay>0) pline.addElement("");
            for (int ii=0;ii<5;ii++) ItemCode += ssCode[ii];
            System.out.println("ItemtCode : "+ItemCode);
            int[] add = (int[])taUnit.get("KAIZEN");
            kaizenRate = (double)add[kaizen]/1000.0; 
          }
          else {
            kaizen = kaizen2;
            unitRate = yunitRate;
            Hashtable yoVal = new Hashtable();
            yoVal.put("1650101","");
            yoVal.put("1650102","");
            yoVal.put("1650103","");
            yoVal.put("1650104","");
            yoVal.put("1650105","");
            yoVal.put("1650106","");
            yoVal.put("1650107","");
            yoVal.put("1650108","");
            yoVal.put("1650109","");
            yoVal.put("12","");
            yoVal.put("16","");
            String[] ssCode = new String[] {(String)careCode.get(cR),"1","1"};
            int ssCount = 0; 

            int multi = 0;
            int multi_less = 0;
            for (int j=0;j<dbm2.Rows;j++) {
              int sbp0 = Integer.parseInt(dbm2.getData("SYSTEM_BIND_PATH",j).toString());
              System.out.println("Rows start: "+j+" sbp0= "+sbp0);
              if (sbp0==15) continue;
              if (sbp0==14) {
                kaisei=Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                System.out.println("kaisei: "+kaisei);
              }
              else {
                if (!yValue.containsKey(Integer.toString(sbp0))) continue;
                int key = Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                String[] val = (String[])yValue.get(Integer.toString(sbp0)); 
               int[] add = (int[]) yaUnit.get(Integer.toString(sbp0));
                //pline.addElement(val[key]);
                if (sbp0==1650101) {
                  ssCode[1] = dbm2.getData("DETAIL_VALUE",j).toString();
                  System.out.println("num"+sbp0+" = "+dbm2.getData("DETAIL_VALUE",j));
                }
                else if (sbp0==1650102) {
                  ssCode[2] = dbm2.getData("DETAIL_VALUE",j).toString();
                  if (key==2) hiwari=true;
                  System.out.println("num"+sbp0+" = "+dbm2.getData("DETAIL_VALUE",j));
                }
                else  if (sbp0==12 && !second) mountRate = (double)add[key]/100.0; 
                else {
                  if (sbp0==1650109 && ssCode[0]=="4") key += 4;
                  if (sbp0==16) key += (cR.equals("13")) ? 2:0;
                  addUnit += add[key];
                  System.out.println("num"+sbp0+" = "+dbm2.getData("DETAIL_VALUE",j)+"("+key+") add = "+add[key]);
                }
                yoVal.put(Integer.toString(sbp0),val[key]);
// multi-serv (value set)
                switch (sbp0) {
                  case 1650104 : if (key==2) {
                                   multi += 1;
                                   multi_less += add[key];
                                 }
                                 break;
                  case 1650105 : if (key==2) {
                                   multi += 2;
                                   multi_less += add[key];
                                 }
                                 break;
                  case 1650106 : if (key==2) {
                                   multi += 4;
                                   multi_less += add[key];
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
            val = (String[])yValue.get("MULTI");
            yoVal.put("MULTI",val[multi]);
            pline.addElement(""); //時間区分
            pline.addElement(""); //施設区分
            pline.addElement((String)yoVal.get("1650101")); //人員
            pline.addElement((String)yoVal.get("12")); //中山間
            pline.addElement(""); //入浴
            pline.addElement(""); //個別I
            pline.addElement(""); //個別II
            pline.addElement((String)yoVal.get("1650108")); //若年
            pline.addElement((String)yoVal.get("1650105")); //栄養
            pline.addElement((String)yoVal.get("1650106")); //口腔
            pline.addElement((String)yoVal.get("16")); //同住
            pline.addElement((String)yoVal.get("1650109")); //サービス
            pline.addElement(""); //
            pline.addElement(""); //
            pline.addElement(""); //
            if (!KAI16) {
              pline.addElement(""); //
              pline.addElement(""); //
            }
            pline.addElement((String)yoVal.get("1650103")); //生活
            pline.addElement((String)yoVal.get("1650104")); //運動
            pline.addElement((String)yoVal.get("1650107")); //評価
            pline.addElement((String)yoVal.get("KAIZEN")); //改善
            pline.addElement((String)yoVal.get("MULTI"));   //選択複数
            if (targetDay>0) pline.addElement((String)yoVal.get("1650102")); //日割り

            for (int ii=0;ii<3;ii++) ItemCode += ssCode[ii];
            System.out.println("ItemtCode : "+ItemCode);
            int[] add = (int[])taUnit.get("KAIZEN");
            kaizenRate = (double)add[kaizen]/1000.0;
            if (hiwari && mountRate>0.0) {
              add = (int[]) yaUnit.get("12");
              mountRate = (double)add[3]/100.0; 
            }
//
            add = (int[])yaUnit.get("MULTI");
            addUnit += add[multi];
            if (add[multi] != 0) addUnit -= multi_less;
            else multi_less = 0;
            System.out.println(" multi_add = "+add[multi]+" multi_less = -"+multi_less);
//

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
            dbm2.connect();
            dbm2.execQuery(buf.toString());
            dbm2.Close();
            int cRows;
            DngDBAccess dbm3 = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
            if (dbm2.Rows>0) { 
              int cYear = Integer.parseInt(dbm2.getData(0,0).toString());
              if (Integer.parseInt(dbm2.getData(1,0).toString())<4) cYear--; 
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
              if (sbp==11511) buf.append("and DETAIL_VALUE='15')");
              else buf.append("and DETAIL_VALUE='65')");
              //buf.append(" and SYSTEM_BIND_PATH");
              //buf.append(" in (701008,701014,701015,701016,701017)");
              buf.append(" and (SYSTEM_BIND_PATH=701008 or ");
              buf.append("SYSTEM_BIND_PATH>=701014 and ");
              buf.append("SYSTEM_BIND_PATH<=701026)");
              buf.append(" order by SYSTEM_BIND_PATH;");
              sql = buf.toString();
              System.out.println(sql);
              dbm3.connect();
              dbm3.execQuery(sql);
              dbm3.Close();
              cRows = dbm3.Rows;
            }
            else cRows=0;
            if (cRows>0) {
              System.out.println("Tani="+dbm3.getData("DETAIL_VALUE",1)+" Tanka="+dbm3.getData("DETAIL_VALUE",2)+"\n");
              int other=0;
              int clid = Integer.parseInt(dbm3.getData("CLAIM_ID",0).toString());
              //if (!hiwari) pline.addElement(new Integer(dbm3.getData("DETAIL_VALUE",0).toString()));
              //else {
              DngDBAccess dbm4 = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
              if (dbm4.connect()) {
                buf.delete(0,buf.length());
                if (hiwari) {
                  buf.append("select count(service_date),SERVICE_USE_TYPE from");
                  buf.append(" (select distinct service_date,SERVICE_USE_TYPE from service");
                }
                else {
                  buf.append("select count(service_id),SERVICE_USE_TYPE from");
                  buf.append(" (select service_id,SERVICE_USE_TYPE from service");
                }
                buf.append(" inner join PATIENT_NINTEI_HISTORY on ");
                buf.append("(PATIENT_NINTEI_HISTORY.PATIENT_ID=");
                buf.append(pNo);
                buf.append(" and NINTEI_HISTORY_ID in ");
                buf.append("(select NINTEI_HISTORY_ID from ");
                buf.append("PATIENT_NINTEI_HISTORY where PATIENT_ID=");
                buf.append(pNo);
                buf.append(" and INSURE_VALID_END>=SERVICE.SERVICE_DATE");
                buf.append(" and SYSTEM_INSURE_VALID_END>=SERVICE.SERVICE_DATE");
                buf.append(" and INSURE_VALID_START<=SERVICE.SERVICE_DATE");
                buf.append(" and SYSTEM_INSURE_VALID_START<=SERVICE.SERVICE_DATE");
                buf.append(")) where SERVICE.PATIENT_ID=");
                buf.append(pNo);
                buf.append(" and ((SYSTEM_SERVICE_KIND_DETAIL=11511 and ");
                buf.append("substring(JOTAI_CODE from 1 for 1)=2) or ");
                buf.append("(SYSTEM_SERVICE_KIND_DETAIL=16511 and ");
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
                dbm4.execQuery(buf.toString());
                dbm4.Close();
                if (dbm4.getData(0,0)!=null) {
                  pline.addElement(new Integer(dbm4.getData(0,0).toString()));
                  totalCount +=Integer.parseInt(dbm4.getData(0,0).toString());
                }
                else 
                  pline.addElement(new String(""));
              }
              else pline.addElement(new String(""));
              //}
              int hiyou = (int)(Float.parseFloat(dbm3.getData("DETAIL_VALUE",1).toString())*Float.parseFloat(dbm3.getData("DETAIL_VALUE",2).toString()));
              int futan = Integer.parseInt(dbm3.getData("DETAIL_VALUE",4).toString());
            
              int kouhiunit,kouhi,jikouhi;
              if (cRows>5) {
                kouhiunit = Integer.parseInt(dbm3.getData("DETAIL_VALUE",5).toString())
                       +Integer.parseInt(dbm3.getData("DETAIL_VALUE",8).toString())
                       +Integer.parseInt(dbm3.getData("DETAIL_VALUE",11).toString());
                kouhi = Integer.parseInt(dbm3.getData("DETAIL_VALUE",6).toString())
                       +Integer.parseInt(dbm3.getData("DETAIL_VALUE",9).toString())
                       +Integer.parseInt(dbm3.getData("DETAIL_VALUE",12).toString());
                jikouhi = Integer.parseInt(dbm3.getData("DETAIL_VALUE",7).toString())
                       +Integer.parseInt(dbm3.getData("DETAIL_VALUE",10).toString())
                       +Integer.parseInt(dbm3.getData("DETAIL_VALUE",13).toString());
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
              dbm3.connect();
              dbm3.execQuery(buf.toString());
              dbm3.Close();
              if (dbm3.Rows>0) {
                 for (int j=1;j<16;j=j+2) {
                   if (dbm3.getData(j,0)!=null) 
                     other += Integer.parseInt(dbm3.getData(j+1,0).toString());
                 }
                 if (dbm3.getData("OTHER_HIMOKU_NO6",0)!=null)
                   other += Integer.parseInt(dbm3.getData("OTHER_PAY_NO6",0).toString());
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
              if (dbm3.connect()) {
                buf.delete(0,buf.length());
                buf.append("select count(service_id),SERVICE_USE_TYPE,");
                buf.append("max(LAST_TIME) as LAST from");
                buf.append(" (select SERVICE_ID,SERVICE_USE_TYPE,");
                buf.append("SERVICE.LAST_TIME from service");
                buf.append(" inner join PATIENT_NINTEI_HISTORY on ");
                buf.append("(PATIENT_NINTEI_HISTORY.PATIENT_ID=");
                buf.append(pNo);
                buf.append(" and NINTEI_HISTORY_ID in ");
                buf.append("(select NINTEI_HISTORY_ID from ");
                buf.append("PATIENT_NINTEI_HISTORY where PATIENT_ID=");
                buf.append(pNo);
                buf.append(" and INSURE_VALID_END>=SERVICE.SERVICE_DATE");
                buf.append(" and SYSTEM_INSURE_VALID_END>=SERVICE.SERVICE_DATE");
                buf.append(" and INSURE_VALID_START<=SERVICE.SERVICE_DATE");
                buf.append(" and SYSTEM_INSURE_VALID_START<=SERVICE.SERVICE_DATE");
                buf.append(")) where SERVICE.PATIENT_ID=");
                buf.append(pNo);
                buf.append(" and ((SYSTEM_SERVICE_KIND_DETAIL=11511 and ");
                buf.append("substring(JOTAI_CODE from 1 for 1)=2) or ");
                buf.append("(SYSTEM_SERVICE_KIND_DETAIL=16511 and ");
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
                buf.append(" order by LAST desc");
                System.out.println(buf.toString());
                dbm3.execQuery(buf.toString());
                dbm3.Close();
                if (dbm3.Rows>0) {
                  if (dbm3.getData(0,0)!=null) {
                    pline.addElement(new Integer(dbm3.getData(0,0).toString()));
                    totalCount +=Integer.parseInt(dbm3.getData(0,0).toString());
                  } else {
                    pline.addElement(new Integer(sCount));
                    if (sCount>0) totalCount += sCount;
                  }
                  pline.addElement(new String(""));
                  pline.addElement(new String(""));
                  pline.addElement(new String(""));
                  pline.addElement(new String(""));
                  pline.addElement(new String(""));
                }
                else {
                    pline.addElement(new String("   -   "));
                    pline.addElement(new String("算出不可"));
                    pline.addElement(new String(""));
                    pline.addElement(new String(""));
                    pline.addElement(new String(""));
                    pline.addElement(new String(""));
                    pline.addElement(new String(""));
                }
              } else {
                    pline.addElement(new String(""));
                    pline.addElement(new String(""));
                    pline.addElement(new String(""));
                    pline.addElement(new String(""));
                    pline.addElement(new String(""));
                    pline.addElement(new String(""));
              }
            }
          }
          else {
            buf.delete(0,buf.length());
            buf.append("select service_unit from m_service_code ");
            buf.append("where system_service_kind_detail='");
            buf.append(sbp);
            buf.append("' and  system_service_code_item='");
            buf.append(ItemCode);
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
              StringBuffer sb = new StringBuffer();
              int p = (int) Integer.parseInt(dbm2.getData(0,0).toString());
              sb.append("basic point = "+p+" add = "+addUnit);
              int pp = p;
              if (mountRate>0.0 && !second ) { 
                int mp = Math.round((float)((double) p * mountRate));
                addUnit += mp;
                sb.append(" mount = "+mp);
              }
              p += addUnit;
              if (kaizenRate>0.0 && !second ) {
                int kp = Math.round((float)((double) p * kaizenRate));
                if (kaizen>2) 
                  kp = Math.round((float)((double)kp*(100-(kaizen-2)*10)/100.0));
                p += kp;
                sb.append(" kaizen = "+kp);
              }
              int hiyou =(int)((double) p * unitRate);
              //int futan = hiyou - (int)((double)hiyou/100.0*(double)insRate);
              int futan = (int)((double)hiyou/100.0*(double)(100-insRate));
 
              //if (hiyou%10>0) futan +=1;
              sb.append(" total point = "+p+" unitRate = "+unitRate+ " hiyou = "+hiyou+" futan = "+futan); 
              System.out.println(sb.toString());
              if (hiwari && second && ItemCode.equals(lastCode)) {
                pline.addElement(new String("   -   "));
                pline.addElement(new String("   -   "));
              } else {
                if (hiyou>0) totalFee1 += hiyou;
                if (futan>0) totalFee2 += futan;
                pline.addElement(new Integer(hiyou));
                pline.addElement(new Integer(futan));
              }
            }
          }
          pdata.addElement(pline);
          trCols = pline.size();
          Calendar cal22 = Calendar.getInstance();
          diffTime = cal22.getTimeInMillis() - cal21.getTimeInMillis();
          difft = Float.parseFloat( diffTime.toString() ) / 1000.000 ;
          System.out.println("PID "+pNo+" [ " + difft + " sec. ]");
        }
        System.out.println(totalTime1+":"+totalTime2+":"+totalCount+":"+totalFee1+":"+totalFee2+":"+totalFee3+":"+totalFee4+":"+totalFee5);
        int cn1,cn2,cn3,cn4,cn5,cn6; 
        cn6= trCols-1;
        cn1= cn6-5;
        cn2= cn6-4;
        cn3= cn6-3;
        cn4= cn6-2;
        cn5= cn6-1;
        for (int c=0;c<trCols;c++) {
          if (c==1) totalRow.addElement(new String("  合 計 値"));
          else if (targetDay>0) {
            if (c==cn5) totalRow.addElement(new Integer(totalFee1));
            else if (c==cn6) totalRow.addElement(new Integer(totalFee2));
            else totalRow.addElement(new String(""));
          }
          else {
            if (c==cn1) totalRow.addElement(new Integer(totalCount));
            else if (c==cn1-2) totalRow.addElement(new Integer(totalKaizen));
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
          JLabel lab2 = new JLabel("　　　　　　　　　　　　　予防サービスで日割りでは無い場合は月間の金額を表示しています。");
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
      Calendar cal2 = Calendar.getInstance();
      date="Panel set end   at "+cal2.get(Calendar.YEAR)+"."+(cal2.get(Calendar.MONTH) + 1) +"."+cal2.get(Calendar.DATE) +" "+cal2.get(Calendar.HOUR) + ":"+cal2.get(Calendar.MINUTE)+":"+cal2.get(Calendar.SECOND)+"."+cal2.get(Calendar.MILLISECOND);
      diffTime = cal2.getTimeInMillis() - cal1.getTimeInMillis();
      difft = Float.parseFloat( diffTime.toString() ) / 1000.000 ;
      System.out.println(date+" [ " + difft + " sec. ]");
    }

    public boolean isSelected() {
      //int sel = usrTbl.getSelectedRow();
      //return (sel!=-1) ? true:false;
      return true;
    }

    public void setSelectable(boolean selectable) {
      isSelectable = selectable;
    }
    public String getTsusyoDataCsv(int pno) {
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
      fieldName.addElement("種類");
      if (td>0) {
        fieldName.addElement("開始時刻");
        fieldName.addElement("終了時刻");
      } 
      fieldName.addElement("時間区分");
      fieldName.addElement("施設区分");
      fieldName.addElement("人員");
      fieldName.addElement("中山間");
      fieldName.addElement("入浴");
      fieldName.addElement("個別I");
      fieldName.addElement("個別II");
      fieldName.addElement("若年");
      fieldName.addElement("栄養");
      fieldName.addElement("口腔");
      fieldName.addElement("同住");
      fieldName.addElement("サー");
      fieldName.addElement("認知");
      fieldName.addElement("中重");
      fieldName.addElement("送減");
      if (!KAI16) {
        fieldName.addElement("送強");
        fieldName.addElement("浴強");
      }
      fieldName.addElement("生活");
      fieldName.addElement("運動");
      fieldName.addElement("評価");
      fieldName.addElement("処遇改善");
      fieldName.addElement("複数");
      if (td==0) {
        fieldName.addElement("回数");
      } else {
        fieldName.addElement("日割");
      }
      fieldName.addElement("費用");
      fieldName.addElement("負担額");
      if (td==0) {
        fieldName.addElement("その他");
        fieldName.addElement("負担額合計");
        fieldName.addElement("公費負担額");
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
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      if (td>0) {
        usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(60);
        usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(60);
      } 
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(65);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(65);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(40);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      if (!KAI16) {
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      }
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(ren);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(60);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(38);
      System.out.println("cid : "+cid);
      if (td==0) {
        usrTbl.getColumnModel().getColumn(cid).setCellRenderer(ren);
      }
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      //sorter.setColumnClass(cid,Integer.class);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(ren);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(60);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(ren);
      usrTbl.getColumnModel().getColumn(cid).setPreferredWidth(60);
      if (td==0) {
        usrTbl.getColumnModel().getColumn(++cid).setCellRenderer(ren);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(80);
        usrTbl.getColumnModel().getColumn(cid).setCellRenderer(ren);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(63);
        usrTbl.getColumnModel().getColumn(cid).setCellRenderer(ren);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(60);
      }
      //usrTbl.getTableHeader().setReorderingAllowed(false);
      JScrollPane scrPane = new JScrollPane();
      scrPane.setOpaque(false);
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
      System.out.println("ColCount = "+usrTbl.getColumnCount());
/*
      if (targetDay==0) {
        num=usrTbl.getColumnCount(); //28;
      } else {
*/
        num=usrTbl.getColumnCount(); //26;
//    }
      float width[] = new float[num];
      int ctype[] = new int[num];
      Arrays.fill(ctype,0);
      ctype[cid] = 2; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = 3; //No.
      width[cid++] = 12; //氏名
      ctype[cid] = 2; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.2"); //年齢
      ctype[cid] = 7;
      width[cid++] = Float.parseFloat("7.0"); //要介護度
      ctype[cid] = 7;
      width[cid++] = 5; //種類
      if (targetDay>0) {
        ctype[cid] = 7;
        width[cid++] = 7; //開始時刻
        ctype[cid] = 7;
        width[cid++] = 7; //終了時刻
      } 
      ctype[cid] = 2;
      width[cid++] = Float.parseFloat("8.0"); //時間区分
      ctype[cid] = 2;
      width[cid++] = Float.parseFloat("7.5"); //施設区分
      ctype[cid] = 7;
      width[cid++] = Float.parseFloat("3.0"); //人員
      ctype[cid] = 7;
      width[cid++] = Float.parseFloat("5.0"); //中山間
      ctype[cid] = 7;
      width[cid++] = Float.parseFloat("3.0"); //入浴
      ctype[cid] = 7;
      width[cid++] = Float.parseFloat("3.0"); //個別I
      ctype[cid] = 7;
      width[cid++] = Float.parseFloat("5.0"); //個別II
      ctype[cid] = 7;
      width[cid++] = Float.parseFloat("3.0"); //若年
      ctype[cid] = 7;
      width[cid++] = Float.parseFloat("3.0"); //栄養
      ctype[cid] = 7;
      width[cid++] = Float.parseFloat("3.0"); //口腔
      ctype[cid] = 7;
      width[cid++] = Float.parseFloat("3.0"); //同住
      ctype[cid] = 7;
      width[cid++] = Float.parseFloat("3.0"); //サー
      ctype[cid] = 7;
      width[cid++] = Float.parseFloat("3.0"); //認知
      ctype[cid] = 7;
      width[cid++] = Float.parseFloat("3.0"); //中重度
      ctype[cid] = 7;
      width[cid++] = Float.parseFloat("3.0"); //送迎減さん
      if (!KAI16) {
        ctype[cid] = 7;
        width[cid++] = Float.parseFloat("3.0"); //送迎強
        ctype[cid] = 7;
        width[cid++] = Float.parseFloat("3.0"); //入浴強化
      }
      ctype[cid] = 7;
      width[cid++] = Float.parseFloat("3.0"); //生活
      ctype[cid] = 7;
      width[cid++] = Float.parseFloat("3.0"); //運動
      ctype[cid] = 7;
      width[cid++] = Float.parseFloat("3.0"); //評価
      ctype[cid] = 2;
      width[cid++] = Float.parseFloat("8.0"); //改善
      ctype[cid] = 7;
      width[cid++] = Float.parseFloat("4.0"); //複数
      ctype[cid] = 7;
      if (targetDay==0) {
        ctype[cid] = 2; // 0 - normal 1 - add comma 2 - align right
      }
      width[cid++] = Float.parseFloat("3.0"); //回数
      ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = 7; //費用
      ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = 7; //負担額
      if (targetDay==0) {
        ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
        width[cid++] = 6; //その他負担額
        ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
        width[cid++] = Float.parseFloat("8.5"); //負担額合計
        ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
        width[cid++] = Float.parseFloat("8"); //公費負担額
      }
      //Calendar cal = Calendar.getInstance();
      //String date=cal.get(Calendar.YEAR)+""+(cal.get(Calendar.MONTH) + 1)
      //            +""+cal.get(Calendar.DATE);
      //String fname = "TSUSYO"+date+".pdf";
      StringBuffer sb = new StringBuffer();
      sb.append("TSUSYO-");
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
      if (pdf.openPDF("通所介護情報")) {
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
