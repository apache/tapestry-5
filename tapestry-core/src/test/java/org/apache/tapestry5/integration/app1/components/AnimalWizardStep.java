package org.apache.tapestry5.integration.app1.components;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.integration.app1.WizardStep;

public class AnimalWizardStep<T extends WizardStep> 
{

    @Parameter
    private WizardStep currentStep;
    
    @Cached // This is what is causing the runtime error
    public T getCurrentStep() 
    {
        return (T) currentStep;
    }
    
}
