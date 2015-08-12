package logic_process;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import util.Constantes;

public class Logic {

    public Calendar getData(Date d) {
        Calendar data = new GregorianCalendar();
        data.setTime(d);
        return data;
    }
    
    public String createErrorString(Map<Integer, String> erros) {
        String errors = "";
        for (String error : erros.values()) {
            errors += error + "\n";
        }
        if (Constantes.SHOW_OTHER_MESSAGES) {
            System.out.println("\n" + errors);
        }
        return errors;
    }

}
