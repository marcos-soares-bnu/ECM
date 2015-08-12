package database;

import java.sql.ResultSet;
import java.sql.SQLException;

import util.Constantes;

public class DBCheck {

    private int id;
    private String check_name;

    public DBCheck(int id) {
        this.id = id;
        this.DB_retrieve();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCheck_name() {
        return check_name;
    }

    public void setCheck_name(String check_name) {
        this.check_name = check_name;
    }

    public void DB_retrieve() {
        if (this.getId() != 0){
            //metodo para pegar as infos do banco
            DBUtil db = new DBUtil();
            String condition = "id=" + this.getId();
            ResultSet rs = db.doSelect("*", Constantes.DB_Checks_Table, condition);
            
            try {
                rs.next();
                
                String name = rs.getString("check_name");
                this.setCheck_name(name);
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Can't retrieve anything from database");
        }
    }
}
