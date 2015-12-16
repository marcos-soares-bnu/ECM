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
import secure.DecryptOTASS;
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
        	
        	//MPS - Add var para testes dos Cmds com intervalos...
        	if (Constantes.LINDE_CMDS_INTERVAL){
        		
				ECMchecksCmds ecmCmds = new ECMchecksCmds(String.valueOf(objCheck.getId()), dbCheckConfig.getPath_cmd());
				try { ecmCmds.callCmdsInterval(); }
				catch (IOException e) { e.printStackTrace(); }
        	}
        	else {
	            if (objCheck.getId() == 2) {
	                //If OTASS, call special implementation
	                this.callCMD(new DecryptOTASS().getUserandPass());
	            } else {
	                //Else, call standard implementation
	                this.callCMD();
	            }
        	}
            
        } else {
        	
//*******************************************************************************************
        	//MPS - Add var para testes dos Cmds com intervalos...
        	if (Constantes.LINDE_CMDS_INTERVAL){
        		
				ECMchecksCmds ecmCmds = new ECMchecksCmds(String.valueOf(objCheck.getId()), dbCheckConfig.getPath_cmd());
				try { ecmCmds.callCmdsInterval(); }
				catch (IOException e) { e.printStackTrace(); }
        	}
        	else {
        	
            //Tratamento para cada log:
            switch (objCheck.getId()) {
                case Constantes.DB_INFRA_ID:
                    dbCheckConfig.setPath_output("C:\\Temp\\script result\\sched_infra_OK.log");
                    break;
                case Constantes.DB_OTASS_ID:
                    dbCheckConfig.setPath_output("C:\\Temp\\script result\\sched_otass_OK.log");
                    break;
                case Constantes.DB_DPWIN_ID:
                    dbCheckConfig.setPath_output("C:\\Temp\\script result\\sched_dpwin_OK.log");
                    break;
                case Constantes.DB_SQL_ID:
                    dbCheckConfig.setPath_output("C:\\Temp\\script result\\sched_sql_OK.log");
                    break;
                case Constantes.DB_FCIR_ID:
                    dbCheckConfig.setPath_output("C:\\Temp\\script result\\sched_fcir_OK.log");
                    break;
                case Constantes.DB_PIXCORE_ID:
                    dbCheckConfig.setPath_output("C:\\Temp\\script result\\sched_pixcore_OK.log");
                    break;
                case Constantes.DB_ICC_ID:
                    dbCheckConfig.setPath_output("C:\\Temp\\script result\\sched_icc.log");
                    break;

                default:
                    break;
                    
            	}
        	}
//*******************************************************************************************
        }
        this.checkCMDOutput();
    }

    public int callCheckCMD(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec("cmd /c start /min /wait " + cmd);
            p.waitFor();
            return p.exitValue();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return 999;
            //JOptionPane.showMessageDialog(null, e);
        }
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

    private void callCMD(String[] userAndPass) {
        try {
            Process p = Runtime.getRuntime().exec("cmd /c start /min /wait " + dbCheckConfig.getPath_cmd() + " " + userAndPass[0] + " " + userAndPass[1]);
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

                    //******************************************************************************
                    //Test Hard-code items set to more than 1 ticket/error line
                    //******************************************************************************
                    if (cItem.getItemName().equals("MON018") || cItem.getItemName().equals("MON019") || cItem.getItemName().equals("MON020")) {

                        //record list of New Errors/Exists on DB...
                        this.recordArrCheckIsNewError(checkID, checkItemID, err, exec_time, status);
                    }else if (cItem.getItemName().equals("OTASS002")) {
                        this.checkOtass002IsNewError(checkID, checkItemID, err, exec_time, status);
                    } else if (cItem.getItemName().equals("OTASS012")) {
                        String[] errors = err.getOutput_error().split("\n");
                        for (int i = 0; i < errors.length; i++) {
                            this.checkOtass012IsNewError(checkID, checkItemID, errors[i], exec_time, status);
                        }
                    } else if (cItem.getItemName().equals("OTASS013")) {
                        this.checkOtass013IsNewError(checkID, checkItemID, err, exec_time, status);
                    } else if (cItem.getItemName().equals("DPWIN001") || cItem.getItemName().equals("DPWIN002")) {
                        //record list of New Errors/Exists on DB...
                        this.recordArrCheckIsNewErrorTasks(checkID, checkItemID, err, exec_time, status);
                    } //else if (cItem.getItemName().equals("FCIR004")) {
//                        this.checkFCIR004IsNewError(checkID, checkItemID, err, exec_time, status);
//                    }
                    else if (cItem.getItemName().equals("FCIR005") || cItem.getItemName().equals("PIX014")) {
                        //MPS - Checks manually implemented...multiple tickets per Batches...  
                        this.recordArrCheckIsNewError(checkID, checkItemID, err, exec_time, status);
                    } else {
                        int isNew = this.checkIsNewError(checkID, checkItemID, err) ? 1 : 0;
                        //Create the error output
                        //Record objCheck information on DB...
                        DBCheckOutput dbOutput = new DBCheckOutput(checkID, checkItemID, status, output, exec_time, isNew);
                        
                        //OTASS013 new verifications
                        if (cItem.getItemName().equals("OTASS013")) {
                            dbOutput.setMail_sent(1);
                        }
                        dbOutput.DB_store();
                    }
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

    private boolean checkIsNewError(int checkID, int checkItemID, OBJCheckOutput err) {
        boolean isNew = true;
        for (DBCheckOutput out : dbLastErrors.values()) {
            if (out.getCheck_id() == checkID && out.getCheck_item_id() == checkItemID) {
                if (!out.getOutput_error().contains("No Errors.")) {
                    isNew = false;
                    break;
                }
            }
        }
        return isNew;
    }
    
//    private void checkFCIR004IsNewError(int checkID, int checkItemID, OBJCheckOutput err, Date exec_time, String status) {
//        String errosNovosDB = "";
//        String errosAntigosDB = "";
//        //Tratamento específico para erros detectados no logfile "ix_exf"
//        if (err.getOutput_error().contains("ix_exf.log")) {
//            //Array para guardar os erros novos.
//            String[] errosAtuais = err.getOutput_error().split("\n");
//            //ArrayList que recebe todos os erros antigos
//            List<String> errosAntigosFullname = new ArrayList<String>();
//            for (DBCheckOutput out : dbLastErrors.values()) {
//                if (out.getCheck_id() == checkID && out.getCheck_item_id() == checkItemID) {
//                    //Só adiciona no ArrayList, erros referentes ao FCIR004 e ao logfile "ix_exf"
//                        errosAntigosFullname.add(out.getOutput_error());
//                }
//            }
//            //Percorre cada erro novo, comparando-o com erros antigos
//            for (int j = 0; j < errosAtuais.length; j++) {
//                //Guarda somente dados relevantes, desprezando data/hora e task number
//                String erroNovoPartname;
//                if (errosAtuais[j].contains("Warning"))
//                    erroNovoPartname = errosAtuais[j].substring(err.getOutput_error().indexOf(".log"));
//                else
//                    erroNovoPartname =  errosAtuais[j].substring(err.getOutput_error().indexOf("\\"), err.getOutput_error().indexOf("#"));
//                //Se há erros antigos, então os atuais têm de ser comparados
//                if (!errosAntigosFullname.isEmpty()) {
//                    boolean ehAntigo = false;
//                    for (int i = 0; i < errosAntigosFullname.size(); i++) {
//                        if (errosAntigosFullname.get(i).contains(erroNovoPartname)) {
//                            //Se o erro atual se refere ao mesmo erro da verif. anterior, insere como erro antigo
//                            errosAntigosDB += "\n"+errosAtuais[j];
//                            ehAntigo = true;
//                        }
//                    }
//                    if (!ehAntigo)
//                        errosNovosDB += "\n"+errosAtuais[j];
//                    //Se não há erros antigos, o erro atual pode ser inserido com isNew=1
//                } else {
//                    errosNovosDB += "\n"+errosAtuais[j];
//                }
//            }
//            //Tratamento geral para outros tipos de logfile
//        } else {
//            
//            if (this.checkIsNewError(checkID, checkItemID, err)) {
//                errosNovosDB += "\n"+err.getOutput_error();
//            } else {
//                errosAntigosDB += "\n"+err.getOutput_error();
//            }
//        }
//        
//        if (errosNovosDB.length() > 0) {
//            errosNovosDB.trim();
//            //Insere os erros novos no BD
//            DBCheckOutput dbOutput = new DBCheckOutput(checkID, checkItemID, status, errosNovosDB, exec_time, 1);
//            dbOutput.DB_store();
//        }
//        if (errosAntigosDB.length() > 0) {
//            errosAntigosDB.trim();
//            //Insere os erros antigos no BD
//            DBCheckOutput dbOutput = new DBCheckOutput(checkID, checkItemID, status, errosAntigosDB, exec_time, 0);
//            dbOutput.DB_store();
//        }
//    }
    
    private void checkOtass002IsNewError(int checkID, int checkItemID, OBJCheckOutput err, Date exec_time, String status) {
        for (DBCheckOutput out : dbLastErrors.values()) {
            if (out.getCheck_id() == checkID && out.getCheck_item_id() == checkItemID) {
              //Utiliza a informação de indice abaixo para extrair somente nome do pool reportado
                int indicePrimeiroEspaco = out.getOutput_error().indexOf(" ");
                //Nome do pool apresentando erro atualmente
                String nomeDoPool = err.getOutput_error().substring(0, indicePrimeiroEspaco);
                if (out.getOutput_error().contains(nomeDoPool)) {
                  //Se o erro atual se refere ao mesmo erro da verif. anterior, insere como erro não-novo
                    DBCheckOutput dbOutput = new DBCheckOutput(checkID, checkItemID, status, err.getOutput_error(), exec_time, 0); //isNew=0);
                    dbOutput.DB_store();
                } else {
                  //Senão insere como erro novo
                    DBCheckOutput dbOutput = new DBCheckOutput(checkID, checkItemID, status, err.getOutput_error(), exec_time, 1); //isNew=1);
                    dbOutput.DB_store();
                }
            }
        }
        
    }
    
    private void checkOtass012IsNewError(int checkID, int checkItemID, String err, Date exec_time, String status) {
        boolean isOld = false;
        for (DBCheckOutput out : dbLastErrors.values()) {
            if (out.getCheck_id() == checkID && out.getCheck_item_id() == checkItemID) {
                //Utiliza as informações de indice abaixo para extrair somente nome do pool reportado
                int indicePrimeiroParentese = err.indexOf("(");
                int indiceSegundoParentese = err.indexOf(")");
                //Nome do pool apresentando erro atualmente
                String nomeDoPool = err.substring(indicePrimeiroParentese, indiceSegundoParentese);
                //Ao verificar que o erro atual já existe no DB, isOld = true.
                if (out.getOutput_error().contains(nomeDoPool))
                    isOld = true;
            }
        }
        //Se o erro atual se refere ao mesmo erro da verif. anterior, insere como erro não-novo
        if (isOld) {
            DBCheckOutput dbOutput = new DBCheckOutput(checkID, checkItemID, status, err, exec_time, 0); //isNew=0);
            dbOutput.DB_store();
        //Senão insere como erro novo    
        } else {
            DBCheckOutput dbOutput = new DBCheckOutput(checkID, checkItemID, status, err, exec_time, 1); //isNew=1);
            dbOutput.DB_store();
        }
    }

    private void checkOtass013IsNewError(int checkID, int checkItemID, OBJCheckOutput err, Date exec_time, String status) {
        String erros = err.getOutput_error().replace("\n", "");
        List<String> listaErros = Arrays.asList(erros.split(Constantes.STRING_JOBNAME));
        List<String> listaErrosTratados = new ArrayList<>();

        for (String s : listaErros) {
            if (s.length() > 1) {
                s = s.replace(Constantes.STRING_ID, "(");
                s = s.replace(Constantes.STRING_STATUS, ")(");
                s = s.replace(Constantes.STRING_TIME, ")(");
                s = s.replace(Constantes.STRING_MESSAGE, ")(");
                s = s.replace("\n", "");
                s = s + ")";
                listaErrosTratados.add(s);
            }
        }

        for (String s : listaErrosTratados) {
            OBJCheckOutput outerr = new OBJCheckOutput(s);
            this.recordArrCheckIsNewError(checkID, checkItemID, outerr, exec_time, status);
        }
    }

    private void recordArrCheckIsNewError(int checkID, int checkItemID, OBJCheckOutput err, Date exec_time, String status) {

        //Store items of Log file...
        String aux_err = err.getOutput_error().replace("\n ", "\n");
        String[] arrayMonErrors = aux_err.split("\n");

        //Store just items of checkID and checkItemID...
        List<String> arrayItemLastErrors = new ArrayList<String>();
        List<String> arrayItemLastErrors_fullName = new ArrayList<String>();
        List<String> arrayItemLastErrors_partName = new ArrayList<String>();
        //add PipeMom to compare when NDocs > 1000... - MPS - 11/09/2015
        List<String> arrayItemLastErrors_pipeMomName = new ArrayList<String>();

        for (DBCheckOutput out : dbLastErrors.values()) {
            if (out.getCheck_id() == checkID && out.getCheck_item_id() == checkItemID) {
                arrayItemLastErrors_fullName.add(out.getOutput_error());

                int indSpc = out.getOutput_error().indexOf(" ");
                int indTsk = out.getOutput_error().indexOf("(");
                int indUnd = out.getOutput_error().indexOf("_"); // NDocs > 0 - 11/09/2015

                if ((indSpc > 0) && (checkID == 1)) {
                    arrayItemLastErrors_partName.add(out.getOutput_error().substring(0, indSpc));
                    if (indUnd > 0) arrayItemLastErrors_pipeMomName.add(out.getOutput_error().substring(0, indUnd)); // NDocs > 0 - 11/09/2015
                } else if ((indTsk > 0) && (checkID == 3 || checkID == 2 || checkID == 5 || checkID == 6)) //Add FCIR005/6 And PIXCORE014 - MPS 25/09/2015
                arrayItemLastErrors_partName.add(out.getOutput_error().substring(0, indTsk));
                else arrayItemLastErrors_partName.add(out.getOutput_error());
            }
        }

        //Test if aux_err is empty...
        if (aux_err.length() > 0) {
            //
            for (String s : arrayMonErrors) {
                //
                int indSpc = s.indexOf(" ");
                int indSpc2 = 0;
                if (indSpc > 0) {
                    indSpc2 = s.substring(indSpc + 1).indexOf(" ") + indSpc + 1;
                }
                int indTsk = s.indexOf("(");

                //======================================================================
                //If CheckID = 1 (INFRA), CheckID = 2 (OTASS), CheckID = 3 (DPWIN)
                //   CheckID = 5 (FCIR),  CheckID = 6 (PIXCORE),compare partName, else s
                //======================================================================
                String partName = "";
                int nDocs = 0;
                if (checkID == 1) {
                    if (indSpc > 0) partName = s.substring(0, indSpc);
                    if (indSpc2 > 0) {
                        try {
                            nDocs = Integer.parseInt(s.substring(indSpc, indSpc2).trim());
                        } catch (Exception e) {
                            nDocs = 0;
                        }
                    } else partName = s;
                    //
                    arrayItemLastErrors = arrayItemLastErrors_partName;
                } else if (checkID == 3 || checkID == 2 || checkID == 5 || checkID == 6) { //Add FCIR005/6 And PIXCORE014 - MPS 25/09/2015
                    if (indTsk > 0) partName = s.substring(0, indTsk);
                    else partName = s;
                    //
                    arrayItemLastErrors = arrayItemLastErrors_partName;
                } else {
                    partName = s;
                    arrayItemLastErrors = arrayItemLastErrors_fullName;
                }
                //
                //Create the error output
                //Record objCheck information on DB...
                //
                //------------------------------------ Ndocs > 1000 (new rule - 10/9/2015)
                //------------------------------------ Pipeline NOT contains _error 
                if ((nDocs > 1000) && (partName.indexOf("_error") == -1)) {

                    //
                    String pipeMom_partName = partName.substring(0, partName.indexOf("_"));

                    //If contains a partname of Mom Pipeline...
                    if (arrayItemLastErrors_pipeMomName.contains(pipeMom_partName)) {
                        DBCheckOutput dbOutput = new DBCheckOutput(checkID, checkItemID, status, s, exec_time, 0); //isNew=0);
                        dbOutput.DB_store();
                    } else {
                        DBCheckOutput dbOutput = new DBCheckOutput(checkID, checkItemID, status, s, exec_time, 1); //isNew=1);
                        dbOutput.DB_store();
                    }
                } else {
                    // Ndocs <= 1000 = Normal Process...
                    if (arrayItemLastErrors.contains(partName)) {
                        DBCheckOutput dbOutput = new DBCheckOutput(checkID, checkItemID, status, s, exec_time, 0); //isNew=0);
                        dbOutput.DB_store();
                    } else {
                        DBCheckOutput dbOutput = new DBCheckOutput(checkID, checkItemID, status, s, exec_time, 1); //isNew=1);
                        dbOutput.DB_store();
                    }
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
        if (indLOGIC >= 0) { //add new Rule when there no exists tasks - 11/09/2015
                             //the validation of tasks below, is set too on CMD...

            //MPS - ajuste quebras em 27/08/2015...
            String aux_err = "";
            aux_err = err.getOutput_error().substring(0, indLOGIC).replace(" \n", "\n");
            arrayMonErrorsTasks = Arrays.asList(aux_err.split("TaskName:"));
            aux_err = err.getOutput_error().substring(indLOGICLEN).replace(" \n", "\n");
            arrayMonErrorsLogicSW = Arrays.asList(aux_err.split("\n"));
        } else {

            //MPS - ajuste quebras em 27/08/2015...
            String aux_err = "";
            aux_err = err.getOutput_error().replace(" \n", "\n");
            arrayMonErrorsTasks = Arrays.asList(aux_err.split("TaskName:"));
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

            //MPS - ajuste quebras em 27/08/2015...
            if (s.length() > 1) {
                //
                indResult = s.indexOf(Constantes.STRING_TKLRES);
                indState = s.indexOf(Constantes.STRING_TKSTAT);

                if ((indResult > 0) && (indState > 0)) {
                    //
                    tmpResult = s.substring(indResult + lenResult, indState);
                    tmpState = s.substring(indState + lenState).replace("\n", "");
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
                    if (tmpState.trim().toUpperCase().equals("ENABLED")) {

                        if ((!tmpResult.trim().equals("0")) && (!tmpResult.trim().equals("267009"))) {

                            arrayMonErrorsTasksCheck.add(s + ")\n");
                        }
                    } else {

                        arrayMonErrorsTasksCheck.add(s + ")\n");
                    }
                }
            }
        }
        //--------------------------------------------------
        //Remove Duplicate Erros e add to List...
        List<String> lst = new ArrayList<String>();

        //Test if arrayMonErrorsLogicSW is null...
        if (!isNullLOGIC) {
            lst = arrCountDuplicateItems(arrayMonErrorsLogicSW);
            for (String s : lst) {
                //
                if (s.length() > 0) {
                    arrayMonErrorsTasksCheck.add(s + "\n");
                }
            }
        }

        //--------------------------------------------------
        //record list of New Errors on DB...
        //
        String aux_err = arrayMonErrorsTasksCheck.toString().replace("\n ", "\n").replace(",", "").replace("[", "").replace("]", "");
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
            if (lstDup.contains(s)) {
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
            if (s.toString().length() > 0) lstCDup.add(s.toString());
        }
        //
        return lstCDup;
    }

    //MPS - end...
    //==============================================================================
}
