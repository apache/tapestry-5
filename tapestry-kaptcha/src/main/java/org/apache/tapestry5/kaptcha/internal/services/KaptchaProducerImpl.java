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

package org.apache.tapestry5.kaptcha.internal.services;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.apache.tapestry5.kaptcha.services.KaptchaProducer;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Properties;

public class KaptchaProducerImpl implements KaptchaProducer
{
    private final DefaultKaptcha producer;

    private final int height;

    private final int width;

    public KaptchaProducerImpl(Map<String, String> configuration)
    {
        producer = new DefaultKaptcha();

        Config config = new Config(toProperties(configuration));

        producer.setConfig(config);

        height = config.getHeight();
        width = config.getWidth();
    }

    @Override
    public int getHeight()
    {
        return height;
    }

    @Override
    public int getWidth()
    {
        return width;
    }

    @Override
    public BufferedImage createImage(String text)
    {
        return producer.createImage(text);
    }

    @Override
    public String createText()
    {
        return producer.createText();
    }

    private static Properties toProperties(Map<String, String> map)
    {
        Properties result = new Properties();

        for (String key : map.keySet())
        {
            result.put(key, map.get(key));
        }

        return result;

    }
}
