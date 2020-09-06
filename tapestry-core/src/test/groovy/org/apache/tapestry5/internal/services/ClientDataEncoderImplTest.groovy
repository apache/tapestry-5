package org.apache.tapestry5.internal.services

import org.apache.tapestry5.alerts.AlertManager
import org.apache.tapestry5.services.ClientDataEncoder
import org.apache.tapestry5.test.ioc.TestBase
import org.easymock.EasyMock
import org.slf4j.Logger
import org.testng.annotations.Test

class ClientDataEncoderImplTest extends TestBase {

    def tryEncodeAndDecode(ClientDataEncoder cde) {
        def now = new Date()
        def input = "The current time is $now"


        String clientData = convertToClientData cde, input

        def stream = cde.decodeClientData clientData

        def output = stream.readObject()

        assert !input.is(output)
        assert input == output
    }

    def String convertToClientData(ClientDataEncoder cde, input) {
        def sink = cde.createSink()

        sink.getObjectOutputStream().with { stream ->
            stream << input
            stream.close()
        }

        sink.clientData
    }

    def extractData(String encoded) {
        def colonx = encoded.indexOf(':')

        encoded.substring(colonx + 1)
    }

    @Test
    void blank_passphrase_works_but_logs_error() {
        Logger logger = newMock Logger
        AlertManager alertManager = newMock AlertManager

        logger.error(EasyMock.isA(String))
        alertManager.error(EasyMock.isA(String))

        replay()

        ClientDataEncoder cde = new ClientDataEncoderImpl(null, "", logger, "foo.bar", alertManager)

        tryEncodeAndDecode cde

        verify()
    }

    @Test
    void no_logged_error_with_non_blank_passphrase() {
        ClientDataEncoder cde = new ClientDataEncoderImpl(null, "Testing, Testing, 1.., 2.., 3...", null, "foo.bar", null)

        tryEncodeAndDecode cde
    }

    @Test
    void passphrase_affects_encoded_output() {
        ClientDataEncoder first = new ClientDataEncoderImpl(null, "first passphrase", null, "foo.bar", null)
        ClientDataEncoder second = new ClientDataEncoderImpl(null, " different passphrase ", null, "foo.bar", null)

        def input = "current time millis is ${System.currentTimeMillis()} ms"

        def output1 = convertToClientData first, input
        def output2 = convertToClientData second, input

        assert output1 != output2

        assert extractData(output1) == extractData(output2)
    }

    @Test(expectedExceptions = IllegalArgumentException)
    void decode_with_missing_hmac_prefix_is_a_failure() {
        ClientDataEncoder cde = new ClientDataEncoderImpl(null, "a passphrase", null, "foo.bar", null)

        cde.decodeClientData("so completely invalid")
    }

    @Test
    void incorrect_hmac_is_detected() {

        // Simulate tampering by encoding with one passphrase and attempting to decode with a different
        // passphrase.
        ClientDataEncoder first = new ClientDataEncoderImpl(null, "first passphrase", null, "foo.bar", null)
        ClientDataEncoder second = new ClientDataEncoderImpl(null, " different passphrase ", null, "foo.bar", null)

        def input = "current time millis is ${System.currentTimeMillis()} ms"

        def output = convertToClientData first, input

        try {
            second.decodeClientData(output)
            unreachable()
        }
        catch (Exception e) {
            assert e.message.contains("HMAC signature does not match")
        }
    }

    @Test(expectedExceptions = EOFException)
    void check_for_eof() {
        ClientDataEncoder cde = new ClientDataEncoderImpl(null, "hmac passphrase", null, "foo.bar", null)

        def sink = cde.createSink()

        def os = sink.objectOutputStream

        def names = ["fred", "barney", "wilma"]

        names.each { os.writeObject it }

        os.close()

        def ois = cde.decodeClientData(sink.clientData)

        names.each { assert (ois.readObject() == it )}

        // This should fail:

        ois.readObject()

        unreachable()
    }

}
