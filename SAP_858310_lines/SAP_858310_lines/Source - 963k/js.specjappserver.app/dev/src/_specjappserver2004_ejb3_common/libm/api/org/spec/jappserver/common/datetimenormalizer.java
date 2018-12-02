/*
 * Created on 01.07.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.spec.jappserver.common;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * This class can be used to normalize time values that need to be stored via
 * JDBC. Solves the issues with <code>java.sql.Date</code> and
 * <code>java.sql.Time</code>: Both needs to have their millisecond values to
 * be normalized (i.e. the time components must be zero for
 * <code>java.sql.Date</code> and the date component must be
 * <code>'1970-01-01'</code> for <code>java.sql.Time</code>), but fail to do the
 * normalization in their constructor.
 *
 * @version $Id: //tc/jdbi/dev/src/java/com/sap/sql/DateTimeNormalizer.java#4 $
 */
public final class DateTimeNormalizer {

    /*
     * prevent instantiation (and subclassing)
     */
     private DateTimeNormalizer() {}

    /*
     *  makes the milli second value accessible (it is protected in GregorianCalendar)
     */
    private static final class SpecialCalendar extends GregorianCalendar {
        public long getTimeInMillis() {
            return super.getTimeInMillis();
        }
        public void setTimeInMillis(long millis) {
            super.setTimeInMillis(millis);
        }
    }

    /*
     * we need to be thread safe
     */
    private static final ThreadLocal LOCAL_CALENDAR = new ThreadLocal() {
        protected Object initialValue() {
            return new SpecialCalendar();
        }
    };

    /**
    * Normalizes a milli second value according to the specification of <code>java.sql.Date.</code>.
    * That is the hour, minute, second and millisecond fields are set to zero according to the
    * VMs default time zone. Typical usage is like:
    * <p>
    * <pre>java.sql.Date birthday = new java.sql.Date(normalizeSqlDateMillies(millis));</pre>
    *
    * @param milliSeconds the value to normalize.
    * @return normalized milli second value.
    */
    public final static long normalizeSqlDateMillies(long milliSeconds) {
        SpecialCalendar cal = (SpecialCalendar) LOCAL_CALENDAR.get();
        cal.setTimeInMillis(milliSeconds);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();
    }

    /**
    * Normalizes a milli second value according to the specification of <code>java.sql.Time.</code>.
    * That is the date fields are set to 01.01.1970 according to the VMs default time zone.
    * Typical usage is like:<p>
    * <pre>java.sql.Time noon = new java.sql.Date(normalizeSqlTimeMillies(millis));</pre>
    *
    * @param milliSeconds the value to normalize.
    * @return normalized milli second value.
    */
    public final static long normalizeSqlTimeMillies(long milliSeconds) {
        SpecialCalendar cal = (SpecialCalendar) LOCAL_CALENDAR.get();
        cal.setTimeInMillis(milliSeconds);

        cal.set(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.YEAR, 1970);
        cal.set(Calendar.MILLISECOND, 0); // TIME cannot store milli second precision

        return cal.getTimeInMillis();
    }
}