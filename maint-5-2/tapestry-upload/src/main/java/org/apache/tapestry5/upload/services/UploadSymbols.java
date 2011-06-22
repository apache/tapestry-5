// Copyright 2007, 2008 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.upload.services;

/**
 * Configuration symbols.
 */
public final class UploadSymbols
{
    /**
     * Location where temporary files will be written. Defaults to java.io.tmpdir property.
     */
    public static final String REPOSITORY_LOCATION = "upload.repository-location";

    /**
     * Threshold (in bytes) that determines when an uploaded file will be written to the repository.
     */
    public static final String REPOSITORY_THRESHOLD = "upload.repository-threshold";

    /**
     * Maximum size (in bytes) of a single upload request Defaults to -1 (no limit).
     */
    public static final String REQUESTSIZE_MAX = "upload.requestsize-max";

    /**
     * Maximum size (in bytes) of a single file within an upload request Defaults to -1 (no limit).
     */
    public static final String FILESIZE_MAX = "upload.filesize-max";

    private UploadSymbols()
    {
    }
}
