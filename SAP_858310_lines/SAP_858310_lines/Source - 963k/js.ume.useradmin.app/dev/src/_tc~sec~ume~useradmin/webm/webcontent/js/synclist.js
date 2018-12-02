var deleteFlag = false;
var resendFlag = false;

    function getForm(formname)
	{
		if( -1 != navigator.userAgent.indexOf("MSIE") ) {
	 	// Internet Explorer 
		return document.getElementById(formname);
 		} 
		else { 
		// other than IE 
		return document.forms[formname];
		} 
	}

function syncList_toggle_select(buttonArray) {
	if ( document.listForm.toggleSelect.checked == true ) {
		syncList_select_all(buttonArray);
	} else {
		syncList_deselect_all(buttonArray);
	}
}

function syncList_select_all(buttonArray) {
	document.listForm.SELECTED_USERS.checked = true;
	var radioGrp = document.listForm.SELECTED_USERS;
	for (var i=0; i<radioGrp.length; i++) {
		radioGrp[i].checked = true;
	}
    deleteFlag=1;
    resendFlag=0;
    
	for ( var j=0; j<buttonArray.length; j++ ) {
		document.getElementById(buttonArray[j]).disabled = false;
	}    
}

function syncList_deselect_all(buttonArray) {
	document.listForm.SELECTED_USERS.checked = false;
	var radioGrp = document.listForm.SELECTED_USERS;
	for (var i=0; i<radioGrp.length; i++) {
		radioGrp[i].checked = false;
	}
    deleteFlag=0;
    resendFlag=1;
    
	for ( var j=0; j<buttonArray.length; j++ ) {
		document.getElementById(buttonArray[j]).disabled = true;
	}       
}

function syncList_submit_error_detail(uid, id) {
        document.listForm.COMMAND.value = "DETAILS";
        document.listForm.UID.value = uid;
        document.listForm.ID.value = id;
        document.listForm.submit();
}

function syncList_changeCurrentPage(i) {
	document.listForm.COMMAND.value = "CHANGEFAILEDCURRENTPAGE" + i;
	document.listForm.submit();
}

function syncList_changeItemPerPage(i) {
	document.listForm.COMMAND.value = "CHANGEFAILEDITEMPERPAGE" + i;
	document.listForm.submit();
}

function syncList_nextPage() {
	document.listForm.COMMAND.value = "NEXTFAILEDPAGE";
	document.listForm.submit();
}

function syncList_prevPage() {
	document.listForm.COMMAND.value = "PREVFAILEDPAGE";
	document.listForm.submit();
}

function syncList_sortNameColumn() {
	document.listForm.COMMAND.value = "SORTNAMECOLUMN";
	document.listForm.submit();
}

function syncList_sortDateColumn() {
	document.listForm.COMMAND.value = "SORTDATECOLUMN";
	document.listForm.submit();
}

function syncList_initialize() {
	document.listForm.COMMAND.value = "";
}

function enableButtons(buttonArray) {
	var frm = getForm("listForm");
	var chckBoxGrp = frm.elements["SELECTED_USERS"];
	var atLeastOneSlcted = false;
	if ( chckBoxGrp.length == null ) {
		chckBoxGrp = document.getElementById("SELECTED_USERS");
		if ( chckBoxGrp.checked ) {
	        atLeastOneSlcted = true;			
		}
	} else {
		for ( var i=0; i<chckBoxGrp.length; i++ ) {
			if ( chckBoxGrp[i].checked ) {
				atLeastOneSlcted = true;
				break;
			}
		}	
	}	
	
	for ( var i=0; i<buttonArray.length; i++ ) {
		if ( atLeastOneSlcted ) {
			document.getElementById(buttonArray[i]).disabled = false;
		} else {
			document.getElementById(buttonArray[i]).disabled = true;			
		}				
	}
}
