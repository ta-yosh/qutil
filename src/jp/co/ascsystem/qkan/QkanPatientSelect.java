package jp.co.ascsystem.qkan;

import java.io.*;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.regex.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;

import jp.co.ascsystem.util.*;
import jp.co.ascsystem.lib.*;

public class QkanPatientSelect {

    private DngDBAccess dbm;
    private Vector fieldName = new Vector(); 
    private Vector data = new Vector();
    private JTable usrTbl;
    private boolean isSelectable=true;
    public int Rows;
    private DefaultTableModel dtm;
    private TableSorter2 sorter;
    private Hashtable careRate = new Hashtable();
    String osType;

    public QkanPatientSelect(String csvFile) {
      String line;
      try {
        BufferedReader reader = new BufferedReader(new FileReader(csvFile));
        while ((line=reader.readLine()) !=null) {
          String[] ritems = new String[14];
          String[] items = line.split(",");
          int skip=0;
          int col=0;
          Vector rdat = new Vector();
          rdat.addElement(new Integer(Rows+1));
          for (int i=0;i<items.length;i++) {
            if (skip>0) {
              skip--;
              continue;
            }
            col++;
            String item = items[i];
            if (item.matches("^\"(([^\"]|[^a-zA-Z_0-9\"])*?(\"\")*?[^\"]*?)*?\"$")) {
              item = item.replaceAll("^\"","").replaceAll("\"$","");
            }
            else if (item.matches("^\"(([^\"]|[^a-zA-Z_0-9\"])*?(\"\")*?[^\"]*?)*$")) {
              while (! item.matches("^\"(([^\"]|[^a-zA-Z_0-9\"])*?(\"\")*?[^\"]*?)*?\"$")) { 
                skip++;
                item = item +","+ items[i+skip];
              }
              item = item.replaceAll("^\"","").replaceAll("\"$","");
            }
            item = item.replaceAll("\"\"","\"");
            if (col==4) item = "   "+item;
            if (col==6) {
              Integer age = new Integer(patientAge(rdat.get(5).toString()));
              rdat.addElement(age);
            }
            else if (col<6) rdat.addElement(item);
            else ritems[col] = item;
          }
          for (int i=7;i<14;i++) {
            if (i<11) rdat.addElement((col==13) ? ritems[i+3]:"");
            else rdat.addElement(ritems[i-4]);
          }
          this.data.addElement(rdat); 
          Rows++;
        }
      }
      catch (Exception e) {
         Rows--;
      }
    }

    public QkanPatientSelect(String dbUri,String dbUser,String dbPass) {
      careRate.put("01","Èó³ºÅö");
      careRate.put("11","Í×»Ù±ç(·Ð²áÅªÍ×²ð¸î)");
      careRate.put("12","Í×»Ù±ç1");
      careRate.put("13","Í×»Ù±ç2");
      careRate.put("21","Í×²ð¸î1");
      careRate.put("22","Í×²ð¸î2");
      careRate.put("23","Í×²ð¸î3");
      careRate.put("24","Í×²ð¸î4");
      careRate.put("25","Í×²ð¸î5");
      osType = System.getProperty("os.name").substring(0,3);
      dbm = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
      StringBuffer buf = new StringBuffer();
      //buf.append("select PATIENT_ID,PATIENT_CODE,PATIENT_FAMILY_NAME||");
      //buf.append("       ' '||PATIENT_FIRST_NAME,PATIENT_FAMILY_KANA||");
      //buf.append("       ' '||PATIENT_FIRST_KANA,PATIENT_SEX");
      //buf.append("      ,PATIENT_BIRTHDAY,PATIENT_BIRTHDAY");
      //buf.append("      ,PATIENT_ZIP_FIRST||'-'||PATIENT_ZIP_SECOND");
      //buf.append("      ,PATIENT_ADDRESS,PATIENT_TEL_FIRST||'-'||");
      //buf.append("       PATIENT_TEL_SECOND||'-'||PATIENT_TEL_THIRD");
      //buf.append("       from PATIENT ");
      buf.append("select PATIENT.*,PATIENT_NINTEI_HISTORY.JOTAI_CODE,");
      buf.append("PATIENT_NINTEI_HISTORY.INSURE_VALID_START,");
      buf.append("PATIENT_NINTEI_HISTORY.INSURE_VALID_END,");
      buf.append("PATIENT_NINTEI_HISTORY.PLANNER,");
      buf.append("PROVIDER.PROVIDER_NAME from PATIENT ");
      buf.append("left outer join PATIENT_NINTEI_HISTORY on ");
      buf.append("(PATIENT_NINTEI_HISTORY.PATIENT_ID=PATIENT.PATIENT_ID) and ");
      buf.append("PATIENT_NINTEI_HISTORY.NINTEI_HISTORY_ID=");
      buf.append("(select max(NINTEI_HISTORY_ID) from ");
      buf.append("PATIENT_NINTEI_HISTORY where PATIENT_ID=PATIENT.PATIENT_ID)");
      buf.append("left outer join PROVIDER on (PROVIDER.PROVIDER_ID=PATIENT_NINTEI_HISTORY.PROVIDER_ID) ");
      buf.append("where PATIENT.DELETE_FLAG=0 and PATIENT.SHOW_FLAG=1 ");
      buf.append("order by PATIENT_FAMILY_KANA");
      String sql = buf.toString(); 
      System.out.println(sql);
      if (dbm.connect()) {
        dbm.execQuery(sql);
        dbm.Close();
        Rows = dbm.Rows;
        System.out.println("Rows = "+Rows);
        Object data[][] = new Object[14][Rows];
        for (int i=0;i<Rows;i++) {
          StringBuffer sb = new StringBuffer();
          data[0][i] = dbm.getData("PATIENT_ID",i);
          data[1][i] = dbm.getData("PATIENT_CODE",i);
          if (dbm.getData("PATIENT_FAMILY_NAME",i)!=null) {
            sb.append(dbm.getData("PATIENT_FAMILY_NAME",i));
          }
          if (dbm.getData("PATIENT_FIRST_NAME",i)!=null) {
            if (sb.length()>0) sb.append(" ");
            sb.append(dbm.getData("PATIENT_FIRST_NAME",i));
          }
          data[2][i] = sb.toString();
          sb.delete(0,sb.length());
          if (dbm.getData("PATIENT_FAMILY_KANA",i)!=null) {
            sb.append(dbm.getData("PATIENT_FAMILY_KANA",i));
          }
          if (dbm.getData("PATIENT_FIRST_KANA",i)!=null) { 
            if (sb.length()>0) sb.append(" ");
            sb.append(dbm.getData("PATIENT_FIRST_KANA",i));
          }
          data[3][i] = sb.toString();
          sb.delete(0,sb.length());
          data[4][i] = dbm.getData("PATIENT_SEX",i);
          data[5][i] = dbm.getData("PATIENT_BIRTHDAY",i);
          data[6][i] = dbm.getData("PATIENT_BIRTHDAY",i);
          if(dbm.getData("PATIENT_ZIP_FIRST",i)!=null && 
             dbm.getData("PATIENT_ZIP_FIRST",i)!="" ) {
            sb.append(dbm.getData("PATIENT_ZIP_FIRST",i));
          }
          if (dbm.getData("PATIENT_ZIP_SECOND",i)!=null && 
              dbm.getData("PATIENT_ZIP_SECOND",i)!="" ) {
            if (sb.length()>0) sb.append("-");
            sb.append(dbm.getData("PATIENT_ZIP_SECOND",i));
          }
          data[7][i] = sb.toString();
          sb.delete(0,sb.length());
          data[8][i] = dbm.getData("PATIENT_ADDRESS",i);
          if (dbm.getData("PATIENT_TEL_FIRST",i)!=null) {
            sb.append(dbm.getData("PATIENT_TEL_FIRST",i));
          }
          if (dbm.getData("PATIENT_TEL_SECOND",i)!=null &&
              dbm.getData("PATIENT_TEL_SECOND",i)!="") {
            if (sb.length()>0) sb.append("-");
            sb.append(dbm.getData("PATIENT_TEL_SECOND",i));
          }
          if (dbm.getData("PATIENT_TEL_THIRD",i)!=null &&
              dbm.getData("PATIENT_TEL_THIRD",i)!="") {
            if (sb.length()>0) sb.append("-");
            sb.append(dbm.getData("PATIENT_TEL_THIRD",i));
          }
          data[9][i] = sb.toString();
          sb.delete(0,sb.length());
          if (dbm.getData("JOTAI_CODE",i)!=null)
          data[10][i] = careRate.get(dbm.getData("JOTAI_CODE",i).toString());
          //if (dbm.getData("INSURE_VALID_START",i)!=null)
          data[11][i] = dbm.getData("INSURE_VALID_START",i);
          //if (dbm.getData("INSURE_VALID_END",i)!=null)
          data[12][i] = dbm.getData("INSURE_VALID_END",i);
          if (dbm.getData("PLANNER",i)!=null)
          data[13][i] = (Integer.parseInt(dbm.getData("PLANNER",i).toString())==1) ? dbm.getData("PROVIDER_NAME",i):"";
        }

        for (int j=0;j<Rows;j++) {
          int[] num = new int[] {0,1,2,3,4,5,6,10,11,12,13,7,8,9};
          Vector rdat = new Vector();
          for (int i=0;i<14;i++) {
            if (i==6) {
              String str;
              Integer age;
              try {
                //str = data[i][j].toString();
                str = data[num[i]][j].toString();
                age = new Integer(patientAge(str));
              } catch(Exception e) {
                age = new Integer(0);
              }
              rdat.addElement(age);
            }
            else {
              try {
                //String str = data[i][j].toString();
                String str = data[num[i]][j].toString();
                if (i==4) str = "   "+((str.equals("1")) ? "ÃË":"½÷");
                rdat.addElement(str);
              }catch(Exception e) {
                rdat.addElement("");
              }
            }
          }
          this.data.addElement(rdat);
        }
      }
      else Rows=-1;
    }

    public boolean isSelected() {
      int sel = usrTbl.getSelectedRow();
      return (sel!=-1) ? true:false;
    }

    public void setSelectable(boolean selectable) {
      isSelectable = selectable;
    }

    public Object[][] getSelectedPatients() {
      int rows[] = usrTbl.getSelectedRows();
      Object pdat[][] = new Object[rows.length][5];
      for (int i=0;i<rows.length;i++) {
        pdat[i][0] = usrTbl.getValueAt(rows[i],0);
        pdat[i][1] = usrTbl.getValueAt(rows[i],1);
        pdat[i][2] = usrTbl.getValueAt(rows[i],2);
        pdat[i][3] = usrTbl.getValueAt(rows[i],3);
        pdat[i][4] = usrTbl.getValueAt(rows[i],5);
      }
      return pdat;
    }

    public Vector getPatientByPno(int pno,int nno) {
      int[] num = new int[] {0,1,2,3,4,5,6,11,12,13,7,8,9,10};
      Vector dat = new Vector();
      int no = nno;
      for (int i=0;i<usrTbl.getRowCount();i++) {
        if (pno==Integer.parseInt((usrTbl.getValueAt(i,0)).toString())) {
          if (nno<0) dat.addElement(new Integer(-no));
          else dat.addElement(new Integer(nno));
          for (int j=1;j<14;j++) {
             if (nno<0) dat.addElement(usrTbl.getValueAt(i,j));
             else dat.addElement(usrTbl.getValueAt(i,num[j]));
          }
          return dat;
        }
      }
      return null;
    }

    public String getPatientBasicDataCsv(int pno) {
      StringBuffer csvRecord;
      int[] num = new int[] {0,1,2,3,4,5,6,11,12,13,7,8,9,10};
      for (int i=0;i<usrTbl.getRowCount();i++) {
        if (pno==Integer.parseInt((usrTbl.getValueAt(i,0)).toString())) {
           csvRecord = new StringBuffer();
           for (int j=1;j<usrTbl.getColumnCount();j++) {
             csvRecord.append("\"");
             //csvRecord.append(usrTbl.getValueAt(i,j).toString().replaceAll("^ +","").replaceAll(" +$",""));
             csvRecord.append(usrTbl.getValueAt(i,num[j]).toString().replaceAll("^ +","").replaceAll(" +$",""));
             csvRecord.append("\"");
             if (j<usrTbl.getColumnCount()-1) csvRecord.append(",");
           }
          return csvRecord.toString();
        }
      }
      return null;
    }

    public String[][] getPatientDataSql(String type,int pno,int newpno,String[] subtype) {
      if (!dbm.connect()) return null;

      Calendar c = Calendar.getInstance();
      int nextYear = c.get(c.YEAR)+1; 
      String rsql[][] = getDataSql(type,pno,(new Integer(newpno)).toString());
      System.out.println("[GET OK]");

      if (subtype==null || rsql==null) {
        dbm.Close();
        return (rsql==null) ? null:rsql;
      }
      else  System.out.println("detail START");
      int ii=0;
      String dsql[][] = new String[rsql.length][];
      for (int i=0;i<rsql.length;i++) {
        ArrayList sqlist = new ArrayList();
        for (int j=0;j<subtype.length-1;j++) {
          for (int k=2006;k<=nextYear;k++) {
            String rsql2[][] = getDataSql(type+subtype[j]+"_"+k,Integer.parseInt(rsql[i][1]),"NEW_ID");
            if (rsql2==null) continue;
            for (int l=0;l<rsql2.length;l++) sqlist.add(rsql2[l][0]);
          }
        }
        if (type=="CLAIM") {
          String rsql3[][] = getDataSql(type+subtype[subtype.length-1],Integer.parseInt(rsql[i][1]),"NEW_ID");
          if (rsql3!=null) {
            for (int l=0;l<rsql3.length;l++) sqlist.add(rsql3[l][0]);
          }
        }
        dsql[i] = new String[sqlist.size()+1];
        dsql[i][0] = rsql[i][0];
        for (int j=0;j<sqlist.size();j++) {
          dsql[i][j+1] = sqlist.get(j).toString();
        }
        System.out.println("dsql: "+i+" sqlist: "+sqlist.size());
      }
      dbm.Close();
      return dsql;
    }

    public String[][] getDataSql(String type,int pno,String newpno) {
      String whereKey;
      StringBuffer sb= new StringBuffer();
      if (type.matches("^SERVICE_D.*")) {
        whereKey = "SERVICE_ID";
      } else if (type.matches("^CLAIM_.*DETAIL.*")) {
        whereKey = "CLAIM_ID";
      } else {
        whereKey = "PATIENT_ID";
      }
      sb.append("select * from ");
      sb.append(type);
      sb.append(" where ");
      sb.append(whereKey);
      sb.append("=");
      sb.append(pno);
      if (type.matches("^PATIENT_KOHI_SER.*")) {
        sb.append(" order by KOHI_ID,SYSTEM_SERVICE_KIND_DETAIL"); 
      } else if (type.matches("^PATIENT_CHANGES.*")) {
        sb.append(" order by CHANGES_HISTORY_ID"); 
      } else if (type.matches("^CLAIM")) {
        sb.append(" order by CLAIM_ID"); 
      } else if (type.matches("^SERVICE")) {
        sb.append(" order by SERVICE_ID"); 
      } else if (type.matches("^HOMONKANGO_PLAN_NOTE")) {
        sb.append(" order by NOTE_ID"); 
      } else if (type.matches("^HOMONKANGO_RESULT_CAL.*")) {
        sb.append(" order by VISIT_DATE"); 
      } else if (type.matches("^PATIENT_.*")) {
        sb.append(" order by ");
        sb.append(type.substring(type.indexOf("_")+1));
        sb.append("_ID"); 
      } else { sb.append(" order by LAST_TIME"); }
      String sql = sb.toString();
      dbm.execQuery(sql);
      System.out.println("[prepare "+dbm.Rows+"] "+sql);
      if (dbm.Rows<1) return null;
      Object fieldName[] = dbm.getFieldNames();
      int ii=0;
      String dsql[][]= new String[dbm.Rows][2];
      Object dat[] = new Object[fieldName.length];
      for (int j=0;j<dbm.Rows;j++) {
        dat = dbm.fetchRow();
        if (type.matches("^SERVICE") || type.matches("^CLAIM")) {
          dsql[ii][1] = dat[0].toString();
        }
        StringBuffer sb1 = new StringBuffer();
        sb1.append("insert into ");
        sb1.append(type);
        sb1.append(" (");
        for (int i=0;i<fieldName.length;i++) {
          if (i>0) sb1.append(",");
          sb1.append(fieldName[i].toString());
        }
        sb1.append(") values (");  
        for (int i=0;i<fieldName.length;i++) {
          if (i>0) sb1.append(",");
          if (fieldName[i].toString().equals(whereKey)) {
            sb1.append(newpno);
          } else if (fieldName[i].toString().equals("CLAIM_ID") && type.matches("^CLAIM")) {
            sb1.append(" (select case when MAX(CLAIM_ID) is null then 1 else max(CLAIM_ID)+1 end from CLAIM)");
          } else if (fieldName[i].toString().equals("SERVICE_ID") && type.matches("^SERVICE")) {
            sb1.append(" (select case when MAX(SERVICE_ID) is null then 1 else max(SERVICE_ID)+1 end from SERVICE)");
          } else {
            if (dat[i]!=null && dat[i].toString().length()>0) {
              sb1.append("'");
              String str = dat[i].toString();
              str = str.replaceAll("\'","''");
              sb1.append(str);
              sb1.append("'");
            }
            else sb1.append("null");
          }
        }
        sb1.append(")");
        dsql[ii++][0] = sb1.toString();
        System.out.println("[GET "+ii+"/"+dbm.Rows+":"+dat[0].toString()+"] "+sb1);
      }
      return dsql;
    }

    public String getPatientBasicDataSql(int pno) {
      Vector dat=getPatientByPno(pno,pno);
      if (dat==null) return null;
      String fieldName[] = {"PATIENT_ID",
                            "PATIENT_CODE",
                            "PATIENT_FAMILY_NAME",
                            "PATIENT_FIRST_NAME",
                            "PATIENT_FAMILY_KANA",
                            "PATIENT_FIRST_KANA",
                            "PATIENT_SEX",
                            "PATIENT_BIRTHDAY",
                            "PATIENT_ZIP_FIRST",
                            "PATIENT_ZIP_SECOND",
                            "PATIENT_ADDRESS",
                            "PATIENT_TEL_FIRST",
                            "PATIENT_TEL_SECOND",
                            "PATIENT_TEL_THIRD",
                            "SHOW_FLAG",
                            "DELETE_FLAG",
                            "LAST_TIME"};
                           
      StringBuffer sb = new StringBuffer();
      sb.append("insert into PATIENT (");
      for (int i=0;i<fieldName.length;i++) {
        if (i>0) sb.append(",");
        sb.append(fieldName[i]);
      }
      sb.append(") values (");  
      sb.append(" (select case when MAX(PATIENT_ID) is null then 1 else max(PATIENT_ID)+1 end from PATIENT)");
      int num = 0;
      int nf = 0;
      for (int i=1;i<10;i++) {
        String[] val = new String[3];
        String[] wk = new String[3];
        String male = new String("ÃË");
        int loop = 1;
        switch (i) {
          case 2: loop=2; 
                  wk= dat.elementAt(++num).toString().split(" |¡¡",2);
                  if (wk.length==1) {
                    if (wk[0].length()>16) {
                      val[0] = wk[0].substring(0,16);
                      val[1] = wk[0].substring(16,Math.min(wk[0].length(),31));
                      wk = null;
                    } 
                  }
                  break;
          case 3: loop=2;
                  wk= dat.elementAt(++num).toString().split(" |¡¡",2);
                  if (wk.length==1) {
                    if (wk[0].length()>16) {
                      val[0] = wk[0].substring(0,16);
                      val[1] = wk[0].substring(16,Math.min(wk[0].length(),31));
                      wk = null;
                    } 
                  } else {
                    if (wk[0].length()>16) {
                      val[0] = wk[0].substring(0,16);
                      wk[1] = wk[0].substring(16,wk[0].length())+wk[1];
                    } 
                    else val[0] = wk[0];
                    if (wk[1].length()>16) {
                      val[1] = wk[1].substring(0,16);
                    }
                    else val[1] = wk[1];
                    wk=null;
                  }
                  break;
          case 4: String v=dat.elementAt(++num).toString();
                  wk[0]=(v.indexOf(male)>=0) ? "1":"2";
                  break;
          case 5: wk[0] = dat.elementAt(++num).toString().replaceAll("/","-");
                  break;
          case 6: loop=0;
                  num++;
                  break;
          case 7: loop=2;
                  wk =  dat.elementAt(++num).toString().split("-",2);
                  break;
          case 9: loop=3;
                  String[] wk0 = dat.elementAt(++num).toString().split("-",3);
                  if ((wk0[0]!=null||wk0[0]!="") && wk0.length==2 && !wk0[0].matches("0[0-9]{1,5}")) {
                    wk[0]= null;
                    wk[1]= wk0[0];
                    wk[2]= wk0[1];
                  }
                  else {
                   for (int l=0;l<wk0.length;l++) if (wk0[l]!=null) wk[l]= wk0[l];
                  }
                  break;
          default: wk[0] = dat.elementAt(++num).toString();
        }
        if (loop==0) continue;
        if (wk!=null) {
          for (int j=0;j<wk.length;j++) {
            val[j] = wk[j];
          }
        }
        
        for (int j=0;j<loop;j++) {
          nf++;
          sb.append(",");
          if (val[j]!=null && val[j].length()>0) sb.append("'");
          sb.append((val[j]!=null && val[j].length()>0) ? val[j]:"null");
          if (val[j]!=null && val[j].length()>0) sb.append("'");
          System.out.println(val[j]);
        }
      }
      sb.append(",1,0,CURRENT_TIMESTAMP)");
      System.out.println(sb);
      return sb.toString();
    }

    public int[] checkDuplicate(Object dat[]) {
      ArrayList nl = new ArrayList();
      for (int i=0;i<usrTbl.getRowCount();i++) {
        if (dat[0].toString().replaceAll(" |¡¡","").equals(
            usrTbl.getValueAt(i,2).toString().replaceAll(" |¡¡","")) &&
            dat[1].toString().replaceAll("-|/","").equals(
            usrTbl.getValueAt(i,5).toString().replaceAll("-","")) ) {
          nl.add(usrTbl.getValueAt(i,0));
        }
      }
      if (nl.isEmpty()) return null;
      int pno[] = new int[nl.size()];
      for (int i=0;i<nl.size();i++) {
        pno[i] = Integer.parseInt((nl.get(i)).toString());
      }
      return pno;
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
    public JScrollPane getScrollList() {
      fieldName.addElement("");
      fieldName.addElement("ID");
      fieldName.addElement("»áÌ¾");
      fieldName.addElement("¤Õ¤ê¤¬¤Ê");
      fieldName.addElement("À­ÊÌ");
      fieldName.addElement("À¸Ç¯·îÆü");
      fieldName.addElement("Ç¯Îð");
      fieldName.addElement("Í×²ð¸îÅÙ");
      fieldName.addElement("Ç§Äê³«»ÏÆü");
      fieldName.addElement("Ç§Äê½ªÎ»Æü");
      fieldName.addElement("µïÂð²ð¸î»Ù±ç»ö¶È½ê");
      fieldName.addElement("Í¹ÊØÈÖ¹æ");
      fieldName.addElement("½»½ê");
      fieldName.addElement("Ï¢ÍíÀè(Tel)");
        dtm = new DefaultTableModel(data, fieldName);
        sorter = new TableSorter2(dtm);
        usrTbl = new JTable(sorter);
        sorter.setTableHeader(usrTbl.getTableHeader());
        sorter.setCellEditableAll(false);
        sorter.setColumnClass(6,Integer.class);
        //sorter.setPrimaryKeyCol(0);
        //sorter.addMouseListenerToHeaderInTable(usrTbl);
	usrTbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	usrTbl.setRowSelectionAllowed(true);
	usrTbl.setDefaultEditor(Object.class, null);
        usrTbl.setShowGrid(false);
	if (!isSelectable) usrTbl.setCellSelectionEnabled(isSelectable);
        usrTbl.getColumnModel().getColumn(0).setMinWidth(0);
        usrTbl.getColumnModel().getColumn(0).setMaxWidth(0);
        usrTbl.getColumnModel().getColumn(1).setPreferredWidth(80);
        usrTbl.getColumnModel().getColumn(2).setPreferredWidth(120);
        usrTbl.getColumnModel().getColumn(3).setPreferredWidth(120);
        usrTbl.getColumnModel().getColumn(4).setPreferredWidth(45);
        usrTbl.getColumnModel().getColumn(5).setPreferredWidth(85);
        usrTbl.getColumnModel().getColumn(6).setPreferredWidth(45);
        usrTbl.getColumnModel().getColumn(7).setPreferredWidth(80);
        usrTbl.getColumnModel().getColumn(8).setPreferredWidth(100);
        usrTbl.getColumnModel().getColumn(9).setPreferredWidth(100);
        usrTbl.getColumnModel().getColumn(10).setPreferredWidth(120);
        usrTbl.getColumnModel().getColumn(11).setPreferredWidth(85);
        usrTbl.getColumnModel().getColumn(12).setPreferredWidth(250);
        usrTbl.getColumnModel().getColumn(13).setPreferredWidth(100);
        usrTbl.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrPane = new JScrollPane();
        scrPane.getViewport().setView(usrTbl);
        scrPane.setFont(new Font("san-serif",Font.PLAIN,14));
	scrPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	scrPane.getHorizontalScrollBar();
	scrPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	scrPane.getVerticalScrollBar();
	return scrPane;
    }

    public TableSorter2 getSorter() {
        return sorter;
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

    public void addRow(Vector dat) {
      dtm.insertRow(0,dat);
      usrTbl.repaint();
    }

    public static void main(String args[]){
       JFrame frame = new JFrame();
       String uri="localhost/3050:/home/deuce/ikenj/cur/data/IKENSYO.FDB";
       String user = "sysdba";
       String pass = "masterkey";
       QkanPatientSelect cont = new QkanPatientSelect(uri,user,pass);
        frame.setTitle("DB TEST");
        frame.setBackground(Color.lightGray);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add("Center", cont.getScrollList());
        frame.pack();
        frame.setSize(650, 600);
        frame.show();
    }

}
