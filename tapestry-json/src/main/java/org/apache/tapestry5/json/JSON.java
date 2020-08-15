/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tapestry5.json;

import org.apache.tapestry5.json.exceptions.JSONInvalidTypeException;

class JSON {
    /**
     * Returns the input if it is a JSON-permissible value; throws otherwise.
     */
    static double checkDouble(double d) throws IllegalArgumentException {
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            throw new IllegalArgumentException("JSON does not allow non-finite numbers.");
        }
        return d;
    }

    static Boolean toBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            String stringValue = (String) value;
            if ("true".equalsIgnoreCase(stringValue)) {
                return true;
            } else if ("false".equalsIgnoreCase(stringValue)) {
                return false;
            }
        }
        return null;
    }

    static Double toDouble(Object value) {
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.valueOf((String) value);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    static Integer toInteger(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return (int) Double.parseDouble((String) value);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    static Long toLong(Object value) {
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            try {
                return (long) Double.parseDouble((String) value);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    static String toString(Object value) {
        if (value instanceof String) {
            return (String) value;
        } else if (value != null) {
            return String.valueOf(value);
        }
        return null;
    }

    static void testValidity(Object value)
    {
        if (value == null) {
            throw new IllegalArgumentException("null isn't valid in JSONArray. Use JSONObject.NULL instead.");
        }

        if (value == JSONObject.NULL)
        {
            return;
        }

        Class<? extends Object> clazz = value.getClass();
        if (Boolean.class.isAssignableFrom(clazz)
            || Number.class.isAssignableFrom(clazz)
            || String.class.isAssignableFrom(clazz)
            || JSONArray.class.isAssignableFrom(clazz)
            || JSONLiteral.class.isAssignableFrom(clazz)
            || JSONObject.class.isAssignableFrom(clazz)
            || JSONString.class.isAssignableFrom(clazz))
        {
            return;
        }

        throw new JSONInvalidTypeException(clazz);
    }
}
