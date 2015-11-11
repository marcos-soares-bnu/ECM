package database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CmdsHandler {

    DBUtil db = new DBUtil();
    
    public List <List <String> > selectCmds(String code) {

    	ResultSet rs;
        String where = "code like %" + code + "%";
        
        rs = db.doSelect("*", "linde_mps_cmds", where);
        try {
        	    int numcols = rs.getMetaData().getColumnCount();
        	    List <List <String> > result = new ArrayList<>();

        	    while (rs.next())
        	    {
        	        List <String> row = new ArrayList<>(numcols); // new list per row

        	        for (int i=1; i<= numcols; i++) {  // don't skip the last column, use <=
        	            row.add(rs.getString(i));
        	            System.out.print(rs.getString(i) + "\t");
        	        }
        	        result.add(row); // add it to the result
        	        System.out.print("\n");
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
