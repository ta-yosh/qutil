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
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.JProgressBar;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JLabel;

public class QkanDBUtilMain {

  final Image icon = (new ImageIcon(getClass().getClassLoader().getResource("jp/co/ascsystem/qkan/icon/qdbutil.png"))).getImage();

  public static void main(String[] args) {

    final QkanDBUtilMain idm = new QkanDBUtilMain();
    final JFrame fr = new JFrame();
    fr.setTitle("給管鳥 データユーティリティ Ver1.5");
    fr.setIconImage(idm.icon);
    final Container contentPane = fr.getContentPane();
    contentPane.setLayout(new BorderLayout());

    final JButton imb = new JButton("利用者別データ取り込み");
    imb.setFont(new Font("SanSerif",Font.PLAIN,14));
    ActionListener triggerImport = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread it = new execThread(fr,1);
        it.start();
        //it.restart();
      }
    };
    imb.addActionListener(triggerImport);

    final JButton exb = new JButton("利用者別データ書き出し");
    exb.setFont(new Font("SanSerif",Font.PLAIN,14));
    ActionListener triggerExport = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread it = new execThread(fr,2);
        it.start();
        //it.restart();
      }
    };
    exb.addActionListener(triggerExport);

    final JButton csb = new JButton("利用者基本情報CSV書き出し");
    csb.setFont(new Font("SanSerif",Font.PLAIN,14));
    ActionListener triggerCsvOut = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread it = new execThread(fr,3);
        it.start();
        //it.restart();
      }
    };
    csb.addActionListener(triggerCsvOut);

    final JButton tdb = new JButton("通所介護利用者情報CSV書き出し");
    tdb.setFont(new Font("SanSerif",Font.PLAIN,14));
    ActionListener triggerTsusyo = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread it = new execThread(fr,4);
        it.start();
        //it.restart();
      }
    };
    tdb.addActionListener(triggerTsusyo);

    final JButton kdb = new JButton("居宅療養管理指導情報CSV書き出し");
    kdb.setFont(new Font("SanSerif",Font.PLAIN,14));
    ActionListener triggerKyotaku= new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread it = new execThread(fr,5);
        it.start();
        //it.restart();
      }
    };
    kdb.addActionListener(triggerKyotaku);

    final JButton rdb = new JButton("通所リハ利用者情報CSV書き出し");
    rdb.setFont(new Font("SanSerif",Font.PLAIN,14));
    ActionListener triggerTsureha = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread it = new execThread(fr,6);
        it.start();
        //it.restart();
      }
    };
    rdb.addActionListener(triggerTsureha);

    final JButton pdb = new JButton("事業者情報");
    pdb.setFont(new Font("SanSerif",Font.PLAIN,14));
    ActionListener triggerProvider= new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread pt = new execThread(fr,7);
        pt.start();
        //it.restart();
      }
    };
    pdb.addActionListener(triggerProvider);


/*
    final JButton web = new JButton("給管鳥ウェブサイト");
    web.setFont(new Font("SanSerif",Font.PLAIN,14));
    ActionListener triggerWeb = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread it = new execThread(fr,4);
        it.start();
        //it.restart();
      }
    };
    web.addActionListener(triggerWeb);
*/
    final JButton cb = new JButton("終了");
    cb.setFont(new Font("SanSerif",Font.PLAIN,14));
    ActionListener appExit = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    };
    cb.addActionListener(appExit);
    JPanel pn = new JPanel(new GridLayout(0,1));
    pn.add(exb);
    pn.add(imb);
    pn.add(csb);
    int ysiz=250;
    if ((new QkanServiceDetect()).tsusyoDetect()==true) {
      pn.add(tdb);
      ysiz=ysiz+50;
    }
    if ((new QkanServiceDetect()).tsurehaDetect()==true) {
      pn.add(rdb);
      ysiz=ysiz+50;
    }
    if ((new QkanServiceDetect()).kyotakuDetect()==true) {
      pn.add(kdb);
      ysiz=ysiz+50;
    }
    pn.add(pdb);
    JLabel sysTitle = new JLabel("給管鳥 データユーティリティ");
    sysTitle.setFont(new Font("SanSerif",Font.BOLD,15));
    contentPane.add(sysTitle,BorderLayout.NORTH);
    contentPane.add(pn,BorderLayout.CENTER);
    contentPane.add(cb,BorderLayout.EAST);

    WindowAdapter AppCloser =  new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    };
    fr.addWindowListener(AppCloser);

    fr.setSize(350,ysiz);
    Dimension sc = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension sz = fr.getSize();
    fr.setLocation((sc.width-sz.width)/2,(sc.height-sz.height)/2);
    fr.setVisible(true);
    //try {
    //  it.join();
    //} catch(InterruptedException ex) {
    //  return;
    //}
  }
}

class execThread extends Thread {
 
  JFrame frm;
  int type;
  public boolean runStat = false;

  execThread(JFrame frm,int type) {
     this.frm = frm;
     this.type = type;
  }

  public void run() {
    //frm.setVisible(false);
    //try {
    //   synchronized(this) {
    //     while(!runStat) wait();
    //   }
    //} catch(InterruptedException e) {
    //}
    if (type==1) {
      QkanPatientImport ipi = new QkanPatientImport();
      if (!ipi.vStat) {
        ipi.statMessage(ipi.STATE_FATAL,"正しくないデータベース設定です。給管鳥を起動してデータベースの設定を確認してください。");
        System.exit(1);
      }
      ipi.setParent(frm);
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
    else if(type==2) {
      QkanPatientExport ipe = new QkanPatientExport();
      if (!ipe.vStat) {
        ipe.statMessage(ipe.STATE_FATAL,"正しくないデータベース設定です。給管鳥を起動してデータベースの設定を確認してください。");
        System.exit(1);
      }
      ipe.setParent(frm);
      try {
        while(ipe.runStat!=ipe.STATE_FATAL) {
          //System.out.println("STAT = "+ipe.runStat); 
          if (ipe.runStat==ipe.STATE_COMPLETE) {
            ipe.destroy();
            break;
          }
          ipe.execExport();
        }
      }
      catch(Exception ex) {
        ipe.statMessage(ipe.STATE_FATAL,ex.getMessage());
      }
    }
    else if(type==3) {
      QkanPatientCsvOut ipc = new QkanPatientCsvOut();
      if (!ipc.vStat) {
        ipc.statMessage(ipc.STATE_FATAL,"正しくないデータベース設定です。給管鳥を起動してデータベースの設定を確認してください。");
        System.exit(1);
      }
      ipc.setParent(frm);
      try {
        while(ipc.runStat!=ipc.STATE_FATAL) {
          //System.out.println("STAT = "+ipc.runStat); 
          if (ipc.runStat==ipc.STATE_COMPLETE) {
            ipc.destroy();
            break;
          }
          ipc.execCsvOut();
        }
      }
      catch(Exception ex) {
        ipc.statMessage(ipc.STATE_FATAL,ex.getMessage());
      }
    }
    else if(type==4) {
      QkanTsusyoUtil itu = new QkanTsusyoUtil();
      if (!itu.vStat) {
        itu.statMessage(itu.STATE_FATAL,"正しくないデータベース設定です。給管鳥を起動してデータベースの設定を確認してください。");
        System.exit(1);
      }
      itu.setParent(frm);
      try {
        while(itu.runStat!=itu.STATE_FATAL) {
          //System.out.println("STAT = "+itu.runStat); 
          if (itu.runStat==itu.STATE_COMPLETE) {
            itu.destroy();
            break;
          }
          itu.execCsvOut();
        }
      }
      catch(Exception ex) {
        itu.statMessage(itu.STATE_FATAL,ex.getMessage());
      }
    }
    else if(type==5) {
      QkanKyotakuUtil iku = new QkanKyotakuUtil();
      if (!iku.vStat) {
        iku.statMessage(iku.STATE_FATAL,"正しくないデータベース設定です。給管鳥を起動してデータベースの設定を確認してください。");
        System.exit(1);
      }
      iku.setParent(frm);
      try {
        while(iku.runStat!=iku.STATE_FATAL) {
          //System.out.println("STAT = "+iku.runStat); 
          if (iku.runStat==iku.STATE_COMPLETE) {
            iku.destroy();
            break;
          }
          iku.execCsvOut();
        }
      }
      catch(Exception ex) {
        iku.statMessage(iku.STATE_FATAL,ex.getMessage());
      }
    }
    else if(type==6) {
      QkanTsusyoRehaUtil itr = new QkanTsusyoRehaUtil();
      if (!itr.vStat) {
        itr.statMessage(itr.STATE_FATAL,"正しくないデータベース設定です。給管鳥を起動してデータベースの設定を確認してください。");
        System.exit(1);
      }
      itr.setParent(frm);
      try {
        while(itr.runStat!=itr.STATE_FATAL) {
          //System.out.println("STAT = "+itr.runStat); 
          if (itr.runStat==itr.STATE_COMPLETE) {
            itr.destroy();
            break;
          }
          itr.execCsvOut();
        }
      }
      catch(Exception ex) {
        itr.statMessage(itr.STATE_FATAL,ex.getMessage());
      }
    }
    else if(type==7) {
      QkanProviderUtil ptr = new QkanProviderUtil();
      if (!ptr.vStat) {
        ptr.statMessage(ptr.STATE_FATAL,"正しくないデータベース設定です。給管鳥を起動してデータベースの設定を確認してください。");
        System.exit(1);
      }
      ptr.setParent(frm);
      try {
        while(ptr.runStat!=ptr.STATE_FATAL) {
          System.out.println("STAT = "+ptr.runStat); 
          if (ptr.runStat==ptr.STATE_COMPLETE) {
            ptr.destroy();
            break;
          }
          ptr.execCsvOut();
        }
      }
      catch(Exception ex) {
        ptr.statMessage(ptr.STATE_FATAL,ex.getMessage());
      }
    }
    //else  {
    //  QkanPatientExport dw = new QkanJumpWeb();
    //  dw.setParent(frm);
    //  dw.disp();
   // }
  }

  synchronized public void pause() {
    runStat = false;
  }

  synchronized public void restart() {
    runStat = true;
    notifyAll();
  }

}
