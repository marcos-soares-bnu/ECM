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

    public void doINSERT(String table, String fields, String values) {
        String sql = "INSERT INTO " + table;

        if (!fields.isEmpty()) {
            sql += "(" + fields + ")";
        }
        sql += " VALUES (" + values + ");";

        //Testing reasons
        if (Constantes.SHOW_DB_MESSAGES) {
            System.out.println(sql);
        }
        Statement stmt = null;
        try {
            stmt = this.getConn().createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e);
        }
    }

    public String getLastExecTime(int checkID, Date execTime) {
        String fields = "MAX(exec_time)";
        String table = Constantes.DB_ChecksOutput_Table;
        String dbExecTime = dtUtil.getDBFormat(execTime);
        String condition = "check_id=" + checkID + " AND exec_time <> '" + dbExecTime + "'";
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

    public void doINSERT(PreparedStatement pstmt) {
        try {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e);
        }
    }
}
