package org.apache.tapestry5.integration.app1.components;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.integration.app1.DogSelectionWizardStep;

public class DogSelectionStep extends AnimalWizardStep<DogSelectionWizardStep> 
{

    public List<String> getEmptyList() 
    {
        return new ArrayList<>();
    }
}
