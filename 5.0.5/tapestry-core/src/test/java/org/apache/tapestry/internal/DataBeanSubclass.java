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

package org.apache.tapestry.internal;

public class DataBeanSubclass extends DataBean
{
    private String _street;

    private String _city;

    private String _state;

    private String _zip;

    public String getStreet()
    {
        return _street;
    }

    public String getCity()
    {
        return _city;
    }

    public String getState()
    {
        return _state;
    }

    public String getZip()
    {
        return _zip;
    }

    public void setCity(String city)
    {
        _city = city;
    }

    public void setStreet(String street)
    {
        _street = street;
    }

    public void setState(String state)
    {
        _state = state;
    }

    public void setZip(String zip)
    {
        _zip = zip;
    }

}
