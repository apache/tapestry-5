package org.apache.tapestry5.json.exceptions;

public class JSONArrayIndexOutOfBoundsException extends ArrayIndexOutOfBoundsException
{

    private static final long serialVersionUID = -53336156278974940L;

    private final int index;

    public JSONArrayIndexOutOfBoundsException(int index)
    {
        super(index);
        this.index = index;
    }

    public int getIndex()
    {
        return this.index;
    }

}
