package database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import object.OBJCheckItem;
import util.Constantes;

public class DBCheckItem {

    private int id;
    private int check_id;
    private String item_name;
    private int item_priority;

    private Map<Integer, OBJCheckItem> itens;

    public DBCheckItem(int check_id) {
        this.check_id = check_id;
        itens = new HashMap<Integer, OBJCheckItem>();
        this.DB_retrieve();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCheck_id() {
        return check_id;
    }

    public void setCheck_id(int check_id) {
        this.check_id = check_id;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public int getItem_priority() {
        return item_priority;
    }

    public void setItem_priority(int item_priority) {
        this.item_priority = item_priority;
    }

    public Map<Integer, OBJCheckItem> getItens() {
        return itens;
    }

    public void setItens(Map<Integer, OBJCheckItem> itens) {
        this.itens = itens;
    }

    public void DB_retrieve() {
        if (this.getCheck_id() != 0) {
            //metodo para pegar as infos do banco
            DBUtil db = new DBUtil();
            String condition = "check_id=" + this.getCheck_id() + " AND on_change=0 AND disabled=0";
            ResultSet rs = db.doSelect("*", Constantes.DB_CheckItens_Table, condition);

            try {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String itemName = rs.getString("item_name");
                    int prio = rs.getInt("item_priority");
                    
                    OBJCheckItem item = new OBJCheckItem(id, this.check_id, itemName, prio);
                    
                    itens.put(itens.size()+1, item);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Can't retrieve anything from database");
        }
    }
}
