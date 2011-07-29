package testsubjects;

import testannotations.KindaInject;
import testinterfaces.Logger;

/**
 *
 */
public class InjectSubClass extends InjectBaseClass
{
    @KindaInject
    private Logger logger;

    public Logger getSubClassLogger()
    {
        return logger;
    }
}
