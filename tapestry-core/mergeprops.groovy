import groovy.io.FileType

import java.util.regex.Pattern

// TODO: Before final release:
// delete the individual files merged together by this script (DONE)
// delete this script

// Script should be executed from the tapestry-core folder, i.e.
// groovy mergeprops.groovy

locales = ["bg", "da", "de", "el", "es", "fi_FI", "fr_FR", "hr", "it", "ja", "mk_MK", "nl", "no_NB", "pt_BR", "ru", "sr_RS", "sv_SE", "zh_CN"]
root = "src/main/resources/org/apache/tapestry5"

// Replacement property keys:
subs = [
    "default-banner": "core-default-error-banner",
    "nesting-not-allowed": "core-form-nesting-not-allowed",
    "invalid-request": "core-invalid-form-request",
    "available-label": "core-palette-available-label",
    "selected-label": "core-palette-selected-label",
    "select-label": "core-palette-select-label",
    "deselect-label": "core-palette-deselect-label",
    "up-label": "core-palette-up-label",
    "down-label": "core-palette-down-label"
]

// For each locale, we're going to merge in the contents of several files.
// $root/internal/ValidationMessages[suffix].properties
// Then from $root/corelib/components/*[suffix].properties
// Sometimes files don't exist
// Output to $root/core[suffix].properties

componentsDir = new File("${root}/corelib/components")

def readProps(file) {

  println "Reading properties from $file"

  Properties props = new Properties()

  if (file.exists()) {
    file.withInputStream { is -> props.load(is) }
  }

  result = [:]

  props.keys().each { key ->
    result[key] = props.get(key)
  }

  println "Read ${result.size()} properties"

  return result;
}

def mergeSuffix(suffix) {

  def props = readProps new File("${root}/internal/ValidationMessages${suffix}.properties")

  // Now find the other files. When matching no suffix, exclude any localization. This check would
  // be more complicated if we support, for example, both fr and fr_BE (where one localization was
  // a prefix of another).

  componentsDir.eachFileMatch FileType.FILES, Pattern.compile(".*${suffix}\\.properties\$"), { File file ->

    //
    if (suffix == "" && file.name.contains("_")) { return; }

    def fileProps = readProps file

    fileProps.each { key, value ->

      def newKey = subs.get(key, "core-$key")

      props[newKey] = value
    }
  }

  def output = new File("$root/core${suffix}.properties")

  println "Writing ${props.size()} properties to $output"

  output.withWriter "utf-8", { writer ->

    writer.println '''\
# Copyright 2012 The Apache Software Foundation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# We try to keep the validation messages consistent, with the constraint
# value (if applicable) as the first parameter, and the field's label as the
# second parameter. Occasionally we must use specific indexing when that's
# not the best order.
'''

    props.sort().each { key, value ->
      writer.println "${key}=${value}"
    }
  }
}

// Now the real work.

// First, no localization suffix

mergeSuffix ""

// Then each locale
locales.each { mergeSuffix("_" + it) }
