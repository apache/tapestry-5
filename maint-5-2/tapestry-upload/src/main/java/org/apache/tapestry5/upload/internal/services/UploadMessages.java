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

package org.apache.tapestry5.upload.internal.services;

import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.internal.util.MessagesImpl;
import org.apache.tapestry5.upload.services.UploadedFile;

import java.io.File;

class UploadMessages
{
    private static final Messages MESSAGES = MessagesImpl.forClass(UploadMessages.class);

    static String unableToDecode()
    {
        return MESSAGES.format("unable-to-decode");
    }

    static String unsupportedEncoding(String encoding)
    {
        return MESSAGES.format("unsupported-encoding", encoding);
    }

    static String unableToOpenContentFile(UploadedFile item)
    {
        return MESSAGES.format("unable-to-open-content-file", item.getFilePath());
    }

    static String writeFailure(File file)
    {
        return MESSAGES.format("write-failure", file);
    }

    static String unableToCreateTempFile()
    {
        return MESSAGES.get("unable-to-create-temp-file");
    }

    static String encloseUploadInForm()
    {
        return MESSAGES.get("enclose-upload-in-form");
    }

    static String encodingTypeAlreadySet(String formId, String requiredEncType, String existingEncType)
    {
        return MESSAGES.format("enctype-already-set", formId, requiredEncType, existingEncType);
    }
}
