package misk.flags

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

interface Flag {
    val name: String
    val description: String
}

interface BooleanFlag : Flag {
    fun get(): Boolean?
}

interface StringFlag : Flag {
    fun get(): String?
}

interface DoubleFlag : Flag {
    fun get(): Double?
}

interface IntFlag : Flag {
    fun get(): Int?
}

class JsonFlag<out T : Any> internal constructor(
        private val stringFlag: StringFlag,
        private val adapter: JsonAdapter<T>
) : Flag {
    override val name = stringFlag.name
    override val description = stringFlag.description

    fun get(): T? = stringFlag.get()?.let { adapter.fromJson(it) }
}

open class Flags internal constructor(val name: String, val context: Context) {
    data class Context(
            val prefix: String,
            val moshi: Moshi,
            val flagStore: DynamicFlagStore
    )

    class StringFlag(
            private val description: String,
            private val name: String? = null,
            private val default: String = ""
    ) {
        operator fun provideDelegate(thisRef: Flags, prop: KProperty<*>):
                ReadOnlyProperty<Flags, String> {
            val propertyFlagName = propertyFlagName(thisRef, name, prop)
            val flag = thisRef.context.flagStore.registerStringFlag(propertyFlagName, description)
            return Property(flag, default)
        }

        private class Property(val flag: misk.flags.StringFlag, val default: String)
            : ReadOnlyProperty<Flags, String> {
            override fun getValue(thisRef: Flags, property: KProperty<*>): String =
                    flag.get() ?: default
        }
    }

    class IntFlag(
            private val description: String,
            private val name: String? = null,
            private val default: Int = 0
    ) {
        operator fun provideDelegate(thisRef: Flags, prop: KProperty<*>):
                ReadOnlyProperty<Flags, Int> {
            val propertyFlagName = propertyFlagName(thisRef, name, prop)
            val flag = thisRef.context.flagStore.registerIntFlag(propertyFlagName, description)
            return Property(flag, default)
        }

        private class Property(val flag: misk.flags.IntFlag, val default: Int)
            : ReadOnlyProperty<Flags, Int> {
            override fun getValue(thisRef: Flags, property: KProperty<*>): Int =
                    flag.get() ?: default
        }
    }

    class LongFlag(
            private val description: String,
            private val name: String? = null,
            private val default: Long = 0L
    ) {
        operator fun provideDelegate(thisRef: Flags, prop: KProperty<*>):
                ReadOnlyProperty<Flags, Long> {
            val propertyFlagName = propertyFlagName(thisRef, name, prop)
            val flag = thisRef.context.flagStore.registerIntFlag(propertyFlagName, description)
            return Property(flag, default)
        }

        private class Property(val flag: misk.flags.IntFlag, val default: Long)
            : ReadOnlyProperty<Flags, Long> {
            override fun getValue(thisRef: Flags, property: KProperty<*>): Long =
                    flag.get()?.toLong() ?: default
        }
    }

    class BooleanFlag(
            private val description: String,
            private val name: String? = null,
            private val default: Boolean = false
    ) {
        operator fun provideDelegate(thisRef: Flags, prop: KProperty<*>):
                ReadOnlyProperty<Flags, Boolean> {
            val propertyFlagName = propertyFlagName(thisRef, name, prop)
            val flag = thisRef.context.flagStore.registerBooleanFlag(propertyFlagName, description)
            return Property(flag, default)
        }

        private class Property(val flag: misk.flags.BooleanFlag, val default: Boolean)
            : ReadOnlyProperty<Flags, Boolean> {
            override fun getValue(thisRef: Flags, property: KProperty<*>): Boolean =
                    flag.get() ?: default
        }
    }

    class DoubleFlag(
            private val description: String,
            private val name: String? = null,
            private val default: Double = 0.0
    ) {
        operator fun provideDelegate(thisRef: Flags, prop: KProperty<*>):
                ReadOnlyProperty<Flags, Double> {
            val propertyFlagName = propertyFlagName(thisRef, name, prop)
            val flag = thisRef.context.flagStore.registerDoubleFlag(propertyFlagName, description)
            return Property(flag, default)
        }

        private class Property(val flag: misk.flags.DoubleFlag, val default: Double)
            : ReadOnlyProperty<Flags, Double> {
            override fun getValue(thisRef: Flags, property: KProperty<*>): Double =
                    flag.get() ?: default
        }
    }

    class FloatFlag(
            private val description: String,
            private val name: String? = null,
            private val default: Float = 0.0f
    ) {
        operator fun provideDelegate(thisRef: Flags, prop: KProperty<*>):
                ReadOnlyProperty<Flags, Float> {
            val propertyFlagName = propertyFlagName(thisRef, name, prop)
            val flag = thisRef.context.flagStore.registerDoubleFlag(propertyFlagName, description)
            return Property(flag, default)
        }

        private class Property(val flag: misk.flags.DoubleFlag, val default: Float)
            : ReadOnlyProperty<Flags, Float> {
            override fun getValue(thisRef: Flags, property: KProperty<*>): Float =
                    flag.get()?.toFloat() ?: default
        }
    }

    inline fun <reified T : Any> JsonFlag(
            description: String,
            name: String? = null,
            default: T? = null
    ) = JsonFlagInternal(T::class, description, name, default)

    class JsonFlagInternal<T : Any>(
            private val kclass: KClass<T>,
            private val description: String,
            private val name: String? = null,
            private val default: T? = null
    ) {
        operator fun provideDelegate(thisRef: Flags, prop: KProperty<*>):
                ReadOnlyProperty<Flags, T?> {
            val adapter = thisRef.context.moshi.adapter(kclass.java)
            val propertyFlagName = propertyFlagName(thisRef, name, prop)
            val flag = thisRef.context.flagStore.registerStringFlag(propertyFlagName, description)
            return Property(flag, adapter, default)
        }

        private class Property<T : Any>(
                val flag: misk.flags.StringFlag,
                val adapter: JsonAdapter<T>,
                val default: T?
        ) : ReadOnlyProperty<Flags, T?> {
            override fun getValue(thisRef: Flags, property: KProperty<*>): T? =
                    flag.get()?.let { adapter.fromJson(it) } ?: default
        }
    }

    companion object {
        internal fun propertyFlagName(
                thisRef: Flags,
                explicitName: String?,
                prop: KProperty<*>
        ): String {
            val name = explicitName ?: prop.name
            return if (thisRef.context.prefix.isEmpty()) {
                "${thisRef.name}.$name"
            } else {
                "${thisRef.context.prefix}.${thisRef.name}.$name"
            }
        }
    }

}

