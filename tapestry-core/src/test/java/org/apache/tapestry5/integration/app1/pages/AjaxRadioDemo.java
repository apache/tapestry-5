package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.integration.app1.data.Department;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.annotations.Inject;

public class AjaxRadioDemo
{
    private Department department;

    private String position;

    private Department loopValue;

    @Inject
    private Messages messages;

    @Inject
    private ComponentResources resources;

    @Inject
    private Block dataOutput;

    void onActionFromReset()
    {
        resources.discardPersistentFieldChanges();
    }

    public Department[] getDepartments()
    {
        return Department.values();
    }

    public Department getDepartment()
    {
        return department;
    }

    public String getPosition()
    {
        return position;
    }

    public Department getLoopValue()
    {
        return loopValue;
    }

    public void setDepartment(Department department)
    {
        this.department = department;
    }

    public void setPosition(String position)
    {
        this.position = position;
    }

    public void setLoopValue(Department loopValue)
    {
        this.loopValue = loopValue;
    }

    public String getLabel()
    {
        return TapestryInternalUtils.getLabelForEnum(messages, loopValue);
    }

    Object onSuccessFromData()
    {
        return dataOutput;
    }


}
