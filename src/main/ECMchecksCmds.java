package main;

import database.CmdsHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ECMchecksCmds {

	public String path_out;
	public String check_cd;
	public String otass_lastid;
	//
	public String unix_usr = "ixosadm";
	public String unix_pwd = "soxi0401";
	public String unix_hst = "iasp.edc.linde.grp";
	//
	public String sql_usr = "ecr_monitoring";
	public String sql_pwd = "ecr0401_monitoring";
	public String sql_hst = "ECR_iasp";
	//
	private CmdsHandler cmdh;
	//
	public String getUnix_usr() {
		return unix_usr;
	}

	public String getOtass_lastid() {
		return otass_lastid;
	}

	public void setOtass_lastid(String otass_lastid) {
		this.otass_lastid = otass_lastid;
	}

	public void setUnix_usr(String unix_usr) {
		this.unix_usr = unix_usr;
	}

	public String getUnix_pwd() {
		return unix_pwd;
	}

	public void setUnix_pwd(String unix_pwd) {
		this.unix_pwd = unix_pwd;
	}

	public String getUnix_hst() {
		return unix_hst;
	}

	public void setUnix_hst(String unix_hst) {
		this.unix_hst = unix_hst;
	}

	public String getSql_usr() {
		return sql_usr;
	}

	public void setSql_usr(String sql_usr) {
		this.sql_usr = sql_usr;
	}

	public String getSql_pwd() {
		return sql_pwd;
	}

	public void setSql_pwd(String sql_pwd) {
		this.sql_pwd = sql_pwd;
	}

	public String getSql_hst() {
		return sql_hst;
	}

	public void setSql_hst(String sql_hst) {
		this.sql_hst = sql_hst;
	}
	//
	private List <List <String> > lstCmds = new ArrayList<>();
	
	public String getPath_out() {
		return path_out;
	}

	public void setPath_out(String path_out) {
		this.path_out = path_out;
	}

	public CmdsHandler getCmdh() {
		return cmdh;
	}

	public void setCmdh(CmdsHandler cmdh) {
		this.cmdh = cmdh;
	}

	public List<List<String>> getLstCmds() {
		return lstCmds;
	}

	public void setLstCmds(List<List<String>> lstCmds) {
		this.lstCmds = lstCmds;
	}

	public String getCheck_cd() {
		return check_cd;
	}

	public void setCheck_cd(String check_cd) {
		this.check_cd = check_cd;
	}
	
	public ECMchecksCmds(String check_cd, String path_out) {
		super();
		this.path_out		= path_out;
		this.check_cd		= check_cd;
		this.cmdh			= new CmdsHandler();
		this.lstCmds 		= this.cmdh.selectCmds(this.check_cd);
		this.otass_lastid	= this.cmdh.selectLastID();
	}

	
	private void treatInternCmds(String aux_cmdr)
	{
		String[] aux_vetCmdsInt = aux_cmdr.split("@@|##");
		List<String> vetCmdsInt = new ArrayList<String>(); 

		for (String s : vetCmdsInt)
		{
			if (s.length() <= 20)
				vetCmdsInt.add(s);
		}
		//
		for (String string : vetCmdsInt)
		{
			
		}
	}
	
	public void callCmdsInterval() throws IOException
	{
        Date execTime			= new Date();
		String aux_path			= this.path_out;
		FileWriter writer 		= new FileWriter(aux_path, false);
		
        for (List<String> list : this.lstCmds)
        {
    		CmdsHandler cmdhi	= new CmdsHandler();
        	String aux_code 	= list.get(1);
        	String aux_cmde 	= list.get(2);
        	String aux_inte 	= list.get(3);
        	
        	//Change @LOGFILE to path_out...   
        	String aux_cmdr		= aux_cmde.replace("@LOGFILE", (aux_path + "." + this.check_cd));

        	//Treat the intern Unix call sql/shell commands to save locally...
        	if (aux_cmdr.indexOf("@@") > 0)
        	{
        		treatInternCmds(aux_cmdr);
        		aux_cmdr		= aux_cmdr.replace("@@", "").replace("##", ""); 
        	}
        	
// *** TEST ***************************************************** SRV to LOCALHOST...
        	aux_cmdr			= aux_cmdr.replace("\\MLGMUC00APP289", "\\LOCALHOST");
        	aux_cmdr			= aux_cmdr.replace("\\MLGMUC00APP290", "\\LOCALHOST");
        	aux_cmdr			= aux_cmdr.replace("\\MLGMUC00SAP019", "\\LOCALHOST");
        	aux_cmdr			= aux_cmdr.replace("\\MLGMUC00SAP041", "\\LOCALHOST");
        	aux_cmdr			= aux_cmdr.replace("\\MLGMUC00APP571", "\\LOCALHOST");
        	aux_cmdr			= aux_cmdr.replace("\\MLGMUC00APP577", "\\LOCALHOST");
        	aux_cmdr			= aux_cmdr.replace("\\mlgmuc00app667", "\\LOCALHOST");
        	aux_cmdr			= aux_cmdr.replace("\\mlgmuc00app664", "\\LOCALHOST");
        	aux_cmdr			= aux_cmdr.replace("\\mlgmuc00app705", "\\LOCALHOST");
        	aux_cmdr			= aux_cmdr.replace("\\MLGMUC00APP706", "\\LOCALHOST");
// *** TEST ***************************************************** SRV to LOCALHOST...
        	
        	//Calc age of lastexec in minutes...
        	String aux_agelast 	= cmdhi.getAgeCheckLastExec(aux_code);
            ChecksExec execChk	= new ChecksExec(Integer.parseInt(this.getCheck_cd()), execTime);
        	Float ageLastExec 	= Float.parseFloat(aux_agelast);
        	Float intervCheck 	= Float.parseFloat(aux_inte);
        	
        	if (ageLastExec 	< intervCheck)
        	{
        		System.out.println("Last execution of " + aux_code + " was (" + ageLastExec + ") minutes Ago... Will start in (" + (intervCheck-ageLastExec) + ") minutes...");
        	}
        	else
        	{
        		//Call Process to update/create tmp CMD... 
        		String fileCMD = updRecCmdFileTmp("tmp." + this.check_cd + ".cmd", aux_cmdr);
				
        		//Call execution...
        		System.out.println("Starting Check Execution - " + aux_code + "...");
        		int ret = 0;
        		
        		//Check if necessary to send Parameters...
        		if ( 	(this.check_cd.equals("2")) || 
        				(this.check_cd.equals("6")) || 
        				(this.check_cd.equals("7")) )
        		{
        			ret	= execChk.callCheckCMD(fileCMD +	" " + this.unix_usr +
        													" " + this.unix_pwd +
        													" " + this.unix_hst	+
        													" " + this.sql_usr 	+
        													" " + this.sql_pwd 	+
        													" " + this.sql_hst	);
        		}
        		else { ret	= execChk.callCheckCMD(fileCMD); }
        		//
        		if (ret == 0)
        		{
        			//Get fileContent of aux_path + "." + this.check_cd...
        	    	String fileContent	= "";
        			fileContent			= readAllFile(aux_path + "." + this.check_cd);

        			cmdhi.setLastLogCheck(fileContent, aux_code);	//update lastlog...
        			cmdhi.setLastExecCheck(aux_code); 				//update lastexec...
    				
        			//Write and Join checks into path_out... 
        			writer.write(fileContent);
        		}
        	}
        	//Close conn...
        	cmdhi.dbClose();
		}
        writer.close();
	}
	
	private String updRecCmdFileTmp(String fileTmp, String aux_cmdr) throws IOException
	{
		String fileCMD 			= fileTmp;
		boolean ehDiffDBcmdr	= true;

		//Fill the file content to compare...
    	//String fileContent 		= "";
		//fileContent				= readAllFile(fileCMD);
		//if (fileContent.trim().contentEquals(aux_cmdr.trim()))
		//	ehDiffDBcmdr 		= false; 

    	if (ehDiffDBcmdr)
    	{
    		//Write CMD to execute later...
    		FileWriter writer 	= new FileWriter(fileCMD, false);
    		writer.write(aux_cmdr + "\n");
    		writer.close();
    	}
		
		return fileCMD;
	}
	
	
	private String readAllFile(String file) throws IOException
	{
    	String fileContent 		= "";
    	File f 					= new File(file);
    	if (f.exists())
    	{
			FileInputStream inp = new FileInputStream(f);
			byte[] bf 			= new byte[(int)f.length()];
			inp.read(bf);
			fileContent 		= new String(bf);
			inp.close();
    	}
		return fileContent;
	}
	
	
	public static void main(String[] args)
	{
		try
		{
			if (args.length > 1)
			{
				ECMchecksCmds ecmCmds = new ECMchecksCmds(args[0], args[1]);
				ecmCmds.callCmdsInterval();
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
