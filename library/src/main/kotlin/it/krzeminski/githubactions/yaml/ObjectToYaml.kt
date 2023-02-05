package it.krzeminski.githubactions.yaml

import org.snakeyaml.engine.v2.common.ScalarStyle

internal fun Any.toYaml(): String {
    val emitter = YamlStringBuilder()

    this.elementToYaml(emitter)

    return emitter.finish()
}

private fun Any?.elementToYaml(emitter: YamlBuilder) {
    when (this) {
        is Map<*, *> -> this.mapToYaml(emitter)
        is List<*> -> this.listToYaml(emitter)
        is String, is Int, is Float, is Boolean, null -> this.scalarToYaml(emitter)
        else -> error("Serializing $this is not supported!")
    }
}

private fun Map<*, *>.mapToYaml(emitter: YamlBuilder) {
    emitter.map {
        this.forEach { (key, value) ->
            // key
            it.scalar("$key", ScalarStyle.PLAIN)
            // value
            value.elementToYaml(it)
        }
    }
}

private fun List<*>.listToYaml(emitter: YamlBuilder) {
    emitter.list {
        this.forEach { value ->
            value.elementToYaml(it)
        }
    }
}

private fun Any?.scalarToYaml(emitter: YamlBuilder) {
    val scalarStyle = if (this is String) {
        if (lines().size > 1) {
            ScalarStyle.LITERAL
        } else if (isEmpty() || (this == "null")) {
            ScalarStyle.SINGLE_QUOTED
        } else {
            ScalarStyle.PLAIN
        }
    } else {
        ScalarStyle.PLAIN
    }

    emitter.scalar("$this", scalarStyle)
}
