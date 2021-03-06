package jp.co.ascsystem.qkan;

import java.io.*;
import java.util.Calendar;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JLabel;

import jp.co.ascsystem.lib.*;
import jp.co.ascsystem.util.*;

public class QkanExecTransaction extends Thread {

    private static final int STATE_SUCCESS = 0;
    private static final int STATE_CANCEL = -1;
    private static final int STATE_ERROR = -2;
    private static final int STATE_FATAL = -3;

    public int stat=STATE_SUCCESS;
    public String errMessage;
    public String errSql;
    public boolean isStarted = false;
    boolean runStat0 = false;
    boolean runStat = true;
    boolean runStat1 = false;
    String pfile;
    int pNos[][];
    JProgressBar progressBar;
    int count;
    QkanPatientSelect iTable;
    QkanPatientSelect oTable;
    QkanTsusyoData tTable;
    QkanHouKaiData hTable;
    QkanHouKanData nTable;
    QkanHouRehaData vTable;
    QkanTsusyoRehaData rTable;
    QkanKyotakuData kTable;
    QkanProviderData pTable;
    public String dbOutPath;

    public void setPnos(int pNo[][]) {
      pNos = pNo;
      count = pNos.length;
    }

    public QkanExecTransaction(String pFile,int total,JProgressBar bar) {
      count = total;
      progressBar = bar;
      pfile = pFile; 
    }
    public QkanExecTransaction(int total,JProgressBar bar) {
      count = total;
      progressBar = bar;
    }

    public void setTable(QkanPatientSelect iTable,QkanPatientSelect oTable) {
      this.iTable=iTable;
      this.oTable=oTable;
    }
    
    public void setTable(QkanHouKaiData hTable) {
      this.iTable=null;
      this.oTable=null;
      this.hTable=hTable;
    }
    
    public void setTable(QkanHouKanData nTable) {
      this.iTable=null;
      this.oTable=null;
      this.nTable=nTable;
    }
    public void setTable(QkanHouRehaData vTable) {
      this.iTable=null;
      this.oTable=null;
      this.vTable=vTable;
    }
    public void setTable(QkanTsusyoData tTable) {
      this.iTable=null;
      this.oTable=null;
      this.tTable=tTable;
    }
    public void setTable(QkanKyotakuData kTable) {
      this.iTable=null;
      this.oTable=null;
      this.kTable=kTable;
    }
    public void setTable(QkanTsusyoRehaData rTable) {
      this.iTable=null;
      this.oTable=null;
      this.rTable=rTable;
    }
    public void setTable(QkanProviderData pTable) {
      this.iTable=null;
      this.oTable=null;
      this.pTable=pTable;
    }
    
    public void run() {
      isStarted = true;
      stat = STATE_SUCCESS;
      DngDBAccess dbm=null; 
      FileWriter fos=null;
      //Calendar c = Calendar.getInstance();
      //int nextYear = c.get(c.YEAR)+1;
      try {
        synchronized(this) {
          while(!runStat0) wait();
        }
      } catch(InterruptedException ie) {
         stat = STATE_CANCEL;
         System.out.println("Interrupted before exec");
         return;
      }
      int lcount = 0;
      if (pfile!=null) {
        DngAppProperty props = new DngAppProperty(pfile); 
        String dbPort = props.getProperty("doc/DBConfig/Port");
        String dbUser = props.getProperty("doc/DBConfig/UserName");
        String dbPass = props.getProperty("doc/DBConfig/Password");
        String dbPath = (oTable==null) ? dbOutPath:props.getProperty("doc/DBConfig/Path");
        String dbServer = props.getProperty("doc/DBConfig/Server");
        String dbUri = dbServer+"/"+dbPort+":"+dbPath;
        dbm = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
        if (!dbm.connect()) {
          stat = STATE_FATAL;
          errMessage = "データベースに接続できません。\nDB:"+dbUri;
          interrupt();
          return;
        }
      }
      else {
        try{
          fos = new FileWriter( dbOutPath );
        } catch (IOException e) {
          errMessage = "ファイルをオープンできません。 \nFile:"+dbOutPath;
          interrupt();
          return;
        }
        if (tTable!=null || kTable!=null || hTable!=null || nTable!=null || vTable!=null  || rTable!=null || pTable!=null) {
          StringBuffer sb = new StringBuffer(); 
          if (tTable!=null) {
            sb.append("\"\",\"");
            sb.append(tTable.curProviderName);
            sb.append("\",\"\",\"\",\"通所介護情報\",\"");
            sb.append(tTable.targetYear);
            sb.append("年\",\"");
            sb.append(tTable.targetMonth);
            sb.append("月\",\"");
            if (tTable.targetDay==0)  
              sb.append("月間");
            else {
                 sb.append(tTable.targetDay);
                 sb.append("日");
            }
          }
          else if (hTable!=null) {
            sb.append("\"\",\"");
            sb.append(hTable.curProviderName);
            sb.append("\",\"\",\"\",\"訪問介護情報\",\"");
            sb.append(hTable.targetYear);
            sb.append("年\",\"");
            sb.append(hTable.targetMonth);
            sb.append("月\",\"");
            if (hTable.targetDay==0)  
              sb.append("月間");
            else {
                 sb.append(hTable.targetDay);
                 sb.append("日");
            }
          }
          else if (nTable!=null) {
            sb.append("\"\",\"");
            sb.append(nTable.curProviderName);
            sb.append("\",\"\",\"\",\"訪問看護情報\",\"");
            sb.append(nTable.targetYear);
            sb.append("年\",\"");
            sb.append(nTable.targetMonth);
            sb.append("月\",\"");
            if (nTable.targetDay==0)  
              sb.append("月間");
            else {
                 sb.append(nTable.targetDay);
                 sb.append("日");
            }
          }
          else if (vTable!=null) {
            sb.append("\"\",\"");
            sb.append(vTable.curProviderName);
            sb.append("\",\"\",\"\",\"訪問リハ情報\",\"");
            sb.append(vTable.targetYear);
            sb.append("年\",\"");
            sb.append(vTable.targetMonth);
            sb.append("月\",\"");
            if (vTable.targetDay==0)  
              sb.append("月間");
            else {
                 sb.append(vTable.targetDay);
                 sb.append("日");
            }
          }
          else if (kTable!=null) {
            sb.append("\"\",\"");
            sb.append(kTable.curProviderName);
            sb.append(" \",\"居宅療養管理指導情報\",\"\",\"\",\"");
            sb.append(kTable.targetYear);
            sb.append("年\",\"");
            sb.append(kTable.targetMonth);
            sb.append("月\",\"");
            if (kTable.targetDay==0)  
              sb.append("月間");
            else {
                 sb.append(kTable.targetDay);
                 sb.append("日");
            }
          }
          else if (pTable!=null) {
            sb.append("\"\",\"");
            sb.append("給管鳥\",\"\",\"事業所情報");
          }
          else {
            sb.append("\"\",\"");
            sb.append(rTable.curProviderName);
            sb.append("\",\"\",\"\",\"通所リハ情報\",\"");
            sb.append(rTable.targetYear);
            sb.append("年\",\"");
            sb.append(rTable.targetMonth);
            sb.append("月\",\"");
            if (rTable.targetDay==0)  
              sb.append("月間");
            else {
                 sb.append(rTable.targetDay);
                 sb.append("日");
            }
          }
          sb.append("\"\r\n");
          String rec = (tTable!=null) ? tTable.getTsusyoDataCsv(-1):
                       (hTable!=null) ?  hTable.getHouKaiDataCsv(-1):
                       (nTable!=null) ?  nTable.getHouKanDataCsv(-1):
                       (vTable!=null) ?  vTable.getHouRehaDataCsv(-1):
                       (kTable!=null) ?  kTable.getKyotakuDataCsv(-1):
                       (pTable!=null) ?  pTable.getProviderDataCsv(-1):
                                         rTable.getTsusyoRehaCsv(-1);
          rec = sb.toString() + rec;
          try {
            fos.write(rec);
            fos.write("\r\n");
          } catch(IOException ex) {
            stat=STATE_ERROR;
            errMessage = "書き出し用CSVファイルに書き込めません。"+ex.toString();
            try{ fos.close();} catch(IOException ex2){} 
            return;
          }
        }
      }
      for (int i=0;i<pNos.length;i++) {
        if (pNos[i]==null) continue;
        System.out.println(pNos[i][0]+" START"+runStat);
        try {
          sleep(0);
          synchronized(this) {
            while(!runStat) wait();
          }
        } catch(InterruptedException ie) {
          stat = STATE_CANCEL;
          System.out.println("Interrupted exec");
          break;
        }
        if (stat == STATE_CANCEL) break;
        if (stat==STATE_SUCCESS) {
          String sql;
          String bsql=null;
          do {
            System.out.println(pNos[i][0]+" try get data");
            bsql = (pfile!=null) ? iTable.getPatientBasicDataSql(pNos[i][0]) :
                  ((iTable!=null) ? iTable.getPatientBasicDataCsv(pNos[i][0]):
                  ((tTable!=null) ? tTable.getTsusyoDataCsv(pNos[i][0]):
                  ((hTable!=null) ? hTable.getHouKaiDataCsv(pNos[i][0]):
                  ((nTable!=null) ? nTable.getHouKanDataCsv(pNos[i][0]):
                  ((vTable!=null) ? vTable.getHouRehaDataCsv(pNos[i][0]):
                  ((kTable!=null) ? kTable.getKyotakuDataCsv(pNos[i][0]):
                  ((pTable!=null) ? pTable.getProviderDataCsv(pNos[i][0]):
                                    rTable.getTsusyoRehaCsv(pNos[i][0]))))))));
            if (bsql.equals("CON0")) {
              System.out.println("DB server has been busy. I try to connect again 20sec. after.... please wait.");
              try {sleep(20000);} catch(Exception ie){};
            }
          } while (bsql.equals("CON0"));
          System.out.println(pNos[i][0]+" set ok");
          int dNum[]=null;
          int patientNo=0;
          if (pfile!=null) {
            String type[] = {"PATIENT_CHANGES_HISTORY",
                             "PATIENT_KOHI",
                             "PATIENT_KOHI_SERVICE",
                             "PATIENT_MEDICAL_HISTORY",
                             "PATIENT_NINTEI_HISTORY",
                             "PATIENT_SHISETSU_HISTORY",
                             "PATIENT_STATION_HISTORY",
                             "SERVICE",
                             "CLAIM",
                     "SERVICE_PASSIVE_CHECK","CLAIM_PATIENT_MEDICAL"
                    ,"HOMONKANGO_JOHO_TEIKYOSHO","HOMONKANGO_KIROKUSHO"
                    ,"HOMONKANGO_PLAN","HOMONKANGO_PLAN_NOTE"
                    ,"HOMONKANGO_RESULT","HOMONKANGO_RESULT_CALENDAR"
                    ,"KYOTAKU_RYOYO","PATIENT"};
            String detail[] = {"_DETAIL_DATE","_DETAIL_INTEGER","_DETAIL_TEXT","_PATIENT_DETAIL"};
            dbm.begin();
            if (pNos[i][1]>0) {
              StringBuffer sb = new StringBuffer();
              dNum = new int[pNos[i].length-1];
              for (int j=1;j<pNos[i].length;j++) {
                if (j>1) sb.append(",");
                sb.append(pNos[i][j]);
                dNum[j-1] = pNos[i][j];
              }

              for (int j=0;j<type.length;j++) {
                if (type[j]=="SERVICE" || type[j]=="CLAIM") {
                  int minYear = (type[j]=="SERVICE") ? iTable.sdMinYear:iTable.cdMinYear;
                  int maxYear = (type[j]=="SERVICE") ? iTable.sdMaxYear:iTable.cdMaxYear;
                  String wh = (type[j]=="SERVICE") ? "SERVICE_ID":"CLAIM_ID";
                  sql = "select "+wh+" from "+type[j]+" where PATIENT_ID in ("+sb.toString()+")";
                  dbm.execQuery(sql);
                  if (dbm.Rows>0) {
                    StringBuffer sbd = new StringBuffer();
                    for (int l=0;l<dbm.Rows;l++) {
                      if (l>0) sbd.append(",");
                      sbd.append(dbm.getData(0,l).toString());
                    }
                    for (int l=0;l<detail.length-1;l++) {
                      for (int n=minYear;n<=maxYear;n++) {
                        sql = "delete from "+type[j]+detail[l]+"_"+n+" where "+wh+" in ("+sbd.toString()+")";
                        try {dbm.execUpdate(sql); 
                        } catch (Exception e) {
                          System.out.println("[DELETE Error detail]"+sql+"\r\n"+e);
                        };
                      }
                    }
                    if (type[j]=="CLAIM") {
                      sql = "delete from CLAIM_PATIENT_DETAIL where "+wh+" in ("+sbd.toString()+")";
                      try {dbm.execUpdate(sql); 
                        System.out.println("[DELETE detail]"+sql);
                      } catch (Exception e) {};
                    }
                  }
                }
                sql = "delete from "+type[j]+" where PATIENT_ID in ("+sb.toString()+")";
                dbm.execUpdate(sql);
                System.out.println("[DELETE TABLE]"+sql);
              }
            }
            int dbstat=dbm.execUpdate(bsql);
            if (dbstat!=-1 && pNos[i][1]>=0 ) {
              sql = "select max(PATIENT_ID) from PATIENT;";
              dbm.execQuery(sql);
              patientNo = Integer.parseInt((dbm.getData(0,0)).toString());
              System.out.println("PATIENT add "+patientNo);
              for (int j=0;j<type.length-1;j++) {
                System.out.println(type[j]+" START");
                String sqls[][] = (type[j]=="SERVICE" || type[j]=="CLAIM") ?
                  iTable.getPatientDataSql(type[j],pNos[i][0],patientNo,detail):
                  iTable.getPatientDataSql(type[j],pNos[i][0],patientNo,null);
                if (sqls==null) {
                  System.out.println(type[j]+" skip");
                  continue;
                }
                System.out.println(type[j]+" has "+sqls.length+" sqls");
                for (int k=0;k<sqls.length;k++) {
                  if (stat==STATE_ERROR) {
                    break;
                  }
                  sql = sqls[k][0];
                  System.out.println("[EXECUTE REQ] "+sql);
                  if (dbm.execUpdate(sql)==-1) {
                     System.out.println("[FAILED]");
                     stat=STATE_ERROR;
                     break;
                  }
                  System.out.println("[EXCUTE SUCCEED]");
                  if (type[j]=="SERVICE" || type[j]=="CLAIM") {
                    StringBuffer sb1= new StringBuffer();
                    sb1.append("select max(");
                    sb1.append(type[j]);
                    sb1.append("_ID),max(");
                    sb1.append(type[j]);
                    sb1.append("_DATE),min(");
                    sb1.append(type[j]);
                    sb1.append("_DATE) from ");
                    sb1.append(type[j]);
                    dbm.execQuery(sb1.toString());
                    String newId = dbm.getData(0,0).toString();
                    System.out.println("[DETAIL "+(sqls[k].length-1)+"line START]");
                    for (int l=1;l<sqls[k].length;l++) {
                      if (sqls[k][l]==null) continue;
                      sql = sqls[k][l].replaceAll("NEW_ID",newId);
                      System.out.println("[DETAIL REQ] "+sql);
                      if (dbm.execUpdate(sql)==-1) {
                        String tbName = sql.substring(12,sql.indexOf(" ",12));
                        String sql0 = crTable(tbName);
                        if (sql0!=null)  {
                          System.out.println(sql0);
                          if (dbm.execUpdate(sql0)!=-1) dbm.commit();
                          else System.out.println(sql0);
                          if (dbm.execUpdate(sql)!=-1) continue;
                        }
                        System.out.println("[FAILED]");
                        stat=STATE_ERROR;
                        break;
                      }
                    }
                    if (stat!=STATE_ERROR) System.out.println("[DETAIL SUCCEED]");
                  }
                }
                if (stat==STATE_ERROR) {
                  errSql = sql;
                  break;
                }
              }
            } 
            else if (dbstat==-1) {
              stat=STATE_ERROR;
              errSql=bsql;
            }
          }
          else {
            try {
              fos.write(bsql);
              fos.write("\r\n");
              System.out.println(pNos[i][0]+" write ok");
            } catch(IOException ex) {
              stat=STATE_ERROR;
              errMessage = "書き出し用CSVファイルに書き込めません。";
              break;
            }
          }
          if (stat==STATE_SUCCESS) {
            if (oTable!=null) {
              dbm.commit();
              if (dNum!=null) {
                oTable.removeRows(dNum);
              }
              int nno = (patientNo!=0) ? patientNo:9999999;
              oTable.addRow(iTable.getPatientByPno(pNos[i][0],-nno));
            }
            progressBar.setValue(++lcount);
            progressBar.setString(String.valueOf(lcount)+"/"+count+"件");
          }
          else {
            errMessage = "取り込みデータに問題があります。";
            if (pfile!=null) { dbm.rollback();}
            else { try{ fos.close();} catch(IOException ex){} }
            break;
          }
        }
      }
      if (pfile!=null && oTable==null && stat>STATE_ERROR) dbm.commit();
      else if (pfile!=null) {dbm.rollback();}
      runStat0 = false;
      runStat1 = true;
      if (pfile!=null) {dbm.Close();}
      else { try{ fos.close();} catch(IOException ex){} }
    }

    synchronized public void pause() {
      if (runStat1) return;
      runStat = false;
    }
    synchronized public void restart() {
      runStat0 = true;
      runStat1 = false;
      runStat = true;
      notifyAll(); 
    }
    synchronized public void interruptExec() {
      if (runStat1) return;
      stat = STATE_CANCEL;
      runStat0 = false;
      interrupt();
      runStat = true;
      notifyAll(); 
    }
    private String crTable(String tbName) {
      StringBuffer sb2= new StringBuffer();
      String type;
      if (tbName.matches(".*DATE.*")) {
        type = "TIMESTAMP";
      } else if (tbName.matches(".*TEXT.*")) {
        type = "VARCHAR(600)";
      } else if (tbName.matches(".*INTEGER.*")) {
        type = "INTEGER";
      } else return null;
      
      sb2.append("create table \"");
      sb2.append(tbName);
      sb2.append("\" (\"");
      sb2.append(tbName.substring(0,tbName.indexOf("DETAIL")));
      sb2.append("ID\" INTEGER NOT NULL,");
      sb2.append("\"SYSTEM_BIND_PATH\" INTEGER NOT NULL,");
      sb2.append("\"DETAIL_VALUE\" ");
      sb2.append(type);
      sb2.append(",");
      sb2.append("\"LAST_TIME\" TIMESTAMP,");
      sb2.append("PRIMARY KEY (");
      sb2.append(tbName.substring(0,tbName.indexOf("DETAIL")));
      sb2.append("ID,SYSTEM_BIND_PATH) )");
      return sb2.toString();
    }
}
