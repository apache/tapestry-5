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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.beaneditor.ReorderProperties;

@ReorderProperties("larry,moe,shemp,curly")
public class StoogeBean
{
    private int moe, larry, curly, shemp;

    public int getMoe()
    {
        return moe;
    }

    public int getCurly()
    {
        return curly;
    }

    public int getLarry()
    {
        return larry;
    }

    public int getShemp()
    {
        return shemp;
    }

    public void setCurly(int curly)
    {
        this.curly = curly;
    }

    public void setLarry(int larry)
    {
        this.larry = larry;
    }

    public void setMoe(int moe)
    {
        this.moe = moe;
    }

    public void setShemp(int shemp)
    {
        this.shemp = shemp;
    }

}
