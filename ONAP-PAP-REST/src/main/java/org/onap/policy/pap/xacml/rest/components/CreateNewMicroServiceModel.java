/*-
 * ============LICENSE_START=======================================================
 * ONAP-PAP-REST
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modified Copyright (C) 2018 Samsung Electronics Co., Ltd.
 * Modifications Copyright (C) 2019 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.pap.xacml.rest.components;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.onap.policy.common.logging.eelf.MessageCodes;
import org.onap.policy.common.logging.eelf.PolicyLogger;
import org.onap.policy.common.logging.flexlogger.FlexLogger;
import org.onap.policy.common.logging.flexlogger.Logger;
import org.onap.policy.pap.xacml.rest.XACMLPapServlet;
import org.onap.policy.pap.xacml.rest.daoimpl.CommonClassDaoImpl;
import org.onap.policy.rest.jpa.MicroServiceModels;
import org.onap.policy.rest.jpa.UserInfo;
import org.onap.policy.rest.util.MsAttributeObject;
import org.onap.policy.rest.util.MsModelUtils;
import org.onap.policy.rest.util.MsModelUtils.ModelType;

public class CreateNewMicroServiceModel {
    private static final Logger logger = FlexLogger.getLogger(CreateNewMicroServiceModel.class);
    private MicroServiceModels newModel = null;
    private HashMap<String, MsAttributeObject> classMap = new HashMap<>();

    private MsModelUtils utils = new MsModelUtils(XACMLPapServlet.getMsOnapName(), XACMLPapServlet.getMsPolicyName());

    public CreateNewMicroServiceModel(String fileName, String serviceName, String string, String version) {
        super();
    }

    /**
     * Instantiates a new creates the new micro service model.
     *
     * @param importFile the import file
     * @param modelName the model name
     * @param description the description
     * @param version the version
     * @param randomID the random ID
     */
    public CreateNewMicroServiceModel(String importFile, String modelName, String description, String version,
            String randomID) {

        this.newModel = new MicroServiceModels();
        this.newModel.setVersion(version);
        this.newModel.setModelName(modelName);
        UserInfo userInfo = new UserInfo();
        userInfo.setUserLoginId("API");
        this.newModel.setUserCreatedBy(userInfo);
        String cleanUpFile = null;

        Map<String, MsAttributeObject> tempMap = new HashMap<>();
        // Need to delete the file
        if (importFile.contains(".zip")) {
            extractFolder(randomID + ".zip");
            File directory = new File("ExtractDir" + File.separator + randomID);
            List<File> fileList = listModelFiles(directory.toString());
            // get all the files from a director
            processFiles(modelName, fileList);
            doCleanUpFiles(randomID);
        } else {
            if (importFile.contains(".yml")) {

                processYmlModel("ExtractDir" + File.separator + randomID + ".yml", modelName);
                cleanUpFile = "ExtractDir" + File.separator + randomID + ".yml";

            } else {
                tempMap = utils.processEpackage("ExtractDir" + File.separator + randomID + ".xmi", ModelType.XMI);
                classMap.putAll(tempMap);
                cleanUpFile = "ExtractDir" + File.separator + randomID + ".xmi";
            }

            File deleteFile = new File(cleanUpFile);
            deleteFile.delete();
        }
    }

    private void processFiles(String modelName, List<File> fileList) {
        Map<String, MsAttributeObject> tempMap;
        for (File file : fileList) {
            if (file.isFile()) {
                int indx = file.getName().lastIndexOf('.');
                String type = file.getName().substring(indx + 1);

                if ("yml".equalsIgnoreCase(type)) {

                    processYmlModel(file.toString(), modelName);

                } else {

                    tempMap = utils.processEpackage(file.getAbsolutePath(), ModelType.XMI);
                    classMap.putAll(tempMap);
                }
            }
        }
    }

    private void doCleanUpFiles(String randomID) {
        String cleanUpFile;
        cleanUpFile = "ExtractDir" + File.separator + randomID + ".zip";
        try {
            FileUtils.deleteDirectory(new File("ExtractDir" + File.separator + randomID));
            FileUtils.deleteDirectory(new File(randomID));
            File deleteFile = new File(cleanUpFile);
            FileUtils.forceDelete(deleteFile);
        } catch (IOException e) {
            logger.error("Failed to unzip model file " + randomID, e);
        }
    }

    private void processYmlModel(String fileName, String modelName) {

        try {

            utils.parseTosca(fileName);

            MsAttributeObject msAttributes = new MsAttributeObject();
            msAttributes.setClassName(modelName);

            LinkedHashMap<String, String> returnAttributeList = new LinkedHashMap<>();
            returnAttributeList.put(modelName, utils.getAttributeString());
            msAttributes.setAttribute(returnAttributeList);

            msAttributes.setSubClass(utils.getRetmap());

            msAttributes.setMatchingSet(utils.getMatchableValues());

            LinkedHashMap<String, String> returnReferenceList = new LinkedHashMap<>();

            returnReferenceList.put(modelName, utils.getReferenceAttributes());
            msAttributes.setRefAttribute(returnReferenceList);

            if (!PolicyDbDao.isNullOrEmpty(utils.getListConstraints())) {
                LinkedHashMap<String, String> enumList = new LinkedHashMap<>();
                String[] listArray = utils.getListConstraints().split("#");
                for (String str : listArray) {
                    String[] strArr = str.split("=");
                    if (strArr.length > 1) {
                        enumList.put(strArr[0], strArr[1]);
                    }
                }
                msAttributes.setEnumType(enumList);
            }
            if (utils.getJsonRuleFormation() != null) {
                msAttributes.setRuleFormation(utils.getJsonRuleFormation());
            }

            if (utils.getDataOrderInfo() != null) {
                msAttributes.setDataOrderInfo(utils.getDataOrderInfo());
            }

            classMap = new LinkedHashMap<>();
            classMap.put(modelName, msAttributes);

        } catch (Exception e) {
            logger.error("Failed to process yml model" + e);
        }

    }

    private List<File> listModelFiles(String directoryName) {
        File directory = new File(directoryName);
        List<File> resultList = new ArrayList<>();
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                resultList.add(file);
            } else if (file.isDirectory()) {
                resultList.addAll(listModelFiles(file.getAbsolutePath()));
            }
        }
        return resultList;
    }

    @SuppressWarnings("rawtypes")
    private void extractFolder(String zipFile) {
        int BUFFER = 2048;
        File file = new File(zipFile);

        try (ZipFile zip = new ZipFile("ExtractDir" + File.separator + file)) {

            String newPath = zipFile.substring(0, zipFile.length() - 4);
            new File(newPath).mkdir();
            Enumeration zipFileEntries = zip.entries();

            // Process each entry
            while (zipFileEntries.hasMoreElements()) {
                // grab a zip file entry
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();
                File destFile = new File("ExtractDir" + File.separator + newPath + File.separator + currentEntry);
                File destinationParent = destFile.getParentFile();

                destinationParent.mkdirs();

                if (!entry.isDirectory()) {
                    BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                    int currentByte;

                    byte data[] = new byte[BUFFER];
                    try (FileOutputStream fos = new FileOutputStream(destFile);
                            BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER)) {

                        while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                            dest.write(data, 0, currentByte);
                        }
                        dest.flush();
                    }
                    is.close();
                }

                if (currentEntry.endsWith(".zip")) {
                    extractFolder(destFile.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            logger.error("Failed to unzip model file " + zipFile + e);
        }
    }

    public Map<String, String> addValuesToNewModel(String type) {

        Map<String, String> successMap = new HashMap<>();
        MsAttributeObject mainClass = null;
        List<String> dependency = null;
        String subAttribute = null;

        if (!classMap.containsKey(this.newModel.getModelName())) {
            logger.error(
                    "Model Provided does not contain the service name provided in request. Unable to import new model");
            PolicyLogger.error(MessageCodes.ERROR_DATA_ISSUE, "AddValuesToNewModel",
                    "Unable to pull out required values, file missing service name provided in request");
            successMap.put("error", "MISSING");
            return successMap;
        }
        mainClass = classMap.get(this.newModel.getModelName());

        if (".yml".equalsIgnoreCase(type)) {

            newModel.setDependency("[]");
            if (mainClass.getSubClass() != null) {
                String value = new Gson().toJson(mainClass.getSubClass());
                newModel.setSubAttributes(value);
            }

            if (mainClass.getAttribute() != null) {
                String attributes = mainClass.getAttribute().toString().replace("{", "").replace("}", "");
                int equalsIndexForAttributes = attributes.indexOf("=");
                String atttributesAfterFirstEquals = attributes.substring(equalsIndexForAttributes + 1);
                this.newModel.setAttributes(atttributesAfterFirstEquals);
            }

            if (mainClass.getRefAttribute() != null) {
                String refAttributes = mainClass.getRefAttribute().toString().replace("{", "").replace("}", "");
                int equalsIndex = refAttributes.indexOf("=");
                String refAttributesAfterFirstEquals = refAttributes.substring(equalsIndex + 1);
                this.newModel.setRefAttributes(refAttributesAfterFirstEquals);
            }

            if (mainClass.getEnumType() != null) {
                this.newModel.setEnumValues(mainClass.getEnumType().toString().replace("{", "").replace("}", ""));
            }

            if (mainClass.getMatchingSet() != null) {
                this.newModel.setAnnotation(mainClass.getMatchingSet().toString().replace("{", "").replace("}", ""));
            }
            if (mainClass.getRuleFormation() != null) {
                this.newModel.setRuleFormation(mainClass.getRuleFormation());
            }

            if (mainClass.getDataOrderInfo() != null) {
                this.newModel.setDataOrderInfo(mainClass.getDataOrderInfo());
            }

        } else {

            String dependTemp = StringUtils.replaceEach(mainClass.getDependency(), new String[] {"[", "]", " "},
                    new String[] {"", "", ""});
            this.newModel.setDependency(dependTemp);
            if (this.newModel.getDependency() != null && !this.newModel.getDependency().isEmpty()) {
                dependency = new ArrayList<String>(Arrays.asList(dependTemp.split(",")));
                dependency = utils.getFullDependencyList(dependency, classMap);
                if (!dependency.isEmpty()) {
                    for (String element : dependency) {
                        MsAttributeObject temp = new MsAttributeObject();
                        if (classMap.containsKey(element)) {
                            temp = classMap.get(element);
                            mainClass.addAllRefAttribute(temp.getRefAttribute());
                            mainClass.addAllAttribute(temp.getAttribute());
                        }
                    }
                }
            }
            subAttribute = utils.createSubAttributes(dependency, classMap, this.newModel.getModelName());

            this.newModel.setSubAttributes(subAttribute);
            if (mainClass.getAttribute() != null && !mainClass.getAttribute().isEmpty()) {
                this.newModel.setAttributes(mainClass.getAttribute().toString().replace("{", "").replace("}", ""));
            }

            if (mainClass.getRefAttribute() != null && !mainClass.getRefAttribute().isEmpty()) {
                this.newModel
                        .setRefAttributes(mainClass.getRefAttribute().toString().replace("{", "").replace("}", ""));
            }

            if (mainClass.getEnumType() != null && !mainClass.getEnumType().isEmpty()) {
                this.newModel.setEnumValues(mainClass.getEnumType().toString().replace("{", "").replace("}", ""));
            }

            if (mainClass.getMatchingSet() != null && !mainClass.getMatchingSet().isEmpty()) {
                this.newModel.setAnnotation(mainClass.getMatchingSet().toString().replace("{", "").replace("}", ""));
            }
        }
        successMap.put("success", "success");
        return successMap;

    }

    public Map<String, String> saveImportService() {
        String modelName = this.newModel.getModelName();
        String imported_by = "API";
        String version = this.newModel.getVersion();
        Map<String, String> successMap = new HashMap<>();
        CommonClassDaoImpl dbConnection = new CommonClassDaoImpl();
        List<Object> result =
                dbConnection.getDataById(MicroServiceModels.class, "modelName:version", modelName + ":" + version);
        if (result == null || result.isEmpty()) {
            MicroServiceModels model = new MicroServiceModels();
            model.setModelName(modelName);
            model.setVersion(version);
            model.setAttributes(this.newModel.getAttributes());
            model.setAnnotation(this.newModel.getAnnotation());
            model.setDependency(this.newModel.getDependency());
            model.setDescription(this.newModel.getDescription());
            model.setEnumValues(this.newModel.getEnumValues());
            model.setRefAttributes(this.newModel.getRefAttributes());
            model.setSubAttributes(this.newModel.getSubAttributes());
            model.setDataOrderInfo(this.newModel.getDataOrderInfo());
            model.setDecisionModel(this.newModel.isDecisionModel());
            model.setRuleFormation(this.newModel.getRuleFormation());
            UserInfo userInfo = new UserInfo();
            userInfo.setUserLoginId(imported_by);
            userInfo.setUserName(imported_by);
            model.setUserCreatedBy(userInfo);
            dbConnection.save(model);
            successMap.put("success", "success");
        } else {
            successMap.put("DBError", "EXISTS");
            logger.error("Import new service failed.  Service already exists");
        }
        return successMap;
    }
}
