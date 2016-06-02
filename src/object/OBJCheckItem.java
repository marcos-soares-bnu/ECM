package object;

import java.util.HashMap;
import java.util.Map;

public class OBJCheckItem {

    private int id;
    private int check_id;
    private String itemName;
    private String status;
    //MPS - add control field to interval checks...
    private boolean ischecked; 

    private Map<Integer, OBJCheckOutput> errors;

    public OBJCheckItem(int id, int check_id, String itemName) {
        this.id = id;
        this.check_id = check_id;
        this.itemName = itemName;
        this.status = "OK";

        this.errors = new HashMap<Integer, OBJCheckOutput>();
        
        //MPS - add control field to interval checks...
        this.setIschecked(false);
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

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }

    public Map<Integer, OBJCheckOutput> getErrors() {
        return errors;
    }

    public void setErrors(Map<Integer, OBJCheckOutput> errors) {
        this.errors = errors;
    }
    
    public void setErrors(OBJCheckOutput objErrors) {
        this.errors = new HashMap<Integer, OBJCheckOutput>();
        this.errors.put(this.errors.size(), objErrors);
        this.status = "NOK";
    }

    public void putInErrorsMap(OBJCheckOutput error) {
        errors.put(errors.size() + 1, error);
    }

    public void putInErrorsMap(Map<Integer, OBJCheckOutput> errs) {
        for (OBJCheckOutput err : errs.values()) {
            this.status = "NOK";
            errors.put(errors.size() + 1, err);
        }
    }

    //MPS - add control field to interval checks...
	public boolean isIschecked() {
		return ischecked;
	}

	public void setIschecked(boolean ischecked) {
		this.ischecked = ischecked;
	}
}
