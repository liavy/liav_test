/*
 * Copyright (c) 2004 Standard Performance Evaluation Corporation (SPEC)
 *               All rights reserved.
 *
 * This source code is provided as is, without any express or implied warranty.
 *
 *  History:
 *  Date        ID, Company             Description
 *  ----------  ----------------        ----------------------------------------------
 *  2003/01/01  John Stecher, IBM       Created for SPECjAppServer2004
 *  2003/03/26  John Stecher, IBM       Implemented Big Decimal Changes
 *  2003/06/28  John Stecher, IBM       Removed unnecessary imports
 */

package org.spec.jappserver.orders.helper;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * @author jstecher
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ShoppingCart implements java.io.Serializable {

    private ArrayList items;
    private BigDecimal total;

    public ShoppingCart() {
        items = new ArrayList();
        total = new BigDecimal("0.00"); 
    }

    public void addItem(ShoppingCartDataBean scdb) {
        total = total.add(scdb.getTotalCost());
        items.add(scdb);
    }

    public ShoppingCartDataBean getItem(int index) {
        return(ShoppingCartDataBean) items.get(index); 
    }

    public int getItemCount() {
        return items.size();    
    }

    public BigDecimal getTotal() {
        return total;   
    }

    public void removeItem(int index) {
        ShoppingCartDataBean scdb = (ShoppingCartDataBean)items.remove(index);
        total = total.subtract(scdb.getTotalCost());    
    }

    public void clearCart() {
        items.clear();
        total = new BigDecimal("0.00"); 
    }

}
