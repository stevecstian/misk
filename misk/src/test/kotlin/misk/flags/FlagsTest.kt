package misk.flags

import com.google.inject.name.Named
import misk.flags.memory.InMemoryDynamicFlagStore
import misk.flags.memory.InMemoryDynamicFlagStoreModule
import misk.inject.KAbstractModule
import misk.moshi.MoshiModule
import misk.testing.ActionTest
import misk.testing.ActionTestModule
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test
import javax.inject.Inject

@ActionTest(startService = false)
class FlagsTest {
    data class JsonData(val message: String)

    @ActionTestModule
    val testModule = object : KAbstractModule() {
        override fun configure() {
            install(MoshiModule())
            install(InMemoryDynamicFlagStoreModule())
            install(object : DynamicFlagModule() {
                override fun configureFlags() {
                    bindBooleanFlag("my-bool", "this is the boolean flag")
                    bindDoubleFlag("my-double", "this is the double flag")
                    bindStringFlag("my-string", "this is the string flag")
                    bindIntFlag("my-int", "this is the int flag")
                    bindJsonFlag<JsonData>("my-json", "this is the json flag")

                    bindBooleanFlag("my-other-bool", "this is the other boolean flag")
                    bindDoubleFlag("my-other-double", "this is the other double flag")
                    bindStringFlag("my-other-string", "this is the other string flag")
                    bindIntFlag("my-other-int", "this is the other int flag")
                    bindJsonFlag<JsonData>("my-other-json", "this is the other json flag")

                }
            })
        }
    }

    @Inject
    private lateinit
    @Named("my-bool")
    var boolean: BooleanFlag

    @Inject
    private lateinit
    @Named("my-string")
    var string: StringFlag

    @Inject
    private lateinit
    @Named("my-int")
    var int: IntFlag

    @Inject
    private lateinit
    @Named("my-double")
    var double: DoubleFlag

    @Inject
    private lateinit
    @Named("my-json")
    var json: JsonFlag<JsonData>

    @Inject
    private lateinit
    @Named("my-other-bool")
    var otherBoolean: BooleanFlag

    @Inject
    private lateinit
    @Named("my-other-string")
    var otherString: StringFlag

    @Inject
    private lateinit
    @Named("my-other-int")
    var otherInt: IntFlag

    @Inject
    private lateinit
    @Named("my-other-double")
    var otherDouble: DoubleFlag

    @Inject
    private lateinit
    @Named("my-other-json")
    var otherJson: JsonFlag<JsonData>

    @Inject
    private lateinit
    var flagStore: InMemoryDynamicFlagStore

    @Test
    fun flagsRegistered() {
        assertThat(flagStore.booleanFlags["my-bool"]!!.description)
                .isEqualTo("this is the boolean flag")
        assertThat(flagStore.intFlags["my-int"]!!.description)
                .isEqualTo("this is the int flag")
        assertThat(flagStore.stringFlags["my-string"]!!.description)
                .isEqualTo("this is the string flag")
        assertThat(flagStore.stringFlags["my-json"]!!.description)
                .isEqualTo("this is the json flag")
        assertThat(flagStore.doubleFlags["my-double"]!!.description)
                .isEqualTo("this is the double flag")

        assertThat(flagStore.booleanFlags["my-other-bool"]!!.description)
                .isEqualTo("this is the other boolean flag")
        assertThat(flagStore.intFlags["my-other-int"]!!.description)
                .isEqualTo("this is the other int flag")
        assertThat(flagStore.stringFlags["my-other-string"]!!.description)
                .isEqualTo("this is the other string flag")
        assertThat(flagStore.stringFlags["my-other-json"]!!.description)
                .isEqualTo("this is the other json flag")
        assertThat(flagStore.doubleFlags["my-other-double"]!!.description)
                .isEqualTo("this is the other double flag")
    }

    @Test
    fun flagsNullByDefault() {
        assertThat(boolean.get()).isNull()
        assertThat(string.get()).isNull()
        assertThat(int.get()).isNull()
        assertThat(double.get()).isNull()
        assertThat(json.get()).isNull()

        assertThat(otherBoolean.get()).isNull()
        assertThat(otherString.get()).isNull()
        assertThat(otherInt.get()).isNull()
        assertThat(otherDouble.get()).isNull()
        assertThat(otherJson.get()).isNull()
    }

    @Test
    fun flagChangesExposed() {
        flagStore.booleanFlags["my-bool"]!!.set(true)
        flagStore.intFlags["my-int"]!!.set(200)
        flagStore.stringFlags["my-string"]!!.set("yo!")
        flagStore.stringFlags["my-json"]!!.set("""{"message":"hello!"}""")
        flagStore.doubleFlags["my-double"]!!.set(272.345)

        assertThat(boolean.get()).isTrue()
        assertThat(string.get()).isEqualTo("yo!")
        assertThat(int.get()).isEqualTo(200)
        assertThat(double.get()).isCloseTo(272.345, Offset.offset(0.0001))
        assertThat(json.get()).isEqualTo(JsonData("hello!"))

        assertThat(otherBoolean.get()).isNull()
        assertThat(otherString.get()).isNull()
        assertThat(otherInt.get()).isNull()
        assertThat(otherDouble.get()).isNull()
        assertThat(otherJson.get()).isNull()
    }
}