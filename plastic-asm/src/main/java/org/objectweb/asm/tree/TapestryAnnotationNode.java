package org.objectweb.asm.tree;

import org.objectweb.asm.AnnotationVisitor;

public class TapestryAnnotationNode extends AnnotationNode {
    public TapestryAnnotationNode(String desc) {
        super(desc);
    }

    public static void accept(AnnotationVisitor annotationVisitor, String name, Object value) {
        AnnotationNode.accept(annotationVisitor, name, value);}
}
