package org.apache.tapestry5.integration.symbolparam.services

/**
 * Just store number and if is odd or even
 */
class Number
{
    int me

    Number(int number)
    {
        me = number
    }

    int getMe()
    {
        return me
    }

    boolean isOdd()
    {
        return me % 2 == 0 ? false : true
    }
}
