package com.sap.security.core.admin;

import java.util.Locale;
import java.util.Date;
import java.util.Calendar;

import com.sap.security.api.InvalidLogonIdException;
import com.sap.security.api.UMFactory;
import com.sap.security.api.IUser;
import com.sap.security.api.IUserAccount;
import com.sap.security.api.ISecurityPolicy;
import com.sap.security.api.UMException;
import com.sap.security.api.InvalidPasswordException;
import com.sap.security.api.UserAccountAlreadyExistsException;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.imp.UserAccountSearchFilter;
import com.sap.security.core.logon.imp.SecurityPolicy;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.ErrorBean;
import com.sap.security.core.util.Message;

public class UserAccountBean extends UserAccountSearchFilter {

    public static final String beanId = "userAccount";

    public final static String  VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/UserAccountBean.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private final static IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);

    public final static String uniqueID = "uniqueID";
    public final static String logonuid = "logonuid";
    public static final String oldpassword = "oldPassword";
    public static final String password = "pass";
    public static final String passwordconfirm = "passconf";
    public static final String syspassword = "sysPassword";

    public final static String created = "created";
    public final static String locked ="locked";
    public final static String pwd_change = "pwd_change";
    public final static String lockreason = "lockreason";
    public final static String loggedinbegin = "loggedinbetween1";
    public final static String loggedinend = "loggedinbetween2";
    public final static String failedlogonbegin = "failedlogonbegin";
    public final static String failedlogonend = "failedlogonend";
    public final static String createdbegin = "createdbegin";
    public final static String createdend = "createdend";
    public final static String failedattempts = "failedattempts";
    public final static String pwdchangebegin = "pwdchangebegin";
    public final static String pwdchangeend = "pwdchangeend";
    public final static String validfrom = "validfrom";
    public final static String validto = "validto";
    public final static String validfrombegin = "validfrombegin";
    public final static String validtobegin = "validtobegin";
    public final static String validfromend = "validfromend";
    public final static String validtoend = "validtoend";
    public final static String maxcount = "maxcount";

    private String _logonuid;
    private String _oldpassword;
    private String _password;
    private String _passwordconfirm;
    private boolean _isSystemGeneratedPassword = false;

    private String _validfrom;
    private String _validto;
    private String _createdbegin;
    private String _createdend;
    private String _loggedinbegin;
    private String _loggedinend;
    private String _pwdchangebegin;
    private String _pwdchangeend;
    private String _failedattempts;
    private String _lockedReason;
    private String _isLocked;
    private StringBuffer _criteria = new StringBuffer();
    private boolean _isSet = false;
    private boolean isMulti = false;
    private StringBuffer sbf = new StringBuffer();

    private Locale _locale = Locale.getDefault();
    private UserAdminLocaleBean localeBean = null;
    private int _maxcount;
    private IUserAccount _userAccount = null;
    private DateUtil _dateU = new DateUtil(_locale);
    
	private boolean toFilter = true;

    public UserAccountBean() {
        _logonuid = util.empty;
        _oldpassword = util.empty;
        _password = util.empty;
        _passwordconfirm = util.empty;
        setLocale(this._locale);
    } // UserAccountBean


    public UserAccountBean(IUser user, Locale locale) {
        this(user, null, locale);
    } // UserAccountBean(IUser)

    public UserAccountBean(IUser user) {
        this(user, null, null);
    } // UserAccountBean(IUser)

    public UserAccountBean(IUserAccount userAccount) {
        this(null, userAccount, null);
    } // UserAccountBean

    public UserAccountBean(IAccessToLogic proxy) {
        _logonuid = proxy.getRequestParameter(logonuid);
        _oldpassword     = proxy.getRequestParameter(oldpassword);
        _password        = proxy.getRequestParameter(password);
        _passwordconfirm = proxy.getRequestParameter(passwordconfirm);
        _isSystemGeneratedPassword = (proxy.getRequestParameter(syspassword)==null)?false:true;
        try {
            _userAccount = UMFactory.getUserAccountFactory().getUserAccounts(proxy.getRequestParameter(UserBean.uidId))[0];
            if ( null != _userAccount) {
                _locale = _userAccount.getAssignedUser().getLocale();
            	 if (null == _locale) _locale = Locale.getDefault();
            } else
                _locale = Locale.getDefault();
        } catch (Exception ex) {
        	if ( trace.beDebug() ) {
        		trace.debugT("UserAccountBean(IAccessToLogic)", ex.getMessage(), ex);
        	}
            _userAccount = null;
            _locale = Locale.getDefault();
        }
        _dateU = new DateUtil(_locale);
        _validfrom = proxy.getRequestParameter(validfrom);
        _validto =proxy.getRequestParameter(validto);
        _isLocked = proxy.getRequestParameter(locked);
        _lockedReason = proxy.getRequestParameter(lockreason);
        _createdbegin = proxy.getRequestParameter(createdbegin);
        _createdend = proxy.getRequestParameter(createdend);
        _failedattempts = proxy.getRequestParameter(failedattempts);
        _loggedinbegin = proxy.getRequestParameter(loggedinbegin);
        _loggedinend = proxy.getRequestParameter(loggedinend);
        _pwdchangebegin = proxy.getRequestParameter(pwdchangebegin);
        _pwdchangeend = proxy.getRequestParameter(pwdchangeend);
    } // UserAccoutBean(IAccessLogic)

    public UserAccountBean(IAccessToLogic proxy, Locale locale) {
        if ( null != locale ) {
            this._locale = locale;
            // System.out.println(locale.toString());
            _dateU = new DateUtil(_locale);
        }
        
        _logonuid = util.setEmpty(proxy.getRequestParameter(logonuid));
        try {
            _userAccount = UMFactory.getUserAccountFactory().getUserAccounts(proxy.getRequestParameter(UserBean.uidId))[0];
        } catch (Exception ex) {
			if ( trace.beDebug() ) {
				trace.debugT("UserAccountBean(IAccessToLogic)", ex.getMessage(), ex);
			}        	
            _userAccount = null;
        }
        
        _oldpassword     = util.setEmpty(proxy.getRequestParameter(oldpassword));
        _password        = util.setEmpty(proxy.getRequestParameter(password));
        _passwordconfirm = util.setEmpty(proxy.getRequestParameter(passwordconfirm));
        _isSystemGeneratedPassword = (proxy.getRequestParameter(syspassword)==null)?false:true;
        _validfrom = util.setEmpty(proxy.getRequestParameter(validfrom));
        _validto =util.setEmpty(proxy.getRequestParameter(validto));

        _isLocked = util.checkEmpty(proxy.getRequestParameter(locked));
        _lockedReason = util.checkEmpty(proxy.getRequestParameter(lockreason));
        _createdbegin = util.checkEmpty(proxy.getRequestParameter(createdbegin));
        _createdend = util.checkEmpty(proxy.getRequestParameter(createdend));
        _failedattempts = util.checkEmpty(proxy.getRequestParameter(failedattempts));
        _loggedinbegin = util.checkEmpty(proxy.getRequestParameter(loggedinbegin));
        _loggedinend = util.checkEmpty(proxy.getRequestParameter(loggedinend));
        _pwdchangebegin = util.checkEmpty(proxy.getRequestParameter(pwdchangebegin));
        _pwdchangeend = util.checkEmpty(proxy.getRequestParameter(pwdchangeend));
    } // UserAccountBean(HttpServletRequest, Locale)

    public void setLocale(Locale locale) {
        if ( null != locale ) {
            // System.out.println(locale.toString());
            Calendar calendar = Calendar.getInstance(locale);
            _dateU = new DateUtil(locale);
            // System.out.println(_dateU.getPattern());
            _validfrom = _dateU.formatInDisplayFormat(calendar.getTime());
            calendar.set(2500, 11, 31);
            _validto = _dateU.formatInDisplayFormat(DateUtil.decDateWhenMidnight(calendar.getTime()));
        }
    } // setLocale(Locale)

    public String getLogonUid() {
        return (this.toFilter==true)?util.filteringSpecialChar(this._logonuid):this._logonuid;
    } // getLongUid

    public void setLogonUid(String id) {
        _logonuid = id;
    } // setLogonUid

    public boolean isSystemGeneratedPassword() {
        return _isSystemGeneratedPassword;
    } // isSystemGeneratedPassword

    public void setSystemGeneratePassword(boolean letSystemGenerate) {
        _isSystemGeneratedPassword = letSystemGenerate;
        _password = com.sap.security.core.util.SecurityUtils.GeneratePassword(UMFactory.getSecurityPolicy());
        _passwordconfirm = _password;
    } // setSystemGeneratePassword

    public String getOldPassword() {
        return (this.toFilter==true)?util.filteringSpecialChar(this._oldpassword):this._oldpassword;
    } // getOldPassword

    public String getPassword() {
        return (this.toFilter==true)?util.filteringSpecialChar(this._password):this._password;
    } // getPassword

    public String getPasswordConfirm() {
        return (this.toFilter==true)?util.filteringSpecialChar(this._passwordconfirm):this._passwordconfirm;
    } // getPasswordConfirm

    public String getCreatedbegin() {
        return (this.toFilter==true)?util.filteringSpecialChar(util.checkNull(this._createdbegin)):util.checkNull(this._createdbegin);
    } // getCreatedbegin

    public String getCreatedend() {
        return (this.toFilter==true)?util.filteringSpecialChar(util.checkNull(this._createdend)):util.checkNull(this._createdend);
    } // getCreatedEnd

    public String getLoggedinbegin() {
        return (this.toFilter==true)?util.filteringSpecialChar(util.checkNull(this._loggedinbegin)):util.checkNull(this._loggedinbegin);
    } // getLoggedinbegin

    public String getLoggedinend() {
        return (this.toFilter==true)?util.filteringSpecialChar(util.checkNull(this._loggedinend)):util.checkNull(this._loggedinend);
    } // getLoggedinend

    public String getPwdchangebegin() {
        return (this.toFilter==true)?util.filteringSpecialChar(util.checkNull(this._pwdchangebegin)):util.checkNull(this._pwdchangebegin);
    } // getPwdchangebegin

    public String getPwdchangeend() {
        return (this.toFilter==true)?util.filteringSpecialChar(util.checkNull(this._pwdchangeend)):util.checkNull(this._pwdchangeend);
    } // getPwdchangeend

    public String getValidFrom() {
        return (this.toFilter==true)?util.filteringSpecialChar(this._validfrom):this._validfrom;
    } // getValidFrom

    public String getValidTo() {
        return (this.toFilter==true)?util.filteringSpecialChar(this._validto):this._validto;
    } // getValidTo

    public Boolean isLocked() {
        return ((_isLocked==null)?null:new Boolean(_isLocked));
    } // isLocaked()

    public Integer getLockReason() {
        return (super.getLockReason()==null)?new Integer(0):super.getLockReason();
    } // getLockReason

    public IUserAccount getUserAccount() {
        return _userAccount;
    } // getUserAccount

    public String getSearchCriteria() {
        return (this.toFilter==true)?util.filteringSpecialChar(_criteria.toString()):_criteria.toString();
    } // getSearchCriteria

    protected boolean isSet() {
        return _isSet;
    } // isSet()

    protected void setPasswordToEmpty() {
        _password = util.empty;
        _passwordconfirm = util.empty;
    } // setPasswordToEmpty

    protected void setMaxCount(String max) {
        if ( null == max ) {
            _maxcount = -1;
        } else {
            _maxcount = strToInt(max);
        }
    } // setMaxCount(String)

    protected int getMaxCount() {
        return _maxcount;
    } // getMaxCount

    protected void setMaxCount(int max) {
        _maxcount = max;
    } // setMaxCount(int)

    protected ErrorBean createUserAccount(String uniqueID, boolean password_change_rq) {
        final String methodName = "createUserAccount";
        IUserAccount ua = null;
        try { 	
                ua = UMFactory.getUserAccountFactory().newUserAccount(_logonuid, uniqueID);
			
			ua.setPassword(getPassword());
			ua.setLocked(false, IUserAccount.LOCKED_NO);

			// set valid-from and valid-to
			if (null != _validfrom)
				ua.setValidFromDate((util.checkEmpty(_validfrom)==null)?getDefaultValidFrom():strToDate(_validfrom));
			if (null != _validto)
				ua.setValidToDate((util.checkEmpty(_validto)==null)?getDefaultValidTo():DateUtil.incDate(strToDate(_validto)));

  			    ua.save();
			    ua.commit();
        } catch (UserAccountAlreadyExistsException ex) {
            trace.errorT(methodName, "get new User Account failed", ex);
            return new ErrorBean(new Message(UserAdminMessagesBean.USERACCOUNT_ALREADY_EXIST));
        } catch (InvalidPasswordException ex) {
            trace.errorT(methodName, "User Account create failed:" + ex.getMessage() , ex);
            if ( ex.getMessage().equalsIgnoreCase(SecurityPolicy.USERID_CONTAINED_IN_PASSWORD))
                return new ErrorBean(new Message(UserAdminMessagesBean.USERID_CONTAINED_IN_PASSWORD));
            else
                return new ErrorBean(new Message(UserAdminMessagesBean.INVALID_PASSWORD));
        } catch (UMException ex) {
            if ( null != ua  ) ua.rollback();
            trace.errorT(methodName, "User Account create failed:" + ex.getMessage(), ex);
            return new ErrorBean(new Message(UserAdminMessagesBean.USERACCOUNT_CREATE_FAILED));
        }
        		
		try {
			ua = UMFactory.getUserAccountFactory().getMutableUserAccount(ua.getUniqueID());
			ua.setPasswordChangeRequired(password_change_rq);
			ua.save();
			ua.commit();
		} catch ( UMException ex) {
			ua.rollback();
			trace.infoT(methodName, "can't set passwordchangerequired attribute", ex);
		}
		
        return null;
    } // createUser

    protected ErrorBean checkUserAccount(boolean isNew, Locale locale) {
        ErrorBean error = null;
        if ( isNew ) error = checkLogonUid(locale);
        if ( null != error ) {
            return error;
        } else {
            return checkPassword(isNew, locale);
        }
    } // checkUserAccount

    protected ErrorBean checkLogonUid(Locale locale) {
        UserAdminLocaleBean localeBean = UserAdminLocaleBean.getInstance(locale);
        if ( null != _logonuid ) {
            if ( util.empty.equals(_logonuid) ) {
                StringBuffer sb = new StringBuffer("\"");
                sb.append(localeBean.get("USER_ID")).append("\"");
                return new ErrorBean(new Message(UserAdminMessagesBean.MISSING_FIELD, new Object[]{sb.toString(), localeBean.get("IS_MISSING")}));
            } else {
				ISecurityPolicy isp = UMFactory.getSecurityPolicy();
				try {
					if ( isp.isLogonIdValid(_logonuid) )
						return null;
					else
						return new ErrorBean(new Message(UserAdminMessagesBean.INVALID_UID, 
							new Integer[]{new Integer(isp.getLogonIdMinLength()), new Integer(isp.getLogonIdMaxLength()),
							new Integer(isp.getLogonIdNumericDigitsRequired()), new Integer(isp.getLogonIdLowerCaseRequired()),
							new Integer(isp.getLogonIdSpecialCharRequired())}));					
				} catch (InvalidLogonIdException ex) {
					if ( trace.beDebug() ) trace.debugT("checkLogonUId", ex.getMessage());
					return new ErrorBean(new Message(UserAdminMessagesBean.INVALID_UID, 
						new Integer[]{new Integer(isp.getLogonIdMinLength()), new Integer(isp.getLogonIdMaxLength()),
						new Integer(isp.getLogonIdNumericDigitsRequired()), new Integer(isp.getLogonIdLowerCaseRequired()),
						new Integer(isp.getLogonIdSpecialCharRequired())}));
				} 
            }
        }
        return null;
    } // checkLogonUid

    protected ErrorBean checkPassword(boolean isNew, Locale locale) {
        final String methodName = "checkPassword";
        UserAdminLocaleBean localeBean = UserAdminLocaleBean.getInstance(locale);
        ErrorBean error = null;
        StringBuffer mf = new StringBuffer();
        boolean isMulti = false;

        if ( toCheckPswd(isNew) || toCheckOldPswd() ) {
            if ( toCheckPswd(isNew) && util.empty.equals(_oldpassword) ) {
                if ( mf.length() > 0 ) {
                    mf.append(", ");
                    isMulti = true;
                }
                mf.append(localeBean.get("OLD_PASSWORD"));
            }

            if ( null == util.checkEmpty(_password) ) {
                if ( mf.length() > 0 ) {
                    mf.append(", ");
                    isMulti = true;
                }
                mf.append(localeBean.get("PASSWORD"));
            }

            if ( null == util.checkEmpty(_passwordconfirm) ) {
                if ( mf.length() > 0 ) {
                    mf.append(", ");
                    isMulti = true;
                }
                mf.append(localeBean.get("PASSWORD_CONFIRM"));
            }
        }

        if ( mf.length() > 0 ) {
            String msgId = UserAdminMessagesBean.MISSING_FIELD_MSG;
            String first = localeBean.get("MISSING_FIELD");
            if ( isMulti ) {
                first = localeBean.get("MISSING_FIELDS");
            }
            Object[] args = {first, new String(mf)};
            Message msg = new Message(msgId, args);
            return new ErrorBean(msg);
        }

        if ( toCheckOldPswd() ) {
            error = checkOldPassword(_userAccount, _oldpassword);
            if ( null != error )
                return error;
        }

        if ( toCheckPswd(isNew) ) {
            if ( !_password.equals(_passwordconfirm) ) {
                trace.debugT(methodName, "password and password confirm don't match");
                return new ErrorBean(new Message(UserAdminMessagesBean.MISSING_PASSWORDS_MISMATCH));
            }

            ISecurityPolicy sp = UMFactory.getSecurityPolicy();
            try {
                sp.isPasswordValid(_password, this.getLogonUid());
            } catch (InvalidPasswordException e) {
                trace.errorT(methodName, e.getMessage(), e );
                if (e.getMessage().equalsIgnoreCase(SecurityPolicy.CHANGE_PASSWORD_NOT_ALLOWED)) {
                    if ( isNew ) return error;
                    else return new ErrorBean(new Message(UserAdminMessagesBean.CHANGE_PASSWORD_NOT_ALLOWED));
                } else if (e.getMessage().equalsIgnoreCase(SecurityPolicy.PASSWORD_TOO_SHORT)){
                    return new ErrorBean(new Message(UserAdminMessagesBean.PASSWORD_TOO_SHORT,
                                        new Integer(sp.getPasswordMinLength())));
                 } else if (e.getMessage().equalsIgnoreCase(SecurityPolicy.PASSWORD_TOO_LONG)) {
                    return new ErrorBean(new Message(UserAdminMessagesBean.PASSWORD_TOO_LONG,
                                        new Integer(sp.getPasswordMaxLength())));
                 } else if (e.getMessage().equalsIgnoreCase(SecurityPolicy.ALPHANUM_REQUIRED_FOR_PSWD)) {
                    return new ErrorBean(new Message(UserAdminMessagesBean.ALPHANUM_REQUIRED_FOR_PASSWORD,
                                        new Integer(sp.getPasswordAlphaNumericRequired())));
                } else if (e.getMessage().equalsIgnoreCase(SecurityPolicy.MIXED_CASE_REQUIRED_FOR_PSWD)) {
                    return new ErrorBean(new Message(UserAdminMessagesBean.MIXED_CASE_REQUIRED_FOR_PASSWORD,
                                        new Integer(sp.getPasswordMixCaseRequired())));
                } else if (e.getMessage().equalsIgnoreCase(SecurityPolicy.SPEC_CHARS_REQUIRED_FOR_PSWD)) {
                    return new ErrorBean(new Message(UserAdminMessagesBean.SPEC_CHARS_REQUIRED_FOR_PASSWORD,
                                        new Integer(sp.getPasswordSpecialCharRequired())));
                } else if (e.getMessage().equalsIgnoreCase(SecurityPolicy.USERID_CONTAINED_IN_PASSWORD)) {
                    return new ErrorBean(new Message(UserAdminMessagesBean.USERID_CONTAINED_IN_PASSWORD));
                } else {
                    return new ErrorBean(new Message(e.getMessage()));
                }
            }
        }
        return error;
    } // checkPassword

    protected ErrorBean checkOldPassword(IUserAccount ua, String oldPswd) {
        if ( !ua.checkPassword(oldPswd) ) {
            return new ErrorBean(new Message(UserAdminMessagesBean.WRONG_OLD_PASSWORD));
        } else {
            return null;
        }
    } // checkOldPassword

    protected ErrorBean checkDateFields() {
        final String methodName = "checkDateFields";
        trace.entering(methodName);

        this.isMulti = false;
        this.sbf = new StringBuffer();

        // must match yyyy?1..2?1..2 or 1..2?1..2?yyyy

        checkDateField(_validfrom, "VALID_FROM");
        checkDateField(_validto, "VALID_TO");
        checkDateField(_createdbegin, "CREATION_DATE");
        checkDateField(_createdend, "CREATION_DATE");
        checkDateField(_loggedinbegin, "LAST_LOGIN_DATE");
        checkDateField(_loggedinend, "LAST_LOGIN_DATE");
        checkDateField(_pwdchangebegin, "DATE_OF_LAST_PSWD_CHANGE");
        checkDateField(_pwdchangeend, "DATE_OF_LAST_PSWD_CHANGE");

        if ( this.sbf.length() > 0 ) {
            this.sbf.append(" - ");
            Object[] args = {this.sbf.toString(), _dateU.getPattern()};
            return new ErrorBean(new Message(UserAdminMessagesBean.INVALID_DATE, args));
        } else {
            return checkBetweenDateFields();
            /**@todo check past dates*/
            /*error = checkBetweenDateFields();
            if ( null != error ) {
                return error;
            } else {
                if ( null == this._userAccount) return null;
                else return checkMustNotHappenInPastFields();
            }*/
        }
    } // checkDateFields

    protected void setUserAccountSearchFilter() {
        Date begin = null;
        Date end = null;
        if ( null == this.localeBean ) localeBean = UserAdminLocaleBean.getInstance(this._locale);

        if ( null != util.checkEmpty(_logonuid) ) {
            if ( !"*".equals(_logonuid) ) {
                super.setLogonUid(_logonuid, util.getOperator(_logonuid), false);
                _isSet = true;
            }
            _criteria.append("\"").append(localeBean.get("USER_ID")).append("\"");
            _criteria.append("=");
            _criteria.append("\"").append(_logonuid).append("\"");
        }

        if ( null != util.checkEmpty(_isLocked) ) {
            super.setLocked(new Boolean(_isLocked).booleanValue());
            trace.debugT("UserAccountBean", "super.isLocked", new Boolean[]{super.isLocked()});
            _isSet = true;
            if ( _criteria.length() > 0 ) {
                _criteria.append(", ");
            }
            _criteria.append("\"").append(localeBean.get("STATUS")).append("\"");
            _criteria.append("=");
            _criteria.append("\"").append(super.isLocked().booleanValue()==true?localeBean.get("DEACTIVATED"):localeBean.get("ACTIVE")).append("\"");
            if ( (null!=util.checkEmpty(_lockedReason)) && super.isLocked().booleanValue() ) {
                super.setLockReason(Integer.parseInt(_lockedReason));
                String theReason = "EITHER";
                if ( IUserAccount.LOCKED_AUTO == super.getLockReason().intValue() ) {
                    theReason = "SYSTEM";
                } else if ( IUserAccount.LOCKED_BY_ADMIN == super.getLockReason().intValue() ) {
                    theReason = "ADMINISTRATOR";
                }
                _isSet = true;
                if ( _criteria.length() > 0 ) {
                    _criteria.append(", ");
                }
                _criteria.append("\"").append(localeBean.get("SET_BY")).append("\"");
                _criteria.append("=");
                _criteria.append("\"").append(localeBean.get(theReason)).append("\"");
            }
        }

        if ( null != util.checkEmpty(_failedattempts) ) {
            super.setFailedLogonAttempts(strToInt(_failedattempts));
            _isSet = true;
        }

        if ( null != util.checkEmpty(_createdbegin) ) begin = strToDate(_createdbegin);
        if ( null != util.checkEmpty(_createdend) ) end = strToDate(_createdend);
        if ( null != _createdbegin || null != _createdend ) {
            if ( null != end )
            	end = getNextDay(end);
            else
            	end = new Date();

            if (null == begin) begin = goBackToFities();
            trace.debugT("UserAccountBean", "super.setCreateBetweenDates", new String[]{(begin==null)? "null":begin.toString(), (end==null)? "null":end.toString()});
            super.setCreateBetween(begin, end);
            _isSet = true;
            setDateCriteria(localeBean, "CREATED", _createdbegin, _createdend);
        }

        if ( null != util.checkEmpty(_loggedinbegin) ) begin = strToDate(_loggedinbegin);
        if ( null != util.checkEmpty(_loggedinend) ) end = strToDate(_loggedinend);
        if (  null != _loggedinbegin || null != _loggedinend ) {
            if ( null != end )
				end = getNextDay(end);
			else
				end = new Date();

            if (null == begin) begin = goBackToFities();
            trace.debugT("UserAccountBean", "super.setLoggedInBetweenDates", new String[]{(begin==null)? "null":begin.toString(), (end==null)? "null":end.toString()});
            super.setLoggedInBetween(begin, end);
            _isSet = true;
            setDateCriteria(localeBean, "LAST_LOGIN_DATE", _loggedinbegin, _loggedinend);
        }

        if ( null != util.checkEmpty(_pwdchangebegin) ) begin = strToDate(_pwdchangebegin);
        if ( null != util.checkEmpty(_pwdchangeend) ) end = strToDate(_pwdchangeend);
        if ( null != _pwdchangebegin || null != _pwdchangeend ) {
            if ( null != end )
				end = getNextDay(end);
			else
				end = new Date();

            if (null == begin) begin = goBackToFities();
            trace.debugT("UserAccountBean", "super.setPasswordChangeBetweenDate1", new String[]{(begin==null)? "null":begin.toString(), (end==null)? "null":end.toString()});
            super.setPasswordChangeBetween(begin, end);
            _isSet = true;
            setDateCriteria(localeBean, "DATE_OF_LAST_PSWD_CHANGE", _pwdchangebegin, _pwdchangeend);
        }
    } // toSetValueInUserAccountSearchFilter

    private UserAccountBean(IUser user, IUserAccount ua, Locale locale) {
        IUser userp = user;

        if ( null != userp ) {
            try {
                IUserAccount[] uas = userp.getUserAccounts();
                if ( uas.length > 0 ) {
                	this._userAccount = uas[0];
                } else {
                	this._userAccount = null;
                } 
            } catch (UMException ex) {
                this._userAccount = null;
            }
        }

        if ( null != ua ) {
            this._userAccount = ua;
            try {
                userp = ua.getAssignedUser();
            } catch (UMException ex) {
                userp = null;
            }
        }

        if ( null != locale ) {
            this._locale = locale;
        } else  if ( null != userp ) {
            if ( null != userp.getLocale() )
                this._locale = userp.getLocale();
        }

        _dateU = new DateUtil(this._locale);
        if ( null != this._userAccount ) {
            this._logonuid = _userAccount.getLogonUid();

            if ( util.checkEmpty(_userAccount.getValidFromDate())==null ) {
                this._validfrom = util.empty;
            } else {
                this._validfrom = _dateU.formatInDisplayFormat(_userAccount.getValidFromDate());
            }
            if ( util.checkEmpty(_userAccount.getValidToDate())==null ) {
                this._validto = util.empty;
            } else {
				this._validto = _dateU.formatInDisplayFormat(DateUtil.decDateWhenMidnight(_userAccount.getValidToDate()));
            }
        } else {
            this._userAccount = null;
            this._logonuid = null;
            this._validfrom = util.empty;
            this._validto = util.empty;
        }
        _oldpassword = util.empty;
        _password = util.empty;
        _passwordconfirm = util.empty;
    } // UserAccountBean(IUser user, IUserAccount ua, Locale locale)

    private boolean toCheckPswd(boolean isNew) {
        return isSystemGeneratedPassword()==true?false:(isNew == true?true:((util.checkEmpty(_password) == null)?false:true));
    } // toCheckPswd

    private boolean toCheckOldPswd() {
        return util.checkEmpty(_oldpassword) == null ? false : true;
    } // toCheckOldPswd

    private Date strToDate(String s) {
        return _dateU.strToDate(s);
    } // StrToDate

    private int strToInt(String str) {
        return new Integer(str).intValue();
    } // strToInt

    private void checkDateField(String value, String label) {
        if ( null == this.localeBean ) localeBean = UserAdminLocaleBean.getInstance(this._locale);
        
        if ( !_dateU.isDateValid(value) ) {
            if ( this.sbf.length() > 0 ) {
                sbf.append(", ");
                this.isMulti = true;
            }
            sbf.append("\"").append(localeBean.get(label)).append("\"");
        }
    } // checkDateField

    private ErrorBean checkBetweenDateFields() {
        final String methodName = "checkBetweenDateFields()";
        trace.entering(methodName);

        this.isMulti = false;
        this.sbf = new StringBuffer();

        checkBetweenDateField(_validfrom, _validto, "VALID_FROM");
        checkBetweenDateField(_createdbegin, _createdend, "CREATION_DATE");
        checkBetweenDateField(_loggedinbegin, _loggedinend, "LAST_LOGIN_DATE");
        checkBetweenDateField(_pwdchangebegin, _pwdchangeend, "DATE_OF_LAST_PSWD_CHANGE");

        if ( this.sbf.length() > 0 ) {
            this.sbf.append(" - ");
            Object[] args = {this.sbf.toString()};
            if ( this.isMulti )
                return new ErrorBean(new Message(UserAdminMessagesBean.ENDDATES_AFTER_STARTDATES, args));
            else
                return new ErrorBean(new Message(UserAdminMessagesBean.ENDDATE_AFTER_STARTDATE, args));
        } else {
            return null;
        }
    } // checkBetweenDateFields()

    private void checkBetweenDateField(String start, String end, String label) {
        if ( null == this.localeBean ) localeBean = UserAdminLocaleBean.getInstance(this._locale);
        if ( null != util.checkEmpty(start) && null != util.checkEmpty(end)) {
            if ( strToDate(start).after(strToDate(end)) ) {
                if ( this.sbf.length() > 0 ) {
                    sbf.append(", ");
                    this.isMulti = true;
                }
                sbf.append("\"").append(localeBean.get(label)).append("\"");
            }
        }
    } // checkBetweenDateField

    private Date getNextDay(Date date) {
        Date nextDay = date;
        long time = nextDay.getTime();
        long oneDayInMilliSeconds = 1*24*60*60*1000;
        nextDay.setTime(time+oneDayInMilliSeconds);
        return nextDay;
    } // getNextDay

    private Date getDefaultValidFrom() {
        Calendar calendar = Calendar.getInstance(this._locale);
        return calendar.getTime();
    } // getDefaultValidFrom

    private Date getDefaultValidTo() {
        Calendar calendar = Calendar.getInstance(this._locale);
        calendar.set(2500, 11, 31);
        return calendar.getTime();
    } // getDefaultValidTo

    private Date goBackToFities() {
        Calendar calendar = Calendar.getInstance(this._locale);
        calendar.set(1950, 01, 01);
        return calendar.getTime();
    } // goBackToOneHundredYear

    private void setDateCriteria(UserAdminLocaleBean localeBean, String dateField, String begin, String end) {
        if ( _criteria.length() > 0 ) {
            _criteria.append(", ");
        }
        if ( null != begin || null != end ) {
            _criteria.append("\"").append(localeBean.get(dateField)).append("\"");
            if ( null != begin ) {
                if ( null != end ) {
                    _criteria.append(" ").append(localeBean.get("BETWEEN"));
                    _criteria.append("\"").append(begin).append("\"");
                    _criteria.append(" ").append(localeBean.get("AND")).append(" ");
                    _criteria.append("\"").append(end).append("\"");
                } else {
                    _criteria.append(" ").append(localeBean.get("SINCE"));
                    _criteria.append(" ");
                    _criteria.append("\"").append(begin).append("\"");
                }
            } else {
                if ( null != end ) {
                    _criteria.append(" ").append(localeBean.get("TILL"));
                    _criteria.append(" ");
                    _criteria.append("\"").append(end).append("\"");
                }
            }
        }
    } // setDateCriteria
    
	public void setFilterFlag(boolean toPerformFiltering) {
		this.toFilter = toPerformFiltering;
	} // setFilterFlag    
}

