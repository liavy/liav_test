<html>
<head>
  <link rel="stylesheet" href="css/ur/ur_nn6.css" type="text/css">
  <title>Login, SAP AG</title>
  <script language="JavaScript" src="js/basic.js" type="text/javascript"/>
  <script type="text/javascript">
    var inPortalScript = false;
    
    function clearEntries() {
      document.logonForm.longUid.value="";
      document.logonForm.password.value="";
    }

    function setFocusToFirstField() {
      myform = document.logonForm;
      for(i=0; i<myform.length; i++) {
        elem = myform.elements[i];
        if(elem.readOnly==false && elem.type=="text") {
          elem.focus();
          break;
        }
      }
    }

    function addTenantPrefix() {
      return true;
    }
  </script>
</head>

<body class="urBdyStd" bgcolor="#F7F9FB" onload="setFocusToFirstField()">
  <table valign="middle" align="center" border="0" cellspacing="0" style="margin-top:10">
    <tr>
      <td colspan="2" valign="Top" width="300" height="24" class="welcome">Welcome</td>
    </tr>

    <tr>
      <td colspan="2" valign="Top">
        <table border="1" bordercolordark="#BEBEBE" bgcolor="#FFFFFF" cellspacing="0" cellpadding="0" bordercolorlight="#BEBEBE">
          <tr>
            <td style="padding-top:10px; padding-left:10px" align="left" valign="top" bgcolor="#E9E9E9" bordercolor="#FFFFFF">
              <!-- data table starts after this line -->
              <form name="logonForm" method="post" action="j_security_check" id="logonForm">
                <table border="0" width="301" height="100" align="left" cellspacing="0" valign="top">
                  <!-- display error message if there is one -->
                  <tr>
                    <td colspan="2" height="33">
                      <div class="urMsgBarErr" style="margin-bottom:3;">
                        <table border="0" cellpadding="0" cellspacing="0">
                          <tbody>
                            <tr>
                              <td class="urTxtStd"><span class="urMsgBarImgError"><img height="12" width="12" src="css/common/1x1.gif"></span></td>
                              <td>
                                <span class="urTxtStd" tabindex= "0">
                                  User authentication <%= (request.getParameter("error") != null)?"failed":"required" %>
                                </span>
                              </td>
                            </tr>
                          </tbody>
                        </table>
                      </div>
                    </td>
                  </tr>
                  <!-- header line -->
                  <tr>
                    <td colspan="2">
                      <div class="urTxtH3">Login</div>
                    </td>
                  </tr>
                  <!-- userid -->
                  <tr>
                    <td width="161" height="20"><label class="urLblStd" for="logonuidfield">User ID <span class="urLblReq">*</span></label></td>
                    <td width="183" height="20"><input style="WIDTH: 21ex" class="urEdfTxtEnbl" id="logonuidfield" name="j_username" type="text" value=""></td>
                  </tr>
                  <!-- password -->
                  <tr>
                    <td width="161" height="20"><label class="urLblStd" for="logonpassfield">Password <span class="urLblReq">*</span></label></td>
                    <td width="183" height="20"><input style="WIDTH: 21ex" class="urEdfTxtEnbl" id="logonpassfield" name="j_password" type="password"></td>
                  </tr>
                  <!-- space above buttons -->
                  <tr>
                    <td colspan="2" height="20"></td>
                  </tr>
                  <!-- logon button -->
                  <tr>
                    <td colspan="2"><input style="height:3ex;" class="urBtnStd" type="submit" name="uidPasswordLogon" value="Logon"></td>
                  </tr>
                </table>
              </form>
              <!-- data table ends before this line -->
            </td>

            <td bordercolor="#FFFFFF">
              <table border="0" cellspacing="0" cellpadding="0">
                <tr><td><img src="layout/branding-image.jpg" alt="Branding Image" border="0"></td></tr>
                <tr><td bordercolor="#FFFFFF"><img src="layout/branding-text.gif" alt="" border="0"></td></tr>
              </table>
            </td>
          </tr>
        </table>
      </td>
    </tr>

    <tr>
      <td></td>
    </tr>

    <tr>
      <td border="0" width="100%" align="left">
        <p class="urLblStdBar">2002-2004 SAP AG All Rights Reserved.<br>
        <img src="layout/sapLogo.gif" alt="SAP AG" title="SAP AG" width="36" height="18" vspace="3"></p>
      </td>
    </tr>
  </table>
</body>
</html>
