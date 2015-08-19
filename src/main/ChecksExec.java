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
                    dbCheckConfig.setPath_output("C:\\Temp\\script result\\sched_fcir2.log");
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
                    // MPS - ini...
                    //
                    //return list of New Erros and record on DB...
                    List<String> arrayItemLastErrors = new ArrayList<String>();                    
                    arrayItemLastErrors = this.arrCheckIsNewError(checkID, checkItemID, err);
                    //******************************************************************************
                    //Test Hard-code items set to more than 1 ticket/error line
                    //******************************************************************************
                    if	(	cItem.getItemName().equals("MON018")	|| cItem.getItemName().equals("MON019") 	||
                            cItem.getItemName().equals("MON020")	|| cItem.getItemName().equals("OTASS012") 	||
                       		cItem.getItemName().equals("OTASS013") 	|| cItem.getItemName().equals("DPWIN002")	){
                        //Insert in DB with flag is_new = 1...
                        for (String serr : arrayItemLastErrors) {
                            //Create the error output
                            DBCheckOutput dbOutput = new DBCheckOutput(checkID, checkItemID, status, serr, exec_time, 1); //isNew);
                            //grava as informações contidas no objCheck no banco
                            dbOutput.DB_store();
                        }
                    	//
                    	//Insert in DB with flag is_new = 0...
                        for (DBCheckOutput out : dbLastErrors.values()) {
                        	if ( (!out.getOutput_error().equals("No Errors.")) && (out.getCheck_id() == checkID && out.getCheck_item_id() == checkItemID) ) {
                        		//Create the error output
                                DBCheckOutput dbOutput = new DBCheckOutput(checkID, checkItemID, status, out.getOutput_error(), exec_time, 0); //isNew);
                                //grava as informações contidas no objCheck no banco
                                dbOutput.DB_store();
                        	}
                        }        
                    }
                    else{
                    	int isNew = this.checkIsNewError(checkID, checkItemID, err) ? 1 : 0;                    	
                        //Create the error output
                        DBCheckOutput dbOutput = new DBCheckOutput(checkID, checkItemID, status, output, exec_time, isNew);
                        //grava as informações contidas no objCheck no banco
                        dbOutput.DB_store();
                    }
                    // MPS - fim...
                    //==============================================================================
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

    //==============================================================================
    // MPS - ini...
    private boolean checkIsNewError(int checkID, int checkItemID, OBJCheckOutput err) {
        boolean isNew = true;
        for (DBCheckOutput out : dbLastErrors.values()) {
            if (out.getCheck_id() == checkID && out.getCheck_item_id() == checkItemID) {
                //Encontrou o item correto

                //Verifica se é o mesmo erro
                //if (out.getOutput_error().equalsIgnoreCase(err.getOutput_error())) {
                //-----------------------------------------------------------
                //Se o erro antigo não existir, isNew = false
                //Para todos os casos que se abre apenas um ticket por check,
                //Só será Novo erro se o anterior for = "No Errors."
                //-----------------------------------------------------------
                if (!out.getOutput_error().contains("No Errors.")) {
                    isNew = false;
                    break;
                }
            }
        }
        return isNew;
    }

    private List<String> arrCheckIsNewError(int checkID, int checkItemID, OBJCheckOutput err) {

        List<String> lstNew = new ArrayList<String>();

        //Store just items of checkID and checkItemID...
        List<String> arrayItemLastErrors = new ArrayList<String>();
        //
        for (DBCheckOutput out : dbLastErrors.values()) {
            if (out.getCheck_id() == checkID && out.getCheck_item_id() == checkItemID) {
                arrayItemLastErrors.add(out.getOutput_error());
            }
        }
        //

        //Split err, for more than One... 
        String[] arrayMonErrors = err.getOutput_error().split("\n");
        //

        lstNew = arrNewErrors(arrayMonErrors, arrayItemLastErrors);

        return lstNew;
    }

    // same as Arrays.equals()
    private List<String> arrNewErrors(String[] arr1, List<String> arr2) {

        //Store just items of checkID and checkItemID...
        List<String> arrayItemNewErrors = new ArrayList<String>(arr2);
        //

        for (String s : arr1) {
            if (arrayItemNewErrors.contains(s)) {
                arrayItemNewErrors.remove(s);
            } else {
                arrayItemNewErrors.add(s);
            }
        }
        //
        for (String s : arr2) {
            if (arrayItemNewErrors.contains(s)) {
                arrayItemNewErrors.remove(s);
            }
        }

        return arrayItemNewErrors;
    }  
    //MPS - fim...
    //==============================================================================
}
