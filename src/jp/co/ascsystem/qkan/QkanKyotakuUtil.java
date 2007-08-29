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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JLabel;

import jp.co.ascsystem.lib.*;
import jp.co.ascsystem.util.*;

public class QkanKyotakuUtil extends QkanPatientImport {

  String dbOutPath=null;
  String realOutPath=null;
  String realInPath=null;
  boolean isMbOutPath;
  public JComboBox ymbox;
  public JLabel nodata;

  QkanKyotakuData tTable;

  public QkanKyotakuUtil() {
    propertyFile = getPropertyFile(); 
    dbServer = getProperty("DBConfig/Server");
    dbPath = getProperty("DBConfig/Path");
    dbPort = getProperty("DBConfig/Port");
  }

  public JDialog dbUpdate(JButton execBtn,final QkanExecTransaction dbexec) throws Exception {

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
        tTable = new QkanKyotakuData(uri,getProperty("DBConfig/UserName"),getProperty("DBConfig/Password"));
        if (tTable.Rows<0) {
          statMessage(STATE_ERROR,"データベースに接続できません。\n給管鳥が問題なく起動する状態かどうかご確認ください。");
          return null;
        }
        if (tTable.Rows==0) {
          if ( JOptionPane.showConfirmDialog(
                fr,
                "現在設定されているデータベースにはデータが存在しません。\n別のデータベースを選択しますか？",
                "居宅療養管理指導情報CSV",JOptionPane.YES_NO_OPTION
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

    JLabel title = new JLabel(" 給管鳥 居宅療養管理指導情報");
    title.setFont(new Font("SansSerif",Font.BOLD,18));
    JLabel dispPath = new JLabel("  現在のデータベース："+realInPath);
    dispPath.setFont(new Font("Serif",Font.PLAIN,12));
    dispPath.setForeground(Color.darkGray);
    JLabel chinf= new JLabel(" ※データベース変更は、給管鳥本体の\"データベース設定\"で行って下さい。");
    chinf.setFont(new Font("Dialog",Font.ITALIC,10));
    chinf.setForeground(Color.blue);
    JPanel northP = new JPanel( new BorderLayout());
    northP.add(title,BorderLayout.NORTH);
    northP.add(dispPath,BorderLayout.CENTER);
    northP.add(chinf,BorderLayout.SOUTH);
    contentPane.add(northP);
    center0P = new JPanel();
    center0P.add(execBtn);
    center0P.add(exitBtn);
    final JPanel seleP = new JPanel(new BorderLayout());
    JPanel spn = tTable.searchCondition();
    seleP.add(spn,BorderLayout.CENTER);
    contentPane.add(seleP);
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
    fr.setSize(720,650);
    //fr.pack();
    Dimension sc = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension sz = fr.getSize();
    fr.setLocation((sc.width-sz.width)/2,(sc.height-sz.height)/2);
    System.out.println("fr OK");
    return fr;
  }

  public void execCsvOut() {
  
    final JProgressBar pb = new JProgressBar();
    final JLabel tit1 = new JLabel("【居宅療養管理指導情報CSV書き出し】");
    final JLabel tit = new JLabel("情報をCSVファイルに書き出しています。");
    tit.setHorizontalAlignment(JLabel.LEFT);
    int stat = STATE_SUCCESS;

    final QkanExecTransaction dbexec= new QkanExecTransaction(0,pb);
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
          tTable.selectAll();
          int[][] pNos = prepareExec();
          if (pNos==null) {
            statMessage(STATE_ERROR,  "データが有りません");
            return;
          }
          pb.setValue(0);
          pb.setString("0/"+String.valueOf(pNos.length)+"件");
          dbOutPath = getExportDBPath(dbOutPath);
          if (dbOutPath==null) return;
          if (dbOutPath.compareToIgnoreCase(realInPath)==0) {
             statMessage(STATE_ERROR,"書き出し元と同一ファイルに書き出す事はできません。\n処理を中止します。");
            dbOutPath=null;
            return;
          }
          dbexec.setPnos(pNos);
          pb.setMaximum(pNos.length);
          JLabel tit0 = new JLabel("CSVファイルへの書き出し処理を開始します。");
          tit0.setFont(new Font("Dialog",Font.BOLD,12));
          JPanel pn = new JPanel();
          pn.setLayout(new BoxLayout(pn, BoxLayout.PAGE_AXIS));
          pn.add(tit0);
          if (JOptionPane.showConfirmDialog(fr,pn,"CSV書き出し",JOptionPane.OK_CANCEL_OPTION,JOptionPane.INFORMATION_MESSAGE)==0) {

            File ofp = new File(dbOutPath);
            center0P.setVisible(false);
            dbexec.dbOutPath = dbOutPath;
            pn0.setVisible(true);
            dbexec.restart();
          }
      }
    };
    sb.addActionListener(actionStart);

    try {
      if (dbUpdate(sb,dbexec)==null){
        runStat=STATE_COMPLETE;
        return;
      }
      dbexec.setTable(tTable);
    } catch (Exception e) {
      statMessage(STATE_ERROR,"データ一覧の取得失敗");
      return;
    }

    final JButton cb = new JButton("キャンセル");
    cb.setFont(new Font("SanSerif",Font.PLAIN,14));
    cb.addActionListener(actionListener);
    pn0.setBackground(Color.white);
    pn0.add(new JLabel("データ書き出し中......."));
    pn0.add(cb);
    contentPane.add(pb);
    contentPane.add(pn0);
    pn0.setVisible(false);
    if (parent != null) parent.setEnabled(false);
    System.out.println("frame start "+runStat);
    fr.setVisible(true);

    while (runStat!=STATE_FATAL) {
      dbexec.pause();
      dbexec.run();
      try {
        dbexec.join();
        pn0.setVisible(false);
        if (dbexec.stat==STATE_SUCCESS || dbexec.stat==STATE_CANCEL) {
          pn0.setVisible(false);
          pn0.removeAll();
          pn0.add(new JLabel("データ書き出し中......."));
          pn0.add(cb);
        }
        System.out.println("runStat = "+runStat);
        if (runStat==STATE_COMPLETE) { 
          if (isMbInPath) new File(dbPath).delete();
          return;
        }
        statMessage(dbexec.stat,dbexec.errMessage);
        runStat = dbexec.stat;
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
    String title = "CSV書き出し";
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

  public int[][] prepareExec() {
    Object pdat[][] = tTable.getSelectedPatients();
    if (pdat.length<1) return null;
    int pNos[][] = new int[pdat.length][2];
    for (int i=0;i<pdat.length;i++) {
      System.out.println(pdat[i][0]);
      int patientNo = Integer.parseInt(pdat[i][0].toString());
      pNos[i][0] = patientNo;
      pNos[i][1] = -1;
    }
    return pNos;
  }


  private String getExportDBPath(String outPath) {
    String path = null;
    String ext[] = {"csv","CSV"};
    String fname = null; 
    System.out.println(tTable.targetMonth+":"+tTable.targetDay);
    if (outPath==null) outPath=realInPath;
    else fname = (new File(outPath)).getName();

    try {
      if (fname==null) {
        Calendar c = Calendar.getInstance();
        StringBuffer sb = new StringBuffer();
        sb.append("SHIDOU-");
        sb.append(tTable.currentProvider);
        sb.append("_");
        sb.append(tTable.targetYear);
        if (tTable.targetMonth<10) sb.append("0");
        sb.append(tTable.targetMonth);
        if (tTable.targetDay>0) {
          if (tTable.targetDay<10) sb.append("0");
          sb.append(tTable.targetDay);
        }
        //sb.append(c.get(c.YEAR));
        //String mm = (new Integer(c.get(c.MONTH)+1)).toString();
        //if ((c.get(c.MONTH)+1)<10) sb.append("0");
        //sb.append(mm);
        //String dd = (new Integer(c.get(c.DATE))).toString();
        //if (c.get(c.DATE)<10) sb.append("0");
        //sb.append(dd);
        sb.append(".csv");
        fname = sb.toString();
      }
      DngFileChooser chooser = new DngFileChooser(fr,"CSV file",ext);
      chooser.setTitle("書き出し用CSVファイルの保存場所を指定して下さい。");
      chooser.setMBPathEnable(true);
      File file = chooser.saveFile(outPath,fname);
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
    QkanKyotakuUtil ipi = new QkanKyotakuUtil();
    try {
      ipi.execCsvOut();
      //System.exit(0);
      //JDialog fr = ipi.dbUpdate(null,null);
      //fr.setVisible(true);
    }
    catch(Exception e) {
      ipi.statMessage(STATE_FATAL,e.getMessage());
      System.exit(1);
    }
  }
}
