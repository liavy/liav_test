/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company               Description
 *  ----------  ----------------          ----------------------------------------------
 *  2003/06/28  John Stecher, IBM         Created for SPECjAppServer2004
 *  2003/08/12  Samuel Kounev, Darmstadt  Added final to constant declarations.
 *  2006/01/17  Bernhard Riedhofer, SAP   Added method round.
 */

package org.spec.jappserver.common;

import java.math.BigDecimal;

/**
 * @author John Stecher
 *
 */
public class BigDecimalUtils
{
   public static final BigDecimal zeroBigDec = new BigDecimal("0.00");
   public static final BigDecimal oneBigDec = new BigDecimal("1.00");
   public static final BigDecimal onehundredBigDec = new BigDecimal("100.00");

   public static BigDecimal round (BigDecimal decimal)
   {
      return decimal.setScale(2, BigDecimal.ROUND_UP);
   }
}
