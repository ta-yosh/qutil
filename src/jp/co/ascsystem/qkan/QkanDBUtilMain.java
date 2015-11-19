package jp.co.ascsystem.qkan;

import java.io.*;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JSeparator;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.JProgressBar;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JLabel;

public class QkanDBUtilMain {

  final Image icon = (new ImageIcon(getClass().getClassLoader().getResource("jp/co/ascsystem/qkan/icon/qdbutil.png"))).getImage();

  public static void main(String[] args) {

    final Color bgColor = new Color(220,180,220);
    //final Color btnColor0 = new Color(235,235,235);
    final Color Color1 = new Color(50,20,20);
    //final Color btnColor1 = new Color(150,150,150);
    //final Color btnColor2 = new Color(250,246,250);
    //final Color btnColor3 = new Color(246,250,250);
    //final Color btnColor4 = new Color(250,246,246);
    //final Color btnColor5 = new Color(246,250,246);
    //final Color btnColor6 = new Color(246,246,250);
    final QkanDBUtilMain idm = new QkanDBUtilMain();
    final JFrame fr = new JFrame();
    fr.setTitle("給管鳥 データユーティリティ ver3.1");
    fr.setIconImage(idm.icon);
    final Container contentPane = fr.getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.setBackground(bgColor);

    Dimension bSize1 = new Dimension(120,30);
    Dimension bSize2 = new Dimension(150,30);
    Dimension bSize3 = new Dimension( 70,30);
    
    final JButton imb = new JButton("取り込み");
    imb.setFont(new Font("SanSerif",Font.PLAIN,14));
    imb.setPreferredSize( bSize1 );
    imb.setMaximumSize( bSize1 );
    imb.setMinimumSize( bSize1 );
    //imb.setOpaque(false);
    //imb.setContentAreaFilled(false);
    //imb.setBorderPainted(false);
    //imb.setBackground(btnColor1);
    //imb.setBorder(BorderFactory.createRaisedBevelBorder());

    ActionListener triggerImport = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread it = new execThread(fr,1);
        it.start();
        //it.restart();
      }
    };
    imb.addActionListener(triggerImport);

    final JButton exb = new JButton("書き出し");
    exb.setFont(new Font("SanSerif",Font.PLAIN,14));
    exb.setPreferredSize( bSize1 );
    exb.setMaximumSize( bSize1 );
    exb.setMinimumSize( bSize1 );
    //exb.setBackground(btnColor1);
    //exb.setBorder(BorderFactory.createRaisedBevelBorder());
    ActionListener triggerExport = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread it = new execThread(fr,2);
        it.start();
        //it.restart();
      }
    };
    exb.addActionListener(triggerExport);

    final JButton csb = new JButton("利用者基本情報");
    csb.setFont(new Font("SanSerif",Font.PLAIN,14));
    csb.setPreferredSize( bSize2 );
    csb.setMaximumSize( bSize2 );
    csb.setMinimumSize( bSize2 );
    //csb.setBackground(btnColor1);
    //csb.setBorder(BorderFactory.createRaisedBevelBorder());
    ActionListener triggerCsvOut = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread it = new execThread(fr,3);
        it.start();
        //it.restart();
      }
    };
    csb.addActionListener(triggerCsvOut);

    final JButton tdb = new JButton("通所介護情報");
    tdb.setFont(new Font("SanSerif",Font.PLAIN,14));
    tdb.setPreferredSize( bSize2 );
    tdb.setMaximumSize( bSize2 );
    tdb.setMinimumSize( bSize2 );
    //tdb.setBackground(btnColor2);
    //tdb.setBorder(BorderFactory.createRaisedBevelBorder());
    ActionListener triggerTsusyo = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread it = new execThread(fr,4);
        it.start();
        //it.restart();
      }
    };
    tdb.addActionListener(triggerTsusyo);

    final JButton kdb = new JButton("居宅療養管理指導情報");
    kdb.setFont(new Font("SanSerif",Font.PLAIN,10));
    kdb.setPreferredSize( bSize2 );
    kdb.setMaximumSize( bSize2 );
    kdb.setMinimumSize( bSize2 );
    //kdb.setBackground(btnColor3);
    //kdb.setBorder(BorderFactory.createRaisedBevelBorder());
    ActionListener triggerKyotaku= new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread it = new execThread(fr,5);
        it.start();
        //it.restart();
      }
    };
    kdb.addActionListener(triggerKyotaku);

    final JButton rdb = new JButton("通所リハ情報");
    rdb.setFont(new Font("SanSerif",Font.PLAIN,14));
    rdb.setPreferredSize( bSize2 );
    rdb.setMaximumSize( bSize2 );
    rdb.setMinimumSize( bSize2 );
    //rdb.setBackground(btnColor2);
    //rdb.setBorder(BorderFactory.createRaisedBevelBorder());
    ActionListener triggerTsureha = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread it = new execThread(fr,6);
        it.start();
        //it.restart();
      }
    };
    rdb.addActionListener(triggerTsureha);

    final JButton ndb = new JButton("訪問看護情報");
    ndb.setFont(new Font("SanSerif",Font.PLAIN,14));
    ndb.setPreferredSize( bSize2 );
    ndb.setMaximumSize( bSize2 );
    ndb.setMinimumSize( bSize2 );
    //hdb.setBackground(btnColor5);
    //hdb.setBorder(BorderFactory.createRaisedBevelBorder());
    ActionListener triggerHouKan = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread it = new execThread(fr,9);
        it.start();
        //it.restart();
      }
    };
    ndb.addActionListener(triggerHouKan);

    final JButton vdb = new JButton("訪問リハ情報");
    vdb.setFont(new Font("SanSerif",Font.PLAIN,14));
    vdb.setPreferredSize( bSize2 );
    vdb.setMaximumSize( bSize2 );
    vdb.setMinimumSize( bSize2 );
    //vdb.setBackground(btnColor5);
    //vdb.setBorder(BorderFactory.createRaisedBevelBorder());
    ActionListener triggerHouReha = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread it = new execThread(fr,10);
        it.start();
        //it.restart();
      }
    };
    vdb.addActionListener(triggerHouReha);

    final JButton hdb = new JButton("訪問介護情報");
    hdb.setFont(new Font("SanSerif",Font.PLAIN,14));
    hdb.setPreferredSize( bSize2 );
    hdb.setMaximumSize( bSize2 );
    hdb.setMinimumSize( bSize2 );
    //hdb.setBackground(btnColor5);
    //hdb.setBorder(BorderFactory.createRaisedBevelBorder());
    ActionListener triggerHouKai = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        execThread it = new execThread(fr,8);
        it.start();
        //it.restart();
      }
    };
    hdb.addActionListener(triggerHouKai);

    final JButton pdb = new JButton("事業者情報");
    pdb.setFont(new Font("SanSerif",Font.PLAIN,14));
    pdb.setPreferredSize( bSize2 );
    pdb.setMaximumSize( bSize2 );
    pdb.setMinimumSize( bSize2 );
    //pdb.setBackground(btnColor6);
    //pdb.setBorder(BorderFactory.createRaisedBevelBorder());
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
    cb.setPreferredSize( bSize3 );
    cb.setMinimumSize( bSize3 );
    cb.setMaximumSize( bSize3 );
    //cb.setBackground(btnColor0);
    //cb.setBorder(BorderFactory.createRaisedBevelBorder());
    ActionListener appExit = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.exit(0);
      }
    };
    cb.addActionListener(appExit);
    JPanel pn = new JPanel();
    pn.setLayout(new BoxLayout(pn,BoxLayout.Y_AXIS));
    pn.setOpaque(false);
    pn.add(Box.createVerticalGlue());
    pn.add(new JSeparator(JSeparator.HORIZONTAL));
    pn.add(Box.createVerticalStrut(5));

    JPanel lpn1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    lpn1.setOpaque(false);
    final JLabel paLabel =  new JLabel("利用者別データ");
    paLabel.setForeground(Color1);
    lpn1.add(paLabel);
    pn.add(lpn1);

    Dimension size1 = new Dimension(470,36);
    Dimension size2 = new Dimension(470,80);
    JPanel bpn0 = new JPanel();
    bpn0.setOpaque(false);
    bpn0.setLayout(new BoxLayout(bpn0,BoxLayout.X_AXIS));
    bpn0.setMinimumSize(size1);
    bpn0.setMaximumSize(size1);
    bpn0.setPreferredSize(size1);
    bpn0.add(Box.createHorizontalGlue());
    bpn0.add(exb);
    bpn0.add(Box.createHorizontalStrut(20));
    bpn0.add(imb);
    bpn0.add(Box.createHorizontalGlue());
    pn.add(bpn0);
    pn.add(Box.createVerticalStrut(10));
    pn.add(new JSeparator(JSeparator.HORIZONTAL));
    pn.add(Box.createVerticalStrut(5));

    JPanel lpn2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    lpn2.setOpaque(false);
    JLabel csLabel =  new JLabel("情報別CSV書き出し／印刷");
    csLabel.setForeground(Color1);
    lpn2.add(csLabel);
    pn.add(lpn2);
 
    JPanel bpn1 = new JPanel();
    bpn1.setOpaque(false);
    bpn1.setLayout(new BoxLayout(bpn1,BoxLayout.X_AXIS));
    bpn1.setMinimumSize(size1);
    bpn1.setMaximumSize(size1);
    bpn1.setPreferredSize(size1);
    bpn1.add(Box.createHorizontalGlue());
    bpn1.add(csb);
    bpn1.add(Box.createHorizontalStrut(20));
    bpn1.add(pdb);
    bpn1.add(Box.createHorizontalGlue());
    pn.add(bpn1);
    pn.add(Box.createVerticalStrut(10));
    JPanel bpn2 = new JPanel(new GridLayout(2,3,5,5));
    bpn2.setOpaque(false);
    bpn2.setMinimumSize(size2);
    bpn2.setMaximumSize(size2);
    bpn2.setPreferredSize(size2);
    if ((new QkanServiceDetect()).tsusyoDetect()==true) {
      bpn2.add(tdb);
    }
    if ((new QkanServiceDetect()).tsurehaDetect()==true) {
      bpn2.add(rdb);
    }
    if ((new QkanServiceDetect()).kyotakuDetect()==true) {
      bpn2.add(kdb);
    }
    if ((new QkanServiceDetect()).houkaiDetect()==true) {
      bpn2.add(hdb);
    }
    if ((new QkanServiceDetect()).hourehaDetect()==true) {
      bpn2.add(vdb);
    }
    if ((new QkanServiceDetect()).houkanDetect()==true) {
      bpn2.add(ndb);
    }
    pn.add(bpn2);
    pn.add(Box.createVerticalStrut(10));
    pn.add(new JSeparator(JSeparator.HORIZONTAL));
    JPanel bpn3 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    bpn3.setOpaque(false);
    bpn3.setMinimumSize(size1);
    bpn3.setMaximumSize(size1);
    bpn3.setPreferredSize(size1);
    bpn3.add(cb);
    pn.add(bpn3);
    pn.add(Box.createVerticalGlue());
    JPanel pn0 = new JPanel();
    pn0.setOpaque(false);
    pn0.setLayout(new BoxLayout(pn0,BoxLayout.X_AXIS));
    pn0.add(Box.createHorizontalGlue());
    pn0.add(pn);
    pn0.add(Box.createHorizontalGlue());

    JLabel sysTitle = new JLabel("給管鳥 データユーティリティ");
    sysTitle.setFont(new Font("SanSerif",Font.BOLD,15));
    sysTitle.setForeground(Color1);
    contentPane.add(sysTitle,BorderLayout.NORTH);
    contentPane.add(pn0,BorderLayout.CENTER);
    contentPane.add(new JLabel("  "),BorderLayout.EAST);
    contentPane.add(new JLabel("  "),BorderLayout.WEST);
    contentPane.add(new JLabel("  "),BorderLayout.SOUTH);

    WindowAdapter AppCloser =  new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        System.exit(0);
      }
    };
    fr.addWindowListener(AppCloser);

    fr.setSize(500,360);
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
    else if(type==8) {
      QkanHouKaiUtil htr = new QkanHouKaiUtil();
      if (!htr.vStat) {
        htr.statMessage(htr.STATE_FATAL,"正しくないデータベース設定です。給管鳥を起動してデータベースの設定を確認してください。");
        System.exit(1);
      }
      htr.setParent(frm);
      try {
        while(htr.runStat!=htr.STATE_FATAL) {
          System.out.println("STAT = "+htr.runStat); 
          if (htr.runStat==htr.STATE_COMPLETE) {
            htr.destroy();
            break;
          }
          htr.execCsvOut();
        }
      }
      catch(Exception ex) {
        htr.statMessage(htr.STATE_FATAL,ex.getMessage());
      }
    }
    else if(type==9) {
      QkanHouKanUtil ntr = new QkanHouKanUtil();
      if (!ntr.vStat) {
        ntr.statMessage(ntr.STATE_FATAL,"正しくないデータベース設定です。給管鳥を起動してデータベースの設定を確認してください。");
        System.exit(1);
      }
      ntr.setParent(frm);
      try {
        while(ntr.runStat!=ntr.STATE_FATAL) {
          System.out.println("STAT = "+ntr.runStat); 
          if (ntr.runStat==ntr.STATE_COMPLETE) {
            ntr.destroy();
            break;
          }
          ntr.execCsvOut();
        }
      }
      catch(Exception ex) {
        ntr.statMessage(ntr.STATE_FATAL,ex.getMessage());
      }
    }
    else if(type==10) {
      QkanHouRehaUtil vtr = new QkanHouRehaUtil();
      if (!vtr.vStat) {
        vtr.statMessage(vtr.STATE_FATAL,"正しくないデータベース設定です。給管鳥を起動してデータベースの設定を確認してください。");
        System.exit(1);
      }
      vtr.setParent(frm);
      try {
        while(vtr.runStat!=vtr.STATE_FATAL) {
          System.out.println("STAT = "+vtr.runStat); 
          if (vtr.runStat==vtr.STATE_COMPLETE) {
            vtr.destroy();
            break;
          }
          vtr.execCsvOut();
        }
      }
      catch(Exception ex) {
        vtr.statMessage(vtr.STATE_FATAL,ex.getMessage());
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
