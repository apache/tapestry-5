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

package org.apache.tapestry.integration.app1.data;

import org.apache.tapestry.beaneditor.NonVisual;

/**
 * One track from a music library.
 */
public class Track implements SimpleTrack
{
    private Long _id;

    private String _album;

    private String _artist;

    private String _genre;

    private int _playCount;

    private String _title;

    private int _rating;

    @NonVisual
    public Long getId()
    {
        return _id;
    }

    public void setId(Long id)
    {
        _id = id;
    }

    public String getTitle()
    {
        return _title;
    }

    public String getAlbum()
    {
        return _album;
    }

    public String getArtist()
    {
        return _artist;
    }

    public String getGenre()
    {
        return _genre;
    }

    public int getPlayCount()
    {
        return _playCount;
    }

    /**
     * Rating as a value between 0 and 100.
     */
    public int getRating()
    {
        return _rating;
    }

    public void setAlbum(String album)
    {
        _album = album;
    }

    public void setArtist(String artist)
    {
        _artist = artist;
    }

    public void setGenre(String genre)
    {
        _genre = genre;
    }

    public void setPlayCount(int playCount)
    {
        _playCount = playCount;
    }

    public void setRating(int rating)
    {
        _rating = rating;
    }

    public void setTitle(String title)
    {
        _title = title;
    }

}
