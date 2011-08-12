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

    pageIsLoading : "Ole hyvä ja odota kunnes sivu on latautunut...",

    missingInitializer : "Funktiota Tapestry.Initializer.#{name}() ei ole olemassa.",

    missingValidator :      "Funktiota Tapestry.Validator.#{name}() ei ole olemassa kentässä '#{fieldName}'.",

    ajaxFailure : "Ajax virhe: Status #{status} pyynnölle #{request.url}: ",

    ajaxRequestUnsuccessful : "Pyyntöä palvelimelle ei voitu suorittaa. Palvelimeen ei saatu yhteyttä.",

    clientException :     "Palvelin palautti vastauksen mutta sen prosessoinnin aikana tapahtui virhe: ",

    missingZone :   "Ajax Zone '#{id}' yritettiin päivittää dynaamisesti, mutta sitä ei löytynyt.",

    noZoneManager :   "Ajax Zone '#{id}':iin ei ole liitetty Tapestry.ZoneManager -objektia." ,

    pathDoesNotStartWithSlash : "Ulkoinen polku #{path} ei ala kauttaviivalla ( merkki '/' ).",

    notAnInteger : "Ei ole kokonaisluku",

    invalidCharacter : "Virheellinen merkki"
};