package app.accrescent.parcelo.data

import app.accrescent.parcelo.routes.auth.SESSION_LIFETIME
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    Database.connect(
        "jdbc:h2:mem:parcelo;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
        "org.h2.Driver",
    )

    transaction {
        SchemaUtils.create(
            AccessControlLists,
            Apps,
            Drafts,
            Sessions,
            ReviewIssues,
            Reviewers,
            Users,
        )

        if (environment.developmentMode) {
            // Create a default superuser
            val user = User.new {
                githubUserId = System.getenv("DEBUG_USER_GITHUB_ID").toLong()
                email = System.getenv("DEBUG_USER_EMAIL")
                publisher = true
            }
            Reviewer.new {
                userId = user.id
                email = System.getenv("DEBUG_USER_REVIEWER_EMAIL")
            }

            // Create a session for said superuser for testing
            Session.new(System.getenv("DEBUG_USER_SESSION_ID")) {
                userId = user.id
                expiryTime = System.currentTimeMillis() + SESSION_LIFETIME.inWholeMilliseconds
            }
        }
    }
}
