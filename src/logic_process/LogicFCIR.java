package logic_process;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import text.TXTCheck;
import util.Constantes;
import util.DateUtil;

public class LogicFCIR extends Logic {

    private DateUtil dtUtil = new DateUtil();

    public void FCIR003(TXTCheck checkInfos, Date timeNOW) {
        Map<Integer, String> errorsMap = new HashMap<Integer, String>();

        String[] filesAndLastFile = checkInfos.getError().split("LASTFILE:");
        String[] files = filesAndLastFile[0].split("\n");

        //pos 0 = null
        for (int i = 1; i < files.length; i++) {
            Date horaArquivo = dtUtil.getDateFromStringFCIRchecks(files[i]);
            Date plannedExecution = this.getPlannedExec(horaArquivo);

            long diff = dtUtil.getMinDif(horaArquivo, timeNOW);

            if (timeNOW.after(plannedExecution)) {
                if (diff > 30) {
                    //erro
                    if (Constantes.SHOW_OTHER_MESSAGES) {
                        System.out.println("NOK: " + files[i]);
                        System.out.println("-- File:" + horaArquivo);
                        System.out.println("-- Plan:" + plannedExecution);
                        System.out.println("---DIFF: " + diff);
                    }
                    errorsMap.put(errorsMap.size(), files[i]);
                } else {
                    //Pode ser processado em breve.
                    //Deve ser aguardado o proximo processamento.
                    if (Constantes.SHOW_OTHER_MESSAGES) {
                        System.out.println("Alert: " + files[i]);
                        System.out.println("-- File:" + horaArquivo);
                        System.out.println("-- Plan:" + plannedExecution);
                        System.out.println("---DIFF: " + diff);
                    }
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

    public void FCIR008(TXTCheck checkInfos, Date timeNOW) {
        String[] arrFiles = checkInfos.getError().split("\n");
        String strDirectory = arrFiles[3];
        String strColumns = arrFiles[6];
        Map<Integer, String> errorsMap = new HashMap<Integer, String>();

        //pos7 = pulo de linha
        for (int i = 8; i < arrFiles.length; i++) {
            if (dtUtil.moreThanXhours(timeNOW, dtUtil.getFileDateFCIR008(arrFiles[i]), 48)) { //48h
                errorsMap.put(errorsMap.size(), arrFiles[i]);
            }
        }

        if (errorsMap.size() > 0) {
            //Create the error string
            String errors = strDirectory + "\n";
            errors += strColumns + "\n";
            errors += this.createErrorString(errorsMap);
            checkInfos.setError(errors);
        } else {
            checkInfos.setError("");
        }
    }

    @SuppressWarnings("deprecation")
    private Date getPlannedExec(Date horaArquivo) {
        Date dtPlanExec = new Date(horaArquivo.getTime());

        int mins = dtUtil.getDateMinute(horaArquivo);
        int execHour = dtUtil.getDateHour(horaArquivo);
        int execMin = 0;
        if (mins < 07) {
            execMin = 07;
        } else if (mins < 22) {
            execMin = 22;
        } else if (mins < 37) {
            execMin = 37;
        } else if (mins < 52) {
            execMin = 52;
        } else if (mins >= 52) {
            execMin = 07;
            execHour = 07;
        }

        Calendar gcPlanExec = this.getData(dtPlanExec);
        //Se está no período em que o job não roda (somente em horas)
        if (dtPlanExec.getHours() < 7 || dtPlanExec.getHours() >= 20) {
            //Se está na última hora possível para rodar (20 horas)
            if (dtPlanExec.getHours() == 20) {
                //Se está com hora >= 20:52 (ultima execução do job no dia)
                if (dtPlanExec.getMinutes() >= 52) {
                    execHour = 7;
                    execMin = 7;
                    gcPlanExec.add(Calendar.DAY_OF_MONTH, 1);
                }
            } else {
                execHour = 7;
                execMin = 7;
                if (dtPlanExec.getHours() > 20) {
                    //add day
                    gcPlanExec.add(Calendar.DAY_OF_MONTH, 1);
                }
            }
        }
        //d1 = true: o arquivo é de domingo
        boolean d1 = dtUtil.getWeekDay(horaArquivo).equalsIgnoreCase("Sonntag");
        Date dtTemp = gcPlanExec.getTime();
        //d2 = true: a dia atual é domingo
        boolean d2 = dtUtil.getWeekDay(dtTemp).equalsIgnoreCase("Sonntag");
        boolean domingo = (d1 || d2);

        if (domingo) {
            //Se hoje é domingo, seta a execução planejada para o primeiro
            //horário de segunda feira.
            if (d2) {
                gcPlanExec.add(Calendar.DAY_OF_MONTH, 1);
                gcPlanExec.set(Calendar.HOUR_OF_DAY, 07);
                gcPlanExec.set(Calendar.MINUTE, 07);
                dtTemp = gcPlanExec.getTime();
                dtPlanExec = dtTemp;
            //Senão     
            } else {
                dtTemp = gcPlanExec.getTime();
                dtTemp.setHours(execHour);
                dtTemp.setMinutes(execMin);
                dtPlanExec = dtTemp;
            }
        } else {
            dtPlanExec = gcPlanExec.getTime();
            dtPlanExec.setHours(execHour);
            dtPlanExec.setMinutes(execMin);
        }

        return dtPlanExec;
    }
}
