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
    private int vDate[][];
    private int initCode[]= new int[7];
    private int ddata[];
    private double tunitRate;
    private double yunitRate;
    private double spRate1;
    private double smRate1;
    private double spRate2;
    private double smRate2;
    private boolean spArea=false;
    private boolean smProv1=false;
    private boolean smProv2=false;

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
    //private Hashtable ratePlus = new Hashtable();

    public QkanKyotakuData(String dbUri,String dbUser,String dbPass) {
      this.dbUri = dbUri;
      this.dbUser = dbUser;
      this.dbPass = dbPass;
      dbm = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
      StringBuffer buf = new StringBuffer();
      buf.append("select PROVIDER_ID,PROVIDER_NAME,SPECIAL_AREA_FLAG from PROVIDER ");
      buf.append("where PROVIDER_ID in (");
      buf.append("   select PROVIDER_ID from PROVIDER_SERVICE");
      buf.append("    where SYSTEM_SERVICE_KIND_DETAIL in (13111,13411)");
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
      careRate.put("1"," Èó³ºÅö");
      careRate.put("11","·Ð²áÅªÍ×²ð¸î");
      careRate.put("12"," Í×»Ù±ç1");
      careRate.put("13"," Í×»Ù±ç2");
      careRate.put("21"," Í×²ð¸î1");
      careRate.put("22"," Í×²ð¸î2");
      careRate.put("23"," Í×²ð¸î3");
      careRate.put("24"," Í×²ð¸î4");
      careRate.put("25"," Í×²ð¸î5");
      pn.setOpaque(false);
      pnl.setOpaque(false);
      pn1.setOpaque(false);
      pn2.setOpaque(false);
      pn3.setOpaque(false);
      tPanel.setOpaque(false);
    }

    public void setUnit(String dat) {
      tValue.put("12",(new String[] {"","Í­","Ìµ"}));
      tValue.put("1310111",(new String[] {"","°å»Õ(I)","»õ²Ê°å»Õ","ÌôºÞ»Õ(°åÎÅµ¡´Ø)","ÌôºÞ»Õ(Ìô¶É)","´ÉÍý±ÉÍÜ»Î","»õ²Ê±ÒÀ¸»ÎÅù","´Ç¸î»Õ","°å»Õ(II:°å³ØÁí¹ç´ÉÍý)"}));
      tValue.put("1310113",(new String[] {"","1¿Í","2\uff5e9¿Í","¤½¤ÎÂ¾"}));
      yValue.put("1340108",(new String[] {"","°å»Õ(I)","»õ²Ê°å»Õ","ÌôºÞ»Õ(°åÎÅµ¡´Ø)","ÌôºÞ»Õ(Ìô¶É)","´ÉÍý±ÉÍÜ»Î","»õ²Ê±ÒÀ¸»ÎÅù","´Ç¸î»Õ","°å»Õ(II:°å³ØÁí¹ç´ÉÍý)"}));
      yValue.put("1340111",(new String[] {"","1¿Í","2\uff5e9¿Í","¤½¤ÎÂ¾"}));

      StringBuffer buf = new StringBuffer();
      buf.append("select service_code_item,service_unit,system_service_kind_detail,system_service_code_item from m_service_code ");
      buf.append(" where system_service_kind_detail in (13111,13411) ");
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

        spRate1 = (double)Integer.parseInt(dbm.getData(1,0).toString())/100.0; //ÆÃÊÌÃÏ°è(¡ó)
        smRate1 = (double)Integer.parseInt(dbm.getData(1,1).toString())/100.0;  //¾®µ¬ÌÏ(¡ó)
        taUnit.put("12",(new int[] {0,0,Integer.parseInt(dbm.getData(1,2).toString())}));
        spRate2 = (double)Integer.parseInt(dbm.getData(1,3).toString())/100.0; //ÆÃÊÌÃÏ°è(¡ó)
        smRate2 = (double)Integer.parseInt(dbm.getData(1,4).toString())/100.0;  //¾®µ¬ÌÏ(¡ó)
        yaUnit.put("12",(new int[] {0,0,Integer.parseInt(dbm.getData(1,5).toString())}));

        buf.delete(0,buf.length());
        buf.append("select provider_id,system_service_kind_detail,");
        buf.append("system_bind_path,detail_value from ");
        buf.append("PROVIDER_SERVICE_DETAIL_INTEGER,PROVIDER_SERVICE ");
        buf.append("where provider_service.provider_service_id = ");
        buf.append("provider_service_detail_integer.provider_service_id ");
        buf.append("and provider_id='");
        buf.append(currentProvider);
        buf.append("' and system_service_kind_detail in ('13111','13411') ");
        buf.append("and system_bind_path in (2,3) ");
        buf.append("order by system_service_kind_detail,system_bind_path");
        System.out.println(buf.toString());
        dbm.connect();
        dbm.execQuery(buf.toString());
        dbm.Close();
        if (dbm.Rows==2) {
          if (Integer.parseInt(dbm.getData(1,0).toString())==13111) {
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
        System.out.println(dbm.Rows+" Small1 = "+smProv1+" Small2 = "+smProv2);
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
            ym[i] = "Ê¿À®"+(new Integer(ymdata[i][0]-1988)).toString()+"Ç¯"+m+"·î";
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
          JLabel nodata = new JLabel("¢«³ºÅö¤¹¤ë¥Ç¡¼¥¿¤¬Í­¤ê¤Þ¤»¤ó¡£");
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
        day[0] = "·î´Ö";
        ddata = new int[dbm.Rows+1];
        ddata[0] = 0;
        for (int i=0;i<dbm.Rows;i++) {
          String d = dbm.getData(0,i).toString();
          ddata[i+1] = Integer.parseInt(d);
          day[i+1] = d+"Æü";
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
      Long diffTime;
      double difft;
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
        buf.append("SYSTEM_SERVICE_KIND_DETAIL,INSURED_ID,PATIENT_BIRTHDAY,");
        buf.append("substring(JOTAI_CODE from 1 for 1),JOTAI_CODE as JOTAI,");
        buf.append("count(SERVICE.SERVICE_ID),INSURE_RATE,");
        buf.append("min(INSURE_VALID_START) as INSURE_VALID_START,");
        buf.append("max(INSURE_VALID_END) as INSURE_VALID_END,");
        buf.append("max(BENEFIT_RATE) as BRATE,SERVICE_USE_TYPE,extract(DAY from SERVICE_DATE) as DS");
        buf.append(" from SERVICE inner join PATIENT on ");
        buf.append("(PATIENT.PATIENT_ID=SERVICE.PATIENT_ID and DELETE_FLAG=0)");
        buf.append(" inner join PATIENT_NINTEI_HISTORY on ");
        buf.append("(PATIENT_NINTEI_HISTORY.PATIENT_ID=SERVICE.PATIENT_ID and");
        buf.append(" NINTEI_HISTORY_ID = ");
        buf.append("(select max(NINTEI_HISTORY_ID) from ");
        buf.append("PATIENT_NINTEI_HISTORY where PATIENT_ID=SERVICE.PATIENT_ID ");
        //
        //buf.append(" and INSURE_VALID_END>=SERVICE.SERVICE_DATE");
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
        buf.append("SYSTEM_SERVICE_KIND_DETAIL, ");
        buf.append("SERVICE.SERVICE_ID,SERVICE_USE_TYPE,SERVICE_DATE,");
        buf.append("substring(JOTAI_CODE from 1 for 1),JOTAI,INSURE_RATE");
        buf.append(" order by PATIENT_FAMILY_NAME,PATIENT_FIRST_NAME,INSURED_ID,SYSTEM_SERVICE_KIND_DETAIL,SERVICE_USE_TYPE desc");

        String sql = buf.toString();
        System.out.println(sql);
        dbm.execQuery(sql);
        dbm.Close();
        Vector pdata = new Vector();
        totalRow = new Vector();
        int totalCount=0;
        int totalFee1=0;
        int totalFee2=0;
        int totalFee5=0;
        int trCols=0;
        DngDBAccess dbm2 = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
        int pNo=-1;
        int sbp=-1;
        int uTp=-1;
        int ln = 0;
        int sCount = 0;
        int countLimit = 2;
        String sids = "";
        String days = "";
        boolean monfin = false;
        boolean second = false;
        String itemCode="";
        StringBuffer sbD = new StringBuffer();

        for (int i=0;i<dbm.Rows;i++){
          int lastP = pNo;
          int lastSbp = sbp;
          String lastCode = itemCode;
          itemCode = "";
          pNo = Integer.parseInt(dbm.getData(2,i).toString());
          sbp = Integer.parseInt(dbm.getData(4,i).toString());
          if (pNo!=lastP || sbp!=lastSbp) {
            uTp = Integer.parseInt(dbm.getData("SERVICE_USE_TYPE",i).toString());
            second = false;
            if (targetDay==0) {
              if (!monfin && (lastP != -1 || lastSbp != -1) ) {
                pNo = lastP;
                sbp = lastSbp;
                i--;
                System.out.println("tbl Create start");
              } else {
                sids = dbm.getData("SID",i).toString();
                sCount = Integer.parseInt(dbm.getData("COUNT",i).toString());
                if (Integer.parseInt(dbm.getData("DS",i).toString())<10) sbD.append("0");
                sbD.append(dbm.getData("DS",i));
                sbD.append("Æü");
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
                sCount += Integer.parseInt(dbm.getData("COUNT",i).toString());
                sbD.append("¡¢");
                if (Integer.parseInt(dbm.getData("DS",i).toString())<10) sbD.append("0");
                sbD.append(dbm.getData("DS",i));
                sbD.append("Æü");
                if (i<dbm.Rows-1) continue;
              }
            }
          }
          monfin = true;
          double mountRate=0;

          String insStart = dbm.getData("INSURE_VALID_START",i).toString();
          String insEnd = dbm.getData("INSURE_VALID_END",i).toString();
          String insId = dbm.getData("INSURED_ID",i).toString();

          Vector pline = new Vector();
          int sNo = new Integer(dbm.getData(3,i).toString()).intValue();
          String nam1=(dbm.getData(1,i)!=null) ? dbm.getData(1,i).toString()+" ":"";
          String nam2=(dbm.getData(0,i)!=null) ? dbm.getData(0,i).toString():"";
          String nam =" "+nam1+nam2;

          int insRate = Integer.parseInt(dbm.getData("INSURE_RATE",i).toString());
          int bRate = (dbm.getData("BRATE",i)==null) ? 0: 
                      Integer.parseInt(dbm.getData("BRATE",i).toString());

          pline.addElement(new Integer(++ln));
          pline.addElement(insId);
          pline.addElement(nam);
          if (dbm.getData("PATIENT_BIRTHDAY",i)!=null) {
            int age =patientAge(dbm.getData("PATIENT_BIRTHDAY",i).toString());
            pline.addElement(new Integer(age));
          } else {
            pline.addElement("");
          }
          String cR="1";
          String mount = "Ìµ";
          String kind = (sbp==13111) ? 
                        "²ð¸î":"Í½ËÉ";
          Calendar cal21 = Calendar.getInstance();
          if (dbm.getData("JOTAI",i)!=null) {
            cR = dbm.getData("JOTAI",i).toString();
            String cRate = (String)careRate.get(cR);
            pline.addElement(cRate);
          } else {
            pline.addElement("");
          }
          if (targetDay>0) {
            //pline.addElement(kind);
            buf.delete(0,buf.length());
            buf.append("select SERVICE_ID,SYSTEM_BIND_PATH,");
            buf.append("extract(HOUR from DETAIL_VALUE),");
            buf.append("extract(MINUTE from DETAIL_VALUE)");
            buf.append(",DETAIL_VALUE");
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
            buf.append(" order by SYSTEM_BIND_PATH desc;");
            sql = buf.toString();
            System.out.println(sql);
            dbm2.connect();
            dbm2.execQuery(sql);
            dbm2.Close();
  
            System.out.println("dbm2:"+dbm2.Rows);
            int addUnit=0;
            int kaisei = 0;
            double unitRate = (sbp==13111) ? tunitRate:yunitRate;
            boolean smProv = (sbp==13111) ? smProv1:smProv2;
            double spRate = (sbp==13111) ? spRate1:spRate2;
            double smRate = (sbp==13111) ? smRate1:smRate2;
            String[] ssCode = new String[] {"1","1","1","1","1"};
            for (int j=0;j<dbm2.Rows;j++){
              System.out.println("Rows start: "+j);
              int sbp0 = Integer.parseInt(dbm2.getData("SYSTEM_BIND_PATH",j).toString());
              System.out.println("sbp = "+sbp0+" val = "+dbm2.getData("DETAIL_VALUE",j));
              if (sbp0==12) {
                int key = Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                String[] val = (String[])tValue.get(Integer.toString(sbp0));
                int[] add = (int[]) taUnit.get(dbm2.getData("SYSTEM_BIND_PATH",j).toString());
                System.out.println("sbp = "+sbp0+" key = "+key+" add= "+add[key]);
                mountRate = (double)add[key]/100.0;
                if (key == 2) mount = "Í­";
              }
              else if (sbp0==16) {
                int key = Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                if (ssCode[0].equals("7")) ssCode[1] = Integer.toString(key+3);
              }
              else if (sbp0==14) {
                kaisei = Integer.parseInt(dbm2.getData("DETAIL_VALUE",j).toString());
                System.out.println("kaisei = "+kaisei);
              }
              else if (sbp0==1310103 || sbp0==1340101 || sbp0==1310111 || sbp0==1340108) {
                ssCode[0] = dbm2.getData("DETAIL_VALUE",j).toString();
                System.out.println("sc0 = ["+ssCode[0]+"]");
              } 
              else if (sbp0==1310113 || sbp0==1340111) {
                ssCode[1] = dbm2.getData("DETAIL_VALUE",j).toString();
              }
              else if (sbp0==1310105 || sbp0==1340103) {
                ssCode[3] = dbm2.getData("DETAIL_VALUE",j).toString();
              }
              else if (sbp0==1310110) {
                ssCode[2] = dbm2.getData("DETAIL_VALUE",j).toString();
              }
              else if (sbp0==1310112 || sbp0==1340109) {
                ssCode[4] = dbm2.getData("DETAIL_VALUE",j).toString();
              }
            }
            for (int k=0;k<5;k++) itemCode += ssCode[k];
            System.out.println("itemCode : "+itemCode);
            buf.delete(0,buf.length());
            buf.append("select service_unit,service_name from m_service_code ");
            buf.append("where system_service_kind_detail='");
            buf.append(sbp);
            buf.append("' and  system_service_code_item='");
            buf.append(itemCode);
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
              StringBuffer sb = new StringBuffer();
              int p = Integer.parseInt(dbm2.getData(0,0).toString());
              sb.append("p = "+p);
              if (spArea && !second) {
                p = p + Math.round((float)((double) p * spRate));
                sb.append(" special = "+spRate);
              }
              if (smProv && !second ) {
                p = p + Math.round((float)((double) p * smRate));
                sb.append(" small = "+smRate);
              }
              if (mountRate>0.0 && !second ) {
                int mp = Math.round((float)((double) p * mountRate));
                addUnit += mp;
                sb.append(" mount = "+mp);
              }
              p += addUnit;
              String sName = dbm2.getData(1,0).toString();
              int hiyou =(int)((double) p * unitRate);
              int futan = hiyou - (int)Math.round((double)hiyou/100.0*(double)insRate);
              if (bRate>0) {
                int bclaim = (int)((double)hiyou/100.0*(double)(bRate-insRate));
                int bfutan = futan - bclaim;
                futan = bfutan;
              }
              sb.append(" unitRate = "+unitRate+ " hiyou = "+hiyou+" futan = "+futan);
              System.out.println(sb.toString());
              if (hiyou>0) totalFee1 += hiyou;
              if (futan>0) totalFee2 += futan;
              pline.addElement(sName);
              pline.addElement((spArea) ? "Í­":"Ìµ");
              pline.addElement((smProv) ? "Í­":"Ìµ");
              pline.addElement(mount);
              pline.addElement(new Integer(hiyou));
              pline.addElement(new Integer(futan));
            }
          } 
          else if (targetDay==0) {
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
              buf.append(" where CLAIM_ID = (select CLAIM_ID from CLAIM_DETAIL_TEXT_");
              buf.append(cYear);
              buf.append(" where CLAIM_ID in (select CLAIM_ID from CLAIM");
  
              buf.append(" where PATIENT_ID=");
              buf.append(pNo);
              buf.append(" and INSURED_ID='");
              buf.append(insId);
              buf.append("' and extract(YEAR from TARGET_DATE)=");
              buf.append(targetYear);
              buf.append(" and extract(MONTH from TARGET_DATE)=");
              buf.append(targetMonth);
              buf.append(" and CATEGORY_NO=7 and PROVIDER_ID='");
              buf.append(currentProvider);
              buf.append("') and SYSTEM_BIND_PATH=701007 ");
              if (sbp==13111) buf.append("and DETAIL_VALUE='31')");
              else buf.append("and DETAIL_VALUE='34')");
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
            days = sbD.toString();
            sbD = new StringBuffer();
            if (! days.equals("")) {
              if (cRows>0) {
                pline.addElement(new Integer(dbm2.getData("DETAIL_VALUE",0).toString()));
                totalCount += Integer.parseInt(dbm2.getData("DETAIL_VALUE",0).toString());
              }
              else {
                pline.addElement(new Integer(sCount));
                totalCount += sCount;
              }
              pline.addElement(days);
              if (cRows>0) {
                int hiyou = (int)(Float.parseFloat(dbm2.getData("DETAIL_VALUE",1).toString())*Float.parseFloat(dbm2.getData("DETAIL_VALUE",2).toString()));
                int futan = Integer.parseInt(dbm2.getData("DETAIL_VALUE",4).toString());
                int jikouhi=0,kouhi=0;
                if (cRows>5) {
                  kouhi = Integer.parseInt(dbm2.getData("DETAIL_VALUE",6).toString())
                          +Integer.parseInt(dbm2.getData("DETAIL_VALUE",9).toString())
                          +Integer.parseInt(dbm2.getData("DETAIL_VALUE",12).toString());
                 jikouhi = Integer.parseInt(dbm2.getData("DETAIL_VALUE",7).toString())
                           +Integer.parseInt(dbm2.getData("DETAIL_VALUE",10).toString())
                           +Integer.parseInt(dbm2.getData("DETAIL_VALUE",13).toString());
                 if (jikouhi>0) futan = jikouhi;
                }
                if (hiyou>0) totalFee1 += hiyou;
                if (futan>0) totalFee2 += futan;
                if (kouhi>0) totalFee5 += kouhi;
                pline.addElement(new Integer(hiyou));
                pline.addElement(new Integer(futan));
                pline.addElement(new Integer(kouhi));
              }
            }
            else {
              totalCount += sCount;
              pline.addElement(new Integer(sCount));
              pline.addElement("");
              pline.addElement("");
              pline.addElement("");
            }
          }
          pdata.addElement(pline);
          trCols = pline.size();
          Calendar cal22 = Calendar.getInstance();
          diffTime = cal22.getTimeInMillis() - cal21.getTimeInMillis();
          difft = Float.parseFloat( diffTime.toString() ) / 1000.000 ;
          System.out.println("PID "+pNo+" [ " + difft + " sec. ]");
        }
        System.out.println(trCols+"fields  "+totalCount+":"+totalFee1+":"+totalFee2+":"+totalFee5);
        int cn1,cn2,cn3,cn4,cn5,cn6;
        cn1= 6;
        cn2= 8;
        cn3= 9;
        cn4= 10;
        cn5= 11;
        cn6= 12;
        for (int c=0;c<trCols;c++) {
          if (c==1) totalRow.addElement(new String("  ¹ç ·× ÃÍ"));
          else if (targetDay>0) {
            if (c==cn5) totalRow.addElement(new Integer(totalFee1));
            else if (c==cn6) totalRow.addElement(new Integer(totalFee2));
            else totalRow.addElement(new String(""));
          }
          else {
            if (c==cn1) totalRow.addElement(new Integer(totalCount));
            else if (c==cn2) totalRow.addElement(new Integer(totalFee1));
            else if (c==cn3) totalRow.addElement(new Integer(totalFee2));
            else if (c==cn4) totalRow.addElement(new Integer(totalFee5));
            else totalRow.addElement(new String(""));
          }
        }
        scp = getScrollList(pdata,targetDay);
        pn2.add(scp);
      }
      if (dbm.Rows>0) {
        if (targetDay>0) {
          JLabel lab1 = new JLabel("¡öÆüÃ±°Ì¤Ç¤Î¶â³Û¤Ë¤Ä¤¤¤Æ¡§ÉéÃ´¶â³Û¤ÏÃ¼¿ô½èÍý¤Î´Ø·¸¤Ç·î´Ö¶â³Û¤È¤Ï°Û¤Ê¤ë¾ì¹ç¤¬¤¢¤ê¤Þ¤¹¡£ ");
          lab1.setFont(new Font("Dialog",Font.PLAIN,11));
          JLabel lab2 = new JLabel("¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¤Þ¤¿¡¢²ó¿ô¸ÂÅÙ¤Ï¹ÍÎ¸¤·¤Æ¤ª¤ê¤Þ¤»¤ó¡£");
          lab2.setFont(new Font("Dialog",Font.PLAIN,11));
          pnl.add(lab1,BorderLayout.NORTH);
          pnl.add(lab2,BorderLayout.CENTER);
        } else {
          JLabel lab1 = new JLabel("¡ö·î´Ö¤Ç¤Î¶â³Û¤Ï¡¢¼ÂÀÓ³ÎÄêÊ¬¤Î¤ßÉ½¼¨¤µ¤ì¤Þ¤¹¡£ ");
          lab1.setFont(new Font("Dialog",Font.PLAIN,11));
          JLabel lab2 = new JLabel("¡ö·îÅÓÃæ¤ÇÍ×²ð¸îÅÙ¤¬ÊÑ¤ï¤Ã¤Æ¤¤¤ë¾ì¹ç¡¢ÊÑ¹¹¸å¤ÎÍ×²ð¸îÅÙ¤¬É½¼¨¤µ¤ì¤Þ¤¹¡£¤Þ¤¿¡¢Í×»Ù±ç¢«¢ªÍ×²ð¸î¤Î¾ì¹ç¤ÏÊÌ¹Ô¤È¤Ê¤ê¤Þ¤¹¡£");
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
      //fieldName.addElement("¥µ¡¼¥Ó¥¹ID");
      fieldName.addElement("No.");
      fieldName.addElement("ÈïÊÝ¸±¼ÔÈÖ¹æ");
      fieldName.addElement("»áÌ¾");
      fieldName.addElement("Ç¯Îð");
      fieldName.addElement("Í×²ð¸îÅÙ");
      if (td>0) {
        fieldName.addElement("³«»Ï»þ¹ï");
        fieldName.addElement("½ªÎ»»þ¹ï");
        fieldName.addElement("¥µ¡¼¥Ó¥¹Ì¾¾Î");
        fieldName.addElement("ÆÃÃÏ");
        fieldName.addElement("¾®µ¬ÌÏ");
        fieldName.addElement("Ãæ»³´Ö");
      } 
      if (td==0) {
        fieldName.addElement("¼ïÎà");
        fieldName.addElement("²ó¿ô");
        fieldName.addElement("Ë¬ÌäÆü");
      }
      fieldName.addElement("ÈñÍÑ");
      fieldName.addElement("ÉéÃ´³Û");
      if (td==0) fieldName.addElement("¸øÈñÉéÃ´³Û");
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
      sorter.setColumnClass(3,Integer.class);
      if (td==0) {
        sorter.setColumnClass(6,Integer.class);
        sorter.setColumnClass(8,Integer.class);
        sorter.setColumnClass(9,Integer.class);
      } else {
        sorter.setColumnClass(11,Integer.class);
        sorter.setColumnClass(12,Integer.class);
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
        usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(60);
        usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(60);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(160);
        usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(32);
        usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(38);
        usrTbl.getColumnModel().getColumn(cid).setCellRenderer(cen);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(38);
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
      if (td==0) {
        usrTbl.getColumnModel().getColumn(cid).setCellRenderer(ren);
        usrTbl.getColumnModel().getColumn(cid++).setPreferredWidth(65);
      }
      usrTbl.getColumnModel().getColumn(cid).setCellRenderer(ren);
      usrTbl.getColumnModel().getColumn(cid).setPreferredWidth(65);
      //usrTbl.getTableHeader().setReorderingAllowed(false);
      JScrollPane scrPane = new JScrollPane();
      scrPane.getViewport().setView(usrTbl);
      scrPane.setFont(new Font("san-serif",Font.PLAIN,14));
      scrPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      scrPane.getHorizontalScrollBar();
      scrPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scrPane.getVerticalScrollBar();
      scrPane.setPreferredSize(new Dimension(740,410));
      scrPane.setBackground(new Color(210,250,230));
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
      int num=usrTbl.getColumnCount();
      float width[] = new float[num];
      int ctype[] = new int[num];
      Arrays.fill(ctype,0);
      ctype[cid] = 2; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = 4; //No.
      ctype[cid] = 2;
      width[cid++] = 7; //ÈïÊÝ¸±¼ÔÈÖ¹æ
      width[cid++] = 16; //»áÌ¾
      ctype[cid] = 2; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = 4; //Ç¯Îð
      ctype[cid] = 7;
      width[cid++] = 6; //Í×²ð¸îÅÙ
      if (targetDay>0) {
        ctype[cid] = 7;
        width[cid++] = 6; //³«»Ï»þ¹ï
        ctype[cid] = 7;
        width[cid++] = 6; //½ªÎ»»þ¹ï
        ctype[cid] = 7;
        width[cid++] = 14; //¥µ¡¼¥Ó¥¹Ì¾¾Î
        ctype[cid] = 7;
        width[cid++] = 4; //ÆÃÊÌÃÏ°è
        ctype[cid] = 7;
        width[cid++] = 4; //¾®µ¬ÌÏ
        ctype[cid] = 7;
        width[cid++] = 4; //Ãæ»³´Ö
      } 
      if (targetDay==0) {
        ctype[cid] = 7;
        width[cid++] = 3; //¼ïÎà
        ctype[cid] = 2; // 0 - normal 1 - add comma 2 - align right
        width[cid++] = 3; //²ó¿ô
        width[cid++] = 14; //Ë¬ÌäÆü
      }
      ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = 7; //ÈñÍÑ
      ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
      width[cid++] = 7; //ÉéÃ´³Û
      if (targetDay==0) {
        ctype[cid] = 1; // 0 - normal 1 - add comma 2 - align right
        width[cid++] = 7; //¸øÈñÉéÃ´
      }
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
        sb.append("Ç¯");
        if (targetMonth<10) sb.append("0");
        sb.append(targetMonth);
        sb.append("·î");
        if (targetDay>0) {
          if (targetDay<10) sb.append("0");
          sb.append(targetDay);
          sb.append("Æü");
        } 
        sb.append(" Äó¶¡Ê¬");
        pdf.setSubTitle(sb.toString());
      if (pdf.openPDF("µïÂðÎÅÍÜ´ÉÍý»ØÆ³¾ðÊó")) {
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
