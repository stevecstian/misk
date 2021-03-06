package misk.web.marshal

import com.google.inject.util.Modules
import com.squareup.moshi.Moshi
import misk.MiskModule
import misk.inject.KAbstractModule
import misk.testing.ActionTest
import misk.testing.ActionTestModule
import misk.testing.TestWebModule
import misk.web.Post
import misk.web.RequestBody
import misk.web.RequestContentType
import misk.web.ResponseContentType
import misk.web.WebActionModule
import misk.web.WebModule
import misk.web.actions.WebAction
import misk.web.jetty.JettyService
import misk.web.mediatype.MediaTypes
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.ByteString
import okio.Okio
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import javax.inject.Inject

@ActionTest(startService = true)
internal class JsonRequestTest {
    data class Packet(val message: String)

    @ActionTestModule
    val module = Modules.combine(
            MiskModule(),
            WebModule(),
            TestWebModule(),
            TestModule())

    private @Inject lateinit var moshi: Moshi
    private @Inject lateinit var jettyService: JettyService
    private val packetJsonAdapter get() = moshi.adapter(Packet::class.java)

    @Test
    fun passAsObject() {
        assertThat(post("/as-object", Packet("foo")).message).isEqualTo("foo as-object")
    }

    @Test
    fun passAsString() {
        assertThat(post("/as-string", Packet("foo")).message).isEqualTo("foo as-string")
    }

    @Test
    fun passAsByteString() {
        assertThat(post("/as-byte-string", Packet("foo")).message).isEqualTo("foo as-byte-string")
    }

    class PassAsObject : WebAction {
        @Post("/as-object")
        @RequestContentType(MediaTypes.APPLICATION_JSON)
        @ResponseContentType(MediaTypes.APPLICATION_JSON)
        fun call(@RequestBody packet: Packet) = Packet("${packet.message} as-object")
    }

    class PassAsString : WebAction {
        @Inject lateinit var moshi: Moshi
        private val packetJsonAdapter get() = moshi.adapter(Packet::class.java)

        @Post("/as-string")
        @RequestContentType(MediaTypes.APPLICATION_JSON)
        @ResponseContentType(MediaTypes.APPLICATION_JSON)
        fun call(@RequestBody encodedPacket: String): Packet {
            val incomingPacket = packetJsonAdapter.fromJson(encodedPacket)
            return Packet("${incomingPacket?.message} as-string")
        }
    }

    class PassAsByteString : WebAction {
        @Inject lateinit var moshi: Moshi
        private val packetJsonAdapter get() = moshi.adapter(Packet::class.java)

        @Post("/as-byte-string")
        @RequestContentType(MediaTypes.APPLICATION_JSON)
        @ResponseContentType(MediaTypes.APPLICATION_JSON)
        fun call(@RequestBody encodedPacket: ByteString): Packet {
            val source = Okio.buffer(Okio.source(ByteArrayInputStream(encodedPacket.toByteArray())))
            val incomingPacket = packetJsonAdapter.fromJson(source)
            return Packet("${incomingPacket?.message} as-byte-string")
        }
    }

    class TestModule : KAbstractModule() {
        override fun configure() {
            install(WebActionModule.create<PassAsObject>())
            install(WebActionModule.create<PassAsString>())
            install(WebActionModule.create<PassAsByteString>())
        }
    }

    private fun post(path: String, packet: Packet): Packet = call(Request.Builder()
            .url(jettyService.serverUrl.newBuilder().encodedPath(path).build())
            .post(okhttp3.RequestBody.create(MediaTypes.APPLICATION_JSON_MEDIA_TYPE,
                    packetJsonAdapter.toJson(packet))))

    private fun call(request: Request.Builder): Packet {
        request.header("Accept", MediaTypes.APPLICATION_JSON)

        val httpClient = OkHttpClient()
        val response = httpClient.newCall(request.build()).execute()
        assertThat(response.code()).isEqualTo(200)
        assertThat(response.header("Content-Type")).isEqualTo(MediaTypes.APPLICATION_JSON)
        return packetJsonAdapter.fromJson(response.body()!!.source())!!
    }
}