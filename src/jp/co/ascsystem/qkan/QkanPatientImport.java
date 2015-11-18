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
        fr.setTitle("���Ļ �ǡ����桼�ƥ���ƥ�");

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
          statMessage(STATE_ERROR,"���Ļ�ǡ����١�������³�Ǥ��ޤ���\n���Ļ ������˵�ư������֤��ɤ�������ǧ����������");
          return null;
        }

        while(!kstat) {
          dbPath0 = getImportDBPath(0);
          realInPath = dbPath0;
          if (dbPath0==null) return null;
          if (dbPath0.compareToIgnoreCase(dbPath)==0) {
          statMessage(STATE_ERROR,"�����߸��ȼ������褬Ʊ��ե�����Ǥ���");
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
               statMessage(STATE_ERROR,"����ΰ�γ��ݤ��Ǥ��ޤ���\n�����߸��ե�������������ե������Ʊ�����ؤ��֤��Ƽ¹Ԥ��ʤ����ƤߤƲ�������");
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
            statMessage(STATE_ERROR,"�����߸��ե��������³�Ǥ��ޤ���\n�ե�����Υ����������򤴳�ǧ����������");
            return null;
          }
          if (iTable.Rows==0) {
            statMessage(STATE_ERROR,"���򤷤��ե�����ϥǡ�����¸�ߤ��ʤ������ޤ��ϡ����Υġ���Ǥϼ����߽���ʤ��ե�����Ǥ���");
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

        JButton exitBtn = new JButton("��λ");
        exitBtn.setFont(new Font("SanSerif",Font.PLAIN,14));
        exitBtn.addActionListener(exitNow);
        JRadioButton appendBtn = new JRadioButton("��ʣ�Ԥϼ����ޤʤ���",((replaceAll)? false:true));
        appendBtn.setFont(new Font("Dialog",Font.PLAIN,12));
        appendBtn.addActionListener(append);
        JRadioButton replaceBtn = new JRadioButton("��ʣ�Ԥ��֤�����",replaceAll);
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
        //    "��������ˡ������"
        //  )
        //);
        JLabel title = new JLabel(" ���Ļ ���Ѽ�"+((isCsv) ? "���ܾ���CSV�ե�����μ�����":"�̥ǡ����μ�����"));
        title.setFont(new Font("SansSerif",Font.BOLD,18));
        JLabel choi = (isCsv) ? new JLabel("����������˴�¸�������ѼԤ��ݻ����졢���򤵤�Ƥ��Ƥ�����ޤ�ޤ���") :
                                new JLabel("��������ˡ������");
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
        JLabel lab0 = new JLabel("  �����߸��ǡ����١�����"+realInPath);
        lab0.setFont(new Font("Serif",Font.PLAIN,12));
        lab0.setForeground(Color.darkGray);
        northP.add(lab0,BorderLayout.CENTER);
        JLabel lab1 = new JLabel("�����߸�����   Ctrl(Shift) + �ޥ�������å���ʣ�������ǽ���������Ctrl(Command) + A ");
        lab1.setFont(new Font("Dialog",Font.PLAIN,12));
        centerP.add(lab1);
        contentPane.add(centerP);
        contentPane.add(iTable.getScrollList());
        contentPane.add(center0P);
        JPanel southP = new JPanel(new GridLayout(0,1));
        JLabel lab2 = new JLabel("  ��������(���ߤε��Ļ)�ǡ����١�����"+dbPath);
        lab2.setFont(new Font("Serif",Font.PLAIN,12));
        lab2.setForeground(Color.darkGray);
        southP.add(lab2);
        JLabel lab21 = new JLabel("    ����������ǡ����١������ѹ��ϵ��Ļ���Τ�\"�ǡ����١�������\"�ǹԤäƲ�������");
        lab21.setFont(new Font("Dialog",Font.ITALIC,10));
        lab21.setForeground(Color.blue);
        southP.add(lab21);
        contentPane.add(southP);
        JLabel lab3 = new JLabel("�����������");
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
          statMessage(STATE_ERROR,"�ǡ����١��������Ĥ���ޤ���\n���Ļ��ư�����������ǡ����١����ե����뤬���ꤵ��Ƥ��뤫����ǧ��������");
        return false;
      }
      return true;
    }

    public boolean checkLocalHost(String server) {
        if (!server.equals("localhost") &&
            !server.equals("127.0.0.1")) {
            statMessage(STATE_ERROR,"�������ϥǡ����١��������ФȤʤäƤ��륳��ԥ塼���ǹԤäƲ�������");
            return false;
        }
        return true;
    }

    public String getImportDBPath(int type) {
      String path = "";
      String ext[] = {"FDB","fdb","old","csv","CSV"};
      String moto = (type==0) ? "�����߸�":"�񤭽Ф���";
      try {
        DngFileChooser chooser = new DngFileChooser(fr,moto+"�ե���������",ext);
        chooser.setTitle(moto+"���Ļ(FDB or CSV)�ޤ��ϰ師��(CSV�Τ�)�Υե��������ꤷ�Ƥ���������");
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
          statMessage(STATE_ERROR, "���Ļ������ե����뤬���Ĥ���ޤ���\n��λ��(OK)�פ򲡤��ƽ�λ���������Υץ�������Ļ�Υ��󥹥ȡ���ǥ��쥯�ȥ�\n�����֤��Ƥ���¹Ԥ��Ʋ�������");
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
        "��������ߤ��ޤ���",
        "�ǡ����桼�ƥ���ƥ�",JOptionPane.INFORMATION_MESSAGE
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
             JLabel l1 = new JLabel("�����߸��ΰʲ��ξ��󤬼�������Ƚ�ʣ���Ƥ��ޤ���");
             JLabel l2 = new JLabel("��������ξ�����ݻ������ʲ��ξ������̵�뤷��¾������ߤޤ���");
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
             JLabel l1 = new JLabel("¾ ¿��");
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
             JLabel l1 = new JLabel("�ʤ�����������ˡ�Ȥ���\"�֤�����\"�����򤵤�Ƥ��ޤ�");
             JLabel l2 = new JLabel("��������ΰʲ��ξ���ϼ����߸��Ƚ�ʣ���Ƥ��뤿�������졢");
             JLabel l3 = new JLabel("�����߸���Ʊ��ξ�����֤��������ޤ���");
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
             JLabel l1 = new JLabel("¾ ¿��");
             l1.setFont(new Font("Dialog",Font.PLAIN,12));
             dupName.add(l1);
           }
        }
      }
      if (pdat.length==dupNum && !replaceAll) {
        pNos=null;
        dupName.removeAll();
        dupName.add(new JLabel("���򤵤줿���ѼԤ����ơ���������Ƚ�ʣ���Ƥ��ޤ���"));

      }
      return pNos;
    }

    public void execImport() {
    
      final JProgressBar pb = new JProgressBar();
      final JLabel tit1 = new JLabel("�ڥǡ��������ߡ�");
      final JLabel tit = new JLabel("........DB�򹹿����Ƥ��ޤ����֥���󥻥�פ򲡤��Ȥ��λ����ʹߤμ����ߤ���ߤ��ޤ���");
      tit.setHorizontalAlignment(JLabel.LEFT);
      int stat = STATE_SUCCESS;

      final QkanExecTransaction dbexec= new QkanExecTransaction(propertyFile,0,pb);
      pb.setStringPainted(true);
      pb.setMinimum(0);

      final JButton sb = new JButton("������");
      sb.setFont(new Font("SanSerif",Font.PLAIN,14));
      final JPanel pn0 = new JPanel();
      final ActionListener actionListener = new ActionListener() {
         public void actionPerformed(ActionEvent e) {
          dbexec.pause();
          if (dbexec.runStat1) return;
          if ( 
            JOptionPane.showConfirmDialog(
              fr,
              "�����ߤ���ߤ��ޤ�����\n�֤Ϥ�(Yes)�פ򲡤��ȸ��߽����Ѥߤΰʹߤμ����ߤ���ߤ��ޤ���",
              "�ǡ���������",JOptionPane.YES_NO_OPTION
            ) == JOptionPane.YES_OPTION
              ) {
            if (dbexec.runStat1) {
              statMessage(STATE_INFO,"���˼����߽��������ƴ�λ���Ƥ��ޤ���");
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
            pb.setString("0/"+String.valueOf(pNos.length)+"��");
            JLabel tit0 = new JLabel(pNos.length+"�ͤ����ѼԤ����򤵤�Ƥ��ޤ�����λ��פ򲡤��Ƚ����򳫻Ϥ��ޤ���");
            //tit0.setFont(new Font("Dialog",Font.PLAIN,12));
            JPanel pn1 = new JPanel();
            pn1.add(tit0);
            JPanel pn = new JPanel();
            pn.setLayout(new BoxLayout(pn, BoxLayout.PAGE_AXIS));
            pn.add(pn1);
            pn.add(dupName);
            if (JOptionPane.showConfirmDialog(fr,pn,"�ǡ���������",JOptionPane.OK_CANCEL_OPTION,JOptionPane.INFORMATION_MESSAGE)==0) {
              center0P.setVisible(false);
              pn0.setVisible(true);
              dbexec.restart();
            }
          }
          else if (dupName!=null) {
            statMessage(STATE_ERROR, "���򤵤줿���ѼԤϤ��٤ơ���������Ƚ�ʣ���Ƥ��ޤ���");
          }
          else {
            statMessage(STATE_ERROR, "���ѼԤ����򤵤�Ƥ��ޤ���");
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
        statMessage(STATE_ERROR,"�ǡ��������μ�������");
        runStat = STATE_COMPLETE;
        return;
      }

      final JButton cb = new JButton("����󥻥�");
      cb.setFont(new Font("SanSerif",Font.PLAIN,14));
      cb.addActionListener(actionListener);
      pn0.setBackground(Color.white);
      pn0.add(new JLabel("�ǡ����μ�������......."));
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
    String title = "�ǡ���������";
    switch (stat) {
      case STATE_INFO:
        JOptionPane.showMessageDialog(
           fr, err, title,
           JOptionPane.INFORMATION_MESSAGE
         ) ;
         break;

      case STATE_SUCCESS:
        JOptionPane.showMessageDialog(
           fr, "�����ߴ�λ���ޤ�����", title,
           JOptionPane.INFORMATION_MESSAGE
         ) ;
         break;

       case STATE_CANCEL:
         JOptionPane.showMessageDialog(
           fr,"�����ߤ����Ǥ��ޤ�����",title,
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
         pn1.add(new JLabel("�¹Է�³��ǽ���顼"),BorderLayout.NORTH);
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
        ipi.statMessage(ipi.STATE_FATAL,"�������ʤ��ǡ����١�������Ǥ������Ļ��ư���ƥǡ����١�����������ǧ���Ƥ���������");
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
