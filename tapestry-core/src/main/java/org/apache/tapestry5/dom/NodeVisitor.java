// Copyright 2026 The Apache Software Foundation
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

package org.apache.tapestry5.dom;

/**
 * A callback interface used to traverse every kind of {@link Node} in a DOM subtree:
 * {@link Element}, {@link Text}, {@link Comment}, {@link CData}, and {@link Raw}.
 * <p>
 * All methods have empty default implementations so implementors only override the node types
 * they care about. Traversal is depth-first pre-order (render order), matching the behaviour of
 * {@link Element#visit(Visitor)} but including non-element nodes.
 * <p>
 * Use {@link Element#visit(NodeVisitor)} or {@link Document#visit(NodeVisitor)} to start a
 * traversal.
 *
 * @since 5.10
 * @see Visitor
 */
public interface NodeVisitor
{
    /**
     * Called for each {@link Element} encountered during traversal.
     *
     * @param element the element being visited
     */
    default void visit(Element element) {}

    /**
     * Called for each {@link Text} node encountered during traversal.
     *
     * @param text the text node being visited
     */
    default void visit(Text text) {}

    /**
     * Called for each {@link Comment} node encountered during traversal.
     *
     * @param comment the comment node being visited
     */
    default void visit(Comment comment) {}

    /**
     * Called for each {@link CData} node encountered during traversal.
     *
     * @param cdata the CDATA node being visited
     */
    default void visit(CData cdata) {}

    /**
     * Called for each {@link Raw} node encountered during traversal.
     *
     * @param raw the raw-markup node being visited
     */
    default void visit(Raw raw) {}
}
