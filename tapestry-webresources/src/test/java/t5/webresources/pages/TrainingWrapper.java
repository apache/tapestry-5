package t5.webresources.pages;

import org.apache.tapestry5.annotations.Import;

import t5.webresources.base.PageWrapper;

@Import(stylesheet = "classpath:/META-INF/assets/TrainingWrapper.less",
        library = "classpath:/META-INF/assets/TrainingWrapper.js")
public class TrainingWrapper extends PageWrapper {
}
