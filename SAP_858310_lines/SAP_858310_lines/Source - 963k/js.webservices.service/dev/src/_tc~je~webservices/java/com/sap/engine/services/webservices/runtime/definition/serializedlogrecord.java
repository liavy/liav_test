/*
 * Copyright (c) 2002 by SAP Labs Bulgaria,
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of SAP Labs Sofia. You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms
 * of the license agreement you entered into with SAP Labs Sofia.
 */
package com.sap.engine.services.webservices.runtime.definition;

import com.sap.tc.logging.LogRecord;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Alexander Zubev
 */
public class SerializedLogRecord implements Serializable {
  private String msgClearText;
  private Date time;
  private int severity;
  private String threadName;

  public SerializedLogRecord(LogRecord record) {
    msgClearText = record.getMsgClear();
    time = record.getTime();
    severity = record.getSeverity();
    threadName = record.getThreadName();
  }

  public static SerializedLogRecord[] fromLogRecords(LogRecord[] logRecords) {
    SerializedLogRecord[] records = new SerializedLogRecord[logRecords.length];
    for (int i = 0; i < logRecords.length; i++) {
      records[i] = new SerializedLogRecord(logRecords[i]);
    }
    return records;
  }

  public String getMsgClearText() {
    return msgClearText;
  }

  public Date getTime() {
    return time;
  }

  public int getSeverity() {
    return severity;
  }

  public String getThreadName() {
    return threadName;
  }
}
