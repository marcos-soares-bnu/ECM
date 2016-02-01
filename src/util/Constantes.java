package util;


public class Constantes {
    
    //Tables
    public static final String DB_Checks_Table = "check_scripts";
    public static final String DB_CheckItens_Table = "check_scripts_itens";
    public static final String DB_CheckConfig_Table = "check_scripts_configs";
    public static final String DB_ChecksOutput_Table = "check_scripts_output";
    public static final String DB_OTASS_Table = "linde_otass";
    public static final String DB_OTASS_JOBS_Table = "otass_jobs";
    public static final String DB_OTASS013_JOBS_STATUS_Table = "otass013_jobs_status";
    public static final String DB_ChecksCmds_Table = "check_scripts_cmds";
    
    //CheckScripts
    public static final int DB_INFRA_ID = 1;
    public static final int DB_OTASS_ID = 2;
    public static final int DB_DPWIN_ID = 3;
    public static final int DB_SQL_ID = 4;
    public static final int DB_FCIR_ID = 5;
    public static final int DB_PIXCORE_ID = 6;
    public static final int DB_ICC_ID = 7;
    
    //String utils
    public static final String STRING_CHECKS = "----------------------CHECKS------------------------------";
    public static final String STRING_ERRORS = "----------------------ERRORS------------------------------";
    public static final String STRING_CONFIG = "----------------------CONFIG------------------------------";
    public static final String STRING_SUBERROR = "---------------------SUBERROR-----------------------------";
    public static final String STRING_LOGICSWITCH = "------------------LOGIC-SWITCH----------------------------";
    
    //Task Schedules const
    public static final String STRING_TKNRUN = "\nNext Run Time:";
    public static final String STRING_TKLRUN = "\nLast Run Time:";
    public static final String STRING_TKLRES = "\nLast Result:";
    public static final String STRING_TKSTAT = "\nScheduled Task State:";
    
    //Strings for OTASS password retrieve
    public static final String STRING_OTASS_COLS = "user, pass";
    public static final String STRING_OTASS_CONDITION = "is_enabled = 1";
    
    //Strings OTASS013
    public static final String STRING_JOBNAME = "JOBNAME: ";
    public static final String STRING_ID = "ID     : ";
    public static final String STRING_STATUS = "STATUS : ";
    public static final String STRING_TIME = "TIME   : ";
    public static final String STRING_MESSAGE = "MESSAGE: ";
    
    //Strings SendMail
    public static final String MAIL_TARGET = "IncidentMailService@t-systems.com";
    public static final String MAIL_TARGET_CC = "FMB-ECM-BR@t-systems.com.br";
    
    //Testing reasons
    public static final boolean LINDE_CMDS_INTERVAL = false;
    
    public static final boolean LINDE_ENVIRONMENT = true;
    public static final boolean USE_MAIL_SEND = false;//true;
    public static final boolean SHOW_DB_MESSAGES = false;
    public static final boolean SHOW_OTHER_MESSAGES = false;
    public static final int SEC_TIME_WAIT = 3;
    
    //Scheduled Jobs
    public static final String[] trans_mlgmuc00app571_U7_EXR3_TIME = {"05:30 AM", "01:30 PM", "02:30 PM", "03:30 PM", "04:30 PM"};
    public static final String[] trans_mlgmuc00app571_U6_EXR3_TIME = {"06:32 AM", "07:32 AM", "08:32 AM", "09:32 AM", "10:32 AM", "12:32 PM"};
}
