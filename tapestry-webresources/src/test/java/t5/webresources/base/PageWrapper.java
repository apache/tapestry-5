package t5.webresources.base;

import org.apache.tapestry5.annotations.Import;

@Import(stylesheet = "classpath:/META-INF/assets/PageWrapper.less", 
    library = "classpath:/META-INF/assets/PageWrapper.js")
public abstract class PageWrapper extends ComponentCommonBase {
}
