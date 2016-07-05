/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package org.wso2.carbon.apimgt.importexport.utils;

import org.wso2.carbon.apimgt.importexport.APIExportException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.importexport.APIImportExportConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This class is used to generate zipped archive files
 */
public class ArchiveGeneratorUtil {
    private static final Log log = LogFactory.getLog(ArchiveGeneratorUtil.class);

    private ArchiveGeneratorUtil() {
    }

    /**
     * Archive a provided source directory to a zipped file
     *
     * @param sourceDirectory Source directory
     * @throws APIExportException If an error occurs while generating archive
     */
    public static void archiveDirectory(String sourceDirectory) throws APIExportException {

        File directoryToZip = new File(sourceDirectory);

        List<File> fileList = new ArrayList<File>();
        getAllFiles(directoryToZip, fileList);
        writeArchiveFile(directoryToZip, fileList);

        if (log.isDebugEnabled()) {
            log.debug("Archived API generated successfully");
        }

    }

    /**
     * Retrieve all the files included in the source directory to be archived
     *
     * @param sourceDirectory Source directory
     * @param fileList        List of files
     */
    private static void getAllFiles(File sourceDirectory, List<File> fileList) {
        File[] files = sourceDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                fileList.add(file);
                if (file.isDirectory()) {
                    getAllFiles(file, fileList);
                }
            }
        }
    }

    /**
     * Generate archive file
     *
     * @param directoryToZip Location of the archive
     * @param fileList       List of files to be included in the archive
     * @throws APIExportException If an error occurs while adding files to the archive
     */
    private static void writeArchiveFile(File directoryToZip, List<File> fileList) throws APIExportException {

        FileOutputStream fileOutputStream = null;
        ZipOutputStream zipOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(directoryToZip.getPath() + ".zip");
            zipOutputStream = new ZipOutputStream(fileOutputStream);
            for (File file : fileList) {
                if (!file.isDirectory()) {
                    addToArchive(directoryToZip, file, zipOutputStream);
                }
            }

        } catch (IOException e) {
            String errorMessage = "I/O error while adding files to archive";
            log.error(errorMessage, e);
            throw new APIExportException(errorMessage, e);
        } finally {
            IOUtils.closeQuietly(zipOutputStream);
            IOUtils.closeQuietly(fileOutputStream);
        }
    }

    /**
     * Add files of the directory to the archive
     *
     * @param directoryToZip  Location of the archive
     * @param file            File to be included in the archive
     * @param zipOutputStream Output stream
     * @throws APIExportException If an error occurs while writing files to the archive
     */
    private static void addToArchive(File directoryToZip, File file, ZipOutputStream zipOutputStream)
            throws APIExportException {

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);

            // Get relative path from archive directory to the specific file
            String zipFilePath = file.getCanonicalPath()
                    .substring(directoryToZip.getCanonicalPath().length() + 1, file.getCanonicalPath().length());
            if (File.separatorChar != APIImportExportConstants.ZIP_FILE_SEPARATOR)
                zipFilePath = zipFilePath.replace(File.separatorChar, APIImportExportConstants.ZIP_FILE_SEPARATOR);
            ZipEntry zipEntry = new ZipEntry(zipFilePath);
            zipOutputStream.putNextEntry(zipEntry);

            IOUtils.copy(fileInputStream, zipOutputStream);

            zipOutputStream.closeEntry();
        } catch (IOException e) {
            String errorMessage = "I/O error while writing files to archive";
            log.error(errorMessage, e);
            throw new APIExportException(errorMessage, e);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        }
    }
}

