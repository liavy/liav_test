package com.sap.sl.util.jarsl.api;

/**
 * Title:        sdm
 * Description:  Software Deployment Manager
 * Copyright:    Copyright (c) 2001
 * Company:      SAP AG
 * @author DL software logistics for Java
 * @version 1.0
 */

public interface ConstantsIF {
  static final String JARSL_MANIFEST             = "MANIFEST.MF";
  static final String JARSL_SAP_MANIFEST         = "SAP_MANIFEST.MF";
  static final String JARSL_METAINF              = "META-INF";
  static final String JARSL_METAINF_MANIFEST     = "META-INF/MANIFEST.MF";
  static final String JARSL_METAINF_SAP_MANIFEST = "META-INF/SAP_MANIFEST.MF";
  
  static final String ATTSPECTIT   = "Specification-Title";
  static final String ATTSPECVERS  = "Specification-Version";
  static final String ATTSPECVEN   = "Specification-Vendor";
  static final String ATTIMPTIT    = "Implementation-Title";
  static final String ATTIMPVERS   = "Implementation-Version";
  static final String ATTIMPVEN    = "Implementation-Vendor";
  static final String ATTIMPVENDID = "Implementation-Vendor-Id";
  static final String ATTDEPENDENCIES = "dependencies";
  static final String ATTDEPENDENCYLIST = "dependencylist";
  static final String ATTJARSLVERSION = "JarSL-Version";
  static final String ATTJARSAPVERSION = "JarSAP-Version";
  static final String ATTJARSAPSTANDALONEVERSION = "JarSAP-Standalone-Version";
  static final String ATTJARSAPPROCESSINGVERSION = "JarSAPProcessing-Version";
  static final String ATTJARSLMFVERSION = "JarSLMf-Version";
  static final String ATTSAPMANIFESTORIGIN = "sapmanifestorigin";
  static final String ATTJARSLFINGERPRINT = "sap_md5fingerprint";
  static final String ATTPUBLIC = "public";
  static final String ATTSAPINTERNAL = "sapinternal";
  static final String ATTDEPLOYFILE = "deployfile";
  static final String ATTREFACTORINGFILE = "refactoringfile";
  static final String ATTSOFTWARESUBTYPE = "softwaresubtype";
  static final String ATTCHANGELISTNUMBER = "changelistnumber";
  static final String ATTPERFORCESERVER = "perforceserver";
  static final String ATTPROJECTNAME = "projectname";
  static final String ATTDTRINTEGRATIONSEQUENCENO = "dtr-integration-sequence-no";
  static final String ATTDTRWORKSPACE = "dtr-workspace";
  static final String ATTSAPCHANGELISTNUMBER = "sap-changelistnumber";
  static final String ATTSAPPERFORCESERVER = "sap-perforceserver";
  static final String ATTPLATTFORM = "platform";
  static final String ATTDOCFILE = "docfile";
  static final String ATTMANIFESTVERSION = "Manifest-Version";
  static final String ATTNAME = "Name: ";
  static final String ATTMAINCLASS = "Main-Class";
  static final String ATTSOFTWARETYPE = "softwaretype";
  static final String ATTCOMPRESS = "compress";
  static final String ATTSDMSDACOMPVERSION = "SDM-SDA-Comp-Version";
  static final String ATTRELEASE = "release";
  static final String ATTSOFTPPMSNUMBER = "Softwarecomponent-Ppmsnumber";
  static final String ATTSOFTVENDOR = "Softwarecomponent-Vendor";
  static final String ATTSOFTNAME = "Softwarecomponent-Name";
  static final String ATTSOFTVERSION = "Softwarecomponent-Version";
  static final String ATTEXPORTSCVERSION = "ExportSC-Version";
  static final String ATTSPNUMBER = "SP-Number";
  static final String ATTSPPATCHLEVEL = "SP-Patchlevel";
  static final String ATTCONTENT = "content";
  static final String ATTSCAFROMDIR = "directory-sca";
  static final String ATTSCAFROMLIST = "list-sca";
  static final String ATTKEYNAME = "keyname";
  static final String ATTDCNAME = "dcname";
  static final String ATTKEYVENDOR = "keyvendor";
  static final String ATTKEYLOCATION = "keylocation";
  static final String ATTKEYCOUNTER = "keycounter";
  static final String ATTCOMPONENTELEMENT = "componentelement";
  static final String ATTSCACREATIONMODE = "scacreationmode";
  static final String ATTSCACREATIONWITHOUTIDCHECK = "scacreationwithoutidcheck";
  static final String ATTSCACREATIONWITHOUTSDAEXISTENCECHECK = "scacreationwithoutsdaexistencecheck";
  static final String ATTEXTSDMSDACOMPVERSION = "Ext-SDM-SDA-Comp-Version";
  static final String ATTTCSPRAVERSION = "TCSpra-Version";
  static final String ATTSKIPNWDIDEPLOYMENT = "skipnwdideployment";
  static final String ATTCSNCOMPONENT = "csncomponent";
  static final String ATTBUNDLEID = "bundleid";
  static final String ATTARCHIVETYPE = "archivetype";
  
  //for DCA
  static final String ATTDCAVERSION = "DCA-Version";
  static final String ATTLOCATION = "dcalocation";
  static final String ATTCOUNTER = "dcacounter"; 
  
  static final String NODBCONTENT = "nodbcontent";
  static final String PERFORCEVERSIONING = "perforceversioning";
  static final String PROJECTBASEDIR = "projectbasedir";
  static final String JARSAPVERSION = "jarsapversion_i"; 
  static final String PROPLOCALMAKE = "localmake";
  static final String TAGDEPENDENCY = "dependency";
  static final String TAGDEPENDENCYTYPE = "dependencyType";
  static final String CONNECTPARAMS = "connectparameters";
  static final String LCRURL = "lcrurl";
  static final String DCRELEASE = "dcrelease";
  static final String MAKELCRENTRY = "makelcrentry";
  static final String CHECKLCRENTRY = "checklcrentry";
  static final String WRITELCRLOG = "writelcrlog";
  static final String EXTSPNUMBER = "extendedspnumber";
  static final String MAKEREL = "make.rel";
  static final String MAKELCRREL = "makelcr.rel";
  static final String SCDCASSOCIATIONFILE = "scdcassociationfile";
  static final String IGNOREMISSINGDCSINASSOCFILE = "ignoremissingdcsinassocfile";
  static final String FINALVERSION = "finalversion";
  static final String PERFORMONLYLCRENTRY = "performonlylcrentry";
  static final String PRINTONLYSCACONTENT = "printonlyscacontent";
  static final String SKIPINTERNALARCHIVES = "skipinternalarchives";
  static final String ARCHIVEDIR = "archive.dir";
  static final String KEYNAMEA = "name";
  static final String KEYVENDORA = "vendor";
  static final String KEYLOCATIONA = "location";
  static final String KEYCOUNTERA = "counter";
  static final String SERVICELEVEL= "servicelevel";
  static final String PATCHLEVEL = "patchlevel";
  static final String SCACONTENTPATH = "scacontentpath";
  static final String SECSTOREDATAFILENAME = "secstore.datafilename";
  static final String SECSTOREKEYFILENAME = "secstore.keyfilename";
  static final String EXTENDFILENAME = "extendfilename";
  static final String SCVERSIONDESCRIPTIONFILE="scversiondescriptionfile";
	static final String SRCCHECKSUMALGORITHM = "srcchecksumalgorithm";
	static final String SRCCHECKSUMVALUE = "srcchecksumvalue";

  static final String W_MESSAGELCR="WARNING during LCR access: ";
  static final String E_MESSAGELCR="ERROR during LCR access: ";
  static final String I_MESSAGELCR="INFO during LCR access: ";

  static final String PR_TYPE="pr_type";
  static final String PR_SOFTWARECOMPONENTVENDOR="pr_softwarecomponentvendor";
  static final String PR_SOFTWARECOMPONENTNAME="pr_softwarecomponentname";
  static final String PR_RELEASE="pr_release";
  static final String PR_SERVICELEVEL="pr_servicelevel";
  static final String PR_DELTAVERSION="pr_deltaversion";
  static final String PR_OWNER="pr_owner";
  static final String PR_PREDECESSORCOUNTER="pr_predecessorcounter";
  static final String PR_REQUIREDVERSIONS="pr_requiredversions";
  static final String PR_EXPORTSEQUENCE="pr_exportsequence";
  static final String PR_SPVENDOR="pr_spvendor";
  static final String PR_SPNAME="pr_spname";
  static final String PR_SPLOCATION="pr_splocation";
  static final String PR_SPCOUNTER="pr_spcounter";
  static final String PR_DEPLOYARCHIVEDIR="pr_deployarchivedir";
  static final String PR_BUILDARCHIVEDIR="pr_buildarchivedir";
  static final String PR_SOURCEARCHIVEDIR="pr_sourcearchivedir";
  
  static final String PR_ORIGINALDEPLOYARCHIVEDIR="pr_originaldeployarchivedir";
  static final String PR_ORIGINALBUILDARCHIVEDIR="pr_originalbuildarchivedir";
  static final String PR_ORIGINALSOURCEARCHIVEDIR="pr_originalsourcearchivedir";
  
  static final String PR_CHANGELISTNUMBERS="pr_changelistnumbers";
  static final String PR_DTRLOCATION="pr_dtrlocation";
  static final String PR_WSCONTENTEXPORTED="pr_wscontentexported";
  static final String PR_SOURCEPOINTER="pr_sourcepointer";
  static final String PR_UPDATEVERSION="pr_updateversion";
  static final String UPDATEVERSION="updateversion";
  static final String PR_XILOCATION="pr_xilocation";
  static final String PR_XIWSCONTENTEXPORTED="pr_xiwscontentexported";
  static final String PR_XICHANGELISTNUMBERS="pr_xichangelistnumbers";
  static final String PR_CMSKEYCOUNTER="pr_cmskeycounter";
  static final String PR_VERSIONHISTORY="pr_versionhistory";
  static final String PR_COMPONENTCONFIGURATION="pr_componentconfiguration";
  static final String PR_PATCHLEVEL="pr_patchlevel";
  static final String PR_APPROVALSTATUS="pr_approvalstatus";
  static final String APPROVALSTATUS="approvalstatus";
  static final String PR_DTRWSPROPAGATIONLISTID="pr_dtrwspropagationlistid";
  static final String PR_PHYSICALSOURCEINCLUDED="pr_physicalsourceincluded";
  static final String PR_DTRREPOSITORYGUID="pr_dtrrepositoryguid";
  static final String PR_DTRISN="pr_dtrisn";
  static final String PR_DTRWORKSPACEGUID = "pr_dtrworkspaceguid";
  static final String PR_EXTCHANGELISTNUMBERS="pr_ext_changelistnumbers";
  static final String PR_ASSEMBLYCONSISTENCE="pr_assemblyconsistence";
  
  static final String CE_SOURCEARCHIVEDIR="ce_sourcearchivedir";
  static final String CE_ORIGINALSOURCEARCHIVEDIR="ce_originalsourcearchivedir";
  
  static final String SELECT_DEPLOYTARGET="select_deploytarget";
  static final String SELECT_SLPROCESSTYPE="select_slprocesstype";
  static final String DEPLOYTARGET="deploytarget";
 
  static final String JARSAP_MODE="jarsapmode";
  static final String DCA_MODE="dcamode";
  
  static final String WRITESTRUCTUREINFO    = "jarsap.info.dir";
  static final String WSI_CODELINE          = "jarsap.info.rel";
  static final String WSI_PROJECT           = "jarsap.info.project";
  static final String WSI_CONFIGURATION     = "jarsap.info.configuration";
  static final String WSI_COMPONENT2ARCHIVE = "component2archive.lst";
  static final String WSI_SC2DC             = "sc2dc.lst";
  static final String WSI_ARCHIVE2ARCHIVE   = "archive2archive.lst";

  static final String[] exattributes={ATTJARSAPVERSION,ATTJARSLVERSION,ATTPUBLIC,ATTSAPINTERNAL,
                                      ATTJARSLFINGERPRINT,ATTDEPENDENCIES,ATTDEPLOYFILE,
                                      ATTCHANGELISTNUMBER,ATTPERFORCESERVER,ATTPLATTFORM,ATTDOCFILE,
                                      ATTSOFTWARETYPE,ATTSDMSDACOMPVERSION,ATTSCACREATIONMODE,
                                      ATTCOMPRESS,ATTJARSAPPROCESSINGVERSION,ATTSOFTPPMSNUMBER,
                                      ATTSOFTVENDOR,ATTSOFTNAME,ATTSOFTVERSION,ATTEXPORTSCVERSION,
                                      ATTSPNUMBER,ATTPROJECTNAME,ATTCONTENT,ATTDEPENDENCYLIST,
                                      ATTDTRINTEGRATIONSEQUENCENO,ATTDTRWORKSPACE,ATTSPPATCHLEVEL,
                                      ATTRELEASE,ATTSCAFROMDIR,ATTSCAFROMLIST,ATTCOMPONENTELEMENT,
                                      ATTEXTSDMSDACOMPVERSION,ATTTCSPRAVERSION,ATTSCACREATIONWITHOUTIDCHECK,
                                      ATTSCACREATIONWITHOUTSDAEXISTENCECHECK,ATTSOFTWARESUBTYPE,
                                                                                                              
                                      PR_TYPE,PR_SOFTWARECOMPONENTVENDOR,PR_SOFTWARECOMPONENTNAME,
                                      PR_RELEASE,PR_SERVICELEVEL,PR_DELTAVERSION,PR_OWNER,
                                      PR_PREDECESSORCOUNTER,PR_REQUIREDVERSIONS,PR_EXPORTSEQUENCE,
                                      PR_SPVENDOR,PR_SPNAME,PR_SPLOCATION,PR_SPCOUNTER,
                                      PR_DEPLOYARCHIVEDIR,PR_BUILDARCHIVEDIR,PR_SOURCEARCHIVEDIR,
                                      PR_CHANGELISTNUMBERS,PR_DTRLOCATION,PR_WSCONTENTEXPORTED,
                                      PR_SOURCEPOINTER,PR_UPDATEVERSION,PR_XILOCATION,
                                      PR_XIWSCONTENTEXPORTED,PR_XICHANGELISTNUMBERS,PR_CMSKEYCOUNTER,
                                      PR_VERSIONHISTORY,PR_COMPONENTCONFIGURATION,PR_PATCHLEVEL,
                                      PR_APPROVALSTATUS,PR_DTRWSPROPAGATIONLISTID,PR_PHYSICALSOURCEINCLUDED,
                                      PR_DTRREPOSITORYGUID,PR_DTRISN,PR_DTRWORKSPACEGUID,
                                      PR_EXTCHANGELISTNUMBERS,PR_ASSEMBLYCONSISTENCE,
                                      PR_ORIGINALDEPLOYARCHIVEDIR,PR_ORIGINALBUILDARCHIVEDIR,PR_ORIGINALSOURCEARCHIVEDIR,
                                      
                                      CE_SOURCEARCHIVEDIR,CE_ORIGINALSOURCEARCHIVEDIR,
                                      
                                      SELECT_DEPLOYTARGET,SELECT_SLPROCESSTYPE,
                     
                                      ATTKEYNAME,ATTKEYVENDOR,ATTKEYLOCATION,ATTKEYCOUNTER,ATTDCNAME,
                                      ATTLOCATION,ATTCOUNTER,ATTSKIPNWDIDEPLOYMENT,ATTJARSAPSTANDALONEVERSION,
                                      ATTSAPMANIFESTORIGIN,ATTJARSLMFVERSION,ATTREFACTORINGFILE,ATTCSNCOMPONENT,ATTBUNDLEID,
                                      ATTARCHIVETYPE};
                                      
  static final String[] bothattributes={PR_CMSKEYCOUNTER,PR_VERSIONHISTORY,PR_COMPONENTCONFIGURATION,
                                        PR_PATCHLEVEL,PR_APPROVALSTATUS,PR_DTRWSPROPAGATIONLISTID,
                                        ATTSKIPNWDIDEPLOYMENT,PR_PHYSICALSOURCEINCLUDED,PR_DTRREPOSITORYGUID,
                                        PR_DTRISN,PR_DTRWORKSPACEGUID,PR_EXTCHANGELISTNUMBERS,PR_ASSEMBLYCONSISTENCE,
                                        SELECT_DEPLOYTARGET,SELECT_SLPROCESSTYPE,ATTREFACTORINGFILE,ATTDCNAME,ATTCSNCOMPONENT,
                                        CE_SOURCEARCHIVEDIR,ATTBUNDLEID,ATTARCHIVETYPE};   
                                        
  // in future every attribute with SELECT_ prefix may be automatically an exattribute, a bothattribute and a selectattribute
  static final String[] selectionattributes={SELECT_DEPLOYTARGET,SELECT_SLPROCESSTYPE,ATTSOFTWARESUBTYPE,ATTSOFTWARETYPE,
                                             ATTDEPENDENCYLIST,ATTCOMPONENTELEMENT,ATTDEPLOYFILE,ATTREFACTORINGFILE,ATTCSNCOMPONENT,
                                             ATTARCHIVETYPE};                                                                       
}