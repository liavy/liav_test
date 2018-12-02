function init(formname,source,target)
{
   var form = formname;

   sort(form,source);
   sort(form,target);

   //updateButtons(f,source,target);
}

function setCmd(form, value)
{
  form.cmd.value = value;
}


function updateButtons(form,source,target)
{
/*
   var f = form;

   if ( ((source = 'allActions') || (source = 'allUsers')) && (f[source].options.length == 0))
      disableButton(f,"add");
   else
	  enableButton(f,"add");

   if ( ((source = 'assignedActions') || (source = 'assignedUsers')) && (f[source].options.length == 0))
      disableButton(f,"remove");
   else
	  enableButton(f,"remove");
*/
}

function disableButton(form,name)
{
   var f = form;
   f[name].disabled = true;
}

function enableButton(form,name)
{
   var f = form;
   f[name].disabled = false;

}
function roleWrapper(name)
{
    this.name = name;
    this.flag = false;
}


function move(form,source, target)
{
	var target_length = form[target].length;
	var option_length = form[source].options.length;

	var j=0;
	for(var i=0; i<option_length; i++)
	{
	   if(( form[source].options[i] != null) && (form[source].options[i].selected ))
		{
			text = form[source].options[i].text;
			value = form[source].options[i].value;
			form[target].options[target_length + j] = new Option(text, value, true, true);
			j++;
		}
	}

	removeOption(form[source].options);
	updateButtons(form,source,target);
	sort(form,source);
	sort(form,target);
}



function moveAll(form,source, target)
{
	var target_length = form[target].length;
	var option_length = form[source].options.length;

	var j=0;
	for(var i=0; i<option_length; i++)
	{
	   if(form[source].options[i] != null)
		{
			text = form[source].options[i].text;
			value = form[source].options[i].value;
			form[target].options[target_length + j] = new Option(text, value, true, true);
			j++;
		}
	}

	removeOption(form[source].options);
        updateButtons(form,source,target);
        form[source].selectedIndex = -1;
        form[source].options.length = 0;

        sort(form,target);
}



function removeOption(options)
{
	var option_length = options.length;
	for(var i=0; i<option_length; i++)
	{
	   if(( options[i] != null) && (options[i].selected ))
		{
			options[i] = null;
			removeOption(options);
		}
	}
}


function forwardRequest(form,command,url)
{
   form.cmd.value = command;
   form.redirectURL.value = url;
}

function selectbothPanes(form,source, target, command)
{
	selectassignedActions(form,source, command)
	selectassignedActions(form,target, command)
}


function selectassignedActions(form,source, command)
{
	var option_length = form[source].options.length;

	for(var i=0; i<option_length; i++)
	{
		form[source].options[i].selected = true;
	}
   form.cmd.value = command;
 }


function saveActions(form,source, command)
{
	var option_length = form[source].options.length;

	for(var i=0; i<option_length; i++)
	{
		form[source].options[i].selected = true;
	}
	form.cmd.value = command;
}

function selectassignedActions(form,source)
{
	var option_length = form[source].options.length;

	for(var i=0; i<option_length; i++)
	{
		form[source].options[i].selected = true;
	}
}


function addRole(form,source,msg)
{
	var  text = form[source].value;
	if (text.length == 0)
	   return false;
	//Remove extra spaces from left or right of the text entered
	text = ltrim(text);
	text = rtrim(text);

	if(doesRoleExist('allActions',text,msg))
	   return false;


	if(doesRoleExist(assignedActions,text))
	   return false;

	var tmpArray  = form["allActions"].options;
	var length = tmpArray.length;
	document.form["allActions"].options[length] = new Option(text);
	document.form[source].value = "";
	return true;
}

function doesRoleExist(form,source,text,msg)
{
	tmpArray  = form[source].options;
	for (var i=0; i< tmpArray.length; i++)
	{
	    var txtElement = form[source].options[i].text;
	    var temp1 = text.toUpperCase();
	    var temp2 = txtElement.toUpperCase();

	    if ( text.toUpperCase() == txtElement.toUpperCase())
	    {
	       alert(msg);
	       return true;
	    }
	}
	return false;
}

function moveposition(form,source, dir)
{
	var sl = form[source].selectedIndex;
	var above = sl - 1;
	if (sl != -1 && form[source].options[sl])
	{
		var oText = form[source].options[sl].text;
		var oValue = form[source].options[sl].value;
		if(form[source].options[sl] && sl > 0 && dir == 0)
		{
			form[source].options[sl].text = form[source].options[sl - 1].text;
			form[source].options[sl].value = form[source].options[sl-1].value;
			form[source].options[sl-1].text = oText;
			form[source].options[sl-1].value = oValue;
			form[source].selectedIndex--;
		}
		else if (form[source].options[sl+1] && sl < form[source].length-1 && dir == 1)
		{
			form[source].options[sl].text = form[source].options[sl+1].text;
			form[source].options[sl].value = form[source].options[sl+1].value;
			form[source].options[sl+1].text = oText;
			form[source].options[sl+1].value = oValue;
			form[source].selectedIndex++;
		}
	}
	sort(form,source);
}


function sort(form,source)
{
	var i,j;
    var slct = form[source];
	var length = slct.options.length

	if (length < 1) return;
	if (length == 1)
	{
		if (slct.options[0].value.lastIndexOf("$UA$") == 0) 
		{
			slct.options[0].style.color = "silver";
		}
		return;
	}

	tmpArray = new Array();
	tmpArray.text = new Array();
	tmpArray.value = new Array();
	for (var i=0; i< length; i++)
	{
		tmpArray.text[i] = slct.options[i].text;
		tmpArray.value[i] = slct.options[i].value;
	}

	for (i=0; i<length; i++) {
		for (j=i+1; j<length; j++)
		{
			var text1 = tmpArray.text[i];
		   	var text2 = tmpArray.text[j];
			if (text1 > text2)
			{
				swapItemsInArray(tmpArray.text, i, j);
				swapItemsInArray(tmpArray.value, i, j);
			}
		}
	}
	slct.selectedIndex = -1;
	slct.options.length = 0;
	for (i=0; i<length; i++)
	{
		slct.options[i] = new Option(tmpArray.text[i],tmpArray.value[i]);
		if (tmpArray.value[i].lastIndexOf("$UA$") == 0) 
		{
			slct.options[i].style.color = "silver";
		}
	}
	slct.selectedIndex = 0;
}

function swapItemsInArray(array, index1, index2)
{
	if (index1<0) return array;
	if (index2<0) return array;
	if (index1>array.length-1) return array;
	if (index2>array.length-1) return array;

	var temp = array[index1]
	array[index1] = array[index2]
	array[index2] = temp;
	return array;
}


function trim(f)
{
  var e = f.elements;
  for(var i=0;i<e.length;i++){
    if(e[i].type!="text") continue;
    var s = e[i].value;
    if(s=="" || s==null) continue;
    e[i].value = ltrim(e[i].value);
    e[i].value = rtrim(e[i].value);
  }
}

function ltrim(s)
{
  var l = s.length;
  for(var i=0;i<l;i++){
    var c = s.charAt(i);
    if(c!=" ") break;
  }
  return s.substring(i);
}

function rtrim(s)
{
  var l = s.length;
  for(var i=l-1;i>=0;i--){
    var c = s.charAt(i);
    if(c!=" ") break;
  }
  return s.substring(0,i+1);
}

function firstBlankField(f,name)
{
  var e = f[name];
  for(var i=0;i<e.length;i++){
    if(e[i].type!="text") continue;
    var s = e[i].value;
    if(s=="" || s==null) {
      return e[i];
    }
  }
  return null;
}

function hasIllegalCharacters(f)
{
  var s = f.elements[0].value;
  var ret = false;

  var l = s.length;
  for(var i=l-1;i>=0;i--)
  {
    var c = s.charAt(i);
    if(isBadCharacter(c) == true)
    {
    	ret =  true;
    	break;
    }
  }
  return ret;   
}

function isBadCharacter(c)
{
  var ch = c;
  var  flag = false;
  switch (ch)
	{
		 case '!': flag = true; break;
		 case '"': flag = true; break;
		 case '#': flag = true; break;
		 case '$': flag = true; break;
		 case '%': flag = true; break;
		 case '&': flag = true; break;
		 case '\'': flag = true; break;
		 case '(': flag = true; break;
		 case ')': flag = true; break;
		 case '*': flag = true; break;
//		 case '+': flag = true; break;
//		 case '-': flag = true; break;
//		 case '.': flag = true; break;
		 case ';': flag = true; break;
		 case '<': flag = true; break;
		 case '=': flag = true; break;
		 case '>': flag = true; break;
		 case '?': flag = true; break;
		 case '@': flag = true; break;
		 case '[': flag = true; break;
// \\ is needed for BPO
//		 case '\\': flag = true; break;
		 case ']': flag = true; break;
		 case '^': flag = true; break;
		 case '`': flag = true; break;
		 case '{': flag = true; break;
		 case '|': flag = true; break;
		 case '}': flag = true; break;
		 case '~': flag = true; break;
		 default:  flag = false; break; 
	}  
	return flag;   
}

function validateMyForm(msg1,msg2)
{

	var f = document.frmRoleInfo;

	if( f.cmd.value == "")
		return false;

	if( f.cmd.value == null)
		return false;

	if( f.cmd.value == "abort")
		return true;

	selectassignedActions(f,'assignedActions'); 

		
	trim(f);

	var fld = firstBlankField(f,'rolename');

	if(fld!=null)
	{
		var message = fld.name + " " + msg1;
		alert(message);
		fld.focus();
		return false;
	}
	else if (hasIllegalCharacters(f) == true)
	{
		alert(msg2);
		return false;
	}
	else
		return true;
}


function validateMyGroupForm(msg1,msg2)
{

	var f = document.frmRoleInfo;

	if( f.cmd.value == "")
		return false;

	if( f.cmd.value == null)
		return false;

	if( f.cmd.value == "group-abort")
		return true;

	trim(f);

	var fld = firstBlankField(f,'groupname');

	if(fld!=null)
	{
		var message = fld.name + " " + msg1;
		alert(message);
		fld.focus();
		return false;
	}
	else if (hasIllegalCharacters(f) == true)
	{
		alert(msg2);
		return false;
	}
	else
		return true;
}

function validateModifyRole(form,source,command,msg1,msg2) 
{
 	var i;
 	var j = 0;

	var option_length = form[source].options.length;

	for(var i=0; i<option_length; i++)
	{
	   if(( form[source].options[i] != null) && (form[source].options[i].selected ))
	   {
			
	      j++;
	   }
	}

	if (j>1) 
	{
	    alert(msg1);
	    return false;
	}

	if (j<=0) 
	{
	    alert(msg2);
	    return false;
	}

	form.cmd.value = command;
	return true;
}


function confirmAddingRoles(form,command,source,msg)
{

	if(addRole(form,source,msg) == false)
	   return false;
	   
	form.cmd.value = command;
	return true;
}

function confirmAssignRoles(form,command,source,msg)
{

	if(checkEmptySelection(form,source,msg) == false)
	   return false;
	   
	form.cmd.value = command;
	return true;
}

function confirmAssignRoles(form,command,source,msg,attr1)
{

	if(checkEmptySelection(form,source,msg) == false)
	   return false;
	   
	form.cmd.value = command;
	form.principal.value = attr1
	return true;
}

function confirmRestoreDefaultRoles(form,command,msg)
{

	if (confirm(msg) == false )
		return false;

	form.cmd.value = command;
   return true;
}

function confirmDeletion(form,command,source,msg1,msg2)
{

	if(checkEmptySelection(form,source,msg2) == false)
	   return false;
	   
	if (confirm(msg1) == false )
		return false;

	form.cmd.value = command;
	return true;
}


function checkEmptySelection(form,source,msg)
{
 	source_selectedIndex = form[source].selectedIndex;
 	if (source_selectedIndex == -1)
	{
       		alert(msg);
       		return false;
	}
	else
		return true;
}



function textCounter(field, maxlimit) 
{
	if (field.value.length > maxlimit)
	{
		field.value = field.value.substring(0, maxlimit);
	}
}

function limitText(fieldObj, maxChars)
{
  var result = true;
  if (fieldObj.value.length >= maxChars)
    result = false;
  
  if (window.event)
    window.event.returnValue = result;
  return result;
}


function moveUp()
{
}

function moveDown()
{
}

function deletePrimaryRoles()
{
}

/*
function validateSelectionBox(form,source) 
{
 	var i, j;

	var option_length = form[source].options.length;
	if (option_length<=0) 
	{
	    alert('Please choose at least one role from the list on the left.');
	    return false;
	}
	return true;
}
*/

