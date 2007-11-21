package jp.co.ascsystem.lib;

import java.io.*;
import java.util.*;
import java.awt.Component;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import jp.co.ascsystem.lib.DngFileFilter;

public class DngFileChooser {
    private Component parent;
    private String fileDescription;
    private String[] fileExtensions;
    private String title=null;
    private String initialPath=".";
    private boolean mbPath=false;
    public boolean isMbPath=false;

    public DngFileChooser(Component parent) {
        this.parent = parent;
    }

    public DngFileChooser(Component parent,String desc,String ext) {
        String exts[] = {ext};
        this.parent = parent;
        fileDescription = desc;
        fileExtensions = exts;
    }

    public DngFileChooser(Component parent,String desc,String[] exts) {
        this.parent = parent;
        fileDescription = desc;
        fileExtensions = exts;
    }

    public void setTitle(String title) {
      this.title = title;
    }
 
    public void setInitPath(String path) {
      initialPath = path;
    }

    public void setMBPathEnable(boolean mbPath) {
      this.mbPath = mbPath;
    }

    public File getFile() {
        String osnAll = System.getProperty("os.name");
        File file=null;
        boolean loopFlg;
        String path = initialPath;
        //parent = this.parent;
        do {
            JFileChooser chooser = new JFileChooser((String)(new File(path).getPath()));
            DngFileFilter filter;
            try {
                //System.out.println("extension = "+fileExtensions[0]);
                filter = new DngFileFilter(fileDescription,fileExtensions);
            } catch(Exception e) {
                //System.out.println("extension auto");
                filter = new DngFileFilter();
            }
            chooser.setFileFilter(filter);
            if (title!=null) chooser.setDialogTitle(title);
            loopFlg = false;

            int returnVal = chooser.showOpenDialog(parent);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                file = chooser.getSelectedFile();
            }

            if (file == null) {
                return null;
            }
            String filePath = file.getPath();

            if (osnAll.equals("Windows Vista") && filePath.indexOf("\\Program Files\\")>=0) {
              filePath = System.getProperty("user.home")+"\\AppData\\Local\\VirtualStore"+filePath.substring(filePath.indexOf("\\Program Files"));
              File fil = new File(filePath);
              if (fil.exists()) {
                file = null;
                file = fil;
              }
            }
            System.out.println(file.getPath()+":"+file.length());

            if (!mbPath) { 
              if (file.getPath().matches("^.*[^a-zA-Z0-9-.+_/: \\\\]+.*$")) {
                JOptionPane.showMessageDialog(parent,"�����߸��ե�����ϡ�\n��Ǽ���(�ե����)��ե�����̾�����ܸ������ε���ʸ������ޤ��Τϡ����ѤǤ��ޤ���\n"+file.getPath(),"�����߸����顼",JOptionPane.ERROR_MESSAGE);
                loopFlg = true;
                //chooser.changeToParentDirectory();
                path = chooser.getCurrentDirectory().getAbsolutePath();
                file=null;
                continue;
              }
            }
            else if (file.getPath().matches("^.*[^a-zA-Z0-9-.+_/: \\\\]+.*$")) isMbPath=true;

            if (!file.exists()) {
                loopFlg = true; 
                continue;
            }
        } while (loopFlg);

        return file;
    }

    public File saveFile(String path,String fname) {
        String osnAll = System.getProperty("os.name");
        File file= null;
        boolean loopFlg;
        //parent = this.parent;
        do {
            JFileChooser chooser = new JFileChooser((String)(new File(path).getPath()));
            DngFileFilter filter;
            try {
                //System.out.println("extension = "+fileExtensions[0]);
                filter = new DngFileFilter(fileDescription,fileExtensions);
            } catch(Exception e) {
                //System.out.println("extension auto");
                filter = new DngFileFilter();
            }
            chooser.setFileFilter(filter);
            chooser.setSelectedFile(new File((String)(chooser.getCurrentDirectory().getPath())+"/"+fname));
            if (title!=null) chooser.setDialogTitle(title);
            loopFlg = false;
            int returnVal = chooser.showSaveDialog(parent);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                file = chooser.getSelectedFile();
            }
            else return null;

            if (file == null) {
                return null;
            }

            String filePath = file.getPath();

            if (osnAll.equals("Windows Vista") && filePath.indexOf("\\Program Files\\")>=0) {
              filePath = System.getProperty("user.home")+"\\AppData\\Local\\VirtualStore"+filePath.substring(filePath.indexOf("\\Program Files"));
              file = null;
              file = new File(filePath);
              File dir = new File(file.getParent());
              if (! dir.isDirectory()) dir.mkdirs();
            }

            if (!mbPath) { 
              if (file.getPath().matches("^.*[^a-zA-Z0-9-.+_/: \\\\]+.*$")) {
                JOptionPane.showMessageDialog(parent,"��¸��ˡ�\n���ܸ�䵭��ʸ������ޤ�ե����̾�ե�����̾�ϡ����ѤǤ��ޤ���\n"+file.getPath(),"��¸�襨�顼",JOptionPane.ERROR_MESSAGE);
                loopFlg = true;
                //chooser.changeToParentDirectory();
                path = chooser.getCurrentDirectory().getAbsolutePath();
                //file=null;
                continue;
              }
            }
            else if (file.getPath().matches("^.*[^a-zA-Z0-9-.+_/: \\\\]+.*$")) isMbPath=true;

            if (file.exists()) {
                if (JOptionPane.showConfirmDialog(parent,file.getName()+"�ϴ���¸�ߤ��ޤ�����񤭤��Ƥ�����Ǥ�����","�ե�������",JOptionPane.YES_NO_OPTION)==JOptionPane.NO_OPTION) {
                  loopFlg = true; 
                  path = chooser.getCurrentDirectory().getAbsolutePath();
                  continue;
                }
            }
        } while (loopFlg);

        return file;
    }
}
