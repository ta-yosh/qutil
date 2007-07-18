package jp.co.ascsystem.lib;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.IOException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AbstractDocument;

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
}
