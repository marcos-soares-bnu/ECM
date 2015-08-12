package database;

import java.sql.ResultSet;
import java.sql.SQLException;

import util.Constantes;

public class DBCheckConfig {

    private int check_id;
    private String path_cmd;
    private String path_output;

    public DBCheckConfig(int check_id) {
        this.check_id = check_id;
        this.DB_retrieve();
    }

    public int getCheck_id() {
        return check_id;
    }

    public void setCheck_id(int check_id) {
        this.check_id = check_id;
    }

    public String getPath_cmd() {
        return path_cmd;
    }

    public void setPath_cmd(String path_cmd) {
        this.path_cmd = path_cmd;
    }

    public String getPath_output() {
        return path_output;
    }

    public void setPath_output(String path_output) {
        this.path_output = path_output;
    }

    public void DB_retrieve() {
        if (this.getCheck_id() != 0){
            //metodo para pegar as infos do banco
            DBUtil db = new DBUtil();
            String condition = "check_id=" + this.getCheck_id();
            ResultSet rs = db.doSelect("*", Constantes.DB_CheckConfig_Table, condition);
            
            try {
                rs.next();
                
                String pathCMD = rs.getString("path_cmd");
                String pathOutput = rs.getString("path_output");
                
                this.setPath_cmd(pathCMD);
                this.setPath_output(pathOutput);
                
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Can't retrieve anything from database");
        }
    }
}
