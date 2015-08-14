package main;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.swing.JOptionPane;

import object.OBJCheck;
import object.OBJCheckItem;
import object.OBJCheckOutput;
import text.CheckHandler;
import text.FileReaderUtil;
import database.DBCheck;
import database.DBCheckConfig;
import database.DBCheckItem;
import database.DBCheckOutput;

//MPS - teste commit MPS_work...
import util.Constantes;
//

public class ChecksExec {

    private Date execTime;
    private OBJCheck objCheck;
    private DBCheckConfig dbCheckConfig;

    private Map<Integer, DBCheckOutput> dbLastErrors;

    public ChecksExec(int checkID, Date exec) {
        this.execTime = exec;
        dbCheckConfig = new DBCheckConfig(checkID);

        //Get checkInfos
        DBCheck dbCheck = new DBCheck(checkID);
        //Get checkItens
        DBCheckItem dbCheckItem = new DBCheckItem(checkID);

        //Get old errors from this check
        DBCheckOutput dbOLDCheckOut = new DBCheckOutput(checkID, execTime);
        dbLastErrors = dbOLDCheckOut.DB_retrieveLastErrors();

        objCheck = new OBJCheck(checkID, dbCheck.getCheck_name(), dbCheckItem.getItens(), dbLastErrors, execTime);
    }

    public Date getExecTime() {
        return execTime;
    }

    public void setExecTime(Date execTime) {
        this.execTime = execTime;
    }

    public OBJCheck getObjCheck() {
        return objCheck;
    }

    public void setObjCheck(OBJCheck objCheck) {
        this.objCheck = objCheck;
    }

    public void execCheck() {
        
    	if (Constantes.LINDE_ENVIRONMENT) {
    		this.callCMD();
    	}
    	else {
    		
    		//Tratamento para cada log:
            switch (objCheck.getId()) {
                case Constantes.DB_INFRA_ID:
                    dbCheckConfig.setPath_output("C:\\Temp\\script_result\\sched_infra.log");
                    break;
                case Constantes.DB_DPWIN_ID:
                    dbCheckConfig.setPath_output("C:\\Temp\\script_result\\sched_dpwin.log");
                    break;
                case Constantes.DB_SQL_ID:
                    dbCheckConfig.setPath_output("C:\\Temp\\script_result\\sched_sql.log");
                    break;
                case Constantes.DB_FCIR_ID:
                    dbCheckConfig.setPath_output("C:\\Temp\\script_result\\sched_fcir.log");
                    break;
                case Constantes.DB_PIXCORE_ID:
                    dbCheckConfig.setPath_output("C:\\Temp\\script_result\\sched_pixcore.log");
                    break;
                    
                default:
                    break;
            }
    	}
        this.checkCMDOutput();
    }

    private void callCMD() {
        try {
            Process p = Runtime.getRuntime().exec("cmd /c start /min /wait " + dbCheckConfig.getPath_cmd());
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void checkCMDOutput() {
        //Get all txt
        FileReaderUtil fileReader = new FileReaderUtil(dbCheckConfig.getPath_output());
        String strOutputCheck = fileReader.readFile();

        //Processar os dados lidos e inserir os mesmos no objCheck
        CheckHandler checkTxtHandler = new CheckHandler(strOutputCheck, objCheck);
        checkTxtHandler.handleTextFile();
    }

    public void storeINDB() {
        int checkID = objCheck.getId();
        Date exec_time = objCheck.getExec_time();

        for (OBJCheckItem cItem : objCheck.getItens().values()) {
            int checkItemID = cItem.getId();
            String status = cItem.getStatus();

            if (cItem.getStatus().equalsIgnoreCase("NOK") || cItem.getStatus().equalsIgnoreCase("WARNING")) {
                for (OBJCheckOutput err : cItem.getErrors().values()) {
                    String output = err.getOutput_error();
                    int isNew = this.checkIsNewError(checkID, checkItemID, err) ? 1 : 0;

                    //Create the error output
                    DBCheckOutput dbOutput = new DBCheckOutput(checkID, checkItemID, status, output, exec_time, isNew);
                    //grava as informações contidas no objCheck no banco
                    dbOutput.DB_store();
                }
            } else {
                String output = "No Errors.";
                //Create the ok output
                DBCheckOutput dbOutput = new DBCheckOutput(checkID, checkItemID, status, output, exec_time);
                //grava as informações contidas no objCheck no banco
                dbOutput.DB_store();
            }
        }
    }

    private boolean checkIsNewError(int checkID, int checkItemID, OBJCheckOutput err) {
        boolean isNew = true;
        for (DBCheckOutput out : dbLastErrors.values()) {
            if (out.getCheck_id() == checkID && out.getCheck_item_id() == checkItemID) {
                //Encontrou o item correto

                //Verifica se é o mesmo erro
                if (out.getOutput_error().equalsIgnoreCase(err.getOutput_error())) {
                    isNew = false;
                    break;
                }
            }
        }
        return isNew;
    }
}
