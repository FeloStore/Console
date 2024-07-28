// Copyright 2023-2024 Logan Magee
//
// SPDX-License-Identifier: AGPL-3.0-only

package app.FeloStore.Console.console

import app.FeloStore.Console.console.routes.appRoutes
import app.FeloStore.Console.console.routes.auth.authRoutes
import app.FeloStore.Console.console.routes.draftRoutes
import app.FeloStore.Console.console.routes.editRoutes
import app.FeloStore.Console.console.routes.sessionRoutes
import app.FeloStore.Console.console.routes.updateRoutes
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.resources.Resources
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    install(Resources)

    routing {
        authRoutes()

        route("/api/v1") {
            sessionRoutes()

            appRoutes()
            draftRoutes()
            editRoutes()
            updateRoutes()
        }
    }
}
