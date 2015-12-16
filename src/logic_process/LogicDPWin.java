package logic_process;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import text.TXTCheck;
import util.DateUtil;

public class LogicDPWin extends Logic {

    private DateUtil dtUtil = new DateUtil();

    public void DPWIN006(TXTCheck checkInfos, Date timeNOW) {
        String[] arrFiles = checkInfos.getError().split("\n");
        Map<Integer, String> errorsMap = new HashMap<Integer, String>();

        //Array 0 virá vazio
        //Preenche o mapa de erros
        if (dtUtil.moreThanXminutes(timeNOW, dtUtil.getFileDateDPWIN006(arrFiles[1]), 30)) { //30min
        	
        	String errors = "";
            for (int i = 2; i < arrFiles.length; i++) {
				errors += arrFiles[i] + "\n";
			}
            errorsMap.put(errorsMap.size(),errors);
        }

        if (errorsMap.size() > 0) {
            //Create the error string
            checkInfos.setError(this.createErrorString(errorsMap));
        } else {
            checkInfos.setError("");
        }
    }
    
    public void DPWIN007(TXTCheck checkInfos, Date timeNOW) {
        String[] arrFiles = checkInfos.getError().split("\n");
        Map<Integer, String> errorsMap = new HashMap<Integer, String>();

        //Array 0 virá vazio
        //Preenche o mapa de erros
        for (int i = 1; i < arrFiles.length; i++) {
            if (dtUtil.moreThanXhours(timeNOW, dtUtil.getFileDateDPWIN007(arrFiles[i]), 24)) { //24h
                errorsMap.put(errorsMap.size(), arrFiles[i]);
            }
        }

        if (errorsMap.size() > 0) {
            //Create the error string
            checkInfos.setError(this.createErrorString(errorsMap));
        } else {
            checkInfos.setError("");
        }
    }
}
