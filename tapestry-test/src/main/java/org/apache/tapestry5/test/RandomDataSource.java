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

package org.apache.tapestry5.test;

import java.io.*;
import static java.lang.String.format;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Provides access to random data that can be used when populating a test database with "reasonable" data. The majority
 * of this is access to random words from an american english dictionary, which can be strung together to form names,
 * sentences and paragraphs.
 */
public final class RandomDataSource
{
    private final Random random = new Random(System.currentTimeMillis());

    private final List<String> words = new ArrayList<String>();

    public RandomDataSource()
    {
        for (int i = 0; i < 4; i++)
            readWords("english." + i);

        for (int i = 0; i < 3; i++)
            readWords("american." + i);

        System.out.printf("Dictionary contains %d words\n", words.size());
    }

    private void readWords(String name)
    {
        System.out.println("Reading " + name + " ...");

        int count = 0;

        InputStream is = getClass().getResourceAsStream(name);

        if (is == null) throw new RuntimeException(format("File '%s' not found.", name));

        try
        {
            BufferedInputStream bis = new BufferedInputStream(is);
            InputStreamReader isr = new InputStreamReader(bis);
            LineNumberReader r = new LineNumberReader(isr);

            while (true)
            {
                String word = r.readLine();

                if (word == null) break;

                count++;
                words.add(word);
            }

            r.close();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(format("Error reading '%s': %s", name + ex.getMessage()), ex);
        }

        System.out.printf("... %d words\n", count);
    }

    public boolean maybe(int percent)
    {
        assert percent > 0 && percent <= 100;

        return random.nextInt(100) < percent;
    }

    public int random(int min, int max)
    {
        assert min <= max;

        return random.nextInt(max - min + 1) + min;
    }

    /**
     * Returns a random word frm the dictionary. These words are usually all lowercase.
     */
    public String word()
    {
        int index = random.nextInt(words.size());

        return words.get(index);
    }

    /**
     * Returns a random word, capitalized. Useful when create random names.
     */
    public String capitalizedWord()
    {
        String word = word();

        char[] chars = word.toCharArray();

        chars[0] = Character.toUpperCase(chars[0]);

        return new String(chars);
    }

    /**
     * Returns a word that is "safe" for use in an email address.
     */
    public String safeWord()
    {
        String word = word();

        int x = word.indexOf('\'');

        return x < 0 ? word : word.substring(0, x);
    }

    /**
     * Returns a random value from the list of values supplied.
     */
    public <T> T oneOf(T... values)
    {
        assert values.length > 0;

        int index = random.nextInt(values.length);

        return values[index];
    }

    /**
     * Returns a random enum value, given the enum type.
     */
    public <T extends Enum> T oneOf(Class<T> enumClass)
    {
        return oneOf(enumClass.getEnumConstants());
    }

    /**
     * Creates a space-separated list of random words. If in sentence form, then the first word is capitalized, and a
     * period is appended.
     *
     * @param minWords   minimun number of words in the list
     * @param maxWords   maximum number of words in the list
     * @param asSentence if true, the output is "dressed up" as a non-sensical sentence
     * @return the word list / sentence
     */
    public String wordList(int minWords, int maxWords, boolean asSentence)
    {
        assert minWords <= maxWords;
        assert minWords > 0;

        StringBuilder builder = new StringBuilder();

        int count = random(minWords, maxWords);

        for (int i = 0; i < count; i++)
        {

            if (i > 0) builder.append(' ');

            if (i == 0 && asSentence)
                builder.append(capitalizedWord());
            else
                builder.append(word());
        }

        if (asSentence) builder.append('.');

        return builder.toString();
    }

    /**
     * Strings together a random number of word lists (in sentence form) to create something that looks like a
     * paragraph.
     *
     * @param minSentences per paragraph
     * @param maxSentences per paragraph
     * @param minWords     per sentence
     * @param maxWords     per sentence
     * @return the random paragraph
     */
    public String paragraph(int minSentences, int maxSentences, int minWords, int maxWords)
    {
        assert minSentences < maxSentences;
        assert minSentences > 0;

        int count = random(minSentences, maxSentences);

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < count; i++)
        {
            if (i > 0) builder.append(' ');

            builder.append(wordList(minWords, maxWords, true));
        }

        return builder.toString();
    }
}
