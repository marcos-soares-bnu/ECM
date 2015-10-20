package logic_process;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import database.DBOTASS_Job;
import database.DBUtil;
import object.OBJOTASS013_JobStatus;
import text.FileReaderUtil;
import util.Constantes;

public class OTASS013Logic {
    //passar o nome dos jobs para DBOTASS_Job
    //Se o job não existe, já será cadastrado.

    //Já terá os status caso existam.
    //Caso não, terá o parâmetro newJOB setado para true.

    private Map<String, OBJOTASS013_JobStatus> jobMap = new HashMap<String, OBJOTASS013_JobStatus>();
    private String strOutputCheck;

    public static void main(String[] args) {
        OTASS013Logic o = new OTASS013Logic();
        DBUtil db = new DBUtil();
        o.readLOG();
        o.splitLogTextLine();

        for (OBJOTASS013_JobStatus readedStatus : o.getJobMap().values()) {
            DBOTASS_Job DBjob = new DBOTASS_Job(readedStatus.getJobName());
            System.out.println("======================================");
            System.out.println(readedStatus.getFullMessage());
            System.out.println("======================================");
            //Confere status antigos
            Map<Integer, OBJOTASS013_JobStatus> statusMap = DBjob.getStatusMap();
            if (statusMap.size() < 1) {
                //Nenhum status
                //Deve ser cadastrados os status encontrados
                String fields = "job_id, status, output, mail_sent, full_output";
                try {
                    PreparedStatement psmtINS = db.getConn().prepareStatement("INSERT INTO " + Constantes.DB_OTASS013_JOBS_STATUS_Table + "(" + fields + ")" + " VALUES (?, ?, ?, ?, ?);");
                    psmtINS.setInt(1, DBjob.getId());
                    psmtINS.setString(2, readedStatus.getStatus());
                    psmtINS.setString(3, readedStatus.getOutput());
                    psmtINS.setInt(4, 0);
                    psmtINS.setString(5, readedStatus.getFullMessage());

                    db.doINSERT(psmtINS);
                    psmtINS.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else if (statusMap.size() == 1) {
                //apenas 1 status
                //deve ser atualizado o status
                OBJOTASS013_JobStatus stats = statusMap.values().iterator().next();
                if (readedStatus.isError()) {
                    if (readedStatus.getOutput().equalsIgnoreCase(stats.getOutput())) {
                        //Atualiza (apenas o full output)
                        try {
                            PreparedStatement psmtINS = db.getConn().prepareStatement("UPDATE " + Constantes.DB_OTASS013_JOBS_STATUS_Table + " SET full_output=? WHERE id=" + stats.getId() + ";");
                            psmtINS.setString(1, readedStatus.getFullMessage());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    } else {
                        //Cadastra novo
                        String fields = "job_id, status, output, mail_sent, full_output";
                        try {
                            PreparedStatement psmtINS = db.getConn().prepareStatement("INSERT INTO " + Constantes.DB_OTASS013_JOBS_STATUS_Table + "(" + fields + ")" + " VALUES (?, ?, ?, ?, ?);");
                            psmtINS.setInt(1, DBjob.getId());
                            psmtINS.setString(2, readedStatus.getStatus());
                            psmtINS.setString(3, readedStatus.getOutput());
                            psmtINS.setInt(4, 0);
                            psmtINS.setString(5, readedStatus.getFullMessage());

                            db.doINSERT(psmtINS);
                            psmtINS.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    //seta status para ok
                    try {
                        PreparedStatement psmtINS = db.getConn().prepareStatement("UPDATE " + Constantes.DB_OTASS013_JOBS_STATUS_Table + " SET status=?, output=?, full_output=?, mail_sent=? WHERE id=" + stats.getId() + ";");
                        psmtINS.setString(1, readedStatus.getStatus());
                        psmtINS.setString(2, readedStatus.getOutput());
                        psmtINS.setString(3, readedStatus.getFullMessage());
                        psmtINS.setInt(4, 0);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                //mais que um status
                //deve ser atualizado o/os status
                for (OBJOTASS013_JobStatus status : statusMap.values()) {
                    if (readedStatus.isError()) {
                        if (readedStatus.getOutput().equalsIgnoreCase(status.getOutput())) {
                            
                        }
                    } else {
                        //Atualiza tudo para OK
                        System.out.println(readedStatus.getFullMessage());
                    }
                }
            }

        }
        db.closeConn();

    }

    public void readLOG() {
        //Get all txt
        FileReaderUtil fileReader;
        if (Constantes.LINDE_ENVIRONMENT) {
            fileReader = new FileReaderUtil("D:\\IAS_Monitoring\\APP_Dev\\SCHEDScripts\\sched_otass.log");
        } else {
            fileReader = new FileReaderUtil("C:\\Temp\\script result\\sched_otass.log");
        }
        strOutputCheck = fileReader.readFile();
    }

    public void splitLogTextLine() {
        String[] OTASS013 = strOutputCheck.split("OTASS013");
        OTASS013 = OTASS013[1].split(Constantes.STRING_ERRORS);

        String jobName;
        String execID;
        String status;
        String time;
        String message;

        String[] jobINFOS = OTASS013[1].split(Constantes.STRING_JOBNAME);
        for (int i = 0; i < jobINFOS.length; i++) {
            if (jobINFOS[i].length() > 3) {
                jobINFOS[i] = jobINFOS[i].replace("\n", "");
                jobINFOS[i] = jobINFOS[i].replace(Constantes.STRING_ID, "\n");
                jobINFOS[i] = jobINFOS[i].replace(Constantes.STRING_STATUS, "\n");
                jobINFOS[i] = jobINFOS[i].replace(Constantes.STRING_TIME, "\n");
                jobINFOS[i] = jobINFOS[i].replace(Constantes.STRING_MESSAGE, "\n");

                String[] strTratada = jobINFOS[i].split("\n");
                jobName = strTratada[0];
                execID = strTratada[1];
                status = strTratada[2];
                time = strTratada[3];
                message = strTratada[4];

                OBJOTASS013_JobStatus job = new OBJOTASS013_JobStatus(jobName, execID, status, time, message);
                jobMap.put(job.getJobName(), job);
            }
        }
    }

    public Map<String, OBJOTASS013_JobStatus> getJobMap() {
        return jobMap;
    }
}
