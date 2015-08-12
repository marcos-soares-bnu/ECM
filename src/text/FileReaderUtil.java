package text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FileReaderUtil {

    private File f;
    private FileReader fr;
    private BufferedReader br;

    public FileReaderUtil(String file) {
        setF(new File(file));
        if (this.getF().exists()) {
            try {
                setFr(new FileReader(this.getF()));
                setBr(new BufferedReader(this.getFr()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    public FileReader getFr() {
        return fr;
    }

    public void setFr(FileReader fr) {
        this.fr = fr;
    }

    public BufferedReader getBr() {
        return br;
    }

    public void setBr(BufferedReader br) {
        this.br = br;
    }

    public File getF() {
        return f;
    }

    public void setF(File f) {
        this.f = f;
    }

    /**
     * Read all lines from the file and return it in String Format
     * 
     * @return all lines from the file
     */
    public String readFile() {
        String fileRows = "";
        String line = "";
        if (this.getF().exists()) {
        	try {
        		while ((line = this.getBr().readLine()) != null) {
        			fileRows += line + "\n";
        		}
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
        }
        return fileRows;
    }
}
