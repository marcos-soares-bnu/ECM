package logic_process;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import object.OBJPIX008FileSet;
import text.TXTCheck;
import util.Constantes;
import util.DateUtil;


public class LogicPixCore extends Logic {
    private DateUtil dtUtil = new DateUtil();

    public void PIX005(TXTCheck checkInfos, Date dateNOW) {
        String[] arrProcess = checkInfos.getError().split("\n");
        
        String strProcExport = arrProcess[1];
        Date epxProcDate = dtUtil.getDateFromStringPIXCOREchecks(strProcExport);
        long diffExp = dtUtil.getMinDif(dateNOW, epxProcDate);
        String strProcImport = arrProcess[2];
        Date impProcDate = dtUtil.getDateFromStringPIXCOREchecks(strProcImport);
        long diffImp = dtUtil.getMinDif(dateNOW, impProcDate);
        String strProcReco = arrProcess[3];
        Date recoProcDate = dtUtil.getDateFromStringPIXCOREchecks(strProcReco);
        long diffReco = dtUtil.getMinDif(dateNOW, recoProcDate);
        String strProcDelete = arrProcess[4];
        Date delProcDate = dtUtil.getDateFromStringPIXCOREchecks(strProcDelete);
        long diffDel = dtUtil.getMinDif(dateNOW, delProcDate);
        
        String error = "";
        if (diffExp > 3 || diffImp > 3 || diffReco > 3 || diffDel > 3) {
            //Algum processo em erro, verificarei qual deles é
            if (diffExp > 3) {
                error += "Process 'Export' is frozen " + diffExp + " minutes.";
            }
            if (diffImp > 3) {
                error += "Process 'FileImport' is frozen " + diffImp + " minutes.";
            }
            if (diffReco > 3) {
                error += "Process 'Reco' is frozen " + diffReco + " minutes.";
            }
            if (diffDel > 3) {
                error += "Process 'Deleter' is frozen " + diffDel + " minutes.";
            }
        }
        checkInfos.setError(error);
    }

    /**
     * Used by:
     * UnzipChecks: PIX006/PIX007
     * ScheduledTask: PIX009/PIX010
     * @param checkInfos
     */
    public void PIXGeneral(TXTCheck checkInfos) {
    	Map<Integer, String> errorsMap = new HashMap<Integer, String>();
        
        String[] taskInfos = checkInfos.getConfig().split("\n");
        Date taskLastRun = dtUtil.getJobInfoDatePIX(taskInfos[2]);
        String taskLastResult = taskInfos[3].split("                          ")[1];
        String taskState = taskInfos[4].split("                 ")[1];
        
        String[] filesAndLastFile = checkInfos.getError().split("LASTFILE:");
        String[] pendingFiles = filesAndLastFile[0].split("\n");
        
        
        //=====================================================================
        //MPS - start - 26/08/2015
        //
        boolean flgTask = false;
        
        //Check2:
        //task state = enabled == OK
        //task state != enabled == NOK!
        if (!taskState.equalsIgnoreCase("Enabled")) {
            errorsMap.put(errorsMap.size(), "Task status is not \'Enabled\', is: " + taskState);
            flgTask = true;
        }
        
        //Check3:
        //task last result = 15 == situação normal de overload
        //task last result = 267009 == task rodando
        //task last result = 0 == OK
        //Diferente dessas 3 = NOK!
        if (taskLastResult.equalsIgnoreCase("15")) {
            //Situação normal de overload
        	flgTask = true;
        	
        } else if (taskLastResult.equalsIgnoreCase("267009")) {
            //Task rodando
        	flgTask = true;
        	
        } else if (taskLastResult.equalsIgnoreCase("0")) {
            //OK
        	//flgTask = true; // It's necessary to check when Result = 0... MPS - 27/08/2015
        	
        } else {
            //Erro
            errorsMap.put(errorsMap.size(), "Task Last Result is not OK. Last Result: " + taskLastResult);
        }

        if (!flgTask){
	        //Check1:
	        //Se a hora do arquivo - hora ultima exec >= 1h == ERRO!
	        //OBS: 01/02/1980  02:00 AM == Em processamento / Ignorar arquivo
	        String inProcessing = "01/02/1980  02:00 AM";
	        Date inProcessingDt = dtUtil.getFileDateFromString(inProcessing);
	        for (int i = 1; i < pendingFiles.length; i++) {
	        	Date fileDt = dtUtil.getFileDateFromString(pendingFiles[i]);
	            if (!inProcessingDt.equals(fileDt)) {
	                long timeDif = dtUtil.getHoursDif(fileDt, taskLastRun);
	                if (timeDif >= 1) {
	                    errorsMap.put(errorsMap.size(), pendingFiles[i]);
	                }
	            }
	        }
        }
        
        //MPS - end
        //=====================================================================
        
        //Caso3:
        //Last Entry in logfiles
        
        if (errorsMap.size() > 0) {
            //Create the error string
            checkInfos.setError(this.createErrorString(errorsMap));
        } else {
            checkInfos.setError("");
        }
    }
    
    public void PIX008(TXTCheck checkInfos, Date dateNOW) {
        Map<Integer, OBJPIX008FileSet> fileSet = new HashMap<Integer, OBJPIX008FileSet>();
        String[] files = checkInfos.getError().split("\n");
        OBJPIX008FileSet pixFile = null;
        //preenche mapa dos arquivos
        String fileName = "";
        for (int i = 1; i < files.length; i++) {
            if (!files[i].contains(fileName) || fileName.isEmpty()) {
                fileName = this.getFileName(files[i]);
                if (pixFile != null) {
                    fileSet.put(fileSet.size(), pixFile);
                }
                pixFile = new OBJPIX008FileSet();
            }
            
            if (files[i].contains("<DIR>")) {
                pixFile.setDirectory(files[i]);
            }
            if (files[i].contains(".imp")) {
                pixFile.setImpFile(files[i]);
            }
            if (files[i].contains(".sca")) {
                pixFile.setScaFile(files[i]);
            }
        }
        if (pixFile != null) {
            fileSet.put(fileSet.size(), pixFile);
        }
        
        String completeError = "";
        for (OBJPIX008FileSet file : fileSet.values()) {
            String errors = ""; 
            if (file.hasDir()) {
                //Verificação de quais são mais velhos q 8h
                if (file.isDirOlderThan8h(dateNOW)) {
                    errors += "\nThe directory is older than 8h:";
                }
                //Verifica se .sca está ausente por mais de uma hora
                if (file.isFileMissing(dateNOW, 'S')) {
                    errors += "\nThe .sca file is missing for more than 1h:";
                }
                //Verifica se .imp está ausente por mais de uma hora
                if (file.isFileMissing(dateNOW, 'I')) {
                    errors += "\nThe .imp file is missing for more than 1h:";
                }
            } else {
                //Orphan files
                errors += "\nThere are orphan files:";
            }
            
            if (!errors.isEmpty()) {
                completeError += errors + "\n";
                completeError += file.toString();
            }
        }
        
        if (completeError.isEmpty()) {
            checkInfos.setError("");
        } else {
            checkInfos.setError(completeError);
        }
    }
    //Used by PIX011 and PIX012
    public void PIXTransJOB(TXTCheck checkInfos, Date dateNOW) {
        Map<Integer, String> errorsMap = new HashMap<Integer, String>();
        
        String[] filesAndLastFile = checkInfos.getError().split("LASTFILE:");
        String[] files = filesAndLastFile[0].split("\n");
        
        for (int i = 1; i < files.length; i++) {
            //PIX011 e PIX012 tem mesmo formato de data do DPWIN007.
            Date dtArquivo = dtUtil.getFileDateDPWIN007(files[i]);
            Date dtPlan = this.getPlannedExec(dtArquivo, checkInfos.getNomeCheck());
            
            //Se for depois, irá ser executado ainda
            if (dtPlan.before(dateNOW)) {
                long timeDiff = dtUtil.getHoursDif(dtPlan, dateNOW);
                if (timeDiff>1) {
                    //Erro
                    String error = files[i] + " -> Should had been executed at " + dtPlan;
                    errorsMap.put(errorsMap.size(), error);
                }
            }
        }
        
        if (errorsMap.size() > 0) {
            //Create the error string
            checkInfos.setError(this.createErrorString(errorsMap));
        } else {
            checkInfos.setError("");
        }
    }
    
    private Date getPlannedExec(Date horaArquivo, String nomeCheck) {
        Date dtPlan = new Date();
        String[] jobTime = null;
        if (nomeCheck.equalsIgnoreCase("PIX011")) {
            //Use:
            jobTime = Constantes.trans_mlgmuc00app571_U7_EXR3_TIME;
        } else if (nomeCheck.equalsIgnoreCase("PIX012")) {
            //Use:
            jobTime = Constantes.trans_mlgmuc00app571_U6_EXR3_TIME;
        }
        Date[] dates = dtUtil.getDates(jobTime, horaArquivo);
        dtPlan = dtUtil.getPlannedExecution(horaArquivo, dates);
        return dtPlan;
    }
    
    /**
     * Used by PIX008
     * @param str
     * @return
     */
    private String getFileName(String str) {
        String file = "";
        
        if (str.contains("<DIR>")) {
            file = str.split("<DIR>")[1].trim();
        } else if (str.contains(".imp") || str.contains(".sca")) {
            file = str.split(" ")[21].trim();
        }
        return file;
    }
}
