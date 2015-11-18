package jp.co.ascsystem.qkan; 

import java.io.*;
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

public class QkanPatientImport {

    public static final int STATE_INFO = 2;
    public static final int STATE_SUCCESS = 0;
    public static final int STATE_CANCEL = -1;
    public static final int STATE_ERROR = -2;
    public static final int STATE_FATAL = -3;
    public static final int STATE_COMPLETE = 1;

    public String propertyFile;
    public boolean propGeted = false;
    public boolean replaceAll = false;
    public boolean isCsv = false;
    public DngAppProperty Props;
    public String dbServer;
    public String dbPath0;
    public String dbPath;
    public String dbPort;
    public QkanPatientSelect iTable,oTable;
    public JFrame parent=null;
    public JDialog fr;
    public JPanel dupName,center0P;
    public int dupNum;
    public Container contentPane;
    public boolean isCalled=false;
    public int runStat=STATE_SUCCESS;
    public boolean vStat = true;
    boolean isMbInPath;

    public QkanPatientImport() {
        propertyFile = getPropertyFile(); 
        dbServer = getProperty("doc/DBConfig/Server");
        dbPath = getProperty("doc/DBConfig/Path");
        dbPort = getProperty("doc/DBConfig/Port");
    }

    public void destroy() {
        fr.dispose();
    }

    public void setParent(JFrame frm) {
        this.parent = frm;
        isCalled = true;
    }

    public QkanPatientSelect getTable(int num) {
      switch(num) {
        case 0: return iTable;
        case 1: return oTable;
        default: return null;
      }
    }

    public Container getPane() {
      return contentPane;
    }

    public JDialog  dbUpdate(JButton execBtn,final QkanExecTransaction dbexec) throws Exception {

      String realInPath=null;
      if (dbPath0==null) {
        fr = (parent!=null) ? new JDialog(parent) : new JDialog();
        fr.setTitle("給管鳥 データユーティリティ");

        if (!checkLocalHost(dbServer)) {
           cancel(); 
        }
        if (!checkDBPath(dbPath)) {
           cancel(); 
        }
        contentPane = fr.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
        boolean kstat = false;
        String uri;
        uri = dbServer + "/" + dbPort + ":" + dbPath;
        oTable = new QkanPatientSelect(uri,getProperty("doc/DBConfig/UserName"),getProperty("doc/DBConfig/Password"));
        oTable.setSelectable(false);
        if (oTable.Rows<0) {
          statMessage(STATE_ERROR,"給管鳥データベースに接続できません。\n給管鳥 が正常に起動する状態かどうかご確認ください。");
          return null;
        }

        while(!kstat) {
          dbPath0 = getImportDBPath(0);
          realInPath = dbPath0;
          if (dbPath0==null) return null;
          if (dbPath0.compareToIgnoreCase(dbPath)==0) {
          statMessage(STATE_ERROR,"取り込み元と取り込み先が同一ファイルです。");
            continue;
          }
          if (dbPath0.matches(".*.csv$") || dbPath0.matches(".*.CSV$") ) {
            isCsv=true;
          }
          if (isMbInPath && ! isCsv) {
            dbPath0 = new File(dbPath).getParent()+"/importwork.fdb";
            try {
            new DngFileUtil().fileCopy(realInPath,dbPath0);
            } catch(Exception err) {
               statMessage(STATE_ERROR,"作業領域の確保ができません。\n取り込み元ファイルを取り込み先ファイルと同じ階層に置いて実行しなおしてみて下さい。");
               return null;
            }
          }
          if (isCsv) {
            iTable = new QkanPatientSelect(dbPath0);
          }
          else {
            uri = dbServer + "/" + dbPort + ":" + dbPath0;
            iTable = new QkanPatientSelect(uri,getProperty("doc/DBConfig/UserName"),getProperty("doc/DBConfig/Password"));
          }

          if (iTable.Rows<0) {
            statMessage(STATE_ERROR,"取り込み元ファイルに接続できません。\nファイルのアクセス権をご確認ください。");
            return null;
          }
          if (iTable.Rows==0) {
            statMessage(STATE_ERROR,"選択したファイルはデータが存在しないか、または、このツールでは取り込み出来ないファイルです。");
            continue;
          }
          kstat = true;
        }

      } 
      else contentPane.removeAll();

        ActionListener exitNow = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            runStat = STATE_COMPLETE;
            if (isMbInPath && ! isCsv ) new File(dbPath0).delete();
            if (isCalled) {
              fr.dispose();
              parent.setEnabled(true);
              parent.setVisible(true);
            }
            else System.exit(0);
            return;
          }
        };

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

        JButton exitBtn = new JButton("終了");
        exitBtn.setFont(new Font("SanSerif",Font.PLAIN,14));
        exitBtn.addActionListener(exitNow);
        JRadioButton appendBtn = new JRadioButton("重複者は取り込まない。",((replaceAll)? false:true));
        appendBtn.setFont(new Font("Dialog",Font.PLAIN,12));
        appendBtn.addActionListener(append);
        JRadioButton replaceBtn = new JRadioButton("重複者は置き換え",replaceAll);
        replaceBtn.setFont(new Font("Dialog",Font.PLAIN,12));
        replaceBtn.addActionListener(replace);
        ButtonGroup bg = new ButtonGroup();
        bg.add(replaceBtn);
        bg.add(appendBtn);
        JPanel rbp = new JPanel(); //new GridLayout(0,1));
        rbp.setBorder(BorderFactory.createLineBorder(Color.black));
        //rbp.setBorder(
        //  BorderFactory.createTitledBorder(
        //  BorderFactory.createLineBorder(Color.black),
        //    "取り込み方法の選択"
        //  )
        //);
        JLabel title = new JLabel(" 給管鳥 利用者"+((isCsv) ? "基本情報CSVファイルの取り込み":"別データの取り込み"));
        title.setFont(new Font("SansSerif",Font.BOLD,18));
        JLabel choi = (isCsv) ? new JLabel("※取り込み先に既存する利用者は保持され、選択されていても取り込まれません。") :
                                new JLabel("取り込み方法の選択：");
        choi.setFont(new Font("Dialog",Font.PLAIN,12));
        rbp.add(choi);
        if (!isCsv) {
          rbp.add(replaceBtn);
          rbp.add(appendBtn);
        }
        JPanel northP = new JPanel(new BorderLayout());
        northP.add(title,BorderLayout.NORTH);
        contentPane.add(northP);
        center0P = new JPanel();
        center0P.add(rbp);
        center0P.add(execBtn);
        center0P.add(exitBtn);
        JPanel centerP = new JPanel();
        JLabel lab0 = new JLabel("  取り込み元データベース："+realInPath);
        lab0.setFont(new Font("Serif",Font.PLAIN,12));
        lab0.setForeground(Color.darkGray);
        northP.add(lab0,BorderLayout.CENTER);
        JLabel lab1 = new JLabel("取り込み元一覧   Ctrl(Shift) + マウスクリックで複数選択可能、全選択はCtrl(Command) + A ");
        lab1.setFont(new Font("Dialog",Font.PLAIN,12));
        centerP.add(lab1);
        contentPane.add(centerP);
        contentPane.add(iTable.getScrollList());
        contentPane.add(center0P);
        JPanel southP = new JPanel(new GridLayout(0,1));
        JLabel lab2 = new JLabel("  取り込み先(現在の給管鳥)データベース："+dbPath);
        lab2.setFont(new Font("Serif",Font.PLAIN,12));
        lab2.setForeground(Color.darkGray);
        southP.add(lab2);
        JLabel lab21 = new JLabel("    ※取り込み先データベースの変更は給管鳥本体の\"データベース設定\"で行って下さい。");
        lab21.setFont(new Font("Dialog",Font.ITALIC,10));
        lab21.setForeground(Color.blue);
        southP.add(lab21);
        contentPane.add(southP);
        JLabel lab3 = new JLabel("取り込み先一覧");
        lab3.setFont(new Font("Dialog",Font.PLAIN,12));
        JPanel pnA = new JPanel();
        pnA.add(lab3);
        contentPane.add(pnA);
        contentPane.add(oTable.getScrollList());
        TableSorter2 iS = iTable.getSorter();
        TableSorter2 oS = oTable.getSorter();
        iS.setSynchroTableSorter(oS);
        oS.setSynchroTableSorter(iS);
        WindowAdapter AppCloser =  new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
            runStat = STATE_COMPLETE;
            if (isMbInPath && ! isCsv ) new File(dbPath0).delete();
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
        //fr.pack();
        fr.setSize(655,610);
        Dimension sc = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension sz = fr.getSize();
        fr.setLocation((sc.width-sz.width)/2,(sc.height-sz.height)/2);
        //fr.setVisible(true);
        return fr;
    }

    public boolean checkDBPath(String path) {
      File dbf = new File(path);
      if (!dbf.exists()) {
          statMessage(STATE_ERROR,"データベースが見つかりません。\n給管鳥を起動して正しくデータベースファイルが設定されているかご確認下さい。");
        return false;
      }
      return true;
    }

    public boolean checkLocalHost(String server) {
        if (!server.equals("localhost") &&
            !server.equals("127.0.0.1")) {
            statMessage(STATE_ERROR,"この操作はデータベースサーバとなっているコンピュータで行って下さい。");
            return false;
        }
        return true;
    }

    public String getImportDBPath(int type) {
      String path = "";
      String ext[] = {"FDB","fdb","old","csv","CSV"};
      String moto = (type==0) ? "取り込み元":"書き出し元";
      try {
        DngFileChooser chooser = new DngFileChooser(fr,moto+"ファイル選択",ext);
        chooser.setTitle(moto+"給管鳥(FDB or CSV)または医見書(CSVのみ)のファイルを指定してください。");
        chooser.setMBPathEnable(true);
        chooser.setInitPath((dbPath0!=null) ? dbPath0:dbPath); 
        File file = chooser.getFile();
        path = file.getPath();
        isMbInPath = chooser.isMbPath;
      } catch(Exception e) {
        if (!isCalled) {
          statMessage(STATE_CANCEL,e.getMessage());
          System.exit(1);
        }
        else return null;
      }
      return path;
    }
   
    public String getProperty(String key)
    {
        String value = "";
        if (!propGeted) {
            Props = new DngAppProperty(propertyFile);
            propGeted = true;
        }
        try
        {
            value = Props.getProperty(key);
            if (value==null) vStat = false;
        }
        catch(Exception ex)
        {
            statMessage(STATE_FATAL,ex.getMessage());
            System.exit(1);
        }
        return value;
    }

    public String getPropertyFile()  {
      String ppath;
      File pf = new File("properity.xml");
      if (!pf.exists()) {
        pf = new File("property.xml");
        if (!pf.exists()) {
          statMessage(STATE_ERROR, "給管鳥の設定ファイルが見つかりません。\n「了解(OK)」を押して終了させ、このプログラムを給管鳥のインストールディレクトリ\nに配置してから実行して下さい。");
          System.exit(1);
        }
      }
      if (pf==null) System.exit(1);
      System.out.println("prop: "+pf.getAbsolutePath());
      return pf.getAbsolutePath();
    }

    public void complete() {
      System.exit(0);
    }

    public void cancel() {
      JOptionPane.showMessageDialog(
        fr,
        "処理を中止します。",
        "データユーティリティ",JOptionPane.INFORMATION_MESSAGE
      );
      System.exit(0);
    }

    public int[][] prepareExec() {
      if (!iTable.isSelected()) return null;
      Object pdat[][] = iTable.getSelectedPatients();
      if (pdat.length<1) return null;
      //String tranSql[] = new String[pdat.length];
      int pNos[][] = new int[pdat.length][];
      dupName = new JPanel(new GridLayout(0,1));
      int dupNum=0;
      for (int i=0;i<pdat.length;i++) {
        Object dat[] = new Object[2];
        dat[0] = pdat[i][2];
        dat[1] = pdat[i][4];
        int patientNo = Integer.parseInt(pdat[i][0].toString());
        int[] pinfo = (oTable!=null) ? oTable.checkDuplicate(dat):null;
        if (pinfo==null) {
           pNos[i] = new int[2];
           pNos[i][0] = patientNo;
           pNos[i][1] = (isCsv) ? -1:0;
        } else if (isCsv || ! replaceAll) {
           if (dupNum==0) {
             pNos[i] = null;
             JLabel l1 = new JLabel("取り込み元の以下の情報が取り込み先と重複しています。");
             JLabel l2 = new JLabel("取り込み先の情報を保持し、以下の情報だけ無視して他を取り込みます。");
             l1.setFont(new Font("Dialog",Font.PLAIN,12));
             l2.setFont(new Font("Dialog",Font.PLAIN,12));
             dupName.add(l1);
             dupName.add(l2);
           }
           if (++dupNum < 10) { 
             JLabel l1 = new JLabel(dat[0].toString());
             l1.setFont(new Font("Dialog",Font.PLAIN,12));
             dupName.add(l1);
           }
           else if(dupNum==10) {
             JLabel l1 = new JLabel("他 多数");
             l1.setFont(new Font("Dialog",Font.PLAIN,12));
             dupName.add(l1);
           }
        } else {
           pNos[i] = new int[pinfo.length+1];
           pNos[i][0] = patientNo;
           for (int j=0;j<pinfo.length;j++) {
             pNos[i][j+1] = pinfo[j];
           }
           if (dupNum==0) {
             JLabel l1 = new JLabel("なお、取り込み方法として\"置き換え\"が選択されています");
             JLabel l2 = new JLabel("取り込み先の以下の情報は取り込み元と重複しているため削除され、");
             JLabel l3 = new JLabel("取り込み元の同一の情報に置き換えられます。");
             l1.setFont(new Font("Dialog",Font.PLAIN,12));
             l2.setFont(new Font("Dialog",Font.PLAIN,12));
             l3.setFont(new Font("Dialog",Font.PLAIN,12));
             dupName.add(l1);
             dupName.add(l2);
             dupName.add(l3);
           }
           if (++dupNum < 10) { 
             JLabel l1 = new JLabel(dat[0].toString());
             l1.setFont(new Font("Dialog",Font.PLAIN,12));
             dupName.add(l1);
           }
           else if(dupNum==10) {
             JLabel l1 = new JLabel("他 多数");
             l1.setFont(new Font("Dialog",Font.PLAIN,12));
             dupName.add(l1);
           }
        }
      }
      if (pdat.length==dupNum && !replaceAll) {
        pNos=null;
        dupName.removeAll();
        dupName.add(new JLabel("選択された利用者は全て、取り込み先と重複しています。"));

      }
      return pNos;
    }

    public void execImport() {
    
      final JProgressBar pb = new JProgressBar();
      final JLabel tit1 = new JLabel("【データ取り込み】");
      final JLabel tit = new JLabel("........DBを更新しています。「キャンセル」を押すとその時点以降の取り込みを中止します。");
      tit.setHorizontalAlignment(JLabel.LEFT);
      int stat = STATE_SUCCESS;

      final QkanExecTransaction dbexec= new QkanExecTransaction(propertyFile,0,pb);
      pb.setStringPainted(true);
      pb.setMinimum(0);

      final JButton sb = new JButton("取り込み");
      sb.setFont(new Font("SanSerif",Font.PLAIN,14));
      final JPanel pn0 = new JPanel();
      final ActionListener actionListener = new ActionListener() {
         public void actionPerformed(ActionEvent e) {
          dbexec.pause();
          if (dbexec.runStat1) return;
          if ( 
            JOptionPane.showConfirmDialog(
              fr,
              "取り込みを中止しますか？\n「はい(Yes)」を押すと現在処理済みの以降の取り込みを中止します。",
              "データ取り込み",JOptionPane.YES_NO_OPTION
            ) == JOptionPane.YES_OPTION
              ) {
            if (dbexec.runStat1) {
              statMessage(STATE_INFO,"既に取り込み処理は全て完了しています。");
              return;
            }
            dbexec.interruptExec();
          }
          else {
            if (dbexec.runStat1) return;
            dbexec.restart();
          }
        }
      };
      ActionListener actionStart = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          int pNos[][] = prepareExec();
          if (pNos!=null && pNos.length>0) {
            dbexec.setPnos(pNos);
            pb.setValue(0);
            pb.setMaximum(pNos.length);
            pb.setString("0/"+String.valueOf(pNos.length)+"件");
            JLabel tit0 = new JLabel(pNos.length+"人の利用者が選択されています。「了解」を押すと処理を開始します。");
            //tit0.setFont(new Font("Dialog",Font.PLAIN,12));
            JPanel pn1 = new JPanel();
            pn1.add(tit0);
            JPanel pn = new JPanel();
            pn.setLayout(new BoxLayout(pn, BoxLayout.PAGE_AXIS));
            pn.add(pn1);
            pn.add(dupName);
            if (JOptionPane.showConfirmDialog(fr,pn,"データ取り込み",JOptionPane.OK_CANCEL_OPTION,JOptionPane.INFORMATION_MESSAGE)==0) {
              center0P.setVisible(false);
              pn0.setVisible(true);
              dbexec.restart();
            }
          }
          else if (dupName!=null) {
            statMessage(STATE_ERROR, "選択された利用者はすべて、取り込み先と重複しています。");
          }
          else {
            statMessage(STATE_ERROR, "利用者が選択されていません。");
          }
        }
      };
      sb.addActionListener(actionStart);

      try {
        if (dbUpdate(sb,dbexec)==null) {
          runStat=STATE_COMPLETE;
          return;
        }
        dbexec.setTable(iTable,oTable);
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
      pn0.add(new JLabel("データの取り込み中......."));
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
          if (runStat==STATE_COMPLETE) {
            if (isMbInPath && ! isCsv ) new File(dbPath0).delete();
            return;
          }
          statMessage(dbexec.stat,dbexec.errMessage);
          runStat = dbexec.stat;
          if (runStat==STATE_ERROR) 
          System.out.println("Error caused by SQL Statements:\n"+dbexec.errSql);
        }
        catch (InterruptedException er) {
          fr.setVisible(false);
        }
        center0P.setVisible(true);
      }
      if (isMbInPath && ! isCsv ) new File(dbPath0).delete();
      return;
    }

  public void statMessage(int stat,String err) {
    String title = "データ取り込み";
    switch (stat) {
      case STATE_INFO:
        JOptionPane.showMessageDialog(
           fr, err, title,
           JOptionPane.INFORMATION_MESSAGE
         ) ;
         break;

      case STATE_SUCCESS:
        JOptionPane.showMessageDialog(
           fr, "取り込み完了しました。", title,
           JOptionPane.INFORMATION_MESSAGE
         ) ;
         break;

       case STATE_CANCEL:
         JOptionPane.showMessageDialog(
           fr,"取り込みを中断しました。",title,
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
    public static void main(String[] args) {
      QkanPatientImport ipi = new QkanPatientImport();
      if (!ipi.vStat) {
        ipi.statMessage(ipi.STATE_FATAL,"正しくないデータベース設定です。給管鳥を起動してデータベースの設定を確認してください。");
        System.exit(1);
      }
      ipi.setParent(null);
      try {
        while(ipi.runStat!=ipi.STATE_FATAL) {
          if (ipi.runStat==ipi.STATE_COMPLETE) {
            ipi.destroy();
            break;
          }
          ipi.execImport();
        }
      }
      catch(Exception ex) {
        ipi.statMessage(ipi.STATE_FATAL,ex.getMessage());
      }
    }
}
