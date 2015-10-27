package OTASS013;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import database.DBUtil;
import util.Constantes;

public class DBOTASSJob {

    //Database INFO
    private int id;
    private String jobName;
    private SortedMap<Integer, DBOTASSJobStatus> statusMap = new TreeMap<Integer, DBOTASSJobStatus>();

    //Logic INFO
    private DBUtil db;
    private boolean newJob;

    //Constructor
    public DBOTASSJob(String jobName) {
        if (!jobName.isEmpty()) {
            this.setJobName(jobName);
            this.DB_retrieveJobID();
            if (this.isNewJob()) {
                //Criar o job
                this.createJOB(this.jobName);
                this.DB_retrieveJobID();
            } else {
                this.DB_RetrieveStatus();
            }
        } else {
            System.err.println("JobName is empty.");
        }
        db.closeConn();
    }

    //===================================DB Functions==================================
    public void DB_retrieveJobID() {
        //metodo para pegar as infos do banco
        db = new DBUtil();
        String condition = "job_name='" + this.jobName + "'";
        ResultSet rs = db.doSelect("*", Constantes.DB_OTASS_JOBS_Table, condition);

        //Pega o ID do JOB
        try {
            if (rs.next()) {
                this.setId(rs.getInt("id"));
                this.setNewJob(false);
            } else {
                this.setNewJob(true);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void DB_RetrieveStatus() {
        String condition = "job_id=" + this.getId();
        ResultSet rs = db.doSelect("*", Constantes.DB_OTASS013_JOBS_STATUS_Table, condition);
        try {
            while (rs.next()) {
                int id = rs.getInt("id");
                String status = rs.getString("status");
                String output = rs.getString("output");
                String fullOutput = rs.getString("full_output");
                int mail_sent = rs.getByte("mail_sent");
                boolean bolMail_sent = (mail_sent == 1 ? true : false);

                DBOTASSJobStatus jobStatus = new DBOTASSJobStatus(id, this.getId(), status, output, fullOutput, bolMail_sent);
                statusMap.put(id, jobStatus);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void createJOB(String jobName) {
        String fields = "job_name";

        PreparedStatement pstmt = null;
        try {
            pstmt = db.getConn().prepareStatement("INSERT INTO " + Constantes.DB_OTASS_JOBS_Table + "(" + fields + ")" + " VALUES (?);");
            pstmt.setString(1, jobName);

            if (Constantes.SHOW_DB_MESSAGES) {
                System.out.println(pstmt);
            }
            db.doINSERT(pstmt);
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e);
        }
    }

    //===================================Getters and Setters===========================
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Map<Integer, DBOTASSJobStatus> getStatusMap() {
        return statusMap;
    }

    public void setStatusMap(SortedMap<Integer, DBOTASSJobStatus> statusMap) {
        this.statusMap = statusMap;
    }

    public boolean isNewJob() {
        return newJob;
    }

    public void setNewJob(boolean newJob) {
        this.newJob = newJob;
    }

}
