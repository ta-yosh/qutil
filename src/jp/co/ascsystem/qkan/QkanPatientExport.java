package jp.co.ascsystem.qkan;

import java.io.*;
import java.util.Calendar;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JLabel;

import jp.co.ascsystem.lib.*;
import jp.co.ascsystem.util.*;

public class QkanPatientExport extends QkanPatientImport {

    String dbOutPath=null;
    String realOutPath=null;
    String realInPath=null;
    File srcFile=null;
    File destFile=null;
    boolean isMbOutPath;

    public QkanPatientExport() {
        propertyFile = getPropertyFile(); 
        dbServer = getProperty("doc/DBConfig/Server");
        dbPath = getProperty("doc/DBConfig/Path");
        dbPort = getProperty("doc/DBConfig/Port");
    }

    public JDialog  dbUpdate(JButton execBtn,final QkanExecTransaction dbexec) throws Exception {

      realInPath = dbPath;
      if (dbOutPath==null) {
        fr = (parent!=null) ? new JDialog(parent) : new JDialog();
        fr.setTitle("給管鳥 データユーティリティ");

        if (!checkLocalHost(dbServer)) {
           cancel(); 
        }
        contentPane = fr.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        boolean kstat = false;
        while (!kstat) {
          if (!checkDBPath(dbPath)) {
            cancel(); 
          }
          String uri = dbServer + "/" + dbPort + ":" + dbPath;
          iTable = new QkanPatientSelect(uri,getProperty("doc/DBConfig/UserName"),getProperty("doc/DBConfig/Password"));
          if (iTable.Rows<0) {
            statMessage(STATE_ERROR,"データベースに接続できません。\n給管鳥が問題なく起動する状態かどうかご確認ください。");
            return null;
          }
          if (iTable.Rows==0) {
            if ( JOptionPane.showConfirmDialog(
                  fr,
                  "現在設定されているデータベースにはデータが存在しません。\n別のデータベースを選択しますか？",
                  "データ書き出し",JOptionPane.YES_NO_OPTION
                 )==JOptionPane.NO_OPTION) {
                runStat = STATE_COMPLETE;
                fr.dispose();
                dbexec.interrupt();
                return null;
            }
            String origPath = dbPath;
            dbPath = getImportDBPath(1); 
            realInPath = dbPath;
            if (dbPath==null) return null;
            if (isMbInPath) {
              dbPath = new File(origPath).getParent()+"/exportwork.fdb";
              try {
                new DngFileUtil().fileCopy(realInPath,dbPath);
              } catch(Exception err) {
                 statMessage(STATE_ERROR,"作業領域の確保ができません。\n書き出し元ファイルを日本語文字を含まないパスに置いて実行しなおしてみて下さい。");
                 return null;
              }
            }
          }
          else kstat = true;
        }
      } 
      else contentPane.removeAll();

        ActionListener append = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
             replaceAll = false;
          }
        };

        ActionListener replace = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
             replaceAll = true;
          }
        };

      final JButton exitBtn = new JButton("終了");
      exitBtn.setFont(new Font("SanSerif",Font.PLAIN,14));
      final ActionListener exitNow = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          runStat = STATE_COMPLETE;
          if (isMbInPath) new File(dbPath).delete();
          if (isCalled) {
            fr.dispose();
            parent.setEnabled(true);
            parent.setVisible(true);
          }
          else System.exit(0);
          return; 
        }
      };
      exitBtn.addActionListener(exitNow);

        JLabel title = new JLabel(" 給管鳥 利用者別データの書き出し");
        title.setFont(new Font("SansSerif",Font.BOLD,18));
        JPanel northP = new JPanel(new BorderLayout());
        northP.add(title,BorderLayout.NORTH);
        contentPane.add(northP);
        center0P = new JPanel();
        center0P.add(execBtn);
        center0P.add(exitBtn);
        JPanel centerP = new JPanel();
        JLabel dispPath = new JLabel("  現在のデータベース："+realInPath);
        dispPath.setFont(new Font("Serif",Font.PLAIN,12));
        dispPath.setForeground(Color.darkGray);
        northP.add(dispPath,BorderLayout.CENTER);
        JLabel chinf= new JLabel(" ※データベース変更は、給管鳥本体の\"データベース設定\"で行って下さい。");
        chinf.setFont(new Font("Dialog",Font.ITALIC,10));
        chinf.setForeground(Color.blue);
        northP.add(chinf,BorderLayout.SOUTH);
        JLabel lab1 = new JLabel("一覧   Ctrl(Shift) + マウスクリックで複数選択可能、全選択はCtrl(Command) + A");
        lab1.setFont(new Font("Dialog",Font.PLAIN,12));
        centerP.add(lab1);
        contentPane.add(centerP);
        contentPane.add(iTable.getScrollList());
        contentPane.add(center0P);
        WindowAdapter AppCloser =  new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            runStat = STATE_COMPLETE;
            if (isMbInPath) new File(dbPath).delete();
            if (isCalled) {
              fr.dispose();
              parent.setEnabled(true);
              parent.setVisible(true);
            }
            else System.exit(0);
            return; 
          }
        };
        fr.addWindowListener(AppCloser);
        fr.setSize(655,610);
        Dimension sc = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension sz = fr.getSize();
        fr.setLocation((sc.width-sz.width)/2,(sc.height-sz.height)/2);
        return fr;
    }

    public void execExport() {
    
      final JProgressBar pb = new JProgressBar();
      final JLabel tit1 = new JLabel("【データ書き出し】");
      final JLabel tit = new JLabel("データを書き出しています。");
      tit.setHorizontalAlignment(JLabel.LEFT);
      int stat = STATE_SUCCESS;

      final QkanExecTransaction dbexec= new QkanExecTransaction(propertyFile,0,pb);
      pb.setStringPainted(true);
      pb.setMinimum(0);

      final JButton sb = new JButton("書き出し");
      sb.setFont(new Font("SanSerif",Font.PLAIN,14));
      final JPanel pn0 = new JPanel();
      final ActionListener actionListener = new ActionListener() {
         public void actionPerformed(ActionEvent e) {
          dbexec.pause();
          if (dbexec.runStat1) return;
          if ( 
            JOptionPane.showConfirmDialog(
              fr,
              "書き出しを中止しますか？\n「はい」を押すと以降の書き出しを中止し、\n書き出し済みののみのファイルが作成されます。",
              "データ書き出し",JOptionPane.YES_NO_OPTION
            ) == JOptionPane.YES_OPTION
              ) {
            if (dbexec.runStat1) {
              statMessage(STATE_INFO,"既に全ての書き出しが完了しています。");
              return;
            }
            dbexec.interruptExec();
          }
          else  {
            if (dbexec.runStat1) return;
            dbexec.restart();
          }
        }
      };
      ActionListener actionStart = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          int pNos[][] = prepareExec();
          if (pNos!=null && pNos.length>0) {
              pb.setValue(0);
              pb.setString("0/"+String.valueOf(pNos.length)+"件");
              dbOutPath = getExportDBPath(dbOutPath);
              System.out.println("Output to "+dbOutPath);
              if (dbOutPath==null) return;
              if (dbOutPath.compareToIgnoreCase(realInPath)==0) {
                 statMessage(STATE_ERROR,"書き出し元と同一ファイルに書き出す事はできません。\n処理を中止します。");
                dbOutPath=null;
                return;
              }
            dbexec.setPnos(pNos);
            pb.setMaximum(pNos.length);
            JLabel tit0 = new JLabel(pNos.length+"人分のデータ書き出し処理を開始します。");
            tit0.setFont(new Font("Dialog",Font.BOLD,12));
            JPanel pn = new JPanel();
            pn.setLayout(new BoxLayout(pn, BoxLayout.PAGE_AXIS));
            pn.add(tit0);
            pn.add(dupName);
            if (JOptionPane.showConfirmDialog(fr,pn,"データ書き出し",JOptionPane.OK_CANCEL_OPTION,JOptionPane.INFORMATION_MESSAGE)==0) {

              File ofp = new File(dbOutPath);
              //if (ofp.exists()) new DngFileUtil().moveFile(dbOutPath,dbOutPath+".old");
              if (ofp.exists()) ofp.renameTo(new File(dbOutPath+".old"));

              if (isMbOutPath) {
                realOutPath = dbOutPath;
                dbOutPath = new File(dbPath).getParent()+"/qkanwork.fdb";
              }

              try {
                new DngFileUtil().fileCopy(dbPath,dbOutPath);
              } catch(IOException er) {
                er.printStackTrace();
                statMessage(STATE_ERROR,"書き出し用データベースの作成に失敗しました。");
                return;
              }

              if (!initExportDB(dbOutPath)) {
                return;
              } 
              center0P.setVisible(false);
              dbexec.dbOutPath = dbOutPath;
              pn0.setVisible(true);
              dbexec.restart();
            }
          }
          else {
            statMessage(STATE_ERROR,  "が選択されていません。");
          }
        }
      };
      sb.addActionListener(actionStart);

      try {
        if (dbUpdate(sb,dbexec)==null){
          runStat=STATE_COMPLETE;
          return;
        }
        dbexec.setTable(iTable,null);
      } catch (Exception e) {
        System.out.println(e);
        statMessage(STATE_ERROR,"データ一覧の取得失敗");
        runStat = STATE_COMPLETE;
        return;
      }

      final JButton cb = new JButton("キャンセル");
      cb.setFont(new Font("SanSerif",Font.PLAIN,14));
      cb.addActionListener(actionListener);
      pn0.setBackground(Color.white);
      pn0.add(new JLabel("データの書き出し中......."));
      pn0.add(cb);
      contentPane.add(pb);
      contentPane.add(pn0);
      pn0.setVisible(false);
      parent.setEnabled(false);
      fr.setVisible(true);
      while (runStat!=STATE_FATAL) {
        dbexec.pause();
        dbexec.run();
        try {
          dbexec.join();
          pn0.setVisible(false);
          if (dbexec.stat==STATE_SUCCESS || dbexec.stat==STATE_CANCEL) {
            pn0.removeAll();
            pn0.add(new JLabel("書き出し先ファイルの最適化中......."));
            pn0.setVisible(true);
            finalizeExportDB();
            if (isMbOutPath) {
              try {
                //new DngFileUtil().fileCopy(dbOutPath,realOutPath);
                new File(dbOutPath).renameTo(destFile);
                destFile.setReadable(true,false);
                destFile.setWritable(true,false);
              } catch(NullPointerException er) {
                er.printStackTrace();
                statMessage(STATE_ERROR,"書き出し先ファイルの保存に失敗しました。");
                return;
              }
              finally {
                //new File(dbOutPath).delete();
                dbOutPath = realOutPath;
              }
            }
            pn0.setVisible(false);
            pn0.removeAll();
            pn0.add(new JLabel("データの書き出し中......."));
            pn0.add(cb);
          }
          //System.out.println("runStat = "+runStat);
          if (runStat==STATE_COMPLETE) { 
            if (isMbInPath) new File(dbPath).delete();
            return;
          }
          statMessage(dbexec.stat,dbexec.errMessage);
          //if (dbexec.errSql!=null) System.out.println(dbexec.errSql);
          runStat = dbexec.stat;
          //System.out.println("dbexec.stat = "+runStat+" dbexec.runStat0= "+dbexec.runStat0);
        }
        catch (InterruptedException er) {
          fr.setVisible(false);
        }
        center0P.setVisible(true);
      }
      if (isMbInPath) new File(dbPath).delete();
      return;
    }

  public void statMessage(int stat,String err) {
    String title = "利用者データ書き出し";
    switch (stat) {
      case STATE_INFO:
        JOptionPane.showMessageDialog(
           fr, err, title,
           JOptionPane.INFORMATION_MESSAGE
         ) ;
         break;

      case STATE_SUCCESS:
        JOptionPane.showMessageDialog(
           fr, "書き出し完了しました。", title,
           JOptionPane.INFORMATION_MESSAGE
         ) ;
         break;

       case STATE_CANCEL:
         JOptionPane.showMessageDialog(
           fr,"書き出しを途中で中断しました。以降の処理は中止します。",title,
           JOptionPane.INFORMATION_MESSAGE
         );
         break;

       case STATE_ERROR:
         JOptionPane.showMessageDialog(
           fr,err, title,
           JOptionPane.ERROR_MESSAGE
         );
         break;

       case STATE_FATAL:
         JPanel pn1 = new JPanel(new BorderLayout());
         pn1.add(new JLabel("実行継続不能エラー"),BorderLayout.NORTH);
         pn1.add(new JLabel(err),BorderLayout.SOUTH);
         JOptionPane.showMessageDialog(
           fr,pn1, title,
           JOptionPane.ERROR_MESSAGE
         );
         break;
    }
  }

  private String getExportDBPath(String outPath) {
    String path = null;
    String ext[] = {"fdb"};
    String fname = null; 
    if (outPath==null) outPath=realInPath;
    else fname = (new File(outPath)).getName();

    try {
      if (fname==null) {
        Calendar c = Calendar.getInstance();
        StringBuffer sb = new StringBuffer();
        sb.append("QKANUSER");
        sb.append(c.get(c.YEAR));
        String mm = (new Integer(c.get(c.MONTH)+1)).toString();
        if ((c.get(c.MONTH)+1)<10) sb.append("0");
        sb.append(mm);
        String dd = (new Integer(c.get(c.DATE))).toString();
        if (c.get(c.DATE)<10) sb.append("0");
        sb.append(dd);
        sb.append(".fdb");
        fname = sb.toString();
      }
      DngFileChooser chooser = new DngFileChooser(fr,"FDB file for Qkan",ext);
      chooser.setTitle("書き出し用FDBファイルの保存場所を指定して下さい。");
      chooser.setMBPathEnable(true);
      File file = chooser.saveFile(outPath,fname);
      destFile = file;
      path = file.getPath();
      isMbOutPath = chooser.isMbPath;
    } catch(Exception e) {
      if (!isCalled) {
        statMessage(STATE_CANCEL,e.getMessage());
        System.exit(1);
      }
      else return null;
    }
    return path;
  }


    public static void main(String[] args) {
        QkanPatientExport ipi = new QkanPatientExport();
        try {
           //while(ipi.runStat!=STATE_FATAL) {
           //  if (ipi.runStat==STATE_COMPLETE) System.exit(0);
             ipi.execExport();
           //}
           System.exit(0);
        }
        catch(Exception e) {
          ipi.statMessage(STATE_FATAL,e.getMessage());
          System.exit(1);
        }
    }

/*
    public boolean initExportDB(String path0,String path) {
       File f = new File(path);
       if (f.exists()) f.delete();

       String initSql[] = {"CREATE TABLE \"CLAIM\" ( \"CLAIM_ID\"  INTEGER NOT NULL, \"CLAIM_STYLE_TYPE\"  INTEGER, \"CATEGORY_NO\"  INTEGER, \"PATIENT_ID\"  INTEGER, \"INSURED_ID\"  VARCHAR(10), \"TARGET_DATE\"  DATE, \"CLAIM_DATE\"  DATE, \"INSURER_ID\"  VARCHAR(6), \"PROVIDER_ID\"  VARCHAR(10), \"CLAIM_FINISH_FLAG\"  INTEGER, \"LAST_TIME\"  TIMESTAMP, PRIMARY KEY (\"CLAIM_ID\"))",
                  "CREATE TABLE \"CLAIM_PATIENT_DETAIL\" ( \"CLAIM_ID\"  INTEGER NOT NULL, \"SELF_SERVICE_NO1\"  VARCHAR(75), \"SELF_PAY_NO1\"  INTEGER, \"SELF_SERVICE_NO2\"  VARCHAR(75), \"SELF_PAY_NO2\"  INTEGER, \"SELF_SERVICE_NO3\"  VARCHAR(75), \"SELF_PAY_NO3\"  INTEGER, \"OTHER_HIMOKU_NO1\"  VARCHAR(75), \"OTHER_PAY_NO1\"  INTEGER, \"OTHER_HIMOKU_NO2\"  VARCHAR(75), \"OTHER_PAY_NO2\"  INTEGER, \"OTHER_HIMOKU_NO3\"  VARCHAR(75), \"OTHER_PAY_NO3\"  INTEGER, \"OTHER_HIMOKU_NO4\"  VARCHAR(75), \"OTHER_PAY_NO4\"  INTEGER, \"OTHER_HIMOKU_NO5\"  VARCHAR(75), \"OTHER_PAY_NO5\"  INTEGER, \"KOJO_TARGET\"  INTEGER, \"LAST_TIME\"  TIMESTAMP, \"INNER_TAX\"  INTEGER, PRIMARY KEY (\"CLAIM_ID\"))",
                  "CREATE TABLE \"CLAIM_PATIENT_MEDICAL\" ( \"CLAIM_PATIENT_MEDICAL_ID\"  INTEGER NOT NULL, \"PATIENT_ID\"  INTEGER, \"PROVIDER_ID\"  VARCHAR(10), \"BILL_SPAN_START\"  TIMESTAMP, \"BILL_SPAN_END\"  TIMESTAMP, \"BILL_PRINTED\"  INTEGER, \"BILL_NO\"  INTEGER, \"BILL_PATIENT_CODE\"  VARCHAR(15), \"BILL_PRINT_DATE\"  TIMESTAMP, \"BY_PATIENT_RATE\"  INTEGER, \"SELF_FLAG\"  INTEGER, \"BILL_INSURE_TYPE\"  VARCHAR(40), \"BILL_TAX\"  DOUBLE PRECISION, \"BY_INSURER_NAME1\"  VARCHAR(20), \"BY_INSURER_PRICE1\"  INTEGER, \"BY_INSURER_NUMBER1\"  INTEGER, \"BY_INSURER_SUM1\"  INTEGER, \"BY_INSURER_NAME2\"  VARCHAR(20), \"BY_INSURER_PRICE2\"  INTEGER, \"BY_INSURER_NUMBER2\"  INTEGER, \"BY_INSURER_SUM2\"  INTEGER, \"BY_INSURER_NAME3\"  VARCHAR(20), \"BY_INSURER_PRICE3\"  INTEGER, \"BY_INSURER_NUMBER3\"  INTEGER, \"BY_INSURER_SUM3\"  INTEGER, \"BY_INSURER_NAME4\"  VARCHAR(20), \"BY_INSURER_PRICE4\"  INTEGER, \"BY_INSURER_NUMBER4\"  INTEGER, \"BY_INSURER_SUM4\"  INTEGER, \"BY_INSURER_NAME5\"  VARCHAR(20), \"BY_INSURER_PRICE5\"  INTEGER, \"BY_INSURER_NUMBER5\"  INTEGER, \"BY_INSURER_SUM5\"  INTEGER, \"BY_INSURER_NAME6\"  VARCHAR(20), \"BY_INSURER_PRICE6\"  INTEGER, \"BY_INSURER_NUMBER6\"  INTEGER, \"BY_INSURER_SUM6\"  INTEGER, \"BY_INSURER_NAME7\"  VARCHAR(20), \"BY_INSURER_PRICE7\"  INTEGER, \"BY_INSURER_NUMBER7\"  INTEGER, \"BY_INSURER_SUM7\"  INTEGER, \"BY_INSURER_NAME8\"  VARCHAR(20), \"BY_INSURER_PRICE8\"  INTEGER, \"BY_INSURER_NUMBER8\"  INTEGER, \"BY_INSURER_SUM8\"  INTEGER, \"BY_INSURER_NAME9\"  VARCHAR(20), \"BY_INSURER_PRICE9\"  INTEGER, \"BY_INSURER_NUMBER9\"  INTEGER, \"BY_INSURER_SUM9\"  INTEGER, \"BY_INSURER_NAME10\"  VARCHAR(20), \"BY_INSURER_PRICE10\"  INTEGER, \"BY_INSURER_NUMBER10\"  INTEGER, \"BY_INSURER_SUM10\"  INTEGER, \"BY_INSURER_NAME11\"  VARCHAR(20), \"BY_INSURER_PRICE11\"  INTEGER, \"BY_INSURER_NUMBER11\"  INTEGER, \"BY_INSURER_SUM11\"  INTEGER, \"BY_INSURER_NAME12\"  VARCHAR(20), \"BY_INSURER_PRICE12\"  INTEGER, \"BY_INSURER_NUMBER12\"  INTEGER, \"BY_INSURER_SUM12\"  INTEGER, \"BY_INSURER_NAME13\"  VARCHAR(20), \"BY_INSURER_PRICE13\"  INTEGER, \"BY_INSURER_NUMBER13\"  INTEGER, \"BY_INSURER_SUM13\"  INTEGER, \"BY_INSURER_NAME14\"  VARCHAR(20), \"BY_INSURER_PRICE14\"  INTEGER, \"BY_INSURER_NUMBER14\"  INTEGER, \"BY_INSURER_SUM14\"  INTEGER, \"BY_INSURER_NAME15\"  VARCHAR(20), \"BY_INSURER_PRICE15\"  INTEGER, \"BY_INSURER_NUMBER15\"  INTEGER, \"BY_INSURER_SUM15\"  INTEGER, \"BY_INSURER_NAME16\"  VARCHAR(20), \"BY_INSURER_PRICE16\"  INTEGER, \"BY_INSURER_NUMBER16\"  INTEGER, \"BY_INSURER_SUM16\"  INTEGER, \"BY_INSURER_NAME17\"  VARCHAR(20), \"BY_INSURER_PRICE17\"  INTEGER, \"BY_INSURER_NUMBER17\"  INTEGER, \"BY_INSURER_SUM17\"  INTEGER, \"BY_INSURER_NAME18\"  VARCHAR(20), \"BY_INSURER_PRICE18\"  INTEGER, \"BY_INSURER_NUMBER18\"  INTEGER, \"BY_INSURER_SUM18\"  INTEGER, \"BY_INSURER_NAME19\"  VARCHAR(20), \"BY_INSURER_PRICE19\"  INTEGER, \"BY_INSURER_NUMBER19\"  INTEGER, \"BY_INSURER_SUM19\"  INTEGER, \"BY_INSURER_NAME20\"  VARCHAR(20), \"BY_INSURER_PRICE20\"  INTEGER, \"BY_INSURER_NUMBER20\"  INTEGER, \"BY_INSURER_SUM20\"  INTEGER, \"BY_PATIENT_NAME1\"  VARCHAR(20), \"BY_PATIENT_PRICE1\"  INTEGER, \"BY_PATIENT_NUMBER1\"  INTEGER, \"BY_PATIENT_SUM1\"  INTEGER, \"BY_PATIENT_USE_TAX1\"  INTEGER, \"BY_PATIENT_TAX1\"  INTEGER, \"BY_PATIENT_NAME2\"  VARCHAR(20), \"BY_PATIENT_PRICE2\"  INTEGER, \"BY_PATIENT_NUMBER2\"  INTEGER, \"BY_PATIENT_SUM2\"  INTEGER, \"BY_PATIENT_USE_TAX2\"  INTEGER, \"BY_PATIENT_TAX2\"  INTEGER, \"BY_PATIENT_NAME3\"  VARCHAR(20), \"BY_PATIENT_PRICE3\"  INTEGER, \"BY_PATIENT_NUMBER3\"  INTEGER, \"BY_PATIENT_SUM3\"  INTEGER, \"BY_PATIENT_USE_TAX3\"  INTEGER, \"BY_PATIENT_TAX3\"  INTEGER, \"BY_PATIENT_NAME4\"  VARCHAR(20), \"BY_PATIENT_PRICE4\"  INTEGER, \"BY_PATIENT_NUMBER4\"  INTEGER, \"BY_PATIENT_SUM4\"  INTEGER, \"BY_PATIENT_USE_TAX4\"  INTEGER, \"BY_PATIENT_TAX4\"  INTEGER, \"BY_PATIENT_NAME5\"  VARCHAR(20), \"BY_PATIENT_PRICE5\"  INTEGER, \"BY_PATIENT_NUMBER5\"  INTEGER, \"BY_PATIENT_SUM5\"  INTEGER, \"BY_PATIENT_USE_TAX5\"  INTEGER, \"BY_PATIENT_TAX5\"  INTEGER, \"BY_PATIENT_NAME6\"  VARCHAR(20), \"BY_PATIENT_PRICE6\"  INTEGER, \"BY_PATIENT_NUMBER6\"  INTEGER, \"BY_PATIENT_SUM6\"  INTEGER, \"BY_PATIENT_USE_TAX6\"  INTEGER, \"BY_PATIENT_TAX6\"  INTEGER, \"BY_PATIENT_NAME7\"  VARCHAR(20), \"BY_PATIENT_PRICE7\"  INTEGER, \"BY_PATIENT_NUMBER7\"  INTEGER, \"BY_PATIENT_SUM7\"  INTEGER, \"BY_PATIENT_USE_TAX7\"  INTEGER, \"BY_PATIENT_TAX7\"  INTEGER, \"BY_PATIENT_NAME8\"  VARCHAR(20), \"BY_PATIENT_PRICE8\"  INTEGER, \"BY_PATIENT_NUMBER8\"  INTEGER, \"BY_PATIENT_SUM8\"  INTEGER, \"BY_PATIENT_USE_TAX8\"  INTEGER, \"BY_PATIENT_TAX8\"  INTEGER, \"BY_PATIENT_NAME9\"  VARCHAR(20), \"BY_PATIENT_PRICE9\"  INTEGER, \"BY_PATIENT_NUMBER9\"  INTEGER, \"BY_PATIENT_SUM9\"  INTEGER, \"BY_PATIENT_USE_TAX9\"  INTEGER, \"BY_PATIENT_TAX9\"  INTEGER, \"BY_PATIENT_NAME10\"  VARCHAR(20), \"BY_PATIENT_PRICE10\"  INTEGER, \"BY_PATIENT_NUMBER10\"  INTEGER, \"BY_PATIENT_SUM10\"  INTEGER, \"BY_PATIENT_USE_TAX10\"  INTEGER, \"BY_PATIENT_TAX10\"  INTEGER, \"BY_PATIENT_NAME11\"  VARCHAR(20), \"BY_PATIENT_PRICE11\"  INTEGER, \"BY_PATIENT_NUMBER11\"  INTEGER, \"BY_PATIENT_SUM11\"  INTEGER, \"BY_PATIENT_USE_TAX11\"  INTEGER, \"BY_PATIENT_TAX11\"  INTEGER, \"BY_PATIENT_NAME12\"  VARCHAR(20), \"BY_PATIENT_PRICE12\"  INTEGER, \"BY_PATIENT_NUMBER12\"  INTEGER, \"BY_PATIENT_SUM12\"  INTEGER, \"BY_PATIENT_USE_TAX12\"  INTEGER, \"BY_PATIENT_TAX12\"  INTEGER, \"BY_PATIENT_NAME13\"  VARCHAR(20), \"BY_PATIENT_PRICE13\"  INTEGER, \"BY_PATIENT_NUMBER13\"  INTEGER, \"BY_PATIENT_SUM13\"  INTEGER, \"BY_PATIENT_USE_TAX13\"  INTEGER, \"BY_PATIENT_TAX13\"  INTEGER, \"BY_PATIENT_NAME14\"  VARCHAR(20), \"BY_PATIENT_PRICE14\"  INTEGER, \"BY_PATIENT_NUMBER14\"  INTEGER, \"BY_PATIENT_SUM14\"  INTEGER, \"BY_PATIENT_USE_TAX14\"  INTEGER, \"BY_PATIENT_TAX14\"  INTEGER, \"BY_PATIENT_NAME15\"  VARCHAR(20), \"BY_PATIENT_PRICE15\"  INTEGER, \"BY_PATIENT_NUMBER15\"  INTEGER, \"BY_PATIENT_SUM15\"  INTEGER, \"BY_PATIENT_USE_TAX15\"  INTEGER, \"BY_PATIENT_TAX15\"  INTEGER, \"BY_PATIENT_NAME16\"  VARCHAR(20), \"BY_PATIENT_PRICE16\"  INTEGER, \"BY_PATIENT_NUMBER16\"  INTEGER, \"BY_PATIENT_SUM16\"  INTEGER, \"BY_PATIENT_USE_TAX16\"  INTEGER, \"BY_PATIENT_TAX16\"  INTEGER, \"BY_PATIENT_NAME17\"  VARCHAR(20), \"BY_PATIENT_PRICE17\"  INTEGER, \"BY_PATIENT_NUMBER17\"  INTEGER, \"BY_PATIENT_SUM17\"  INTEGER, \"BY_PATIENT_USE_TAX17\"  INTEGER, \"BY_PATIENT_TAX17\"  INTEGER, \"BY_PATIENT_NAME18\"  VARCHAR(20), \"BY_PATIENT_PRICE18\"  INTEGER, \"BY_PATIENT_NUMBER18\"  INTEGER, \"BY_PATIENT_SUM18\"  INTEGER, \"BY_PATIENT_USE_TAX18\"  INTEGER, \"BY_PATIENT_TAX18\"  INTEGER, \"BY_PATIENT_NAME19\"  VARCHAR(20), \"BY_PATIENT_PRICE19\"  INTEGER, \"BY_PATIENT_NUMBER19\"  INTEGER, \"BY_PATIENT_SUM19\"  INTEGER, \"BY_PATIENT_USE_TAX19\"  INTEGER, \"BY_PATIENT_TAX19\"  INTEGER, \"BY_PATIENT_NAME20\"  VARCHAR(20), \"BY_PATIENT_PRICE20\"  INTEGER, \"BY_PATIENT_NUMBER20\"  INTEGER, \"BY_PATIENT_SUM20\"  INTEGER, \"BY_PATIENT_USE_TAX20\"  INTEGER, \"BY_PATIENT_TAX20\"  INTEGER, \"PROVIDE_DAY_1\"  INTEGER, \"PROVIDE_DAY_2\"  INTEGER, \"PROVIDE_DAY_3\"  INTEGER, \"PROVIDE_DAY_4\"  INTEGER, \"PROVIDE_DAY_5\"  INTEGER, \"PROVIDE_DAY_6\"  INTEGER, \"PROVIDE_DAY_7\"  INTEGER, \"PROVIDE_DAY_8\"  INTEGER, \"PROVIDE_DAY_9\"  INTEGER, \"PROVIDE_DAY_10\"  INTEGER, \"PROVIDE_DAY_11\"  INTEGER, \"PROVIDE_DAY_12\"  INTEGER, \"PROVIDE_DAY_13\"  INTEGER, \"PROVIDE_DAY_14\"  INTEGER, \"PROVIDE_DAY_15\"  INTEGER, \"PROVIDE_DAY_16\"  INTEGER, \"PROVIDE_DAY_17\"  INTEGER, \"PROVIDE_DAY_18\"  INTEGER, \"PROVIDE_DAY_19\"  INTEGER, \"PROVIDE_DAY_20\"  INTEGER, \"PROVIDE_DAY_21\"  INTEGER, \"PROVIDE_DAY_22\"  INTEGER, \"PROVIDE_DAY_23\"  INTEGER, \"PROVIDE_DAY_24\"  INTEGER, \"PROVIDE_DAY_25\"  INTEGER, \"PROVIDE_DAY_26\"  INTEGER, \"PROVIDE_DAY_27\"  INTEGER, \"PROVIDE_DAY_28\"  INTEGER, \"PROVIDE_DAY_29\"  INTEGER, \"PROVIDE_DAY_30\"  INTEGER, \"PROVIDE_DAY_31\"  INTEGER, \"BILL_NOTE\"  VARCHAR(210), \"BILL_NO_TAX_BY_INSURER\"  INTEGER, \"BILL_NO_TAX_BY_PATIENT\"  INTEGER, \"BILL_IN_TAX_BY_INSURER\"  INTEGER, \"BILL_IN_TAX_BY_PATIENT\"  INTEGER, \"BILL_FULL_TOTAL\"  INTEGER, \"LAST_TIME\"  TIMESTAMP, \"BY_PATIENT_TAX_TARGET1\"  INTEGER, \"BY_PATIENT_TAX_TARGET2\"  INTEGER, \"BY_PATIENT_TAX_TARGET3\"  INTEGER, \"BY_PATIENT_TAX_TARGET4\"  INTEGER, \"BY_PATIENT_TAX_TARGET5\"  INTEGER, \"BY_PATIENT_TAX_TARGET6\"  INTEGER, \"BY_PATIENT_TAX_TARGET7\"  INTEGER, \"BY_PATIENT_TAX_TARGET8\"  INTEGER, \"BY_PATIENT_TAX_TARGET9\"  INTEGER, \"BY_PATIENT_TAX_TARGET10\"  INTEGER, \"BY_PATIENT_TAX_TARGET11\"  INTEGER, \"BY_PATIENT_TAX_TARGET12\"  INTEGER, \"BY_PATIENT_TAX_TARGET13\"  INTEGER, \"BY_PATIENT_TAX_TARGET14\"  INTEGER, \"BY_PATIENT_TAX_TARGET15\"  INTEGER, \"BY_PATIENT_TAX_TARGET16\"  INTEGER, \"BY_PATIENT_TAX_TARGET17\"  INTEGER, \"BY_PATIENT_TAX_TARGET18\"  INTEGER, \"BY_PATIENT_TAX_TARGET19\"  INTEGER, \"BY_PATIENT_TAX_TARGET20\"  INTEGER, PRIMARY KEY (\"CLAIM_PATIENT_MEDICAL_ID\"))",
                  "CREATE TABLE \"HOMONKANGO_JOHO_TEIKYOSHO\" ( \"PATIENT_ID\"  INTEGER NOT NULL, \"TARGET_DATE\"  DATE NOT NULL, \"CREATE_DATE\"  DATE, \"JOHO_TEIKYO_SAKI\"  VARCHAR(32), \"JOTAI_CODE\"  INTEGER, \"DISEASE\"  VARCHAR(70), \"PROVIDER_ADDRESS\"  VARCHAR(70), \"PROVIDER_NAME\"  VARCHAR(64), \"PROVIDER_TEL_FIRST\"  VARCHAR(6), \"PROVIDER_TEL_SECOND\"  VARCHAR(4), \"PROVIDER_TEL_THIRD\"  VARCHAR(4), \"PROVIDER_ADMINISTRATOR\"  VARCHAR(38), \"PATIENT_JOB\"  VARCHAR(10), \"DOCTOR_NAME\"  VARCHAR(32), \"DOCTOR_ADDRESS\"  VARCHAR(70), \"ADL_MOVE\"  INTEGER, \"ADL_EXCRETION\"  INTEGER, \"ADL_CHANGE_CLOTH\"  INTEGER, \"ADL_FOOD\"  INTEGER, \"ADL_BATH\"  INTEGER, \"ADL_COSMETIC\"  INTEGER, \"BYOJO_SHOGAI_STATE\"  VARCHAR(150), \"HOMON_KAISU_DAY\"  INTEGER, \"HOMON_KAISU_COUNT\"  INTEGER, \"KANGO_NAIYO\"  VARCHAR(150), \"HOKEN_FUKUSHI_SERVICE\"  VARCHAR(150), \"RYUIJIKO\"  VARCHAR(150), \"LAST_TIME\"  TIMESTAMP, PRIMARY KEY (\"PATIENT_ID\", \"TARGET_DATE\"))",
                  "CREATE TABLE \"HOMONKANGO_KIROKUSHO\" ( \"PATIENT_ID\"  INTEGER NOT NULL, \"TARGET_DATE\"  DATE NOT NULL, \"NURSE_NAME\"  VARCHAR(32), \"FIRST_VISIT_DATE\"  DATE, \"FIRST_VISIT_DATE_START\"  TIME, \"FIRST_VISIT_DATE_END\"  TIME, \"DISEASE\"  VARCHAR(200), \"BYOJO_CHIYU_JOTAI\"  VARCHAR(350), \"GENBYOREKI\"  VARCHAR(350), \"KIOREKI\"  VARCHAR(350), \"SEIKATSUREKI\"  VARCHAR(350), \"FAMILY_NAME1\"  VARCHAR(22), \"FAMILY_AGE1\"  INTEGER, \"FAMILY_RELATION1\"  VARCHAR(6), \"FAMILY_JOB1\"  VARCHAR(12), \"FAMILY_COMMENT1\"  VARCHAR(30), \"FAMILY_NAME2\"  VARCHAR(22), \"FAMILY_AGE2\"  INTEGER, \"FAMILY_RELATION2\"  VARCHAR(6), \"FAMILY_JOB2\"  VARCHAR(12), \"FAMILY_COMMENT2\"  VARCHAR(30), \"FAMILY_NAME3\"  VARCHAR(22), \"FAMILY_AGE3\"  INTEGER, \"FAMILY_RELATION3\"  VARCHAR(6), \"FAMILY_JOB3\"  VARCHAR(12), \"FAMILY_COMMENT3\"  VARCHAR(30), \"FAMILY_NAME4\"  VARCHAR(22), \"FAMILY_AGE4\"  INTEGER, \"FAMILY_RELATION4\"  VARCHAR(6), \"FAMILY_JOB4\"  VARCHAR(12), \"FAMILY_COMMENT4\"  VARCHAR(30), \"FAMILY_NAME5\"  VARCHAR(22), \"FAMILY_AGE5\"  INTEGER, \"FAMILY_RELATION5\"  VARCHAR(6), \"FAMILY_JOB5\"  VARCHAR(12), \"FAMILY_COMMENT5\"  VARCHAR(30), \"FAMILY_NAME6\"  VARCHAR(22), \"FAMILY_AGE6\"  INTEGER, \"FAMILY_RELATION6\"  VARCHAR(6), \"FAMILY_JOB6\"  VARCHAR(12), \"FAMILY_COMMENT6\"  VARCHAR(30), \"CAREGIVER\"  VARCHAR(200), \"HOUSE\"  VARCHAR(200), \"PURPOSE\"  VARCHAR(300), \"DOCTOR_NAME\"  VARCHAR(32), \"MEDICAL_FACILITY_NAME\"  VARCHAR(64), \"MEDICAL_FACILITY_ADDRESS\"  VARCHAR(150), \"MEDICAL_FACILITY_TEL_FIRST\"  VARCHAR(6), \"MEDICAL_FACILITY_TEL_SECOND\"  VARCHAR(4), \"MEDICAL_FACILITY_TEL_THIRD\"  VARCHAR(4), \"ADL_MOVE\"  INTEGER, \"ADL_FOOD\"  INTEGER, \"ADL_EXCRETION\"  INTEGER, \"ADL_BATH\"  INTEGER, \"ADL_CHANGE_CLOTH\"  INTEGER, \"ADL_COSMETIC\"  INTEGER, \"ADL_COMMUNICATION\"  INTEGER, \"DOCTOR_RENRAKUSAKI\"  VARCHAR(450), \"MEDICAL_RENRAKUSAKI1\"  VARCHAR(20), \"MEDICAL_PREPARED1\"  VARCHAR(12), \"MEDICAL_NOTE1\"  VARCHAR(38), \"MEDICAL_RENRAKUSAKI2\"  VARCHAR(20), \"MEDICAL_PREPARED2\"  VARCHAR(12), \"MEDICAL_NOTE2\"  VARCHAR(38), \"MEDICAL_RENRAKUSAKI3\"  VARCHAR(20), \"MEDICAL_PREPARED3\"  VARCHAR(12), \"MEDICAL_NOTE3\"  VARCHAR(38), \"MEDICAL_RENRAKUSAKI4\"  VARCHAR(20), \"MEDICAL_PREPARED4\"  VARCHAR(12), \"MEDICAL_NOTE4\"  VARCHAR(38), \"USED_STATE\"  VARCHAR(350), \"LAST_TIME\"  TIMESTAMP, PRIMARY KEY (\"PATIENT_ID\", \"TARGET_DATE\"))",
                  "CREATE TABLE \"HOMONKANGO_PLAN\" ( \"PATIENT_ID\"  INTEGER NOT NULL, \"TARGET_DATE\"  DATE NOT NULL, \"JOTAI_CODE\"  INTEGER, \"DOCTOR_NAME\"  VARCHAR(32), \"CREATE_DATE\"  DATE, \"PROVIDER_NAME\"  VARCHAR(64), \"ADMINISTRATOR_NAME\"  VARCHAR(22), \"PLAN_PURPOSE\"  VARCHAR(650), \"PLAN_COMMENT\"  VARCHAR(250), \"LAST_TIME\"  TIMESTAMP, PRIMARY KEY (\"PATIENT_ID\", \"TARGET_DATE\"))",
                  "CREATE TABLE \"HOMONKANGO_PLAN_NOTE\" ( \"PATIENT_ID\"  INTEGER NOT NULL, \"NOTE_ID\"  INTEGER NOT NULL, \"NOTE_DATE\"  DATE, \"NOTE_COMMENT\"  VARCHAR(3100), \"LAST_TIME\"  TIMESTAMP, PRIMARY KEY (\"PATIENT_ID\", \"NOTE_ID\"))",
                  "CREATE TABLE \"HOMONKANGO_RESULT\" ( \"PATIENT_ID\"  INTEGER NOT NULL, \"TARGET_DATE\"  DATE NOT NULL, \"JOTAI_CODE\"  INTEGER, \"DOCTOR_NAME\"  VARCHAR(32), \"CREATE_DATE\"  DATE, \"PROVIDER_NAME\"  VARCHAR(64), \"ADMINISTRATOR_NAME\"  VARCHAR(22), \"BYOJO_STATE\"  VARCHAR(350), \"KANGO_REHA_NAIYO\"  VARCHAR(450), \"RYOYO_KAIGO_STATE\"  VARCHAR(400), \"REPORT_COMMENT\"  VARCHAR(300), \"LAST_TIME\"  TIMESTAMP, PRIMARY KEY (\"PATIENT_ID\", \"TARGET_DATE\"))",
                  "CREATE TABLE \"HOMONKANGO_RESULT_CALENDAR\" ( \"PATIENT_ID\"  INTEGER NOT NULL, \"VISIT_DATE\"  DATE NOT NULL, \"VISIT_VALUE\"  INTEGER, \"LAST_TIME\"  TIMESTAMP, PRIMARY KEY (\"PATIENT_ID\", \"VISIT_DATE\"))",
                  "CREATE TABLE \"KYOTAKU_RYOYO\" ( \"PATIENT_ID\"  INTEGER NOT NULL, \"TARGET_DATE\"  DATE NOT NULL, \"JOTAI_CODE\"  INTEGER, \"SHOGAI_JIRITSUDO\"  INTEGER, \"NINCHISHO_JIRITSUDO\"  INTEGER, \"PATIENT_ADDRESS\"  VARCHAR(200), \"PATIENT_TEL_FIRST\"  VARCHAR(6), \"PATIENT_TEL_SECOND\"  VARCHAR(4), \"PATIENT_TEL_THIRD\"  VARCHAR(4), \"CREATE_DATE_ZAITAKU\"  DATE, \"VISIT_THIS_MONTH_NO1\"  DATE, \"VISIT_THIS_MONTH_NO2\"  DATE, \"VISIT_THIS_MONTH_NO3\"  DATE, \"VISIT_THIS_MONTH_NO4\"  DATE, \"VISIT_THIS_MONTH_NO5\"  DATE, \"VISIT_THIS_MONTH_NO6\"  DATE, \"VISIT_NEXT_MONTH_NO1\"  DATE, \"VISIT_NEXT_MONTH_NO2\"  DATE, \"VISIT_NEXT_MONTH_NO3\"  DATE, \"VISIT_NEXT_MONTH_NO4\"  DATE, \"VISIT_NEXT_MONTH_NO5\"  DATE, \"VISIT_NEXT_MONTH_NO6\"  DATE, \"ADVICE_MONTH\"  INTEGER, \"ADVICE\"  VARCHAR(1000), \"MEDICAL_FACILITY_NAME\"  VARCHAR(64), \"DOCTOR_NAME\"  VARCHAR(32), \"MEDICAL_FACILITY_ADDRESS\"  VARCHAR(150), \"MEDICAL_FACILITY_TEL_FIRST\"  VARCHAR(6), \"MEDICAL_FACILITY_TEL_SECOND\"  VARCHAR(4), \"MEDICAL_FACILITY_TEL_THIRD\"  VARCHAR(4), \"SENMONIN\"  VARCHAR(32), \"PROVIDER_NAME\"  VARCHAR(64), \"CREATE_DATE_KYOTAKU\"  DATE, \"CONDITION\"  VARCHAR(250), \"CONDITION_PASSAGE\"  VARCHAR(250), \"NOTE_OF_KAIGO_SERVICE\"  VARCHAR(250), \"NOTE_OF_DAILY_LIFE\"  VARCHAR(250), \"REMARKS\"  VARCHAR(250), \"LAST_TIME\"  TIMESTAMP, PRIMARY KEY (\"PATIENT_ID\", \"TARGET_DATE\"))",
                  "CREATE TABLE \"PATIENT\" ( \"PATIENT_ID\"  INTEGER NOT NULL, \"PATIENT_CODE\"  VARCHAR(16), \"PATIENT_FAMILY_NAME\"  VARCHAR(16), \"PATIENT_FIRST_NAME\"  VARCHAR(16), \"PATIENT_FAMILY_KANA\"  VARCHAR(16), \"PATIENT_FIRST_KANA\"  VARCHAR(16), \"PATIENT_SEX\"  INTEGER, \"PATIENT_BIRTHDAY\"  DATE, \"PATIENT_TEL_FIRST\"  VARCHAR(6), \"PATIENT_TEL_SECOND\"  VARCHAR(4), \"PATIENT_TEL_THIRD\"  VARCHAR(4), \"PATIENT_ZIP_FIRST\"  VARCHAR(3), \"PATIENT_ZIP_SECOND\"  VARCHAR(4), \"PATIENT_ADDRESS\"  VARCHAR(64), \"SHOW_FLAG\"  INTEGER, \"BELONG_TYPE\"  INTEGER, \"AREA_TYPE\"  INTEGER, \"DELETE_FLAG\"  INTEGER, \"LAST_TIME\"  TIMESTAMP, PRIMARY KEY (\"PATIENT_ID\"))",
                  "CREATE TABLE \"PATIENT_CHANGES_HISTORY\" ( \"PATIENT_ID\"  INTEGER NOT NULL, \"CHANGES_HISTORY_ID\"  INTEGER NOT NULL, \"SYSTEM_SERVICE_KIND_DETAIL\"  INTEGER, \"CHANGES_CONTENT\"  INTEGER, \"CHANGES_DATE\"  DATE, \"CHANGES_TIME\"  TIMESTAMP, \"CHANGES_REASON\"  INTEGER, \"REASON_MEMO\"  VARCHAR(50), \"LAST_TIME\"  TIMESTAMP, PRIMARY KEY (\"PATIENT_ID\", \"CHANGES_HISTORY_ID\"))",
                  "CREATE TABLE \"PATIENT_KOHI\" ( \"PATIENT_ID\"  INTEGER NOT NULL, \"KOHI_ID\"  INTEGER NOT NULL, \"INSURE_TYPE\"  INTEGER, \"KOHI_TYPE\"  INTEGER, \"BENEFIT_RATE\"  INTEGER, \"KOHI_LAW_NO\"  VARCHAR(2), \"INSURER_ID\"  VARCHAR(6), \"KOHI_RECIPIENT_NO\"  VARCHAR(7), \"KOHI_VALID_START\"  DATE, \"KOHI_VALID_END\"  DATE, \"SELF_PAY\"  INTEGER, \"LAST_TIME\"  TIMESTAMP, PRIMARY KEY (\"PATIENT_ID\", \"KOHI_ID\"))",
                  "CREATE TABLE \"PATIENT_KOHI_SERVICE\" ( \"PATIENT_ID\"  INTEGER NOT NULL, \"KOHI_ID\"  INTEGER NOT NULL, \"SYSTEM_SERVICE_KIND_DETAIL\"  INTEGER NOT NULL, \"LAST_TIME\"  TIMESTAMP, PRIMARY KEY (\"PATIENT_ID\", \"KOHI_ID\", \"SYSTEM_SERVICE_KIND_DETAIL\"))",
                  "CREATE TABLE \"PATIENT_MEDICAL_HISTORY\" ( \"PATIENT_ID\"  INTEGER NOT NULL, \"MEDICAL_HISTORY_ID\"  INTEGER NOT NULL, \"INSURE_TYPE\"  INTEGER, \"OLD_FLAG\"  INTEGER, \"SELF_FLAG\"  INTEGER, \"OLD_RATE_FLAG\"  INTEGER, \"MEDICAL_LAW_NO\"  VARCHAR(2), \"MEDICAL_INSURER_ID\"  VARCHAR(6), \"MEDICAL_INSURE_ID\"  VARCHAR(16), \"MEDICAL_VALID_START\"  DATE, \"MEDICAL_VALID_END\"  DATE, \"BENEFIT_RATE\"  INTEGER, \"CITY_LAW_NO\"  VARCHAR(2), \"CITY_INSURER_ID\"  VARCHAR(6), \"OLD_RECIPIENT_ID\"  VARCHAR(7), \"LAST_TIME\"  TIMESTAMP, PRIMARY KEY (\"PATIENT_ID\", \"MEDICAL_HISTORY_ID\"))",
                  "CREATE TABLE \"PATIENT_NINTEI_HISTORY\" ( \"PATIENT_ID\"  INTEGER NOT NULL, \"NINTEI_HISTORY_ID\"  INTEGER NOT NULL, \"INSURER_ID\"  VARCHAR(6), \"INSURED_ID\"  VARCHAR(10), \"INSURE_RATE\"  INTEGER, \"PLANNER\"  INTEGER, \"PROVIDER_ID\"  VARCHAR(10), \"SHUBETSU_CODE\"  INTEGER, \"CHANGE_CODE\"  INTEGER, \"JOTAI_CODE\"  INTEGER, \"SHINSEI_DATE\"  DATE, \"NINTEI_DATE\"  DATE, \"INSURE_VALID_START\"  DATE, \"INSURE_VALID_END\"  DATE, \"STOP_DATE\"  DATE, \"STOP_REASON\"  INTEGER, \"REPORTED_DATE\"  DATE, \"LIMIT_RATE\"  INTEGER, \"EXTERNAL_USE_LIMIT\"  INTEGER, \"LAST_TIME\"  TIMESTAMP, PRIMARY KEY (\"PATIENT_ID\", \"NINTEI_HISTORY_ID\"))",
                  "CREATE TABLE \"PATIENT_SHISETSU_HISTORY\" ( \"PATIENT_ID\"  INTEGER NOT NULL, \"SHISETSU_HISTORY_ID\"  INTEGER NOT NULL, \"TOKUTEI_NYUSHO_FLAG\"  INTEGER, \"LIMIT_SHOKUHI\"  INTEGER, \"LIMIT_UNIT_KOSHITSU\"  INTEGER, \"LIMIT_UNIT_JUNKOSHITSU\"  INTEGER, \"LIMIT_JURAIGATA1\"  INTEGER, \"LIMIT_JURAIGATA2\"  INTEGER, \"LIMIT_TASHOSHITSU\"  INTEGER, \"KYUSOCHI_FLAG\"  INTEGER, \"DISEASE\"  VARCHAR(64), \"LAST_TIME\"  TIMESTAMP, PRIMARY KEY (\"PATIENT_ID\", \"SHISETSU_HISTORY_ID\"))",
                  "CREATE TABLE \"PATIENT_STATION_HISTORY\" ( \"PATIENT_ID\"  INTEGER NOT NULL, \"STATION_HISTORY_ID\"  INTEGER NOT NULL, \"BODY_STATE\"  VARCHAR(200), \"DISEASE\"  VARCHAR(200), \"SPECIAL_SHIPPEI\"  INTEGER, \"SHOKUMU_JIYU\"  INTEGER, \"SPECIAL_AREA_ADD\"  INTEGER, \"HOMON_TIME\"  VARCHAR(64), \"SHIJISHO_VALID_START\"  DATE, \"SHIJISHO_VALID_END\"  DATE, \"SPECIAL_SHIJISHO_VALID_START\"  DATE, \"SPECIAL_SHIJISHO_VALID_END\"  DATE, \"MEDICAL_FACILITY_ID\"  INTEGER, \"DOCTOR_NAME\"  VARCHAR(32), \"LAST_TIME\"  TIMESTAMP, PRIMARY KEY (\"PATIENT_ID\", \"STATION_HISTORY_ID\"))",
                  "CREATE TABLE \"SERVICE\" ( \"SERVICE_ID\"  INTEGER NOT NULL, \"SERVICE_USE_TYPE\"  INTEGER, \"PATIENT_ID\"  INTEGER, \"PROVIDER_ID\"  VARCHAR(10), \"SYSTEM_SERVICE_KIND_DETAIL\"  INTEGER, \"SERVICE_DATE\"  DATE, \"WEEK_DAY\"  INTEGER, \"REGULATION_RATE\"  INTEGER, \"LAST_TIME\"  TIMESTAMP, PRIMARY KEY (\"SERVICE_ID\"))",
                  "CREATE TABLE \"SERVICE_PASSIVE_CHECK\" ( \"PATIENT_ID\"  INTEGER NOT NULL, \"TARGET_DATE\"  DATE NOT NULL, \"CHECK_TYPE\"  INTEGER NOT NULL, \"LAST_TIME\"  TIMESTAMP, PRIMARY KEY (\"PATIENT_ID\", \"TARGET_DATE\", \"CHECK_TYPE\"))"};

        String dbUser = getProperty("doc/DBConfig/UserName");
        String dbPass = getProperty("doc/DBConfig/Password");
        String dbUri = dbServer+"/"+dbPort+":"+path0;
        //Calendar c =  Calendar.getInstance();
        //int nextYear = c.get(c.YEAR)+1; 
        DngDBAccess dbm = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
        dbm.execUpdate("CREATE DATABASE 'locahost/3050:"+path+"' USER '"+dbUser+"' PASSWORD '"+dbPass+"'");
        System.out.println("CREATE DATABASE 'locahost/3050:"+path+"' USER '"+dbUser+"' PASSWORD '"+dbPass+"'");
        dbm.commit();
        dbm.Close();
        dbUri = dbServer+"/"+dbPort+":"+path;
        dbm = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
        if (!dbm.connect()) {
          statMessage(STATE_ERROR,"データベースに接続できません。\nDB:"+dbUri);
          return false;
        }
        for (int i=0;i<initSql.length;i++) {
          dbm.execUpdate(initSql[i]);
        }
        dbm.Close();
        return false;
    }

*/

    public boolean initExportDB(String path) {
        String tables[] = {"FIXED_FORM","INSURER","INSURER_LIMIT_RATE"
                         ,"INSURER_LIMIT_RATE_DETAIL"
                         ,"MEDICAL_FACILITY","M_AFFAIR_INFO","M_AREA_UNIT_PRICE"
                         ,"M_CODE","M_DETAIL","M_DETAIL_COMMENT"
                         ,"M_DETAIL_CONTROL","M_FIXED_FORM_GROUP","M_KOHI"
                         ,"M_KOHI_SERVICE","M_LIMIT_RATE","M_LIMIT_RATE_DETAIL"
                         ,"M_MENU","M_NO_CONTROL","M_PARAMETER","M_POST"
                         ,"M_QKAN_VERSION","M_RESIDENCE_FOOD_COST","M_RYOYOHI"
                         ,"M_SERVICE","M_SERVICE_CODE","M_SPECIAL_CLINIC"
                         ,"M_SJ_SERVICE_CODE","M_SJ_SERVICE_CODE_HISTORY"
                         ,"PROVIDER_MENU","PROVIDER_SERVICE"
                         ,"PROVIDER_SERVICE_DETAIL_DATE"
                         ,"PROVIDER_SERVICE_DETAIL_INTEGER"
                         ,"PROVIDER_SERVICE_DETAIL_TEXT"
                         ,"SERVICE_DETAIL_DATE"
                         ,"SERVICE_DETAIL_INTEGER","SERVICE_DETAIL_TEXT"
                         ,"STAFF","TAX","CLAIM_DETAIL_DATE"
                         ,"CLAIM_DETAIL_INTEGER","CLAIM_DETAIL_TEXT"};
        String pTable[] = {"PATIENT","PATIENT_CHANGES_HISTORY","PATIENT_KOHI",
                           "PATIENT_KOHI_SERVICE","PATIENT_MEDICAL_HISTORY",
                           "PATIENT_NINTEI_HISTORY","PATIENT_SHISETSU_HISTORY",
                     "PATIENT_STATION_HISTORY","SERVICE","SERVICE_PASSIVE_CHECK"
                    ,"HOMONKANGO_JOHO_TEIKYOSHO","HOMONKANGO_KIROKUSHO"
                    ,"HOMONKANGO_PLAN","HOMONKANGO_PLAN_NOTE"
                    ,"HOMONKANGO_RESULT","HOMONKANGO_RESULT_CALENDAR"
                    ,"KYOTAKU_RYOYO","CLAIM","CLAIM_PATIENT_DETAIL"
                    ,"CLAIM_PATIENT_MEDICAL"};
        String dbUser = getProperty("doc/DBConfig/UserName");
        String dbPass = getProperty("doc/DBConfig/Password");
        String dbUri = dbServer+"/"+dbPort+":"+path;
        //Calendar c =  Calendar.getInstance();
        //int nextYear = c.get(c.YEAR)+1; 
        DngDBAccess dbm = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
        if (!dbm.connect()) {
          statMessage(STATE_ERROR,"書き出し用データベースに接続できません。\nDB:"+dbUri);
          return false;
        }
        String sql;
        for(int i=0;i<tables.length;i++) {
          sql = "drop table "+tables[i];        
          System.out.println(sql);
          dbm.execUpdate(sql);
          if (tables[i].matches("^[^M][^_]*_DETAIL.*")) {
            int minYear = (tables[i].matches("^S.*")) ? iTable.sdMinYear:iTable.cdMinYear;
            int maxYear = (tables[i].matches("^S.*")) ? iTable.sdMaxYear:iTable.cdMaxYear;

            for (int y=minYear;y<=maxYear;y++) {
              sql = "delete from "+tables[i]+"_"+y;        
              System.out.println(sql);
              try {dbm.execUpdate(sql);} catch(Exception e){};
            }
            sql = "delete from "+tables[i]+"_2006";        
            System.out.println(sql);
            try {dbm.execUpdate(sql);} catch(Exception e){};
          }
        }
        for(int i=0;i<pTable.length;i++) {
          sql = "delete from "+pTable[i];        
          System.out.println(sql);
          dbm.execUpdate(sql);
        }
        dbm.Close();
        dbOutPath = path;
        
        return true;
    }
 
    public void finalizeExportDB() {

      boolean is20 = false;
      String dbUser = getProperty("doc/DBConfig/UserName");
      String dbPass = getProperty("doc/DBConfig/Password");
      String dbTmpPath = dbOutPath+".fbak";
      String[] envp= new String[1];
      String gbak;

      String cmd[] = new String[8];
      String quot = "";
      String rmc = null;   
      String osn = System.getProperty("os.name").substring(0,3);

      if (osn.equals("Mac")) {
         cmd[0] = "/Library/Frameworks/Firebird.framework/Versions/A/Resources/bin/gbak";
      }
      else { 
        Process process;
        try {
          if (osn.equals("Win")) process = Runtime.getRuntime().exec("cmd.exe /c ECHO %ProgramFiles%");
          else process = Runtime.getRuntime().exec("which gbak");
          InputStream is = process.getInputStream();
          BufferedReader br = new BufferedReader(new InputStreamReader(is));
          cmd[0] = br.readLine();
        } catch (Exception e) {
          return;
        }
        System.out.println(cmd[0]);
        if (cmd[0].equals(null)) return;
      }
      if (osn.equals("Win")) {
        quot = "\"";
        gbak = cmd[0]+"\\Firebird\\Firebird_1_5\\bin\\gbak.exe";
        File gf = new File(gbak);
        if (! gf.exists()) {
          gbak = cmd[0]+"\\Firebird\\Firebird_2_0\\bin\\gbak.exe";
          gf = new File(gbak);
          if (! gf.exists()) {
            gbak = cmd[0]+"\\Firebird\\Firebird_2_1\\bin\\gbak.exe";
            gf = new File(gbak);
            if (! gf.exists()) {
              gbak = cmd[0]+"\\Firebird\\Firebird_2_5\\bin\\gbak.exe";
              gf = new File(gbak);
            }
          }
          is20 = true;
        }
        cmd[0] = quot+gbak+quot;
      //  rmc = "cmd.exe /c del "+quot+dbTmpPath+quot;
      }
     // else rmc = "rm "+dbTmpPath;
      
      cmd[1] = "-b";
      cmd[2] = "-user";
      cmd[3] = dbUser;
      cmd[4] = "-pass";
      cmd[5] = dbPass;
      cmd[6] = quot+dbServer+":"+dbOutPath+quot;
      cmd[7] = quot+dbTmpPath+quot;

      try {
          Runtime runtime = Runtime.getRuntime();
          Process process = runtime.exec(cmd,null);
          //InputStream is = process.getInputStream();
          //BufferedReader br = new BufferedReader(new InputStreamReader(is));
          //String line;
          //while((line=br.readLine())!=null) {
          //  System.out.println(line);
          //}
          int tmpI = process.waitFor();
          if (tmpI==0) {
             //if (is20) {
             //  gbak = cmd[0];
             //  cmd = new String[9];
             //  cmd[0] = gbak;
             //  cmd[1] = "-r";
             //  cmd[2] = "-REP";
             //  cmd[3] = "-user";
             //  cmd[4] = dbUser;
             //  cmd[5] = "-pass";
             //  cmd[6] = dbPass;
             //  cmd[7] = quot+dbTmpPath+quot;
             //  cmd[8] = quot+dbOutPath+quot;
             //} else {
               System.out.println("gbak -b end.");
               cmd[1] = "-rep";
               cmd[6] = quot+dbTmpPath+quot;
               cmd[7] = quot+dbServer+":"+dbOutPath+quot;
             //}
             process = runtime.exec(cmd,null);
             tmpI = process.waitFor();
             if (tmpI==0) {
               System.out.println("gbak -rep complete.");
             }
             if (osn.equals("Mac")) new DngFileUtil().chOwn("$USER","firebird",cmd[7]);
             if (!osn.equals("Win")) new DngFileUtil().chMod("660",cmd[7]);
             //new File(dbTmpPath).delete();
             //process = runtime.exec(rmc);
             //tmpI = process.waitFor();
          }
      } catch (Exception e) {
          System.out.println(e.toString());
      }
    }
}

