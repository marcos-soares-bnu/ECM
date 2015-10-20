package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import object.OBJOTASS013_JobStatus;
import util.Constantes;

public class DBOTASS_Job {

    private DBUtil db;

    private int id;
    private String jobName;
    private boolean newJOB;

    private Map<Integer, OBJOTASS013_JobStatus> statusMap = new HashMap<Integer, OBJOTASS013_JobStatus>();

    public DBOTASS_Job(String jobName) {
        if (!jobName.isEmpty()) {
            this.setJobName(jobName);
            this.DB_retrieveJobID();
            if (this.isNewJOB()) {
                //Criar o job
                this.createJOB(this.jobName);
                this.DB_retrieveJobID();
            } else {
                this.DB_RetrieveStatus();
            }
        } else {
            System.out.println("JobName is empty.");
        }
    }

    public void DB_retrieveJobID() {
        //metodo para pegar as infos do banco
        db = new DBUtil();
        String condition = "job_name='" + this.jobName + "'";
        ResultSet rs = db.doSelect("*", Constantes.DB_OTASS_JOBS_Table, condition);

        //Pega o ID do JOB
        try {
            if (rs.next()) {
                this.setId(rs.getInt("id"));
                this.setNewJOB(false);
            } else {
                this.setNewJOB(true);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
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

    private void DB_RetrieveStatus() {
        String condition = "job_id=" + this.getId();
        ResultSet rs = db.doSelect("*", Constantes.DB_OTASS013_JOBS_STATUS_Table, condition);
        try {
            while (rs.next()) {
                int id = rs.getInt("id");
                String status = rs.getString("status");
                String output = rs.getString("output");
                int mail_sent = rs.getByte("mail_sent");
                boolean bolMail_sent = (mail_sent == 1 ? true : false);

                OBJOTASS013_JobStatus jobStatus = new OBJOTASS013_JobStatus(id, status, output, bolMail_sent);
                statusMap.put(id, jobStatus);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e);
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public boolean isNewJOB() {
        return newJOB;
    }

    public void setNewJOB(boolean newJOB) {
        this.newJOB = newJOB;
    }

    public String getJobName() {
        return jobName;
    }
    
    public Map<Integer, OBJOTASS013_JobStatus> getStatusMap() {
        return statusMap;
    }
}
