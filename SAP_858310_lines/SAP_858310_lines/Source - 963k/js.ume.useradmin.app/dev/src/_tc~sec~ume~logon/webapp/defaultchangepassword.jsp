<html>
<head>
  <link rel="stylesheet" href="css/ur/ur_nn6.css" type="text/css">
  <title>Change Password, SAP AG</title>
  <script language="JavaScript" src="js/basic.js" type="text/javascript">
  <script type="text/javascript">
    var inPortalScript = false;
    
    function setFocusToFirstField() {
      myform = document.changePasswordForm;
      for(i=0; i<myform.length; i++) {
        elem = myform.elements[i];
        if(elem.readOnly==false && (elem.type=="text" || elem.type=="password")) {
          elem.focus();
          break;
        }
      }
    }
  </script>
</head>

<body class="urBdyStd" bgcolor="#F7F9FB" onload="setFocusToFirstField()">
  <table valign="middle" align="center" border="0" cellspacing="0" style="margin-top:10">
    <tr><td colspan="2" valign="Top" width="300" height="24" class="welcome">Welcome</td></tr>
    <tr>
      <td colspan="2" valign="Top">
        <table border="1" bordercolordark="#BEBEBE" bgcolor="#FFFFFF" cellspacing="0" cellpadding="0" bordercolorlight="#BEBEBE">
          <tr>
            <td style="padding-top:10px; padding-left:10px" align="left" valign="top" bgcolor="#E9E9E9" bordercolor="#FFFFFF">
              <!-- data table starts after this line -->
              <form name="changePasswordForm" method="post" action="sap_j_security_check" id="changePasswordForm">
                <table border="0" width="301" height="100%" align="left" cellspacing="0" valign="top">
                  <!-- display error message if there is one -->
                  <tr>
                    <td colspan="2" height="33">
                      <div class="urMsgBarErr" style="margin-bottom:3;">
                        <table border="0" cellpadding="0" cellspacing="0">
                          <tbody>
                            <tr>
                              <td class="urTxtStd"><span class="urMsgBarImgError"><img height="12" width="12" src="css/common/1x1.gif"></span></td>
                              <td>
                                <span class="urTxtStd" tabindex="0">
                                  <%= (request.getParameter("error") != null)?"Change password failed":"Password has expired" %>
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
                      <div class="urTxtH3">
                        Change Password
                      </div>
                    </td>
                  </tr>
                  <!-- old password -->
                  <tr>
                    <td width="161" height="20"><label class="urLblStd" for="logonoldpassfield">Old Password<span class="urLblReq">*</span></label></td>
                    <td width="183" height="20"><input class="urEdfTxtEnbl" id="logonoldpassfield" name="j_sap_current_password" type="password"></td>
                  </tr>
                  <!-- new password -->
                  <tr>
                    <td width="161" height="20"><label class="urLblStd" for="logonnewpassfield">New Password<span class="urLblReq">*</span></label></td>
                    <td width="183" height="20"><input class="urEdfTxtEnbl" id="logonnewpassfield" name="j_sap_password" type="password"></td>
                  </tr>
                  <!-- retype new password -->
                  <tr>
                    <td width="161" height="20"><label class="urLblStd" for="logonretypepassfield">Confirm Password<span class="urLblReq">*</span></label></td>
                    <td width="183" height="20"><input class="urEdfTxtEnbl" id="logonretypepassfield" name="j_sap_again" type="password"></td>
                  </tr>
                  <!-- space above buttons -->
                  <tr>
                    <td colspan="2" height="20"></td>
                  </tr>
                  <!-- submit buttons -->
                  <tr>
                    <td colspan="2"><input style="height:20;" class="urBtnStd" type="submit" name="performChangePassword" value="Change">
                    <input style="height:20;" class="urBtnStd" type="submit" name="showUidPasswordLogonPage" value="Cancel"></td>
                  </tr>
                </table>
              </form>
              <!-- data table ends before this line -->
            </td>
            <td bordercolor="#FFFFFF">
              <table border="0" cellspacing="0" cellpadding="0">
                <tr><td><img src="layout/branding-image.jpg" alt="Branding Image" border="0"></td></tr>
                <tr><td bordercolor="#FFFFFF"><img src="layout/branding-text.gif"alt="" border="0"></td></tr>
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
        <p class="urLblStdBar">2002-2004 SAP AG All RightsReserved.<br>
        <img src="layout/sapLogo.gif" alt="SAP AG" title="SAP AG" width="36"height="18" vspace="3"></p>
      </td>
    </tr>
  </table>
</body>
</html>
