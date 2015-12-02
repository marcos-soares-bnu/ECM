package manual_interface;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import database.DBUtil;
import main.ECMchecksCmds;
import text.FileReaderUtil;
import util.Constantes;

public class MainFrame extends JFrame {

    private DBUtil dbUtil;

    private JTextArea tfaLog;
    private JButton btnUpdateActions;

    private JPanel pnRun;
    private JPanel pnLog;

    private JPanel pnDBOutput;
    private JPanel pnOutputFile;
    private JButton btnUpdate;
    private JTextField tfOut;

    public MainFrame() {
        setMaximumSize(new Dimension(1000, 700));
        setSize(new Dimension(1000, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dbUtil = new DBUtil();
        setTitle("Script Testing Interface");

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        pnRun = new JPanel();
        tabbedPane.addTab("Scripts", null, pnRun, null);
        pnRun.setLayout(new BoxLayout(pnRun, BoxLayout.X_AXIS));

        pnLog = new JPanel();
        tabbedPane.addTab("LOG", null, pnLog, null);
        pnLog.setLayout(new BorderLayout(0, 0));

        btnUpdate = new JButton("UPDATE");
        btnUpdate.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent arg0) {
                FileReaderUtil fileUtil = new FileReaderUtil(tfOut.getText());
                tfaLog.setText(fileUtil.readFile());
            }
        });
        pnLog.add(btnUpdate, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane();
        pnLog.add(scrollPane, BorderLayout.CENTER);

        tfaLog = new JTextArea();
        scrollPane.setViewportView(tfaLog);

        pnDBOutput = new JPanel();
        tabbedPane.addTab("DBOutput", null, pnDBOutput, null);
        pnDBOutput.setLayout(new BoxLayout(pnDBOutput, BoxLayout.X_AXIS));

        pnOutputFile = new JPanel();
        getContentPane().add(pnOutputFile, BorderLayout.NORTH);
        FlowLayout fl_pnOutputFile = (FlowLayout) pnOutputFile.getLayout();
        fl_pnOutputFile.setAlignment(FlowLayout.LEFT);

        JLabel lblOutputFile = new JLabel("Output File:");
        pnOutputFile.add(lblOutputFile);

        tfOut = new JTextField();
        tfOut.setText("C:\\Temp\\scriptlog_.log");
        pnOutputFile.add(tfOut);
        tfOut.setColumns(20);

        btnUpdateActions = new JButton("Update Actions");
        btnUpdateActions.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                pnRun.removeAll();
                pnRun.revalidate();
                pnRun.repaint();

                pnDBOutput.removeAll();
                pnDBOutput.revalidate();
                pnDBOutput.repaint();

                loadDBScripts(true);
                loadDBScripts(false);
            }
        });
        pnOutputFile.add(btnUpdateActions);

        loadDBScripts(true);
        loadDBScripts(false);
    }

    private void loadDBScripts(boolean mainScreen) {
        //Busca os checks do banco de dados e insere no mapa
        Map<Integer, InterfaceCheck> checks = new TreeMap<Integer, InterfaceCheck>();
        ResultSet rs = dbUtil.doSelect("*", Constantes.DB_Checks_Table);
        try {
            while (rs.next()) {
                int id = rs.getInt(1);
                InterfaceCheck c = new InterfaceCheck(id);
                checks.put(id, c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Se foi buscado algo do Banco de dados, continua
        if (!checks.isEmpty()) {
            //Insere no layout
            for (InterfaceCheck c : checks.values()) {
                JPanel pn = new JPanel();
                pn.setLayout(new BoxLayout(pn, BoxLayout.Y_AXIS));
                pn.setAlignmentY(Component.TOP_ALIGNMENT);
                String title = c.getId() + " - " + c.getCheck_name();
                pn.setBorder(new TitledBorder(null, title, TitledBorder.LEADING, TitledBorder.TOP, null, null));
                pn.setMinimumSize(new Dimension(300, 30));
                pn.setMaximumSize(new Dimension(300, 1000));

                //Get ITENS from Check
                List<InterfaceCheckItem> cItens = c.getCheckItens();
                for (int i = 0; i < cItens.size(); i++) {
                    String btnName = cItens.get(i).getItem_name();
                    JButton btn = new JButton(btnName);
                    btn.setAlignmentX(Component.CENTER_ALIGNMENT);
                    btn.setMinimumSize(new Dimension(120, 20));
                    btn.setMaximumSize(new Dimension(180, 30));
                    addButtonAction(btn, cItens.get(i), mainScreen);
                    pn.add(btn);
                }

                if (mainScreen) {
                    pnRun.add(pn);
                } else {
                    pnDBOutput.add(pn);
                }
            }
        }
    }

    private void addButtonAction(JButton btn, InterfaceCheckItem checkItem, boolean mainScreen) {
        final String code = checkItem.getItem_name();
        if (mainScreen) {
            final String check_cd = String.valueOf(checkItem.getCheck_id());
            final String path_out = tfOut.getText();

            btn.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent arg0) {
                    ECMchecksCmds cmd = new ECMchecksCmds(check_cd, code, path_out);
                    try {
                        cmd.callCmdsInterval();
                        JOptionPane.showMessageDialog(null, "Check executed!");
                    } catch (IOException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE, null);
                    }
                }
            });
        } else {
            ResultSet rs = dbUtil.doSelect("lastexec, lastLog", Constantes.DB_ChecksCmds_Table, "code='" + code + "'");
            try {
                if (rs.next()) {
                    final String exec = String.valueOf(rs.getTimestamp("lastexec"));
                    final String log = rs.getString("lastlog");

                    btn.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent arg0) {
                            JOptionPane.showMessageDialog(null, code + "\nLast Exec Time: " + exec + "\nLast Log:\n" + log);
                        }
                    });
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
