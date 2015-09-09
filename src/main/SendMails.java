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
import text.CheckHandler;
import text.FileReaderUtil;
import util.Constantes;

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

        	String aux = returnXML(out.getTicket_ci(), out.getTicket_brief(), out.getOutput_error());
        	String tmp = "";
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
    	aux_xml.append("  <BRIEF_DESCRIPTION>" + ticketBrief.replace("\n", "|") + "</BRIEF_DESCRIPTION> ");
    	aux_xml.append("  <ACTION>" + ticketDesc + "</ACTION> ");
    	aux_xml.append("</DSCEMO>");
    	
    	return aux_xml.toString();
    }
    
}
