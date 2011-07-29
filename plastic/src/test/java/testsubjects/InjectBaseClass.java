package testsubjects;

import testannotations.KindaInject;
import testinterfaces.Logger;


public class InjectBaseClass
{
    @KindaInject
    private Logger logger;

    public Logger getBaseClassLogger() { return logger; }
}
