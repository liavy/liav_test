<%@ include file="/proxy.txt" %>             
<% if ( util.isServlet23() ) proxy.setResponseContentType("text/html; charset=utf-8"); %>
             
<% java.util.Locale locale = proxy.getLocale();
   if ( null == locale ) locale = java.util.Locale.getDefault();
   DateUtil dateU = new DateUtil(locale); 
   String previousMonths = userAdminLocale.get("PREVIOUS_3_MONTHS");
   String nextMonths = userAdminLocale.get("NEXT_3_MONTHS");
   String closeWin = userAdminLocale.get("CLOSE_WIN");
   String formID = (String) proxy.getRequestAttribute("formID");
   String fieldID = (String) proxy.getRequestAttribute("fieldID");
%> 

<% if ( !inPortal ) { %>
<html>
<head>
<title><%=userAdminLocale.get("CALENDAR")%></title>
<script language="JavaScript" src="js/basic.js"></script>
<LINK REL="stylesheet" HREF="<%=webpath%>css/calendar/images.css" type="text/css">
<% if ( request.getHeader("User-Agent").indexOf("MSIE") > 0 ) { %>
<LINK REL="stylesheet" HREF="<%=webpath%>css/calendar/main_ie.css" type="text/css">
<% } else { %>
<LINK REL="stylesheet" HREF="<%=webpath%>css/calendar/main_ns.css" type="text/css">
<% } %>
<% } %>

<script>
function WEFCalendar() {
    this.currentDate = new Date();			// Thu Nov 1 11:54:00 PST 2001
    this.currentYear = this.currentDate.getFullYear();  // 2001
    this.currentDay = this.currentDate.getDay();		// 4 - Thursday
    this.currentMonth = this.currentDate.getMonth();	// 10
    this.today = this.currentDate.getDate();		//1
    this.daysInMonth = new Array(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
    this.timeMask = null;
    this.lowLimit = null;
    this.upperLimit = null;
    this.dayNamesF = null;
    this.monthNamesF = null;
    this.ampm = null;
    this.era = null;
    this.timeZone=null;
    this.InputField = "1";
    this.firstDay = "1";
    this.minDaysInFirstWeek = "1";
    this.DateString = "";
    this.weekAbbreviation = "<%=userAdminLocale.get("WK")%>";
    this.select = "<%=userAdminLocale.get("SELECT")%>";
    this.dayNames = new Array('<%=userAdminLocale.get("SU")%>',
                          '<%=userAdminLocale.get("MO")%>',
                          '<%=userAdminLocale.get("TU")%>',
                          '<%=userAdminLocale.get("WE")%>',
                          '<%=userAdminLocale.get("TH")%>',
                          '<%=userAdminLocale.get("FR")%>',
                          '<%=userAdminLocale.get("SA")%>');
    this.monthNames = new Array('<%=userAdminLocale.get("JAN")%>',
                            '<%=userAdminLocale.get("FEB")%>',
                            '<%=userAdminLocale.get("MAR")%>',
                            '<%=userAdminLocale.get("APR")%>',
                            '<%=userAdminLocale.get("MAY")%>',
                            '<%=userAdminLocale.get("JUN")%>',
                            '<%=userAdminLocale.get("JUL")%>',
                            '<%=userAdminLocale.get("AUG")%>',
                            '<%=userAdminLocale.get("SEP")%>',
                            '<%=userAdminLocale.get("OCT")%>',
                            '<%=userAdminLocale.get("NOV")%>',
                            '<%=userAdminLocale.get("DEC")%>');  
    this.dateMask = new Array('<%=dateU.getDateDisplayFormat()%>');                            
}
var calendar = null;

function createCalendar() {
    if (calendar == null)
        calendar = new WEFCalendar();
    calendar.currentSelection=calendar.currentYear+""+calendar.currentMonth+"" + calendar.today;
    calendar.formID = "<%=com.sap.security.core.util.taglib.EncodeHtmlTag.encode(formID)%>";
    calendar.fieldID = "<%=com.sap.security.core.util.taglib.EncodeHtmlTag.encode(fieldID)%>";    
    HTML_String = WEFMakeCal(0);
    WEFWriteCalendar(HTML_String);    
}

function WEFSkipCal(month) {
    HTML_String = WEFMakeCal(month);
    WEFWriteCalendar(HTML_String);
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
    window.close();
}

function WEFWriteCalendar(HTML_stuff){
    document.body.innerHTML = HTML_stuff;
    window.focus();
}

function WEFMakeCal(startMonth) {
    var HTML_String = "";
    HTML_String += '<form name="CalendarForm">';
    HTML_String += '<center>';
    HTML_String += '<table cellpadding="0" cellspacing="0" border="0" width="140">';
    HTML_String += '<tr><td>';
    HTML_String += '<table cellpadding="0" cellspacing="0" border="0">';
    HTML_String += '<tr><td><img src="<%=webpath%>layout/sp.gif" style="height:6px; width:1px"></td></tr>';
    HTML_String += '</table>';
    HTML_String += '<table cellpadding="0" cellspacing="0" border="0" class="TBDATA_BDR_BG" width="100%">';
    HTML_String += '<tr><td align="center">';
    HTML_String += '<table cellpadding="2" cellspacing="1" border="0">';
    HTML_String += '<TR>';
    HTML_String += '<td class="TBDATA_HEAD">';
    HTML_String += '<a href=\"javascript:WEFSkipCal('+(startMonth - 3) + ')\">';
    HTML_String += '<img src="<%=webpath%>layout/caldup.gif" tabindex="0" style="border:0" width="13" height="14" alt="<%=previousMonths%>" title="<%=previousMonths%>">';
    HTML_String += '</a>';
    HTML_String += '</td>';
    HTML_String += '<td class="TBDATA_HEAD">';
    HTML_String += '<a href=\"javascript:WEFSkipCal('+(startMonth + 3) + ')\">';
    HTML_String += '<img src="<%=webpath%>layout/caldown.gif" tabindex="0" style="border:0" width="13" height="14" alt="<%=nextMonths%>" title="<%=nextMonths%>">';
    HTML_String += '</a>';
    HTML_String += '</td>';
    HTML_String += '<td class="TBDATA_HEAD" width="100%"><span tabindex="0"><%=userAdminLocale.get("CALENDAR")%></span></td>';
    HTML_String += '<td class="TBDATA_HEAD">';
    HTML_String += '<a href=\"javascript:WEFCalClose()\">';
    HTML_String += '<img src="<%=webpath%>layout/closex.gif" tabindex="0" style="border:0" width="13" height="14" alt="<%=closeWin%>" title="<%=closeWin%>">';
    HTML_String += '</a>';
    HTML_String += '</td>';
    HTML_String += '</TR>';
    HTML_String += '</table>';
    HTML_String += '</td></tr>';
    HTML_String += '</table>';
    HTML_String += '</td></tr>';
    HTML_String += '</table>';

  // start calendars
    HTML_String += '<table cellpadding="0" cellspacing="0" border="0">';
    HTML_String += '<tr><td><img src="<%=webpath%>layout/sp.gif" style="height:6px; width:1px"></td></tr>';
    HTML_String += '</table>';
    HTML_String += '<table cellpadding="0" cellspacing="0" border="0" class="TBDATA_BDR_BG" align="center">';
    HTML_String += '<tr>';
    HTML_String +=WEFMakeMonth(startMonth - 1, false);
    HTML_String +='</tr>';
    HTML_String += '<tr>';
    HTML_String +=WEFMakeMonth(startMonth, true);
    HTML_String +='</tr>';
    HTML_String += '<tr>';
    HTML_String +=WEFMakeMonth(startMonth + 1, false);
    HTML_String +='</tr>';
    HTML_String += '</table>';
  //finish callendars
  
    HTML_String += '<table cellpadding="0" cellspacing="0" border="0" width="140">';
    HTML_String += '<tr><td>';
    HTML_String += '<table cellpadding="0" cellspacing="0" border="0">';
    HTML_String += '<tr><td><img src="<%=webpath%>layout/sp.gif" style="height:6px; width:1px"></td></tr>';
    HTML_String += '</table>';
    HTML_String += '<table cellpadding="0" cellspacing="0" border="0" class="TBDATA_BDR_BG" width="100%">';
    HTML_String += '<tr><td align="center">';
    HTML_String += '<table cellpadding="2" cellspacing="1" border="0">';
    HTML_String += '<TR>';
    HTML_String += '<td class="TBDATA_HEAD">';
    HTML_String += '<a href=\"javascript:WEFSkipCal('+(startMonth - 3) + ')\">';
    HTML_String += '<img src="<%=webpath%>layout/caldup.gif" tabindex="0" style="border:0" width="13" height="14" alt="<%=previousMonths%>" title="<%=previousMonths%>">';
    HTML_String += '</a>';
    HTML_String += '</td>';
    HTML_String += '<td class="TBDATA_HEAD">';
    HTML_String += '<a href=\"javascript:WEFSkipCal('+(startMonth + 3) + ')\">';
    HTML_String += '<img src="<%=webpath%>layout/caldown.gif" tabindex="0" style="border:0" width="13" height="14" alt="<%=nextMonths%>" title="<%=nextMonths%>">';
    HTML_String += '</a>';
    HTML_String += '</td>';
    HTML_String += '<td class="TBDATA_HEAD" width="100%"><span tabindex="0"><%=userAdminLocale.get("CALENDAR")%></span></td>';
    HTML_String += '<td class="TBDATA_HEAD">';
    HTML_String += '<a href=\"javascript:WEFCalClose()\">';
    HTML_String += '<img src="<%=webpath%>layout/closex.gif" tabindex="0" style="border:0" width="13" height="14" alt="<%=closeWin%>" title="<%=closeWin%>">';
    HTML_String += '</a>';
    HTML_String += '</td>';
    HTML_String += '</TR>';
    HTML_String += '</table>';
    HTML_String += '</td></tr>';
    HTML_String += '</table>';
    HTML_String += '</td></tr>';
    HTML_String += '</table>';
    
    HTML_String += '</td></tr>';
    HTML_String += '</table>';

    HTML_String += '</center>';
    HTML_String += '</form>';
    return HTML_String;
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

function WEFMakeMonth(number, firstMonth) {
    var floor = Math.floor((calendar.currentMonth + number)/12);
    var Month = (calendar.currentMonth + number) - floor*12;   //0-11
    var Year = calendar.currentYear + floor;
    var First_Date = new Date(Year, Month, 1); 

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
	tmp +='<td align="center">';
	tmp +='<table cellpadding="1" cellspacing="1" border="0">';
	tmp +='<TR class="TBDATA_CNT_ODD_BG">';
	tmp +='<TD colspan="8" class="CAL_XXS_MAINB">'+ Heading + '<img src="<%=webpath%>layout/sp.gif" style="height:1px; width:1px"></TD>';
	tmp +='</TR>';
	tmp +='<TR>';
	tmp +='<TD scope="col" class="TBDATA_HEAD">' + calendar.weekAbbreviation+ '</TD>';
	for (i =0; i <7; i++) {
	    tmp +='<TD scope="col" tabindex="0" class="TBDATA_HEAD">'+ calendar.dayNames[i] + '</TD>';
	}
	tmp +='</TR>';
	
	var counter = 1;
	var dayCounter = calendar.firstDay;
	var newMonthCounter = 0;
	var limit = Days;
	if (extraRow) {
		limit = Days + 7;
	}
	while(counter <= limit) {
		tmp +='<TR class="SIDE_CNT_BG">';
		tmp +='<TD scope="row" class="TBLO_XXS_C">' + WeekOfYear + '</TD>';
		for (j=1; j< 8; j++) {
			if (dayCounter < First_Day ) {
				dayCounter++;
				if ((calendar.firstDay==1 && j ==1) || (j+calendar.firstDay) == 8 || (j+calendar.firstDay) == 9) {
					tmp +='<TD tabindex="0" class="CAL_XXS_WEKND_DIS">'+ (Prev_Days - First_Day + dayCounter) +'</TD>';
				} else {
					tmp +='<TD tabindex="0" class="CAL_XXS_DIS">'+ (Prev_Days - First_Day + dayCounter) +'</TD>';
				}
			} else if (counter <= Days){
				 if ((calendar.firstDay==1 && j ==1) || (j+calendar.firstDay) == 8 || (j+calendar.firstDay) == 9) {
				 	if (Year == calendar.currentYear && Month == calendar.currentMonth && counter == calendar.today) {
						tmp +='<TD tabindex="0" ID="'+Year+""+Month+""+counter+'" class="CAL_XXS_TODAY"><a ID="'+Year+""+Month+""+counter+'_cell" class="CAL_XXS_TODAY" href=\"\" onClick=\"javascript:WEFReturnDate(' + Year+ ',' + (Month + 1) + ',' + counter + ',' + (calendar.firstDay + j - 2)  + ',' + false + ')\">' + counter+ '</a></TD>';
						calendar.weekday=false;
					} else {
						tmp +='<TD tabindex="0" ID="'+Year+""+Month+""+counter+'" class="CAL_XXS_WEKND"><a ID="'+Year+""+Month+''+counter+'_cell" class="CAL_XXS_WEKND" href=\"\" onClick=\"javascript:WEFReturnDate(' + Year+ ',' + (Month + 1) + ',' + counter +',' + (calendar.firstDay + j - 2)    + ',' + false + ')\">' + counter+ '</a></TD>';
					}

				} else {
					if (Year == calendar.currentYear && Month == calendar.currentMonth && counter == calendar.today) {
						tmp +='<TD tabindex="0" ID="'+Year+""+Month+""+counter+'" class="CAL_XXS_TODAY"><a ID="'+Year+""+Month+""+counter+'_cell" class="CAL_XXS_TODAY" href=\"\" onClick=\"javascript:WEFReturnDate(' + Year+ ',' + (Month + 1) + ',' + counter + ',' + (calendar.firstDay + j - 2)  + ',' + true + ')\">' + counter+ '</a></TD>';
						calendar.weekday=true;
					} else {
						tmp +='<TD tabindex="0" ID="'+Year+""+Month+""+counter+'" class="CAL_XXS_MAIN"><a ID="'+Year+""+Month+""+counter+'_cell" class="CAL_XXS_MAIN" href=\"\" onClick=\"javascript:WEFReturnDate(' + Year+ ',' + (Month + 1) + ',' + counter +',' + (calendar.firstDay + j - 2)   + ',' + true + ')\">' + counter+ '</a></TD>';

					}
				}
				counter++;
			} else {
				newMonthCounter++;
				counter++;
				if ((calendar.firstDay==1 && j ==1) || (j+calendar.firstDay) == 8 || (j+calendar.firstDay) == 9) {
					tmp +='<TD tabindex="0" class="CAL_XXS_WEKND_DIS">'+ newMonthCounter +'</TD>';
				} else {
					tmp +='<TD tabindex="0" class="CAL_XXS_DIS">'+ newMonthCounter +'</TD>';
				}
			}
		}
		WeekOfYear++;
		tmp +='</TR>';
	}
	tmp +='</table>';
	tmp +='</td>';

	return tmp;
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

	var elem = document.getElementById(calendar.currentSelection);
	if (elem != null) {
		if (calendar.weekday) {
			elem.className="CAL_XXS_MAIN";
			document.getElementById(calendar.currentSelection+"_cell").className="CAL_XXS_MAIN";
		} else {
			elem.className="CAL_XXS_WEKND";
			document.getElementById(calendar.currentSelection+"_cell").className="CAL_XXS_WEKND";
		}
	}
	if (weekday) {
		calendar.weekday= true;
	} else {
		calendar.weekday = false;
	}
	calendar.currentSelection=Year+""+(Month-1)+"" +Day;
	document.getElementById(calendar.currentSelection).className="CAL_XXS_TODAY";
	document.getElementById(calendar.currentSelection+"_cell").className="CAL_XXS_TODAY";
}

function WEFReturnDate(Year, Month, Day, DayInWeek, weekday) {
    <% if ( null == proxy.getSessionAttribute("UMESESSION")  ) { %>
        calendar.DateString == "";
    <% } %>
    
    WEFCreateDate(Year, Month, Day, DayInWeek, weekday);
    
	if (calendar.DateString == "") {
		WEFCalClose();
		return;
	}
	var elem = eval("window.opener."+"document." + calendar.formID + "['" + calendar.fieldID + "']" );
	elem.value = calendar.DateString;
	elem.focus();
	WEFCalClose();
}
</script>

<% if ( !inPortal ) { %>
</head>
<% } %>

<<% if ( !inPortal ) { %>
<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="createCalendar();">
</body>
</html>
<% } else { %>
<script>
	//createCalendar();
	EPCM.subscribeEvent( "urn:com.sapportals.portal:browser", "load", createCalendar);
</script>
<% } %>