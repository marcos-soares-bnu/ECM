package object;

import java.util.Date;

import util.DateUtil;

public class OBJPIXSchedTaskFileSet {

    private DateUtil dtUtil = new DateUtil();
    
    private String directory;
    private String expFile;

    public OBJPIXSchedTaskFileSet(String directory, String expFile) {
        this.directory = directory;
        this.expFile = expFile;
    }

    public OBJPIXSchedTaskFileSet() {
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getExpFile() {
        return expFile;
    }

    public void setExpFile(String expFile) {
        this.expFile = expFile;
    }

    public Date getDirDate() {
        Date dirDate = dtUtil.getFileDatePIX(this.getDirectory());
        return dirDate;
    }
    
    public boolean hasDir() {
        return !this.getDirectory().isEmpty();
    }
    
    public boolean isDirOlderThan1hLS(Date taskLastRun) {
        boolean isOlder = false;
        long hDiff = dtUtil.getHoursDif(this.getDirDate(), taskLastRun);
        if (hDiff > 1) {
            isOlder = true;
        }
        return isOlder;
    }
    
    public String toString() {
        String strInfos = "";
        if (this.getDirectory() != null && !this.getDirectory().isEmpty()) {
            strInfos += this.getDirectory() + "\n";
        }
        if (this.getExpFile() != null && !this.getExpFile().isEmpty()) {
            strInfos += this.getExpFile();
        }
        return strInfos;
    }
}
