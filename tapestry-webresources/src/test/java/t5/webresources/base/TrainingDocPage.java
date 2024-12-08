package t5.webresources.base;

import org.apache.tapestry5.annotations.Import;

import t5.webresources.pages.TrainingWrapper;

@Import(stylesheet = "classpath:/META-INF/assets/TrainingDocPage.less",
    library = "classpath:/META-INF/assets/TrainingDocPage.js")
public abstract class TrainingDocPage extends TrainingWrapper {
}
