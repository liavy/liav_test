function submit_request(type, id) {
  /* type = 3 - process single delete of id
            4 - process single resend of id
            5 - process single resend-alternate of id
  */
    document.detailForm.COMMAND.value = "";
    switch(type) {
      case 3:
        document.detailForm.DELETELIST.value = id + "_";
        document.detailForm.submit();
        break;
      case 4:
        document.detailForm.RESENDLIST.value = id + "_";
        document.detailForm.submit();
        break;
      case 5:
        document.detailForm.RESENDALTLIST.value = id + "_";
        document.detailForm.submit();
        break;
      default:
        document.detailForm.DELETELIST.value = "";
        document.detailForm.RESENDLIST.value = "";
        document.detailForm.COMMAND.value = "CANCEL";
        document.detailForm.submit();
        break;
    }
}
