package org.apache.tapestry5.javadoc;

import com.sun.source.doctree.DocCommentTree;

import javax.lang.model.element.Element;

public interface DocCommentTreeProvider
{
    DocCommentTree getDocCommentTree(Element e);
}
