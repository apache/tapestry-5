// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import java.util.List;

import org.apache.tapestry.Asset;
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.ioc.util.BodyBuilder;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.AssetSource;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassTransformWorker;
import org.apache.tapestry.services.TransformConstants;

/**
 * Supports injection for fields of type {@link Asset}. This worker must be scheduled
 * <em>before</em> {@link InjectNamedWorker}, because it uses the same annotation, {@link Inject},
 * but interprets the value as the relative path to the asset.
 * 
 * @see AssetSource
 */
public class InjectAssetWorker implements ComponentClassTransformWorker
{
    static final String ASSET_TYPE_NAME = Asset.class.getName();

    private final AssetSource _assetSource;

    public InjectAssetWorker(final AssetSource assetSource)
    {
        _assetSource = assetSource;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        List<String> names = transformation.findFieldsOfType(ASSET_TYPE_NAME);

        String assetSourceFieldName = null;
        String baseResourceFieldName = null;
        String resourcesFieldName = null;

        BodyBuilder builder = new BodyBuilder();
        builder.begin();

        for (String name : names)
        {

            Inject annotation = transformation.getFieldAnnotation(name, Inject.class);

            // If the field has no annotation, or no value for its annotation, that's probably
            // a programmer error, but we'll let the later Inject-related workers complain about it.

            if (annotation == null)
                continue;

            String path = annotation.value();

            if (path.equals(""))
                continue;

            // This is tricky because we support sublcasses; if we ask the component at runtime for
            // its Resource (via the ComponentModel), we get the subclass, which will break the link
            // to any Asset resources from super-classes.

            if (assetSourceFieldName == null)
            {
                assetSourceFieldName = transformation.addInjectedField(
                        AssetSource.class,
                        "assetSource",
                        _assetSource);
                baseResourceFieldName = transformation.addInjectedField(
                        Resource.class,
                        "baseResource",
                        model.getBaseResource());
                resourcesFieldName = transformation.getResourcesFieldName();
            }

            builder.addln(
                    "%s = %s.findAsset(%s, \"%s\", %s.getLocale());",
                    name,
                    assetSourceFieldName,
                    baseResourceFieldName,
                    path,
                    resourcesFieldName);

            transformation.makeReadOnly(name);

            // Keep InjectNamedWorker from doing anything to it.

            transformation.claimField(name, annotation);
        }

        // If no matches

        if (assetSourceFieldName == null)
            return;

        builder.end();

        transformation.extendMethod(TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE, builder
                .toString());

    }
}
