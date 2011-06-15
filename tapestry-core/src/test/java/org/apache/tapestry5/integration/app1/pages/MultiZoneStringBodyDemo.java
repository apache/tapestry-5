package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.ajax.MultiZoneUpdate;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;

public class MultiZoneStringBodyDemo {

    @Property
    private String[] list = {
            "Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten"
    };

    @Property
    private int index;

    @Property
    private String item;

    @InjectComponent
    private Zone wholeLoopZone;

    @InjectComponent
    private Zone dummyZone;

    public String getRowId() {
        return "row-" + index;
    }

    public String getClickId() {
        return "click_" + getItemId();
    }

    public int getItemId() {
        return index;
    }

    public MultiZoneUpdate onClick(int i) {

        MultiZoneUpdate mzu = new MultiZoneUpdate("dummyZone", dummyZone);

        while (i < list.length) {

            String clientId = "row-" + (i);

            String value = Integer.toString(i) + " is the integer value";

            mzu = mzu.add(clientId, value);

            ++i;
        }

        return mzu;

    }

    public Block onReset() {
        return wholeLoopZone.getBody();
    }

}
