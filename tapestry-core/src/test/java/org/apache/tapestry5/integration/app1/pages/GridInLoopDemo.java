package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.beanmodel.BeanModel;
import org.apache.tapestry5.beanmodel.services.BeanModelSource;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.integration.app1.data.Track;
import org.apache.tapestry5.ioc.annotations.Inject;

import java.util.Collection;

public class GridInLoopDemo extends InplaceGridInLoopDemo {
    @Inject
    private BeanModelSource beanModelSource;

    @Inject
    private Messages messages;

    public BeanModel<Track> getModel() {
        BeanModel<Track> model = beanModelSource.createDisplayModel(Track.class,messages);
        //For every loop iteration after the first remove one more column
        int toRemove = Math.min(index, model.getPropertyNames().size()) - 1;
        for(int i = 0;i<toRemove;i++) {
            model.exclude(model.getPropertyNames().get(0));
        }
        //Disable sorting for all columns except rating.The reason for that is because all
        //grids share the same GridPaginationModel but not the same BeanModel.This might result to
        //a sort of a column that is included in ones grid model but not in another.The only column
        //that is included in all models is the rating column
        for(String propertyName:(Collection<String>)model.getPropertyNames()) {
            if(!propertyName.equals("rating")) {
                model.get(propertyName).sortable(false);
            }
        }
        return model;
    }

}
