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

package org.apache.tapestry;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.internal.util.InternalUtils;

/**
 * Standard implmentation of {@link ValidationTracker}. Works pretty hard to ensure a minimum
 * amount of data is stored in the HttpSession.
 */
public final class ValidationTrackerImpl implements ValidationTracker, Serializable
{
    private static final long serialVersionUID = -8029192726659275677L;

    private static class FieldTracker implements Serializable
    {
        private static final long serialVersionUID = -3653306147088451811L;

        private final String _fieldName;

        private String _input;

        private String _errorMessage;

        FieldTracker(String fieldName)
        {
            _fieldName = fieldName;
        }

        public String getFieldName()
        {
            return _fieldName;
        }

        public void setErrorMessage(String errorMessage)
        {
            _errorMessage = errorMessage;
        }

        public String getErrorMessage()
        {
            return _errorMessage;
        }

        public String getInput()
        {
            return _input;
        }

        public void setInput(String input)
        {
            _input = input;
        }

    }

    private List<String> _extraErrors;

    private List<FieldTracker> _fieldTrackers;

    // Rebuilt on-demand

    private transient Map<String, FieldTracker> _fieldToTracker;

    private void refreshFieldToTracker()
    {
        if (_fieldToTracker != null)
            return;

        if (_fieldTrackers == null)
            return;

        _fieldToTracker = CollectionFactory.newMap();

        for (FieldTracker ft : _fieldTrackers)
            _fieldToTracker.put(ft.getFieldName(), ft);
    }

    private FieldTracker get(Field field)
    {
        String key = field.getElementName();

        refreshFieldToTracker();

        FieldTracker result = InternalUtils.get(_fieldToTracker, key);

        if (result == null)
            result = new FieldTracker(key);

        return result;
    }

    private void store(FieldTracker fieldTracker)
    {
        if (_fieldTrackers == null)
            _fieldTrackers = CollectionFactory.newList();

        refreshFieldToTracker();

        String key = fieldTracker.getFieldName();

        if (!_fieldToTracker.containsKey(key))
        {
            _fieldTrackers.add(fieldTracker);
            _fieldToTracker.put(key, fieldTracker);
        }
    }

    public void clear()
    {
        _extraErrors = null;
        _fieldTrackers = null;
        _fieldToTracker = null;
    }

    public String getError(Field field)
    {
        return get(field).getErrorMessage();
    }

    public List<String> getErrors()
    {
        List<String> result = CollectionFactory.newList();

        if (_extraErrors != null)
            result.addAll(_extraErrors);

        if (_fieldTrackers != null)
        {
            for (FieldTracker ft : _fieldTrackers)
            {
                String errorMessage = ft.getErrorMessage();

                if (errorMessage != null)
                    result.add(errorMessage);
            }
        }

        return result;
    }

    public boolean getHasErrors()
    {
        return !getErrors().isEmpty();
    }

    public String getInput(Field field)
    {
        return get(field).getInput();
    }

    public boolean inError(Field field)
    {
        return InternalUtils.isNonBlank(get(field).getErrorMessage());
    }

    public void recordError(Field field, String errorMessage)
    {
        FieldTracker ft = get(field);

        ft.setErrorMessage(errorMessage);

        store(ft);
    }

    public void recordError(String errorMessage)
    {
        if (_extraErrors == null)
            _extraErrors = CollectionFactory.newList();

        _extraErrors.add(errorMessage);
    }

    public void recordInput(Field field, String input)
    {
        FieldTracker ft = get(field);

        ft.setInput(input);

        store(ft);
    }

}
