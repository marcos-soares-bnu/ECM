package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import util.Constantes;

public class CmdsHandler {

    DBUtil db = new DBUtil();
    
    
    public void dbClose() {
  
    	this.db.closeConn();
    }
    
    public String getAgeCheckLastExec(String code){
    	
    	String lastexe = db.getAgeCheckLastExec(code);
    	return lastexe;
    }
    
    public static java.sql.Timestamp getCurrentJavaSqlTimestamp()
    {
        java.util.Date date = new java.util.Date();
        return new java.sql.Timestamp(date.getTime());
    }    
    
    public void setLastExecCheck(String code) {

    	String table = Constantes.DB_ChecksCmds_Table;
        String field = "lastexec";

        PreparedStatement pstmt = null;
        try {
            pstmt = db.getConn().prepareStatement("UPDATE " + table + " SET " + field + " = ? WHERE code = ?;");
            //
            java.sql.Timestamp timestamp = getCurrentJavaSqlTimestamp();
            pstmt.setTimestamp(1, timestamp);            
            pstmt.setString(2, code);            
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        db.doINSERT(pstmt);
    }

    public void setLastLogCheck(String out, String code) {

    	String table = Constantes.DB_ChecksCmds_Table;
        String field = "lastlog";

        PreparedStatement pstmt = null;
        try {
            pstmt = db.getConn().prepareStatement("UPDATE " + table + " SET " + field + " = ? WHERE code = ?;");
            //
            pstmt.setString(1, out);            
            pstmt.setString(2, code);            
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        db.doINSERT(pstmt);
    }
    
    public void updateLastID(String lastID) {

    	String table = "linde_otass";
        String field = "last_id";

        PreparedStatement pstmt = null;
        try {
            pstmt = db.getConn().prepareStatement("UPDATE " + table + " SET " + field + " = ? WHERE is_enabled = 1;");
            //
            pstmt.setString(1, lastID);            
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        db.doINSERT(pstmt);
    }
    
    public String selectLastID() {
        ResultSet rs;
        String ret = "";
        rs = db.doSelect("last_id", "linde_otass", "is_enabled = 1");
        try {
            if (rs.next()) {
                ret = rs.getString("last_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
		return ret;
    }
    
    
    public String selectCmdBody(String check_cd, String code)
    {
    	ResultSet rs;
        String where = "check_cd = '"+ check_cd + "'";
        where += " AND code = '" + code + "'";
        String ret = "";
        
        rs = db.doSelect("body", Constantes.DB_ChecksCmds_Table, where);
        try {
            if (rs.next()) {
                ret = rs.getString("body");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
		return ret;
    }
    
    
    public List <List <String> > selectCmds(String check_cd) {

    	ResultSet rs;
        String where = "check_cd = '" + check_cd + "'";
        where += " order by code ASC";
        
        rs = db.doSelect("*", Constantes.DB_ChecksCmds_Table, where);
        try {
        	    int numcols = rs.getMetaData().getColumnCount();
        	    List <List <String> > result = new ArrayList<>();

        	    while (rs.next())
        	    {
        	        List <String> row = new ArrayList<>(numcols); // new list per row

        	        for (int i=1; i<= numcols; i++) {  // don't skip the last column, use <=
        	            row.add(rs.getString(i));
        	            //System.out.print(rs.getString(i) + "\t");
        	        }
        	        result.add(row); // add it to the result
        	        //System.out.print("\n");
        	    }        	
        	    return result;
        	    
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }	
	
	
}
