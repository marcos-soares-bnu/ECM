import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import text.FileReaderUtil;

public class RunOTASS {
	public static void main(String[] args) {
		String pathCheck = "D:\\IAS_Monitoring\\APP_Dev\\SCHEDScripts\\02_SCHEDULEDcheck_OTASS_NO_LASTID_UPDATE.cmd user password";
		System.out.println("Check Path: \n" + pathCheck);
		try {
			Process p = Runtime.getRuntime().exec("cmd /c start /min /wait " + pathCheck);
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

		String pathFile = "D:\\IAS_Monitoring\\APP_Dev\\SCHEDScripts\\sched_otass_nolastid.log";
		System.out.println("Check File: \n" + pathFile);
		FileReaderUtil fileReader = new FileReaderUtil(pathFile);
		String fileContent = fileReader.readFile();

		if (!fileContent.isEmpty()) {
			JFrame jf = new JFrame();
			jf.setTitle("TextResult");
			jf.setResizable(false);
			jf.setPreferredSize(new Dimension(800, 800));
			jf.setMinimumSize(new Dimension(800, 800));
			jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			JScrollPane sp = new JScrollPane();
			sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			jf.getContentPane().add(sp, BorderLayout.CENTER);

			JTextArea ta = new JTextArea();
			sp.setViewportView(ta);

			ta.setText(fileContent);
			jf.show();
		}
	}
}
