package ioc.specs

import com.example.ExtraRunnable
import com.example.ExtraRunnableModule
import org.apache.tapestry5.ioc.RegistryBuilder
import spock.lang.Specification

class StartupSpec extends Specification {

    def "@Startup does not interfere with services with Runnable configuration types"() {


        ExtraRunnableModule.InvokeCounts.reset()

        expect:

        ExtraRunnableModule.InvokeCounts.startupInvokeCount == 0
        ExtraRunnableModule.InvokeCounts.contributionInvokeCount == 0


        when:

        def reg = new RegistryBuilder().add(ExtraRunnableModule).build()

        reg.performRegistryStartup()

        then:

        ExtraRunnableModule.InvokeCounts.startupInvokeCount == 1
        ExtraRunnableModule.InvokeCounts.contributionInvokeCount == 0

        when:

        def r = reg.getService(ExtraRunnable)

        then:

        ExtraRunnableModule.InvokeCounts.startupInvokeCount == 1
        ExtraRunnableModule.InvokeCounts.contributionInvokeCount == 0

        when:

        r.runOrThrow()

        then:

        ExtraRunnableModule.InvokeCounts.startupInvokeCount == 1
        ExtraRunnableModule.InvokeCounts.contributionInvokeCount == 1
    }

}
