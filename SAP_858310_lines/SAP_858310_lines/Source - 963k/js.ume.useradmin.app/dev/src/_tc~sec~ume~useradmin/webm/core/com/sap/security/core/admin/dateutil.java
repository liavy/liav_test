package com.sap.security.core.admin;

import java.util.Locale;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.util.IUMTrace;

public class DateUtil {
    public final static String  VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/DateUtil.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
	
	public static final int SHORT = DateFormat.SHORT;
	public static final int MEDIEM = DateFormat.MEDIUM;
	public static final int LONG = DateFormat.LONG;
	
    private static final IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);

    private static final String month = "M";
    private static final String year = "y";
    private static final String day = "d";
    private static final String not_to_be_used = "}";
    private final SimpleDateFormat sdf;
    private final SimpleDateFormat sdtf;
    private String pattern;
    private String seperator = not_to_be_used;
    private String fp, sp, tp;
    private Calendar calendar;

	public DateUtil (int style, Locale locale) {
		int i;

		if (com.sap.security.core.admin.util.CHECKNOTNULL(locale))
			locale=java.util.Locale.getDefault();

		this.calendar = Calendar.getInstance(locale);
		this.sdf = (SimpleDateFormat) DateFormat.getDateInstance(style, locale);
		this.sdtf = (SimpleDateFormat) DateFormat.getDateTimeInstance(style, style, locale);

		pattern = sdf.toPattern();
		int len = pattern.length();
		trace.debugT("DateUtil", pattern);

		for (i=0; i<=len; i++) {
			String s = pattern.substring(i, i+1);
			trace.debugT("DateUtil", s);
			if ( ! year.equalsIgnoreCase(s)
				&& !month.equalsIgnoreCase(s)
				&& !day.equalsIgnoreCase(s) ) {
				seperator = s;
				break;
			}
		}
		trace.debugT("DateUtil", "separator is: "+seperator);

		if ( i < len ) {
			StringTokenizer st = new StringTokenizer(pattern, seperator); 
			fp = st.nextToken().substring(0, 1);
			sp = st.nextToken().substring(0, 1);
			tp = st.nextToken().substring(0, 1);
		} else {
			// no seperator
			fp = pattern.substring(0, 1);
			for ( i=1; i<len; i++) {
				if ( fp.equalsIgnoreCase(pattern.substring(i, i+1)) ) {
					continue;
				} else {
					sp = pattern.substring(i, i+1);
				}
			}
			for ( i=i+1; i<len; i++) {
				if ( sp.equalsIgnoreCase(pattern.substring(i, i+1)) ) {
					continue;
				} else {
					tp = pattern.substring(i, i+1);
				}
			}
		}
		trace.debugT("DateUtil", fp+" " +sp+" "+tp);		
	} // DateUtil(int, Locale)
	
    public DateUtil (Locale locale) {
    	this(SHORT, locale);
    } // DateUtil(Locale)

	public Calendar getCalendar() {
        return this.calendar;
    } // getCalendar

	public SimpleDateFormat getDateTimeFormat() {
		return this.sdtf;
	} // getDateTimeFormat
		
    public SimpleDateFormat getDateFormat() {
        return this.sdf;
    } // getDateFormat

    public boolean isYearAtFirst() {
        return (fp.equals(year)==true?true:false);
    } // isYearAtFirst

    public String getDateFormatSeperator() {
        return (seperator.equals(not_to_be_used)==true?"":seperator);
    } // getDateFormatSeperator

    public String getPattern() {
        String str = pattern;
        StringBuffer sb = new StringBuffer(str);
        sb.replace(str.indexOf(year), str.lastIndexOf(year)+1, "yyyy");
        str = sb.toString();
        sb.replace(str.indexOf(month), str.lastIndexOf(month)+1, "mm");
        str = sb.toString();
        sb.replace(str.indexOf(day), str.lastIndexOf(day)+1, "dd");
        return sb.toString();
    } // getPattern

    public String getSP() {
        return sp;
    } // getSP

    public String getFP() {
        return fp;
    } // getFP

    public String getTP() {
        return tp;
    } // getTP

    public int getActualMaximumDays(Date date) {
        Calendar cal = this.calendar;
        if ( null != date ) cal.setTime(date);
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    } // getActualMaximumDays

    public int getActualMaximumMonths(Date date) {
        Calendar cal = this.calendar;
        if ( null != date ) cal.setTime(date);
        return cal.getActualMaximum(Calendar.MONTH)+1;
    } // getActualMaximumMonths

    public int getActualMinimumDays(Date date) {
        Calendar cal = this.calendar;
        if ( null != date ) cal.setTime(date);
        return cal.getActualMinimum(Calendar.DAY_OF_MONTH);
    } // getActualMinimumDays

    public int getActualMinimumMonths(Date date) {
        Calendar cal = this.calendar;
        if ( null != date ) cal.setTime(date);
        return cal.getActualMinimum(Calendar.MONTH);
    } // getActualMiniMonths

    public int getActualMaximumYear(Date date) {
        Calendar cal = this.calendar;
        if ( null != date ) cal.setTime(date);
        return cal.getActualMaximum(Calendar.YEAR);
    } // getActualMaximumYear

    public int getActualMinimumYear(Date date) {
        Calendar cal = this.calendar;
        if ( null != date ) cal.setTime(date);
        return cal.getActualMinimum(Calendar.YEAR);
    } // getActualMinimumYear

    public String getDateDisplayFormat() {
        StringBuffer ddf = new StringBuffer(); // DateDisplayFormat
        String s = fp;
        for (int i=1; i<4; i++) {
            if ( i == 2) {
                s = sp;
            } else if ( i == 3) {
                s = tp;
            }
            if ( s.equalsIgnoreCase (year) ) {
                ddf.append("year_full");
            } else if ( s.equalsIgnoreCase(month) ) {
                ddf.append("month_in_year_two");
            } else if ( s.equalsIgnoreCase(day) ) {
                ddf.append("day_in_month");
            }
            if ( i < 3 ) ddf.append(seperator);
        }
		if ( !pattern.endsWith(fp) && !pattern.endsWith(sp) && !pattern.endsWith(tp) )
			ddf.append(pattern.charAt((pattern.length()-1)));        
        return ddf.toString();
    } // getDateDisplayFormat

	public String getTime(Date date) {
		if (util.CHECKNOTNULL(date))
			return "";

		return this.sdtf.format(date);	
	} // getTime
	
    public String format(Date date) {
    	if (util.CHECKNOTNULL(date))
    		return "";

        return this.sdf.format(date);
    } // format

    public String formatInDisplayFormat(Date date) {
    	/*@todo: test for null */
    	if (com.sap.security.core.admin.util.CHECKNOTNULL(date))
    		return "";

		String dates = format(date);
        StringBuffer datesb = new StringBuffer(dates);
        Calendar calendar = this.calendar;
        calendar.setTime(date);
        int length = dates.length();
		String seperator = this.getDateFormatSeperator();
		int len = seperator.length();
		if ( isYearAtFirst() ) {
			if ( len > 0 ) {
				int firstpos = dates.indexOf(seperator);
				if (  firstpos < 4 ) {
					datesb.replace(0, firstpos, Integer.toString(calendar.get(Calendar.YEAR)));
				}
			} else {
				// todo
			}            
		} else {
			if ( len > 0 ) {
				int lastpos = dates.lastIndexOf(seperator);
				if (  (length -lastpos) < 4 ) {
					datesb.replace((lastpos+1), length, Integer.toString(calendar.get(Calendar.YEAR)));
				}
			} else {
				// todo
			}        	
		}
        return datesb.toString();
    } // formatInDisplayFormat

    public String composeStrInRequiredFormat(String s) {
        String seperator = getDateFormatSeperator();
        s = s.trim();
        char[] cha = s.toCharArray();
        StringBuffer sb = new StringBuffer(s);
        for ( int i=0; i<sb.length(); i++) {
            if ( !Character.isDigit(cha[i]) && !(Character.isSpace(cha[i]) || Character.isSpaceChar(cha[i]))) {
                if ( seperator.length() > 0 )
                    sb.replace(i, i+1, seperator);
                else
                    sb.deleteCharAt(i);
            } // else continue
        }
        return sb.toString();
    } // composeStrInRequiredFormat(String)

    public Date strToDate(String s) {
        trace.debugT("strToDate", "before any change!!!!!!!!!!!!!!!!", new String[]{s});
        String str = composeStrInRequiredFormat(s);
        trace.debugT("strToDate", "before dateFormat.parse!!!!!!!!!!!!!!!!", new String[]{str});
        if ( null == util.checkEmpty(str) ) {
            return null;
        } else {
            try {
                sdf.setLenient(false);
                trace.debugT("strToDate", "after dateFormat.parse!!!!!!!!!!!!!!!!", new String[]{sdf.parse(str).toString()});
                return sdf.parse(str);
            } catch (java.text.ParseException ex) {
                trace.errorT("strToDate", ex.getMessage(), ex);
                return null;
            }
        }
    } // StrToDate
    
	public boolean isDateValid(String s) {
		final String methodName = "isDateValid";
		Date dt = null;
		if ( null == util.checkEmpty(s)) {
			return true;
		} else {
			try {
				sdf.setLenient(false);
				dt = this.getDateFormat().parse(s);
			} catch(Exception ex) {
				trace.errorT(methodName, ex.getMessage(), ex);
				return false;
			} // to parse date

			boolean eOccur = false;
			StringTokenizer st = new StringTokenizer(s, seperator);
			String toCompare = this.getFP();
			String input = null;
			for (int i=1; st.hasMoreTokens(); i++) {				
				input = st.nextToken().trim();
				if ( i == 2 ) {
					toCompare = this.getSP();
				} else if ( i == 3 ) {
					toCompare = this.getTP();
				}

				int value = 0;
				try 
				{
					value = (new Integer(input)).intValue();
				} 
				catch (NumberFormatException ex) 
				{
					trace.warningT(methodName, ex.getMessage(), ex);
					eOccur = true;
					break;
				}

				if ( toCompare.indexOf(year) >= 0 ) {
					if ( value > this.getActualMaximumYear(dt)
						|| value < this.getActualMinimumYear(dt) ) {
						eOccur = true;
						break;
					}
				} else if ( toCompare.indexOf(month) >= 0 ) {
					if ( value > this.getActualMaximumMonths(dt)
						|| value < this.getActualMinimumMonths(dt) ) {
						eOccur = true;
						break;
					}
				} else if  ( toCompare.indexOf(day)  >= 0) {
					if ( value > this.getActualMaximumDays(dt)
						|| value < this.getActualMinimumDays(dt) ) {
						eOccur = true;
						break;
					}
				}
			}
			if ( eOccur )  return false;
			else return true;
		}
	} // isDateValid  

	public static Date decDateWhenMidnight(Date date)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);
		int seconds = calendar.get(Calendar.SECOND);
		int mseconds = calendar.get(Calendar.MILLISECOND);
		if (hours == 0 && minutes == 0 && seconds == 0 && mseconds == 0) calendar.add(Calendar.DAY_OF_MONTH, -1);
		return calendar.getTime();
	}
	
	public static Date incDate(Date date)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		return calendar.getTime();
	}    


}
