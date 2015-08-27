package util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class DateUtil {

    SimpleDateFormat sdfDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);
    SimpleDateFormat sdfOutput = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss", Locale.GERMANY);
    DateFormat sdfCMD = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.GERMANY);
    DateFormat sdfFiles = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.GERMANY);
    DateFormat sdfFilesPix = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a", Locale.GERMANY);
    SimpleDateFormat sdfWeekDay = new SimpleDateFormat("EEEE", Locale.GERMANY);
    SimpleDateFormat sdfJustTime = new SimpleDateFormat("hh:mm a", Locale.GERMANY);
    SimpleDateFormat sdfJustHour = new SimpleDateFormat("HH", Locale.GERMANY);
    SimpleDateFormat sdfJustMinute = new SimpleDateFormat("mm", Locale.GERMANY);

    public Date getDateFromString(String s) {
        Date dt = null;
        try {
            dt = (Date) sdfFiles.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt;
    }

    public Date getDateFromDBString(String s) {
        Date dt = null;
        try {
            dt = (Date) sdfDB.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt;
    }
    
    public String getDBFormat(Date exec_time) {
        String strDB = sdfDB.format(exec_time);
        return strDB;
    }

    public String getStrDateFromDB(long time) {
        return sdfDB.format(new Date(time));
    }

    public Date getFileDateFromString(String s) {
        Date dt = null;
        try {
            dt = (Date) sdfFiles.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt;
    }

    public Date getFileDateFromStringPIXCORE(String s) {
        Date dt = null;
        try {
            dt = (Date) sdfFilesPix.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt;
    }

    public long getHoursDif(Date d1, Date d2) {
        return (d2.getTime() - d1.getTime()) / 3600000;
    }

    public boolean moreThanXhours(Date nowDate, Date fileDate, int XHours) {
        long horas = (nowDate.getTime() - fileDate.getTime()) / 3600000;
        if (horas >= XHours) {
            return true;
        }
        return false;
    }

    public String getWeekDay(Date d) {
        String wDay = sdfWeekDay.format(d);
        return wDay;
    }

    public Date getDateTime(String s) {
        Date dt = null;
        try {
            dt = (Date) sdfJustTime.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt;
    }

    public int getDateHour(Date d) {
        int hour = 0;
        hour = Integer.valueOf(sdfJustHour.format(d));
        return hour;
    }

    public int getDateMinute(Date d) {
        int minute = 0;
        minute = Integer.valueOf(sdfJustMinute.format(d));
        return minute;
    }

    public int getStringHour(String d) {
        Date dt = null;
        int hour = 0;
        try {
            dt = new Date(sdfJustTime.parse(d).getTime());
            hour = Integer.valueOf(sdfJustHour.format(dt));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return hour;
    }

    public int getStringMinute(String d) {
        Date dt = null;
        int minute = 0;
        try {
            dt = new Date(sdfJustTime.parse(d).getTime());
            minute = Integer.valueOf(sdfJustMinute.format(dt));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return minute;
    }

    /**
     * Used by FCIR003
     * 
     * @param fileInfo
     * @return
     */
    public Date getDateFromStringFCIRchecks(String fileInfo) {
        String[] str = fileInfo.split(" ");
        String strDtFull = str[0] + " " + str[2] + " " + str[3];
        Date dt = (Date) this.getFileDateFromString(strDtFull);
        return dt;
    }
    
    /**
     * Used by PIX005
     * 
     * @param fileInfo
     * @return
     */
    public Date getDateFromStringPIXCOREchecks(String fileInfo) {
        String[] str = fileInfo.split(" ");
        String strDtFull = str[0] + " " + str[1] + " " + str[2];
        Date dt = (Date) this.getFileDateFromStringPIXCORE(strDtFull);
        return dt;
    }

    public Date getFileDateFCIR008(String fileInfo) {
        String[] str = fileInfo.split("   ");
        String strDtFull = str[3] + " " + str[4];
        Date dt = (Date) this.getFileDateFromString(strDtFull);
        return dt;
    }

    public Date getFileDateDPWIN007(String fileInfo) {
        String[] str = fileInfo.split("    ");
        Date dt = (Date) this.getFileDateFromString(str[0]);
        return dt;
    }

    public Date getJobInfoDatePIX(String fileInfo) {
        String[] str = fileInfo.split("                        ");
        Date dt = null;
        try {
            dt = (Date) sdfFilesPix.parse(str[1].trim());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt;
    }

    /**
     * Used by: PIX005, PIX007, PIX009, PIX010
     * 
     * @param fileInfo
     * @return
     */
    public Date getFileDatePIX(String fileInfo) {
      String[] str = fileInfo.split("    ");
      Date dt = (Date) this.getFileDateFromStringPIXCORE(str[0]);
      return dt;
    }

    public long getMinDif(Date d1, Date d2) {
        long diff = 0;
        diff = d1.getTime() - d2.getTime();
        //diff em min:
        diff = diff / 1000 / 60;

        return diff;
    }

    /**
     * Used in PIX011/PIX012
     * 
     * @param fileDate
     * @param arrayDates
     * @return
     */
    @SuppressWarnings("deprecation")
    public Date[] getDates(String[] strDate, Date defaultDate) {
        Date[] datesArray = new Date[strDate.length];

        int hour = 0;
        int min = 0;

        for (int i = 0; i < strDate.length; i++) {
            min = this.getStringMinute(strDate[i]);
            hour = this.getStringHour(strDate[i]);

            Date dtTemp = new Date(defaultDate.getTime());
            dtTemp.setHours(hour);
            dtTemp.setMinutes(min);

            datesArray[i] = dtTemp;
        }
        return datesArray;
    }

    /**
     * Used in PIX011/PIX012
     */
    public Date getPlannedExecution(Date fileDate, Date[] arrayDates) {
        Date planDate = null;

        //Não está entre as horas do dia (é antes do inicio das execuções ou depois do final delas)
        if (fileDate.before(arrayDates[0]) || fileDate.after(arrayDates[arrayDates.length - 1])) {
            planDate = arrayDates[0];
            if (fileDate.after(arrayDates[arrayDates.length - 1])) {
                Calendar gcPlanExec = new GregorianCalendar();
                gcPlanExec.setTime(planDate);
                gcPlanExec.add(Calendar.DAY_OF_MONTH, 1);
                planDate = gcPlanExec.getTime();
            }
        } else {
            Date dateTemp = null;
            for (int i = 0; i < arrayDates.length; i++) {
                if (dateTemp == null) {
                    dateTemp = arrayDates[i];
                }
                if (!fileDate.after(arrayDates[i]) && !fileDate.equals(arrayDates[i])) {
                    dateTemp = arrayDates[i];
                    break;
                }
            }
            planDate = dateTemp;
        }
        if (Constantes.SHOW_OTHER_MESSAGES) {
            System.out.println(fileDate + "\tSHOULD BE AT\t" + planDate);
        }
        return planDate;
    }
}
