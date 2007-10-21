// Copyright 2007 The Apache Software Foundation
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

package org.example.upload.pages;

import java.io.File;

import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.upload.services.UploadedFile;

public class Start
{
    public static final String TARGET_DIR = "target/tmp/";

    @Persist
    private UploadedFile _file;

    public UploadedFile getFile()
    {
        return _file;
    }

    public void setFile(UploadedFile file)
    {
        _file = file;
    }

    public void onSuccess()
    {
        File copied = new File(TARGET_DIR + _file.getFileName());

        _file.write(copied);
    }
}
