package com.sap.engine.services.webservices.espbase.configuration.ann.dt;

public enum TransportGuaranteeEnumsLevel 
{
	NONE("None"),
	@Deprecated
	INTEGRITY("Integrity"),
	@Deprecated
	CONFIDENTIALITY("Confidentiality"),
	@Deprecated
	BOTH("Both"),
	INTEGRITY_AND_CONFIDENTIALITY("IntegrityAndConfidentiality");
	
    private final String value;
    
    TransportGuaranteeEnumsLevel(final String value)
    {
        this.value = value;
    }

    final String getValue()
    {
        return this.value;
    }
}
