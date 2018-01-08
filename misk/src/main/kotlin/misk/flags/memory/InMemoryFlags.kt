package misk.flags.memory

import misk.flags.BooleanFlag
import misk.flags.DoubleFlag
import misk.flags.IntFlag
import misk.flags.StringFlag
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/** In-memory representation of flags, allowing the flag to be programmatically changed by tests */
class InMemoryBooleanFlag internal constructor(
        override val name: String,
        override val description: String
) : BooleanFlag {

    private val set = AtomicBoolean()
    private val value = AtomicBoolean()

    override fun get(): Boolean? = if (set.get()) value.get() else null

    fun set(b: Boolean) {
        value.set(b)
        set.set(true)
    }
}

class InMemoryIntFlag internal constructor(
        override val name: String,
        override val description: String
) : IntFlag {

    private val set = AtomicBoolean()
    private val value = AtomicInteger()

    override fun get(): Int? = if (set.get()) value.get() else null

    fun set(n: Int) {
        value.set(n)
        set.set(true)
    }
}

class InMemoryDoubleFlag internal constructor(
        override val name: String,
        override val description: String
) : DoubleFlag {

    private val set = AtomicBoolean()
    private val value = AtomicLong()

    override fun get(): Double? = if (set.get()) Double.fromBits(value.get()) else null

    fun set(n: Double) {
        value.set(n.toBits())
        set.set(true)
    }
}

class InMemoryStringFlag internal constructor(
        override val name: String,
        override val description: String
) : StringFlag {

    private val value = AtomicReference<String>()

    override fun get(): String? = value.get()

    fun set(s: String) {
        value.set(s)
    }
}
