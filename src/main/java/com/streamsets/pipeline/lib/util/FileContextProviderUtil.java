/**
 * Copyright 2016 StreamSets Inc.
 * <p>
 * Licensed under the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.streamsets.pipeline.lib.util;


import com.streamsets.pipeline.lib.io.LiveFile;

import java.io.IOException;
import java.nio.file.Files;

public final class FileContextProviderUtil {

    private FileContextProviderUtil() {
    }

    public static long getLongOffsetFromFileOffset(String fileOffset) {
        String offsetString = fileOffset.split("::")[0];
        return (offsetString.isEmpty()) ? 0L : Long.parseLong(offsetString);
    }

    private static LiveFile getLiveFileFromFileOffset(String fileOffset) throws IOException {
        String liveFileSerializedString = fileOffset.split("::")[1];
        return LiveFile.deserialize(liveFileSerializedString).refresh();
    }

    public static LiveFile getRefreshedLiveFileFromFileOffset(String fileOffset) throws IOException {
        return getLiveFileFromFileOffset(fileOffset);
    }

    /**
     * If passed a valid fileOffsetString, it will return what is the offset lag in the file.
     *
     * @param fileOffsetString
     * @return offset lag for the live file.
     * @throws IOException
     */
    public static long getOffsetLagForFile(String fileOffsetString) throws IOException {
        long offset = FileContextProviderUtil.getLongOffsetFromFileOffset(fileOffsetString);
        //We are refreshing the live file here because we are going to get the size by using path.
        LiveFile file = FileContextProviderUtil.getRefreshedLiveFileFromFileOffset(fileOffsetString);
        if (null == file) {
            return 0L;
        }
        long fileSizeInBytes = Files.size(file.getPath().toAbsolutePath());
        return (fileSizeInBytes - offset);
    }

    public static String createFileOffsetString(long offset, LiveFile liveFile) {
        return Long.toString(offset) + "::" + liveFile.serialize();
    }

}