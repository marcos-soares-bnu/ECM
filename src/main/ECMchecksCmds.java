package main;

import database.CmdsHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import output.SyncPipe;


public class ECMchecksCmds {

	public String path_out;
	public String check_cd;
	//
	private CmdsHandler cmdh;
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
	}

	
	private List<String> getListInternCmds(String aux_cmdr, String sep, int MAXCOL)
	{
		String[] aux_vetCmdsInt = aux_cmdr.split(sep);
		List<String> vetCmdsInt = new ArrayList<String>(); 

		for (String s : aux_vetCmdsInt)
		{
			if ( (s.length() > 1) && (s.length() <= MAXCOL) && (s.indexOf("\n") == -1) )
				vetCmdsInt.add(s);
		}
		return vetCmdsInt;
	}

	
	private String changeParams(CmdsHandler cmdhi, String aux_in)
	{
    	//Treat intern parameters and Replace for database values...
    	if (aux_in.indexOf("%P%") > 0)
    	{
        	List<String> vParamInt	= getListInternCmds(aux_in, "%P%|##", 20); 
    		for (String s : vParamInt)
    		{
    			String aux_parval	= cmdhi.selectCmdBody("-1", s);
    			aux_in				= aux_in.replace("%P%" + s + "##", aux_parval);
    		}
    	}
		return aux_in;
	}
	
	
	public void callCmdsInterval() throws IOException
	{
		CmdsHandler cmdhi		= new CmdsHandler();
		String aux_path			= this.path_out;
		FileWriter writer 		= new FileWriter(aux_path, false);
		
        for (List<String> list : this.lstCmds)
        {
        	String aux_code 	= list.get(1);
        	String aux_cmde 	= list.get(2);
        	String aux_inte 	= list.get(3);

        	//Calc age of lastexec in minutes...
        	String aux_agelast 	= cmdhi.getAgeCheckLastExec(aux_code);
        	Float ageLastExec 	= Float.parseFloat(aux_agelast);
        	Float intervCheck 	= Float.parseFloat(aux_inte);
        	
        	if (ageLastExec 	< intervCheck)
        	{
        		System.out.println("Last execution of " + aux_code + " was (" + ageLastExec + ") minutes Ago... Will start in (" + (intervCheck-ageLastExec) + ") minutes...");
        	}
        	else
        	{
	        	//Change @LOGFILE to path_out...   
	        	String aux_cmdr				= aux_cmde.replace("@LOGFILE", (aux_path + "." + this.check_cd));
	
	        	//Treat intern parameters and Replace for database values...
	        	aux_cmdr					= changeParams(cmdhi, aux_cmdr);
	        	
	        	//Treat the intern Unix call sql/shell commands to save locally...
	        	if (aux_cmdr.indexOf("@@") > 0)
	        	{
	            	List<String> vCmdsInt	= getListInternCmds(aux_cmdr, "@@|##", 20); 
	        		aux_cmdr				= aux_cmdr.replace("@@", "").replace("##", "");
	        		//
	        		for (String s : vCmdsInt)
	        		{
	        			String aux_cmdi		= cmdhi.selectCmdBody("0", s);
	        			
	        			//MPS***  trata os programas externos  *********************
	        			if (aux_cmdi.indexOf("@@") > 0)
	        			{
	    	            	List<String> vCmdsInti	= getListInternCmds(aux_cmdi, "@@|##", 20); 
	    	            	aux_cmdi				= aux_cmdi.replace("@@", "").replace("##", "");

	    	        		for (String si : vCmdsInti)
	    	        		{
	    	        			String aux_cmdin	= cmdhi.selectCmdBody("0", si);
	    	        			
	    	        			//Treat intern parameters and Replace for database values...
	    	        			aux_cmdin			= changeParams(cmdhi, aux_cmdin);

	    	        			//Call Process to update/create tmp CMD... 
	    	            		updRecCmdFileTmp(si, aux_cmdin);
	    	        		}
	        			}
	        			//**********************************************************
	        			
	        			//Treat intern parameters and Replace for database values...
	        			aux_cmdi			= changeParams(cmdhi, aux_cmdi);
	                	
	            		//Call Process to update/create tmp CMD... 
	            		updRecCmdFileTmp(s, aux_cmdi);
					}
	        	}
	        	
	    		//Call execution...
	    		System.out.println("Starting Check Execution - " + aux_code + "...");
	    		String ret 					= "0";
	    		try
	    		{
	    			List<String> listDB = Arrays.asList(aux_cmdr.split("\n")); 
					ret = execProcess(listDB);
				}
	    		catch (InterruptedException e)
	    		{
					e.printStackTrace();
					ret = e.getMessage();
				}
	    		System.out.println("End Check Execution - " + aux_code + " - " + ret);
	    		//
	    		if (ret.equals("0"))
	    		{
	    			//Get fileContent of aux_path + "." + this.check_cd...
	    	    	String fileContent		= "";
	    			fileContent				= readAllFile(aux_path + "." + this.check_cd);
	
	    			if (fileContent.length() >= 65535)
	    				cmdhi.setLastLogCheck(fileContent.substring(0, 65535), aux_code);	//upd. lastlog...
	    			else
	    				cmdhi.setLastLogCheck(fileContent, aux_code);						//upd. lastlog...
	
	    			cmdhi.setLastExecCheck(aux_code); 										//upd. lastexec...
					
	    			//Write and Join checks into path_out... 
	    			writer.write(fileContent);
	    		}
        	}
		}
    	//Close conn...
    	cmdhi.dbClose();
        writer.close();
	}

	
	private String updRecCmdFileTmp(String fileTmp, String aux_cmdr) throws IOException
	{
		String fileCMD 			= fileTmp;
		boolean ehDiffDBcmdr	= true;

		//Fill the file content to compare...
    	String fileContent 		= "";
		fileContent				= readAllFile(fileCMD);

		if (fileContent.trim().contentEquals(aux_cmdr.trim()))
			ehDiffDBcmdr 		= false; 

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
	
	
	public static String execProcess(List<String> list) throws IOException, InterruptedException
	{
		String[] command = { "cmd", };
		Process p = Runtime.getRuntime().exec(command);
		new Thread(new SyncPipe(p.getErrorStream(), System.err)).start();
		new Thread(new SyncPipe(p.getInputStream(), System.out)).start();
		PrintWriter stdin = new PrintWriter(p.getOutputStream());

		// write any other commands you want here
		for (String s : list)
		{
			if (!s.isEmpty())
			{
				stdin.println(s);
			}
		} 
		//    
	    stdin.close();
	    int returnCode = p.waitFor();
	    //
    	return String.valueOf(returnCode);
	}
	
	
	public static void main(String[] args) throws InterruptedException
	{
		try
		{
			//
			//ECMchecksCmds ecmCmds1 = new ECMchecksCmds("111", "mptest_infra.log");
			//ecmCmds1.callCmdsInterval();
			//
			//refactoring cmds no DB... (testado por Marlon) ok 

			
			//ECMchecksCmds ecmCmds222 = new ECMchecksCmds("222", "mptest_otass.log");
			//ecmCmds222.callCmdsInterval();
			//
			//refactoring cmds no DB... - testado MPS ok 

			
			ECMchecksCmds ecmCmds333 = new ECMchecksCmds("333", "mptest_dpwin.log");
			ecmCmds333.callCmdsInterval();
			//
			//refactoring cmds no DB... - testado MPS.... 
			
			
			// 
			//ECMchecksCmds ecmCmds1 = new ECMchecksCmds("1", "mps_infra.log");
			//ecmCmds1.callCmdsInterval();
			// ok
			//ECMchecksCmds ecmCmds2 = new ECMchecksCmds("2", "mps_otass.log");
			//ecmCmds2.callCmdsInterval();
			// ok
//			ECMchecksCmds ecmCmds3 = new ECMchecksCmds("3", "mps_dpwin.log");
//			ecmCmds3.callCmdsInterval();
//			// 
//			ECMchecksCmds ecmCmds4 = new ECMchecksCmds("4", "mps_sql.log");
//			ecmCmds4.callCmdsInterval();
//			//
//			ECMchecksCmds ecmCmds5 = new ECMchecksCmds("5", "mps_fcir.log");
//			ecmCmds5.callCmdsInterval();
//
//			ECMchecksCmds ecmCmds6 = new ECMchecksCmds("6", "mps_pix.log");
//			ecmCmds6.callCmdsInterval();
//
//			ECMchecksCmds ecmCmds7 = new ECMchecksCmds("7", "mps_icc.log");
//			ecmCmds7.callCmdsInterval();
			
			
			//if (args.length > 1)
			//{
			//	ECMchecksCmds ecmCmds = new ECMchecksCmds(args[0], args[1]);
			//	ecmCmds.callCmdsInterval();
			//}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
