package jp.co.ascsystem.qkan;

import jp.co.ascsystem.lib.*;
import jp.co.ascsystem.util.*;

public class QkanServiceDetect {
  
  QkanPatientImport qpi;
  String dbUri, dbUser, dbPass;

  public QkanServiceDetect() {
    qpi = new QkanPatientImport();
    dbUri = qpi.dbServer + "/" + qpi.dbPort + ":" + qpi.dbPath;
    dbUser = qpi.getProperty("doc/DBConfig/UserName");
    dbPass = qpi.getProperty("doc/DBConfig/Password");
    System.out.println("DB_URI: "+dbUri);
    System.out.println("DB_USR: "+dbUser);
    System.out.println("DB_PW: "+dbPass);
  }

  public boolean tsusyoDetect() {
  
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

  public boolean houkaiDetect() {
  
    DngDBAccess dbm = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
    StringBuffer buf = new StringBuffer();
    buf.append("select PROVIDER_ID,PROVIDER_NAME from PROVIDER ");
    buf.append("where PROVIDER_ID in (");
    buf.append("   select PROVIDER_ID from PROVIDER_SERVICE");
    buf.append("    where SYSTEM_SERVICE_KIND_DETAIL in (11111,16111)");
    buf.append(")");
    String sql = buf.toString();
    if (dbm.connect()) {
      dbm.execQuery(sql);
      dbm.Close();
      return (dbm.Rows>0) ? true:false;
    }
    return false;
  }

  public boolean houkanDetect() {
  
    DngDBAccess dbm = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
    StringBuffer buf = new StringBuffer();
    buf.append("select PROVIDER_ID,PROVIDER_NAME from PROVIDER ");
    buf.append("where PROVIDER_ID in (");
    buf.append("   select PROVIDER_ID from PROVIDER_SERVICE");
    buf.append("    where SYSTEM_SERVICE_KIND_DETAIL in (11311,16311)");
    buf.append(")");
    String sql = buf.toString();
    if (dbm.connect()) {
      dbm.execQuery(sql);
      dbm.Close();
      return (dbm.Rows>0) ? true:false;
    }
    return false;
  }

  public boolean hourehaDetect() {
  
    DngDBAccess dbm = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
    StringBuffer buf = new StringBuffer();
    buf.append("select PROVIDER_ID,PROVIDER_NAME from PROVIDER ");
    buf.append("where PROVIDER_ID in (");
    buf.append("   select PROVIDER_ID from PROVIDER_SERVICE");
    buf.append("    where SYSTEM_SERVICE_KIND_DETAIL in (11411,16411)");
    buf.append(")");
    String sql = buf.toString();
    if (dbm.connect()) {
      dbm.execQuery(sql);
      dbm.Close();
      return (dbm.Rows>0) ? true:false;
    }
    return false;
  }

  public boolean kyotakuDetect() {
  
    DngDBAccess dbm = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
    StringBuffer buf = new StringBuffer();
    buf.append("select PROVIDER_ID,PROVIDER_NAME from PROVIDER ");
    buf.append("where PROVIDER_ID in (");
    buf.append("   select PROVIDER_ID from PROVIDER_SERVICE");
    buf.append("    where SYSTEM_SERVICE_KIND_DETAIL in (13111,13411)");
    buf.append(")");
    String sql = buf.toString();
    if (dbm.connect()) {
      dbm.execQuery(sql);
      dbm.Close();
      return (dbm.Rows>0) ? true:false;
    }
    return false;
  }

  public boolean tsurehaDetect() {
  
    DngDBAccess dbm = new DngDBAccess("firebird",dbUri,dbUser,dbPass);
    StringBuffer buf = new StringBuffer();
    buf.append("select PROVIDER_ID,PROVIDER_NAME from PROVIDER ");
    buf.append("where PROVIDER_ID in (");
    buf.append("   select PROVIDER_ID from PROVIDER_SERVICE");
    buf.append("    where SYSTEM_SERVICE_KIND_DETAIL in (11611,16611)");
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
