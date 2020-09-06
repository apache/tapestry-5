package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.Stack;
import org.apache.tapestry5.corelib.components.Tree;
import org.apache.tapestry5.integration.app1.Stuff;
import org.apache.tapestry5.tree.DefaultTreeSelectionModel;
import org.apache.tapestry5.tree.TreeModel;
import org.apache.tapestry5.tree.TreeNode;
import org.apache.tapestry5.tree.TreeSelectionModel;

import java.util.List;

/**
 *
 */
public class TreeSelectionDemo
{
    @Persist
    private TreeSelectionModel selectionModel;

    @InjectComponent
    private Tree tree;

    @Property
    private Stuff selectedObject;

    void onActionFromClearAll()
    {
        tree.getExpansionModel().clear();
        tree.getSelectionModel().clear();
    }

    public TreeModel<Stuff> getStuffModel()
    {
        return Stuff.createTreeModel();
    }

    @Log
    public TreeSelectionModel<Stuff> getSelectionModel()
    {
        if (selectionModel == null)
        {
            selectionModel = new DefaultTreeSelectionModel();
        }

        return selectionModel;
    }

    public List<Stuff> getSelectedObjects()
    {

        List<Stuff> result = CollectionFactory.newList();
        Stack<TreeNode<Stuff>> queue = CollectionFactory.newStack();

        TreeModel<Stuff> model = getStuffModel();
        TreeSelectionModel<Stuff> selectionModel = getSelectionModel();

        for (TreeNode<Stuff> root : model.getRootNodes())
        {
            queue.push(root);
        }


        while (!queue.isEmpty())
        {
            TreeNode<Stuff> current = queue.pop();

            if (selectionModel.isSelected(current))
            {
                result.add(current.getValue());
            }

            for (TreeNode<Stuff> child : current.getChildren())
            {
                queue.push(child);
            }
        }


        return result;
    }

}
