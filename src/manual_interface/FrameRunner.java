package manual_interface;

import javax.swing.UIManager;

public class FrameRunner {

    public static void main(String[] args) {
        try {
            // select Look and Feel
            UIManager.setLookAndFeel("com.jtattoo.plaf.acryl.AcrylLookAndFeel");
            // start application
            MainFrame fr = new MainFrame();
            fr.setVisible(true);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
}
