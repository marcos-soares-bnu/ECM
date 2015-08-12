package object;

import java.util.Date;

import util.DateUtil;

public class OBJPIX008FileSet {

    private DateUtil dtUtil = new DateUtil();
    
    private String directory;
    private String impFile;
    private String scaFile;

    public OBJPIX008FileSet() {

    }

    public OBJPIX008FileSet(String directory, String impFile, String scaFile) {
        this.directory = directory;
        this.impFile = impFile;
        this.scaFile = scaFile;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getImpFile() {
        return impFile;
    }

    public void setImpFile(String impFile) {
        this.impFile = impFile;
    }

    public String getScaFile() {
        return scaFile;
    }

    public void setScaFile(String scaFile) {
        this.scaFile = scaFile;
    }

    public Date getDirDate() {
        Date dirDate = dtUtil.getFileDatePIX(this.getDirectory());
        return dirDate;
    }
    
    public boolean hasDir() {
        return !this.getDirectory().isEmpty();
    }
    
    public boolean isDirOlderThan8h(Date timeNow) {
        boolean isOlder = false;
        long hDiff = dtUtil.getHoursDif(timeNow, this.getDirDate());
        if (hDiff > 8) {
            isOlder = true;
        }
        return isOlder;
    }
    
    public boolean isImpMissing(Date timeNow) {
        boolean isMissing = false;
        
        if (this.getImpFile() != null) {
            isMissing = false;
        } else {
            long hDiff = dtUtil.getHoursDif(timeNow, this.getDirDate());
            if (hDiff > 1) {
                isMissing = true;
            }
        }
        
        return isMissing;
    }
    
    public String toString() {
        String strInfos = "";
        if (this.getDirectory() != null && !this.getDirectory().isEmpty()) {
            strInfos += this.getDirectory() + "\n";
        }
        if (this.getImpFile() != null && !this.getImpFile().isEmpty()) {
            strInfos += this.getImpFile() + "\n";
        }
        if (this.getScaFile() != null && !this.getScaFile().isEmpty()) {
            strInfos += this.getScaFile();
        }
        return strInfos;
    }
    
}
