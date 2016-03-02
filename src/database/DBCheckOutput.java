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

public class DBCheckOutput {

    private int id;
    private int check_id;
    private int check_item_id;
    private String status;
    private String output_error;
    private Date exec_time;
    private int is_new;
    private int mail_sent;
    private String check_item_name;
    private String ticket_ci;
    private String ticket_brief;
    private int ticket_prio;

    public DBCheckOutput(int id, String check_item_name, String ticket_ci, String ticket_brief, String output_error, int ticketPrio) {
        this.id = id;
        this.setCheck_item_name(check_item_name);
        this.setTicket_ci(ticket_ci);
        this.setTicket_brief(ticket_brief);
        this.output_error = output_error;
        this.setTicket_prio(ticketPrio);
    }

    public DBCheckOutput() {
    }

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

    public int getTicket_prio() {
        return ticket_prio;
    }

    private void setTicket_prio(int ticket_prio) {
        this.ticket_prio = ticket_prio;
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

    public Map<Integer, DBCheckOutput> DB_retrieveNewErrors() {
        Map<Integer, DBCheckOutput> newErrors = new HashMap<Integer, DBCheckOutput>();
        //metodo para pegar as infos do banco
        DBUtil db = new DBUtil();

        //Get lastExecTime
        String strDBLastExec = db.getLastExecTime();

        //Check if exists last errors 
        if (!strDBLastExec.isEmpty()) {
            //Fields for all errors, except OTASS013 errors.
            String fields_All = "outp.id, citens.item_name as 'Check Item', lcitens.ticket_ci as 'Ticket CI', lcitens.item_prio as 'Ticket Prio', lcitens.ticket_brief as 'Ticket Brief', outp.output_error as 'Ticket Description'";
            String tables0_All = "check_scripts_output outp ";
            String tables1_All = "INNER JOIN check_scripts_itens citens ON ";
            String tables2_All = "citens.id = outp.check_item_id ";
            String tables3_All = "INNER JOIN check_scripts_tickets_config lcitens ON ";
            String tables4_All = "lcitens.check_item_id = citens.id ";
            String condition_All = "outp.status='NOK' AND outp.is_new='1' AND outp.mail_sent='0' AND outp.check_item_id <> 35 AND outp.exec_time = '" + strDBLastExec + "'";
            //ResultSet for all errors, except OTASS013 errors.
            ResultSet rsAll = db.doSelect(fields_All, (tables0_All + tables1_All + tables2_All + tables3_All + tables4_All), condition_All);

            //Fields for OTASS013 errors.
            String fields_Otass = "stats.id, jobs.job_name as'Job Name', stats.full_output as 'Ticket Description', lcitens.ticket_brief as 'Ticket Brief', lcitens.ticket_CI as 'Ticket CI', lcitens.ticket_prio as 'Ticket Prio'";
            String tables0_Otass = "otass013_jobs_status stats ";
            String tables1_Otass = "INNER JOIN otass_jobs jobs ON ";
            String tables2_Otass = "stats.job_id = jobs.id ";
            String tables3_Otass = "INNER JOIN linde_check_itens lcitens ON ";
            String tables4_Otass = "lcitens.code = 'OTASS013' ";
            String condition_Otass = "stats.status='ERROR' AND stats.mail_sent=0";
            //ResultSet for OTASS013 errors.
            ResultSet rsOtass = db.doSelect(fields_Otass, (tables0_Otass + tables1_Otass + tables2_Otass + tables3_Otass + tables4_Otass), condition_Otass);

            try {
                //At first retrieves all errors not related to OTASS013.
                while (rsAll.next()) {
                    int id = rsAll.getInt("outp.id");
                    String checkItem = rsAll.getString("Check Item");
                    String ticketCI = rsAll.getString("Ticket CI");
                    String ticketBrief = rsAll.getString("Ticket Brief");
                    String ticketDescr = rsAll.getString("Ticket Description");
                    int ticketPrio = rsAll.getInt("Ticket Prio");

                    DBCheckOutput err = new DBCheckOutput(id, checkItem, ticketCI, ticketBrief, ticketDescr, ticketPrio);
                    newErrors.put(newErrors.size() + 1, err);
                }
                //And here, retrieves only OTASS013 errors.
                while (rsOtass.next()) {
                    int id = rsOtass.getInt("stats.id");
                    String checkItem = "OTASS013"; //manual
                    String ticketCI = rsOtass.getString("Ticket CI");
                    String ticketBrief = rsOtass.getString("Ticket Brief").replace("JOBNAME", rsOtass.getString("Job Name"));
                    String ticketDescr = rsOtass.getString("Ticket Description");
                    int ticketPrio = rsOtass.getInt("Ticket Prio");

                    DBCheckOutput err = new DBCheckOutput(id, checkItem, ticketCI, ticketBrief, ticketDescr, ticketPrio);
                    newErrors.put(newErrors.size() + 1, err);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, e);
            } finally {
                db.closeConn();
            }
        }
        return newErrors;
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
                String condition = "check_id=" + this.getCheck_id() + " AND exec_time LIKE '" + strDBLastExec + "%'";
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
                } finally {
                    db.closeConn();
                }
            }
        }
        return lastErrors;
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
        db.closeConn();
    }

    public void DB_updateMailSent() {
        DBUtil db = new DBUtil();
        String table = Constantes.DB_ChecksOutput_Table;
        String field = "mail_sent";

        PreparedStatement pstmt = null;
        try {
            pstmt = db.getConn().prepareStatement("UPDATE " + table + " SET " + field + " = 1 WHERE id = ?;");
            pstmt.setInt(1, this.getId());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        db.doINSERT(pstmt);
        db.closeConn();
    }

    public void DB_updateMailSentOTASS013() {
        DBUtil db = new DBUtil();
        String table = Constantes.DB_OTASS013_JOBS_STATUS_Table;
        String field = "mail_sent";

        PreparedStatement pstmt = null;
        try {
            pstmt = db.getConn().prepareStatement("UPDATE " + table + " SET " + field + " = 1 WHERE id = ?;");
            pstmt.setInt(1, this.getId());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        db.doINSERT(pstmt);
        db.closeConn();
    }

    public String getCheck_item_name() {
        return check_item_name;
    }

    public void setCheck_item_name(String check_item_name) {
        this.check_item_name = check_item_name;
    }

    public String getTicket_ci() {
        return ticket_ci;
    }

    public void setTicket_ci(String ticket_ci) {
        this.ticket_ci = ticket_ci;
    }

    public String getTicket_brief() {
        return ticket_brief;
    }

    public void setTicket_brief(String ticket_brief) {
        this.ticket_brief = ticket_brief;
    }
}
