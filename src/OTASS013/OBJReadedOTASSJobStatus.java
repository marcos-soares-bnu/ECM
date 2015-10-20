package OTASS013;

public class OBJReadedOTASSJobStatus {

    private String jobName;
    private String execID;
    private String status;
    private String time;
    private String output;

    //===================================Constructor==================================
    public OBJReadedOTASSJobStatus(String jobName, String execID, String status, String time, String output) {
        this.jobName = jobName;
        this.execID = execID;
        this.status = status;
        this.time = time;
        this.output = output;
    }

    //===================================Other Methods=================================
    public boolean isError() {
        return (this.getStatus().equalsIgnoreCase("ERROR"));
    }

    public String getFullOutput() {
        String str = "";
        str += "JOB: " + this.getJobName();
        str += "\nExecID: " + this.getExecID();
        str += "\nStatus: " + this.getStatus();
        str += "\nTime: " + this.getTime();
        str += "\nMessage: " + this.getOutput();
        return str;
    }

    //===================================Getters and Setters===========================
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
}
