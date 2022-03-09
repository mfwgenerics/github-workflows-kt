// This file was generated using 'wrapper-generator' module. Don't change it by hand, your changes will
// be overwritten with the next wrapper code regeneration. Instead, consider introducing changes to the
// generator itself.
package it.krzeminski.githubactions.actions.azure

import it.krzeminski.githubactions.actions.Action
import kotlin.String
import kotlin.Suppress

/**
 * Action: Azure Container Registry Login
 *
 * Log in to Azure Container Registry (ACR) or any private docker container registry
 *
 * [Action on GitHub](https://github.com/Azure/docker-login)
 */
public class DockerLoginV1(
    /**
     * Container registry username
     */
    public val username: String? = null,
    /**
     * Container registry password
     */
    public val password: String? = null,
    /**
     * Container registry server url
     */
    public val loginServer: String? = null
) : Action("Azure", "docker-login", "v1") {
    @Suppress("SpreadOperator")
    public override fun toYamlArguments() = linkedMapOf(
        *listOfNotNull(
            username?.let { "username" to it },
            password?.let { "password" to it },
            loginServer?.let { "login-server" to it },
        ).toTypedArray()
    )
}