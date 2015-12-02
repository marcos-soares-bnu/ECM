package manual_interface;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.DBUtil;
import util.Constantes;

public class InterfaceCheck {

    private int id;
    private String check_name;

    private InterfaceCheckConfig checkConfig;
    private List<InterfaceCheckItem> checkItens;

    public InterfaceCheck(int id) {
        this.id = id;

        //Just init the variables
        checkConfig = null;
        checkItens = new ArrayList<InterfaceCheckItem>();
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

    public InterfaceCheckConfig getCheckConfig() {
        return checkConfig;
    }

    public void setCheckConfig(InterfaceCheckConfig checkConfig) {
        this.checkConfig = checkConfig;
    }

    public List<InterfaceCheckItem> getCheckItens() {
        return checkItens;
    }

    public void setCheckItens(List<InterfaceCheckItem> checkItens) {
        this.checkItens = checkItens;
    }

    public void DB_retrieve() {
        if (this.getId() != 0) {
            //metodo para pegar as infos do banco
            this.DBGetCheckName();
            this.DBGetCheckConfig();
            this.DBGetCheckItens();
        } else {
            System.err.println("Can't retrieve anything from database");
        }
    }

    private void DBGetCheckItens() {
        DBUtil db = new DBUtil();
        String condition = "check_id=" + this.getId() + " AND on_change=0 AND disabled=0";
        ResultSet rs = db.doSelect("id", Constantes.DB_CheckItens_Table, condition);
        try {
            while (rs.next()) {
                int itemId = rs.getInt("id");

                InterfaceCheckItem item = new InterfaceCheckItem(itemId, this.getId());
                this.checkItens.add(item);
            }
            db.closeConn();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void DBGetCheckConfig() {
        this.checkConfig = new InterfaceCheckConfig(this.getId());
    }

    private void DBGetCheckName() {
        DBUtil db = new DBUtil();
        String condition = "id=" + this.getId();
        ResultSet rs = db.doSelect("*", Constantes.DB_Checks_Table, condition);
        try {
            if (rs.next()) {
                String name = rs.getString("check_name");
                this.setCheck_name(name);
            }
            db.closeConn();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
