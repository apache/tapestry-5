package testsubjects;

import testannotations.KindaInject;
import testinterfaces.Logger;

public class InjectSubClass extends InjectMidClass
{
    @KindaInject
    private Logger logger;

    public Logger getSubClassLogger()
    {
        return logger;
    }
}
