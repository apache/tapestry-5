package yuicompressor.testapp.pages;

import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.ValueEncoderSource;
import org.apache.tapestry5.util.EnumSelectModel;

import java.util.List;

public class Index
{
    static enum Languages
    {
        JAVA, SCALA, RUBY, CLOJURE, HASKELL, C, JAVASCRIPT
    }

    @Property
    @Persist
    private List<Languages> languages;

    @Inject
    private Messages messages;

    void onActivate()
    {
        if (languages == null)
        {
            languages = CollectionFactory.newList();
        }
    }

    @Inject
    private ValueEncoderSource vec;

    public ValueEncoder<Languages> getLanguagesEncoder()
    {
        return vec.getValueEncoder(Languages.class);
    }

    public String getSelectedLanguages()
    {
        return InternalUtils.join(languages);
    }

    public SelectModel getLanguagesModel()
    {
        return new EnumSelectModel(Languages.class, messages);
    }
}
