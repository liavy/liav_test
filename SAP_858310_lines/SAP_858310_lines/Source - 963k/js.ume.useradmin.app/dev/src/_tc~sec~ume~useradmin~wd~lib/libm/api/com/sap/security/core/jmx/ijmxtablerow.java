package com.sap.security.core.jmx;

import javax.management.openmbean.CompositeData;

/**
 * This interface contains a table row structure.
 */
public interface IJmxTableRow extends CompositeData {

    public static final String COLUNIQUEID = "ColUniqueId";

    public static final String COLTYPE = "ColType";

    public static final String COLREFUNIQUEID = "ColRefUniqueId";

    public static final String COLDELETEABLE = "ColDeleteable";

    public static final String COLSTATUS0 = "ColStatus0";

    public static final String COL0 = "Col0";

    public static final String COL1 = "Col1";

    public static final String COL2 = "Col2";

    public static final String COL3 = "Col3";

    public static final String COL4 = "Col4";

    public static final String COL5 = "Col5";

    public static final String COL6 = "Col6";

    public static final String COL7 = "Col7";

    public static final String COL8 = "Col8";

    public static final String COL9 = "Col9";

    public String getColUniqueId();

    public String getColRefUniqueId();

    public String getColType();

    public boolean getColDeleteable();

    public boolean getColStatus0();

    public String getCol0();

    public String getCol1();

    public String getCol2();

    public String getCol3();

    public String getCol4();

    public String getCol5();

    public String getCol6();

    public String getCol7();

    public String getCol8();

    public String getCol9();

}