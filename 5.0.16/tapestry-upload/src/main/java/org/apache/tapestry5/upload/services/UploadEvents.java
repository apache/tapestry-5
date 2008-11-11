//  Copyright 2008 The Apache Software Foundation
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
 * Names of events that may be triggered on components due to file uploads.
 */
public class UploadEvents
{
    /**
     * Name of event fired on a page when an upload form associated with that page encounters a {@link
     * org.apache.commons.fileupload.FileUploadException} while processing the multipart form submission.
     */
    public static final String UPLOAD_EXCEPTION = "uploadException";
}                       