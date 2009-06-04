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

public class QkanProviderUtil extends QkanPatientImport {

  String dbOutPath=null;
  String realOutPath=null;
  String realInPath=null;
  boolean isMbOutPath;
  public JComboBox ymbox;
  public JLabel nodata;

  QkanProviderData pTable;

  public QkanProviderUtil() {
    propertyFile = getPropertyFile(); 
    dbServer = getProperty("doc/DBConfig/Server");
    dbPath = getProperty("doc/DBConfig/Path");
    dbPort = getProperty("doc/DBConfig/Port");
  }

  public JDialog dbUpdate(JButton execBtn,final QkanExecTransaction dbexec) throws Exception {

    realInPath = dbPath;
    if (dbOutPath==null) {
      fr = (parent!=null) ? new JDialog(parent) : new JDialog();
      fr.setTitle("���Ļ �ǡ����桼�ƥ���ƥ�");
      /*
      if (!checkLocalHost(dbServer)) {
         cancel(); 
      }
      */
      contentPane = fr.getContentPane();
      contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
      boolean kstat = false;
      while (!kstat) {
        if ((dbServer.equals("localhost") || dbServer.equals("127.0.0.1")) &&
!checkDBPath(dbPath)) {
          cancel(); 
        }
        String uri = dbServer + "/" + dbPort + ":" + dbPath;
        pTable = new QkanProviderData(uri,getProperty("doc/DBConfig/UserName"),getProperty("doc/DBConfig/Password"));
        if (pTable.Rows<0) {
          statMessage(STATE_ERROR,"�ǡ����١�������³�Ǥ��ޤ���\n���Ļ������ʤ���ư������֤��ɤ�������ǧ����������");
          return null;
        }
        if (pTable.Rows==0) {
          if ( JOptionPane.showConfirmDialog(
                fr,
                "�������ꤵ��Ƥ���ǡ����١����ˤϥǡ�����¸�ߤ��ޤ���\n�̤Υǡ����١��������򤷤ޤ�����",
                "�̽������ѼԾ���CSV�񤭽Ф�",JOptionPane.YES_NO_OPTION
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
               statMessage(STATE_ERROR,"����ΰ�γ��ݤ��Ǥ��ޤ���\n�񤭽Ф����ե���������ܸ�ʸ����ޤޤʤ��ѥ����֤��Ƽ¹Ԥ��ʤ����ƤߤƲ�������");
               return null;
            }
          }
        }
        else kstat = true;
      }
    } 
    else contentPane.removeAll();

    final JButton pdfBtn = new JButton("��������");
    pdfBtn.setFont(new Font("SanSerif",Font.PLAIN,14));
    final ActionListener pdfOut = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String fname = pTable.PDFout();
        if (fname!=null) {
          File pr = new File(getProperty("doc/Acrobat/Path"));
          File f = new File(fname);
          if (!pr.exists() && !System.getProperty("os.name").substring(0,3).equals("Mac")) {
            statMessage(STATE_ERROR,"PDF���꤬�����Ǥ��뤿�ᡢ�����Ǥ��ޤ��󡣵��Ļ���Τ�PDF������ǧ���Ƥ���������");
            f.delete();
            return;
          }
          String cmd[] = {getProperty("doc/Acrobat/Path"),fname};
          if ( System.getProperty("os.name").substring(0,3).equals("Mac") ) {
            cmd[0] = "open";
          }
          try {
            Process ps = Runtime.getRuntime().exec(cmd,null);
            ps.waitFor();
            if (System.getProperty("os.name").substring(0,3).equals("Mac")) {
              cmd[0] = "sleep";
              cmd[1] = "5";
              ps = Runtime.getRuntime().exec(cmd,null);
              ps.waitFor();
            }
            f.delete();
          } catch (Exception ex) {
            return;
          }
        }
        return;
      }
    };
    pdfBtn.addActionListener(pdfOut);

    final JButton exitBtn = new JButton("��λ");
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

    JLabel title = new JLabel(" ���Ļ  ���Ƚ����");
    title.setFont(new Font("SansSerif",Font.BOLD,18));
    JLabel dispPath = new JLabel("  ���ߤΥǡ����١�����"+dbServer+"/"+dbPort+":"+realInPath);
    dispPath.setFont(new Font("Serif",Font.PLAIN,12));
    dispPath.setForeground(Color.darkGray);
    JLabel chinf= new JLabel(" ���ǡ����١����ѹ��ϡ����Ļ���Τ�\"�ǡ����١�������\"�ǹԤäƲ�������");
    chinf.setFont(new Font("Dialog",Font.ITALIC,10));
    chinf.setForeground(Color.blue);
    JPanel northP = new JPanel( new BorderLayout());
    northP.add(title,BorderLayout.NORTH);
    northP.add(pdfBtn,BorderLayout.EAST);
    northP.add(dispPath,BorderLayout.CENTER);
    northP.add(chinf,BorderLayout.SOUTH);
    contentPane.add(northP);
    contentPane.add(pTable.getScrollList());
    center0P = new JPanel();
    center0P.add(execBtn);
    center0P.add(exitBtn);
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
    fr.setSize(800,600);
    //fr.pack();
    Dimension sc = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension sz = fr.getSize();
    fr.setLocation((sc.width-sz.width)/2,(sc.height-sz.height)/2);
    return fr;
  }

  public void execCsvOut() {
  
    final JProgressBar pb = new JProgressBar();
    final JLabel tit1 = new JLabel("�ڻ��Ƚ����CSV�񤭽Ф���");
    final JLabel tit = new JLabel("�����CSV�ե�����˽񤭽Ф��Ƥ��ޤ���");
    tit.setHorizontalAlignment(JLabel.LEFT);
    int stat = STATE_SUCCESS;

    final QkanExecTransaction dbexec= new QkanExecTransaction(0,pb);
    pb.setStringPainted(true);
    pb.setMinimum(0);

    final JButton sb = new JButton("�񤭽Ф�");
    sb.setFont(new Font("SanSerif",Font.PLAIN,14));
    final JPanel pn0 = new JPanel();
    final ActionListener actionListener = new ActionListener() {
       public void actionPerformed(ActionEvent e) {
        dbexec.pause();
        if (dbexec.runStat1) return;
        if ( 
          JOptionPane.showConfirmDialog(
            fr,
            "�񤭽Ф�����ߤ��ޤ�����\n�֤Ϥ��פ򲡤��Ȱʹߤν񤭽Ф�����ߤ���\n�񤭽Ф��ѤߤΤΤߤΥե����뤬��������ޤ���",
            "�ǡ����񤭽Ф�",JOptionPane.YES_NO_OPTION
          ) == JOptionPane.YES_OPTION
            ) {
          if (dbexec.runStat1) {
            statMessage(STATE_INFO,"�������Ƥν񤭽Ф�����λ���Ƥ��ޤ���");
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
          pTable.selectAll();
          int[][] pNos = prepareExec();
          if (pNos==null) {
            statMessage(STATE_ERROR,  "�ǡ�����ͭ��ޤ���");
            return;
          }
          pb.setValue(0);
          pb.setString("0/"+String.valueOf(pNos.length)+"��");
          dbOutPath = getExportDBPath(dbOutPath);
          if (dbOutPath==null) return;
          if (dbOutPath.compareToIgnoreCase(realInPath)==0) {
             statMessage(STATE_ERROR,"�񤭽Ф�����Ʊ��ե�����˽񤭽Ф����ϤǤ��ޤ���\n��������ߤ��ޤ���");
            dbOutPath=null;
            return;
          }
          dbexec.setPnos(pNos);
          pb.setMaximum(pNos.length);
          JLabel tit0 = new JLabel("CSV�ե�����ؤν񤭽Ф������򳫻Ϥ��ޤ���");
          tit0.setFont(new Font("Dialog",Font.BOLD,12));
          JPanel pn = new JPanel();
          pn.setLayout(new BoxLayout(pn, BoxLayout.PAGE_AXIS));
          pn.add(tit0);
          if (JOptionPane.showConfirmDialog(fr,pn,"CSV�񤭽Ф�",JOptionPane.OK_CANCEL_OPTION,JOptionPane.INFORMATION_MESSAGE)==0) {

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
      dbexec.setTable(pTable);
    } catch (Exception e) {
      System.out.println(e.toString());
      statMessage(STATE_ERROR,"�ǡ��������μ�������");
      return;
    }

    final JButton cb = new JButton("����󥻥�");
    cb.setFont(new Font("SanSerif",Font.PLAIN,14));
    cb.addActionListener(actionListener);
    pn0.setBackground(Color.white);
    pn0.add(new JLabel("�ǡ����񤭽Ф���......."));
    pn0.add(cb);
    contentPane.add(pb);
    contentPane.add(pn0);
    pn0.setVisible(false);
    if (parent != null) parent.setEnabled(false);
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
          pn0.add(new JLabel("�ǡ����񤭽Ф���......."));
          pn0.add(cb);
        }
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
    String title = "CSV�񤭽Ф�";
    switch (stat) {
      case STATE_INFO:
        JOptionPane.showMessageDialog(
           fr, err, title,
           JOptionPane.INFORMATION_MESSAGE
         ) ;
         break;

      case STATE_SUCCESS:
        JOptionPane.showMessageDialog(
           fr, "�񤭽Ф���λ���ޤ�����", title,
           JOptionPane.INFORMATION_MESSAGE
         ) ;
         break;

       case STATE_CANCEL:
         JOptionPane.showMessageDialog(
           fr,"�񤭽Ф�����������Ǥ��ޤ������ʹߤν�������ߤ��ޤ���",title,
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

  public int[][] prepareExec() {
    Object pdat[][] = pTable.getSelectedProviders();
    if (pdat.length<1) return null;
    int pNos[][] = new int[pdat.length][2];
    for (int i=0;i<pdat.length;i++) {
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
    if (outPath==null) outPath=realInPath;
    else fname = (new File(outPath)).getName();

    try {
      if (fname==null) {
        Calendar c = Calendar.getInstance();
        StringBuffer sb = new StringBuffer();
        sb.append("JIGYOSYO");
        sb.append(c.get(c.YEAR));
        String mm = (new Integer(c.get(c.MONTH)+1)).toString();
        if ((c.get(c.MONTH)+1)<10) sb.append("0");
        sb.append(mm);
        String dd = (new Integer(c.get(c.DATE))).toString();
        if (c.get(c.DATE)<10) sb.append("0");
        sb.append(dd);
        sb.append(".csv");
        fname = sb.toString();
      }
      DngFileChooser chooser = new DngFileChooser(fr,"CSV file",ext);
      chooser.setTitle("�񤭽Ф���CSV�ե��������¸������ꤷ�Ʋ�������");
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
    QkanProviderUtil ipi = new QkanProviderUtil();
    try {
      //ipi.execCsvOut();
      //System.exit(0);
      JDialog fr = ipi.dbUpdate(null,null);
      fr.setVisible(true);
    }
    catch(Exception e) {
      ipi.statMessage(STATE_FATAL,e.getMessage());
      System.exit(1);
    }
  }
}