// Copyright 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package testsubjects;

import java.sql.SQLException;

public class MultipleMethods
{
    void fred()
    {
    }

    protected void barney(String input)
    {
        try
        {
            wilma();
        }
        catch (SQLException ex)
        {
        }
    }

    private void wilma() throws SQLException
    {
        betty(1, 2, 3);
    }

    public synchronized void betty()
    {
    }

    public void betty(int a)
    {
    }

    public String betty(int a, int b)
    {
        return null;
    }

    private int betty(int a, int b, int c)
    {
        return 0;
    }
}
