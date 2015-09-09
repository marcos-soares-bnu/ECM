package main;

import java.io.IOException;
import java.util.Map;

import javax.swing.JOptionPane;

import database.DBCheckOutput;

public class SendMails {

    private Map<Integer, DBCheckOutput> dbNewErrors;

    public SendMails() {

        //Get new errors from this check
        DBCheckOutput dbNEWCheckOut = new DBCheckOutput();
        dbNewErrors = dbNEWCheckOut.DB_retrieveNewErrors();
    }

    public void startSend() {
        this.sendingArrNewErrors();
    }


    private void sendingArrNewErrors() {
        for (DBCheckOutput out : dbNewErrors.values()) {
            //Prepare XML data to be send
        	String xmlContent = "\""+returnXML(out.getTicket_ci(), out.getTicket_brief(), out.getOutput_error())+"\"";
        	String mailTitle = "\""+out.getTicket_brief()+"\"";
        	String command = "cmd /c start /min /wait " + 
                	"D:\\IAS_Monitoring\\APP_Dev\\SCHEDScripts\\psexec \\MLGMUC00APP289 C:\\Users\\SRSK0006\\Desktop\\email.bat " +
                    xmlContent + " " + mailTitle;
        	//Call the .bat file to send email
        	try {
                Process p = Runtime.getRuntime().exec(command);
            //Catch the exit code from .bat
            int exitCode = p.waitFor();
            
            if (exitCode == 0) {
                //Mail sent: set mail_sent = 1
                out.DB_updateMailSent();
            }
            
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, e);
            }
        }
        
        

    }
    
    
    private String returnXML(String ticketCI, String ticketBrief, String ticketDesc){
    	
    	StringBuilder aux_xml = new StringBuilder();
    	aux_xml.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
    	aux_xml.append("<DSCEMO> ");
    	aux_xml.append("  <EVTYPE>DSCEMO</EVTYPE> ");
    	aux_xml.append("  <CONTACT_NAME>DUMMY-USER-LINDE</CONTACT_NAME> ");
    	aux_xml.append("  <PROBLEM_MSS>2770.0011.0000</PROBLEM_MSS> ");
    	aux_xml.append("  <ASSIGNMENT>SI.DHS.INT.LINDE_IAS_AO_2ND</ASSIGNMENT> ");
    	aux_xml.append("  <LOGICAL_NAME>" + ticketCI + "</LOGICAL_NAME> ");
    	//aux_xml.append("  <BRIEF_DESCRIPTION><![CDATA[" + ticketBrief + "]]></BRIEF_DESCRIPTION> ");
    	aux_xml.append("  <BRIEF_DESCRIPTION>" + ticketBrief + "</BRIEF_DESCRIPTION> ");
    	aux_xml.append("  <ACTION>" + ticketDesc.replace("\n", "|") + "</ACTION> ");
    	aux_xml.append("</DSCEMO>");
    	
    	return aux_xml.toString();
    }
    
}
