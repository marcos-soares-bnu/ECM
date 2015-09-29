package main;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;

import javax.swing.JOptionPane;

import util.Constantes;
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
            //Prepare XML data to be send. Sets priority for the ticket at the beginning of the title (e.g. P2,P3,P5)
        	String xmlContent = "\""+returnXML(out.getTicket_ci(), "P"+out.getTicket_prio()+" \\ "+out.getTicket_brief(), out.getOutput_error())+"\"";
        	String mailTitle = "\""+out.getTicket_brief()+"\"";
        	this.writeBAT(mailTitle, xmlContent);
        	
        	String command1 = "cmd /c start /min /wait D:\\IAS_Monitoring\\APP_Dev\\SCHEDScripts\\psexec -c \\\\MLGMUC00APP289 D:\\IAS_Monitoring\\APP_Dev\\SCHEDScripts\\mail.bat";
        	String command2 = "cmd /c start /min /wait D:\\IAS_Monitoring\\APP_Dev\\SCHEDScripts\\psexec \\\\MLGMUC00APP289 C:\\Temp\\mail.bat";
        	//Send the locally created batch file to the remote host
        	try {
                Process p1 = Runtime.getRuntime().exec(command1);
                p1.waitFor();
            } catch (IOException | InterruptedException e) {
            	System.out.println("Impossible to copy mail batch.");
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, e);
            }
        	//Execute the recently sent batch at the remote host
        	try {
				Process p2 = Runtime.getRuntime().exec(command2);
				//Catch the exit code from .bat
	            if (p2.waitFor() == 0) {
	                //Mail sent: set mail_sent = 1
	                out.DB_updateMailSent();
	            }
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
        }
    }
    
    //Write the mailSend batch locally
    private void writeBAT(String TITLE, String XML){
    	String[] batchLines = new String[]{"echo @ECHO OFF> C:\\Temp\\mail.bat",
    	    	"echo SET MAIL=" + XML + ">> C:\\Temp\\mail.bat",
    	    	"echo SET MAIL_TITLE=" + TITLE + ">> C:\\Temp\\mail.bat",
    	    	"echo SET Returncode=0>> C:\\Temp\\mail.bat",
    	    	"echo SET mail_progr=C:/Users/SRSK0006/Desktop/sendEmail.exe>> C:\\Temp\\mail.bat",
    	    	"echo SET mail_server=smtpeu.linde.grp>> C:\\Temp\\mail.bat",
    	    	"echo SET mail_sender=ixos-admin@Linde-DCM.com>> C:\\Temp\\mail.bat",
    	    	"echo SET mail_target=" + Constantes.MAIL_TARGET + ">> C:\\Temp\\mail.bat",
    	    	"echo \"%%mail_progr%%\" -f \"%%mail_sender%%\" -t \"%%mail_target%%\" -u %%MAIL_TITLE%% -m %%MAIL%% -s \"%%mail_server%%\">> C:\\Temp\\mail.bat"};
    	
    	PrintWriter writer = null;
    	try {
			writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream("D:\\IAS_Monitoring\\APP_Dev\\SCHEDScripts\\mail.bat")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	for (int i = 0; i < batchLines.length; i++) {
			writer.println(batchLines[i]);
		}
		writer.close();    	
    }
    
    //Return the xml that is the content of the email to SM9 mail interface
    private String returnXML(String ticketCI, String ticketBrief, String ticketDesc){
    	StringBuilder aux_xml = new StringBuilder();
    	aux_xml.append("<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\"?>");
    	aux_xml.append("<DSCEMO>");
    	aux_xml.append("<EVTYPE>DSCEMO</EVTYPE>");
    	aux_xml.append("<CONTACT_NAME>DUMMY-USER-LINDE</CONTACT_NAME>");
    	aux_xml.append("<PROBLEM_MSS>1000.0002.0004</PROBLEM_MSS>");
    	aux_xml.append("<ASSIGNMENT>SI.DHS.INT.LINDE_IAS_AO_1ST</ASSIGNMENT>");
    	aux_xml.append("<LOGICAL_NAME>" + ticketCI + "</LOGICAL_NAME>");
    	aux_xml.append("<BRIEF_DESCRIPTION>" + ticketBrief + "</BRIEF_DESCRIPTION>");
    	aux_xml.append("<ACTION>" + ticketDesc.replace("\n", "|") + "</ACTION>");
    	aux_xml.append("</DSCEMO>");
    	
    	return aux_xml.toString();
    }
}