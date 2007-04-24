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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.beaneditor.Order;

public class StoogeBean
{
    private int _moe, _larry, _curly, _shemp;

    @Order(100)
    public int getCurly()
    {
        return _curly;
    }

    public void setCurly(int curly)
    {
        _curly = curly;
    }

    public int getLarry()
    {
        return _larry;
    }

    @Order(-1)
    public void setLarry(int larry)
    {
        _larry = larry;
    }

    public int getMoe()
    {
        return _moe;
    }

    public void setMoe(int moe)
    {
        _moe = moe;
    }

    public int getShemp()
    {
        return _shemp;
    }

    public void setShemp(int shemp)
    {
        _shemp = shemp;
    }

}
