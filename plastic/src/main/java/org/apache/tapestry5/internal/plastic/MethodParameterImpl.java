package org.apache.tapestry5.internal.plastic;

import org.apache.tapestry5.internal.plastic.asm.tree.AnnotationNode;
import org.apache.tapestry5.plastic.MethodParameter;

import java.util.List;

class MethodParameterImpl extends PlasticMember implements MethodParameter
{
    private final String type;

    private final int index;

    MethodParameterImpl(PlasticClassImpl plasticClass, List<AnnotationNode> visibleAnnotations, String type, int index)
    {
        super(plasticClass, visibleAnnotations);

        this.type = type;
        this.index = index;
    }

    @Override
    public String getType()
    {
        plasticClass.check();

        return type;
    }

    @Override
    public int getIndex()
    {
        plasticClass.check();

        return index;
    }
}
