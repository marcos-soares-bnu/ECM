package main;

import database.CmdsHandler;

import java.util.ArrayList;
import java.util.List;

public class ECMchecksCmds {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		List <List <String> > lstCmds = new ArrayList<>();		
		CmdsHandler cmdh = new CmdsHandler();
		
        if (args.length > 0)
        {
        	lstCmds = cmdh.selectCmds(args[0]);
        }
		
        for (List<String> list : lstCmds) {

        	for (String s : list) {

        		System.out.println(s);
			}
        	
		}
		
		
		
	}

}
