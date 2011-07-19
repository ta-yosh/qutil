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

public class QkanHouKanData {

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
    private double spRate;
    private double smRate;
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
    private boolean spArea=false;
    private boolean smProv1=false;
    private boolean smProv2=false;
    private DefaultTableModel dtm;
    private TableSorter2 sorter;

    private Hashtable tValue = new Hashtable();
    private Hashtable yValue = new Hashtable();
    private Hashtable taUnit = new Hashtable();
    private Hashtable yaUnit = new Hashtable();
    private Hashtable careRate = new Hashtable();
    private Hashtable ratePlus = new Hashtable();
    private Hashtable firstDate= new Hashtable();

    public QkanHouKanData(String dbUri,String dbUser,String dbPass) {
      this.dbUri = dbUri;
      this.dbUser = dbUser;
      this.dbPass = dbPass;
      dbm = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
      StringBuffer buf = new StringBuffer();
      buf.append("select PROVIDER_ID,PROVIDER_NAME,SPECIAL_AREA_FLAG from PROVIDER ");
      buf.append("where PROVIDER_ID in (");
      buf.append("   select PROVIDER_ID from PROVIDER_SERVICE");
      buf.append("    where SYSTEM_SERVICE_KIND_DETAIL in (11311,16311)");
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
      careRate.put("1","����"); 
      careRate.put("11","�в�Ū�ײ��");
      careRate.put("12","�׻ٱ�1");
      careRate.put("13","�׻ٱ�2");
      careRate.put("21","�ײ��1");
      careRate.put("22","�ײ��2");
      careRate.put("23","�ײ��3");
      careRate.put("24","�ײ��4");
      careRate.put("25","�ײ��5");
    }

    public void setUnit(String dat) {
      String osn = System.getProperty("os.name").substring(0,3);
      String tilde;  
      if (osn.equals("Win")) tilde = "\uff5e";
      else tilde = "��";

      tValue.put("1130103",(new String[] {"","����","�±�/���Ž�"}));
      tValue.put("1130104",(new String[] {"","������","���","PT,OT,ST"}));
      tValue.put("1130105",(new String[] {"","20ʬ̤��","30ʬ̤��","0.5"+tilde+"1����","1"+tilde+"1.5����"}));
      tValue.put("1130106",(new String[] {"","�̾�","��ī","���","����"}));
      tValue.put("1130108",(new String[] {"","̵��","ͭ��"}));
      tValue.put("1130109",(new String[] {"","̵��","ͭ��"}));
      tValue.put("1130110",(new String[] {"","̵��","ͭ��"}));
      tValue.put("1130111",(new String[] {"","20ʬ̤��","30ʬ̤��","0.5"+tilde+"1����","1"+tilde+"1.5����"}));
      tValue.put("1130112",(new String[] {"","1��","2��"}));
      tValue.put("1130113",(new String[] {"","̵��","ͭ��"}));
      tValue.put("1130114",(new String[] {"","̵��","ͭ��"}));
      tValue.put("1130115",(new String[] {"","30ʬ̤��","30ʬ�ʾ�"}));
      tValue.put("12",(new String[] {"","̵��","ͭ��"}));
      yValue.put("1630101",(new String[] {"","����","�±�/���Ž�"}));
      yValue.put("1630102",(new String[] {"","������","���","PT,OT,ST"}));
      yValue.put("1630103",(new String[] {"","20ʬ̤��","30ʬ̤��","0.5"+tilde+"1����","1"+tilde+"1.5����"}));
      yValue.put("1630104",(new String[] {"","�̾�","��ī","���","����"}));
      yValue.put("1630105",(new String[] {"","̵��","ͭ��"}));
      yValue.put("1630106",(new String[] {"","̵��","ͭ��"}));
      yValue.put("1630107",(new String[] {"","20ʬ̤��","30ʬ̤��","0.5"+tilde+"1����","1"+tilde+"1.5����"}));
      yValue.put("1630108",(new String[] {"","1��","2��"}));
      yValue.put("1630109",(new String[] {"","̵��","ͭ��"}));
      yValue.put("1630110",(new String[] {"","̵��","ͭ��"}));
      yValue.put("1630111",(new String[] {"","30ʬ̤��","30ʬ�ʾ�"}));
      yValue.put("12",(new String[] {"","̵��","ͭ��"}));
      StringBuffer buf = new StringBuffer();
      buf.append("select service_code_item,service_unit,system_service_kind_detail from m_service_code ");
      buf.append("where service_code_item in (8000,8100,8110,3100,3200,4000,7000,6101)");
      buf.append(" and system_service_kind_detail in (11311,16311) ");
      buf.append(" and SUBSTRING(system_service_code_item from 1 for 1)='Z'");
      buf.append(" and service_valid_start<='");
      buf.append(dat);
      buf.append("' and service_valid_end>='");
      buf.append(dat);
      buf.append("' order by system_service_kind_detail,service_code_item");
      System.out.println(buf.toString());
      if (dbm.connect()) {
        dbm.execQuery(buf.toString());
        dbm.Close();
        if (targetYear<2009 || targetYear==2009 && targetMonth<4) {
          taUnit.put("1130108",(new int[] {0,0,Integer.parseInt(dbm.getData(1,0).toString()),0,Integer.parseInt(dbm.getData(1,1).toString())})); //�۵޻��û�
          taUnit.put("1130109",(new int[] {0,0,Integer.parseInt(dbm.getData(1,2).toString())})); //���̴���
          taUnit.put("1130110",(new int[] {0,0,Integer.parseInt(dbm.getData(1,3).toString())})); //�����ߥʥ륱��
          taUnit.put("SPECIAL",dbm.getData(1,4)); //�����ϰ�(��)
          yaUnit.put("1630105",(new int[] {0,0,Integer.parseInt(dbm.getData(1,5).toString()),0,Integer.parseInt(dbm.getData(1,6).toString())})); //�۵޻��û�
          yaUnit.put("1630106",(new int[] {0,0,Integer.parseInt(dbm.getData(1,7).toString())})); //���̴���
          yaUnit.put("SPECIAL",dbm.getData(1,8)); //�����ϰ�(��)
        } else if (targetYear>2009 || targetYear==2009 && targetMonth>=4) {
          taUnit.put("1130108",(new int[] {0,0,Integer.parseInt(dbm.getData(1,0).toString()),0,Integer.parseInt(dbm.getData(1,1).toString())})); //�۵޻��û�
          taUnit.put("1130109",(new int[] {0,0,Integer.parseInt(dbm.getData(1,2).toString())})); //���̴���
          taUnit.put("1130110",(new int[] {0,0,Integer.parseInt(dbm.getData(1,4).toString())})); //�����ߥʥ륱��
          taUnit.put("1130113",(new int[] {0,0,Integer.parseInt(dbm.getData(1,3).toString())})); //�����ӥ���������
          taUnit.put("SPECIAL",dbm.getData(1,5)); //�����ϰ�(��)
          taUnit.put("SMALL",dbm.getData(1,6));  //������(��)
          taUnit.put("12",(new int[] {0,0,Integer.parseInt(dbm.getData(1,7).toString())}));
          yaUnit.put("1630105",(new int[] {0,0,Integer.parseInt(dbm.getData(1,8).toString()),0,Integer.parseInt(dbm.getData(1,9).toString())})); //�۵�
          yaUnit.put("1630106",(new int[] {0,0,Integer.parseInt(dbm.getData(1,10).toString())})); //���̴���
          yaUnit.put("1630109",(new int[] {0,0,Integer.parseInt(dbm.getData(1,11).toString())})); //�����ӥ���������
          yaUnit.put("SPECIAL",dbm.getData(1,12)); //�����ϰ�(��)
          yaUnit.put("SMALL",dbm.getData(1,13));  //������(��)
          yaUnit.put("12",(new int[] {0,0,Integer.parseInt(dbm.getData(1,14).toString())}));
        }


        buf.delete(0,buf.length());
        buf.append("select provider_id,system_service_kind_detail,");
        buf.append("system_bind_path,detail_value from ");
        buf.append("PROVIDER_SERVICE_DETAIL_INTEGER,PROVIDER_SERVICE ");
        buf.append("where provider_service.provider_service_id = ");
        buf.append("provider_service_detail_integer.provider_service_id ");
        buf.append("and provider_id='");
        buf.append(currentProvider);
        buf.append("' and system_service_kind_detail in ('11311','16311') ");
        buf.append("and system_bind_path in (2,3) ");
        buf.append("order by system_service_kind_detail");
        dbm.connect();
        dbm.execQuery(buf.toString());
        dbm.Close();
        System.out.println(buf.toString());
        int mountFlg[] = new int[] {0,0,0,0};  
        if (dbm.Rows==2) {
          if (Integer.parseInt(dbm.getData(1,0).toString())==11311) {
            if (Integer.parseInt(dbm.getData(3,0).toString())==2 && 
                Integer.parseInt(dbm.getData(3,1).toString())==2)
              smProv1 = true; 
          } else {
            if (Integer.parseInt(dbm.getData(3,0).toString())==2 && 
                Integer.parseInt(dbm.getData(3,1).toString())==2)
              smProv2 = true; 
          }
        }
        if (dbm.Rows==4) {
          if (Integer.parseInt(dbm.getData(3,0).toString())==2 && 
              Integer.parseInt(dbm.getData(3,1).toString())==2)
             smProv1 = true; 
          if (Integer.parseInt(dbm.getData(3,2).toString())==2 && 
              Integer.parseInt(dbm.getData(3,3).toString())==2)
             smProv2 = true; 
        }
        System.out.println(dbm.Rows+" Small1 = "+smProv1+"Small2 = "+smProv2);
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
        buf.append("' and SYSTEM_SERVICE_KIND_DETAIL in (11311,16311) ");
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
              setHouKanPanel(targetYear,targetMonth,targetDay);
            }
          };
          ymbox.addActionListener(ymChange);
          pn1.add(ymbox);
          dayCondition();
          pn1.add(pn3);

          setHouKanPanel(ymdata[0][0],ymdata[0][1],targetDay);
          
        } else {
          JLabel nodata = new JLabel("����������ǡ�����ͭ��ޤ���¾�λ��Ƚ꤬��������򤷤ʤ����Ʋ�������");
          nodata.setFont(new Font("Dialog",Font.PLAIN,12));
          pn1.add(nodata);
          setHouKanPanel(0,0,0);
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
      //buf.append("' and SYSTEM_SERVICE_KIND_DETAIL in (11311,16311) ");
      buf.append("' and ((SYSTEM_SERVICE_KIND_DETAIL=11311 and ");
      buf.append("substring(JOTAI_CODE from 1 for 1)=2) or ");
      buf.append("(SYSTEM_SERVICE_KIND_DETAIL=16311 and ");
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
            setHouKanPanel(targetYear,targetMonth,targetDay);
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
      buf.append(" and system_service_kind_detail in (11311,16311) ");
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
  
    public void setHouKanPanel(int targetYear,int targetMonth,int targetDay) {
      Hashtable firsted= new Hashtable();
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
      if (targetYear>2009 || targetYear==2009 && targetMonth>=4) kaisei=20090401;
      if (dbm.connect()) {
        StringBuffer buf = new StringBuffer();

        buf.append("select PATIENT_FIRST_NAME,PATIENT_FAMILY_NAME,");
        buf.append("SERVICE.PATIENT_ID,SERVICE.SERVICE_ID as SID,");
        buf.append("SYSTEM_SERVICE_KIND_DETAIL,max(JOTAI_CODE) as JOTAI_CODE,PATIENT_BIRTHDAY,");
        buf.append("count(SERVICE.SERVICE_ID),INSURE_RATE,");
        buf.append("min(INSURE_VALID_START) as INSURE_VALID_START,");
        buf.append("max(INSURE_VALID_END) as INSURE_VALID_END,");
        buf.append("min(extract(DAY from SERVICE_DATE)) as FIRST_DAY");
        buf.append(",SERVICE_USE_TYPE");
        buf.append(",substring(SERVICE.LAST_TIME from 1 for 16) as LAST,");
        buf.append("SERVICE_DATE,");
        buf.append("SERVICE_DETAIL_DATE_");
        buf.append(detYear+".DETAIL_VALUE as START");
        buf.append(" from SERVICE ");
        buf.append(" inner join SERVICE_DETAIL_DATE_"+detYear+" on ");
        buf.append("SERVICE.SERVICE_ID=SERVICE_DETAIL_DATE_"+detYear);
        buf.append(".SERVICE_ID and SYSTEM_BIND_PATH=3");
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
        }
        if (targetDay==0) {
          buf.append("' and INSURE_VALID_START<'");
          buf.append(nEnd);
        }

        buf.append("')) where ((SYSTEM_SERVICE_KIND_DETAIL=11311 and ");
        buf.append("substring(JOTAI_CODE from 1 for 1)=2) or ");
        buf.append("(SYSTEM_SERVICE_KIND_DETAIL=16311 and ");
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
        buf.append(",START,SERVICE_USE_TYPE,SERVICE_DATE");
        buf.append(" order by SYSTEM_SERVICE_KIND_DETAIL,SERVICE.PATIENT_ID");
        buf.append(",LAST desc,SERVICE_DATE asc,START asc");

        String sql = buf.toString();
        System.out.println(sql);
        dbm.execQuery(sql);
        dbm.Close();
        Vector pdata = new Vector();

        DngDBAccess dbm2 = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
        int pNo=-1;
        int uTp=-1;
        int ln = 0;
        String sids = "";
        boolean monfin = false;

        for (int i=0;i<dbm.Rows;i++){
          int lastP = pNo;
          pNo = Integer.parseInt(dbm.getData(2,i).toString());
          if (pNo!=lastP) {
            uTp = Integer.parseInt(dbm.getData("SERVICE_USE_TYPE",i).toString());
            if (targetDay==0) {
              firstDate.put(dbm.getData("PATIENT_ID",i).toString(),dbm.getData("FIRST_DAY",i).toString());
              if (!monfin && lastP != -1) {
                pNo = lastP;
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
          double mountRate=0;
          int sCount = Integer.parseInt(dbm.getData("COUNT",i).toString());
          int insRate = Integer.parseInt(dbm.getData("INSURE_RATE",i).toString());
          String insStart = dbm.getData("INSURE_VALID_START",i).toString();
          String insEnd = dbm.getData("INSURE_VALID_END",i).toString();

          Vector pline = new Vector();
          if (!firsted.containsKey(new Integer(pNo)))
            firsted.put(new Integer(pNo), new Vector());

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
          int sbp = Integer.parseInt(dbm.getData(4,i).toString());
          String kind = (sbp==11311) ? 
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
          buf.append(" and SYSTEM_BIND_PATH");
          buf.append(" in (12,14,1130103,1130104,1130105,1130106,1130107,1130108,1130109,1130110,1130111,1130112,1130113,1130114,1130115,1630101,1630102,1630103,1630104,1630105,1630106,1630107,1630108,1630109,1630110,1630111)");
          if (targetDay==0)
            buf.append(" group by SYSTEM_BIND_PATH ");
          buf.append(" order by SYSTEM_BIND_PATH;");
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
          int skubun=0;
          String ItemCode="";
          double unitRate;
          boolean smProv = false;
          int ssc = (kaisei!=0)? 7:4;
          String[] ssCode = (kaisei!=0) ?
                              new String[] {"0","0","1","0","0","0","0"}:
                              new String[] {"0","0","0","0"};
          if (sbp==11311) {
            smProv = smProv1;
            unitRate = tunitRate;
            Hashtable tVal = new Hashtable();
            tVal.put("12","̵��");
            tVal.put("1130103","");
            tVal.put("1130104","");
            tVal.put("1130105","");
            tVal.put("1130106","");
            tVal.put("1130108","̵��");
            tVal.put("1130109","̵��");
            tVal.put("1130110","̵��");
            tVal.put("1130111","");
            tVal.put("1130112","");
            tVal.put("1130113","̵��");
            tVal.put("1130114","̵��");
            tVal.put("1130115","");
            int ssCount = 0; 
            for (int j=0;j<dbm2.Rows;j++){
              System.out.println("Rows start: "+j);
              int sbp0 = Integer.parseInt(dbm2.getData("SYSTEM_BIND_PATH",j).toString());
              if (sbp0==14) {
                kaisei=Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                System.out.println("kaisei: "+kaisei);
              }
              else if (sbp0==12 || sbp0>=1130108 && sbp0<=1130110 || sbp0==1130113 ) {
                int key = Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                String[] val = (String[])tValue.get(Integer.toString(sbp0)); 
                //pline.addElement(val[key]);
                tVal.put(Integer.toString(sbp0),val[key]);
                int[] add = (int[]) taUnit.get(dbm2.getData("SYSTEM_BIND_PATH",j).toString());
                if (sbp0==12) mountRate = (double)add[key]/100.0; 
                else if (sbp0==1130113) addUnit += add[key];
                else {
                  if (targetDay==Integer.parseInt(firstDate.get(Integer.toString(
pNo)).toString()) && !((Vector)firsted.get(new Integer(pNo))).contains(new Integer(sbp0))) {
                    if (sbp0==1130108) 
                       addUnit += (skubun==1) ? add[key]:add[key+2];
                    else addUnit += add[key];
                    if (add[key]>0) ((Vector)firsted.get(new Integer(pNo))).addElement(new Integer(sbp0));
                  }
                }
                System.out.println("sbp = "+sbp0+" key = "+key+" add= "+addUnit);
              }
              else {
                int key = Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                String[] val = (String[])tValue.get(Integer.toString(sbp0)); 
                //pline.addElement(val[key]);
                tVal.put(Integer.toString(sbp0),val[key]);
                switch (sbp0) {
                  case 1130103: skubun = key; 
                           ssCode[0]= (new Integer(key)).toString();
                           break;
                  case 1130105: 
                          ssCode[1]= (new Integer(key)).toString();
                           break;
                  case 1130111: 
                          ssCode[1]= (new Integer(key)).toString();
                           break;
                  case 1130114: ssCode[2]= (new Integer(key)).toString();
                           break;
                  case 1130104: if (key==3) ssCode[0] = "0";
                           if (kaisei!=0)
                             ssCode[3]= (new Integer(key)).toString();
                           else
                             ssCode[2]= (new Integer(key)).toString();
                           break;
                  case 1130106: ssCode[(kaisei!=0) ? 4:3] = 
                              (key>2)? (new Integer(key-1)).toString()
                                      :(new Integer(key)).toString(); 
                           break;
                  case 1130112: ssCode[5]= (new Integer(key)).toString();
                           break;
                  case 1130115: ssCode[6]= (new Integer(key)).toString();
                           break;
                }
              }
              System.out.println("num"+sbp0+"num");
            }
            pline.addElement((String)tVal.get("1130103"));
            pline.addElement((String)tVal.get("1130104"));
            pline.addElement((String)tVal.get((kaisei!=0)? "1130111":"1130105"));
            pline.addElement((String)tVal.get("1130106"));
            pline.addElement((String)tVal.get("1130108"));
            pline.addElement((String)tVal.get("1130109"));
            pline.addElement((String)tVal.get("1130110"));
            if (kaisei!=0) {
              pline.addElement((String)tVal.get("1130112"));
              pline.addElement((String)tVal.get("1130115"));
              pline.addElement((String)tVal.get("1130113"));
              pline.addElement((String)tVal.get("1130114"));
              pline.addElement((String)tVal.get("12"));
            }
            for (int ii=0;ii<ssc;ii++) ItemCode += ssCode[ii];
            System.out.println("ItemtCode : "+ItemCode);
            spRate = (double)Integer.parseInt(taUnit.get("SPECIAL").toString())/100.0; 
            if (kaisei!=0) smRate = (double)Integer.parseInt(taUnit.get("SMALL").toString())/100.0; 
          }
          else {
            smProv = smProv2;
            unitRate = yunitRate;
            Hashtable yoVal = new Hashtable();
            yoVal.put("12","̵��");
            yoVal.put("1630101","");
            yoVal.put("1630102","");
            yoVal.put("1630103","");
            yoVal.put("1630104","");
            yoVal.put("1630105","̵��");
            yoVal.put("1630106","̵��");
            yoVal.put("1630107","");
            yoVal.put("1630108","");
            yoVal.put("1630109","̵��");
            yoVal.put("1630110","̵��");
            yoVal.put("1630111","");
            int ssCount = 0; 
            for (int j=0;j<dbm2.Rows;j++) {
              System.out.println("Rows start: "+j);
              int sbp0 = Integer.parseInt(dbm2.getData("SYSTEM_BIND_PATH",j).toString());
              if (sbp0==14) {
                kaisei=Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                System.out.println("kaisei: "+kaisei);
              }
              else if (sbp0==12 || sbp0==1630105 || sbp0==163106 || sbp0==1630109 ) {
                int key = Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                String[] val = (String[])yValue.get(Integer.toString(sbp0)); 
                //pline.addElement(val[key]);
                yoVal.put(Integer.toString(sbp0),val[key]);
                int[] add = (int[]) yaUnit.get(dbm2.getData("SYSTEM_BIND_PATH",j).toString());
                System.out.println("sbp = "+sbp0+" key = "+key+" add= "+add[key]);
                if (sbp0==12) mountRate = (double)add[key]/100.0; 
                else if (sbp0==1630109) addUnit += add[key];
                else {
                  if (targetDay==Integer.parseInt(firstDate.get(Integer.toString(
pNo)).toString()) && !((Vector)firsted.get(new Integer(pNo))).contains(new Integer(sbp0))) {
                    if (sbp0==1630105) 
                       addUnit += (skubun==1) ? add[key]:add[key+2];
                    else addUnit += add[key];
                    if (add[key]>0) ((Vector)firsted.get(new Integer(pNo))).addElement(new Integer(sbp0));
                  }
                }
              }
              else {
                int key = Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                String[] val = (String[])yValue.get(Integer.toString(sbp0)); 
                //pline.addElement(val[key]);
                yoVal.put(Integer.toString(sbp0),val[key]);
                switch (sbp0) {
                  case 1630101: skubun = key; 
                           ssCode[0]= (new Integer(key)).toString();
                           break;
                  case 1630103: ssCode[1]= (new Integer(key)).toString();
                           break;
                  case 1630107: ssCode[1]= (new Integer(key)).toString();
                           break;
                  case 1630110: ssCode[2]= (new Integer(key)).toString();
                           break;
                  case 1630102: if (key==3) ssCode[0] = "0";
                           if (kaisei!=0)
                             ssCode[3]= (new Integer(key)).toString();
                           else
                             ssCode[2]= (new Integer(key)).toString();
                           break;
                  case 1630104: ssCode[(kaisei!=0) ? 4:3] = 
                              (key>2)? (new Integer(key-2)).toString()
                                      : (key==1)?"3":"1"; 
                           break;
                  case 1630108: ssCode[5]= (new Integer(key)).toString();
                           break;
                  case 1630111: ssCode[6]= (new Integer(key)).toString();
                           break;
                }
              }
              System.out.println("num"+sbp0+"num");
            }
            pline.addElement((String)yoVal.get("1630101"));
            pline.addElement((String)yoVal.get("1630102"));
            pline.addElement((String)yoVal.get((kaisei!=0)? "1630107":"1630103"));
            pline.addElement((String)yoVal.get("1630104"));
            pline.addElement((String)yoVal.get("1630105"));
            pline.addElement((String)yoVal.get("1630106"));
            pline.addElement("");
            if (kaisei!=0) {
              pline.addElement((String)yoVal.get("1630108"));
              pline.addElement((String)yoVal.get("1630111"));
              pline.addElement((String)yoVal.get("1630109"));
              pline.addElement((String)yoVal.get("1630110"));
              pline.addElement((String)yoVal.get("12"));
            }
            for (int ii=0;ii<ssc;ii++) ItemCode += ssCode[ii];
            System.out.println("ItemtCode : "+ItemCode);
            spRate = (double)Integer.parseInt(yaUnit.get("SPECIAL").toString())/100.0; 
            if (kaisei!=0) smRate = (double)Integer.parseInt(yaUnit.get("SMALL").toString())/100.0; 
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
              if (sbp==11311) buf.append("and DETAIL_VALUE='13')");
              else buf.append("and DETAIL_VALUE='63')");
              //buf.append(" and SYSTEM_BIND_PATH");
              //buf.append(" in (701008,701014,701015,701016,701017)");
              buf.append(" and (SYSTEM_BIND_PATH=701008 or ");
              buf.append("SYSTEM_BIND_PATH>=701014 and ");
              buf.append("SYSTEM_BIND_PATH<=701026)");
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
                buf.append(" and INSURE_VALID_START<=SERVICE.SERVICE_DATE");
                buf.append(")) where SERVICE.PATIENT_ID=");
                buf.append(pNo);
                buf.append(" and ((SYSTEM_SERVICE_KIND_DETAIL=11311 and ");
                buf.append("substring(JOTAI_CODE from 1 for 1)=2) or ");
                buf.append("(SYSTEM_SERVICE_KIND_DETAIL=16311 and ");
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
            }
            if (cRows>0) {
              int other=0;
              int clid = Integer.parseInt(dbm2.getData("CLAIM_ID",0).toString());
              if (dbm3.Rows>0 && dbm3.getData(0,0)!=null)
                pline.addElement(new Integer(dbm3.getData(0,0).toString()));
              else
                pline.addElement(new Integer(dbm2.getData("DETAIL_VALUE",0).toString()));
              int hiyou = (int)(Float.parseFloat(dbm2.getData("DETAIL_VALUE",1).toString())*Float.parseFloat(dbm2.getData("DETAIL_VALUE",2).toString()));
              int futan = Integer.parseInt(dbm2.getData("DETAIL_VALUE",4).toString());

              int kouhiunit,kouhi,jikouhi;
              if (cRows>5) {
                kouhiunit = Integer.parseInt(dbm2.getData("DETAIL_VALUE",5).toString())
                       +Integer.parseInt(dbm2.getData("DETAIL_VALUE",8).toString())
                       +Integer.parseInt(dbm2.getData("DETAIL_VALUE",11).toString());
                kouhi = Integer.parseInt(dbm2.getData("DETAIL_VALUE",6).toString())
                       +Integer.parseInt(dbm2.getData("DETAIL_VALUE",9).toString())
                       +Integer.parseInt(dbm2.getData("DETAIL_VALUE",12).toString());
                jikouhi = Integer.parseInt(dbm2.getData("DETAIL_VALUE",7).toString())
                       +Integer.parseInt(dbm2.getData("DETAIL_VALUE",10).toString())
                       +Integer.parseInt(dbm2.getData("DETAIL_VALUE",13).toString());
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
              sb.append("p = "+p+" ");
              int pp = p;
              if (spArea) {
                p = p + (int) ((double) p * spRate +0.50 );
                sb.append("special = "+spRate+" ");
              }
              if (kaisei!=0 && smProv) {
                p = p + (int) ((double) p * smRate +0.50 );
                sb.append("small = "+smRate+" ");
              }
              if (kaisei!=0 && mountRate>0.0) { 
                addUnit += (int)( (double) p * mountRate + 0.50 );
                sb.append("mount = "+mountRate+" ");
              }
              p += addUnit;
              int hiyou =(int)((double) p * unitRate);
              int futan = hiyou - (int)((double)hiyou/100.0*(double)insRate);
              //if (hiyou%10>0) futan +=1;
              sb.append(" add = "+addUnit+" unitRate = "+unitRate+ " hiyou = "+hiyou+" futan = "+futan); 
              System.out.println(sb.toString());
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
          //JLabel lab2 = new JLabel("��������������������������ͽ�ɥ����ӥ��ξ��Ϸ�֤ζ�ۤ�ɽ����ɽ�����Ƥ��ޤ���");
          JLabel lab3 = new JLabel("��������������������������������ôʬ�Ϲ�θ���Ƥ���ޤ���");
          lab3.setFont(new Font("Dialog",Font.PLAIN,11));
          //lab2.setFont(new Font("Dialog",Font.PLAIN,11));
          pnl.add(lab1,BorderLayout.NORTH);
          pnl.add(lab3,BorderLayout.CENTER);
          //pnl.add(lab2,BorderLayout.CENTER);
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
      System.out.println("Panel created OK");
    }

    public boolean isSelected() {
      //int sel = usrTbl.getSelectedRow();
      //return (sel!=-1) ? true:false;
      return true;
    }

    public void setSelectable(boolean selectable) {
      isSelectable = selectable;
    }
    public String getHouKanDataCsv(int pno) {
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
      fieldName.addElement("���߶�ʬ");
      fieldName.addElement("������ʬ");
      fieldName.addElement("���ֶ�ʬ");
      fieldName.addElement("������");
      fieldName.addElement("�۵�");
      fieldName.addElement("�ô�");
      fieldName.addElement("����");
      if (targetYear>2009 || targetYear==2009 && targetMonth>=4) {
        fieldName.addElement("�Ϳ�");
        fieldName.addElement("2���ܻ���");
        fieldName.addElement("����");
        fieldName.addElement("Ĺ��");
        fieldName.addElement("�滳��");
      }
      if (td==0) {
        fieldName.addElement("���");
      }
      fieldName.addElement("����");
      fieldName.addElement("��ô��");
      if (td==0) {
        fieldName.addElement("����¾");
        fieldName.addElement("��ô�۹��");
        fieldName.addElement("������ô");
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
      sorter.setColumnClass(0,Integer.class);
      sorter.setColumnClass(2,Integer.class);
      if (td==0) {
        if (targetYear>2009 || targetYear==2009 && targetMonth>=4) {
          sorter.setColumnClass(17,Integer.class);
          sorter.setColumnClass(18,Integer.class);
          sorter.setColumnClass(19,Integer.class);
          sorter.setColumnClass(20,Integer.class);
          sorter.setColumnClass(21,Integer.class);
          sorter.setColumnClass(22,Integer.class);
        } else {
          sorter.setColumnClass(12,Integer.class);
          sorter.setColumnClass(13,Integer.class);
          sorter.setColumnClass(14,Integer.class);
          sorter.setColumnClass(15,Integer.class);
          sorter.setColumnClass(16,Integer.class);
          sorter.setColumnClass(17,Integer.class);
        }
      } else {
        if (targetYear>2009 || targetYear==2009 && targetMonth>=4) {
          sorter.setColumnClass(19,Integer.class);
          sorter.setColumnClass(20,Integer.class);
        } else {
          sorter.setColumnClass(14,Integer.class);
          sorter.setColumnClass(15,Integer.class);
       }
      }
      usrTbl.getColumnModel().getColumn(0).setCellRenderer(ren);
      usrTbl.getColumnModel().getColumn(2).setCellRenderer(ren);

      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(85);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(60);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      if (td>0) {
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(60);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(60);
      } 
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(72);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(55);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(68);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(40);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
      if (targetYear>2009 || targetYear==2009 && targetMonth>=4) {
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(65);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(40);
      }
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
        usrTbl.getColumnModel().getColumn(cid).setCellRenderer(ren);
        usrTbl.getColumnModel().getColumn(cid).setPreferredWidth(60);
      }
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
        num=(targetYear>2009 || targetYear==2009 && targetMonth>=4)? 23:18;
      } else {
        num=(targetYear>2009 || targetYear==2009 && targetMonth>=4)? 21:16;
      }

      float width[] = new float[num];
      int ctype[] = new int[num];
      Arrays.fill(ctype,0);
      ctype[cid] = 2; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("2.5"); //No.
      width[cid++] = Float.parseFloat("11.5"); //��̾
      ctype[cid] = 2; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("3.5"); //ǯ��
      width[cid++] = Float.parseFloat("5.5"); //�ײ����
      width[cid++] = Float.parseFloat("3.5"); //����
      if (targetDay>0) {
        width[cid++] = 6; //���ϻ���
        width[cid++] = 6; //��λ����
      } 
      width[cid++] = Float.parseFloat("7.5"); //���߶�ʬ
      width[cid++] = Float.parseFloat("5.5"); //������ʬ
      width[cid++] = Float.parseFloat("6.8"); //���ֶ�ʬ
      width[cid++] = Float.parseFloat("4.5"); //������
      width[cid++] = Float.parseFloat("3.0"); //�۵�
      width[cid++] = Float.parseFloat("3.2"); //�ô�
      width[cid++] = Float.parseFloat("3.2"); //�����ߥʥ�
      if (targetYear>2009 || targetYear==2009 && targetMonth>=4) {
        width[cid++] = Float.parseFloat("3.2"); //�Ϳ�
        width[cid++] = Float.parseFloat("6.5"); //����ܻ���
        width[cid++] = Float.parseFloat("3.2"); //����
        width[cid++] = Float.parseFloat("3.2"); //Ĺ����
        width[cid++] = Float.parseFloat("4.5"); //�滳��
      }
      if (targetDay==0) {
        ctype[cid] = 2; // 0 - normal 1 - add comma 2 - align right
        width[cid++] = Float.parseFloat("3.0"); //���
      }
      ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("6.5"); //����
      ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = Float.parseFloat("5.5"); //��ô��
      if (targetDay==0) {
        ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
        width[cid++] = Float.parseFloat("5.5"); //����¾��ô��
        ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
        width[cid++] = Float.parseFloat("7.5"); //��ô�۹��
        ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
        width[cid++] = Float.parseFloat("6.5"); //������ô��
      }
      //Calendar cal = Calendar.getInstance();
      //String date=cal.get(Calendar.YEAR)+""+(cal.get(Calendar.MONTH) + 1)
      //            +""+cal.get(Calendar.DATE);
      //String fname = "HOUKAI"+date+".pdf";
      StringBuffer sb = new StringBuffer();
      sb.append("HOUKAI-");
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
      if (pdf.openPDF("ˬ��Ǹ����")) {
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
