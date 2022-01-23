package it.krzeminski.githubactions.actions.actions

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class DownloadArtifactTest : DescribeSpec({
    it("renders with defaults") {
        // given
        val action = DownloadArtifactV2(
            artifactName = "CoolArtifact",
            path = "/some/path",
        )

        // when
        val yaml = action.toYamlArguments()

        // then
        yaml shouldBe linkedMapOf(
            "name" to "CoolArtifact",
            "path" to "/some/path",
        )
    }
})