package text;

public class TXTCheck {

    private String nomeCheck;
    private int priority;
    private String config;
    private String error;

    public TXTCheck(String nomeCheck, int priority, String config, String error) {
        this.nomeCheck = nomeCheck;
        this.priority = priority;
        this.config = config;
        this.error = error;
    }

    public String getNomeCheck() {
        return nomeCheck;
    }

    public void setNomeCheck(String nomeCheck) {
        this.nomeCheck = nomeCheck;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

}
