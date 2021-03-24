package jp.co.ascsystem.util;

import java.io.*;

import java.util.Vector;
import javax.swing.JTable;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import java.awt.Color;

public class DngPdfTable { 
    Document document;
    BaseFont mincho,gothic;
    Font mincho6,mincho7,mincho8,mincho9,mincho10,mincho12,mincho14,mincho16;
    Font mincho8b,mincho9b,mincho10b,mincho12b,mincho14b,mincho16b;
    Font gothic6b,gothic7b,gothic8,gothic9,gothic10,gothic12,gothic14,gothic16;
    Font gothic8b,gothic9b,gothic10b,gothic12b,gothic14b,gothic16b;
    PdfPTable ptable;
    String fname,title,subtitle;
    String ctype[];
    int ptotal,type;

    public DngPdfTable(String fname, int type) {
        this.fname = fname; 
        this.type = type;
        document = new Document(((type>0) ? PageSize.A4.rotate():PageSize.A4) ,20,20,25,10);
        try {
          mincho = BaseFont.createFont("HeiseiMin-W3",
                                     "UniJIS-UCS2-HW-H",true);
          gothic = BaseFont.createFont("HeiseiKakuGo-W5",
                                     "UniJIS-UCS2-HW-H",true);
          mincho6 = new Font(mincho,6);
          mincho7 = new Font(mincho,7);
          mincho8 = new Font(mincho,8);
          mincho9 = new Font(mincho,9);
          mincho10 = new Font(mincho,10);
          mincho12 = new Font(mincho,12);
          mincho14 = new Font(mincho,14);
          mincho16 = new Font(mincho,16);
          mincho8b = new Font(mincho,8,Font.BOLD);
          mincho9b = new Font(mincho,9,Font.BOLD);
          mincho10b = new Font(mincho,10,Font.BOLD);
          mincho12b = new Font(mincho,12,Font.BOLD);
          mincho14b = new Font(mincho,14,Font.BOLD);
          mincho16b = new Font(mincho,16,Font.BOLD);
          gothic8 = new Font(gothic,8);
          gothic9 = new Font(gothic,9);
          gothic10 = new Font(gothic,10);
          gothic12 = new Font(gothic,12);
          gothic14 = new Font(gothic,14);
          gothic16 = new Font(gothic,16);
          gothic6b = new Font(gothic,6,Font.BOLD);
          gothic7b = new Font(gothic,7,Font.BOLD);
          gothic8b = new Font(gothic,8,Font.BOLD);
          gothic9b = new Font(gothic,9,Font.BOLD);
          gothic10b = new Font(gothic,10,Font.BOLD);
          gothic12b = new Font(gothic,12,Font.BOLD);
          gothic14b = new Font(gothic,14,Font.BOLD);
          gothic16b = new Font(gothic,16,Font.BOLD);
        } catch (DocumentException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void setTitle(String title) {
      this.title = title;
    }

    public void setSubTitle(String subtitle) {
      this.subtitle = subtitle;
    }

    public boolean openPDF(String title) {
      this.title = title;
      try {
        PdfWriter.getInstance(document,new FileOutputStream(fname));
/*
        Paragraph para1 = new Paragraph(title+"\n", gothic10b);
        para1.setAlignment(Element.ALIGN_CENTER);
        Phrase headerPhrase = new Phrase();
        headerPhrase.add(para1);
        if (subtitle!="") {
          para1 = new Paragraph(subtitle, gothic8);
          para1.setAlignment(Element.ALIGN_RIGHT);
          headerPhrase.add(para1);
        }
        document.setHeader(header);
*/
        //para1.setAlignment(Element.ALIGN_CENTER);
        //document.add(para1);
      } catch (FileNotFoundException e) {
          e.printStackTrace();
          return false;
      } catch (DocumentException e) {
          e.printStackTrace();
          return false;
      } catch(Exception e) {
          e.printStackTrace();
          return false;
      }
      return true;
    }

    public void setParagraph(int lead,Phrase phrase) {
      Paragraph para1 = new Paragraph(phrase);
      if (lead<0) {
        para1.setAlignment(Element.ALIGN_RIGHT);
      } 
      else if (lead==0) {
        para1.setAlignment(Element.ALIGN_CENTER);
      }
      try {
        document.add(para1);
      } catch (DocumentException e) {
          e.printStackTrace();
      }
    }

    public void setParagraph(int lead,String phrase) {
      Paragraph para1;
      if (lead>0) {
        para1 = new Paragraph(lead,phrase, gothic10);
      } else if (lead<0) {
        para1 = new Paragraph(lead,phrase, gothic10);
        para1.setAlignment(Element.ALIGN_RIGHT);
      } else {
        para1 = new Paragraph(phrase, gothic10);
      }
      try {
        document.add(para1);
      } catch (DocumentException e) {
          e.printStackTrace();
      }
    }

    public void setRow(Vector row,float[] width,int[] ctype,int colCount) {
      try {
      ptable = (colCount==0) ? new PdfPTable(row.size()):
                               new PdfPTable(colCount);
      ptable.setHorizontalAlignment(Element.ALIGN_LEFT);
      ptable.setWidthPercentage(100);
      ptable.setWidths(width);
      ptable.setKeepTogether(true);
      for (int j=0;j<row.size();j++) {
          Object value;
          value=row.get(j);
          PdfPCell cell;
          if (value!=null) {
            String cval = value.toString().replaceAll("^ +","").replaceAll(" +$","");
// System.out.println("pdfROW "+j+" cval = ["+cval+"] ctype["+ctype[j]+"]");
            if (cval.matches("[0-9]+")) {
              if (ctype[j]==1) {
                 cval = (new DngNumUtil()).addComma(cval);
              }
            }

            Font font =(ctype[j]==6)? mincho6:mincho7;
            if (ctype[j]>7) {
               if (cval.length()>ctype[j]/2) font = mincho6;
            }
            cell = new PdfPCell(new Phrase(cval,font));
          }
          else {
            cell = new PdfPCell(new Phrase(" ", mincho12));
          }
          if (ctype[j]>0 && ctype[j]<4) cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
          if (ctype[j]==7) cell.setHorizontalAlignment(Element.ALIGN_CENTER);
          cell.setBorderColor(new BaseColor(255,255,255));
          cell.setVerticalAlignment(Element.ALIGN_TOP);
          ptable.addCell(cell);
      }
      setParagraph(0," ");
      document.add(ptable);
      } catch (DocumentException e) {
          e.printStackTrace();
      } catch(Exception e) {
          e.printStackTrace();
      }
    }

    public void setTable(JTable table,float[] width,int[] ctype,int colCount) {
      try {
      ptable = (colCount==0) ? new PdfPTable(table.getColumnCount()):
                               new PdfPTable(colCount);
      ptable.setHorizontalAlignment(Element.ALIGN_LEFT);
      ptable.setWidthPercentage(100);
      ptable.setWidths(width);
      ptable.setKeepTogether(true);

      int rcount = 0;
      int rpp = (colCount>0 || type>1) ? type : 25;
      int rpp2 = (colCount>0 || type>1) ? type : 25;
      int pcount = 0;
      ptotal = (table.getRowCount()-rpp)/rpp2 + 1 ;
      if ((table.getRowCount()-rpp)%rpp2>0) ptotal++;
/*
      if (ptotal>1) {
        HeaderFooter footer = new HeaderFooter(new Phrase("- ",gothic8),new Phrase("/"+ptotal+" -",gothic8));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setBorder(Rectangle.NO_BORDER);
        document.setFooter(footer);
      }
*/
      document.open();
      setParagraph(0, new Phrase(title, gothic10b));
      setParagraph(0,"  ");
      setParagraph(-1, new Phrase(3,subtitle, gothic8));
      setParagraph(0,"  ");
      for (int j=0;j<table.getColumnCount();j++) {
        PdfPCell cell = new PdfPCell(new Phrase(table.getColumnName(j), 
            (ctype[j]==7)? gothic6b:  gothic7b));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        if (colCount>0) {
          if (j<5 && j>6) cell.setRowspan(2);
          if (j==5) cell.setColspan(2);
        }
        ptable.addCell(cell);
      }
      for (int i=0;i<table.getRowCount();i++) {
        if (rcount==rpp) {
          document.add(ptable);
          pcount++;
          setParagraph(0,new Phrase("\n- "+pcount+"/"+ptotal+" -", gothic10));

          while (rcount>0) {
            ptable.deleteLastRow();
            if (colCount>0) ptable.deleteLastRow();
            rcount--;
          }
          rpp=rpp2;
          document.newPage();
          setParagraph(0, new Phrase(title, gothic10b));
          setParagraph(-1, new Phrase(3,subtitle, gothic8));
        }
        for (int j=0;j<table.getColumnCount();j++) {
          Object value;
          value=table.getValueAt(i,j);
          PdfPCell cell;
          if (value!=null) {
            String cval = value.toString().replaceAll("^ +","").replaceAll(" +$","");
            if (cval.matches("[0-9]+")) {
              if (ctype[j]==1) {
                 cval = (new DngNumUtil()).addComma(cval);
              }
            }

            Font font =(ctype[j]==6||ctype[j]==7)? mincho7:mincho8;
            if (ctype[j]>7) {
               if (cval.length()>ctype[j]/2) font = mincho7;
            }
            cell = new PdfPCell(new Phrase(cval,font));
            if (ctype[j]>0 && ctype[j]<4) cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            if (ctype[j]==7) cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorderColor(new BaseColor(100,100,100));
          }
          else {
            cell = new PdfPCell(new Phrase(" ", mincho12));
          }
          ptable.addCell(cell);
        }
        rcount++;
      }
      document.add(ptable);
      pcount++;
      } catch (DocumentException e) {
          e.printStackTrace();
      } catch(Exception e) {
          e.printStackTrace();
      }
    }

    public void flush() {
      document.close();
    }
}
