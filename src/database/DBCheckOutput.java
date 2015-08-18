package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import util.Constantes;
import util.DateUtil;

public class DBCheckOutput {

    private DateUtil dtUtil = new DateUtil();
    
    private int id;
    private int check_id;
    private int check_item_id;
    private String status;
    private String output_error;
    private Date exec_time;
    private int is_new;
    private int mail_sent;

    public DBCheckOutput(int id, int check_id, int check_item_id, String status, String output_error, Date exec_time, int is_new, int mail_sent) {
        this.id = id;
        this.check_id = check_id;
        this.check_item_id = check_item_id;
        this.status = status;
        this.output_error = output_error;
        this.exec_time = exec_time;
        this.is_new = is_new;
        this.mail_sent = mail_sent;
    }

    public DBCheckOutput(int check_id, int check_item_id, String status, String output_error, Date exec_time, int is_new) {
        this.check_id = check_id;
        this.check_item_id = check_item_id;
        this.status = status;
        this.output_error = output_error;
        this.exec_time = exec_time;
        this.is_new = is_new;
        this.mail_sent = 0;
    }

    public DBCheckOutput(int check_id, int check_item_id, String status, String output_error, Date exec_time) {
        this.check_id = check_id;
        this.check_item_id = check_item_id;
        this.status = status;
        this.output_error = output_error;
        this.exec_time = exec_time;
        this.is_new = 0;
        this.mail_sent = 0;
    }

    public DBCheckOutput(int checkID, Date execTime) {
        this.check_id = checkID;
        this.exec_time = execTime;
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

    public int getCheck_item_id() {
        return check_item_id;
    }

    public void setCheck_item_id(int check_item_id) {
        this.check_item_id = check_item_id;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOutput_error() {
        return output_error;
    }

    public void setOutput_error(String output_error) {
        this.output_error = output_error;
    }

    public Date getExec_time() {
        return exec_time;
    }

    public void setExec_time(Date exec_time) {
        this.exec_time = exec_time;
    }

    public int getIs_new() {
        return is_new;
    }

    public void setIs_new(int is_new) {
        this.is_new = is_new;
    }

    public int getMail_sent() {
        return mail_sent;
    }

    public void setMail_sent(int mail_sent) {
        this.mail_sent = mail_sent;
    }

    public Map<Integer, DBCheckOutput> DB_retrieveLastErrors() {
        Map<Integer, DBCheckOutput> lastErrors = new HashMap<Integer, DBCheckOutput>();
        if (this.getCheck_id() == 0 || this.exec_time == null) {
            System.err.println("Can't retrieve anything from database");
        } else {
            //metodo para pegar as infos do banco
            DBUtil db = new DBUtil();

            //Get lastExecTime
            String strDBLastExec = db.getLastExecTime(this.getCheck_id(), exec_time);

            //Check if exists last errors 
            if (!strDBLastExec.isEmpty()) {
                String condition = "check_id=" + this.getCheck_id() + " AND exec_time = '" + strDBLastExec + "'";
                ResultSet rs = db.doSelect("*", Constantes.DB_ChecksOutput_Table, condition);

                try {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        int checkItemID = rs.getInt("check_item_id");
                        String status = rs.getString("status");
                        String error = rs.getString("output_error");
                        Timestamp execTime = rs.getTimestamp("exec_time");
                        int isNew = rs.getByte("is_new");
                        int mailSent = rs.getByte("mail_sent");

                        DBCheckOutput err = new DBCheckOutput(id, check_id, checkItemID, status, error, new Date(execTime.getTime()), isNew, mailSent);
                        lastErrors.put(lastErrors.size() + 1, err);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, e);
                }
            }
        }
        return lastErrors;
    }

    public void DB_store_OLD() {
        //metodo para gravar este item no banco
        DBUtil db = new DBUtil();
        String table = Constantes.DB_ChecksOutput_Table;
        String fields = "check_id, check_item_id, status, output_error, exec_time, is_new, mail_sent";
        String strDate = dtUtil.getDBFormat(this.getExec_time()); 
        String values = "'" + this.getCheck_id() + "'," + "'" + this.getCheck_item_id() + "'," + "'" + this.getStatus() + "'," + "'" + this.getOutput_error() + "'," +
                "'" + strDate + "'," +  this.getIs_new() + "," + this.getMail_sent();
        
        db.doINSERT(table, fields, values);
    }
    
    public void DB_store() {
        //metodo para gravar este item no banco
        DBUtil db = new DBUtil();
        String table = Constantes.DB_ChecksOutput_Table;
        String fields = "check_id, check_item_id, status, output_error, exec_time, is_new, mail_sent";

        PreparedStatement pstmt = null;
        try {
            pstmt = db.getConn().prepareStatement("INSERT INTO " + table + "(" + fields + ")" + " VALUES (?, ?, ?, ?, ?, ?, ?);");

            pstmt.setInt(1, this.getCheck_id());
            pstmt.setInt(2, this.getCheck_item_id());
            pstmt.setString(3, this.getStatus());
            pstmt.setString(4, this.getOutput_error());
            pstmt.setTimestamp(5, new Timestamp((this.getExec_time().getTime())));
            pstmt.setInt(6, this.getIs_new());
            pstmt.setInt(7, this.getMail_sent());

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e);
        }

        db.doINSERT(pstmt);
    }
}
