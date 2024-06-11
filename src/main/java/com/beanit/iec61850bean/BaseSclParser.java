package com.beanit.iec61850bean;

import com.alibaba.fastjson.JSONObject;
import com.beanit.iec61850bean.internal.scl.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.config.Config;
import org.example.device.config.DeviceConfig;
import org.example.device.config.mapper.AttributeMapper;
import org.example.device.entity.DOItem;
import org.example.device.entity.LNDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

@Slf4j
public class BaseSclParser {
    private static final Logger logger = LoggerFactory.getLogger(BaseSclParser.class);

    private final Map<String, DataSet> dataSetsMap = new HashMap<>();

    private final List<LnSubDef> dataSetDefs = new ArrayList<>();

    private TypeDefinitions typeDefinitions;

    private Document doc;

    private String iedName;

    private Map<String, String> dataInfo;

    private DeviceConfig deviceConfig;

    private List<ServerModel> serverModels = new ArrayList<>();

    private boolean useResvTmsAttributes = false;

    private List<String> reportRefs = new ArrayList<>();

    private List<LNDevice> lnDevices = new ArrayList<>();

    private Map<String, Integer> refDeviceMapper = new HashMap<>();

    public BaseSclParser(String devType, String devName) {
        this.dataInfo = Config.getDataInfo().get(devType);
//        this.deviceConfig = Config.getDeviceConfigs().get(devName);
    }

    public List<ServerModel> parse(InputStream is) throws SclParseException {
        parseStream(is);
        return this.serverModels;
    }

    public List<ServerModel> parse(String sclFilePath) throws SclParseException {
        try {
            return parse(new FileInputStream(sclFilePath));
        } catch (FileNotFoundException e) {
            throw new SclParseException(e);
        }
    }

    private void parseStream(InputStream icdFileStream) throws SclParseException {
        this.typeDefinitions = new TypeDefinitions();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        try {
            this.doc = factory.newDocumentBuilder().parse(icdFileStream);
        } catch (Exception e) {
            throw new SclParseException(e);
        }
        Node rootNode = this.doc.getDocumentElement();
        if (!"SCL".equals(rootNode.getNodeName()))
            throw new SclParseException("Root node in SCL file is not of type \"SCL\"");
        readTypeDefinitions();
        NodeList iedList = this.doc.getElementsByTagName("IED");
        if (iedList.getLength() == 0)
            throw new SclParseException("No IED section found!");
        for (int z = 0, iedListLength = iedList.getLength(); z < iedListLength; z++) {
            Node iedNode = iedList.item(z);
            this.useResvTmsAttributes = false;
            Node nameAttribute = iedNode.getAttributes().getNamedItem("name");
            this.iedName = nameAttribute.getNodeValue();
            if (this.iedName == null || this.iedName.length() == 0)
                throw new SclParseException("IED must have a name!");
            NodeList iedElements = iedNode.getChildNodes();
            for (int i = 0, childItemCount = iedElements.getLength(); i < childItemCount; i++) {
                Node element = iedElements.item(i);
                String nodeName = element.getNodeName();
                if (!"#text".equals(nodeName))
                    if ("AccessPoint".equals(nodeName)) {
                        ServerSap serverSap = createAccessPoint(element);
                        if (serverSap != null)
                            this.serverModels.add(serverSap.serverModel);
                    } else if ("Services".equals(nodeName)) {
                        NodeList servicesElements = element.getChildNodes();
                        for (int j = 0; j < servicesElements.getLength(); j++) {
                            if ("ReportSettings".equals(servicesElements.item(j).getNodeName())) {
                                Node resvTmsAttribute = servicesElements.item(j).getAttributes().getNamedItem("resvTms");
                                if (resvTmsAttribute != null)
                                    this.useResvTmsAttributes = resvTmsAttribute.getNodeValue().equalsIgnoreCase("true");
                            }
                        }
                    }
            }
        }
    }

    private void readTypeDefinitions() throws SclParseException {
        NodeList dttSections = this.doc.getElementsByTagName("DataTypeTemplates");
        if (dttSections.getLength() != 1)
            throw new SclParseException("Only one DataTypeSection allowed");
        Node dtt = dttSections.item(0);
        NodeList dataTypes = dtt.getChildNodes();
        for (int i = 0; i < dataTypes.getLength(); i++) {
            Node element = dataTypes.item(i);
            String nodeName = element.getNodeName();
            if (!"#text".equals(nodeName))
                if (nodeName.equals("LNodeType")) {
                    this.typeDefinitions.putLNodeType(new LnType(element));
                } else if (nodeName.equals("DOType")) {
                    this.typeDefinitions.putDOType(new DoType(element));
                } else if (nodeName.equals("DAType")) {
                    this.typeDefinitions.putDAType(new DaType(element));
                } else if (nodeName.equals("EnumType")) {
                    this.typeDefinitions.putEnumType(new EnumType(element));
                }
        }
    }

    private ServerSap createAccessPoint(Node accessPointNode) throws SclParseException {
        ServerSap serverSap = null;
        NodeList elements = accessPointNode.getChildNodes();
        for (int i = 0, childItemCount = elements.getLength(); i < childItemCount; i++) {
            Node element = elements.item(i);
            if (element.getNodeName().equals("Server")) {
                ServerModel server = createServerModel(element);
                Node namedItem = accessPointNode.getAttributes().getNamedItem("name");
                if (namedItem == null)
                    throw new SclParseException("AccessPoint has no name attribute!");
                serverSap = new ServerSap(this.deviceConfig.getPort(), 0, null, server, null);
                break;
            }
        }
        return serverSap;
    }

    private ServerModel createServerModel(Node serverXMLNode) throws SclParseException {
        NodeList elements = serverXMLNode.getChildNodes();
        List<LogicalDevice> logicalDevices = new ArrayList<>(elements.getLength());
        for (int i = 0, k = elements.getLength(); i < k; i++) {
            Node element = elements.item(i);
            if (element.getNodeName().equals("LDevice"))
                logicalDevices.add(createNewLDevice(element));
        }
        ServerModel serverModel = new ServerModel(logicalDevices, null);
        for (LnSubDef dataSetDef : this.dataSetDefs) {
            DataSet dataSet = createDataSet(serverModel, dataSetDef.logicalNode, dataSetDef.defXmlNode);
            this.dataSetsMap.put(dataSet.getReferenceStr(), dataSet);
        }
        serverModel.addDataSets(this.dataSetsMap.values());
        return serverModel;
    }

    private LogicalDevice createNewLDevice(Node ldXmlNode) throws SclParseException {
        String ref, inst = null;
        String ldName = null;
        NamedNodeMap attributes = ldXmlNode.getAttributes();
        for (int i = 0, k = attributes.getLength(); i < k; i++) {
            Node node = attributes.item(i);
            String nodeName = node.getNodeName();
            if (nodeName.equals("inst")) {
                inst = node.getNodeValue();
            } else if (nodeName.equals("ldName")) {
                ldName = node.getNodeValue();
            }
        }
        if (inst == null)
            throw new SclParseException("Required attribute \"inst\" in logical device not found!");
        NodeList elements = ldXmlNode.getChildNodes();
        List<LogicalNode> logicalNodes = new ArrayList<>();
        if (ldName != null && ldName.length() != 0) {
            ref = ldName;
        } else {
            ref = this.iedName + inst;
        }
        for (int j = 0, childCount = elements.getLength(); j < childCount; j++) {
            Node element = elements.item(j);
            if (element.getNodeName().equals("LN") || element.getNodeName().equals("LN0"))
                logicalNodes.add(createNewLogicalNode(element, ref, inst));
        }
        return new LogicalDevice(new ObjectReference(ref), logicalNodes);
    }

    private LogicalNode createNewLogicalNode(Node lnXmlNode, String parentRef,
        String parentInst) throws SclParseException {
        String inst = null;
        String lnClass = null;
        String lnType = null;
        String prefix = "";
        String lnDesc = "";
        String lnSensorid = "";
        String lnEquipmentid = "";
        String phase = "";
        String type = null;
        AttributeMapper attributeMapper = this.deviceConfig.getAttributeMapper();
        NamedNodeMap attributes = lnXmlNode.getAttributes();
        for (int i = 0, attrCount = attributes.getLength(); i < attrCount; i++) {
            Node node = attributes.item(i);
            String nodeName = node.getNodeName();
            if (nodeName.equals("inst")) {
                inst = node.getNodeValue();
            } else if (nodeName.equals("lnType")) {
                lnType = node.getNodeValue();
            } else if (nodeName.equals("lnClass")) {
                lnClass = node.getNodeValue();
            } else if (nodeName.equals("prefix")) {
                prefix = node.getNodeValue();
            } else if (nodeName.equals(attributeMapper.getLnDesc())) {
                lnDesc = node.getNodeValue();
            }
        }
        if (inst == null)
            throw new SclParseException("Required attribute \"inst\" not found!");
        if (lnType == null)
            throw new SclParseException("Required attribute \"lnType\" not found!");
        if (lnClass == null)
            throw new SclParseException("Required attribute \"lnClass\" not found!");
        String kk = parentInst + "_" + lnClass + "_" + inst;
        if (attributeMapper.getIds().get(kk) != null) {
            lnSensorid = (attributeMapper.getIds().get(kk)).get("ext:uri");
            lnEquipmentid = (attributeMapper.getIds().get(kk)).get("ext:devid");
            phase = (attributeMapper.getIds().get(kk)).get("ext:Phase");
            type = (attributeMapper.getIds().get(kk)).get("ext:type");
            attributeMapper.getIds().get(kk).put("ext:desc", lnDesc);
        }
        String ref = parentRef + '/' + prefix + lnClass + inst;
        LnType lnTypeDef = this.typeDefinitions.getLNodeType(lnType);
        List<FcDataObject> dataObjects = new ArrayList<>();
        if (lnTypeDef == null)
            throw new SclParseException("LNType " + lnType + " not defined!");
        NodeList childeNodes = lnXmlNode.getChildNodes();
        for (Do dobject : lnTypeDef.dos) {
            Node doiNodeFound = null;
            for (int n = 0, i1 = childeNodes.getLength(); n < i1; n++) {
                Node childNode = childeNodes.item(n);
                if ("DOI".equals(childNode.getNodeName())) {
                    NamedNodeMap doiAttributes = childNode.getAttributes();
                    Node nameAttribute = doiAttributes.getNamedItem("name");
                    if (nameAttribute != null && nameAttribute.getNodeValue().equals(dobject.getName())) {
                        doiNodeFound = childNode;
                        break;
                    }
                }
            }
            dataObjects.addAll(createFcDataObjects(dobject.getName(), ref, dobject.getType(), doiNodeFound));
        }
        for (int j = 0, childCount = childeNodes.getLength(); j < childCount; j++) {
            Node childNode = childeNodes.item(j);
            if ("ReportControl".equals(childNode.getNodeName()))
                dataObjects.addAll(createReportControlBlocks(childNode, ref));
        }
        LogicalNode lNode = new LogicalNode(new ObjectReference(ref), dataObjects);
        for (int k = 0, m = childeNodes.getLength(); k < m; k++) {
            Node childNode = childeNodes.item(k);
            if ("DataSet".equals(childNode.getNodeName()))
                this.dataSetDefs.add(new LnSubDef(childNode, lNode));
        }

        if (StringUtils.isBlank(lnSensorid) || StringUtils.isBlank(lnEquipmentid)) {
            return lNode;
        }

        List<DOItem> watchedItems = new ArrayList<>();
        DOItem moDevFlt = null;
        List<DOItem> doItems = new ArrayList<>();
        for (int n = 0, i1 = childeNodes.getLength(); n < i1; n++) {
            Node childNode = childeNodes.item(n);

            if (!childNode.getNodeName().equalsIgnoreCase("DOI") || !childNode.hasAttributes()) {
                continue;
            }
            Node nameNode = childNode.getAttributes().getNamedItem("name");
            if (nameNode == null) {
                continue;
            }

            String doiName = nameNode.getNodeValue();
            String doiRef = String.format("%s.%s", ref, doiName);

            // key 为新值， value 为原值
            if (!attributeMapper.getAttrRefs().containsValue(doiName)
                && !attributeMapper.getLnMoDevFlt().equals(doiName)) {
                continue;
            }
            this.reportRefs.add(doiRef);

            DOItem doItem = new DOItem();
            doItem.setRef(doiRef);
            doItem.setSrcAttr(doiName);
            String destAttr = attributeMapper.getKeyByValue(doiName);
            if (StringUtils.isBlank(destAttr)) {
                logger.warn("[MAP-IGN]: {}-{}", this.deviceConfig.getDeviceName(), doiName);
                continue;
            }
            doItem.setDestAttr(destAttr);
            String desc = this.dataInfo.get(destAttr);
            if (desc != null) {
                doItem.setDesc(desc);
            } else {
                Node descNode = childNode.getAttributes().getNamedItem(attributeMapper.getLnDoiDesc());
                if (descNode != null) {
                    doItem.setDesc(descNode.getNodeValue());
                }
            }
            if (attributeMapper.getWatchDOIs().contains(doiName)) {
                doItem.setWatched(true);
                watchedItems.add(doItem);
            }
            doItems.add(doItem);

            this.refDeviceMapper.put(doiRef, this.lnDevices.size());

            if (attributeMapper.getLnMoDevFlt().equals(doiName)) {
                moDevFlt = doItem;
            }
        }

        if (doItems.size() > 0) {
            LNDevice lnDevice = new LNDevice();
            lnDevice.setRef(ref);
            lnDevice.setDesc(lnDesc);
            lnDevice.setDoItems(doItems);
            lnDevice.setSensorid(lnSensorid);
            if (StringUtils.isBlank(type)) {
                lnDevice.setType(this.deviceConfig.getDeviceDataTypeNumber());
            } else {
                lnDevice.setType(type);
            }
            lnDevice.setEquipmentid(lnEquipmentid);
            lnDevice.setMoDevFlt(moDevFlt);
            lnDevice.setPhase(phase);
            lnDevice.setPrefixGroupItemMap(this.deviceConfig.getAttributeMapper().getPrefixGroupItemMap());
            this.lnDevices.add(lnDevice);
            log.info("LNDevice: {}, {}, {}, {}, {}", lnSensorid, ref, lnDevice,
                this.deviceConfig.getDeviceDataTypeNumber(), doItems.size());
            logger.info("LN_WATCHES: {}, {}, {}", JSONObject.toJSONString(watchedItems), lnSensorid, lnDesc);
            lnDevice.setWatchCount(watchedItems.size());
        }

        return lNode;
    }

    private DataSet createDataSet(ServerModel serverModel, LogicalNode lNode, Node dsXmlNode) throws SclParseException {
        Node nameAttribute = dsXmlNode.getAttributes().getNamedItem("name");
        if (nameAttribute == null)
            throw new SclParseException("DataSet must have a name");
        String name = nameAttribute.getNodeValue();
        List<FcModelNode> dsMembers = new ArrayList<>();
        for (int i = 0; i < dsXmlNode.getChildNodes().getLength(); i++) {
            Node fcdaXmlNode = dsXmlNode.getChildNodes().item(i);
            if ("FCDA".equals(fcdaXmlNode.getNodeName())) {
                String ldInst = null;
                String prefix = "";
                String lnClass = null;
                String lnInst = "";
                String doName = "";
                String daName = "";
                Fc fc = null;
                NamedNodeMap attributes = fcdaXmlNode.getAttributes();
                for (int j = 0; j < attributes.getLength(); j++) {
                    Node node = attributes.item(j);
                    String nodeName = node.getNodeName();
                    if (nodeName.equals("ldInst")) {
                        ldInst = node.getNodeValue();
                    } else if (nodeName.equals("lnInst")) {
                        lnInst = node.getNodeValue();
                    } else if (nodeName.equals("lnClass")) {
                        lnClass = node.getNodeValue();
                    } else if (nodeName.equals("prefix")) {
                        prefix = node.getNodeValue();
                    } else if (nodeName.equals("doName")) {
                        doName = node.getNodeValue();
                    } else if (nodeName.equals("daName")) {
                        if (!node.getNodeValue().isEmpty())
                            daName = "." + node.getNodeValue();
                    } else if (nodeName.equals("fc")) {
                        fc = Fc.fromString(node.getNodeValue());
                        if (fc == null)
                            throw new SclParseException("FCDA contains invalid FC: " + node.getNodeValue());
                    }
                }
                if (ldInst == null)
                    throw new SclParseException("Required attribute \"ldInst\" not found in FCDA: " + nameAttribute + "!");
                if (lnClass == null)
                    throw new SclParseException("Required attribute \"lnClass\" not found in FCDA!");
                if (fc == null)
                    throw new SclParseException("Required attribute \"fc\" not found in FCDA!");
                if (!doName.isEmpty()) {
                    String objectReference = this.iedName + ldInst + "/" + prefix + lnClass + lnInst + "." + doName + daName;
                    ModelNode fcdaNode = serverModel.findModelNode(objectReference, fc);
                    if (fcdaNode == null)
                        throw new SclParseException("Specified FCDA: " + objectReference + " in DataSet: " + nameAttribute + " not found in Model.");
                    dsMembers.add((FcModelNode) fcdaNode);
                } else {
                    String objectReference = this.iedName + ldInst + "/" + prefix + lnClass + lnInst;
                    ModelNode logicalNode = serverModel.findModelNode(objectReference, null);
                    if (logicalNode == null)
                        throw new SclParseException("Specified FCDA: " + objectReference + " in DataSet: " + nameAttribute + " not found in Model.");
                    List<FcDataObject> fcDataObjects = ((LogicalNode) logicalNode).getChildren(fc);
                    dsMembers.addAll(fcDataObjects);
                }
            }
        }
        return new DataSet(lNode.getReference().toString() + '.' + name, dsMembers, false);
    }

    private List<Rcb> createReportControlBlocks(Node xmlNode, String parentRef) throws SclParseException {
        Fc fc = Fc.RP;
        NamedNodeMap rcbNodeAttributes = xmlNode.getAttributes();
        Node attribute = rcbNodeAttributes.getNamedItem("buffered");
        if (attribute != null && "true".equalsIgnoreCase(attribute.getNodeValue()))
            fc = Fc.BR;
        Node nameAttribute = rcbNodeAttributes.getNamedItem("name");
        if (nameAttribute == null)
            throw new SclParseException("Report Control Block has no name attribute.");
        int maxInstances = 1;
        for (int i = 0; i < xmlNode.getChildNodes().getLength(); i++) {
            Node childNode = xmlNode.getChildNodes().item(i);
            if ("RptEnabled".equals(childNode.getNodeName())) {
                Node rptEnabledMaxAttr = childNode.getAttributes().getNamedItem("max");
                if (rptEnabledMaxAttr != null) {
                    maxInstances = Integer.parseInt(rptEnabledMaxAttr.getNodeValue());
                    if (maxInstances < 1 || maxInstances > 99)
                        throw new SclParseException("Report Control Block max instances should be between 1 and 99 but is: " + maxInstances);
                }
            }
        }
        List<Rcb> rcbInstances = new ArrayList<>(maxInstances);
        for (int z = 1; z <= maxInstances; z++) {
            ObjectReference reportObjRef;
            if (maxInstances == 1) {
                reportObjRef = new ObjectReference(parentRef + "." + nameAttribute.getNodeValue());
            } else {
                reportObjRef = new ObjectReference(parentRef + "." + nameAttribute.getNodeValue() + String.format("%02d", z));
            }
            BdaTriggerConditions trigOps = new BdaTriggerConditions(new ObjectReference(reportObjRef + ".TrgOps"), fc);
            BdaOptFlds optFields = new BdaOptFlds(new ObjectReference(reportObjRef + ".OptFlds"), fc);
            for (int j = 0; j < xmlNode.getChildNodes().getLength(); j++) {
                Node childNode = xmlNode.getChildNodes().item(j);
                if (childNode.getNodeName().equals("TrgOps")) {
                    NamedNodeMap attributes = childNode.getAttributes();
                    if (attributes != null)
                        for (int k = 0; k < attributes.getLength(); k++) {
                            Node node = attributes.item(k);
                            String nodeName = node.getNodeName();
                            if ("dchg".equals(nodeName)) {
                                trigOps.setDataChange(node.getNodeValue().equalsIgnoreCase("true"));
                            } else if ("qchg".equals(nodeName)) {
                                trigOps.setQualityChange(node.getNodeValue().equalsIgnoreCase("true"));
                            } else if ("dupd".equals(nodeName)) {
                                trigOps.setDataUpdate(node.getNodeValue().equalsIgnoreCase("true"));
                            } else if ("period".equals(nodeName)) {
                                trigOps.setIntegrity(node.getNodeValue().equalsIgnoreCase("true"));
                            } else if ("gi".equals(nodeName)) {
                                trigOps.setGeneralInterrogation(node.getNodeValue().equalsIgnoreCase("true"));
                            }
                        }
                } else if ("OptFields".equals(childNode.getNodeName())) {
                    NamedNodeMap attributes = childNode.getAttributes();
                    if (attributes != null)
                        for (int k = 0; k < attributes.getLength(); k++) {
                            Node node = attributes.item(k);
                            String nodeName = node.getNodeName();
                            if ("seqNum".equals(nodeName)) {
                                optFields.setSequenceNumber(node.getNodeValue().equalsIgnoreCase("true"));
                            } else if ("timeStamp".equals(nodeName)) {
                                optFields.setReportTimestamp(node.getNodeValue().equalsIgnoreCase("true"));
                            } else if ("reasonCode".equals(nodeName)) {
                                optFields.setReasonForInclusion(node.getNodeValue().equalsIgnoreCase("true"));
                            } else if ("dataSet".equals(nodeName)) {
                                optFields.setDataSetName(node.getNodeValue().equalsIgnoreCase("true"));
                            } else if (nodeName.equals("bufOvfl")) {
                                optFields.setBufferOverflow(node.getNodeValue().equalsIgnoreCase("true"));
                            } else if (nodeName.equals("entryID")) {
                                optFields.setEntryId(node.getNodeValue().equalsIgnoreCase("true"));
                            }
                        }
                } else if ("RptEnabled".equals(childNode.getNodeName())) {
                    Node rptEnabledMaxAttr = childNode.getAttributes().getNamedItem("max");
                    if (rptEnabledMaxAttr != null) {
                        maxInstances = Integer.parseInt(rptEnabledMaxAttr.getNodeValue());
                        if (maxInstances < 1 || maxInstances > 99)
                            throw new SclParseException("Report Control Block max instances should be between 1 and 99 but is: " + maxInstances);
                    }
                }
            }
            if (fc == Fc.RP) {
                optFields.setEntryId(false);
                optFields.setBufferOverflow(false);
            }
            List<FcModelNode> children = new ArrayList<>();
            BdaVisibleString rptId = new BdaVisibleString(new ObjectReference(reportObjRef.toString() + ".RptID"), fc, "", 129, false, false);
            attribute = rcbNodeAttributes.getNamedItem("rptID");
            if (attribute != null) {
                rptId.setValue(attribute.getNodeValue().getBytes());
            } else {
                rptId.setValue(reportObjRef.toString());
            }
            children.add(rptId);
            children.add(new BdaBoolean(new ObjectReference(reportObjRef
                .toString() + ".RptEna"), fc, "", false, false));
            if (fc == Fc.RP)
                children.add(new BdaBoolean(new ObjectReference(reportObjRef
                    .toString() + ".Resv"), fc, "", false, false));
            BdaVisibleString datSet = new BdaVisibleString(new ObjectReference(reportObjRef.toString() + ".DatSet"), fc, "", 129, false, false);
            attribute = xmlNode.getAttributes().getNamedItem("datSet");
            if (attribute != null) {
                String nodeValue = attribute.getNodeValue();
                String dataSetName = parentRef + "$" + nodeValue;
                datSet.setValue(dataSetName.getBytes());
            }
            children.add(datSet);
            BdaInt32U confRef = new BdaInt32U(new ObjectReference(reportObjRef.toString() + ".ConfRev"), fc, "", false, false);
            attribute = xmlNode.getAttributes().getNamedItem("confRev");
            if (attribute == null)
                throw new SclParseException("Report Control Block does not contain mandatory attribute confRev");
            confRef.setValue(Long.parseLong(attribute.getNodeValue()));
            children.add(confRef);
            children.add(optFields);
            BdaInt32U bufTm = new BdaInt32U(new ObjectReference(reportObjRef.toString() + ".BufTm"), fc, "", false, false);
            attribute = xmlNode.getAttributes().getNamedItem("bufTime");
            if (attribute != null)
                bufTm.setValue(Long.parseLong(attribute.getNodeValue()));
            children.add(bufTm);
            children.add(new BdaInt8U(new ObjectReference(reportObjRef.toString() + ".SqNum"), fc, "", false, false));
            children.add(trigOps);
            BdaInt32U intgPd = new BdaInt32U(new ObjectReference(reportObjRef.toString() + ".IntgPd"), fc, "", false, false);
            attribute = xmlNode.getAttributes().getNamedItem("intgPd");
            if (attribute != null)
                intgPd.setValue(Long.parseLong(attribute.getNodeValue()));
            children.add(intgPd);
            children.add(new BdaBoolean(new ObjectReference(reportObjRef.toString() + ".GI"), fc, "", false, false));

            boolean noOwner = false;
            for (String rcbStr : Config.getOwnerRcbs()) {
                if (parentRef.startsWith(rcbStr)) {
                    noOwner = true;
                    break;
                }
            }

            String objRefStr = reportObjRef.toString();
            Rcb rcb = null;
            if (fc == Fc.BR) {
                children.add(new BdaBoolean(new ObjectReference(objRefStr + ".PurgeBuf"), fc, "", false, false));
                children.add(new BdaOctetString(new ObjectReference(objRefStr + ".EntryID"), fc, "", 8, false, false));
                children.add(new BdaEntryTime(new ObjectReference(objRefStr + ".TimeOfEntry"), fc, "", false, false));
                if (this.useResvTmsAttributes) {
                    children.add(new BdaInt16(new ObjectReference(objRefStr + ".ResvTms"), fc, "", false, false));
                }
                if (!noOwner) {
                    children.add(new BdaOctetString(
                        new ObjectReference(objRefStr + ".Owner"), fc, "", 64, false, false));
                }
                rcb = new Brcb(reportObjRef, children);
            } else {
                if (!noOwner) {
                    children.add(new BdaOctetString(
                        new ObjectReference(objRefStr + ".Owner"), fc, "", 64, false, false));
                }
                rcb = new Urcb(reportObjRef, children);
            }
            rcbInstances.add(rcb);
        }
        return rcbInstances;
    }

    private List<FcDataObject> createFcDataObjects(String name, String parentRef, String doTypeID,
        Node doiNode) throws SclParseException {
        DoType doType = this.typeDefinitions.getDOType(doTypeID);
        if (doType == null)
            throw new SclParseException("DO type " + doTypeID + " not defined!");
        String ref = parentRef + '.' + name;
        List<ModelNode> childNodes = new ArrayList<>();
        for (Da dattr : doType.das) {
            Node iNodeFound = findINode(doiNode, dattr.getName());
            if (dattr.getCount() >= 1) {
                childNodes.add(createArrayOfDataAttributes(ref + '.' + dattr.getName(), dattr, iNodeFound));
                continue;
            }
            childNodes.add(createDataAttribute(ref + '.' + dattr.getName(), dattr.getFc(), dattr, iNodeFound, false, false, false));
        }
        for (Sdo sdo : doType.sdos) {
            Node iNodeFound = findINode(doiNode, sdo.getName());
            childNodes.addAll(createFcDataObjects(sdo.getName(), ref, sdo.getType(), iNodeFound));
        }
        Map<Fc, List<FcModelNode>> subFCDataMap = new LinkedHashMap<>();
        for (Fc fc : Fc.values())
            subFCDataMap.put(fc, new LinkedList<>());
        for (ModelNode childNode : childNodes)
            subFCDataMap.get(((FcModelNode) childNode).getFc()).add((FcModelNode) childNode);
        List<FcDataObject> fcDataObjects = new LinkedList<>();
        ObjectReference objectReference = new ObjectReference(ref);
        for (Fc fc : Fc.values()) {
            if (subFCDataMap.get(fc).size() > 0)
                fcDataObjects.add(new FcDataObject(objectReference, fc, subFCDataMap.get(fc)));
        }
        return fcDataObjects;
    }

    private Node findINode(Node iNode, String dattrName) {
        if (iNode == null)
            return null;
        for (int i = 0; i < iNode.getChildNodes().getLength(); i++) {
            Node childNode = iNode.getChildNodes().item(i);
            if (childNode.getAttributes() != null) {
                Node nameAttribute = childNode.getAttributes().getNamedItem("name");
                if (nameAttribute != null && nameAttribute.getNodeValue().equals(dattrName))
                    return childNode;
            }
        }
        return null;
    }

    private Array createArrayOfDataAttributes(String ref, Da dataAttribute, Node iXmlNode) throws SclParseException {
        Fc fc = dataAttribute.getFc();
        int size = dataAttribute.getCount();
        List<FcModelNode> arrayItems = new ArrayList<>();
        for (int i = 0; i < size; i++)
            arrayItems.add(createDataAttribute(ref + '(' + i + ')', fc, dataAttribute, iXmlNode, dataAttribute.isDchg(), dataAttribute
                .isDupd(), dataAttribute.isQchg()));
        return new Array(new ObjectReference(ref), fc, arrayItems);
    }

    private FcModelNode createDataAttribute(String ref, Fc fc, AbstractDataAttribute dattr, Node iXmlNode, boolean dchg,
        boolean dupd, boolean qchg) throws SclParseException {
        if (dattr instanceof Da) {
            Da dataAttribute = (Da) dattr;
            dchg = dataAttribute.isDchg();
            dupd = dataAttribute.isDupd();
            qchg = dataAttribute.isQchg();
        }
        String bType = dattr.getbType();
        if (bType.equals("Struct")) {
            DaType datype = this.typeDefinitions.getDaType(dattr.getType());
            if (datype == null)
                throw new SclParseException("DAType " + dattr.getbType() + " not declared!");
            List<FcModelNode> subDataAttributes = new ArrayList<>();
            for (Bda bda : datype.bdas) {
                Node iNodeFound = findINode(iXmlNode, bda.getName());
                subDataAttributes
                    .add(createDataAttribute(ref + '.' + bda.getName(), fc, bda, iNodeFound, dchg, dupd, qchg));
            }
            return new ConstructedDataAttribute(new ObjectReference(ref), fc, subDataAttributes);
        }
        String val = null;
        String sAddr = null;
        if (iXmlNode != null) {
            NamedNodeMap attributeMap = iXmlNode.getAttributes();
            Node sAddrAttribute = attributeMap.getNamedItem("sAddr");
            if (sAddrAttribute != null)
                sAddr = sAddrAttribute.getNodeValue();
            NodeList elements = iXmlNode.getChildNodes();
            for (int i = 0; i < elements.getLength(); i++) {
                Node node = elements.item(i);
                if (node.getNodeName().equals("Val"))
                    val = node.getTextContent();
            }
            if (val == null)
                val = dattr.value;
        }
        if (bType.equals("BOOLEAN")) {
            BdaBoolean bda = new BdaBoolean(new ObjectReference(ref), fc, sAddr, dchg, dupd);
            if (val != null)
                if (val.equalsIgnoreCase("true") || val.equals("1")) {
                    bda.setValue(true);
                } else if (val.equalsIgnoreCase("false") || val.equals("0")) {
                    bda.setValue(false);
                } else {
                    throw new SclParseException("invalid boolean configured value: " + val);
                }
            return bda;
        }
        if (bType.equals("INT8")) {
            BdaInt8 bda = new BdaInt8(new ObjectReference(ref), fc, sAddr, dchg, dupd);
            if (val != null)
                try {
                    bda.setValue(Byte.parseByte(val));
                } catch (NumberFormatException e) {
                    throw new SclParseException("invalid INT8 configured value: " + val);
                }
            return bda;
        }
        if (bType.equals("INT16")) {
            BdaInt16 bda = new BdaInt16(new ObjectReference(ref), fc, sAddr, dchg, dupd);
            if (val != null)
                try {
                    bda.setValue(Short.parseShort(val));
                } catch (NumberFormatException e) {
                    throw new SclParseException("invalid INT16 configured value: " + val);
                }
            return bda;
        }
        if (bType.equals("INT32")) {
            BdaInt32 bda = new BdaInt32(new ObjectReference(ref), fc, sAddr, dchg, dupd);
            if (val != null)
                try {
                    bda.setValue(Integer.parseInt(val));
                } catch (NumberFormatException e) {
                    throw new SclParseException("invalid INT32 configured value: " + val);
                }
            return bda;
        }
        if (bType.equals("INT64")) {
            BdaInt64 bda = new BdaInt64(new ObjectReference(ref), fc, sAddr, dchg, dupd);
            if (val != null)
                try {
                    bda.setValue(Long.parseLong(val));
                } catch (NumberFormatException e) {
                    throw new SclParseException("invalid INT64 configured value: " + val);
                }
            return bda;
        }
        if (bType.equals("INT128")) {
            BdaInt128 bda = new BdaInt128(new ObjectReference(ref), fc, sAddr, dchg, dupd);
            if (val != null)
                try {
                    bda.setValue(Long.parseLong(val));
                } catch (NumberFormatException e) {
                    throw new SclParseException("invalid INT128 configured value: " + val);
                }
            return bda;
        }
        if (bType.equals("INT8U")) {
            BdaInt8U bda = new BdaInt8U(new ObjectReference(ref), fc, sAddr, dchg, dupd);
            if (val != null)
                try {
                    bda.setValue(Short.parseShort(val));
                } catch (NumberFormatException e) {
                    throw new SclParseException("invalid INT8U configured value: " + val);
                }
            return bda;
        }
        if (bType.equals("INT16U")) {
            BdaInt16U bda = new BdaInt16U(new ObjectReference(ref), fc, sAddr, dchg, dupd);
            if (val != null)
                try {
                    bda.setValue(Integer.parseInt(val));
                } catch (NumberFormatException e) {
                    throw new SclParseException("invalid INT16U configured value: " + val);
                }
            return bda;
        }
        if (bType.equals("INT32U")) {
            BdaInt32U bda = new BdaInt32U(new ObjectReference(ref), fc, sAddr, dchg, dupd);
            if (val != null)
                try {
                    bda.setValue(Long.parseLong(val));
                } catch (NumberFormatException e) {
                    throw new SclParseException("invalid INT32U configured value: " + val);
                }
            return bda;
        }
        if (bType.equals("FLOAT32")) {
            BdaFloat32 bda = new BdaFloat32(new ObjectReference(ref), fc, sAddr, dchg, dupd);
            if (val != null)
                try {
                    bda.setFloat(Float.parseFloat(val));
                } catch (NumberFormatException e) {
                    throw new SclParseException("invalid FLOAT32 configured value: " + val);
                }
            return bda;
        }
        if (bType.equals("FLOAT64")) {
            BdaFloat64 bda = new BdaFloat64(new ObjectReference(ref), fc, sAddr, dchg, dupd);
            if (val != null)
                try {
                    bda.setDouble(Double.parseDouble(val));
                } catch (NumberFormatException e) {
                    throw new SclParseException("invalid FLOAT64 configured value: " + val);
                }
            return bda;
        }
        if (bType.startsWith("VisString")) {
            BdaVisibleString bda = new BdaVisibleString(new ObjectReference(ref), fc, sAddr, Integer.parseInt(dattr.getbType().substring(9)), dchg, dupd);
            if (val != null)
                bda.setValue(val.getBytes());
            return bda;
        }
        if (bType.startsWith("Unicode")) {
            BdaUnicodeString bda = new BdaUnicodeString(new ObjectReference(ref), fc, sAddr, Integer.parseInt(dattr.getbType().substring(7)), dchg, dupd);
            if (val != null) {
                bda.setValue(val.getBytes());
            }
            return bda;
        }
        if (bType.startsWith("Octet")) {
            BdaOctetString bda = new BdaOctetString(new ObjectReference(ref), fc, sAddr, Integer.parseInt(dattr.getbType().substring(5)), dchg, dupd);
            if (val != null)
                ;
            return bda;
        }
        if (bType.equals("Quality"))
            return new BdaQuality(new ObjectReference(ref), fc, sAddr, qchg);
        if (bType.equals("Check"))
            return new BdaCheck(new ObjectReference(ref));
        if (bType.equals("Dbpos"))
            return new BdaDoubleBitPos(new ObjectReference(ref), fc, sAddr, dchg, dupd);
        if (bType.equals("Tcmd"))
            return new BdaTapCommand(new ObjectReference(ref), fc, sAddr, dchg, dupd);
        if (bType.equals("OptFlds"))
            return new BdaOptFlds(new ObjectReference(ref), fc);
        if (bType.equals("TrgOps"))
            return new BdaTriggerConditions(new ObjectReference(ref), fc);
        if (bType.equals("EntryID"))
            return new BdaOctetString(new ObjectReference(ref), fc, sAddr, 8, dchg, dupd);
        if (bType.equals("EntryTime"))
            return new BdaEntryTime(new ObjectReference(ref), fc, sAddr, dchg, dupd);
        if (bType.equals("PhyComAddr"))
            return new BdaOctetString(new ObjectReference(ref), fc, sAddr, 6, dchg, dupd);
        if (bType.equals("Timestamp")) {
            BdaTimestamp bda = new BdaTimestamp(new ObjectReference(ref), fc, sAddr, dchg, dupd);
//      if (val != null)
//        throw new SclParseException("parsing configured value for TIMESTAMP is not supported yet.");
            return bda;
        }
        if (bType.equals("Enum")) {
            String type = dattr.getType();
            if (type == null)
                throw new SclParseException("The exact type of the enumeration is not set.");
            EnumType enumType = this.typeDefinitions.getEnumType(type);
            if (enumType == null)
                throw new SclParseException("Definition of enum type: " + type + " not found.");
            if (enumType.max > 127 || enumType.min < -128) {
                BdaInt16 bdaInt16 = new BdaInt16(new ObjectReference(ref), fc, sAddr, dchg, dupd);
                if (val != null) {
                    for (EnumVal enumVal : enumType.getValues()) {
                        if (val.equals(enumVal.getId())) {
                            bdaInt16.setValue((short) enumVal.getOrd());
                            return bdaInt16;
                        }
                        if (val.equals(String.valueOf(enumVal.getOrd()))) {
                            bdaInt16.setValue((byte) enumVal.getOrd());
                            return bdaInt16;
                        }
                    }
                    throw new SclParseException("unknown enum value: " + val);
                }
                return bdaInt16;
            }
            BdaInt8 bda = new BdaInt8(new ObjectReference(ref), fc, sAddr, dchg, dupd);
            if (val != null) {
                for (EnumVal enumVal : enumType.getValues()) {
                    if (val.equals(enumVal.getId())) {
                        bda.setValue((byte) enumVal.getOrd());
                        return bda;
                    }
                    if (val.equals(String.valueOf(enumVal.getOrd()))) {
                        bda.setValue((byte) enumVal.getOrd());
                        return bda;
                    }
                }
                if (val.equals("0")) {
                    val = "1";
                    for (EnumVal enumVal : enumType.getValues()) {
                        if (val.equals(String.valueOf(enumVal.getOrd()))) {
                            bda.setValue((byte) enumVal.getOrd());
                            return bda;
                        }
                    }
                }
                throw new SclParseException("unknown enum value: " + val);
            }
            return bda;
        }
        if (bType.equals("ObjRef")) {
            BdaVisibleString bda = new BdaVisibleString(new ObjectReference(ref), fc, sAddr, 129, dchg, dupd);
            if (val != null)
                bda.setValue(val.getBytes());
            return bda;
        }
        throw new SclParseException("Invalid bType: " + bType);
    }

    public List<String> getReportRefs() {
        return this.reportRefs;
    }

    public List<LNDevice> getLnDevices() {
        return this.lnDevices;
    }

    public Map<String, Integer> getRefDeviceMapper() {
        return this.refDeviceMapper;
    }
}
