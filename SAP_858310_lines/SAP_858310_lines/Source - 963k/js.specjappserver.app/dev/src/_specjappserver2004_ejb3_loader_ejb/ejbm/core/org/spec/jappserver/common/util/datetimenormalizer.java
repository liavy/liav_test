package org.spec.jappserver.common.util;

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
        @Override public long getTimeInMillis() {
            return super.getTimeInMillis();
        }
        @Override public void setTimeInMillis(long millis) {
            super.setTimeInMillis(millis);
        }
    }

    /*
     * we need to be thread safe
     */
    private static final ThreadLocal LOCAL_CALENDAR = new ThreadLocal() {
        @Override protected Object initialValue() {
            return new SpecialCalendar();
        }
    };

    /**
    * Normalizes a milli second value according to the specification of <code>java.sql.Date.</code>.
    * That is the hour, minute, second and millisecond fields are set to zero according to the
    * VMs default time zone.
    * Typical usage is like:
    * <p>
    * <pre>java.sql.Date birthday = new java.sql.Date(normalizeSqlDateMillies(millis));</pre>
    *
    * @param milliSeconds the value to normalize.
    * @return normalized milli second value.
    */
    public final static long normalizeSqlDateMillis(long milliSeconds) {
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
    public final static long normalizeSqlTimeMillis(long milliSeconds) {
        SpecialCalendar cal = (SpecialCalendar) LOCAL_CALENDAR.get();
        cal.setTimeInMillis(milliSeconds);

        cal.set(Calendar.DAY_OF_YEAR, 1);
        cal.set(Calendar.YEAR, 1970);
        cal.set(Calendar.MILLISECOND, 0); // TIME cannot store milli second precision

        return cal.getTimeInMillis();
    }

    /**
     * Normalizes the milli second value of <code>java.sql.Date</code> object according to its specifiaction.
     * That is the hour, minute, second and millisecond fields are set to zero according to the
     * VMs default time zone.
     *
     * @param date the value to normalize.
     */
    public final static void normalizeSqlDate(java.sql.Date date) {
        date.setTime(normalizeSqlDateMillis(date.getTime()));
    }
    
    /**
     * Normalizes the milli second value of according <code>java.sql.Time.</code> to its specification.
     * That is the date fields are set to 01.01.1970 according to the VMs default time zone.
     *
     * @param time the value to normalize.
     */
    public final static void normalizeSqlTime(java.sql.Time time) {
        time.setTime(normalizeSqlTimeMillis(time.getTime()));
    }
    
    /**
     * Normalizes current milli second value according to the specification of <code>java.sql.Date.</code>.
     * That is the hour, minute, second and millisecond fields are set to zero according to the
     * VMs default time zone.
     * The normalized milli second value is used to instantiate a java.sql.Date object. 
     *
     * @param milliSeconds the value to normalize.
     * @return current date value.
     */
    public final static java.sql.Date getNormalizedSqlDate(long milliSeconds) {
        return new java.sql.Date(normalizeSqlDateMillis(milliSeconds));
    }

    /**
     * Normalizes a milli second value according to the specification of <code>java.sql.Time.</code>.
     * That is the date fields are set to 01.01.1970 according to the VMs default time zone.
     * The normalized milli second value is used to instantiate a java.sql.Time object. 
     *
     * @param milliSeconds the value to normalize.
     * @return current time value.
     */
    public final static java.sql.Time getNormalizedSqlTime(long milliSeconds) {
        return new java.sql.Time(normalizeSqlTimeMillis(milliSeconds));
    }

    /**
     * Normalizes current milli second value according to the specification of <code>java.sql.Date.</code>.
     * That is the hour, minute, second and millisecond fields are set to zero according to the
     * VMs default time zone.
     * The normalized milli second value is used to instantiate a java.sql.Date object. 
     *
     * @return current date value.
     */
    public final static java.sql.Date getNormalizedCurrentSqlDate() {
        return getNormalizedSqlDate(System.currentTimeMillis());
    }

    /**
     * Normalizes current milli second value according to the specification of <code>java.sql.Time.</code>.
     * That is the date fields are set to 01.01.1970 according to the VMs default time zone.
     * The normalized milli second value is used to instantiate a java.sql.Time object. 
     *
     * @return current time value.
     */
    public final static java.sql.Time getNormalizedCurrentSqlTime() {
        return getNormalizedSqlTime(System.currentTimeMillis());
    }
}