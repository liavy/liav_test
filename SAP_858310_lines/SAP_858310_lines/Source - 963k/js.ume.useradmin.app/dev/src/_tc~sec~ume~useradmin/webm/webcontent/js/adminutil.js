function doOpenNewWin(wname, url) {
    window.name = wname;
    window.open(url, "sub", "WIDTH=460, HEIGHT=400, status=yes, resizable=no scrollbars=no");
}

function doSELFREGFORMSubmit(action) {
	var frm;
	if( -1 != navigator.userAgent.indexOf("MSIE") ) {
	 	// Internet Explorer 
		frm = document.getElementById("selfRegForm");
 	} 
	else { 
		// other than IE 
		frm = document.forms["selfRegForm"];
	} 
      inputTag = document.createElement("input");
      inputTag.setAttribute("name", action);
      inputTag.setAttribute("type", "hidden");
      inputTag.setAttribute("value", "");
      frm.appendChild( inputTag );
      frm.submit();	
}


    function doCompanySearch(id, arraytrue) {
          object = document.getElementById("selfreg_searchcom");
          inputTag1 = document.createElement("input");
          inputTag1.setAttribute("name", "<%=SelfRegServlet.performCompanySearchAction%>");
          inputTag1.setAttribute("type", "hidden");
          inputTag1.setAttribute("value", "");
          object.appendChild( inputTag1 );
          inputTag2 = document.createElement("input");
          inputTag2.setAttribute("name", "<%=companySelect.companySearchNameId%>");
          inputTag2.setAttribute("type", "hidden");
          inputTag2.setAttribute("value", id);
          object.appendChild( inputTag2 );
          alert(inputTag2.value);
        if ( arraytrue == "true" ) {
            inputTag3 = document.createElement("input");
            inputTag3.setAttribute("name", "array");
            inputTag3.setAttribute("type", "hidden");
            inputTag3.setAttribute("value", "");
            object.appendChild( inputTag3 );
        }
          object.submit();
    }
