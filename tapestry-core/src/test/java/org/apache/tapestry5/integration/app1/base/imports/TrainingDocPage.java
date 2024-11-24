package org.apache.tapestry5.integration.app1.base.imports;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.integration.app1.pages.imports.TrainingWrapper;

@Import(stylesheet = "context:css/TrainingDocPage.css")
public abstract class TrainingDocPage extends TrainingWrapper {
}
