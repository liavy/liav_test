function MM_goToURL() { //v3.0
  		var i, args=MM_goToURL.arguments; document.MM_returnValue = false;
  		for (i=0; i<(args.length-1); i+=2) eval(args[i]+".location='"+args[i+1]+"'");}
		
		function newWindow() {
		window.open("create_distributionlist_newwin.htm", "sub", "WIDTH=650, HEIGHT=500, status=yes, resizable=yes scrollbars=yes")
}
	
function toggle(idName) {
	target = document.all(idName);
	if (target.style.display == "none") {
		target.style.display = "";
		}else {
		target.style.display = "none";
	}
}
	
function putFocus(formInst, elementInst) {
  if (document.forms.length > 0) {
   document.forms[formInst].elements[elementInst].focus();
  }
}
 

function expandME(idTray, nCancelState, altmin, altmax) { 
	var elBody = document.getElementById(idTray+"-bd");
	var elExpander = document.getElementById(idTray+"-exp");
	var elHeader = document.getElementById(idTray+"-hd");
	var elExpandState = document.getElementById(idTray+"-es");


	if ( elBody != null && elExpander != null )
	{
		if ( elBody.style.display == "none" )
		{
			elBody.style.display = "";
			elExpander.src = "layout/icon_open.gif";
			elExpander.alt = altmin;
			elExpander.title = altmin;

			if ( elExpandState )
				elExpandState.value = "1";
			if ( nCancelState == "1" )
			{
				event.returnValue = false;
				event.cancelBubble = true;
				return false;
			}
 		}
		else
		{
			elBody.style.display = "none";
			elExpander.src = "layout/icon_close.gif";
			elExpander.alt = altmax;
			elExpander.title = altmax;

			if ( elExpandState )
				elExpandState.value = "0";
			if ( nCancelState == "2" )
			{
				event.returnValue = false;
				event.cancelBubble = true;
				return false;
			}
		}
	}
	return true;	
}


function expandME(idTray, nCancelState, altmin, altmax, webpath) { 
	var elBody = document.getElementById(idTray+"-bd");
	var elExpander = document.getElementById(idTray+"-exp");
	var elHeader = document.getElementById(idTray+"-hd");
	var elExpandState = document.getElementById(idTray+"-es");


	if ( elBody != null && elExpander != null )
	{
		if ( elBody.style.display == "none" )
		{
			elBody.style.display = "";
			elExpander.src = webpath+"layout/icon_open.gif";
			elExpander.alt = altmin;
			elExpander.title = altmin;			

			if ( elExpandState )
				elExpandState.value = "1";
			if ( nCancelState == "1" )
			{
				event.returnValue = false;
				event.cancelBubble = true;
				return false;
			}
 		}
		else
		{
			elBody.style.display = "none";
			elExpander.src = webpath+"layout/icon_close.gif";
			elExpander.alt = altmax;
			elExpander.title = altmax;			

			if ( elExpandState )
				elExpandState.value = "0";
			if ( nCancelState == "2" )
			{
				event.returnValue = false;
				event.cancelBubble = true;
				return false;
			}
		}
	}
	return true;	
}

	
function toggleside(idName) {
		target = document.all(idName);
		if (target.style.display == "none") {
			target.style.display = "";
			eval("document.img" + idName + ".src = 'layout/tab1open.gif'");
		}else {
			target.style.display = "none";
			eval("document.img" + idName + ".src = 'layout/tab1closed.gif'");
		}
}
	
function MM_swapImgRestore() { //v3.0
  var i,x,a=document.MM_sr; for(i=0;a&&i<a.length&&(x=a[i])&&x.oSrc;i++) x.src=x.oSrc;
}

function MM_preloadImages() { //v3.0
  var d=document; if(d.images){ if(!d.MM_p) d.MM_p=new Array();
    var i,j=d.MM_p.length,a=MM_preloadImages.arguments; for(i=0; i<a.length; i++)
    if (a[i].indexOf("#")!=0){ d.MM_p[j]=new Image; d.MM_p[j++].src=a[i];}}
}

function MM_findObj(n, d) { //v3.0
  var p,i,x;  if(!d) d=document; if((p=n.indexOf("?"))>0&&parent.frames.length) {
    d=parent.frames[n.substring(p+1)].document; n=n.substring(0,p);}
  if(!(x=d[n])&&d.all) x=d.all[n]; for (i=0;!x&&i<d.forms.length;i++) x=d.forms[i][n];
  for(i=0;!x&&d.layers&&i<d.layers.length;i++) x=MM_findObj(n,d.layers[i].document); return x;
}

function MM_swapImage() { //v3.0
  var i,j=0,x,a=MM_swapImage.arguments; document.MM_sr=new Array; for(i=0;i<(a.length-2);i+=3)
   if ((x=MM_findObj(a[i]))!=null){document.MM_sr[j++]=x; if(!x.oSrc) x.oSrc=x.src; x.src=a[i+2];}
}

