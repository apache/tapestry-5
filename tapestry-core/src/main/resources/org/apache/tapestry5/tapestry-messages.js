// Copyright 2009 The Apache Software Foundation
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

Tapestry.Messages = {

    pageIsLoading : "Please wait for the page to finish loading ...",

    missingInitializer : "Function Tapestry.Initializer.#{name}() does not exist.",

    missingValidator :      "Function Tapestry.Validator.#{name}() does not exist for field '#{fieldName}'.",

    ajaxFailure : "Ajax failure: Status #{status} for #{request.url}: ",

    ajaxRequestUnsuccessful : "Server request was unsuccessful. There may be a problem accessing the server.",

    clientException :     "Client exception processing response: ",

    missingZone :   "Unable to locate Ajax Zone '#{id}' for dynamic update.",

    noZoneManager :   "Ajax Zone '#{id}' does not have an associated Tapestry.ZoneManager object." ,

    pathDoesNotStartWithSlash : "External path #{path} does not start with a leading slash.",

    notAnInteger : "Not an integer",

    invalidCharacter : "Invalid character",

    communicationFailed : "Communication with the server failed: "
};