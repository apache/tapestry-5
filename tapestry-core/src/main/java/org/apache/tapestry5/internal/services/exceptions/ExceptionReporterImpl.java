// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services.exceptions;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.ExceptionReportWriter;
import org.apache.tapestry5.services.ExceptionReporter;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class ExceptionReporterImpl implements ExceptionReporter
{

    @Inject
    @Symbol(SymbolConstants.EXCEPTION_REPORTS_DIR)
    private File logDir;

    @Inject
    @Symbol(SymbolConstants.RESTRICTIVE_ENVIRONMENT)
    private boolean restrictive;

    private final AtomicInteger uid = new AtomicInteger();

    @Inject
    private Logger logger;


    @Inject
    private ExceptionReportWriter exceptionReportWriter;

    @Override
    public void reportException(Throwable exception)
    {
        Date date = new Date();
        String fileName = String.format(
                "exception-%tY%<tm%<td-%<tH%<tM%<tS-%<tL.%d.txt", date,
                uid.getAndIncrement());

        File folder = getOutputFolder(date);

        try
        {
            if (! restrictive)
            {
                folder.mkdirs();
            }
            File log = new File(folder, fileName);
            writeExceptionToFile(exception, log);

            logger.warn(String.format("Wrote exception report to %s", toURI(log)));
        } catch (Exception ex)
        {
            logger.error(String.format("Unable to write exception report %s at %s: %s",
                    fileName, folder.getAbsolutePath(), ExceptionUtils.toMessage(ex)));

            logger.error("Original exception:", exception);
        }
    }

    /**
     * Get the path of the directory in which the exception report file(s) should
     * be written. Except in "restrictive" environments like GAE, this is a
     * dated sub-directory of the one specified in the
     * tapestry.exception-reports-dir symbol.
     * 
     * @param date the date to be used if a dated directory is needed
     * @return the File object representing the folder
     */
    private File getOutputFolder(Date date)
    {
        if (restrictive)
        {
            // Good luck with this; all exceptions written to a single folder.
            return logDir;
        } else
        {
            String folderName = String.format("%tY-%<tm-%<td/%<tH/%<tM", date);
            return new File(logDir, folderName);
        }

    }

    private String toURI(File file)
    {
        try
        {
            return file.toURI().toString();
        } catch (Exception e)
        {
            return file.toString();
        }
    }

    private void writeExceptionToFile(Throwable exception, File log) throws IOException
    {
        log.createNewFile();

        PrintWriter writer = null;

        try
        {
            writer = new PrintWriter(log);
            exceptionReportWriter.writeReport(writer, exception);
        } finally
        {
            InternalUtils.close(writer);
        }
    }


}
