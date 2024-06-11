package org.example.device.config.enable;

import com.beanit.iec61850bean.*;
import org.example.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.util.*;

public class DevTool {
    private static final Logger logger = LoggerFactory.getLogger(DevTool.class);

    public DevTool() {
    }

    public static void main(String[] args) {
        try {
            if (args == null || args.length != 3) {
                System.out.println("参数个数不对");
            }
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String icdFileName = args[2];
            getReportEnable(host, port, icdFileName);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static void getReportEnable(String host, int port, String icdFileName) throws Throwable {

        ClientSap clientSap = new ClientSap();
        System.out.println("开始连接设备 ip:" + host + "....");
        ClientAssociation clientAssociation = clientSap.associate(InetAddress.getByName(host), port,
            null, null);
        System.out.println("连接设备成功.....等待获取设备数据....");

        reportEnableFind(clientAssociation, icdFileName, host);
    }

    public static void reportEnableFind(ClientAssociation clientAssociation, String icdFileName,
        String host) throws Throwable {

        try {

            ServerModel serverModel = null;
            boolean flg = false;

            try {
                serverModel = clientAssociation.retrieveModel();
                flg = true;
            } catch (Exception var61) {
                List<ServerModel> serverModels =
                    SclParser.parse((new File(System.getProperty("user.dir"), icdFileName)).getAbsolutePath());
                serverModel = serverModels.get(0);
            }

            Collection<Brcb> brcbs = serverModel.getBrcbs();
            Collection<Urcb> urcbs = serverModel.getUrcbs();
            File file = new File(System.getProperty("user.dir"), host + ".txt");
            File fileAll = new File(System.getProperty("user.dir"), host + "_all.txt");
            StringBuffer sb = new StringBuffer();
            Map<String, Map<String, List<String>>> brcbMap = new HashMap();
            Map<String, String> rcbAll = new HashMap();
            sb.append("brcb相关的ref和使能信息如下:\n");
            rcbAll.put("brcb", "brcb相关的ref和使能信息如下:\\n");
            int i = 0;
            Iterator<Brcb> brcbsIter = brcbs.iterator();

            String parentRef;
            String rcbValue;
            Map<String, List<String>> map;
            while (brcbsIter.hasNext()) {
                Brcb brcb = brcbsIter.next();
                if (!flg) {
                    try {
                        clientAssociation.enableReporting(brcb);
                        clientAssociation.disableReporting(brcb);
                    } catch (Exception var60) {
                    }
                }

                clientAssociation.getRcbValues(brcb);
                ++i;

                if (i == 2 || i == 3) {
                    clientAssociation.enableReporting(brcb);
                }

                clientAssociation.getRcbValues(brcb);
                rcbAll.put(String.valueOf(i), "ref:" + brcb.getReference().toString() + "\t" + "enabled:" + brcb.getRptEna().getValueString() + "\n");
                if (!brcb.getRptEna().getValue()) {
                    parentRef = brcb.getParent().getReference().toString();
                    if (brcbMap.containsKey(parentRef)) {
                        map = brcbMap.get(parentRef);
                    } else {
                        map = new HashMap();
                    }

                    rcbValue = brcb.getDatSet().getStringValue();
                    List<String> infos;
                    if (map.containsKey(rcbValue)) {
                        infos = map.get(rcbValue);
                    } else {
                        infos = new ArrayList();
                    }

                    infos.add("ref:" + brcb.getReference().toString() + "\t" + "enabled:" + brcb.getRptEna().getValueString() + "\n");
                    map.put(rcbValue, infos);
                    brcbMap.put(parentRef, map);
                }
            }

            List<String> bkeysList = new ArrayList<>();
            bkeysList.addAll(brcbMap.keySet());
            Collections.sort(bkeysList);
            Iterator bkeysListIter = bkeysList.iterator();

            String info;
            while (bkeysListIter.hasNext()) {
                String key = (String) bkeysListIter.next();
                sb.append("LDevice," + key + "下的brcb\n");
                List<String> dataSetsList = new ArrayList();
                dataSetsList.addAll(brcbMap.get(key).keySet());
                Collections.sort(dataSetsList);
                Iterator<String> dataSetsListIter = dataSetsList.iterator();

                while (dataSetsListIter.hasNext()) {
                    parentRef = dataSetsListIter.next();
                    sb.append("数据集:" + parentRef + "对应的可用报告控制模块\n");
                    List<String> infos = brcbMap.get(key).get(parentRef);
                    Collections.sort(infos);
                    Iterator<String> infosIter = infos.iterator();
                    while (infosIter.hasNext()) {
                        info = infosIter.next();
                        sb.append(info + "\n");
                    }
                }
            }

            sb.append("-------------------------------\n");
            sb.append("urcb相关的ref和使能信息如下:\n");
            rcbAll.put("urcb", "urcb相关的ref和使能信息如下:\n");
            Map<String, Map<String, List<String>>> urcbMap = new HashMap();
            Iterator var76 = urcbs.iterator();

            while (var76.hasNext()) {
                Urcb urcb = (Urcb) var76.next();
                if (!flg) {
                    try {
                        clientAssociation.enableReporting(urcb);
                        clientAssociation.disableReporting(urcb);
                    } catch (Exception var59) {
                    }
                }

                clientAssociation.getRcbValues(urcb);
                ++i;
                rcbAll.put(String.valueOf(i), "uref:" + urcb.getReference().toString() + "\t" + "enabled:" + urcb.getRptEna().getValueString() + "\n");
                if (!urcb.getRptEna().getValue()) {
                    parentRef = urcb.getParent().getReference().toString();
                    if (urcbMap.containsKey(parentRef)) {
                        map = urcbMap.get(parentRef);
                    } else {
                        map = new HashMap();
                    }

                    rcbValue = urcb.getDatSet().getStringValue();
                    List infos;
                    if (map.containsKey(rcbValue)) {
                        infos = map.get(rcbValue);
                    } else {
                        infos = new ArrayList();
                    }

                    infos.add("ref:" + urcb.getReference().toString() + "\t" + "enabled:" + urcb.getRptEna().getValueString() + "\n");
                    map.put(rcbValue, infos);
                    urcbMap.put(parentRef, map);
                }
            }

            List<String> ukeysList = new ArrayList();
            ukeysList.addAll(urcbMap.keySet());
            Collections.sort(ukeysList);
            Iterator ukeysListIter = ukeysList.iterator();

            Iterator dataSetsListIter;
            while (ukeysListIter.hasNext()) {
                String key = (String) ukeysListIter.next();
                sb.append("LDevice," + key + "下的brcb\n");
                List<String> dataSetsList = new ArrayList();
                dataSetsList.addAll(urcbMap.get(key).keySet());
                Collections.sort(dataSetsList);
                dataSetsListIter = dataSetsList.iterator();

                while (dataSetsListIter.hasNext()) {
                    rcbValue = (String) dataSetsListIter.next();
                    sb.append("数据集:" + rcbValue + "对应的可用报告控制模块\n");
                    List<String> infos = urcbMap.get(key).get(rcbValue);
                    Collections.sort(infos);
                    Iterator infosIter = infos.iterator();
                    while (infosIter.hasNext()) {
                        info = (String) infosIter.next();
                        sb.append(info + "\n");
                    }
                }
            }

            Throwable var79 = null;
            parentRef = null;

            try {
                FileOutputStream out = new FileOutputStream(file);

                try {
                    out.write(sb.toString().getBytes());
                    out.flush();
                } finally {
                    if (out != null) {
                        out.close();
                    }

                }
            } catch (Throwable var65) {
                if (var79 == null) {
                    var79 = var65;
                } else if (var79 != var65) {
                    var79.addSuppressed(var65);
                }

                throw var79;
            }

            Collection<String> rcbAllValue = rcbAll.values();
            List<String> rcbAllList = new ArrayList();
            rcbAllList.addAll(rcbAllValue);
            Collections.sort(rcbAllList);
            StringBuffer sb1 = new StringBuffer();
            Iterator<String> rcbAllListIter = rcbAllList.iterator();

            while (rcbAllListIter.hasNext()) {
                rcbValue = rcbAllListIter.next();
                sb1.append(rcbValue + "\n");
            }

            Throwable var86 = null;
            info = null;

            try {
                FileOutputStream out = new FileOutputStream(fileAll);

                try {
                    out.write(sb1.toString().getBytes());
                    out.flush();
                } finally {
                    if (out != null) {
                        out.close();
                    }

                }
            } catch (Throwable var63) {
                if (var86 == null) {
                    var86 = var63;
                } else if (var86 != var63) {
                    var86.addSuppressed(var63);
                }

                throw var86;
            }

            System.out.println("设备数据获取成功，筛选可用的报告控制模块数据保存路径:" + file.getAbsolutePath());
            System.out.println("设备数据获取成功，全部报告控制模的数据保存路径:" + fileAll.getAbsolutePath());
            clientAssociation.disconnect();
            clientAssociation.close();
        } catch (Exception var66) {
            if (var66 instanceof ServiceError) {
                ServiceError e1 = (ServiceError) var66;
                System.out.println("errorCode:" + e1.getErrorCode());
                logger.error("设备数据获取失败: " + var66.toString());
            }

            var66.printStackTrace();
        }
    }

    public static Map<String, Map<String, List<Brcb>>>[] reportEnableFindBrcb(ClientAssociation clientAssociation,
        ServerModel serverModelLocal) {

        Map[] array = new HashMap[2];

        Map<String, Map<String, List<Brcb>>> freeBrcbMap = new HashMap<>();
        Map<String, Map<String, List<Brcb>>> allBrcbMap = new HashMap<>();

        allBrcbMap = reportEnableFindAllBrcb(clientAssociation, serverModelLocal);
        array[0] = allBrcbMap;
        array[1] = freeBrcbMap;
        return array;
    }

    public static Map<String, Map<String, List<Urcb>>>[] reportEnableFindUrcb(ClientAssociation clientAssociation,
        ServerModel serverModelLocal) {
        Map[] array = new HashMap[2];
        Map<String, Map<String, List<Urcb>>> freeUrcbMap = new HashMap<>();
        Map<String, Map<String, List<Urcb>>> allUrcbMap = new HashMap<>();
        allUrcbMap = reportEnableFindAllUrcb(clientAssociation, serverModelLocal);
        array[0] = allUrcbMap;
        array[1] = freeUrcbMap;
        return array;
    }

    public static Map<String, Map<String, List<Urcb>>> reportEnableFindAllUrcb(ClientAssociation clientAssociation,
        ServerModel serverModelLocal) {
        Map<String, Map<String, List<Urcb>>> allUrcbMap = new HashMap<>();
        try {
            ServerModel serverModel = serverModelLocal;
            if (!Config.getLocalICD()) {
                try {
                    serverModel = clientAssociation.retrieveModel();
                } catch (Exception e) {
                    serverModel = serverModelLocal;
                }
            }
            Collection<Urcb> urcbs = serverModel.getUrcbs();
            allUrcbMap = sortUrcbList(urcbs, true);
        } catch (Exception var66) {
            if (var66 instanceof ServiceError) {
                ServiceError e1 = (ServiceError) var66;
                System.out.println("errorCode:" + e1.getErrorCode());
            }
            var66.printStackTrace();
        }
        return allUrcbMap;
    }

    public static Map<String, Map<String, List<Brcb>>> reportEnableFindAllBrcb(ClientAssociation clientAssociation,
        ServerModel serverModelLocal) {
        Map<String, Map<String, List<Brcb>>> allUrcbMap = new HashMap<>();
        try {
            ServerModel serverModel = serverModelLocal;
            if (!Config.getLocalICD()) {
                try {
                    serverModel = clientAssociation.retrieveModel();
                } catch (Exception e) {
                    serverModel = serverModelLocal;
                }
            }
            Collection<Brcb> brcbs = serverModel.getBrcbs();
            allUrcbMap = sortBrcbList(brcbs, true);
        } catch (Exception var66) {
            if (var66 instanceof ServiceError) {
                ServiceError e1 = (ServiceError) var66;

                System.out.println("errorCode:" + e1.getErrorCode());
            }
            var66.printStackTrace();
        }
        return allUrcbMap;
    }

    public static String reportDevRef(ClientAssociation clientAssociation) {
        String parentRef = "";

        try {
            ServerModel serverModel = null;
            serverModel = clientAssociation.retrieveModel();
            Collection<Brcb> brcbs = serverModel.getBrcbs();
            Iterator<Brcb> brcbsIter = brcbs.iterator();

            while (brcbsIter.hasNext()) {
                Brcb brcb = brcbsIter.next();
                clientAssociation.getRcbValues(brcb);
                parentRef = brcb.getParent().getReference().toString();
                break;
            }
        } catch (Exception var66) {
            if (var66 instanceof ServiceError) {
                ServiceError e1 = (ServiceError) var66;
                System.out.println("errorCode:" + e1.getErrorCode());
            }
            var66.printStackTrace();
        }
        return parentRef;
    }

    public static Map<String, Map<String, List<Brcb>>> sortBrcbList(Collection<Brcb> brcbs, Boolean isAllOrFree) {
        Map<String, Map<String, List<Brcb>>> rcbMap = new HashMap<>();

        try {
            ArrayList<Brcb> brcbs1 = new ArrayList<>(brcbs);
            brcbs1.sort(new ComparatorByRcb());
            Iterator<Brcb> brcbsIter = brcbs1.iterator();
            String parentRef = "";
            while (brcbsIter.hasNext()) {
                Brcb brcb = brcbsIter.next();
                Map<String, List<Brcb>> map;
                parentRef = brcb.getParent().getReference().toString();
                if (rcbMap.containsKey(parentRef)) {
                    map = rcbMap.get(parentRef);
                } else {
                    map = new TreeMap<>();
                }
                String rcbValue = brcb.getReference().toString().substring(0, brcb.getReference().toString().length() - 2);
                List brcbList;
                if (map.containsKey(rcbValue)) {
                    brcbList = map.get(rcbValue);
                } else {
                    brcbList = new ArrayList();
                }
                brcbList.add(brcb);
                map.put(rcbValue, brcbList);
                rcbMap.put(parentRef, map);
            }
        } catch (Exception var66) {
            if (var66 instanceof ServiceError) {
                ServiceError e1 = (ServiceError) var66;
                System.out.println("errorCode:" + e1.getErrorCode());
            }
            var66.printStackTrace();
        }
        return rcbMap;
    }

    public static Map<String, Map<String, List<Urcb>>> sortUrcbList(Collection<Urcb> brcbs, Boolean isAllOrFree) {
        Map<String, Map<String, List<Urcb>>> rcbMap = new HashMap<>();

        try {
            ArrayList<Urcb> brcbs1 = new ArrayList<>(brcbs);
            brcbs1.sort(new ComparatorByRcb());
            Iterator<Urcb> brcbsIter = brcbs1.iterator();
            String parentRef = "";
            while (brcbsIter.hasNext()) {
                Urcb brcb = brcbsIter.next();
                Map<String, List<Urcb>> map;
                parentRef = brcb.getParent().getReference().toString();
                if (rcbMap.containsKey(parentRef)) {
                    map = rcbMap.get(parentRef);
                } else {
                    map = new TreeMap<>();
                }
                String rcbValue = brcb.getReference().toString().substring(0, brcb.getReference().toString().length() - 2);
                List brcbList;
                if (map.containsKey(rcbValue)) {
                    brcbList = map.get(rcbValue);
                } else {
                    brcbList = new ArrayList();
                }
                brcbList.add(brcb);
                map.put(rcbValue, brcbList);
                rcbMap.put(parentRef, map);
            }
        } catch (Exception var66) {
            if (var66 instanceof ServiceError) {
                ServiceError e1 = (ServiceError) var66;
                System.out.println("errorCode:" + e1.getErrorCode());
            }
            var66.printStackTrace();
        }
        return rcbMap;
    }

}
