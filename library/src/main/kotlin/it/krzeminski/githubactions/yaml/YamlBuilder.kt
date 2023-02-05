package it.krzeminski.githubactions.yaml

import org.snakeyaml.engine.v2.api.DumpSettings
import org.snakeyaml.engine.v2.api.StreamDataWriter
import org.snakeyaml.engine.v2.common.FlowStyle
import org.snakeyaml.engine.v2.common.ScalarStyle
import org.snakeyaml.engine.v2.emitter.Emitter
import org.snakeyaml.engine.v2.events.DocumentEndEvent
import org.snakeyaml.engine.v2.events.DocumentStartEvent
import org.snakeyaml.engine.v2.events.ImplicitTuple
import org.snakeyaml.engine.v2.events.MappingEndEvent
import org.snakeyaml.engine.v2.events.MappingStartEvent
import org.snakeyaml.engine.v2.events.ScalarEvent
import org.snakeyaml.engine.v2.events.SequenceEndEvent
import org.snakeyaml.engine.v2.events.SequenceStartEvent
import org.snakeyaml.engine.v2.events.StreamEndEvent
import org.snakeyaml.engine.v2.events.StreamStartEvent
import java.io.StringWriter
import java.util.*

internal interface YamlBuilder {
    fun map(block: (YamlBuilder) -> Unit)
    fun list(block: (YamlBuilder) -> Unit)

    fun scalar(scalar: String, style: ScalarStyle)
}

internal class YamlStringBuilder : YamlBuilder {
    private val settings = DumpSettings.builder()
        // Otherwise line breaks appear in places that create an incorrect YAML, e.g. in the middle of GitHub
        // expressions.
        .setWidth(Int.MAX_VALUE)
        .build()

    private val writer = object : StringWriter(), StreamDataWriter {
        override fun flush() {
            // no-op
        }
    }

    private val emitter = Emitter(settings, writer).apply {
        emit(StreamStartEvent())
        emit(DocumentStartEvent(false, Optional.empty(), emptyMap()))
    }

    override fun map(block: (YamlBuilder) -> Unit) {
        emitter.emit(MappingStartEvent(Optional.empty(), Optional.empty(), true, FlowStyle.BLOCK))

        block(this)

        emitter.emit(MappingEndEvent())
    }

    override fun list(block: (YamlBuilder) -> Unit) {
        emitter.emit(SequenceStartEvent(Optional.empty(), Optional.empty(), true, FlowStyle.BLOCK))

        block(this)

        emitter.emit(SequenceEndEvent())
    }

    override fun scalar(
        scalar: String,
        style: ScalarStyle,
    ) {
        emitter.emit(ScalarEvent(Optional.empty(), Optional.empty(), ImplicitTuple(true, true), scalar, style))
    }

    fun finish(): String {
        emitter.emit(DocumentEndEvent(false))
        emitter.emit(StreamEndEvent())

        return writer.toString()
    }
}
