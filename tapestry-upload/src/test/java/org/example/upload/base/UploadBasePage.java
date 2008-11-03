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

package org.example.upload.base;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.upload.services.UploadedFile;

import java.io.File;

public class UploadBasePage
{
    public static final String TARGET_DIR = "target/tmp/";

    @Persist
    @Property
    @Validate("required")
    private UploadedFile file;

    public void onSuccess()
    {
        File copied = new File(TARGET_DIR + file.getFileName());

        file.write(copied);
    }
}
