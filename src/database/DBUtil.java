package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import javax.swing.JOptionPane;

import util.Constantes;
import util.DateUtil;

public class DBUtil {

    private DateUtil dtUtil = new DateUtil();
    private Connection conn;

    public DBUtil() {
        this.conn = new ConnectionManager().getConnection();
    }

    public DBUtil(Connection conn) {
        this.conn = conn;
    }

    public Connection getConn() {
        if (conn == null) {
            conn = new ConnectionManager().getConnection();
        }
        return conn;
    }

    public void execSQL(String sql) {
        //Testing reasons
        if (Constantes.SHOW_DB_MESSAGES) {
            System.out.println(sql);
        }

        Statement stmt = null;
        try {
            stmt = this.getConn().createStatement();
            stmt.executeUpdate(sql);

            //best practices JDBC close...
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ResultSet doSelect(String fields, String table, String condition) {
        String sql = "SELECT " + fields;
        sql += " FROM " + table;

        if (!condition.isEmpty()) {
            sql += " WHERE " + condition;
        }
        sql += ";";

        //Testing reasons
        if (Constantes.SHOW_DB_MESSAGES) {
            System.out.println(sql);
        }

        try {
            Statement stmt = conn.createStatement();

            return stmt.executeQuery(sql);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e);
        }
        return null;
    }

    public String getAgeCheckLastExec(String code)
    {
        String fields = "(TIME_TO_SEC(TIMEDIFF(NOW(), lastexec)) / 60)";
        String table = Constantes.DB_ChecksCmds_Table;
        String condition = "code = '" + code + "'";
        ResultSet rs = this.doSelect(fields, table, condition);

        String strExec = "";
        try
        {
            if (rs.next())
            {
                String lastexec = rs.getString(1);
                if (lastexec != null)	{ strExec = lastexec; }
                else					{ strExec = "999999"; }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            strExec = "999999";
        }
        if (strExec.equals(""))	{ strExec = "999999"; }        
        return strExec;
    }
    

    public String getLastExecTime() {
        String fields = "MAX(exec_time)";
        String table = Constantes.DB_ChecksOutput_Table;
        String condition = "";
        ResultSet rs = this.doSelect(fields, table, condition);

        String strExec = "";
        try {
            if (rs.next()) {
                Timestamp execDB = rs.getTimestamp("MAX(exec_time)");
                if (execDB != null) {
                    strExec = dtUtil.getStrDateFromDB(execDB.getTime());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return strExec;
    }

    //
    //Bug 16: (LINDE - Java Logic) Update - Rotina de atualização (No Errors) dos checks fora do INTERVALO
    //
    public String[] getLastStatusOutput(int checkID, int checkItemID, String strExecTime) {

    	String[] vals = new String[2];
    	
    	String fields = "status, output_error";
        String table = Constantes.DB_ChecksOutputBKP_Table;
        
        String condition = "check_id=" + checkID + " AND check_item_id=" + checkItemID + " AND exec_time like '" + strExecTime + "%'";
        ResultSet rs = this.doSelect(fields, table, condition);
        String strError = "No Errors.";
        String strStatus = "OK";
        try {
            if (rs.next()) {
                String status = rs.getString("status");
                String error = rs.getString("output_error");
                if (error != null) {
                    strError = error;
                    strStatus = status;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        vals[0] = strStatus;
        vals[1] = strError;
        return vals;
    }

    //
    //Bug 16: (LINDE - Java Logic) Update - Rotina de atualização (No Errors) dos checks fora do INTERVALO
    //
    public String getLastExecTime(int checkID, int checkItemID, Date execTime) {
        String fields = "MAX(exec_time)";
        String table = Constantes.DB_ChecksOutputBKP_Table;
        String dbExecTime = dtUtil.getDBFormat(execTime);

        String condition = "check_id=" + checkID + " AND check_item_id=" + checkItemID + " AND exec_time <> '" + dbExecTime + "'";
        ResultSet rs = this.doSelect(fields, table, condition);

        String strExec = "";
        try {
            if (rs.next()) {
                Timestamp execDB = rs.getTimestamp("MAX(exec_time)");
                if (execDB != null) {
                    strExec = dtUtil.getStrDateFromDB(execDB.getTime());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        String[] strSplit = strExec.split(":");
        strExec = strSplit[0] + ":" + strSplit[1];
        
        return strExec;
    }
    //
    
    public String getLastExecTime(int checkID, Date execTime) {
        String fields = "MAX(exec_time)";
        String table = Constantes.DB_ChecksOutput_Table;
        String dbExecTime = dtUtil.getDBFormat(execTime);

        //MPS - Change to adopt new logic to clean Pending tickets...(get the last date for any checkID)
        String condition = "check_id=" + checkID + " AND exec_time <> '" + dbExecTime + "'";
        if (checkID == 0){
        	condition = "exec_time <> '" + dbExecTime + "'";
        }
        //MPS
        ResultSet rs = this.doSelect(fields, table, condition);

        String strExec = "";
        try {
            if (rs.next()) {
                Timestamp execDB = rs.getTimestamp("MAX(exec_time)");
                if (execDB != null) {
                    strExec = dtUtil.getStrDateFromDB(execDB.getTime());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        String[] strSplit = strExec.split(":");
        strExec = strSplit[0] + ":" + strSplit[1];
        
        return strExec;
    }

    public void doINSERT(PreparedStatement pstmt) {
        try {
            if (Constantes.SHOW_DB_MESSAGES) {
                System.out.println(pstmt);
            }
            pstmt.executeUpdate();

            //best practices JDBC close...
            pstmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e);
        }
    }

    //close at the end...
    public void closeConn() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
