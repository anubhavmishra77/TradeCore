pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TradeCore"

include(":app")
include(":core:common")
include(":core:model")
include(":core:network")
include(":core:database")
include(":core:datastore")
include(":core:ui")
include(":core:testing")
include(":feature:auth")
include(":feature:market")
include(":feature:watchlist")
include(":feature:orders")
include(":feature:portfolio")
include(":feature:wallet")
