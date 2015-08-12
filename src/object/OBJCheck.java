package object;

import java.util.Date;
import java.util.Map;

import database.DBCheckOutput;

public class OBJCheck {

    private int id;
    private String name;
    private Map<Integer, OBJCheckItem> itens;
    private Map<Integer, DBCheckOutput> lastErrors;
    private Date exec_time;

    public OBJCheck(int id, String name, Map<Integer, OBJCheckItem> itens, Map<Integer, DBCheckOutput> lastErrors, Date exec_time) {
        this.id = id;
        this.name = name;
        this.itens = itens;
        this.lastErrors = lastErrors;
        this.exec_time = exec_time;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Integer, OBJCheckItem> getItens() {
        return itens;
    }
    
    public Map<Integer, DBCheckOutput> getLastErrors() {
        return lastErrors;
    }

    public void setItens(Map<Integer, OBJCheckItem> itens) {
        this.itens = itens;
    }

    public Date getExec_time() {
        return exec_time;
    }

    public void setExec_time(Date exec_time) {
        this.exec_time = exec_time;
    }
    
    public OBJCheckItem getItem(String itemName) {
        for (OBJCheckItem item : itens.values()) {
            if (item.getItemName().equalsIgnoreCase(itemName)){
                return item;
            }
        }
        return null;
    }

}
