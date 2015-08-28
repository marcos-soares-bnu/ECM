package text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class FileReaderUtil {

    private File file;
    private FileReader fileReader;
    private BufferedReader bufferedReader;

    public FileReaderUtil(String file) {
        setFile(new File(file));
        if (this.getFile().exists()) {
            try {
                setFileReader(new FileReader(this.getFile()));
                setBufferedReader(new BufferedReader(this.getFileReader()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    public FileReader getFileReader() {
        return fileReader;
    }

    public void setFileReader(FileReader fr) {
        this.fileReader = fr;
    }

    public BufferedReader getBufferedReader() {
        return bufferedReader;
    }

    public void setBufferedReader(BufferedReader br) {
        this.bufferedReader = br;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File f) {
        this.file = f;
    }

    /**
     * Read all lines from the file and return it in String Format
     * 
     * @return all lines from the file
     */
    public String readFile() {
        String fileRows = "";
        String line = "";
        if (this.getFile().exists()) {
        	try {
        		while ((line = this.getBufferedReader().readLine()) != null) {
        			fileRows += line + "\n";
        		}
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
        }
        return fileRows;
    }
}
