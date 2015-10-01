package main;

import java.util.Date;

import util.Constantes;
import change.ChangeCheck;

public class MajorMain {

    public static void main(String[] args) {
        Date executionTime = new Date();

        System.out.println("Starting the execution of the SCHEDULED Scripts at:\n" + executionTime);

        ChangeCheck change = new ChangeCheck(executionTime);
        change.checkChanges();

        if(Constantes.SHOW_OTHER_MESSAGES) {
            System.out.println("==================================================================================================================================");
        }
        
        ChecksExec infra = new ChecksExec(Constantes.DB_INFRA_ID, executionTime);
        //So procede se existem itens (nao esta em change)
        if (infra.getObjCheck().getItens().size() > 0) {
            infra.execCheck();
            infra.storeINDB();
        }

        ChecksExec otass = new ChecksExec(Constantes.DB_OTASS_ID, executionTime);
        if (otass.getObjCheck().getItens().size() > 0) {
            otass.execCheck();
            otass.storeINDB();
        }

        ChecksExec dpwin = new ChecksExec(Constantes.DB_DPWIN_ID, executionTime);
        if (dpwin.getObjCheck().getItens().size() > 0) {
            dpwin.execCheck();
            dpwin.storeINDB();
        }

        ChecksExec sql = new ChecksExec(Constantes.DB_SQL_ID, executionTime);
        if (sql.getObjCheck().getItens().size() > 0) {
            sql.execCheck();
            sql.storeINDB();
        }

        ChecksExec fcir = new ChecksExec(Constantes.DB_FCIR_ID, executionTime);
        if (fcir.getObjCheck().getItens().size() > 0) {
            fcir.execCheck();
            fcir.storeINDB();
        }

        ChecksExec pixcore = new ChecksExec(Constantes.DB_PIXCORE_ID, executionTime);
        if (pixcore.getObjCheck().getItens().size() > 0) {
            pixcore.execCheck();
            pixcore.storeINDB();
        }
        
        if(Constantes.SHOW_OTHER_MESSAGES) {
            System.out.println("==================================================================================================================================");
        }
        
        //SendMails sendMails = new SendMails();
        //sendMails.startSend();

        executionTime = new Date();
        System.out.println("Finished the execution of the SCHEDULED Scripts at:\n" + executionTime);

        //Sleep de X segundos para conseguir ler a msg 
        try {
            Thread.sleep(Constantes.SEC_TIME_WAIT * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
