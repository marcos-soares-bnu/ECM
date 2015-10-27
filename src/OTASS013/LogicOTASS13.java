package OTASS013;

import java.util.SortedMap;
import java.util.TreeMap;

import text.FileReaderUtil;
import util.Constantes;

public class LogicOTASS13 {

    private SortedMap<String, OBJReadedOTASSJobStatus> readedJobStatusMap = new TreeMap<String, OBJReadedOTASSJobStatus>();

    //===================================Main Methods=================================
    public void MainMethod() {
        this.ReadLog();
        this.MainLoop();
    }

    public void MainLoop() {
        for (OBJReadedOTASSJobStatus readedStatus : readedJobStatusMap.values()) {
            DBOTASSJob DBjobINFOs = new DBOTASSJob(readedStatus.getJobName());
            if (Constantes.SHOW_OTHER_MESSAGES) {
                System.out.println("=======================================");
                System.out.println(DBjobINFOs.getId() + " - " + DBjobINFOs.getJobName());
                System.out.println("=+==+==+==+==+==+==+==+==+==+==+==+==+=");
                System.out.println(readedStatus.getFullOutput());
                System.out.println("=+==+==+==+==+==+==+==+==+==+==+==+==+=");
            }

            //Verifica a quantidade de Status cadastrados para este JOB:
            if (DBjobINFOs.getStatusMap().size() < 1) {
                //Não há status cadastrados para este JOB
                //Converte para OBJ lido para um OBJ do Banco
                DBOTASSJobStatus readedStatus_DB = new DBOTASSJobStatus(readedStatus, DBjobINFOs.getId());
                //Cadastra o status encontrado
                readedStatus_DB.insertNewStatus();
            } else if (DBjobINFOs.getStatusMap().size() == 1) {
                //Existe apenas 1 status para este JOB
                //Converte para OBJ lido para um OBJ do Banco
                DBOTASSJobStatus DBjobStatus = DBjobINFOs.getStatusMap().values().iterator().next();
                if (readedStatus.getOutput().equalsIgnoreCase(DBjobStatus.getOutput())) {
                    //Mesmo output, deve ser apenas atualizado o status (full_output)
                    DBjobStatus.setStatus(readedStatus.getStatus());
                    DBjobStatus.setOutput(readedStatus.getOutput());
                    DBjobStatus.setFullOutput(readedStatus.getFullOutput());
                    if (!readedStatus.isError()) {
                        DBjobStatus.setMailSent(false);
                    }

                    //Atualiza as informações
                    DBjobStatus.updateSTATUS();
                } else {
                    //output diferente, deve ser cadastrado um novo status
                    //Converte para OBJ lido para um OBJ do Banco
                    DBOTASSJobStatus readedStatus_DB = new DBOTASSJobStatus(readedStatus, DBjobINFOs.getId());
                    //Cadastra o status
                    readedStatus_DB.insertNewStatus();
                }
            } else {
                //Existem mais de 1 status para este JOB
                //verifica se é erro ou nao
                if (readedStatus.isError()) {
                    //Verifica se existe um mesmo erro cadastrado
                    DBOTASSJobStatus statusEncontrado = null;
                    for (DBOTASSJobStatus dbStatus : DBjobINFOs.getStatusMap().values()) {
                        if (dbStatus.getOutput().equalsIgnoreCase(readedStatus.getOutput())) {
                            statusEncontrado = dbStatus;
                            break;
                        }
                    }
                    //Se existir um erro igual deve ser atualizado, caso contrario, deve ser inserido um novo status
                    if (statusEncontrado != null) {
                        statusEncontrado.setStatus(readedStatus.getStatus());
                        statusEncontrado.setOutput(readedStatus.getOutput());
                        statusEncontrado.setFullOutput(readedStatus.getFullOutput());

                        //Atualiza as informações
                        statusEncontrado.updateSTATUS();
                    } else {
                        DBOTASSJobStatus readedStatus_DB = new DBOTASSJobStatus(readedStatus, DBjobINFOs.getId());
                        //Cadastra o status
                        readedStatus_DB.insertNewStatus();
                    }
                } else {
                    //Atualiza tudo para ok (ataulizando o primeiro e deletando os seguintes)
                    boolean atualizado = false;
                    for (DBOTASSJobStatus dbStatus : DBjobINFOs.getStatusMap().values()) {
                        if (!atualizado) {
                            atualizado = true; //Apenas para controlar que foi feito apenas um update
                            //Atualiza um dos status
                            dbStatus.setStatus(readedStatus.getStatus());
                            dbStatus.setOutput(readedStatus.getOutput());
                            dbStatus.setFullOutput(readedStatus.getFullOutput());
                            dbStatus.setMailSent(false);

                            //Atualiza as informações
                            dbStatus.updateSTATUS();
                        } else {
                            //Deleta o status
                            dbStatus.deleteSTATUS();
                        }
                    }
                }
            }
            //FIM DO IF De verificação de Quantidade de status cadastrados para este JOB
        }
    }

    //===================================Reader Methods=================================
    public void ReadLog() {
        //Get all txt
        FileReaderUtil fileReader;
        if (Constantes.LINDE_ENVIRONMENT) {
            fileReader = new FileReaderUtil("D:\\IAS_Monitoring\\APP_Dev\\SCHEDScripts\\sched_otass.log");
        } else {
            fileReader = new FileReaderUtil("C:\\Temp\\script result\\sched_otass.log");
        }
        splitLogTextLine(fileReader.readFile());
    }

    public void splitLogTextLine(String allOutput) {
        String[] OTASS013 = allOutput.split("OTASS013");
        OTASS013 = OTASS013[1].split(Constantes.STRING_ERRORS);

        String jobName;
        String execID;
        String status;
        String time;
        String message;

        String[] jobINFOS = OTASS013[1].split(Constantes.STRING_JOBNAME);
        for (int i = 0; i < jobINFOS.length; i++) {
            if (jobINFOS[i].length() > 3) {
                jobINFOS[i] = jobINFOS[i].replace("\n", "");
                jobINFOS[i] = jobINFOS[i].replace(Constantes.STRING_ID, "\n");
                jobINFOS[i] = jobINFOS[i].replace(Constantes.STRING_STATUS, "\n");
                jobINFOS[i] = jobINFOS[i].replace(Constantes.STRING_TIME, "\n");
                jobINFOS[i] = jobINFOS[i].replace(Constantes.STRING_MESSAGE, "\n");

                String[] strTratada = jobINFOS[i].split("\n");
                jobName = strTratada[0];
                execID = strTratada[1];
                status = strTratada[2];
                time = strTratada[3];
                message = strTratada[4];

                OBJReadedOTASSJobStatus readedJobStatus = new OBJReadedOTASSJobStatus(jobName, execID, status, time, message);
                readedJobStatusMap.put(readedJobStatus.getJobName(), readedJobStatus);
            }
        }
    }
}
