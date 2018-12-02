
/* Date help for input field */

    function WEFCalendar() {
	this.currentDate = new Date();			// Thu Nov 1 11:54:00 PST 2001
	this.currentYear = this.currentDate.getFullYear();  // 2001
	this.currentDay = this.currentDate.getDay();		// 4 - Thursday
	this.currentMonth = this.currentDate.getMonth();	// 10
	this.today = this.currentDate.getDate();		//1
	this.daysInMonth = new Array(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
}

var calendar = null;

function HelpDateTime(webpath,
                      CalendarId, 
                      dayNames,  
                      dayNamesF, 
                      monthNames, 
                      monthNamesF, 
                      AMPM, 
                      spacer, 
                      timeZone, 
                      dateMask, 
                      timeMask, 
                      lowLimit, 
                      upperLimit, 
                      era, 
                      CssAttributes, 
                      FormName, 
                      FirstDay, 
                      minDaysInWeek, 
                      localeData, 
                      InputField ){
	if (calendar == null)
		calendar = new WEFCalendar();
	calendar.weekAbbreviation = localeData[0];
	calendar.title = localeData[1];
	calendar.previousMonths = localeData[2];
	calendar.nextMonths = localeData[3];
	calendar.closeWindow = localeData[4];
	calendar.time_title = localeData[5];
	calendar.date_title = localeData[6];
	calendar.select = localeData[7];
	calendar.cancel = localeData[8];
	calendar.monthNames = monthNames;
	calendar.firstDay = FirstDay;
	calendar.minDaysInFirstWeek = minDaysInWeek;
	calendar.dayNames = dayNames;
	calendar.formID = FormName;
	calendar.fieldID = CalendarId;
	calendar.dateMask = dateMask;
	calendar.timeMask = timeMask;
	calendar.lowLimit = lowLimit;
	calendar.upperLimit = upperLimit;
	calendar.css = CssAttributes;
	calendar.dayNamesF = dayNamesF;
	calendar.monthNamesF = monthNamesF;
	calendar.ampm = AMPM;
	calendar.era = era;
	calendar.spacer=spacer;
	calendar.timeZone=timeZone;
	calendar.InputField = InputField;
	calendar.DateString = "";
	calendar.currentSelection=calendar.currentYear+""+calendar.currentMonth+"" + calendar.today;
//        var lnDotPos = document.domain.indexOf( '.' );
//        if (lnDotPos >= 0) document.domain = document.domain.substr( lnDotPos + 1 );	
HTML_String = WEFMakeCal(0, webpath);	
calendar.newWindow=window.open(webpath,calendar.formID,"width=200, height=490, resizable=1;");
	
        //var lnDotPos = document.domain.indexOf( '.' );
        //if (lnDotPos >= 0) calendar.newWindow.document.domain = document.domain.substr( lnDotPos + 1 ); 
//	WEFWriteCalendar(HTML_String);

}

var HTML_String;

function doSomething()
{
	// alert(HTML_String);
	WEFWriteCalendar(HTML_String);
}

function WEFMakeCal(startMonth, webpath) {
	/*HTML_String = "<div>hallo1</div>";
	HTML_String+= "<div>hallo2</div>";
	HTML_String+= "<div>hallo3</div>";

	return HTML_String;*/


	var down_src = webpath+"layout/caldown.gif";
	var close_src = webpath+"layout/closex.gif";
	var HTML_String = "";
//	HTML_String += '<html>\n';
//	HTML_String += '<head>\n';
//	HTML_String += '<title>' + calendar.title + '</title>\n';
	for (i=0; i < calendar.css.length; i++) {
		HTML_String +="<LINK REL=\"stylesheet\" HREF=\"" + calendar.css[i] + "\" TYPE=\"text/css\">\n";
	}
//	HTML_String += '<script>\n';
//	HTML_String += 'function setDomain() { \n';
//	HTML_String += 'var lnDotPos = document.domain.indexOf( \".\" );\n';
//	HTML_String += 'if (lnDotPos >= 0) document.domain = document.domain.substr( lnDotPos + 1 );\n';
//	HTML_String += '}\n';
//	HTML_String += '<script>\n';
	//var Netscape6=(navigator.appName=="Netscape" && navigator.appVersion.substring(0,1) == "5")?" onblur='window.close()'":"";
//	HTML_String += '</head>\n';
//	HTML_String += '<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">\n';
    HTML_String += '<form name="CalendarForm">';
	HTML_String += '<center>';
	HTML_String += '<table cellpadding="0" cellspacing="0" border="0" width="120">';
	HTML_String += '<tr><td>';
	HTML_String += '<table cellpadding="0" cellspacing="0" border="0"><tr><td><img src="' + calendar.spacer + '" style="height:6px; width:1px"></td></tr></table>';
	HTML_String += '<table cellpadding="0" cellspacing="0" border="0" class="TBDATA_BDR_BG" width="100%">';
	HTML_String += '<tr>';
	HTML_String += '<td align="center">';
	HTML_String += '<table cellpadding="2" cellspacing="1" border="0">';
	HTML_String += '<TR>';
	HTML_String += '<td class="TBDATA_HEAD"><a href=';
    HTML_String += '\"javascript:window.opener.WEFSkipCal('+(startMonth - 1) + ')\">';
	HTML_String += '<img id="up" src="layout/caldup.gif" style="border:0" width="13" height="14" alt="' + calendar.previousMonths + '" title="' + calendar.previousMonths + '" ></a></td>';
	HTML_String += '<td class="TBDATA_HEAD"><a href=';
    HTML_String += '\"javascript:window.opener.WEFSkipCal('+(startMonth + 1) + ')\">';
	HTML_String += '<img src="layout/caldown.gif" style="border:0" width="13" height="14" alt="' + calendar.nextMonths + '" title="' + calendar.nextMonths + '" ></a></td>';
	HTML_String += '<td class="TBDATA_HEAD" width="100%">' + calendar.title + '</td>';
	HTML_String += '<td class="TBDATA_HEAD"><a href=\"javascript:window.opener.WEFCalClose()\">';
	HTML_String += '<img src="layout/closex.gif" style="border:0" width="13" height="14" alt="' + calendar.closeWindow + '" title="' + calendar.closeWindow + '" ></a></td>';
    HTML_String += '</TR>';
	HTML_String += '</table>';
    HTML_String += '</td>';
	HTML_String += '</tr>';
	HTML_String += '</table>';
	// start calendars
	HTML_String += '<table cellpadding="0" cellspacing="0" border="0"><tr><td><img src="' + calendar.spacer + '" style="height:6px; width:1px"></td></tr></table>';
	HTML_String += '<table cellpadding="0" cellspacing="0" border="0" class="TBDATA_BDR_BG" align="center">';
	HTML_String += '<tr>';
	HTML_String +=WEFMakeMonth(startMonth - 1, false);
	HTML_String += '</tr><tr>';
	HTML_String +=WEFMakeMonth(startMonth, true);
	//HTML_String += '<td><td class="TBDATA_CNT_ODD_BG"><img src="' + calendar.spacer + '" style="height:1px; width:20px"></td>';
	HTML_String += '</tr><tr>';
	HTML_String +=WEFMakeMonth(startMonth + 1, false);
	HTML_String += '</tr>';
	HTML_String += '</table>';
	//finish callendars
	
	HTML_String += '<table cellpadding="0" cellspacing="0" border="0"><tr><td><img src="' + calendar.spacer + '" style="height:10px; width:1px"></td></tr></table>';	
	
	HTML_String += '<table cellpadding="0" cellspacing="0" border="0" class="TBDATA_BDR_BG" width="100%">';
	HTML_String += '<tr>';
	HTML_String += '<td align="center">';
	HTML_String += '<table cellpadding="2" cellspacing="1" border="0">';
	HTML_String += '<TR>';
	HTML_String += '<td class="TBDATA_HEAD"><a href=';
    HTML_String += '\"javascript:window.opener.WEFSkipCal('+(startMonth - 1) + ')\">';
	HTML_String += '<img src="layout/caldup.gif" style="border:0" width="13" height="14" alt="' + calendar.previousMonths + '" title="' + calendar.previousMonths + '" ></a></td>';
	HTML_String += '<td class="TBDATA_HEAD"><a href=';
    HTML_String += '\"javascript:window.opener.WEFSkipCal('+(startMonth + 1) + ')\">';
	HTML_String += '<img src="layout/caldown.gif" style="border:0" width="13" height="14" alt="' + calendar.nextMonths + '" title="' + calendar.nextMonths + '" ></a></td>';
	HTML_String += '<td class="TBDATA_HEAD" width="100%">' + calendar.title + '</td>';
	HTML_String += '<td class="TBDATA_HEAD"><a href=\"javascript:window.opener.WEFCalClose()\">';
	HTML_String += '<img src="layout/closex.gif" style="border:0" width="13" height="14" alt="' + calendar.closeWindow + '" title="' + calendar.closeWindow + '" ></a></td>';
    HTML_String += '</TR>';
	HTML_String += '</table>';
    HTML_String += '</td>';
	HTML_String += '</tr>';
	HTML_String += '</table>';
		
	HTML_String += '<table cellpadding="0" cellspacing="0" border="0"><tr><td><img src="' + calendar.spacer + '" style="height:10px; width:1px"></td></tr></table>';
   	HTML_String += '<table cellpadding="0" cellpadding="0" border="0">';
	HTML_String += '<tr>';
	//HTML_String += '<td class="TBLO_XXS_L" nowrap>' + calendar.date_title + ' :</td>';
	HTML_String += '<td class="TX_XS">&nbsp;<input type="hidden" name="startdate" value="' + calendar.DateString + '" size="15" maxlength="20" class="TX_XS" ></td>';
	if (calendar.timeMask != null) {
	HTML_String += '<td>&nbsp;&nbsp;&nbsp;</td>';
	HTML_String += '<td class="TBLO_XXS_C" nowrap>' + calendar.time_title + ' :</TD>';
	HTML_String += '<td class="DROPDOWN_XS">&nbsp;<select name="hours" class="DROPDOWN_XS">';
	
	for (var i = calendar.lowLimit; i <= calendar.upperLimit; i++) {
		var hr = new String(i);
		if (i < 10) {
			hr = "0" + hr;
		}

		if (i != 12) {
			HTML_String +='<option value="' + hr +'">' + hr + '</option>';
		} else {
			HTML_String +='<option value="' + hr +'" selected>' + hr + '</option>';
		}
	}
	HTML_String += '</select>';
	HTML_String += '</td>';
	HTML_String += '<Td>&nbsp;</TD>';
	HTML_String += '<td class="DROPDOWN_XS"><select name="minutes" class="DROPDOWN_XS">';
	for (var i =0; i <= 59; i++) {
		var min = new String(i);
		if (i < 10) {
			min = "0" + min;
		}
		HTML_String +='<option value="' + min +'">' + min + '</option>';
	}

	HTML_String += '</select>';
	HTML_String += '</td>';
	if (calendar.ampm != null) { // not mil time
		HTML_String += '<td>&nbsp;</td>';
		HTML_String += '<td class="DROPDOWN_XS"><select name="ampmmark" class="DROPDOWN_XS">';
		for (i=0; i < 2; i++) {
			HTML_String += '<option value="' + calendar.ampm[i] + '">' + calendar.ampm[i] + '</option>';
		}
		HTML_String += '</select>&nbsp;' + calendar.timeZone + '';

	} else {
		HTML_String += '<td>&nbsp;' + calendar.timeZone + '';
	}
	HTML_String += '</td>';
	}

	HTML_String += '</tr>';

	HTML_String += '</table>';
	HTML_String += '</td>';
	HTML_String += '</tr>';
	HTML_String += '<tr>';
	HTML_String += '<td><img src="' + calendar.spacer + '" style="height:7px; width:1px"></td>';
	HTML_String += '</tr>';
	/*HTML_String += '<tr>';
	HTML_String += '<td><input class="BTN_S" type="button" value="&nbsp;' + calendar.select + '&nbsp;"';
	if (calendar.ampm != null) { // not mil time
		HTML_String += 'onClick="javascript:window.opener.WEFReturnDate(hours.options ,minutes.options, ampmmark.options )">&nbsp;';
	} else if (calendar.timeMask != null) {
		HTML_String += 'onClick="javascript:window.opener.WEFReturnDate(hours.options ,minutes.options, null )">&nbsp;';
	} else {
		HTML_String += 'onClick="javascript:window.opener.WEFReturnDate(null ,null, null )">&nbsp;';
	}
	HTML_String += '<input class="BTN_S" type="button" value="&nbsp;' + calendar.cancel + '&nbsp;"  onClick="javascript:window.opener.WEFCalClose()"></td>';
	HTML_String += '</tr>';*/
	HTML_String += '</table>';
	HTML_String += '<table>';
	HTML_String += '</table>';

	HTML_String +='</center>';
	HTML_String +='</form>';
//	HTML_String +='</body>\n';
//	HTML_String +='</html>\n';
	return HTML_String;

}

function WEFMakeMonth(number, firstMonth) {

	var floor = Math.floor((calendar.currentMonth + number)/12);
	var Month = (calendar.currentMonth + number) - floor*12;   //0-11
	var Year = calendar.currentYear + floor;


	var First_Date = new Date(Year, Month, 1); // Thu Nov 1 00:00:00 PST 2001

	var Heading = WEFCalHeader(Year, Month , calendar.monthNames); // November 2001
	var Days = calendar.daysInMonth[Month];
	var Prev_Days = 0;  // number of days in previous month -to display the disabled end of the prev. month
	if (Month != 0) {
		Prev_Days = calendar.daysInMonth[Month -1];
	} else {
		Prev_Days = calendar.daysInMonth[11];
	}

	var DayOfYear = 0;
	for (i = 0; i < Month; i++ ) {
		DayOfYear += calendar.daysInMonth[i];
	}
	var DayOfWeek = First_Date.getDay() + 1; //1-7; 1-Sun, 2-Mon,...

	//week calculation is taken from java.util.GregorianCalendar
	var relDow = (DayOfWeek + 7 - calendar.firstDay) % 7; // 0..6
    var relDowJan1 = (DayOfWeek - DayOfYear + 701 - calendar.firstDay) % 7; // 0..6
    var WeekOfYear = Math.floor((DayOfYear - 1 + relDowJan1) / 7); // 0..53
   	if ((7 - relDowJan1) >= calendar.minDaysInFirstWeek) {
            ++WeekOfYear;
	}
	var extraRow = false;
	// # of rows
	if (firstMonth) {
		var rows = WEFNumberOfRows(Days, DayOfWeek);
		var nextDays =0;
		
		var nextFirstDate;
		if (Month != 11) {
			nextDays = calendar.daysInMonth[Month + 1];
			nextFirstDate = new Date(Year, Month + 1, 1);
		} else {
			nextDays = calendar.daysInMonth[0];
			nextFirstDate = new Date(Year, 0, 1);
		}
		var nextDayOfWeek = nextFirstDate.getDay() + 1;
		var nextMonthRows = WEFNumberOfRows(nextDays, nextDayOfWeek);
		if (nextMonthRows > rows) {
			extraRow = true;
			calendar.extraRow = false;
		} else if (nextMonthRows < rows) {
			extraRow = false;
			calendar.extraRow = true;
		} else {
			extraRow = false;
			calendar.extraRow = false;
		}
	} else {
		extraRow = calendar.extraRow;
	}
	var First_Day = DayOfWeek; // 5 - first day for November
	if (First_Day < calendar.firstDay)
		First_Day +=7;

	var tmp = "";
	tmp +='<td align="center">\n';
	tmp +='<table cellpadding="1" cellspacing="1" border="0">\n';
	tmp +='<TR class="TBDATA_CNT_ODD_BG">\n';
	tmp +='<TD colspan="8" class="CAL_XXS_MAINB">'+ Heading + '<img src="' + calendar.spacer + '" style="height:1px; width:1px"></TD>\n';
	tmp +='</TR>\n';
	tmp +='<TR>\n';
	tmp +='<TD scope="col" class="TBDATA_HEAD">' + calendar.weekAbbreviation+ '</TD>\n';
	for (i = calendar.firstDay - 1; i < 6 + calendar.firstDay; i++) {
		if (i < 7) {
			tmp +='<TD scope="col" class="TBDATA_HEAD">'+ calendar.dayNames[i] + '</TD>\n';
		} else {
			tmp +='<TD scope="col" class="TBDATA_HEAD">'+ calendar.dayNames[i - 7] + '</TD>\n';
		}
	}

	tmp +='</TR>\n';
	var counter = 1;
	var dayCounter = calendar.firstDay;
	var newMonthCounter = 0;
	var limit = Days;
	if (extraRow) {
		limit = Days + 7;
	}
	while(counter <= limit) {
		tmp +='<TR class="SIDE_CNT_BG">\n';
		tmp +='<TD scope="row" class="TBLO_XXS_C">' + WeekOfYear + '</TD>\n';
		for (j=1; j< 8; j++) {
			if (dayCounter < First_Day ) {
				dayCounter++;
				if ((calendar.firstDay==1 && j ==1) || (j+calendar.firstDay) == 8 || (j+calendar.firstDay) == 9) {
					tmp +='<TD class="CAL_XXS_WEKND_DIS">'+ (Prev_Days - First_Day + dayCounter) +'</TD>\n';
				} else {
					tmp +='<TD class="CAL_XXS_DIS">'+ (Prev_Days - First_Day + dayCounter) +'</TD>\n';
				}
			} else if (counter <= Days){
				 if ((calendar.firstDay==1 && j ==1) || (j+calendar.firstDay) == 8 || (j+calendar.firstDay) == 9) {
				 	if (Year == calendar.currentYear && Month == calendar.currentMonth && counter == calendar.today) {
						tmp +='<TD ID="'+Year+""+Month+""+counter+'" class="CAL_XXS_TODAY"><a ID="'+Year+""+Month+""+counter+'_cell" class="CAL_XXS_TODAY" href=\"javaScript:window.opener.WEFCreateDate(' + Year+ ',' + (Month + 1) + ',' + counter + ',' + (calendar.firstDay + j - 2)  + ',' + false + ')\" onDblClick=\"javascript:window.opener.WEFReturnDate(null ,null, null )\">' + counter+ '</a></TD>\n';
						calendar.weekday=false;
					} else {
						tmp +='<TD ID="'+Year+""+Month+""+counter+'" class="CAL_XXS_WEKND"><a ID="'+Year+""+Month+''+counter+'_cell" class="CAL_XXS_WEKND" href=\"javaScript:window.opener.WEFCreateDate(' + Year+ ',' + (Month + 1) + ',' + counter +',' + (calendar.firstDay + j - 2)    + ',' + false + ')\" onDblClick=\"javascript:window.opener.WEFReturnDate(null ,null, null )\">' + counter+ '</a></TD>\n';
					}
					
				} else {
					if (Year == calendar.currentYear && Month == calendar.currentMonth && counter == calendar.today) {
						tmp +='<TD ID="'+Year+""+Month+""+counter+'" class="CAL_XXS_TODAY"><a ID="'+Year+""+Month+""+counter+'_cell" class="CAL_XXS_TODAY" href=\"javaScript:window.opener.WEFCreateDate(' + Year+ ',' + (Month + 1) + ',' + counter + ',' + (calendar.firstDay + j - 2)  + ',' + true + ')\" onDblClick=\"javascript:window.opener.WEFReturnDate(null ,null, null )\">' + counter+ '</a></TD>\n';
						calendar.weekday=true;
					} else {
						tmp +='<TD ID="'+Year+""+Month+""+counter+'" class="CAL_XXS_MAIN"><a ID="'+Year+""+Month+""+counter+'_cell" class="CAL_XXS_MAIN" href=\"javaScript:window.opener.WEFCreateDate(' + Year+ ',' + (Month + 1) + ',' + counter +',' + (calendar.firstDay + j - 2)   + ',' + true + ')\" onDblClick=\"javascript:window.opener.WEFReturnDate(null ,null, null )\">' + counter+ '</a></TD>\n';
						
					}
				}
				counter++;
			} else {
				newMonthCounter++;
				counter++;
				if ((calendar.firstDay==1 && j ==1) || (j+calendar.firstDay) == 8 || (j+calendar.firstDay) == 9) {
					tmp +='<TD class="CAL_XXS_WEKND_DIS">'+ newMonthCounter +'</TD>\n';
				} else {
					tmp +='<TD class="CAL_XXS_DIS">'+ newMonthCounter +'</TD>\n';
				}
			}
		}
		WeekOfYear++;
		tmp +='</TR>\n';
	}
	tmp +='</table>\n';
	tmp +='</td>\n';

	return tmp;
}

function WEFCalHeader(Year, Month, monthNames) {
	if ((Year % 400 == 0) || ((Year % 4 == 0) && (Year % 100 != 0))) {
		calendar.daysInMonth[1] = 29;
    	} else {
		calendar.daysInMonth[1] = 28;
	}

   var name = calendar.monthNames[parseInt(Month)];
   var Header_String = name + ' ' + Year;
   return Header_String;
}

function WEFCalClose(){
	calendar.newWindow.close();
	calendar.newWindow=null;
}
var is_ie;

function WEFWriteCalendar(HTML_stuff){ 	
        calendar.newWindow.document.body.innerHTML = HTML_stuff;
  	/*with (calendar.newWindow.document) {
  		open();
  		write(HTML_stuff);
		close();
  	}*/
	/*
	if(is_ie) { // to fix table borders problem
		calendar.newWindow.history.go(0);
	}*/
	calendar.newWindow.focus();
}

function WEFSkipCal(month) {
	HTML_String = WEFMakeCal(month);
	WEFWriteCalendar(HTML_String);	
}


function WEFCreateDate(Year, Month, Day, DayInWeek, weekday){
	var DateString = "";
	var year = new String(Year);
	var month = new String(Month);
	var day = new String(Day);
	calendar.DateString = ""; // reset
	for (i = 0 ; i < calendar.dateMask.length; i++) {

		if (calendar.monthNamesF != null) {
			month = calendar.monthNamesF[Month - 1];
			DateString = calendar.dateMask[i].replace("month_in_year",month);
		} else {
			if (calendar.dateMask[i].indexOf("month_in_year_two") >= 0) {
				if (Month < 10) {
					month = "0" + month;
				}
				DateString = calendar.dateMask[i].replace("month_in_year_two",month);
			} else {
				DateString = calendar.dateMask[i].replace("month_in_year_one",month);
			}
		}
		calendar.selectMonth = month;
		if (DateString.indexOf("day_in_month_full") >= 0) { 
			if	(Day < 10) {
				day =  "0" + day;
			}
			DateString = DateString.replace("day_in_month_full",day);
		} else {
			DateString = DateString.replace("day_in_month",day);
		}
		calendar.selectDay = day;
		if (DateString.indexOf("year_full") >= 0) {
			DateString = DateString.replace("year_full",year);
		} else {
			year = year.substring(2);
			DateString = DateString.replace("year",year);
		}
		calendar.selectYear = year;
		if (calendar.era != null) {
			DateString = DateString.replace("era_designator",calendar.era);
		}
		if (calendar.dayNamesF != null) {
			if (DayInWeek < 7) {
				calendar.selectDayInWeek = calendar.dayNamesF[DayInWeek];
				DateString = DateString.replace("day_in_week",calendar.dayNamesF[DayInWeek]);
			} else {
				calendar.selectDayInWeek = calendar.dayNamesF[DayInWeek - 7];
				DateString = DateString.replace("day_in_week",calendar.dayNamesF[DayInWeek - 7]);
			}
		}
		calendar.DateString += DateString;
		i++;
		if (i < calendar.dateMask.length) {
			calendar.DateString += calendar.dateMask[i];
		}
	}

	calendar.newWindow.document.CalendarForm.startdate.value = calendar.DateString;
	var elem = calendar.newWindow.document.getElementById(calendar.currentSelection);
	if (elem != null) {
		if (calendar.weekday) {
			elem.className="CAL_XXS_MAIN";
			calendar.newWindow.document.getElementById(calendar.currentSelection+"_cell").className="CAL_XXS_MAIN";
		} else {
			elem.className="CAL_XXS_WEKND";
			calendar.newWindow.document.getElementById(calendar.currentSelection+"_cell").className="CAL_XXS_WEKND";
		}
	}
	if (weekday) {
		calendar.weekday= true;
	} else {
		calendar.weekday = false;
	}
	calendar.currentSelection=Year+""+(Month-1)+"" +Day;
	calendar.newWindow.document.getElementById(calendar.currentSelection).className="CAL_XXS_TODAY";
	calendar.newWindow.document.getElementById(calendar.currentSelection+"_cell").className="CAL_XXS_TODAY";
	//calendar.newWindow.document.getElementById("Test111").innerHTML='<TD class="CAL_XXS_DIS">11</TD>';
}

function WEFReturnDate(hours, mins, ampms) {
	if (calendar.DateString == "") {
		WEFCalClose();
		return;
	}
	if (calendar.timeMask == null) { //InputField.DATE
		var elem = eval("document." + calendar.formID + "['" + calendar.fieldID + "']" );
		elem.value = calendar.DateString;
		elem.focus();
	} else {
		
		if (calendar.InputField != null) { //InputField.DATETIME
			WEFCreateTimeString(hours, mins, ampms, true);
			//this.document.getElementById(calendar.fieldID).value = DateString;
			var elem = eval("document." + calendar.formID + "['" + calendar.fieldID + "']" );
			elem.value = calendar.TimeString;
			elem.focus();
		} else {//DateTimeComponent
			WEFCreateTimeString(hours, mins, ampms, false);
			var elem = eval("document." + calendar.formID + "['" + calendar.fieldID + "_date']" );
			elem.value = calendar.DateString;
			var elem1 = eval("document." + calendar.formID + "['" + calendar.fieldID + "_time']" );
			elem1.value = calendar.TimeString;
			elem.focus();
		}
	}
	WEFCalClose();
}

function WEFHelpTime(CalendarId, AMPM, spacer, timeZone, timeMask,  lowLimit, upperLimit, CssAttributes, FormName, localeData ){
	
	if (calendar == null)
		calendar = new WEFCalendar();
	calendar.closeWindow = localeData[4];
	calendar.time_title = localeData[5];
	calendar.select = localeData[7];
	calendar.cancel = localeData[8];
	calendar.formID = FormName;
	calendar.fieldID = CalendarId;
	calendar.timeMask = timeMask;
	calendar.lowLimit = lowLimit;
	calendar.upperLimit = upperLimit;
	calendar.css = CssAttributes;
	calendar.ampm = AMPM;
	calendar.spacer=spacer;
	calendar.timeZone=timeZone;
	calendar.InputField = "1";
	calendar.newWindow=window.open("",calendar.formID,"width=300, height=140, resizable=1;");
	HTML_String = WEFMakeTime();
	WEFWriteCalendar(HTML_String);

}


function WEFMakeTime() {

	var HTML_String = "";
	HTML_String += '<html>\n';
	HTML_String += '<head>\n';
	HTML_String += '<title>' + calendar.time_title + '</title>\n';
	for (i=0; i < calendar.css.length; i++) {
		HTML_String +="<LINK REL=\"stylesheet\" HREF=\"" + calendar.css[i] + "\" TYPE=\"text/css\">\n";
	}
	HTML_String += '<script>\n';
	HTML_String += 'function setDomain() {\n';
	HTML_String += 'var lnDotPos = document.domain.indexOf( \".\" );\n';
	HTML_String += 'if (lnDotPos >= 0) document.domain = document.domain.substr( lnDotPos + 1 );\n';
	HTML_String += '}\n';
	HTML_String += '</script>\n';
	//var Netscape6=(navigator.appName=="Netscape" && navigator.appVersion.substring(0,1) == "5")?" onblur='window.close()'":"";
	HTML_String += '</head>\n';
	HTML_String += '<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onLoad="setDomain()">\n';
    HTML_String += '<form name="TimeForm">\n';
	HTML_String += '<center>\n';
	HTML_String += '<table cellpadding="0" cellspacing="0" border="0" width="250">\n';
	HTML_String += '<tr><td>\n';
	HTML_String += '<table cellpadding="0" cellspacing="0" border="0"><tr><td><img src="' + calendar.spacer + '" style="height:6px; width:1px"></td></tr></table>\n';
	HTML_String += '<table cellpadding="0" cellspacing="0" border="0" class="TBDATA_BDR_BG" width="100%">\n';
	HTML_String += '<tr>\n';
	HTML_String += '<td align="center">\n';
	HTML_String += '<table cellpadding="2" cellspacing="1" border="0">\n';
	HTML_String += '<TR>\n';
	HTML_String += '<td class="TBDATA_HEAD" width="100%">' + calendar.time_title + '</td>\n';
	HTML_String += '<td class="TBDATA_HEAD"><a href=\"javascript:window.opener.WEFCalClose()\">\n';
	HTML_String += '<img src="' + calendar.spacer + '" class="IMG_CLOSEX" style="border:0" alt="' + calendar.closeWindow + '" title="' + calendar.closeWindow + '" ></a></td>\n';
    HTML_String += '</TR>\n';
	HTML_String += '</table>\n';
    HTML_String += '</td>\n';
	HTML_String += '</tr>\n';
	HTML_String += '</table>\n';

	HTML_String += '<table cellpadding="0" cellspacing="0" border="0"><tr><td><img src="' + calendar.spacer + '" style="height:15px; width:1px"></td></tr></table>\n';
   	HTML_String += '<table cellpadding="0" cellpadding="0" border="0" align="center">\n';
	HTML_String += '<tr>\n';
	HTML_String += '<td class="TBLO_XXS_C" nowrap>' + calendar.time_title + ' :</TD>\n';
	HTML_String += '<td class="DROPDOWN_XS">&nbsp;<select name="hours" class="DROPDOWN_XS">\n';
	for (var i =calendar.lowLimit; i <= calendar.upperLimit; i++) {
		var hr = new String(i);
		if (i < 10) {
			hr = "0" + hr;
		}

		if (i != 12) {
			HTML_String +='<option value="' + hr +'">' + hr + '</option>\n';
		} else {
			HTML_String +='<option value="' + hr +'" selected>' + hr + '</option>\n';
		}
	}
	HTML_String += '</select>\n';

	HTML_String += '</td>\n';
	HTML_String += '<Td>&nbsp;</TD>\n';
	HTML_String += '<td class="DROPDOWN_XS"><select name="minutes" class="DROPDOWN_XS">\n';
	for (var i =0; i <= 59; i++) {
		var min = new String(i);
		if (i < 10) {
			min = "0" + min;
		}
		HTML_String +='<option value="' + min +'">' + min + '</option>\n';
	}

	HTML_String += '</select>\n';
	HTML_String += '</td>\n';

	if (calendar.ampm != null) { // not mil time
		HTML_String += '<td>&nbsp;</td>\n';
		HTML_String += '<td class="DROPDOWN_XS"><select name="ampmmark" class="DROPDOWN_XS">\n';
		for (i=0; i < 2; i++) {
			HTML_String += '<option value="' + calendar.ampm[i] + '">' + calendar.ampm[i] + '</option>\n';
		}
		HTML_String += '</select>&nbsp;' + calendar.timeZone + '\n';

	} else {
		HTML_String += '<td>&nbsp;' + calendar.timeZone + '\n';
	}
	HTML_String += '</td>\n';	

	HTML_String += '</tr>\n';

	HTML_String += '</table>\n';
	HTML_String += '</td>\n';
	HTML_String += '</tr>\n';
	HTML_String += '<tr>\n';
	HTML_String += '<td><img src="' + calendar.spacer + '" style="height:7px; width:1px"></td>\n';
	HTML_String += '</tr>\n';
	HTML_String += '<tr>\n';
	HTML_String += '<td><input class="BTN_S" type="button" value="&nbsp;' + calendar.select + '&nbsp;"';
	if (calendar.ampm != null) { // not mil time
		HTML_String += 'onClick="javascript:window.opener.WEFReturnTime(hours.options ,minutes.options, ampmmark.options )">&nbsp;\n';
	} else  {
		HTML_String += 'onClick="javascript:window.opener.WEFReturnTime(hours.options ,minutes.options, null )">&nbsp;\n';
	} 
	HTML_String += '<input class="BTN_S" type="button" value="&nbsp;' + calendar.cancel + '&nbsp;"  onClick="javascript:window.opener.WEFCalClose()"></td>\n';
	HTML_String += '</tr>\n';
	
	HTML_String += '</table>\n';

	HTML_String +='</center>\n';
	HTML_String +='</form>\n';
	HTML_String +='</body>\n';
	HTML_String +='</html>\n';

	return HTML_String;

}

function WEFReturnTime(hours, mins, ampms) {
	WEFCreateTimeString(hours, mins, ampms, false);	
	var elem = eval("document." + calendar.formID + "['" + calendar.fieldID + "']" );
	elem.value = calendar.TimeString;
	elem.focus();
	WEFCalClose();
}

function WEFCreateTimeString(hours, mins, ampms, datetime) {
	var hour = null;
	var min = null;
	var ampm = null;
	for (i = 0; i < hours.length; i++) {
		if (hours[i].selected) {
			hour = hours[i].value;
			break;
		}
	}
	for (i = 0; i < mins.length; i++) {
		if (mins[i].selected) {
			min = mins[i].value;
			break;
		}
	}
	if (ampms != null) {
		for (i = 0; i < ampms.length; i++) {
			if (ampms[i].selected) {
				ampm = ampms[i].value;
				break;
			}
		}
	}
	calendar.TimeString = "";
	for (i=0; i < calendar.timeMask.length; i++) {
		var TimeString = calendar.timeMask[i];
		if (datetime) {
			var match1 = /month_in_year_one|month_in_year_two|month_in_year/;
			var match2 = /day_in_month_full|day_in_month/;
			var match3 = /year_full|year/;
			TimeString = TimeString.replace(match1, calendar.selectMonth);
			TimeString = TimeString.replace(match2, calendar.selectDay);
			TimeString = TimeString.replace(match3, calendar.selectYear);
			if (calendar.era != null) {
				TimeString = TimeString.replace("era_designator", calendar.era);
			}
			if (calendar.selectDayInWeek != null) {
				TimeString = TimeString.replace("day_in_week", calendar.selectDayInWeek);
			}

		}
		if (TimeString.indexOf("minute_in_hour_full") >= 0) {
			TimeString= TimeString.replace("minute_in_hour_full", min);
		} else if (TimeString.indexOf("minute_in_hour") >= 0 ) {
			if (min.indexOf("0") == 0) {
				TimeString= TimeString.replace("minute_in_hour", min.substring(1));
			} else {
				TimeString= TimeString.replace("minute_in_hour", min);
			}
		}
		if (TimeString.indexOf("hour_in_day_full") >= 0) {
			TimeString= TimeString.replace("hour_in_day_full", hour);
		} else if (TimeString.indexOf("hour_in_day") >= 0 ) {
			if (hour.indexOf("0") == 0) {
				TimeString= TimeString.replace("hour_in_day", hour.substring(1));
			} else {
				TimeString= TimeString.replace("hour_in_day", hour);
			}
		} 
		
		TimeString = TimeString.replace("ampm_marker", ampm);
		TimeString = TimeString.replace("time_zone", calendar.timeZone);
		if (TimeString.indexOf("second_in_minute_full") >= 0 ) {
			TimeString= TimeString.replace("second_in_minute_full", "00");
		} else if (TimeString.indexOf("second_in_minute") >= 0 ) {
			TimeString= TimeString.replace("second_in_minute", "0");
		}
				
		calendar.TimeString += TimeString;
		i++;
		if (i < calendar.timeMask.length) {
			calendar.TimeString += calendar.timeMask[i];
		}
	}
			
}
	
function WEFNumberOfRows(daysInMonth, dayOfWeek) {

	var relDow = (dayOfWeek + 7 - calendar.firstDay) % 7; // 0..6
	var days = daysInMonth;
	var rows = 0;
	if (relDow != 0) {
		rows =1;
		days=days - (7 - relDow);
	}
	rows += Math.ceil(days/7);
	return rows;
}
