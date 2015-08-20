package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import database.DBCheck;
import database.DBCheckConfig;
import database.DBCheckItem;
import database.DBCheckOutput;
import object.OBJCheck;
import object.OBJCheckItem;
import object.OBJCheckOutput;
import text.CheckHandler;
import text.FileReaderUtil;
import util.Constantes;

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
        } else {
            //Tratamento para cada log:
            switch (objCheck.getId()) {
                case Constantes.DB_INFRA_ID:
                    dbCheckConfig.setPath_output("C:\\Temp\\script result\\sched_infra.log");
                    break;
                case Constantes.DB_DPWIN_ID:
                    dbCheckConfig.setPath_output("C:\\Temp\\script result\\sched_dpwin.log");
                    break;
                case Constantes.DB_SQL_ID:
                    dbCheckConfig.setPath_output("C:\\Temp\\script result\\sched_sql.log");
                    break;
                case Constantes.DB_FCIR_ID:
                    dbCheckConfig.setPath_output("C:\\Temp\\script result\\sched_fcir.log");
                    break;
                case Constantes.DB_PIXCORE_ID:
                    dbCheckConfig.setPath_output("C:\\Temp\\script result\\sched_pixcore.log");
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
                    
                    //==============================================================================
                    // MPS - start...
                    //
                    //******************************************************************************
                    //Test Hard-code items set to more than 1 ticket/error line
                    //******************************************************************************
                    if (cItem.getItemName().equals("MON018") || cItem.getItemName().equals("MON019") || cItem.getItemName().equals("MON020")){

                        //record list of New Errors/Exists on DB...
                        this.recordArrCheckIsNewError(checkID, checkItemID, err, exec_time, status);
                    }
                    else if	(cItem.getItemName().equals("OTASS012") || cItem.getItemName().equals("OTASS013")){

                    	//Check Details...
                    }
                    else if (cItem.getItemName().equals("DPWIN001") || cItem.getItemName().equals("DPWIN002")){

                    	//Check Details...
                    }
                    else{
                    	int isNew = this.checkIsNewError(checkID, checkItemID, err) ? 1 : 0;                    	
                    	//Create the error output
                    	//Record objCheck information on DB...
                        DBCheckOutput dbOutput = new DBCheckOutput(checkID, checkItemID, status, output, exec_time, isNew);
                        dbOutput.DB_store();
                    }
                    // MPS - end...
                    //==============================================================================
                }
            } else {
                String output = "No Errors.";
            	//Create the error output
            	//Record objCheck information on DB...
                DBCheckOutput dbOutput = new DBCheckOutput(checkID, checkItemID, status, output, exec_time);
                dbOutput.DB_store();
            }
        }
    }

    //==============================================================================
    // MPS - start...
    private boolean checkIsNewError(int checkID, int checkItemID, OBJCheckOutput err) {
        boolean isNew = true;
        for (DBCheckOutput out : dbLastErrors.values()) {
            if (out.getCheck_id() == checkID && out.getCheck_item_id() == checkItemID) {

                //if (out.getOutput_error().equalsIgnoreCase(err.getOutput_error())) {
                //-----------------------------------------------------------
                if (!out.getOutput_error().contains("No Errors.")) {
                    isNew = false;
                    break;
                }
            }
        }
        return isNew;
    }

    private void recordArrCheckIsNewError(int checkID, int checkItemID, OBJCheckOutput err, Date exec_time, String status) {

    	//Store items of Log file...
        String[] arrayMonErrors = err.getOutput_error().split("\n");

    	//Store just items of checkID and checkItemID...
        List<String> arrayItemLastErrors = new ArrayList<String>();

        for (DBCheckOutput out : dbLastErrors.values()) {
            if (out.getCheck_id() == checkID && out.getCheck_item_id() == checkItemID) {
                arrayItemLastErrors.add(out.getOutput_error());
            }
        }

        for (String s : arrayMonErrors) {

        	//=================================================================
        	//If CheckID = 1 (Infra), compare partName, else s
        	//=================================================================
        	String partName;
        	if (checkID == 1)
        		partName = s.substring(0, s.indexOf(" "));
        	else
        		partName = s;
        	//
        	//Create the error output
        	//Record objCheck information on DB...
            if (arrayItemLastErrors.contains(partName)) {
            	DBCheckOutput dbOutput = new DBCheckOutput(checkID, checkItemID, status, s, exec_time, 0); //isNew=0);
            	dbOutput.DB_store();
            } else {
            	DBCheckOutput dbOutput = new DBCheckOutput(checkID, checkItemID, status, s, exec_time, 1); //isNew=1);
            	dbOutput.DB_store();
            }
        }
    }  
    //MPS - end...
    //==============================================================================
}
