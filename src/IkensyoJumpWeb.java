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
import java.net.URL;

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

import jp.co.saias.lib.*;
import jp.co.saias.util.*;

public class IkensyoJumpWeb {

  public static void main(String[] args) {

    JDialog fr = new JDialog();
    fr.setTitle("医見書 患者データユーティリティ");

    Container contentPane = fr.getContentPane();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
    final JButton exitBtn = new JButton("終了");
    exitBtn.setFont(new Font("SanSerif",Font.PLAIN,14));
    DngPreviewHtml pn = new DngPreviewHtml();
    contentPane.add(pn);
    try {
    pn.setURI(new URL(args[0]));
    } catch(Exception e) {
    }
    contentPane.add(exitBtn);

    fr.setSize(655,610);
    Dimension sc = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension sz = fr.getSize();
    fr.setLocation((sc.width-sz.width)/2,(sc.height-sz.height)/2);
    fr.setVisible(true);
  }
}
