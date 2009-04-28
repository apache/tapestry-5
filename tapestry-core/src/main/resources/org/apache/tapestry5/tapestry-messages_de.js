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

    pageIsLoading : "Bitte warten während die Seite zu Ende lädt ...",

    missingInitializer : "Die Funktion Tapestry.Initializer.#{name}() existiert nicht.",

    missingValidator :      "Die Funktion Tapestry.Validator.#{name}() existiert nicht für das Feld '#{fieldName}'.",

    ajaxFailure : "Ajax Fehler: Status #{status} für #{request.url}: ",

    ajaxRequestUnsuccessful : "Die Serveranfrage schlug fehl. Es gibt womöglich ein Problem beim Zugriff auf den Server.",

    clientException :     "Client exception beim Verarbeiten der Antwort: ",

    missingZone :   "Ajax Zone '#{id}' konnte für ein dynamisches Update nicht gefunden werden.",

    noZoneManager :   "Ajax Zone '#{id}' ist nicht mit einem Tapestry.ZoneManager Objekt verknüpft." ,

    pathDoesNotStartWithSlash : "Der externe Pfad #{path} beginnt nicht mit einem führenden '/'.",

    notAnInteger : "Kein ganzzahliger Wert",

    invalidCharacter : "Ungültiges Zeichen",

    communicationFailed : "Kommunikation mit dem Server ist fehlgeschlagen: "
};
