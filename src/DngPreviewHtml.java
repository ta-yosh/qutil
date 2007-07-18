import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AbstractDocument;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JButton;

public class DngPreviewHtml extends JPanel {

  protected Component html;

  public DngPreviewHtml() {
    setLayout(new BorderLayout());
    initComponents();
  }

  protected void initComponents() {
    JEditorPane editorPane = new JEditorPane();
    AbstractDocument doc = (AbstractDocument)editorPane.getDocument();
    doc.setAsynchronousLoadPriority(1); 
    editorPane.setEditable(false);
    editorPane.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          setURI(event.getURL());
        }
      }
    });
    html = editorPane;
    JScrollPane scroll = new JScrollPane();
    scroll.setViewportView(editorPane);
    add(scroll, BorderLayout.CENTER);
  }

  public void setURI(URL uri) {
    Runnable loader = createLoader(uri);
    SwingUtilities.invokeLater(loader);
  }

  protected Runnable createLoader(final URL uri) {
    return new Runnable() {
      public void run() {
        try {
          assert html instanceof JEditorPane;
          JEditorPane editorPane = (JEditorPane)html;
          editorPane.setPage(uri);
        } catch (IOException e) {
        }
      }
    };
  }

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
