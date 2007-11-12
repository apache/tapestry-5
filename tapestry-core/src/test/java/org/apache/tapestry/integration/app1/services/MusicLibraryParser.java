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

package org.apache.tapestry.integration.app1.services;

import org.apache.tapestry.integration.app1.data.Track;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newStack;
import org.apache.tapestry.ioc.util.Stack;
import org.slf4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.BufferedInputStream;
import java.io.InputStream;
import static java.lang.String.format;
import java.net.URL;
import java.util.List;

/**
 * Reads an iTunes music library file into a list of
 * {@link org.apache.tapestry.integration.app1.data.Track} elements.
 */
public class MusicLibraryParser
{
    private final Logger _logger;

    private static final int STATE_START = 0;

    private static final int STATE_PLIST = 1;

    private static final int STATE_DICT1 = 2;

    private static final int STATE_IGNORE = 3;

    private static final int STATE_DICT2 = 4;

    private static final int STATE_DICT_TRACK = 5;

    private static final int STATE_COLLECT_KEY = 6;

    private static final int STATE_COLLECT_VALUE = 7;

    private static class Item
    {
        StringBuilder _buffer;

        boolean _ignoreCharacterData;

        int _priorState;

        void addContent(char buffer[], int start, int length)
        {
            if (_ignoreCharacterData) return;

            if (_buffer == null) _buffer = new StringBuilder(length);

            _buffer.append(buffer, start, length);
        }

        String getContent()
        {
            if (_buffer != null)
                return _buffer.toString().trim();
            else
                return null;
        }

        Item(int priorState, boolean ignoreCharacterData)
        {
            _priorState = priorState;
            _ignoreCharacterData = ignoreCharacterData;
        }
    }

    private class Handler extends DefaultHandler
    {
        private final List<Track> _tracks = newList();

        private Stack<Item> _stack = newStack();

        private int _state = STATE_START;

        /**
         * Most recently seen key.
         */
        private String _key;

        /**
         * Currently building Track.
         */
        private Track _track;

        public List<Track> getTracks()
        {
            return _tracks;
        }

        private Item peek()
        {
            return _stack.peek();
        }

        private void pop()
        {
            _state = _stack.pop()._priorState;
        }

        private void push(int newState)
        {
            push(newState, true);
        }

        protected void push(int newState, boolean ignoreCharacterData)
        {
            Item item = new Item(_state, ignoreCharacterData);

            _stack.push(item);

            _state = newState;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException
        {
            end(getElementName(localName, qName));
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException
        {
            String elementName = getElementName(localName, qName);
            begin(elementName);
        }

        private String getElementName(String localName, String qName)
        {
            return qName == null ? localName : qName;
        }

        @Override
        public void characters(char ch[], int start, int length) throws SAXException
        {
            peek().addContent(ch, start, length);
        }

        private void begin(String element)
        {
            switch (_state)
            {
                case STATE_START:
                    enterStart(element);
                    return;

                case STATE_PLIST:
                    enterPlist(element);
                    return;

                case STATE_DICT1:
                    enterDict1(element);
                    return;

                case STATE_DICT2:
                    enterDict2(element);
                    return;

                case STATE_IGNORE:
                    push(STATE_IGNORE);
                    return;

                case STATE_DICT_TRACK:
                    enterDictTrack(element);
                    return;
            }
        }

        private void enterStart(String element)
        {
            if (element.equals("plist"))
            {
                push(STATE_PLIST);
                return;
            }
        }

        private void enterPlist(String element)
        {
            if (element.equals("dict"))
            {
                push(STATE_DICT1);
                return;
            }

            push(STATE_IGNORE);
        }

        private void enterDict1(String element)
        {
            if (element.equals("dict"))
            {
                push(STATE_DICT2);
                return;
            }

            push(STATE_IGNORE);
        }

        private void enterDict2(String element)
        {
            if (element.equals("dict"))
            {
                beginDictTrack(element);
                return;
            }

            push(STATE_IGNORE);
        }

        private void beginDictTrack(String element)
        {
            _track = new Track();

            _tracks.add(_track);

            push(STATE_DICT_TRACK);
        }

        private void enterDictTrack(String element)
        {
            if (element.equals("key"))
            {
                beginCollectKey(element);
                return;
            }

            beginCollectValue(element);
        }

        private void beginCollectKey(String element)
        {
            push(STATE_COLLECT_KEY, false);
        }

        private void beginCollectValue(String element)
        {
            push(STATE_COLLECT_VALUE, false);

        }

        private void end(String element)
        {
            switch (_state)
            {
                case STATE_COLLECT_KEY:

                    endCollectKey(element);
                    return;

                case STATE_COLLECT_VALUE:
                    endCollectValue(element);
                    return;

                default:
                    pop();
            }
        }

        private void endCollectKey(String element)
        {
            _key = peek().getContent();

            pop();
        }

        private void endCollectValue(String element)
        {
            String value = peek().getContent();

            pop();

            if (_key.equals("Name"))
            {
                _track.setTitle(value);
                return;
            }

            if (_key.equals("Artist"))
            {
                _track.setArtist(value);
                return;
            }

            if (_key.equals("Album"))
            {
                _track.setAlbum(value);
                return;
            }

            if (_key.equals("Genre"))
            {
                _track.setGenre(value);
                return;
            }

            if (_key.equals("Play Count"))
            {
                _track.setPlayCount(Integer.parseInt(value));
                return;
            }

            if (_key.equals("Rating"))
            {
                _track.setRating(Integer.parseInt(value));
                return;
            }

            // Many other keys are just ignored.
        }

        @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException
        {
            if (publicId.equals("-//Apple Computer//DTD PLIST 1.0//EN"))
            {
                InputStream local = new BufferedInputStream(getClass().getResourceAsStream(
                        "PropertyList-1.0.dtd"));

                return new InputSource(local);
            }

            // Perform normal processing, such as accessing via system id. That's
            // what we want to avoid, since presentations are often given when there
            // is no Internet connection.

            return null;
        }
    }

    public MusicLibraryParser(final Logger logger)
    {
        _logger = logger;
    }

    public List<Track> parseTracks(URL resource)
    {
        _logger.info(format("Parsing music library %s", resource));

        long start = System.currentTimeMillis();

        Handler handler = new Handler();

        try
        {
            XMLReader reader = XMLReaderFactory.createXMLReader();

            reader.setContentHandler(handler);
            reader.setEntityResolver(handler);

            InputSource source = new InputSource(resource.openStream());

            reader.parse(source);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }

        List<Track> result = handler.getTracks();
        long elapsed = System.currentTimeMillis() - start;

        _logger.info(format("Parsed %d tracks in %d ms", result.size(), elapsed));

        return result;
    }
}
