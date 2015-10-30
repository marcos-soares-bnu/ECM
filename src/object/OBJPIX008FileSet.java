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
    //PIX008 retorna as datas no mesmo formato que DPWIN007.
    public Date getDirDate() {
        Date dirDate = dtUtil.getFileDateDPWIN007(this.getDirectory());    
        return dirDate;
    }
    
    public boolean hasDir() {
        return !(this.getDirectory() == null);
    }
    
    public boolean isDirOlderThan8h(Date timeNow) {
        boolean isOlder = false;
        long hDiff = dtUtil.getHoursDif(this.getDirDate(), timeNow);
        if (hDiff > 8) {
            isOlder = true;
        }
        return isOlder;
    }
    
    //Send 'S' as 2nd argument for '.sca' or 'I' for '.imp' files
    public boolean isFileMissing(Date timeNow, char typeOfFile) {
        boolean isMissing = false;
        String getFile = null;
        
        if(typeOfFile == 'S')
            getFile = this.getScaFile();
        else if(typeOfFile == 'I')
            getFile = this.getImpFile();
        
        if (getFile != null) {
            isMissing = false;
        } else {
            //If the file doesn't exists, check if the directory is older than one hour
            long hDiff = dtUtil.getHoursDif(this.getDirDate(), timeNow);
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
