package manual_interface;

import java.sql.ResultSet;
import java.sql.SQLException;

import database.DBUtil;
import util.Constantes;

public class InterfaceCheckItem {

    private int id;
    private int check_id;
    private String item_name;
    private boolean openMoreTickets;

    public InterfaceCheckItem(int checkItemID, int check_id) {
        this.id = checkItemID;
        this.check_id = check_id;
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

    public boolean isOpenMoreTickets() {
        return openMoreTickets;
    }

    public void setOpenMoreTickets(boolean openMoreTickets) {
        this.openMoreTickets = openMoreTickets;
    }

    public void DB_retrieve() {
        if (this.getCheck_id() != 0) {
            //metodo para pegar as infos do banco
            DBUtil db = new DBUtil();
            String condition = "id=" + this.getId() + " AND check_id=" + this.getCheck_id() + " AND on_change=0 AND disabled=0";
            ResultSet rs = db.doSelect("item_name, open_moretickets", Constantes.DB_CheckItens_Table, condition);

            try {
                if (rs.next()) {
                    String itemName = rs.getString("item_name");
                    int openMoreTickets = rs.getByte("open_moretickets");

                    this.setItem_name(itemName);
                    this.setOpenMoreTickets(openMoreTickets == 1 ? true : false);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Can't retrieve anything from database");
        }
    }
}
