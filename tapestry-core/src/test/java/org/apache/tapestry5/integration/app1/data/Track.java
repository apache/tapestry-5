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

package org.apache.tapestry5.integration.app1.data;

import org.apache.tapestry5.beaneditor.NonVisual;

/**
 * One track from a music library.
 */
public class Track implements SimpleTrack
{
    private Long id;

    private String album, artist, genre, title;

    private int playCount, rating;

    @NonVisual
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public String getAlbum()
    {
        return album;
    }

    public String getArtist()
    {
        return artist;
    }

    public String getGenre()
    {
        return genre;
    }

    public int getPlayCount()
    {
        return playCount;
    }

    /**
     * Rating as a value between 0 and 100.
     */
    public int getRating()
    {
        return rating;
    }

    public void setAlbum(String album)
    {
        this.album = album;
    }

    public void setArtist(String artist)
    {
        this.artist = artist;
    }

    public void setGenre(String genre)
    {
        this.genre = genre;
    }

    public void setPlayCount(int playCount)
    {
        this.playCount = playCount;
    }

    public void setRating(int rating)
    {
        this.rating = rating;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

}
