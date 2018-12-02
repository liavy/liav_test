/***************************************************************
 * Framework Functions. Do not Override them....
 * Author: Kiran K.G.
 * Date  : Sept-2001
 ***************************************************************/

/*** formally utils_ie4.js **/


// This whole script was taken from David Flanagan's "JavaScript: The
// Definitive Guide"

// The constructor function: creates a cookie object for the specified
// document, with a specified name and optional attributes.
// Arguments:
//   document: The Document object that the cookie is stored for. Required.
//   name:     A string that specifies a name for the cookie. Required.
//   hours:    An optional number that specifies the number of hours from now
//             that the cookie should expire.
//   path:     An optional string that specifies the cookie path attribute.
//   domain:   An optional string that specifies the cookie domain attribute.
//   secure:   An optional Boolean value that, if true, requests a secure cookie.
//
function Cookie(document, name, hours, path, domain, secure)
{
    // All the predefined properties of this object begin with '$'
    // to distinguish them from other properties which are the values to
    // be stored in the cookie.
    this.$document = document;
    this.$name = name;
    if (hours)
        this.$expiration = new Date((new Date()).getTime() + hours*3600000);
    else this.$expiration = null;
    if (path) this.$path = path; else this.$path = null;
    if (domain) this.$domain = domain; else this.$domain = null;
    if (secure) this.$secure = true; else this.$secure = false;
}

// This function is the store() method of the Cookie object.
function _Cookie_store()
{
    // First, loop through the properties of the Cookie object and
    // put together the value of the cookie. Since cookies use the
    // equals sign and semicolons as separators, we'll use colons
    // and ampersands for the individual state variables we store
    // within a single cookie value. Note that we escape the value
    // of each state variable, in case it contains punctuation or other
    // illegal characters.
    var cookieval = "";
    for(var prop in this) {
        // Ignore properties with names that begin with '$' and also methods.
        if ((prop.charAt(0) == '$') || ((typeof this[prop]) == 'function'))
            continue;
        if (cookieval != "") cookieval += '&';
        cookieval += prop + ':' + escape(this[prop]);
    }

    // Now that we have the value of the cookie, put together the
    // complete cookie string, which includes the name and the various
    // attributes specified when the Cookie object was created.
    var cookie = this.$name + '=' + cookieval;
    if (this.$expiration)
        cookie += '; expires=' + this.$expiration.toGMTString();
    if (this.$path) cookie += '; path=' + this.$path;
    if (this.$domain) cookie += '; domain=' + this.$domain;
    if (this.$secure) cookie += '; secure';

    // Now store the cookie by setting the magic Document.cookie property.
    this.$document.cookie = cookie;
}
// This function is the load() method of the Cookie object.
function _Cookie_load()
{
    // First, get a list of all cookies that pertain to this document.
    // We do this by reading the magic Document.cookie property.
    var allcookies = this.$document.cookie;
    if (allcookies == "") return false;

    // Now extract just the named cookie from that list.
    var start = allcookies.indexOf(this.$name + '=');
    if (start == -1) return false;   // Cookie not defined for this page.
    start += this.$name.length + 1;  // Skip name and equals sign.
    var end = allcookies.indexOf(';', start);
    if (end == -1) end = allcookies.length;
    var cookieval = allcookies.substring(start, end);

    // Now that we've extracted the value of the named cookie, we've
    // got to break that value down into individual state variable
    // names and values. The name/value pairs are separated from each
    // other by ampersands, and the individual names and values are
    // separated from each other by colons. We use the split method
    // to parse everything.
    var a = cookieval.split('&');    // Break it into array of name/value pairs.
    for(var i=0; i < a.length; i++)  // Break each pair into an array.
        a[i] = a[i].split(':');

    // Now that we've parsed the cookie value, set all the names and values
    // of the state variables in this Cookie object. Note that we unescape()
    // the property value, because we called escape() when we stored it.
    for(var i = 0; i < a.length; i++) {
        this[a[i][0]] = unescape(a[i][1]);
    }

    // We're done, so return the success code.
    return true;
}

// This function is the remove() method of the Cookie object.
function _Cookie_remove()
{
    var cookie;
    cookie = this.$name + '=';
    if (this.$path) cookie += '; path=' + this.$path;
    if (this.$domain) cookie += '; domain=' + this.$domain;
    cookie += '; expires=Fri, 02-Jan-1970 00:00:00 GMT';

    this.$document.cookie = cookie;
}

// Create a dummy Cookie object, so we can use the prototype object to make
// the functions above into methods.
new Cookie();
Cookie.prototype.store = _Cookie_store;
Cookie.prototype.load = _Cookie_load;
Cookie.prototype.remove = _Cookie_remove;

/*** end formally utils_ie4.js **/

/** formally pdc_ie4.js **/
//<!--
// Ultimate client-side JavaScript client sniff. Version 3.03
// (C) Netscape Communications 1999-2001.  Permission granted to reuse and distribute.

// DKG find this script to put stuff back in for better browser detection..
// search the "Internet" or the "World" "wide" "Web" for "Ultimate JavaScript client Sniffer

    var agt=navigator.userAgent.toLowerCase();
    var is_major = parseInt(navigator.appVersion);
    var is_minor = parseFloat(navigator.appVersion);
    var is_nav  = (agt.indexOf('mozilla')!=-1) ;
    var is_nav6 = (is_nav && (is_major == 5));
    var is_ie     = (agt.indexOf("msie") != -1);


//--> end hide JavaScript

if (is_ie == true) {
// ie
 var TABLECELLDISPLAY="block";
 var IMAGEDISPLAY="block";
 var WEFtooltip = "wefTooltip_ie";
}

if (is_nav6 == true) {
// nn 6
 var TABLECELLDISPLAY="table-cell";
 var IMAGEDISPLAY="inline";
 var WEFtooltip = "wefTooltip_ns";
}


function swapObjects(primaryId, substituteId,targetId,wefCookie) {
    if (wefCookie.display=="none") {
       wefCookie.display=TABLECELLDISPLAY;
       primaryId.style.display = IMAGEDISPLAY;
       substituteId.style.display = "none";
    } else {
       wefCookie.display="none";
       primaryId.style.display = "none";
       substituteId.style.display = IMAGEDISPLAY;
    }
    wefCookie.store();
    targetId.style.display = wefCookie.display;
}

function onRetractorRollover (imageObject, tempObject,szImageRollover) {
   if (szImageRollover!=null) {
       tempObject.src=imageObject.src;
       imageObject.src = eval(szImageRollover + ".src");
    } else {
       imageObject.src = tempObject.src;
   }
}

function expandhtmlbSubmit(elem,formID,controlID,eventName,paramCount,param1,param2,param3,param4,param5,param6,param7,param8, param9) {
    var subFormId = param2;
    var pos = subFormId.indexOf(":");
    if(pos != -1)
        subFormId = subFormId.substring(0, pos);
    var expandObject = document.getElementById(controlID + "_" + subFormId + "_expand" + param5);
    if(expandObject == null)
        htmlbSubmit(elem,formID,controlID,eventName,paramCount,param1,param2,param3,param4,param5,param6,param7,param8, param9);
    else {
        // Already have the expanded row so just hide or display it.
        var f=document.forms[controlID];
        if(f==null){
            alert("expandhtmlbSubmit :: form "+ controlID + "not found");
            return true;
        }
        var subFormId = param2;
        var pos = subFormId.indexOf(":");
        if(pos != -1)
            subFormId = subFormId.substring(0, pos);
        var expandDiv = document.getElementById(controlID + "_" + subFormId + "_expandDiv" + param5);
        if(expandDiv==null){
            alert("Image id "+ controlID + "_" + subFormId + "_expandDiv" + param5 + " not found");
            return true;
        }
	    var rowList=f.wef_expandRowList;
        var myString = rowList.value;
        var expandString = ":exp" + param5 + ":";
        var collapseString = ":col" + param5 + ":";
        if(expandObject.style.display == "") {
            expandObject.style.display = "none";
            expandDiv.className = "IMG_EXPANDER_CLOSE";
            rowList.value = _wef_replaceOrAppendString(myString, expandString, collapseString);
        } else {
            expandObject.style.display = "";
            expandDiv.className = "IMG_EXPANDER_OPEN";
            rowList.value = _wef_replaceOrAppendString(myString, collapseString, expandString);
        }
    }
}

/** end formally pdc_ie4.js **/

/***********************TABLE RELATED FUNCTIONS BEGIN****************/
/*
 helper method used extensively
*/
function _wef_isObjectAlive(obj){
	if(obj==null) return false;
    if(String(obj)=="undefined")return false;
	return true;
}

/*****************************************************************
// Function that helps replace a string in a line or if the
// string is not found then just append the new string to the end.
******************************************************************/
function _wef_replaceOrAppendString(myString, oldString, newString) {
    var pos = myString.indexOf(oldString);
    var returnLine = myString;
    if(pos != -1) {
        var postString = myString.substring(0, pos);
        var preString = myString.substring(pos + oldString.length, myString.length);
        returnLine = postString + newString + preString;
    } else {
        returnLine = returnLine + newString;
    }
    return returnLine;

}

/*******************************************************************
//Function that adds Table related information to htmlbEvent object.
********************************************************************/
function _wef_setTableEventInfo(htmlbevent, parentRowIndex, parentId) {
    var ret = new Object();
    ret.rowIndex = htmlbevent.srcElement.getAttribute("rr");
    ret.colIndex = htmlbevent.srcElement.getAttribute("cc");
    ret.parentId = parentId;
    ret.parentRowIndex = parentRowIndex;
    eval("htmlbevent.tableEventInfo = ret;");
    return ret;
}
/*******************************************************************
//From a table, column could be retrieved either like table.columnName or table.getColumn
//Preferably use table.columnName instead of calling getColumn
********************************************************************/
function _wef_table_getColumn(name){
	var col=null;
	eval(" col=this."+name+";");
	return col;
}
/********************************************************************
Internal method called by the generated script.
htmlbName = htmlb_0_myform_5
wefForm= wef wrapper around html form
********************************************************************/
function _wef_table_createColumn(htmlbName,wefForm){
	var col=new Object();
	col.getCell=_wef_table_column_getCell;
	col.htmlbName=htmlbName;
	col.wefform=wefForm;
	return col;
}
/********************************************************************
Returns a control present in that particular row. Please note you
can not access static texts using this because they are not part of
any form control like InputField,button etc.
********************************************************************/
function _wef_table_column_getCell(row){
	if(this.wefform==null) return null;
	var ctrl=this.wefform._form[this.htmlbName+"_a"+row];
    if(ctrl==null) {
        /** check if it is the selections column **/
        ctrl=this.wefform._form[this.htmlbName+"-chk"+row];
    }
    if(ctrl==null){
        /**May be Nested Table***/
        eval("ctrl= ctrl_"+this.htmlbName+"_"+row);
	}
	if(ctrl==null){
		/**May be date time component **/
		ctrl=_wef_createDateTime(this.wefform_form,this.htmlbName+"_a"+row);
	}
	return ctrl;
}
function _wef_createDateTime(htmlForm,dateTimeMangledName){
	this._date=htmlForm[dateTimeMangledName+"_date"];
	this._time=htmlForm[dateTimeMangledName+"_time"];
	this.type="datetime";
	if(this._date!=null && this._time!=null){
	return this;
	}else{
	return null;
	}
}
/********************************************************************
Internal method called by the generated script.
htmlbName = htmlb_0_myform_5
realName= CHECKBOX_COL what ever specifed in the table view model
********************************************************************/
function _wef_table_addColumn(htmlbName, realName){
    var col=_wef_table_createColumn(htmlbName,this.wefform);
    if(_wef_isObjectAlive(realName)==false){
        /** Adding a nested Table**/
      eval("this."+htmlbName+"=col;");
    }else{
	eval("this."+realName+"=col;");
    }
}
/******************************************************************
strColName ="col1" row=2
******************************************************************/
function _wef_table_getCell(strColName,row){

	var col=null;
	eval(" col=this."+strColName+";");
	if(col!=null){
		return col.getCell(row);
	}
	return null;
}
/****TABLE RELATED FUNCTIONS END********************************/

/*****WEFFORM RELATED FUNCTIONS BEGIN*************************

 * WefForm object wraps the htmlb/html form
 * this should be created at the end of the form
**************************************************************/

 /*****Internal method to place a control ********************/
 function _wef_form_addControl(htmlbName,realName){
	  var ctrl=	this._form[htmlbName];
	  if(ctrl!=null){
		eval("this."+realName+"=ctrl;");
	  }else{
		//may be date-time
		ctrl=_wef_createDateTime(this._form,htmlbName);
		if(ctrl!=null){
			eval("this."+realName+"=ctrl;");
			eval("this."+htmlbName+"=ctrl;");
			eval("this."+realName+"_date=ctrl._date;");
			eval("this."+realName+"_time=ctrl._time;");
		}
	  }
	  return ctrl;
 }
 function _wef_form_getControl(htmlbName){
	 var ctrl=	this._form[htmlbName];
	 if(ctrl!=null) return ctrl;
	 //Check if it is DateTime
	 ctrl=this[htmlbName];
	 if(ctrl!=null) return ctrl;
	 //check if it is TableColumn..
	 ctrl=_wef_createDateTime(this._form,htmlbName);	
	 return ctrl;
 }
 function _wef_form_validate(){
    return this._fwValidator.validateForm(this);
 }
 function _wef_form_getValidator(){
    return this._fwValidator;
 }
 function _wef_form_doSubmit(bValidate,action,newWin,bNested){
 	if(true==bValidate){
		var bRet=this.validate();
		if(false==bRet) return false;
	}
	if(_wef_isObjectAlive(action)){
		this._form.action=action;
	}
	this.setNestedFlag(bNested);
	if(_wef_isObjectAlive(newWin)){
		var oldTarget=this._form.target;
		this._form.target=newWin.name;
		this._form.submit();
		this._form.target=oldTarget;
	}else{
		this._form.submit();
	}
    return true;
 }
 //this is an internal method.
 //setAction shouldnt be used by application.
 //targetWindow is string.
 function _wef_form_setAction(actionName,bNested,targetWindow){
    this._form.action=actionName;
	this.setNestedFlag(bNested);
    if(_wef_isObjectAlive(targetWindow)){
        this._form.target=targetWindow;
     }

 }
 function _wef_form_createTable(name,rowCount){
    var ret= new Object();
	ret.name=name;
	ret.addColumn=_wef_table_addColumn;
	ret.getCell=_wef_table_getCell;
	ret.getColumn=_wef_table_getColumn;
	ret.rowCount=rowCount;
    ret.wefform=this;
	//Add table to the form..
	eval("this."+name+"=ret;");
	return ret;
}
function _wef_form_setNestedFlag(bSet){
	if(this._form.wef_next_nested!=null){
		if(true==bSet)
		this._form.wef_next_nested.value="1";
		else
		this._form.wef_next_nested.value="";
	}
}
function _wef_form_getHiddenActionForward(){
	if(_wef_isObjectAlive(this._form.wef_actionForward)){
		return this._form.wef_actionForward.value;
	}
	return null;
}
function _wef_form_getHiddenUniqueId(){
	if(_wef_isObjectAlive(this._form.wef_formuniqueid)){
		return this._form.wef_formuniqueid.value;
	}
	return null;
}
function _wef_form_getHiddenActionMapping(){
	if(_wef_isObjectAlive(this._form.wef_actionmapping)){
		return this._form.wef_actionmapping.value;
	}
	return null;
}
function _wef_form_getHiddenStrutsToken(){
	if(_wef_isObjectAlive(this._form["org.apache.struts.taglib.html.TOKEN"])){
		return this._form["org.apache.struts.taglib.html.TOKEN"].value;
	}
	return null;
}
function _wef_form_getHiddenStateAsUrl(bNested){
   var form=this._form;	
   var completeUrl="";
   var controls= new Array(form.wef_actionForward,form.wef_formuniqueid,form.wef_actionmapping,form["org.apache.struts.taglib.html.TOKEN"]);
   for(var kir=0;kir<controls.length;kir++){
    var ctrl=controls[kir];
    if(_wef_isObjectAlive(ctrl)==false) continue;
    completeUrl=completeUrl.concat(ctrl.name+"="+ctrl.value+"&");
   }
   if(true==bNested)  completeUrl=completeUrl.concat("wef_next_nested.value=1&");
   return completeUrl;
}
/**
formName ="htmlb_0_myform
realName ="myform"
**/
function newWefForm(formName,realName){
	 var ret=new Object();
	 ret._name=formName;
	 ret._realName=realName;
	 ret._form=document.forms[formName];
	 ret.addControl=_wef_form_addControl;
     ret.validate=_wef_form_validate;
     ret._fwValidator= newFrameworkValidator(ret);
     ret.getValidator=_wef_form_getValidator;
	 ret.getControl=_wef_form_getControl;
     ret.doSubmit=_wef_form_doSubmit;
     ret.setAction=_wef_form_setAction;
	 ret.createTable=_wef_form_createTable;
	 ret.setNestedFlag=_wef_form_setNestedFlag;
	 ret.getHiddenActionForward=_wef_form_getHiddenActionForward;
	 ret.getHiddenUniqueId=_wef_form_getHiddenUniqueId;
	 ret.getHiddenActionMapping=_wef_form_getHiddenActionMapping;
	 ret.getHiddenStrutsToken=_wef_form_getHiddenStrutsToken;
	 ret.getHiddenStateAsUrl=_wef_form_getHiddenStateAsUrl;
	 return ret;
 }
 /*****WEFFORM RELATED FUNCTIONS END**********/
 /****VALIDATORS BEGIN *********/
 /**
 * Validator object for doing simple validations.
 * Please use only the public methods..
 */
function newFrameworkValidator(wefForm){
    var ret= new Object();
    ret.requiredFieldsInfo= new Array();
    ret.maskedFieldsInfo= new Array();
    ret.validateForm=_wef_validateForm;
    ret.addMaskedControl=_wef_addMaskedControl;
    ret.addRequiredControl=_wef_addRequiredControl;
    ret.validateRequired=_wef_validateRequired;
    ret.validateMasks=_wef_validateMasks;
    ret.matchPattern=_wef_matchPattern;
	ret.wefform=wefForm;
    return ret;
}
/***
 * Internal methods, objects. Dont use them directly.
 */
function newMaskedInfo(name,mask,errMesg){
    this.name=name;
    this.mask=mask;
    this.errMesg=errMesg;
}
/**
 * For Mask Validation
 */
function _wef_addMaskedControl(name,mask,errMesg){
     var oneMaskedInfo= new newMaskedInfo(name,mask,errMesg);
     this.maskedFieldsInfo[this.maskedFieldsInfo.length]=oneMaskedInfo;
}
/**
 * For Required Validator
 */
function _wef_addRequiredControl(name,errMesg){
    var oneObj= new Object();
    oneObj.name=name;
    oneObj.errMesg=errMesg;
    this.requiredFieldsInfo[this.requiredFieldsInfo.length]=oneObj;
}
/**
 * Called when form submit is called
 */
function _wef_validateForm(wefform) {
     return this.validateRequired(wefform)
	    && this.validateMasks(wefform)
	    && doApplicationValidation(wefform);
}
/**
 * Real validation functions.
 */
function _wef_validateRequired(wefform){
        var bValid=true;
        var i = 0;
        var fields = new Array();
		var reqs=this.requiredFieldsInfo;
        for(var j=0;j<reqs.length;j++){
		    var oneReq=reqs[j];
            var control= wefform.getControl(oneReq.name);
			if(control==null) continue;
            if ((control.type == "text" || control.type == "textarea" || control.type=="password" ) && control.value == "") {
                fields[i++]=oneReq.errMesg;
                bValid = false;
            }
			else if(control.type=="datetime"){
		        if (control._date.value == "" || control._time.value == "") {
					fields[i++]=oneReq.errMesg;
					bValid = false;
				}
			}
        }
        if (fields.length > 0) {
               alert(fields.join("\n"));
        }
        return bValid;
}
function _wef_validateMasks(wefform) {
        var bValid = true;
        var i = 0;
        var fields = new Array();
	var masks= this.maskedFieldsInfo;

	for(var j=0;j<masks.length;j++){
	    var oneMask=masks[j];
	    var control=wefform.getControl(oneMask.name);
            if ((control.type == "text" || control.type == "textarea" || control.type=="password" ) && control.value.length > 0) {
               if (!this.matchPattern(control.value,oneMask.mask)) {
	              fields[i++] = oneMask.errMesg;
	              bValid = false;
	           }
            }
	}
        if (fields.length > 0) {
           alert(fields.join("\n"));
	}
        return bValid;
}

function _wef_matchPattern(value, mask) {
     var regExp = mask;
     var bMatched = regExp.test(value);
     return bMatched;
}
/****VALIDATORS END *********/
/**
 * Framework assists in setting up of Actions and flow and other requried features
 * Use methods only thro these. Do not use Internal methods directly.
*/
function newFrameWorkObject(){
    var ret=new Object();

    ret.setAction=_wef_setAction;
	ret.setDefaultAction=_wef_setDefaultAction;
    ret.commit=_wef_commit;
    ret.navigateTo=_wef_navigateTo;
	ret.getWefUrl=_wef_getUrl;
    ret.setTableEventInfo=_wef_setTableEventInfo;
	ret.submitForm=_wef_submitForm;
	ret.setDoValidation=_wef_setDoValidation;
	ret.validate=true;
	ret.onFormSubmit=_wef_fw_onFormSubmit;
	ret.validateHtmlForm=_wef_fw_validateHtmlForm;
	ret.createForm=_wef_fw_createForm;
	ret.wefforms=new Array();
	ret.wefformsArray=new Array();
	ret.getForms=_wef_fw_getForms;
    ret.getForm=_wef_getForm;
	ret.destroyForm=_wef_fw_destroyForm;
    return ret;
}
function _wef_fw_getForms(){
	return this.wefforms;
}
function _wef_fw_destroyForm(realName){
	var _f= this.getForm(realName);
	if(_f==null) return;
    this.wefforms[realName]=null;
    this.wefforms[_f._name]=null;

	var tmpForms=new Array();
	var tmpFormsArray=new Array();
	for(var i=0;i<this.wefformsArray.length;i++){
		var _tf=this.wefformsArray[i];
		if(_tf._realName!=realName){
			tmpForms[tmpForms.length]=_tf;
			tmpFormsArray[tmpFormsArray.length]=_tf;
            tmpForms[_tf._realName]=_tf;
            tmpForms[_tf._name]=_tf;
		}
	}
	this.wefforms=tmpForms;
	this.wefformsArray=tmpFormsArray;
}

function _wef_fw_createForm(htmlbName,realName){
	var _f= newWefForm(htmlbName,realName);
	this.wefforms[this.wefforms.length]=_f;
    eval("this.wefforms[\""+realName+"\"]=_f;");
    eval("this.wefforms[\""+htmlbName+"\"]=_f;");
	this.wefformsArray[this.wefformsArray.length]=_f;
	return _f;
}
//DEPRECATED. ONLY FOR OLD VERSION
function _wef_setDefaultAction(strAction){
	if(this.wefformsArray==null || this.wefformsArray.length<1){
		window.status="bug: default Action setting";
		return;
	}
	var wefform=this.wefformsArray[this.wefformsArray.length-1];
	wefform.setAction(strAction,false,null);
}
function _wef_setDoValidation(bSet){
	this.validate=bSet;
}
function _wef_fw_onFormSubmit(realName){
    var ret=true;
	if(this.validate==false || isValidationNeededForThisSubmit(realName)!=true){
        ret=true;
    }else{
		var f=this.getForm(realName);
		if(f==null){
			alert("_wef_fw_onFormSubmit:: form "+ realName+ " not found");
			ret=true;
		}else{
			ret= f.validate();
		}
	}
	if(ret==true){
		this.destroyForm(realName);
    }
	return ret;
}
//Needed for Netscape....
function _wef_fw_validateHtmlForm(htmlName){
    var forms=this.getForms();
    var f=forms[htmlName];
    if(f==null){
        alert("_wef_fw_validateHtmlForm form "+ htmlName + " not found");
        return true;
    }
    var ret=false;
	if(this.validate==false || isValidationNeededForThisSubmit(f._realName)!=true){
        ret=true;
    }else{
    ret= f.validate();
    }
    if(true==ret){
    	this.destroyForm(f._realName);
    }
    return ret;
}
/**
 * Internal methods. do not use them.
 * Kiran this should be changed to take Form-Name
 */
function _wef_submitForm(bValidate,action,newWin,bNested,formName){
    var forms=this.getForms();
    var f=null;
    if(_wef_isObjectAlive(formName)==false){
        //backword compatibility use first form..
        window.status="_wef_submitForm: please pass formName too";
        f=forms[0];
    }else{
        f=forms[formName];
    }
    if(f!=null)
        f.doSubmit(bValidate,action,newWin,bNested);
    else
        alert("_wef_submitForm form" + formName + " not found");
}
//realName = myform
function _wef_getForm(realName){
    var forms=this.getForms();
    var f=forms[realName];
	if(f==null) alert("_wef_getForm form "+realName+" not found");
    return f;
}
/***
 * Support for Creation of Link, ie Appending our own hidden infos with the Link
 * that is sent..Code below is very sensitive to what is present in wef.util.ExConstants
 * in case there is any change there, it should be reflected here.
 */

 function _wef_getUrl(oldUrl,retainTransaction,nested){
   if(retainTransaction==false)
     return oldUrl;
   if(oldUrl.indexOf("?")==-1){
     oldUrl=oldUrl.concat("?");
   }else{
    var ch=oldUrl.charAt(oldUrl.length-1);
    if(ch!="&" && ch!= "?" )
        oldUrl=oldUrl.concat("&");
   }
   if(this.wefformsArray.length<1) return oldUrl;
   var wefform=this.wefformsArray[this.wefformsArray.length-1];
   var completeUrl=oldUrl+wefform.getHiddenStateAsUrl(nested);
   return completeUrl;
}
function _wef_navigateTo(oldUrl,retainTransaction,nested){
    var url=this.getWefUrl(oldUrl,retainTransaction,nested);
	window.location=url;
}

/***
 * Used by buttons to change Action dynamically
 */
function _wef_setAction(formName,actionName,bNested,targetWindow,bValidate){
    var wefform=this.getForm(formName);
    wefform.setAction(actionName,bNested,targetWindow);
    this.setDoValidation(false!=bValidate);
}
/**
 * Used in case of multiple Windows from the Child Window
 */
function _wef_commit(obj){
    var x= window.opener;
    if(_wef_isObjectAlive(x)){
	x.setClientChildWindowResult(obj);
	window.close();
    }
}
/**If u rename these, gotta change the renderers.**/
var frameWorkObject= newFrameWorkObject();

function getWefFramework(){
    return frameWorkObject;
}
function getWefScriptingWindow(){
	return window;
}

/********************************************************************
 * Functions which could be Over loaded by Application.
 * More documentation will come soon..
 * For the time being see how to use in the Sample application.
********************************************************************/
function openNewWindow(url){
    return window.open(url);
}
/**
 * If application wants to do their own Validations on the Client
 * They could do inside this method. (Of course this needs to be overridden
 * in their jsp page). Default implementation returns true;
 */
function doApplicationValidation(form){
    return true;
}
function setClientChildWindowResult(obj){
}
/**
 * If validation should be turned off entirely then over load this method.
 */
function isValidationNeededForThisSubmit(formName){
	return true;
}


