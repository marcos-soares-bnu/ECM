package OTASS013;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import database.DBUtil;
import util.Constantes;

public class DBOTASSJobStatus {

    private int id;
    private int jobId;
    private String status;
    private String output;
    private String fullOutput;
    private boolean mailSent;

    //===================================Constructors==================================
    //Constructor used when creating the JOB INFOS
    public DBOTASSJobStatus(int id, int jobId, String status, String output, String fullOutput, boolean mailSent) {
        this.id = id;
        this.jobId = jobId;
        this.status = status;
        this.output = output;
        this.fullOutput = fullOutput;
        this.mailSent = mailSent;
    }

    //Constructor used when is a new status for a job
    public DBOTASSJobStatus(OBJReadedOTASSJobStatus readedStatus, int jobID) {
        this.jobId = jobID;
        this.status = readedStatus.getStatus();
        this.output = readedStatus.getOutput();
        this.fullOutput = readedStatus.getFullOutput();
        this.mailSent = false;
    }

    //===================================DB Functions==================================
    public void insertNewStatus() {
        DBUtil db = new DBUtil();
        String fields = "job_id, status, output, mail_sent, full_output";
        try {
            PreparedStatement prepStmt = db.getConn().prepareStatement("INSERT INTO " + Constantes.DB_OTASS013_JOBS_STATUS_Table + "(" + fields + ")" + " VALUES (?, ?, ?, ?, ?);");
            prepStmt.setInt(1, this.getJobId());
            prepStmt.setString(2, this.getStatus());
            prepStmt.setString(3, this.getOutput());
            int bitMailSent = this.isMailSent() ? 1 : 0;
            prepStmt.setInt(4, bitMailSent);
            prepStmt.setString(5, this.getFullOutput());

            //execute SQL
            db.doINSERT(prepStmt);

            //Close stmt
            prepStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //Close conn
        db.closeConn();
    }

    public void updateSTATUS() {
        DBUtil db = new DBUtil();
        try {
            PreparedStatement prepStmt = db.getConn().prepareStatement("UPDATE " + Constantes.DB_OTASS013_JOBS_STATUS_Table + " SET status=?, output=?, full_output=?, mail_sent=? WHERE id=?;");
            prepStmt.setString(1, this.getStatus());
            prepStmt.setString(2, this.getOutput());
            prepStmt.setString(3, this.getFullOutput());
            int bitMailSent = this.isMailSent() ? 1 : 0;
            prepStmt.setInt(4, bitMailSent);
            prepStmt.setInt(5, this.getId());

            //execute SQL
            db.doINSERT(prepStmt);

            //Close stmt
            prepStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //Close conn
        db.closeConn();
    }

    public void deleteSTATUS() {
        DBUtil db = new DBUtil();
        try {
            PreparedStatement prepStmt = db.getConn().prepareStatement("DELETE FROM " + Constantes.DB_OTASS013_JOBS_STATUS_Table + " WHERE id=?;");
            prepStmt.setInt(1, this.getId());

            //execute SQL
            db.doINSERT(prepStmt);

            //Close stmt
            prepStmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //Close conn
        db.closeConn();
    }

    //===================================Getters and Setters===========================
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getFullOutput() {
        return fullOutput;
    }

    public void setFullOutput(String fullOutput) {
        this.fullOutput = fullOutput;
    }

    public boolean isMailSent() {
        return mailSent;
    }

    public void setMailSent(boolean mailSent) {
        this.mailSent = mailSent;
    }
}
