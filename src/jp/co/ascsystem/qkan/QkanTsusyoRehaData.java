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
    private int timePlus[][]= new int[4][];
    private int kiboPlus[][]= new int[4][];
    private int initCode[]= new int[4];
    private int yinitCode[]= new int[4];
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

    private Hashtable tValue = new Hashtable();
    private Hashtable yValue = new Hashtable();
    private Hashtable taUnit = new Hashtable();
    private Hashtable yaUnit = new Hashtable();
    private Hashtable careRate = new Hashtable();
    private Hashtable ratePlus = new Hashtable();

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

        buf.delete(0,buf.length());
        buf.append("select service_unit from m_service_code ");
        buf.append("where service_code_item in (5002,5003,5004,5005,");
        buf.append("5301,5400,5601,5602,5603,5604,5605,5606,5607)");
        buf.append(" and system_service_kind_detail in (11611,16611) ");
        buf.append("order by service_code_item");
        if (dbm.connect()) {
          dbm.execQuery(buf.toString());
          yaUnit.put("1660103",(new int[] {0,0,Integer.parseInt(dbm.getData(0,0).toString())}));
          yaUnit.put("1660104",(new int[] {0,0,Integer.parseInt(dbm.getData(0,1).toString())}));
          yaUnit.put("1660105",(new int[] {0,0,Integer.parseInt(dbm.getData(0,2).toString())}));
          yaUnit.put("1660106",(new int[] {0,0,Integer.parseInt(dbm.getData(0,3).toString())}));
          taUnit.put("1160105",(new int[] {0,0,Integer.parseInt(dbm.getData(0,4).toString())}));
          taUnit.put("1160107",(new int[] {0,0,Integer.parseInt(dbm.getData(0,5).toString())}));
          taUnit.put("1160111",(new int[] {0,0,Integer.parseInt(dbm.getData(0,6).toString())}));
          taUnit.put("1160112",(new int[] {0,0,Integer.parseInt(dbm.getData(0,7).toString()),Integer.parseInt(dbm.getData(0,8).toString()),Integer.parseInt(dbm.getData(0,9).toString())}));
          taUnit.put("1160113",(new int[] {0,0,Integer.parseInt(dbm.getData(0,12).toString())}));
          taUnit.put("1160114",(new int[] {0,0,Integer.parseInt(dbm.getData(0,10).toString())}));
          taUnit.put("1160115",(new int[] {0,0,Integer.parseInt(dbm.getData(0,11).toString())}));
        }
        System.out.println("create test ="+dbm.execUpdate("create table test_t (id integer,test varchar(8))"));
        dbm.Close();
      }
      else Rows=-1;
      careRate.put("1","����"); 
      careRate.put("11","�в�Ū�ײ��");
      careRate.put("12","�׻ٱ�1");
      careRate.put("13","�׻ٱ�2");
      careRate.put("21","�ײ��1");
      careRate.put("22","�ײ��2");
      careRate.put("23","�ײ��3");
      careRate.put("24","�ײ��4");
      careRate.put("25","�ײ��5");
      System.out.println("set ratePlus start");
      ratePlus.put("1", "0"); 
      ratePlus.put("11","0");
      ratePlus.put("12","0");
      ratePlus.put("13","0");
      ratePlus.put("21","1");
      ratePlus.put("22","2");
      ratePlus.put("23","3");
      ratePlus.put("24","4");
      ratePlus.put("25","5");
      System.out.println("set timePlus start");
      timePlus[0] = new int[] {0,0,0,0,0,0,0};
      timePlus[1] = new int[] {0,0,10,20,30,40,50};
      timePlus[2] = new int[] {0,0,10,20,30,40,50};
      timePlus[3] = new int[] {0,0,10,20,30,40,50};
      System.out.println("set kiboPlus start");
      kiboPlus[0] = new int[] {0,0,0,0};
      kiboPlus[1] = new int[] {0,0,1000,2000};
      kiboPlus[2] = new int[] {0,0,200,400};
      kiboPlus[3] = new int[] {0,0,200,400};
      System.out.println("set initCode start");
      initCode = new int[]  {1140,1140,8400,9400};
      yinitCode = new int[]  {1111,1111,8001,9001};
      //tValue.put("1160103",(new String[] {"","�����Ϸ�","�̾﷿","����"}));
      String osn = System.getProperty("os.name").substring(0,3);
      if (osn.equals("Win")) {
        tValue.put("1160104",(new String[] {"","2\uff5e3����","3\uff5e4����","4\uff5e6����","6\uff5e8����","8\uff5e9����","9\uff5e10����"}));
      }
      else {
        tValue.put("1160104",(new String[] {"","2��3����","3��4����","4��6����","6��8����","8��9����","9��10����"}));
      }
      tValue.put("1160105",(new String[] {"","̵��","ͭ��"}));
      tValue.put("1160107",(new String[] {"","̵��","ͭ��"}));
      tValue.put("1160111",(new String[] {"","̵��","ͭ��"}));
      tValue.put("1160112",(new String[] {"","̵��","1�����","1���3�����","3���"}));
      tValue.put("1160113",(new String[] {"","̵��","ͭ��"}));
      tValue.put("1160114",(new String[] {"","̵��","ͭ��"}));
      tValue.put("1160115",(new String[] {"","̵��","ͭ��"}));
      yValue.put("1660103",(new String[] {"","̵��","ͭ��"}));
      yValue.put("1660104",(new String[] {"","̵��","ͭ��"}));
      yValue.put("1660105",(new String[] {"","̵��","ͭ��"}));
      yValue.put("1660106",(new String[] {"","̵��","ͭ��"}));
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
            ym[i] = "ʿ��"+(new Integer(ymdata[i][0]-1988)).toString()+"ǯ"+m+"��";
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
          dbm.connect();
          dbm.execQuery("select provider_area_type from provider where provider_id='"+currentProvider+"'");
          dbm.Close();
          buf.delete(0,buf.length());
          buf.append("select unit_price_value from m_area_unit_price ");
          buf.append("where unit_price_type=");
          buf.append(dbm.getData(0,0).toString());
          buf.append(" and system_service_kind_detail in (11611,16611) ");
          buf.append("order by system_service_kind_detail");
          dbm.connect();
          dbm.execQuery(buf.toString());
          dbm.Close();
          tunitRate = Float.parseFloat(dbm.getData(0,0).toString());
          yunitRate = Float.parseFloat(dbm.getData(0,1).toString());
          System.out.println("trate = "+tunitRate+" yrate = "+yunitRate);

          setTsusyoPanel(ymdata[0][0],ymdata[0][1],targetDay);
          
        } else {
          JLabel nodata = new JLabel("����������ǡ�����ͭ��ޤ���¾�λ��Ƚ꤬��������򤷤ʤ����Ʋ�������");
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
        day[0] = "���";
        ddata = new int[dbm.Rows+1];
        ddata[0] = 0;
        for (int i=0;i<dbm.Rows;i++) {
          String d = dbm.getData(0,i).toString();
          ddata[i+1] = Integer.parseInt(d);
          day[i+1] = d+"��";
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

      if (dbm.connect()) {
        StringBuffer buf = new StringBuffer();

        buf.append("select PATIENT_FIRST_NAME,PATIENT_FAMILY_NAME,");
        buf.append("SERVICE.PATIENT_ID,max(SERVICE_ID) as SID,");
        buf.append("SYSTEM_SERVICE_KIND_DETAIL,max(JOTAI_CODE) as JOTAI_CODE,PATIENT_BIRTHDAY,");
        buf.append("count(SERVICE.SERVICE_ID),INSURE_RATE,");
        buf.append("min(INSURE_VALID_START) as INSURE_VALID_START,");
        buf.append("max(INSURE_VALID_END) as INSURE_VALID_END");
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
        buf.append(" order by SYSTEM_SERVICE_KIND_DETAIL");

        String sql = buf.toString();
        System.out.println(sql);
        dbm.execQuery(sql);
        dbm.Close();
        Vector pdata = new Vector();
        DngDBAccess dbm2 = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
        for (int i=0;i<dbm.Rows;i++){
          int pointCode=0;
          int sCount = Integer.parseInt(dbm.getData("COUNT",i).toString());
          int insRate = Integer.parseInt(dbm.getData("INSURE_RATE",i).toString());
          String insStart = dbm.getData("INSURE_VALID_START",i).toString();
          String insEnd = dbm.getData("INSURE_VALID_END",i).toString();

          Vector pline = new Vector();
          int pNo = new Integer(dbm.getData(2,i).toString()).intValue();
          //pline.addElement(dbm.getData(2,i).toString());
          int sNo = new Integer(dbm.getData(3,i).toString()).intValue();
          //pline.addElement(dbm.getData(3,i).toString());
          String nam1=(dbm.getData(1,i)!=null) ? dbm.getData(1,i).toString()+" ":"";
          String nam2=(dbm.getData(0,i)!=null) ? dbm.getData(0,i).toString():"";
          String nam =nam1+nam2;
          pline.addElement(new Integer(i+1));
          pline.addElement(nam);
          if (dbm.getData("PATIENT_BIRTHDAY",i)!=null) {
            int age =patientAge(dbm.getData("PATIENT_BIRTHDAY",i).toString());
            pline.addElement(new Integer(age));
          } else {
            pline.addElement("");
          }
          int sbp = Integer.parseInt(dbm.getData(4,i).toString());
          String kind = (sbp==11611) ? 
                        "":"ͽ��";
          String cR="1";

          if (targetDay==0) {
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
          int rPlus = Integer.parseInt((String)ratePlus.get(cR));
          buf.delete(0,buf.length());
          buf.append("select SERVICE_ID,SYSTEM_BIND_PATH,");
          buf.append("DETAIL_VALUE");
          buf.append(" from SERVICE_DETAIL_INTEGER_");
          buf.append(detYear);
          buf.append(" where SERVICE_ID=");
          buf.append(sNo);
          buf.append(" and SYSTEM_BIND_PATH");
          buf.append(" in (1160103,1160104,1160105,1160107,1160109,1160110,1160111,1160112,1160113,1160114,1160115,1660101,1660102,1660103,1660104,1660105,1660106)");
          buf.append(" order by SYSTEM_BIND_PATH;");
          sql = buf.toString();
          System.out.println(sql);
          dbm2.connect();
          dbm2.execQuery(sql);
          dbm2.Close();

          System.out.println("dbm2:"+dbm2.Rows);
          int inic=0;
          int hId=0;
          int kiboId=0;
          int timeId=0;
          int kPlus=0;
          int tPlus=0;
          int addUnit=0;
          double unitRate;
          boolean hiwari = false;
          if (sbp==11611) {
            unitRate = tunitRate;
            Hashtable tVal = new Hashtable();
            tVal.put("1160105","̵��");
            tVal.put("1160107","̵��");
            tVal.put("1160111","̵��");
            tVal.put("1160112","̵��");
            tVal.put("1160113","̵��");
            tVal.put("1160114","̵��");
            tVal.put("1160115","̵��");
            for (int j=0;j<dbm2.Rows;j++){
              System.out.println("Rows start: "+j);
              int sbp0 = Integer.parseInt(dbm2.getData("SYSTEM_BIND_PATH",j).toString());
              if (sbp0==1160103) {
                kiboId = Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                System.out.println("kiboId : "+kiboId+"("+sbp0+")");
              } 
              else if (sbp0==1160109) {
                hId = Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                inic= initCode[hId];
                kPlus = kiboPlus[hId][kiboId];
                tPlus = timePlus[hId][timeId];
              } 
              else if (sbp0==1160110) {
                if (dbm2.getData("DETAIL_VALUE",j).toString().equals("2")) kPlus = kPlus+100;
              }
              else {
                System.out.println("num"+sbp0+"num");
                String[] val;
                int[] add;
                int key;
                if (sbp0==1160104) {
                  timeId = Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                  val = (String[])tValue.get("1160104"); 
                  key = Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                  pline.addElement(val[key]);
                }
                else {
                  val = (String[])tValue.get(dbm2.getData("SYSTEM_BIND_PATH",j).toString());
                  add = (int[]) taUnit.get(dbm2.getData("SYSTEM_BIND_PATH",j).toString());
                  key = Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                  System.out.println("sbp = "+sbp0+" key = "+key+" add= "+add[key]);
                  addUnit += add[key];
                }
                tVal.put(dbm2.getData("SYSTEM_BIND_PATH",j).toString(),val[key]);
              }
            }
            pline.addElement((String)tVal.get("1160105"));
            pline.addElement((String)tVal.get("1160111"));
            pline.addElement((String)tVal.get("1160112"));
            pline.addElement((String)tVal.get("1160107"));
            pline.addElement((String)tVal.get("1160113"));
            pline.addElement((String)tVal.get("1160114"));
            pline.addElement((String)tVal.get("1160115"));
            pline.addElement("");
            pline.addElement("");
            System.out.println("inic : "+inic+"+"+rPlus+"+"+kPlus+"+"+tPlus);
            pointCode = inic+rPlus+tPlus+kPlus;
            System.out.println("pointCode : "+pointCode);
          }
          else {
            unitRate = yunitRate;
            for (int j=0;j<6;j++) pline.addElement("");
            Hashtable yoVal = new Hashtable();
            yoVal.put("1660103","̵��");
            yoVal.put("1660104","̵��");
            yoVal.put("1660105","̵��");
            yoVal.put("1660106","̵��");
            for (int j=0;j<dbm2.Rows;j++) {
              int sbp0 = Integer.parseInt(dbm2.getData("SYSTEM_BIND_PATH",j).toString());
              if (sbp0==1660101) {
                hId = Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                inic= yinitCode[hId];
                if (cR.equals("13")) inic +=10;
              } 
              else if (sbp0==1660102) {
                if(Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString())==2) {
                   inic = inic+1;
                   hiwari = true;
                }
              } 
              else {

                String[] val = (String[])yValue.get(dbm2.getData("SYSTEM_BIND_PATH",j).toString());
                int[] add = (int[]) yaUnit.get(dbm2.getData("SYSTEM_BIND_PATH",j).toString());
                int key = Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                yoVal.put(dbm2.getData("SYSTEM_BIND_PATH",j).toString(),val[key]);
                if (!hiwari) addUnit += add[key];
              }
            }
            pline.addElement((String)yoVal.get("1660104"));
            pline.addElement((String)yoVal.get("1660105"));
            pline.addElement((String)yoVal.get("1660103"));
            pline.addElement((String)yoVal.get("1660106"));
            pointCode = inic;
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
              buf.append(" and SYSTEM_BIND_PATH");
              buf.append(" in (701008,701014,701015,701016,701017)");
              buf.append(" order by SYSTEM_BIND_PATH;");
              sql = buf.toString();
              System.out.println(sql);
              dbm2.connect();
              dbm2.execQuery(sql);
              dbm2.Close();
              cRows = dbm2.Rows;
            }
            else cRows=0;
            if (cRows>0) {
              int other=0;
              if (!hiwari) pline.addElement(new Integer(dbm2.getData("DETAIL_VALUE",0).toString()));
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
                  if (dbm3.getData(0,0)!=null);
                    pline.addElement(new Integer(dbm3.getData(0,0).toString()));
                }
              }
              int hiyou = (int)(Float.parseFloat(dbm2.getData("DETAIL_VALUE",1).toString())*Float.parseFloat(dbm2.getData("DETAIL_VALUE",2).toString()));
              int futan = Integer.parseInt(dbm2.getData("DETAIL_VALUE",4).toString());
            
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
              buf.append("')");
              System.out.println(buf.toString());
              dbm2.connect();
              dbm2.execQuery(buf.toString());
              dbm2.Close();
              if (dbm2.Rows>0) {
                 for (int j=1;j<16;j=j+2) {
                   if (dbm2.getData(j,0)!=null) 
                     other += Integer.parseInt(dbm2.getData(j+1,0).toString());
                 }
              }

              pline.addElement(new Integer(hiyou));
              pline.addElement(new Integer(futan));
              pline.addElement(new Integer(other));
              pline.addElement(new Integer(other+futan));
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
                  if (dbm3.getData(0,0)!=null)
                    pline.addElement(new Integer(dbm3.getData(0,0).toString()));
                  else
                    pline.addElement(new Integer(sCount));
                }
                else {
                  pline.addElement(new String("-"));
                  pline.addElement(new String("�����Բ�"));
                }
              }
            }
          }
          else if ( sbp==11611 && (cR.equals("12") || cR.equals("13"))) {
              int hiyou =(int)((double) addUnit * unitRate);
              int futan = hiyou - (int)((double)hiyou/100.0*(double)insRate);
              //if (hiyou%10>0) futan +=1;
              System.out.println("add = "+addUnit+" hiyou = "+hiyou+" futan = "+futan);
              pline.addElement(new Integer(hiyou));
              pline.addElement(new Integer(futan));
          } 
          else {
            buf.delete(0,buf.length());
            buf.append("select service_unit from m_service_code ");
            buf.append("where system_service_kind_detail='");
            buf.append(sbp);
            buf.append("' and  service_code_item='");
            buf.append(pointCode);
            buf.append("'");
            dbm2.connect();
            System.out.println(buf.toString());
            dbm2.execQuery(buf.toString());
            dbm2.Close();
            if (dbm2.Rows>0) {
              int p = Integer.parseInt(dbm2.getData(0,0).toString());
              p += addUnit;
              int hiyou =(int)((double) p * unitRate);
              int futan = hiyou - (int)((double)hiyou/100.0*(double)insRate);
              //if (hiyou%10>0) futan +=1;
              System.out.println("p = "+p+" add = "+addUnit+" hiyou = "+hiyou+" futan = "+futan);
              pline.addElement(new Integer(hiyou));
              pline.addElement(new Integer(futan));
            }
          }
          pdata.addElement(pline);
        }
        scp = getScrollList(pdata,targetDay);
        pn2.add(scp);
      }
      if (dbm.Rows>0) {
        if (targetDay>0) {
          JLabel lab1 = new JLabel("����ñ�̤Ǥζ�ۤˤĤ��ơ���ô��ۤ�ü�������δط��Ƿ�ֶ�ۤȤϰۤʤ��礬����ޤ��� ");
          lab1.setFont(new Font("Dialog",Font.PLAIN,11));
          JLabel lab2 = new JLabel("��������������������������ͽ�ɥ����ӥ��ξ��Ϸ�֤ζ�ۤ�ɽ��������ξ��ϴ���ñ�̿��Τߤζ�ۤ�ɽ�����Ƥ��ޤ���");
          lab2.setFont(new Font("Dialog",Font.PLAIN,11));
          pnl.add(lab1,BorderLayout.NORTH);
          pnl.add(lab2,BorderLayout.CENTER);
        } else {
          JLabel lab1 = new JLabel("����֤Ǥζ�ۤˤĤ��ơ����ӳ���ʬ�Τ�ɽ������ޤ��� ");
          lab1.setFont(new Font("Dialog",Font.PLAIN,11));
          JLabel lab2 = new JLabel("����������ײ���٤��Ѥ�äƤ����硢�ѹ�����ײ���٤�ɽ������ޤ����ޤ����׻ٱ碫���ײ��ξ����̹ԤȤʤ�ޤ���");
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
      //fieldName.addElement("�����ӥ�ID");
      fieldName.addElement("No.");
      fieldName.addElement("��̾");
      fieldName.addElement("ǯ��");
      fieldName.addElement("�ײ����");
      fieldName.addElement("����");
      if (td>0) {
        fieldName.addElement("���ϻ���");
        fieldName.addElement("��λ����");
      } 
      fieldName.addElement("���ֶ�ʬ");
      fieldName.addElement("����");
      fieldName.addElement("���");
      fieldName.addElement("û��");
      fieldName.addElement("��Ƴ");
      fieldName.addElement("��ǯ");
      fieldName.addElement("����");
      fieldName.addElement("����");
      fieldName.addElement("��ư");
      fieldName.addElement("ɾ��");
      if (td==0) {
        fieldName.addElement("���");
      }
      fieldName.addElement("����");
      fieldName.addElement("��ô��");
      if (td==0) {
        fieldName.addElement("����¾��ô��");
        fieldName.addElement("��ô�۹��");
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
      if (td==0) {
        sorter.setColumnClass(15,Integer.class);
        sorter.setColumnClass(16,Integer.class);
        sorter.setColumnClass(17,Integer.class);
        sorter.setColumnClass(18,Integer.class);
        sorter.setColumnClass(19,Integer.class);
      } else {
        sorter.setColumnClass(17,Integer.class);
        sorter.setColumnClass(18,Integer.class);
      }
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
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(65);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(80);
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
      System.out.println("cid : "+cid);
      if (td==0) {
        usrTbl.getColumnModel().getColumn(cid).setCellRenderer(ren);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      }
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
      }
      //usrTbl.getTableHeader().setReorderingAllowed(false);
      JScrollPane scrPane = new JScrollPane();
      scrPane.getViewport().setView(usrTbl);
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
      if (targetDay==0) {
        num=20;
      } else {
        num=19;
      }
      float width[] = new float[num];
      int ctype[] = new int[num];
      Arrays.fill(ctype,0);
      ctype[cid] = 2; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = 3; //No.
      width[cid++] = 14; //��̾
      ctype[cid] = 2; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.5"); //ǯ��
      width[cid++] = Float.parseFloat("6.0"); //�ײ����
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = 4; //����
      if (targetDay>0) {
        ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
        width[cid++] = 6; //���ϻ���
        ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
        width[cid++] = 6; //��λ����
      } 
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("6.5"); //���ֶ�ʬ
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.5"); //����
      width[cid++] = Float.parseFloat("3.5"); //���
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("8.0"); //û��
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.5"); //ˬ���Ƴ
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.5"); //��ǯ
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.5"); //����
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.5"); //����
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.5"); //��ư
      ctype[cid] = 7; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.5"); //ɾ��
      if (targetDay==0) {
        ctype[cid] = 2; // 0 - normal 1 - add comma 2 - align right
        width[cid++] = Float.parseFloat("3.5"); //���
      }
      ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = 6; //����
      ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = 5; //��ô��
      if (targetDay==0) {
        ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
        width[cid++] = 8; //����¾��ô��
        ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
        width[cid++] = Float.parseFloat("7.5"); //��ô�۹��
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
        sb.append("ǯ");
        if (targetMonth<10) sb.append("0");
        sb.append(targetMonth);
        sb.append("��");
        if (targetDay>0) {
          if (targetDay<10) sb.append("0");
          sb.append(targetDay);
          sb.append("��");
        } 
        sb.append(" ��ʬ");
        pdf.setSubTitle(sb.toString());
      if (pdf.openPDF("�̽��Ͼ���")) {
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