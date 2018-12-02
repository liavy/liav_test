package com.sap.security.core.admin;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

import com.sap.security.api.AttributeList;
import com.sap.security.api.IPrincipal;
import com.sap.security.api.IUser;
import com.sap.security.api.IUserMaint;
import com.sap.security.api.UMException;
import com.sap.security.api.UMFactory;
import com.sap.security.api.UserAlreadyExistsException;
import com.sap.security.core.InternalUMFactory;
import com.sap.security.core.imp.User;
import com.sap.security.core.imp.UserSearchFilter;
import com.sap.security.core.util.BeanException;
import com.sap.security.core.util.ErrorBean;
import com.sap.security.core.util.IUMTrace;
import com.sap.security.core.util.LocaleString;
import com.sap.security.core.util.Message;
import com.sapmarkets.tpd.TradingPartnerDirectoryCommon;
import com.sapmarkets.tpd.master.PartnerID;
import com.sapmarkets.tpd.master.TradingPartnerDirectoryInterface;
import com.sapmarkets.tpd.master.TradingPartnerInterface;
import com.sapmarkets.tpd.util.TpdException;

public class UserBean extends UserSearchFilter {
    public static final String beanId = "user";

    public final static String  VERSIONSTRING = "$Id: //engine/js.ume.useradmin.app/dev/src/_tc~sec~ume~useradmin/webm/core/com/sap/security/core/admin/UserBean.java#1 $ from $DateTime: 2008/09/17 17:08:55 $ ($Change: 217715 $)";
    private final static IUMTrace trace = InternalUMFactory.getTrace(VERSIONSTRING);

    public static final String UM = IPrincipal.DEFAULT_NAMESPACE; // default NameSpace
    public static final String UM_ADDATTRS = "UM_ADDATTRS"; // for customized fields
    public static final String UUCOMPANYID = "APPROVAL_REQUEST_COMPANYID";
    public static final String uidId = "uniqueID";
    public static final String displayNameId = User.DISPLAYNAME;
    public static final String firstNameId = User.FIRSTNAME;
    public static final String lastNameId = User.LASTNAME;
    public static final String salutationId = User.SALUTATION;
    public static final String titleId = User.TITLE;
    public static final String preferredLanguageId = "preferredlanguage";
    public static final String localeId = User.LOCALE;
    public static final String timeZoneId = User.TIMEZONE;
    public static final String currencyId = User.CURRENCY;
    public static final String emailId = User.EMAIL;
    public static final String telephoneId = User.TELEPHONE;
    public static final String faxId = User.FAX;
    public static final String mobileId = User.MOBILE;
    public static final String zipId = User.ZIP;
    public static final String cityId = User.CITY;
    public static final String stateId = User.STATE;
    public static final String countryId = User.COUNTRY;
    public static final String streetAddressId = User.STREETADDRESS;
    public static final String positionId = User.JOBTITLE;
    public static final String departmentId = User.DEPARTMENT;
    public static final String companyId = User.COMPANY;
    public static final String accessibilitylevelId = User.ACCESSIBILITYLEVEL;
    
    // additional attributes for EBP
    public static final String orgUnitId = "ORGUNIT";
    public static final String regId = "regId";
    // used for self-registration/approval
    public final static String noteToAdmin = "NOTE_TO_ADMINISTRATOR";
    public final static String messageToRequestor = "messageToRequestor";
    // lock
    public final static String lockReason = "lockReason";
    public final static String lockPerson = "lockperson";
    public final static String lockMessage = "lockmessage";
    // unlock
    public final static String unlockPerson = "unlockperson";
    public final static String unlockMessage = "unlockmessage";
    public final static String unlockDate = "unlockdate";
    // pswd expire
    public final static String expirePerson = "expireperson";
    public final static String expireMessage = "expirekmessage";
    public final static String expireDate = "expiredate";
    // company selection
    public static final String companyModeId = "companyMode";
    public static final String allUsersMode = "allMode";
    public static final String individualUsersMode = "individualMode";
    public static final String companyUsersMode = "companyMode";

    // search options
    public static final String maxNumberId = "maxNumber";

    public final static String UM_EMAIL_NOTIFICATION = "um.admin.email.notification";//"UM_EMAIL_NOTIFICATION"

    /** @todo public static final int displayname_len */
    public static final int firstname_len = 30;
    public static final int lastname_len = 50;
    public static final int salutation_len = 30;
    public static final int title_len = 30;
    public static final int preferredLanguage_len = 30;
    public static final int timeZone_len = 30;
    public static final int lang_len = 30;
    public static final int timezone_len = 30;
    public static final int telephone_len = 40;
    public static final int fax_len = 40;
    public static final int mobile_len = 40;
    public static final int email_len = 70;
    // public static final int companyid = 50;
    public static final int currency_len = 20;
    // public static final int langCountry_len = 30;
    // public static final int location_len = 50;
    public static final int zip_len = 15;
    public static final int city_len = 30;
    public static final int state_len = 20;
    public static final int country_len = 30;
    public static final int streetAddress_len = 60;
    public static final int position_len = 40;
    public static final int department_len = 50;
	
    protected static final String UMEXCEPTION_GENERAL = "generalUMException";
    protected static final String UMEXCEPTION_USERALREADYEXIST = "userAlreadyExistException";
    
	private final static AttributeList populatedAttributesList;
    private IUserMaint _user = null;
    private UserAdminCustomization uac = null;

    private boolean orgReq = false;
    private Boolean isApproved = null;
    private boolean isModified = false;

    private String uid;
    private String displayname;
    private String firstname;
    private String lastname;
    private String salutation;
    private String title;
    private String language;
    private String timezone;
    private String currency;
    private String email;
    private String telephone;
    private String fax;
    private String mobile;
    private String zip;
    private String city;
    private String state;
    private String country;
    private String street;
    private String position;
    private String department;
    private int accessibilitylevel; // accessibilitylevel
    private String _orgUnit;
    private String _regId;
    private String _company; // company id
    private String _companyName; // company display name
    private String companyMode; // company search mode
    private boolean toFilter = true;

    private Locale _locale = null;
    private UserAdminLocaleBean _localeBean = null;

    /* email notification*/
    private String _noteToAdmin;
    private String _messageToRequestor;

    private String orderBy[];

    // additional attributes
    private Hashtable _addAttris = new Hashtable();

    private boolean _isSet = false;
    private StringBuffer _criteria = new StringBuffer();

  	static {
		populatedAttributesList = new AttributeList();
		populatedAttributesList.addAttribute(UM, User.DISPLAYNAME, AttributeList.TYPE_STRING); // 0
		populatedAttributesList.addAttribute(UM, User.FIRSTNAME, AttributeList.TYPE_STRING); // 1
		populatedAttributesList.addAttribute(UM, User.LASTNAME, AttributeList.TYPE_STRING); // 2
		populatedAttributesList.addAttribute(UM, User.SALUTATION, AttributeList.TYPE_STRING); // 2
		populatedAttributesList.addAttribute(UM, User.TITLE, AttributeList.TYPE_STRING); // 4
		populatedAttributesList.addAttribute(UM, User.LOCALE, AttributeList.TYPE_UNKNOWN); // 5
		populatedAttributesList.addAttribute(UM, User.TIMEZONE, AttributeList.TYPE_UNKNOWN); // 6
		populatedAttributesList.addAttribute(UM, User.CURRENCY, AttributeList.TYPE_STRING); // 7
		populatedAttributesList.addAttribute(UM, User.EMAIL, AttributeList.TYPE_STRING); // 8
		populatedAttributesList.addAttribute(UM, User.TELEPHONE, AttributeList.TYPE_STRING); // 9
		populatedAttributesList.addAttribute(UM, User.FAX, AttributeList.TYPE_STRING); // A
		populatedAttributesList.addAttribute(UM, User.MOBILE, AttributeList.TYPE_STRING); // B
		populatedAttributesList.addAttribute(UM, User.ZIP, AttributeList.TYPE_STRING); // C
		populatedAttributesList.addAttribute(UM, User.CITY, AttributeList.TYPE_STRING); // D
		populatedAttributesList.addAttribute(UM, User.STATE, AttributeList.TYPE_STRING); // E
		populatedAttributesList.addAttribute(UM, User.STREETADDRESS, AttributeList.TYPE_STRING); // F
		populatedAttributesList.addAttribute(UM, User.JOBTITLE, AttributeList.TYPE_STRING); // 10
		populatedAttributesList.addAttribute(UM, User.DEPARTMENT, AttributeList.TYPE_STRING); // 11
		populatedAttributesList.addAttribute(UM, User.ACCESSIBILITYLEVEL, AttributeList.TYPE_STRING); // 12    	
    }
    
    public static int getFirstNameMaxLength() {
        return getFieldMaxLength(firstNameId);
    } // getFirstNameDefaultLength

    public static int getLastNameMaxLength() {
        return getFieldMaxLength(lastNameId);
    } // getLastNameDefaultLength

    public static int getSalutationMaxLength() {
        return getFieldMaxLength(salutationId);
    } // getSalutationDefaultLength

    public static int getTitleMaxLength() {
        return getFieldMaxLength(titleId);
    } // getTitleMaxLength

    public static int getPreferredLanguageMaxLength() {
        return getFieldMaxLength(preferredLanguageId);
    } // getPerferrredLanguageLength

    public static int getTimeZoneMaxLength() {
        return getFieldMaxLength(timeZoneId);
    } // getTimeZoneMaxLength

    public static int getCurrencyMaxLength() {
        return getFieldMaxLength(currencyId);
    } // getCurrencyMaxLength

    public static int getEmailMaxLength() {
        return getFieldMaxLength(emailId);
    } // getEmailMaxLength

    public static int getTelephoneMaxLength() {
        return getFieldMaxLength(telephoneId);
    } // getTelephoneMaxLength

    public static int getFaxMaxLength() {
        return getFieldMaxLength(faxId);
    } // getFaxMaxLength

    public static int getMobileMaxLength() {
        return getFieldMaxLength(mobileId);
    } // getMobileMaxLength

    public static int getZipMaxLength() {
        return getFieldMaxLength(zipId);
    } // getZipMaxLength

    public static int getCityMaxLength() {
        return getFieldMaxLength(cityId);
    } // getCityMaxLength

    public static int getStateMaxLength() {
        return getFieldMaxLength(stateId);
    } // getStateMaxLength

    public static int getCountryMaxLength() {
        return getFieldMaxLength(countryId);
    } // getCountryMaxLength

    public static int getStreetAddressMaxLength() {
        return getFieldMaxLength(streetAddressId);
    } // getStreetAddressMaxLength

    public static int getPositionMaxLength() {
        return getFieldMaxLength(positionId);
    } // getPositionMaxLength

    public static int getDepartmentMaxLength() {
        return getFieldMaxLength(departmentId);
    } // getDepartmentMaxLength

    public UserBean() {
        /* initializing all user attributes
         * from persistent storage
         * for display purpose, if null set to empty string
         */
        uid        = util.empty;
        displayname = util.empty;
        firstname  = util.empty;
        lastname   = util.empty;
        salutation = util.empty;
        title      = util.empty;
        language   = util.empty;
        timezone   = util.empty;
        currency   = util.empty;
        email      = util.empty;
        telephone  = util.empty;
        fax        = util.empty;
        mobile     = util.empty;
        zip        = util.empty;
        city       = util.empty;
        state      = util.empty;
        country    = util.empty;
        street     = util.empty;
        position   = util.empty;
        department = util.empty;
        accessibilitylevel = IUser.DEFAULT_ACCESSIBILITY_LEVEL;
         _orgUnit    = util.empty;
         _noteToAdmin = util.empty;
    } // UserBean()

    public UserBean(IUser user) throws UMException {
        /* initializing all user attributes
         * from persistent storage
         * for display purpose, if null set to empty string
         */        
        uid        = util.checkNull(user.getUniqueID()); // UniqueId
        user = UMFactory.getUserFactory().getUser(uid, populatedAttributesList);
        displayname = util.checkNull(user.getDisplayName());
        firstname  = util.checkNull(user.getFirstName());
        lastname   = util.checkNull(user.getLastName());
        salutation = util.checkNull(user.getSalutation());
        title      = util.checkNull(user.getTitle());
        language   = (user.getLocale()==null)?"":user.getLocale().toString();
        timezone   = (user.getTimeZone()==null)?"":user.getTimeZone().getID();
        currency   = util.checkNull(user.getCurrency());
        email      = util.checkNull(user.getEmail());
        telephone  = util.checkNull(user.getTelephone());
        fax        = util.checkNull(user.getFax());
        mobile     = util.checkNull(user.getCellPhone());
        zip        = util.checkNull(user.getZip());
        city       = util.checkNull(user.getCity());
        state      = util.checkNull(user.getState());
        country    = util.checkNull(user.getCountry());
        street     = util.checkNull(user.getStreet());
        position   = util.checkNull(user.getJobTitle());
        department = util.checkNull(user.getDepartment());
        accessibilitylevel = user.getAccessibilityLevel();

        _company = user.getCompany();
		
        _orgUnit    = util.empty;
        String[] addAttri = user.getAttribute(UM, orgUnitId);
        if ( null != addAttri ) {
            _orgUnit = addAttri[0];
        }

        _noteToAdmin = util.empty;
        addAttri = user.getAttribute(UM, noteToAdmin);
        if ( null != addAttri ) {
            _noteToAdmin = addAttri[0];
        }

        // to retrieve all additional attribute names
        getAddAttri(user);
        // over
    } // UserBean(IUser user)

    public UserBean(IAccessToLogic proxy) {
        /* processing user input
         * user attribute display or change
         * when user attribute is empty means no input
         */
        uid		   = util.checkNull(proxy.getRequestParameter(uidId));
        displayname = util.checkNull(proxy.getRequestParameter(displayNameId));
        firstname  = util.checkNull(proxy.getRequestParameter(firstNameId));
        lastname   = util.checkNull(proxy.getRequestParameter(lastNameId));
        salutation = util.checkNull(proxy.getRequestParameter(salutationId));
        title      = util.checkNull(proxy.getRequestParameter(titleId));
        language   = util.checkNull(proxy.getRequestParameter(preferredLanguageId));
        timezone   = util.checkNull(proxy.getRequestParameter(timeZoneId));
        currency   = util.checkNull(proxy.getRequestParameter(currencyId));
        email      = util.checkNull(proxy.getRequestParameter(emailId));
        telephone  = util.checkNull(proxy.getRequestParameter(telephoneId));
        fax        = util.checkNull(proxy.getRequestParameter(faxId));
        mobile     = util.checkNull(proxy.getRequestParameter(mobileId));
        zip        = util.checkNull(proxy.getRequestParameter(zipId));
        city       = util.checkNull(proxy.getRequestParameter(cityId));
        state      = util.checkNull(proxy.getRequestParameter(stateId));
        country    = util.checkNull(proxy.getRequestParameter(countryId));
        street     = util.checkNull(proxy.getRequestParameter(streetAddressId));
        position   = util.checkNull(proxy.getRequestParameter(positionId));
        department = util.checkNull(proxy.getRequestParameter(departmentId));
        accessibilitylevel = (proxy.getRequestParameter(accessibilitylevelId)==null)?IUser.DEFAULT_ACCESSIBILITY_LEVEL:IUser.SCREENREADER_ACCESSIBILITY_LEVEL;

        // additioanal fields for EBP
        _orgUnit = proxy.getRequestParameter(orgUnitId);
        if ( null != _orgUnit ) orgReq = true;
        _orgUnit    = util.checkNull(_orgUnit);

        // additional field from SUS
        _regId      = util.checkNull(proxy.getRequestParameter(regId));

        // company
        // pass to companyselectbean
        companyMode = util.checkNull(proxy.getRequestParameter(companyModeId), allUsersMode);

        _noteToAdmin = util.checkNull(proxy.getRequestParameter(noteToAdmin));
        _messageToRequestor = util.checkNull(proxy.getRequestParameter(messageToRequestor));

        // to retrieve all additional attribute names
        getAddAttri(proxy, false);
    } // UserBean(IAccessToLogic proxy)

    public UserBean(IAccessToLogic proxy, boolean toSearch) {
        /* for search when true */
        if ( toSearch ) {
			if ( null == _localeBean ) {
				this._localeBean = (UserAdminLocaleBean) proxy.getSessionAttribute(UserAdminLocaleBean.beanId);
			}            	
            /* basic user info */
            displayname = util.checkEmpty(proxy.getRequestParameter(displayNameId));
            firstname  = util.checkEmpty(proxy.getRequestParameter(firstNameId));
            lastname   = util.checkEmpty(proxy.getRequestParameter(lastNameId));
            salutation = util.checkEmpty(proxy.getRequestParameter(salutationId));
            email      = util.checkEmpty(proxy.getRequestParameter(emailId));
            language   = util.checkEmpty(proxy.getRequestParameter(preferredLanguageId));
            title      = util.checkEmpty(proxy.getRequestParameter(titleId));
            // companySearchName get from companyselectbean
            /* contact info */
            timezone   = util.checkEmpty(proxy.getRequestParameter(timeZoneId));
            telephone  = util.checkEmpty(proxy.getRequestParameter(telephoneId));
            fax        = util.checkEmpty(proxy.getRequestParameter(faxId));
            street     = util.checkEmpty(proxy.getRequestParameter(streetAddressId));
            city       = util.checkEmpty(proxy.getRequestParameter(cityId));
            state      = util.checkEmpty(proxy.getRequestParameter(stateId));
            mobile     = util.checkEmpty(proxy.getRequestParameter(mobileId));
            zip        = util.checkEmpty(proxy.getRequestParameter(zipId));
            country    = util.checkEmpty(proxy.getRequestParameter(countryId));

            /* additional information*/
            position   = util.checkEmpty(proxy.getRequestParameter(positionId));
            department = util.checkEmpty(proxy.getRequestParameter(departmentId));
            currency   = util.checkEmpty(proxy.getRequestParameter(currencyId));

            companyMode = util.checkNull(proxy.getRequestParameter(companyModeId), allUsersMode);

            // to retrieve all additional attribute names
            getAddAttri(proxy, true);

            setUserSearchFilter(proxy);
        } else {
            uid		   = proxy.getRequestParameter(uidId);
            displayname = proxy.getRequestParameter(displayNameId);
            firstname  = proxy.getRequestParameter(firstNameId);
            lastname   = proxy.getRequestParameter(lastNameId);
            salutation = proxy.getRequestParameter(salutationId);
            title      = proxy.getRequestParameter(titleId);
            language   = proxy.getRequestParameter(preferredLanguageId);
            timezone   = proxy.getRequestParameter(timeZoneId);
            currency   = proxy.getRequestParameter(currencyId);
            email      = proxy.getRequestParameter(emailId);
            telephone  = proxy.getRequestParameter(telephoneId);
            fax        = proxy.getRequestParameter(faxId);
            mobile     = proxy.getRequestParameter(mobileId);
            zip        = proxy.getRequestParameter(zipId);
            city       = proxy.getRequestParameter(cityId);
            state      = proxy.getRequestParameter(stateId);
            country    = proxy.getRequestParameter(countryId);
            street     = proxy.getRequestParameter(streetAddressId);
            position   = proxy.getRequestParameter(positionId);
            
            department = proxy.getRequestParameter(departmentId);
            accessibilitylevel = (proxy.getRequestParameter(accessibilitylevelId)==null)?IUser.DEFAULT_ACCESSIBILITY_LEVEL:IUser.SCREENREADER_ACCESSIBILITY_LEVEL;

            // additioanal fields for EBP
            _orgUnit = proxy.getRequestParameter(orgUnitId);
            // System.out.println((_orgUnit==null)?"null":"not null");
            if ( null != _orgUnit ) orgReq = true;

            // additional field from SUS
            _regId      = proxy.getRequestParameter(regId);

            // company
            // pass to companyselectbean
            companyMode = util.checkNull(proxy.getRequestParameter(companyModeId), allUsersMode);

            _noteToAdmin = proxy.getRequestParameter(noteToAdmin);
            _messageToRequestor = proxy.getRequestParameter(messageToRequestor);

            // to retrieve all additional attribute names
            getAddAttri(proxy, false);
        }
    } // UserBean(IAccessToLogic proxy, boolean toSearch)

    public void modifyUser(IUserMaint user) {
        String methodName = "modifyUser";
        _user = user;
        trace.entering(methodName, new String[]{user.getDisplayName()});

        /* only when attribute is changed then set*/

        // instead of setPreferredLlanguage, setLocale
        // if no lanaguage selected, setLocale to null pointer
        // if default is set, setLocale to Locale.getDefault
        if ( null != language ) {
	        Locale locale = LocaleString.getLocaleFromString(language);
			boolean isSet = util.isLocaleChanged(user.getLocale(), locale);
	        if ( isSet ) {
	            user.setLocale(locale);
	            isModified = true;
	        }
        }

        this.setAttribute(firstNameId, this.firstname);
        this.setAttribute(lastNameId, this.lastname);
        this.setAttribute(displayNameId, this.displayname);
        this.setAttribute(emailId, this.email);
        this.setAttribute(salutationId, this.salutation);
        this.setAttribute(titleId, this.title);
        this.setAttribute(timeZoneId, this.timezone);
        this.setAttribute(telephoneId, this.telephone);
        this.setAttribute(faxId, this.fax);
        this.setAttribute(mobileId, this.mobile);
        this.setAttribute(currencyId, this.currency);
        this.setAttribute(zipId, this.zip);
        this.setAttribute(streetAddressId, this.street);
        this.setAttribute(cityId, this.city);
        this.setAttribute(stateId, this.state);
        this.setAttribute(countryId, this.country);
        this.setAttribute(positionId, this.position);
        this.setAttribute(departmentId, this.department);
        this.setAttribute(accessibilitylevelId, String.valueOf(this.accessibilitylevel));
        this.setAttribute(noteToAdmin, this._noteToAdmin);
        this.setAttribute(orgUnitId, this._orgUnit);

        // to retrieve all additional attribute names
        if ( !_addAttris.isEmpty() ) {
            trace.debugT(methodName, "to set additional attributes");
            // System.out.println("to set additional attributes");
            int size = _addAttris.size();
            String[] names = new String[size];
            Enumeration keys = _addAttris.keys();
            names = util.enumToStringArray(keys);
            String name = null;
            String[] values = null;
            for ( int i=0; i<_addAttris.size(); i++ )  {
                name = names[i];
                values = (String[]) _addAttris.get(name);
                if ( null != util.setEmpty(values[0]) ) {
                    // System.out.println("Modify name "+name+" value "+value);
                    this.setAttribute(name, values[0]);
                } else {
                    continue;
                }
            }
        }
        // over
        trace.exiting(methodName);
    } // modifyUser

    public IUserMaint createUser(String uniqueName,
                           String companyId,
                           boolean approve,
                           UserBean addAttributes)
        throws UMException {
        final String methodName = "createUser";
        IUserMaint user = null;
        
        try {
            user = UMFactory.getUserFactory().newUser(uniqueName); // take logonid as uniqueName

			modifyUser(user);
        
			if ( null != addAttributes ) {
				addAttributes.setAccessibilityLevel(this.accessibilitylevel);
				addAttributes.setLocale(user.getLocale());
				addAttributes.modifyUser(user);
			}
         
			if ( approve ) {
				if ( null != companyId ) {
					user.setCompany(companyId);
				} else {
					user.setCompany(util.empty);
				}
			} else {
				user.setAttribute(UM, UUCOMPANYID, new String[]{companyId});
				user.setCompany(util.empty);
			}      
			
			user.save();
			user.commit();
        } catch (UserAlreadyExistsException ex) {
            trace.warningT(methodName, "user creation failed", ex);
            throw new UMException(UMEXCEPTION_USERALREADYEXIST);
        } catch (UMException ex) {
            trace.warningT(methodName, "user creation failed", ex);
            throw new UMException(UMEXCEPTION_GENERAL);
        }

        return user;
    } // createUser

    public ErrorBean checkUser(Locale locale) {
        String methodName = "checkUser";

        if ( null == this.uac ) this.uac = new UserAdminCustomization();

        // if checkPasswordOnly set to true, isNew is ignored
        ErrorBean error = checkFieldExistence(locale);
        if ( null != error ) {
            trace.debugT(methodName, "user input missing fields");
            return error;
        } else {
            trace.debugT(methodName, "all required fields are filled");
            trace.debugT(methodName, "goto check user input validity");
            error = checkFieldValidity(locale);
            if ( null != error ) {
                trace.debugT(methodName, "user input contains invalidated data");
                return error;
            } else {
                if ( this.uac.toCheckPhone() ) {
                    trace.debugT(methodName, "required fields are validated, going to check phone");
                    return checkPhone(locale);
                } else {
                    return null;
                }
            }
        }
    } // checkUser(boolean isNew)

    public Locale getLocale() {
        return _locale;
    } // getLocale

    public void setLocale(Locale locale) {
        _locale = locale;
    } // setLocale

    public ErrorBean checkFieldExistence(Locale locale) {
        String methodName = "checkFieldExistence";
        trace.entering(methodName);

        UserAdminLocaleBean localeBean = UserAdminLocaleBean.getInstance(locale);
        ErrorBean error = null;
        boolean isMulti = false;

        StringBuffer mf = new StringBuffer(); // missing fields

        if ( null != firstname ) {
            if ( util.empty.equals(firstname) ) {
                if ( mf.length() > 0 ) {
                    mf.append(", ");
                    isMulti = true;
                }
                mf.append(localeBean.get("FIRST_NAME"));
            }
        }

        if ( null != lastname ) {
            if ( util.empty.equals(lastname) ) {
                if ( mf.length() > 0 ) {
                    mf.append(", ");
                    isMulti = true;
                }
                mf.append(localeBean.get("LAST_NAME"));
            }
        }

        if ( null != email ) {
            if ( util.empty.equals(email) ) {
                if ( mf.length() > 0 ) {
                    mf.append(", ");
                    isMulti = true;
                }
                mf.append(localeBean.get("EMAIL"));
            }
        }

        if ( orgReq ) {
            if ( null == util.checkEmpty(_orgUnit) ) {
                if ( mf.length() > 1 ) {
                    mf.append(", ");
                    isMulti = true;
                }
                mf.append(localeBean.get("ORGUNIT"));
            }
            if ( null == util.checkEmpty(country) ) {
                if ( mf.length() > 0 ) {
                    mf.append(", ");
                    isMulti = true;
                }
                mf.append(localeBean.get("COUNTRY"));
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
        } // all missing fields

        return error;
    } // checkFieldExistence

    public ErrorBean checkFieldValidity(Locale locale) {
        final String methodName = "checkFieldValidity";
        trace.entering(methodName);

        if ( null == this.uac ) this.uac = new UserAdminCustomization();
        UserAdminLocaleBean localeBean = UserAdminLocaleBean.getInstance(locale);
        UserAdminMessagesBean msgBean =  UserAdminMessagesBean.getInstance(locale);

        if ( !util.empty.equals(util.checkNull(email)) ) {
            try {
                util.checkEmail(email);
            } catch ( BeanException ex) {
				trace.debugT("checkFieldValidity", ex);
                return new ErrorBean(ex);
            }
        }

        /* toCheckFieldLength
         * firstNameId, lastNameId, emailId, telephoneId, faxId, mobileId,
         * streetAddressId, zipId, cityId, stateId, positionId, departmentId
         * title, salutation -> not included
         * preferredLanguageId, countryId, timeZoneId -> predefined
         */
        StringBuffer msg = new StringBuffer();
        String msgId = UserAdminMessagesBean.INVALID_LENGTH;
        if ( null != util.checkEmpty(firstname) ) {
            if ( firstname.length() > getFirstNameMaxLength() ) {
                msg.append(msgBean.print(new Message(msgId, new Object[]{localeBean.get("FIRST_NAME"), new Integer(getFirstNameMaxLength())})));
            }
        }
        if ( null != util.checkEmpty(lastname) ) {
            if ( lastname.length() > getLastNameMaxLength() ) {
                if ( msg.length() > 0 ) msg.append("<BR>");
                msg.append(msgBean.print(new Message(msgId, new Object[]{localeBean.get("LAST_NAME"), new Integer(getLastNameMaxLength())})));
            }
        }
        if ( null != util.checkEmpty(salutation) ) {
            if ( salutation.length() > getSalutationMaxLength() ) {
                if ( msg.length() > 0 ) msg.append("<BR>");
                msg.append(msgBean.print(new Message(msgId, new Object[]{localeBean.get("SALUTATION"), new Integer(getSalutationMaxLength())})));
            }
        }
        if ( null != util.checkEmpty(title) ) {
            if ( title.length() > getTitleMaxLength() ) {
                if ( msg.length() > 0 ) msg.append("<BR>");
                msg.append(msgBean.print(new Message(msgId, new Object[]{localeBean.get("USER_TITLE"), new Integer(getTitleMaxLength())})));
            }
        }
        if ( null != util.checkEmpty(telephone) ) {
            if ( telephone.length() > getTelephoneMaxLength() ) {
                if ( msg.length() > 0 ) msg.append("<BR>");
                msg.append(msgBean.print(new Message(msgId, new Object[]{localeBean.get("PHONE"), new Integer(getTelephoneMaxLength())})));
            }
        }
        if ( null != util.checkEmpty(fax) ) {
            if ( fax.length() > getFaxMaxLength() ) {
                if ( msg.length() > 0 ) msg.append("<BR>");
                msg.append(msgBean.print(new Message(msgId, new Object[]{localeBean.get("FAX"), new Integer(getFaxMaxLength())})));
            }
        }
        if ( null != util.checkEmpty(mobile) ) {
            if ( mobile.length() > getMobileMaxLength() ) {
                if ( msg.length() > 0 ) msg.append("<BR>");
                msg.append(msgBean.print(new Message(msgId, new Object[]{localeBean.get("MOBILE"), new Integer(getMobileMaxLength())})));
            }
        }
        if ( null != util.checkEmpty(zip) ) {
            if ( zip.length() > getZipMaxLength() ) {
                if ( msg.length() > 0 ) msg.append("<BR>");
                msg.append(msgBean.print(new Message(msgId, new Object[]{localeBean.get("ZIP"), new Integer(getZipMaxLength())})));
            }
        }
        if ( null != util.checkEmpty(city) ) {
            if ( city.length() > getCityMaxLength() ) {
                if ( msg.length() > 0 ) msg.append("<BR>");
                msg.append(msgBean.print(new Message(msgId, new Object[]{localeBean.get("CITY"), new Integer(getCityMaxLength())})));
            }
        }
        if ( null != util.checkEmpty(state) ) {
            if ( state.length() > getStateMaxLength() ) {
                if ( msg.length() > 0 ) msg.append("<BR>");
                msg.append(msgBean.print(new Message(msgId, new Object[]{localeBean.get("STATE"), new Integer(getStateMaxLength())})));
            }
        }
        if ( null != util.checkEmpty(street) ) {
            if ( street.length() > getStreetAddressMaxLength() ) {
                if ( msg.length() > 0 ) msg.append("<BR>");
                msg.append(msgBean.print(new Message(msgId, new Object[]{localeBean.get("STREET"), new Integer(getStreetAddressMaxLength())})));
            }
        }
        if ( null != util.checkEmpty(position) ) {
            if ( position.length() > getPositionMaxLength() ) {
                if ( msg.length() > 0 ) msg.append("<BR>");
                msg.append(msgBean.print(new Message(msgId, new Object[]{localeBean.get("POSITION"), new Integer(getPositionMaxLength())})));
            }
        }
        if ( null != util.checkEmpty(department) ) {
            if ( department.length() > getDepartmentMaxLength() ) {
                if ( msg.length() > 0 ) msg.append("<BR>");
                msg.append(msgBean.print(new Message(msgId, new Object[]{localeBean.get("DEPARTMENT"), new Integer(getDepartmentMaxLength())})));
            }
        }


        // over
        if ( msg.length() > 0 ) {
            return new ErrorBean(new Message(UserAdminMessagesBean.EMPTY, new Object[]{msg.toString()}) );
        } else {
            return null;
        }
    } // checkFieldValidity

    public ErrorBean checkPhone(Locale locale) {
        String methodName = "checkPhone";
        trace.entering(methodName, new String[]{telephone, fax, mobile});

        UserAdminLocaleBean localeBean = UserAdminLocaleBean.getInstance(locale);

        String label = null;

        String phoneValue = util.checkEmpty(telephone);
        String faxValue = util.checkEmpty(fax);
        String mobileValue = util.checkEmpty(mobile);

        if ( null != phoneValue ) {
            try {
                label = localeBean.get("PHONE");
                util.checkPhone(telephoneId, phoneValue, label);
            } catch ( BeanException ex) {
				trace.debugT("checkPhone", ex);            	
                return new ErrorBean(ex);
            }
        }

        if ( null != faxValue ) {
            try {
                label = localeBean.get("FAX");
                util.checkPhone(faxId, faxValue, label);
            } catch ( BeanException ex) {
				trace.debugT("checkPhone", ex);
                return new ErrorBean(ex);
            }
        }

        if ( null != mobileValue ) {
            try {
                label = localeBean.get("MOBILE");
                util.checkPhone(mobileId, mobileValue, label);
            } catch ( BeanException ex) {
            	trace.debugT("checkPhone", ex);
                return new ErrorBean(ex);
            }
        }
        return null;
    } // checkPhone

    public String checkRegId() throws UMException {
        String id = util.checkEmpty(_regId);
        if ( null == id ) {
            return null;
        } else {
            //UmSusRegistration susReg = new UmSusRegistration();
                String bpartner = ""; //susReg.susRegistrationCheck(_regId);
                return bpartner;
        }
    } // checkRegId

    public void deleteRegId() throws UMException {
        //UmSusRegistration susReg = new UmSusRegistration();
    } // deleteRegId

    public IUser getUser() {
        try {
            return UMFactory.getUserFactory().getUser(uid);
        } catch (UMException ex) {
			if ( trace.beDebug() )
			   trace.debugT("getUser", ex.getMessage(), ex);             
            return null;
        }
    } // getUser

    public void setFilterFlag(boolean toPerformFiltering) {
        this.toFilter = toPerformFiltering;
    } // setFilterFlag

    public String getUniqueID() {
        return (this.toFilter==true)?util.filteringSpecialChar(uid):uid;
    } // getUniqueID

    public String getUid() {
        return getUniqueID();
    } // getUid

    public String getDisplayName() {
        return (this.toFilter==true)?util.filteringSpecialChar(this.displayname):this.displayname;
    } // getDisplayName

    public String getFirstName() {
        return (this.toFilter==true)?util.filteringSpecialChar(this.firstname):this.firstname;
    } // getFirstName

    public String getLastName() {
        return (this.toFilter==true)?util.filteringSpecialChar(this.lastname):this.lastname;
    } // getLastName

    public String getSalutation() {
        return (this.toFilter==true)?util.filteringSpecialChar(this.salutation):this.salutation;
    } // getSalutation

    public String getTitle() {
        return (this.toFilter==true)?util.filteringSpecialChar(this.title):this.title;
    } // getTitle

    public String getPreferredLanguage() {
        return language;
    } // getPreferredLanguage

    public String getTimeZone() {
        return timezone;
    } // getTimeZone

    public String getCurrency() {
        return (this.toFilter==true)?util.filteringSpecialChar(this.currency):this.currency;
    } // getCurrency

    public String getEmail() {
        return (this.toFilter==true)?util.filteringSpecialChar(this.email):this.email;
    } // getEmail

    public String getTelephone() {
        return (this.toFilter==true)?util.filteringSpecialChar(this.telephone):this.telephone;
    } // getTelephone

    public void setTelephone(String phone) {
        this.telephone = phone;
    }

    public String getFax()  {
        return (this.toFilter==true)?util.filteringSpecialChar(this.fax):this.fax;
    } // getFax

    public String getMobile() {
        return (this.toFilter==true)?util.filteringSpecialChar(this.mobile):this.mobile;
    } // getMobile

    public String getZip() {
        return (this.toFilter==true)?util.filteringSpecialChar(this.zip):this.zip;
    } // getZip

    public String getCity() {
        return (this.toFilter==true)?util.filteringSpecialChar(this.city):this.city;
    } //  getCity

    public String getState() {
        return (this.toFilter==true)?util.filteringSpecialChar(this.state):this.state;
    } // getState

    public String getCountry() {
        return (this.toFilter==true)?util.filteringSpecialChar(this.country):this.country;
    } // getCountry

    public String getStreetAddress() {
        return (this.toFilter==true)?util.filteringSpecialChar(this.street):this.street;
    } // getStreetAddress

    public String getPosition() {
        return (this.toFilter==true)?util.filteringSpecialChar(this.position):this.position;
    } // getPosition

    public String getDepartment() {
        return (this.toFilter==true)?util.filteringSpecialChar(this.department):this.department;
    } // getDepartment

    public int getAccessibilityLevel() {
        return accessibilitylevel;
    }
    
    public void setAccessibilityLevel(int newLevel) {
		accessibilitylevel = newLevel;
	}

    public String getCompanyMode() {
        return companyMode;
    } // getCompanyMode

    /**
     * Gets the unique id of the trading partner/ business partner this user belongs to.
     *
     * @return	 company id or null if this user does not belong to any trading partner
     */
    public String getCompanyId() {
        return (this.toFilter==true)?util.filteringSpecialChar(this._company):this._company;
    } // getCompanyId

    public String getCompanyName() throws TpdException {
        if ( null == _company ) return util.empty;
        TradingPartnerDirectoryInterface tpd = TradingPartnerDirectoryCommon.getTPD();
        try {
            PartnerID mpid = PartnerID.instantiatePartnerID( _company );
            TradingPartnerInterface company = tpd.getPartner( mpid );
            _companyName = util.checkNull(company.getDisplayName());
        } catch ( TpdException ex ) {
            trace.errorT("UserBean", ex.getMessage(), ex);
            return util.empty;
        }
        return (this.toFilter==true)?util.filteringSpecialChar(this._companyName):this._companyName;
    } // getCompanyName

    public void setCompanyId(String companyId) {
        if ( UserAdminHelper.isCompanyConceptEnabled() ) {
            _company = companyId;
            if ( null == companyId ) {
                super.setCompany(util.empty, com.sap.security.api.ISearchAttribute.EQUALS_OPERATOR, false);
                _isSet = true;
            } else {
                if ( !"*".equals(companyId) ) {
                    super.setCompany(companyId, util.getOperator(companyId), false);
                    _isSet = true;
                }
            }
        }
    } // setCompanyId

    public void setCompanyName(String companyName) {
        _companyName = companyName;
    }

    // email
    public String getNoteToAdmin() {
        return (this.toFilter==true)?util.filteringSpecialChar(_noteToAdmin):this._noteToAdmin;
    }

    public void setNoteToAdmin(String note) {
        _noteToAdmin = note;
    }

    public String getMessageToRequestor() {
        return (this.toFilter==true)?util.filteringSpecialChar(this._messageToRequestor):this._messageToRequestor;
    } // getMessageToRequestor

    public void setMessageToRequestor(String message) {
        _messageToRequestor = message;
    } // setMessageToRequestor

    public String getOrgUnit() {
        return (this._orgUnit==null)?null:((this.toFilter==true)?util.filteringSpecialChar(this._orgUnit):this._orgUnit);
    } // getOrgUnit

    public void setOrgUnit(String unit) {
        _orgUnit = unit;
        super.setSearchAttribute(UM, orgUnitId, unit, util.getOperator(unit), false);
    } // setORgUnit

    public void setOrgReq(boolean toSet) {
        orgReq = toSet;
    } // setOrgReq

    public String getRegId() {
        return (this.toFilter==true)?util.filteringSpecialChar(this._regId):this._regId;
    } // getRegId

    public void setRegId(String id) {
        _regId = id;
    } // setRegId

    public String[] getAttribute(String attriname) {
        trace.debugT("getAttribute", "past parameter= "+attriname);
        String name = attriname;
        if ( name.startsWith(UserAdminCustomization.prefix) ) {
        	name = name.substring(UserAdminCustomization.prefix.length());
			trace.debugT("getAttribute", "processed name= "+name);
        }

        if ( !_addAttris.isEmpty() ) {
            String[] value = (String[]) _addAttris.get(name);
            if (value != null) {
                trace.debugT("getAttribute", "get from bean= "+value[0]);
                for (int i=0; i<value.length; i++) {
                    value[i] = (this.toFilter==true)?util.filteringSpecialChar(value[i]):value[i];
                }
            }
            return (value!=null)?value:new String[]{util.empty};
        } else {
            trace.debugT("getAttribute", "get from bean=empty");
            return new String[]{util.empty};
        }
    } // getAttribute

    public String[] getOrderByField() {
        return (orderBy==null)?null:orderBy;
    }

    public void setOrderByField(String[] orderBy) {
        this.orderBy = orderBy;
        // super.setOrderBy(orderBy);
    }

    public boolean searchAll() {
        return allUsersMode.equals(companyMode);
    } // searchAll

    public boolean searchIndividual() {
        return individualUsersMode.equals(companyMode);
    } // searchIndividual

    public boolean searchCompanyUsers() {
        return companyUsersMode.equals(companyMode);
    } // searchCompanyUser

    public boolean addCompanyUser() {
        return companyUsersMode.equals(companyMode);
    } // addCompanyUser

    public Boolean isApproved() {
        return isApproved;
    } // isApproved

    public void setApproved(boolean approved) {
        isApproved = new Boolean(approved);
        // super.setApproved(approved);
    } // setApproved

    public boolean isFirstNameWanted() {
        return true;
    } // isFirstNameWanted

    public boolean isLastNameWanted() {
        return true;
    } // isLastNameWanted

    public boolean isEmailWanted() {
        return true;
    } // isEmailWanted

    protected String getSearchCriteria() {
        return _criteria.toString();
    } // getSearchCriteria

    protected boolean isSet() {
        return this._isSet;
    } // isSet()

    protected void setModified(boolean modified) {
        this.isModified = modified;
    } // setModified

    protected boolean isModified() {
        return this.isModified;
    } // isModiified

    protected boolean deleteUser(IUser user) {
        final String methodName = "deletUser";
        try {
            UMFactory.getUserFactory().deleteUser(user.getUniqueID());
        } catch (UMException ex) {
            trace.errorT(methodName, ex.getMessage(), ex);
            return false;
        }
        return true;
    } // deleteUser

    private static int getFieldMaxLength(String fieldName) {
        if ( fieldName.equals(firstNameId) ) {
            return firstname_len;
        } else if ( fieldName.equals(lastNameId) ) {
            return lastname_len;
        } else if ( fieldName.equals(salutationId) ) {
            return salutation_len;
        } else if ( fieldName.equals(titleId) ) {
            return title_len;
        } else if ( fieldName.equals(preferredLanguageId) ) {
            return preferredLanguage_len;
        } else if ( fieldName.equals(timeZoneId) ) {
            return timeZone_len;
        } else if ( fieldName.equals(currencyId) ) {
            return currency_len;
        } else if ( fieldName.equals(emailId) ) {
            return email_len;
        } else if ( fieldName.equals(telephoneId) ) {
            return telephone_len;
        } else if ( fieldName.equals(faxId) ) {
            return fax_len;
        } else if ( fieldName.equals(mobileId) ) {
            return mobile_len;
        } else if ( fieldName.equals(zipId) ) {
            return zip_len;
        } else if ( fieldName.equals(cityId) ) {
            return city_len;
        } else if ( fieldName.equals(stateId) ) {
            return state_len;
        } else if ( fieldName.equals(countryId) ) {
            return country_len;
        } else if ( fieldName.equals(streetAddressId) ) {
            return streetAddress_len;
        } else if ( fieldName.equals(positionId) ) {
            return position_len;
        } else if ( fieldName.equals(departmentId) ) {
            return department_len;
        } else {
            return -1;
        }
    } // getFieldDefaultLength

    private void setUserSearchFilter(IAccessToLogic proxy) {
        if ( null != displayname ) {
            if ( !"*".equals(displayname) ) {
                _isSet = true;
                super.setDisplayName(displayname, util.getOperator(displayname), false);
            }
            setCriteria(true, "DISPLAYNAME", displayname);
        }

        if ( null != firstname ) {
            if ( !"*".equals(firstname) ) {
                _isSet = true;
                super.setFirstName(firstname, util.getOperator(firstname), false);
            }
            setCriteria(true, "FIRST_NAME", firstname);
        }

        if ( null != salutation ) {
            if ( !"*".equals(salutation)) {
                _isSet = true;
                super.setSalutation(salutation, util.getOperator(salutation), false);
            }
            setCriteria(true, "SALUTATION", salutation);
        }

        if ( null != lastname ) {
            if ( !"*".equals(lastname)) {
                _isSet = true;
                super.setLastName(lastname, util.getOperator(lastname), false);
            }
            setCriteria(true, "LAST_NAME", lastname);
        }

        if ( null != title ) {
            if ( !"*".equals(title)) {
                _isSet = true;
                super.setTitle(title, util.getOperator(title), false);
            }
            setCriteria(true, "USER_TITLE", title);
        }

        if ( null != language ) {
            if ( !"*".equals(language)) {
                _isSet = true;
                super.setSearchAttribute(IPrincipal.DEFAULT_NAMESPACE, 
                						 User.LOCALE, 
                						 LocaleString.getLocaleFromString(language).toString(),
                						 util.getOperator(language), 
                						 false);
            }
            LanguagesBean languagesBean = (LanguagesBean) proxy.getSessionAttribute(LanguagesBean.beanId);
            StringBuffer lang = new StringBuffer(50);
            lang.append(language);
           	String description = languagesBean.getName(language);
           	if ( !"".equals(description) ) {
				lang.append(" ").append(description);
           	}            
            setCriteria(true, "LANGUAGE", lang.toString());
        }

        if ( null != currency ) {
            if ( !"*".equals(currency)) {
                _isSet = true;
                super.setCurrency(currency, util.getOperator(currency), false);
            }
            setCriteria(true, "CURRENCY", currency);
        }

        if ( null != email ) {
            if ( !"*".equals(email)) {
                _isSet = true;
                super.setEmail(email, util.getOperator(email), false);
            }
            setCriteria(true, "EMAIL", email);
        }

        if ( null != telephone ) {
            if ( !"*".equals(telephone)) {
                _isSet = true;
                super.setTelephone(telephone, util.getOperator(telephone), false);
            }
            setCriteria(true, "PHONE", telephone);
        }

        if ( null != fax ) {
            if ( !"*".equals(fax)) {
                _isSet = true;
                super.setFax(fax, util.getOperator(fax), false);
            }
            setCriteria(true, "FAX", fax);
        }

        if ( null != mobile ) {
            if ( !"*".equals(mobile)) {
                _isSet = true;
                super.setCellPhone(mobile, util.getOperator(mobile), false);
            }
            setCriteria(true, "MOBILE", mobile);
        }

        if ( null != zip ) {
            if ( ! "*".equals(zip) ) {
                _isSet = true;
                super.setZip(zip, util.getOperator(zip), false);
            }
            setCriteria(true, "ZIP", zip);
        }

        if ( null != city ) {
            if ( !"*".equals(city) ) {
                _isSet = true;
                super.setCity(city, util.getOperator(city), false);
            }
            setCriteria(true, "CITY", city);
        }

        if ( null != state ) {
            if ( ! "*".equals(state) ) {
                _isSet = true;
                super.setState(state, util.getOperator(state), false);
            }
            setCriteria(true, "STATE", state);
        }

        if ( null != country ) {
            if ( !"*".equals(country) ) {
                _isSet = true;
                super.setCountry(country, util.getOperator(country), false);
            }
			CountriesBean countriesBean = (CountriesBean) proxy.getSessionAttribute(CountriesBean.beanId);
			StringBuffer coun = new StringBuffer(50);
			coun.append(language);
			String description = countriesBean.getName(country);
			if ( !"".equals(description) ) {
				coun.append(" ").append(description);
			}             
            setCriteria(true, "COUNTRY", coun.toString());
        }

        if ( null != timezone ) {
            if ( !"*".equals(timezone) ) {
                _isSet = true;
                super.setSearchAttribute(IPrincipal.DEFAULT_NAMESPACE, User.TIMEZONE, java.util.TimeZone.getTimeZone(timezone).getID(), util.getOperator(timezone), false);
            }
            setCriteria(true, "TIME_ZONE", timezone);
        }

        if ( null != street )  {
            if ( !"*".equals(street) ) {
                _isSet = true;
                super.setStreet(street, util.getOperator(street), false);
            }
            setCriteria(true, "STREET", street);
        }

        if ( null != position ) {
            if ( !"*".equals(position) ) {
                _isSet = true;
                super.setJobTitle(position, util.getOperator(position), false);
            }
            setCriteria(true, "POSITION", position);
        }

        if ( null != department ) {
            if ( !"*".equals(department) ) {
                _isSet = true;
                super.setDepartment(department, util.getOperator(department), false);
            }
            setCriteria(true, "DEPARTMENT", department);
        }

        /*
        if ( companyMode.equals(allUsersMode) ) {
            super.searchAllUsers(true);
        } else {
            super.searchAllUsers(false);
        }
        */
    } // setUserSearchFilter()

    private void setCriteria(boolean localized, String label, String value) {       
        if ( _criteria.length() > 0 ) {
            _criteria.append(", ");
        }
        _criteria.append("\"");
        if ( localized ) {
			_criteria.append(this._localeBean.get(label));
        } else {
			_criteria.append(label);
        }
            
        _criteria.append("\"").append("=");
        _criteria.append("\"").append(value).append("\"");
    } // setCriteria

    // to retrieve all additional attribute names
    private void getAddAttri(IUser user) {
        String methodName = "getAddAttri(IUser)";
        StringBuffer message = new StringBuffer(80);
        message.append("To retrieve user ").append(user.getUniqueID()).append("'s non-standard attributes");
        trace.entering(message.toString());
        String[] nameSpaces = user.getAttributeNamespaces();
        String nameSpace = null;
        String[] addAttriNames;
        int size = -1;
		String[] addAttri;
		String addAttriName;
		StringBuffer nameWithNameSpace = new StringBuffer(80);
        for ( int i=0; i<nameSpaces.length; i++ ) {
        	nameSpace = nameSpaces[i];
			addAttriNames = user.getAttributeNames(nameSpace);
			message.delete(0, message.length());
			if ( (null!=addAttriNames) || (addAttriNames.length>0) ) {
				message.append("attribute not empty under namespace ").append(nameSpace);
				trace.debugT(methodName, message.toString());
				size = addAttriNames.length;
				for ( int j=0; j<size; j++) {
					addAttriName = addAttriNames[j];
					if ( (null != user.getAttributeType(nameSpace, addAttriName)) 
						&& IPrincipal.STRING_TYPE.equals(user.getAttributeType(nameSpace, addAttriName)) ) {
						addAttri = user.getAttribute(nameSpace, addAttriName);
						nameWithNameSpace.delete(0, nameWithNameSpace.length());
						if ( !nameSpace.equalsIgnoreCase(UM) ) {
							nameWithNameSpace.append(nameSpace).append(UserAdminCustomization.nameSpaceIdentifier);
						}					
						nameWithNameSpace.append(addAttriName);
						if ( null == addAttri ) addAttri = new String[]{""};
						_addAttris.put(nameWithNameSpace.toString(), addAttri);
					}
				}
			} else {
				message.append("no attributes stored under namespace ").append(nameSpace);
				trace.debugT(methodName, message.toString());
			}			 
        }
    } // getAddAttri(IUser user)

    // to retrieve all additional attribute names from request
    private void getAddAttri(IAccessToLogic proxy, boolean toSearch) {
        Enumeration allPs = proxy.getRequestParameterNames();
        String[] parameters = util.enumToStringArray(allPs);
        String parameter = null;
        String nameSpace = UM;
        String[] values = new String[10];
        int namespaceIdentifierPos = -1;
        for (int i=0; i<parameters.length; i++) {
            parameter = parameters[i];
            nameSpace = UM;
            if ( parameter.startsWith(UserAdminCustomization.prefix)) {
                values = proxy.getRequestParameterValues(parameter);
                String first = util.checkEmpty(proxy.getRequestParameter(parameter));
                parameter = parameter.substring(UserAdminCustomization.prefix.length());

                if ( toSearch ) {
					namespaceIdentifierPos = parameter.indexOf(UserAdminCustomization.nameSpaceIdentifier);
					if ( namespaceIdentifierPos > 1 ) {
						nameSpace = parameter.substring(0, namespaceIdentifierPos);
						parameter = parameter.substring((namespaceIdentifierPos+1));
					}                	
                    if ( null != first) {
                        if ( !"*".equals(first) ) {
                            this._isSet = true;
                            trace.debugT("set search attribute", nameSpace+"@"+parameter);
                            super.setSearchAttribute(nameSpace, parameter, values[0], util.getOperator(values[0]), false);
                        }
                        setCriteria(false, parameter, values[0]);
                    }
                } else {
                    _addAttris.put(parameter, values);
                }
            } else {
                continue;
            }
        }
    } // setAddAttri(IAccessToLogic proxy)

    private void setAttribute(String attributeName, String newValue) {
    	
        if ( null != newValue ) {
			int nameSpaceIdentiferPos = attributeName.indexOf(UserAdminCustomization.nameSpaceIdentifier);
			String nameSpace = UM;
			if ( nameSpaceIdentiferPos > 1 ) {
				nameSpace = attributeName.substring(0, nameSpaceIdentiferPos);
				attributeName = attributeName.substring((nameSpaceIdentiferPos+1));
			}    	
			String uniqueid = _user.getUniqueID();
			boolean readOnly = false;
			if (uniqueid != null)
			{
				try
				{
					if (!UMFactory.getPrincipalFactory().isPrincipalAttributeModifiable(uniqueid,nameSpace,attributeName))
					{
						return;
					}
				}
				catch (UMException ume)
				{
					trace.debugT("setAttribute", ume);
				}
			}
			String oldValue[] = _user.getAttribute(nameSpace, attributeName);        	
			boolean toSet = false;
			newValue = newValue.trim();
			if ( (null==oldValue) || (oldValue.length<1) ) {
				if ( !"".equals(newValue) ) {
					toSet = true;
				}
			} else {
				if ( !oldValue[0].equals(newValue) ) {
					toSet = true;
				}
			}
            if ( toSet ) {
                _user.setAttribute(nameSpace, attributeName, new String[]{newValue});
                isModified = true;
            }
        }
    } // setAttribute
} // UserBean

