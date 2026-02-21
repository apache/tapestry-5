package org.apache.tapestry5.integration.app1;


public interface WizardStep 
{
    String getStepName();

    WizardStep getNextStep();

    String getBlockName();
}
