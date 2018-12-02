 
function conFirm() { 
if (confirm("User ABraght has been informed of your approval. To review the next new user request, click on the 'Yes' button. To return to your inbox, click on the 'Cancel' button.") ) 
{ 
parent.location='07.htm'; 

} 
else 
{ 
parent.location='06.htm'; 
} 

} 

function conFirm2() { 
if (confirm("This log will be deleted now. You may want to print this page for future reference. To leave now, click on OK. Otherwise, click Cancel.") ) 
{ 
parent.location='13_5.htm'; 

} 
else 
{ 
parent.location='13_5_1.htm'; 
} 

} 
