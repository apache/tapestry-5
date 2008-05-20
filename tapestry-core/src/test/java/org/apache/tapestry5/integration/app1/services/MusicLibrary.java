// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1.services;

import org.apache.tapestry5.integration.app1.data.Track;

import java.util.List;

public interface MusicLibrary
{
    /**
     * Gets a track by its unique id.
     *
     * @param id of track to retrieve
     * @return the Track
     * @throws IllegalArgumentException if no such track exists
     */
    Track getById(long id);

    /**
     * Provides a list of all tracks in an indeterminate order.
     */
    List<Track> getTracks();

    /**
     * Performs a case-insensitive search, finding all tracks whose title contains the input string (ignoring case).
     *
     * @param title a partial title
     * @return a list of all matches
     */
    List<Track> findByMatchingTitle(String title);
}
