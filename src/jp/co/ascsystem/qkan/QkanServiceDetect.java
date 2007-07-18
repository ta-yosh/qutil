package jp.co.ascsystem.qkan;

import jp.co.ascsystem.lib.*;
import jp.co.ascsystem.util.*;

public class QkanServiceDetect {
  
  public QkanServiceDetect() {
  }

  public boolean tsusyoDetect() {
  
    QkanPatientImport qpi = new QkanPatientImport();
    String dbUri = qpi.dbServer + "/" + qpi.dbPort + ":" + qpi.dbPath;
    String dbUser = qpi.getProperty("DBConfig/UserName");
    String dbPass = qpi.getProperty("DBConfig/Password");

    DngDBAccess dbm = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
    StringBuffer buf = new StringBuffer();
    buf.append("select PROVIDER_ID,PROVIDER_NAME from PROVIDER ");
    buf.append("where PROVIDER_ID in (");
    buf.append("   select PROVIDER_ID from PROVIDER_SERVICE");
    buf.append("    where SYSTEM_SERVICE_KIND_DETAIL in (11511,16511)");
    buf.append(")");
    String sql = buf.toString();
    if (dbm.connect()) {
      dbm.execQuery(sql);
      dbm.Close();
      return (dbm.Rows>0) ? true:false;
    }
    return false;
  }
}
