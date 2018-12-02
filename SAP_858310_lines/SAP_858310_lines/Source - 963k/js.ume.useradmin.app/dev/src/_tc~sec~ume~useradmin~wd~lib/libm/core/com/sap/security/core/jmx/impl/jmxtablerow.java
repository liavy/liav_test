package com.sap.security.core.jmx.impl;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.InvalidKeyException;
import javax.management.openmbean.OpenDataException;

import com.sap.jmx.modelhelper.ChangeableCompositeData;
import com.sap.jmx.modelhelper.OpenTypeFactory;
import com.sap.security.core.jmx.IJmxTableRow;

public class JmxTableRow extends ChangeableCompositeData implements
        IJmxTableRow {
    
	private static final long serialVersionUID = -7039022729670178207L;

    private static CompositeType myType;

    public JmxTableRow() throws OpenDataException {
        super(getMyType());
        initialize();
    }

    public JmxTableRow(CompositeData data) throws OpenDataException {
        super(data);
    }

    private static CompositeType getMyType() throws OpenDataException {
        CompositeType type = myType;
        if (type == null) {
            type = OpenTypeFactory.getCompositeType(IJmxTableRow.class);
            myType = type;
        }
        return type;
    }

    /*
     * (non-Javadoc)
     */
    public String getColUniqueId() {
        return (String) get(COLUNIQUEID);
    }

    public String setColUniqueId(String colUniqueId) {
        return (String) set(COLUNIQUEID, colUniqueId);
    }

    /*
     * (non-Javadoc)
     */
    public String getColRefUniqueId() {
        return (String) get(COLREFUNIQUEID);
    }

    public String setColRefUniqueId(String colRefUniqueId) {
        return (String) set(COLREFUNIQUEID, colRefUniqueId);
    }

    /*
     * (non-Javadoc)
     */
    public boolean getColDeleteable() {
        return ((Boolean) get(COLDELETEABLE)).booleanValue();
    }

    public boolean setColDeleteable(boolean colDeleteable) {
        Boolean result = (Boolean) set(COLDELETEABLE,
                new Boolean(colDeleteable));
        if (result == null) {
            return false;
        }
        return result.booleanValue();
    }

    /*
     * (non-Javadoc)
     */
    public boolean getColStatus0() {
        return ((Boolean) get(COLSTATUS0)).booleanValue();
    }

    public boolean setColStatus0(boolean colStatus0) {
        Boolean result = (Boolean) set(COLSTATUS0, new Boolean(colStatus0));
        if (result == null) {
            return false;
        }
        return result.booleanValue();
    }

    /*
     * (non-Javadoc)
     */
    public String getColType() {
        return (String) get(COLTYPE);
    }

    public String setColType(String colType) {
        return (String) set(COLTYPE, colType);
    }

    /*
     * (non-Javadoc)
     */
    public String getCol0() {
        return (String) get(COL0);
    }

    public String setCol0(String col0) {
        return (String) set(COL0, col0);
    }

    /*
     * (non-Javadoc)
     */
    public String getCol1() {
        return (String) get(COL1);
    }

    public String setCol1(String col1) {
        return (String) set(COL1, col1);
    }

    /*
     * (non-Javadoc)
     */
    public String getCol2() {
        return (String) get(COL2);
    }

    public String setCol2(String col2) {
        return (String) set(COL2, col2);
    }

    /*
     * (non-Javadoc)
     */
    public String getCol3() {
        return (String) get(COL3);
    }

    public String setCol3(String col3) {
        return (String) set(COL3, col3);
    }

    /*
     * (non-Javadoc)
     */
    public String getCol4() {
        return (String) get(COL4);
    }

    public String setCol4(String col4) {
        return (String) set(COL4, col4);
    }

    /*
     * (non-Javadoc)
     */
    public String getCol5() {
        return (String) get(COL5);
    }

    public String setCol5(String col5) {
        return (String) set(COL5, col5);
    }

    /*
     * (non-Javadoc)
     */
    public String getCol6() {
        return (String) get(COL6);
    }

    public String setCol6(String col6) {
        return (String) set(COL6, col6);
    }

    /*
     * (non-Javadoc)
     */
    public String getCol7() {
        return (String) get(COL7);
    }

    public String setCol7(String col7) {
        return (String) set(COL7, col7);
    }

    /*
     * (non-Javadoc)
     */
    public String getCol8() {
        return (String) get(COL8);
    }

    public String setCol8(String col8) {
        return (String) set(COL8, col8);
    }

    /*
     * (non-Javadoc)
     */
    public String getCol9() {
        return (String) get(COL9);
    }

    public String setCol9(String col9) {
        return (String) set(COL9, col9);
    }

    private static String[] getStringAttributes() {
        return new String[] { COLTYPE, COLUNIQUEID, COLDELETEABLE,
                COLREFUNIQUEID, COLSTATUS0, COL0, COL1, COL2, COL3, COL4, COL5,
                COL6, COL7, COL8, COL9 };
    }
    
    private void initialize(){
        this.setCol0(CompanyPrincipalFactory.EMPTY);
        this.setCol1(CompanyPrincipalFactory.EMPTY);
        this.setCol2(CompanyPrincipalFactory.EMPTY);
        this.setCol3(CompanyPrincipalFactory.EMPTY);
        this.setCol4(CompanyPrincipalFactory.EMPTY);
        this.setCol5(CompanyPrincipalFactory.EMPTY);
        this.setCol6(CompanyPrincipalFactory.EMPTY);
        this.setCol7(CompanyPrincipalFactory.EMPTY);
        this.setCol8(CompanyPrincipalFactory.EMPTY);
        this.setCol9(CompanyPrincipalFactory.EMPTY);
        this.setColDeleteable(false);
        this.setColRefUniqueId(CompanyPrincipalFactory.EMPTY);
        this.setColStatus0(false);
        this.setColType(CompanyPrincipalFactory.EMPTY);
        this.setColUniqueId(CompanyPrincipalFactory.EMPTY);
    }

    public String toString() {
        StringBuffer res = new StringBuffer();
        res.append("\n");
        res
                .append("***************************************************************************\n");
        res.append("* ");
        res.append(this.getClass().getName());
        res.append(" ");
        res.append((new java.util.Date()).toString());
        res.append("\n");

        String[] args = getStringAttributes();
        for (int i = 0; i < args.length; i++) {
            res.append("* ");
            res.append(args[i]);
            res.append(" : ");
            try {
                res.append(get(args[i]));
            } catch (InvalidKeyException e) {
                res.append("n/a");
            }
            res.append("\n");
        }

        res
                .append("***************************************************************************\n");
        return res.toString();
    }

    public void setTableRowValue(int column, String value) {
        switch (column) {
            case 0:
                this.setCol0(value);
                break;
            case 1:
                this.setCol1(value);
                break;
            case 2:
                this.setCol2(value);
                break;
            case 3:
                this.setCol3(value);
                break;
            case 4:
                this.setCol4(value);
                break;
            case 5:
                this.setCol5(value);
                break;
            case 6:
                this.setCol6(value);
                break;
            case 7:
                this.setCol7(value);
                break;
            case 8:
                this.setCol8(value);
                break;
            case 9:
                this.setCol9(value);
                break;
        }
    }

}