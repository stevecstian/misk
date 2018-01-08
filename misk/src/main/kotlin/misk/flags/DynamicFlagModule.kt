package misk.flags

import com.google.inject.TypeLiteral
import com.google.inject.name.Names
import com.squareup.moshi.Moshi
import misk.inject.KAbstractModule
import misk.inject.parameterizedType
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

/**
 * Installs support for dynamic flag. Applications should inherit from this module and
 * override [configureFlags], calling the various bindXXXFlags methods to make flag
 * variables available
 */
abstract class DynamicFlagModule : KAbstractModule() {
    override fun configure() {
        requireBinding(DynamicFlagStore::class.java)
        configureFlags()
    }

    protected abstract fun configureFlags()

    inline fun <reified T : Flags> bindFlags(prefix: String = "", qualifier: Annotation? = null) =
            bindFlags(T::class, prefix, qualifier)

    fun <T : Flags> bindFlags(
            kclass: KClass<T>,
            prefix: String = "",
            qualifier: Annotation? = null
    ) {
        val actualQualifier = qualifier ?: if (!prefix.isEmpty()) Names.named(prefix) else null
        val constructor = kclass.constructors.firstOrNull {
            it.parameters.size == 1 && it.parameters[0].type.classifier == Flags.Context::class
        } ?: throw IllegalArgumentException(
                "$kclass has no single argument constructor taking a Flags.Context")


        if (actualQualifier == null) {
            bind(kclass.java)
                    .toProvider(FlagsProvider(constructor, prefix))
                    .asEagerSingleton()
        } else {
            bind(kclass.java)
                    .annotatedWith(actualQualifier)
                    .toProvider(FlagsProvider(constructor, prefix))
                    .asEagerSingleton()
        }
    }

    fun bindBooleanFlag(name: String, desc: String, qualifier: Annotation = Names.named(name)) {
        bind<BooleanFlag>()
                .annotatedWith(qualifier)
                .toProvider(BooleanFlagProvider(name, desc))
                .asEagerSingleton()
    }

    fun bindStringFlag(name: String, desc: String, qualifier: Annotation = Names.named(name)) {
        bind<StringFlag>()
                .annotatedWith(qualifier)
                .toProvider(StringFlagProvider(name, desc))
                .asEagerSingleton()
    }

    fun bindIntFlag(name: String, desc: String, qualifier: Annotation = Names.named(name)) {
        bind<IntFlag>()
                .annotatedWith(qualifier)
                .toProvider(IntFlagProvider(name, desc))
                .asEagerSingleton()
    }

    fun bindDoubleFlag(name: String, desc: String, qualifier: Annotation = Names.named(name)) {
        bind<DoubleFlag>()
                .annotatedWith(qualifier)
                .toProvider(DoubleFlagProvider(name, desc))
                .asEagerSingleton()
    }

    inline fun <reified T : Any> bindJsonFlag(
            name: String,
            desc: String,
            qualifier: Annotation = Names.named(name)
    ) = bindJsonFlag(T::class, name, desc, qualifier)

    fun <T : Any> bindJsonFlag(
            kclass: KClass<T>,
            name: String,
            desc: String,
            a: Annotation = Names.named(name)
    ) {
        val flagType = parameterizedType<JsonFlag<T>>(kclass.java)

        @Suppress("UNCHECKED_CAST")
        val flagTypeLiteral = TypeLiteral.get(flagType) as TypeLiteral<JsonFlag<T>>
        bind(flagTypeLiteral)
                .annotatedWith(a)
                .toProvider(JsonFlagProvider(kclass, name, desc))
                .asEagerSingleton()
    }

    private class BooleanFlagProvider(val name: String, val desc: String) : Provider<BooleanFlag> {
        @Inject
        lateinit var flagStore: DynamicFlagStore

        override fun get(): BooleanFlag = flagStore.registerBooleanFlag(name, desc)
    }

    private class StringFlagProvider(val name: String, val desc: String) : Provider<StringFlag> {
        @Inject
        lateinit var flagStore: DynamicFlagStore

        override fun get(): StringFlag = flagStore.registerStringFlag(name, desc)
    }

    private class IntFlagProvider(val name: String, val desc: String) : Provider<IntFlag> {
        @Inject
        lateinit var flagStore: DynamicFlagStore

        override fun get(): IntFlag = flagStore.registerIntFlag(name, desc)
    }

    private class DoubleFlagProvider(val name: String, val desc: String) : Provider<DoubleFlag> {
        @Inject
        lateinit var flagStore: DynamicFlagStore

        override fun get(): DoubleFlag = flagStore.registerDoubleFlag(name, desc)
    }

    private class JsonFlagProvider<T : Any>(
            val kclass: KClass<T>,
            val name: String,
            val desc: String
    ) : Provider<JsonFlag<T>> {
        @Inject
        lateinit var flagStore: DynamicFlagStore

        @Inject
        lateinit var moshi: Moshi

        override fun get(): JsonFlag<T> {
            val stringFlag = flagStore.registerStringFlag(name, desc)
            val adapter = moshi.adapter(kclass.java)
            return JsonFlag(stringFlag, adapter)
        }
    }

    private class FlagsProvider<T : Any>(
            val constructor: KFunction<T>,
            val prefix: String
    ) : Provider<T> {
        @Inject
        lateinit var flagStore: DynamicFlagStore
        @Inject
        lateinit var moshi: Moshi

        override fun get(): T {
            val context = Flags.Context(prefix = prefix, flagStore = flagStore, moshi = moshi)
            return constructor.call(context)
        }
    }
}