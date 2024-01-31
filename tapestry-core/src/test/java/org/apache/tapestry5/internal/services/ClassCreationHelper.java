// Copyright 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.plastic.PlasticInternalUtils;
import org.apache.tapestry5.internal.plastic.asm.ClassWriter;
import org.apache.tapestry5.internal.plastic.asm.MethodVisitor;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import static org.apache.tapestry5.internal.plastic.asm.Opcodes.*;

public class ClassCreationHelper
{

    public final String tempDir;

    public ClassCreationHelper()
    {
        this(String.format("%s/tapestry-test-classpath/%s",
                System.getProperty("java.io.tmpdir"),
                UUID.randomUUID().toString()));
    }

    public ClassCreationHelper(String tempDir)
    {
        this.tempDir = tempDir;
    }

    public void writeFile(ClassWriter writer, String className) throws Exception
    {
        File classFile = toFile(className);

        classFile.getParentFile().mkdirs();

        OutputStream os = new BufferedOutputStream(new FileOutputStream(classFile));

        os.write(writer.toByteArray());

        os.close();
    }

    public ClassWriter createWriter(String className, String superClassName, String... interfaceNames)
    {
        String[] interfaceInternalNames = new String[interfaceNames.length];
        for (int i = 0; i < interfaceNames.length; i++)
        {
            interfaceInternalNames[i] = PlasticInternalUtils.toInternalName(interfaceNames[i]);
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);

        cw.visit(V1_5, ACC_PUBLIC, PlasticInternalUtils.toInternalName(className), null,
                PlasticInternalUtils.toInternalName(superClassName), interfaceInternalNames);

        return cw;
    }

    public void implementPublicConstructor(ClassWriter cw, String superClassName)
    {

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, PlasticInternalUtils.toInternalName(superClassName), "<init>", "()V");
        mv.visitInsn(RETURN);
        mv.visitEnd();
    }


    public long readDTM(String className) throws Exception
    {
        URL url = toFile(className).toURL();

        return readDTM(url);
    }

    private File toFile(String className)
    {
        String path = String.format("%s/%s.class",
                tempDir,
                PlasticInternalUtils.toInternalName(className));

        return new File(path);
    }

    private long readDTM(URL url) throws Exception
    {
        URLConnection connection = url.openConnection();

        connection.connect();

        return connection.getLastModified();
    }


}
