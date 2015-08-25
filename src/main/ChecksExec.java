package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

                    	//To be Implemented...
                    }
                    else if (cItem.getItemName().equals("DPWIN001") || cItem.getItemName().equals("DPWIN002")){

                        //record list of New Errors/Exists on DB...
                        this.recordArrCheckIsNewErrorTasks(checkID, checkItemID, err, exec_time, status);
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
    	String aux_err = err.getOutput_error().replace("\n ","\n");
    	String[] arrayMonErrors = aux_err.split("\n");

    	//Store just items of checkID and checkItemID...
        List<String> arrayItemLastErrors = new ArrayList<String>();
        List<String> arrayItemLastErrors_fullName = new ArrayList<String>();
        List<String> arrayItemLastErrors_partName = new ArrayList<String>();

        for (DBCheckOutput out : dbLastErrors.values()) {
            if (out.getCheck_id() == checkID && out.getCheck_item_id() == checkItemID) {
                arrayItemLastErrors_fullName.add(out.getOutput_error());
                
                //==========================================================
                //
                int indSpc = out.getOutput_error().indexOf(" ");
                int indTsk = out.getOutput_error().indexOf("(");
                
                if ( (indSpc > 0) && (checkID == 1))
                	arrayItemLastErrors_partName.add(out.getOutput_error().substring(0, indSpc));
                else if ( (indTsk > 0) && (checkID == 3))
                	arrayItemLastErrors_partName.add(out.getOutput_error().substring(0, indTsk));
                else
                	arrayItemLastErrors_partName.add(out.getOutput_error());
                //==========================================================
            }
        }

        //Test if aux_err is empty...
        if (aux_err.length() > 0){
        	//
	        for (String s : arrayMonErrors) {
	        	//
	        	int indSpc = s.indexOf(" ");
	        	int indTsk = s.indexOf("(");
	        	
	        	//======================================================================
	        	//If CheckID = 1 (INFRA), CheckID = 3 (DPWIN), compare partName, else s
	        	//======================================================================
	        	String partName;
	        	if (checkID == 1){
	        		if (indSpc > 0)
	        			partName = s.substring(0, indSpc);
	        		else
	        			partName = s;
	        		//
	        		arrayItemLastErrors = arrayItemLastErrors_partName;
	        	}
	        	else if (checkID == 3){
	        		if (indTsk > 0)
	        			partName = s.substring(0, indTsk);
	        		else
	        			partName = s;
	        		//
	        		arrayItemLastErrors = arrayItemLastErrors_partName;
	        	}
	        	else{
	        		partName = s;
	        		arrayItemLastErrors = arrayItemLastErrors_fullName;
	        	}
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
        //If aux_err is empty, it means that search of tasks return no errors...
        else {
        	DBCheckOutput dbOutput = new DBCheckOutput(checkID, checkItemID, "OK", "No Errors.", exec_time, 0); //isNew=0);
        	dbOutput.DB_store();
        }
    }
    
    //MPS - logic TASKS...
    private void recordArrCheckIsNewErrorTasks(int checkID, int checkItemID, OBJCheckOutput err, Date exec_time, String status) {

    	List<String> arrayMonErrorsTasksCheck = new ArrayList<String>();
    	List<String> arrayMonErrorsTasks;
    	List<String> arrayMonErrorsLogicSW;
    	
    	//Check if exists STRING_LOGICSWITCH...
    	boolean isNullLOGIC = false;
    	int indLOGIC = 0;
    	int indLOGICLEN = 0;
    	indLOGIC = err.getOutput_error().indexOf(Constantes.STRING_LOGICSWITCH);
    	indLOGICLEN = indLOGIC + Constantes.STRING_LOGICSWITCH.length();
    	
    	//Store items of Log file...
    	if (indLOGIC > 0){

    		arrayMonErrorsTasks = Arrays.asList(err.getOutput_error().substring(0, indLOGIC).split("TaskName:"));
    		arrayMonErrorsLogicSW = Arrays.asList(err.getOutput_error().substring(indLOGICLEN).split("\n"));
    	}
    	else{

    		arrayMonErrorsTasks = Arrays.asList(err.getOutput_error().split("TaskName:"));
    		arrayMonErrorsLogicSW = null;
        	isNullLOGIC = true;
    	}
    	
    	//Clean spaces and set new string to tasks list...
    	int lenState = Constantes.STRING_TKSTAT.length();
    	int lenResult = Constantes.STRING_TKLRES.length();
    	int indResult = 0;
    	int indState = 0;
    	String tmpState = "";
    	String tmpResult = "";
    	//
    	for (String s : arrayMonErrorsTasks) {

    		if (s.length() > 0){
    			//
    			indResult = s.indexOf(Constantes.STRING_TKLRES);
    			indState = s.indexOf(Constantes.STRING_TKSTAT);
    			
    			if ((indResult > 0) && (indState > 0)){
	    			//
	    	    	tmpResult = s.substring(indResult+lenResult, indState);
	    	    	tmpState = s.substring(indState+lenState).replace("\n","");
	    			//
	    			s = s.replace(Constantes.STRING_TKNRUN, "(");
	    			s = s.replace(Constantes.STRING_TKLRUN, ")(");
	    			s = s.replace(Constantes.STRING_TKLRES, ")(");
	    			s = s.replace(Constantes.STRING_TKSTAT, ")(");
	    			s = s.replace(" ", "");
	    			s = s.replace("\n", "");
	    			//
	    			//------------------------------------
	    			//Check if Last Result and Task State 
	    			//exists and diff (0 && 267009) and 
	    			//diff (Enabled)
	    			if (tmpState.trim().toUpperCase().equals("ENABLED")){
	
	    				if	(	( !tmpResult.trim().equals("0") ) && ( !tmpResult.trim().equals("267009") ) ){
	
	    	    			arrayMonErrorsTasksCheck.add(s + ")\n");
	    				}
	    			}
	    			else{
	    				
		    			arrayMonErrorsTasksCheck.add(s + ")\n");
	    			}
    			}
    		}
		}
    	//--------------------------------------------------
    	//Remove Duplicate Erros e add to List...
        List<String> lst = new ArrayList<String>();
        
        //Test if arrayMonErrorsLogicSW is null...
        if (!isNullLOGIC){
	        lst = arrCountDuplicateItems(arrayMonErrorsLogicSW);
	    	for (String s : lst) {
	    		//
	    		if (s.length() > 0){
	    			arrayMonErrorsTasksCheck.add(s + "\n");
	    		}
	    	}
        }

    	//--------------------------------------------------
    	//record list of New Errors on DB...
    	//
    	String aux_err = arrayMonErrorsTasksCheck.toString().replace("\n ", "\n").replace(",","").replace("[", "").replace("]", "");
    	//
    	OBJCheckOutput outerr = new OBJCheckOutput(aux_err);
        this.recordArrCheckIsNewError(checkID, checkItemID, outerr, exec_time, status);
        
    }

    
    //===========================================================================
    // Remove Duplicated Items...
    //===========================================================================
    private List<String> arrRemoveDuplicateItems(List<String> arr1) {

        List<String> lst = new ArrayList<String>(arr1);
        //
        Set<String> set = new HashSet<String>();
        set.addAll(lst);
        lst.clear();
        lst.addAll(set);
        
        return lst;
    }  
    

    //===========================================================================
    // Remove e COUNT Duplicated Items... 25/8/2015 - by MPS
    //===========================================================================
    private List<String> arrCountDuplicateItems(List<String> arr1) {

    	int indDup = 0;
        List<String> lst = new ArrayList<String>(arr1);
        List<String> lstNDup = arrRemoveDuplicateItems(lst);
        List<String> lstDup = new ArrayList<String>(lst);
        List<String> lstCDup = new ArrayList<String>();
        //
        for (Object s : lstNDup) {
        	if (lstDup.contains(s)){
        		lstDup.remove(s);
        	}
        }
        //
        for (Object s : lstDup) {
        	indDup++;
        	lstCDup.add(s + " - " + indDup);
        }
        //
        for (Object s : lstNDup) {
        	if (s.toString().length() > 0)
        		lstCDup.add(s.toString());
        }
        //
        return lstCDup;
    }  
    
    //MPS - end...
    //==============================================================================
}
