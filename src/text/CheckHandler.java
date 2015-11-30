package text;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import logic_process.LogicDPWin;
import logic_process.LogicFCIR;
import logic_process.LogicPixCore;
import object.OBJCheck;
import object.OBJCheckItem;
import object.OBJCheckOutput;
import util.Constantes;
import util.DateUtil;

public class CheckHandler {

    private DateUtil dtUtil = new DateUtil();

    private String output = "";
    private OBJCheck check;

    public CheckHandler(String outputString, OBJCheck check) {
        this.output = outputString;
        this.check = check;
    }

    /**
     * Select all useful informations of the texts for each checkItem
     */
    public void handleTextFile() {
        Map<Integer, TXTCheck> txtChecksInfos = new HashMap<Integer, TXTCheck>();

        String[] txtChecks = this.splitEachCHECKS(output);

        for (int i = 1; i < txtChecks.length; i++) {
            String name = null;
            String cfg = null;
            String errors = null;

            String[] checksInfosArray = this.splitNameAndErrors(txtChecks[i]);
            //Extract the check name from the array checksInfosArray.
            String checkName = checksInfosArray[0];
            if (!checkName.isEmpty()) {
                name = checkName.trim(); //get name
                if (checksInfosArray.length > 1) {
                    if (checksInfosArray[1].contains(Constantes.STRING_CONFIG)) {
                        String[] errorsAndCfg = this.splitErrorsAndConfig(checksInfosArray[1]);
                        if (errorsAndCfg != null) {
                            cfg = errorsAndCfg[0];
                            errors = errorsAndCfg[1]; //Complete Error(s) output
                        } else {
                            cfg = "";
                            errors = "";
                        }
                    } else {
                        cfg = "";
                        errors = checksInfosArray[1];
                    }
                } else {
                    cfg = "";
                    errors = "";
                    
                }

                TXTCheck checkInfos = new TXTCheck(name, cfg, errors);

                txtChecksInfos.put(txtChecksInfos.size() + 1, checkInfos);
            }
        }
        this.setObjecErrors(txtChecksInfos);
    }

    private Map<Integer, OBJCheckOutput> getErrorsMap(String errors) {
        Map<Integer, OBJCheckOutput> errorsMap = new HashMap<Integer, OBJCheckOutput>();

        //suberrors logic
        if (errors.length() > 3) {
            String[] subErrorChecks = this.splitSubErrors(errors);
            for (int j = 0; j < subErrorChecks.length; j++) {
                //Se a string tiver vazia ou se for menor que 3 caracteres
                if (subErrorChecks[j].length() > 3) {
                    int index = errorsMap.size() + 1;
                    OBJCheckOutput error = new OBJCheckOutput(subErrorChecks[j]);
                    errorsMap.put(index, error);
                } else {
                    subErrorChecks[j] = "";
                }
            }
        }

        return errorsMap;
    }

    private void setObjecErrors(Map<Integer, TXTCheck> txtChecksInfos) {
        LogicDPWin lDP = new LogicDPWin();
        LogicFCIR lFCIR = new LogicFCIR();
        LogicPixCore lPIX = new LogicPixCore();
        
        for (TXTCheck checkInfos : txtChecksInfos.values()) {
            OBJCheckItem item = check.getItem(checkInfos.getNomeCheck());

            if (checkInfos.getConfig().isEmpty()) {
                Map<Integer, OBJCheckOutput> errorsMap = this.getErrorsMap(checkInfos.getError());
                if (item != null) {
                    item.putInErrorsMap(errorsMap);
                    if (item.getCheck_id() == Constantes.DB_PIXCORE_ID) {
                        if (item.getItemName().equalsIgnoreCase("PIX001") || item.getItemName().equalsIgnoreCase("PIX002") || item.getItemName().equalsIgnoreCase("PIX003") || item.getItemName().equalsIgnoreCase("PIX004")) {
                            if (item.getStatus().equalsIgnoreCase("NOK")) {
                                item.setStatus("WARNING");
                            }
                        }
                    }
                }
            } else {
                //logica de quando terá uma configuração de definicao de erro
                if (item.getItemName().equalsIgnoreCase("DPWIN006")) {
                    Date dateNOW = this.getDateNow(checkInfos);
                    lDP.DPWIN006(checkInfos, dateNOW);
                    this.setItemErrors(checkInfos);
                    //--------------------------------------------------------------------------------
                } else if (item.getItemName().equalsIgnoreCase("DPWIN007")) {
                    Date dateNOW = this.getDateNow(checkInfos);
                    lDP.DPWIN007(checkInfos, dateNOW);
                    this.setItemErrors(checkInfos);
                    //--------------------------------------------------------------------------------
                } else if (item.getItemName().equalsIgnoreCase("FCIR003")) {
                    Date dateNOW = this.getDateNow(checkInfos);
                    lFCIR.FCIR003(checkInfos, dateNOW);
                    this.setItemErrors(checkInfos);
                } else if (item.getItemName().equalsIgnoreCase("FCIR008")) {
                    Date dateNOW = this.getDateNow(checkInfos);
                    lFCIR.FCIR008(checkInfos, dateNOW);
                    this.setItemErrors(checkInfos);
                    //--------------------------------------------------------------------------------
                } else if (item.getItemName().equalsIgnoreCase("PIX005")) {
                    Date dateNOW = this.getDateNow(checkInfos);
                    lPIX.PIX005(checkInfos, dateNOW);
                    this.setItemErrors(checkInfos);
                } else if (item.getItemName().equalsIgnoreCase("PIX006") || item.getItemName().equalsIgnoreCase("PIX007") || item.getItemName().equalsIgnoreCase("PIX009") || item.getItemName().equalsIgnoreCase("PIX010")) {
                    lPIX.PIXGeneral(checkInfos);
                    this.setItemErrors(checkInfos);
                } else if (item.getItemName().equalsIgnoreCase("PIX008")) {
                    Date dateNOW = this.getDateNow(checkInfos);
                    lPIX.PIX008(checkInfos, dateNOW);
                    this.setItemErrors(checkInfos);
                } else if (item.getItemName().equalsIgnoreCase("PIX011") || item.getItemName().equalsIgnoreCase("PIX012")) {
                    Date dateNOW = this.getDateNow(checkInfos);
                    lPIX.PIXTransJOB(checkInfos, dateNOW);
                    this.setItemErrors(checkInfos);
                }
            }
        }
    }

    private void setItemErrors(TXTCheck checkInfos) {
        OBJCheckItem item = check.getItem(checkInfos.getNomeCheck());
        
        if (!checkInfos.getError().isEmpty()) {
            OBJCheckOutput objOut = new OBJCheckOutput(checkInfos.getError());
            item.setErrors(objOut);
        }
    }

    private String[] splitEachCHECKS(String s) {
        String[] txtChecks = s.split(Constantes.STRING_CHECKS);
        for (int i = 0; i < txtChecks.length; i++) {
            txtChecks[i] = txtChecks[i].trim();
        }
        return txtChecks;
    }

    private String[] splitNameAndErrors(String s) {
        String[] errorChecks = s.split(Constantes.STRING_ERRORS);
        for (int i = 0; i < errorChecks.length; i++) {
            errorChecks[i] = errorChecks[i].trim();
        }
        return errorChecks;
    }

    private String[] splitErrorsAndConfig(String s) {
        String[] cfganderrors = null;
        if (s.length() > 3) {
            cfganderrors = s.split(Constantes.STRING_CONFIG);
        }
        return cfganderrors;
    }

    private String[] splitSubErrors(String s) {
        String[] subErrorChecks = s.split(Constantes.STRING_SUBERROR);
        return subErrorChecks;
    }
    
    private Date getDateNow(TXTCheck checkInfos) {
        String strdate = checkInfos.getConfig().replace("\n", " ");
        strdate = strdate.trim().substring(4);
        Date dateNOW = dtUtil.getDateFromString(strdate);
        
        return dateNOW;
    }
}
