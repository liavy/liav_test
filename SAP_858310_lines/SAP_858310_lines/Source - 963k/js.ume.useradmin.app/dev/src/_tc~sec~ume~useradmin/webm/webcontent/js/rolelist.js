function setSelect(slct) {
	for(var i=0; i<slct.options.length; i++) {
		if ( slct.options[i].value != "") {
			slct.options[i].selected = true;
		} else {
			slct.options[i].selected = false;
		}
	}
	return true;	
}


function add(direction) 
{
  var i, j, k, n;
  var slctMyNews, slctAvNews; 
  var textArray = new Array();
  var valueArray = new Array();
  var selArray = new Array();
  var ltextArray = new Array();
  var lvalueArray = new Array();
  var totalSelected;
  var totalLeft;
  var frm = document.forms[0];
  if ( direction == "left" ) {
  	slctMyNews = frm.elements["assignedRoles"];
  	slctAvNews = frm.elements["availableRoles"];
  } else {
  	slctMyNews = frm.elements["availableRoles"];
  	slctAvNews = frm.elements["assignedRoles"];	
  }


  if (slctAvNews.options.selectedIndex<0) return;

  j=0;
  n=0;
  for (i=0; i<slctAvNews.options.length; i++) {
    if (slctAvNews.options[i].selected) {
      contains = false;	
      for (k=0; k<slctMyNews.options.length; k++) {
      	if ( slctMyNews.options[k].text == slctAvNews.options[i].text ) {
      		contains = true;
    	} else {
    		continue;
    	}
      }		
      if ( !contains ) {
          selArray[j++] = i;
      } else {
        ltextArray[n] = slctAvNews.options[i].text;
        lvalueArray[n] = slctAvNews.options[i].value;
      }
    } else {
      ltextArray[n] = slctAvNews.options[i].text;
      lvalueArray[n]= slctAvNews.options[i].value;
      n++;
    }
  } 
  totalSelected = j;
  totalLeft = n;

  for (i=0; i<slctMyNews.options.length; i++) {
    textArray[i] = slctMyNews.options[i].text;
    valueArray[i] = slctMyNews.options[i].value;
  }

  slctMyNews.selectedIndex = -1;
  slctMyNews.options.length = 0;
  for (i=0; i<totalSelected; i++) {
    slctMyNews.options[i] = new Option(slctAvNews.options[selArray[i]].text, slctAvNews.options[selArray[i]].value);
  }

  for (i=0; i<textArray.length; i++) {
    slctMyNews.options[i+totalSelected] = new Option(textArray[i], valueArray[i]);
  }
  slctMyNews.selectedIndex = 0;

  /*
  for (i=0; i<slctAvNews.options.length; i++) {
    if (slctAvNews.options[i].selected) {
      slctAvNews.options[i] = null;
    }
  }
  */
  slctAvNews.selectedIndex = -1;
  slctAvNews.options.length = 0;
  for (i=0; i<totalLeft; i++) {
    slctAvNews.options[i] = new Option(ltextArray[i], lvalueArray[i]);
  }

  slctAvNews.selectedIndex = (selArray[totalSelected-1]<slctAvNews.options.length) ? 
                             selArray[totalSelected-1] : (slctAvNews.options.length-1);
}

function addAll()
{
  var frm = document.forms[0];
  var slctMyNews = frm.elements["assignedRoles"];
  var slctAvNews = frm.elements["availableRoles"];
  var textArray = new Array();
  var valueArray = new Array();
  var notyetArray = new Array();
  
  var j=0;
  for (var i=0; i<slctAvNews.options.length; i++) {
      contains = false;	
      for (k=0; k<slctMyNews.options.length; k++) {
      	if ( slctMyNews.options[k].text == slctAvNews.options[i].text ) {
      		contains = true;
    	} else {
    		continue;
    	}
      }		
      if ( !contains ) {
          notyetArray[j++] = i;
      }
  }
  totalnotyet = j;
  
  for (i=0; i<slctMyNews.options.length; i++) {
    textArray[i] = slctMyNews.options[i].text;
    valueArray[i] = slctMyNews.options[i].value;
  }

  slctMyNews.selectedIndex = -1;
  slctMyNews.options.length = 0;
  for (i=0; i<totalnotyet; i++) {
    slctMyNews.options[i] = new Option(slctAvNews.options[notyetArray[i]].text, slctAvNews.options[notyetArray[i]].value);
  }

  for (i=0; i<textArray.length; i++) {
    slctMyNews.options[i+totalnotyet] = new Option(textArray[i], valueArray[i]);
  }
  slctMyNews.selectedIndex = 0;
  
  slctAvNews.selectedIndex = -1;
  slctAvNews.options.length = 0;
}

function removeAll()
{
  var frm = document.forms[0];
  var slctMyNews = frm.elements["assignedRoles"];
  var slctAvNews = frm.elements["availableRoles"];
  var textArray = new Array();
  var valueArray = new Array();
  var notyetArray = new Array();
  
  var j=0;
  for (var i=0; i<slctMyNews.options.length; i++) {
      contains = false;	
      for (k=0; k<slctAvNews.options.length; k++) {
      	if ( slctAvNews.options[k].text == slctMyNews.options[i].text ) {
      		contains = true;
    	} else {
    		continue;
    	}
      }		
      if ( !contains ) {
          notyetArray[j++] = i;
      }
  }
  totalnotyet = j;
  
  for (i=0; i<slctAvNews.options.length; i++) {
    textArray[i] = slctAvNews.options[i].text;
    valueArray[i] = slctAvNews.options[i].value;
  }

  slctAvNews.selectedIndex = -1;
  slctAvNews.options.length = 0;
  for (i=0; i<totalnotyet; i++) {
    slctAvNews.options[i] = new Option(slctMyNews.options[notyetArray[i]].text, slctMyNews.options[notyetArray[i]].value);
  }

  for (i=0; i<textArray.length; i++) {
    slctAvNews.options[i+totalnotyet] = new Option(textArray[i], valueArray[i]);
  }
  slctAvNews.selectedIndex = 0;
  
  slctMyNews.selectedIndex = -1;
  slctMyNews.options.length = 0;
}

function move(direction)
{
  var i;
  var frm = document.forms[0];
  var slctMyNews = frm.elements["assignedRoles"];
  var slctAvNews = frm.elements["availableRoles"];

  if (slctMyNews.selectedIndex==-1) return;

  var selArray = new Array();
  var totalSelected;

  for (i=0; i<slctMyNews.options.length; i++) {
    if (slctMyNews.options[i].selected) {
      selArray[selArray.length] = i;
    }
  } 
  totalSelected = selArray.length;

  var skip = false;
  var skipEnd;
  if (direction==-1) {
    skipEnd = 0;
    if (selArray[0]==0) {
      skip = true;
    } 
    for (i=0; i<totalSelected; i++) {
      if (skip && i>0 && (selArray[i]-selArray[i-1]>1)) {
        skip = false;
        skipEnd = i;
      }
      if (!skip) {
        moveIt(-1,selArray[i]);
      }
    }
    for (i=0; i<skipEnd; i++) {
      slctMyNews.options[selArray[i]].selected = true;
    }
    for (i=skipEnd; i<totalSelected; i++) {
      if (selArray[i]+direction>0) {
        slctMyNews.options[selArray[i]+direction].selected = true;
      } else {
        slctMyNews.options[0].selected = true;
      }
    }
  } else {
    if (selArray[totalSelected-1]==slctMyNews.options.length-1) {
      skip = true;
    }
    skipEnd = totalSelected-1;
    for (i=totalSelected-1; i>=0; i--) {      
      if (skip && i<(totalSelected-1) && (selArray[i+1]-selArray[i]>1)) {
        skip = false;
        skipEnd = i;
      }
      if (!skip) {
        moveIt(1,selArray[i]);
      }
    }
    for (i=totalSelected-1; i>skipEnd; i--) {
      slctMyNews.options[selArray[i]].selected = true;
    }
    for (i=skipEnd; i>=0; i--) {
      if (selArray[i]+direction<slctMyNews.options.length) {
        slctMyNews.options[selArray[i]+direction].selected = true;
      } else {
        slctMyNews.options[slctMyNews.options.length-1].selected = true;
      }
    }
  }
}

function moveIt(direction, selectedIndex) 
{
  var i;
  var frm = document.forms[0];
  var slctMyNews = frm.elements["assignedRoles"];
  var slctAvNews = frm.elements["availableRoles"];
  var textArray = new Array();

  if (selectedIndex==0 && direction==-1) return;
  if (selectedIndex==slctMyNews.options.length-1 && direction==1) return;
  for (i=0; i<slctMyNews.options.length; i++) {
    textArray[i] = slctMyNews.options[i].text;
  }
  swapItemsInArray(textArray, selectedIndex, selectedIndex+direction);
  slctMyNews.selectedIndex = -1;
  slctMyNews.options.length = 0;
  for (i=0; i<textArray.length; i++) {
    slctMyNews.options[i] = new Option(textArray[i]);
  }
}

function swapItemsInArray(array, i1, i2)
{
  if (i1<0 || i2<0 || i1>array.length-1 || i2>array.length-1) return array;
  var temp = array[i1];
  array[i1] = array[i2];
  array[i2] = temp;
  return array;
}

function sort(slct)
{
  var i,j;
  var frm = document.forms[0];
  var slctSort;
  var textArray = new Array();

  if (slct=="AvNews") {
    slctSort = frm.elements["availableRoles"];
  } else {
    slctSort = frm.elements["assignedRoles"];
  }
  
  if (slctSort.options.length<2) return;
  for (i=0; i<slctSort.options.length; i++) {
    textArray[i] = slctSort.options[i].text;
  }
  for (i=0; i<slctSort.options.length; i++) {
    for (j=i+1; j<slctSort.options.length; j++) {
      if (textArray[i].toUpperCase()>textArray[j].toUpperCase()) {
        swapItemsInArray(textArray,i,j);
      }
    }
  }
  slctSort.selectedIndex = -1;
  slctSort.options.length = 0;
  for (i=0; i<textArray.length; i++) {
    slctSort.options[i] = new Option(textArray[i]);
  }
  slctSort.selectedIndex = 0;
}


function validate() {
  var i, j;
  var frm = document.forms[0];
  var slctMyNews = frm.elements["assignedRoles"];
  var slctAvNews = frm.elements["availableRoles"];
  var textArray = new Array();
  var lastSelected;

  if (slctMyNews.options.selectedIndex<0) {
alert('Please choose at least one role from the list on the left.');
event.returnValue=false;
}
}


