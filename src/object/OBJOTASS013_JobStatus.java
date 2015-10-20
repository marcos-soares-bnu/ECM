package object;

public class OBJOTASS013_JobStatus {

    private int id;
    private String jobName;
    private String execID;
    private String status;
    private String time;
    private String output;
    private boolean mail_sent;

    public OBJOTASS013_JobStatus(String jobName, String execID, String status, String time, String output) {
        this.jobName = jobName;
        this.execID = execID;
        this.status = status;
        this.time = time;
        this.output = output;
    }

    public OBJOTASS013_JobStatus(int id, String status, String output, boolean bolMail_sent) {
        this.id = id;
        this.status = status;
        this.output = output;
        this.mail_sent = bolMail_sent;
    }

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

    public String getExecID() {
        return execID;
    }

    public void setExecID(String execID) {
        this.execID = execID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public boolean isMail_sent() {
        return mail_sent;
    }

    public void setMail_sent(boolean mail_sent) {
        this.mail_sent = mail_sent;
    }
        
    public boolean isError() {
        return (this.getStatus().equalsIgnoreCase("ERROR"));
    }

    public String getFullMessage() {
        String str = "";
        str += "ID: " + this.getId();
        str += "\nJOB: " + this.getJobName();
        str += "\nExecID: " + this.getExecID();
        str += "\nStatus: " + this.getStatus();
        str += "\nTime: " + this.getTime();
        str += "\nMessage: " + this.getOutput();
        str += "\nMail_Sent " + this.isMail_sent();
        return str;
    }
}
