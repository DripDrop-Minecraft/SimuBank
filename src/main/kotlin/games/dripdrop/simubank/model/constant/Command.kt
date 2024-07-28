package games.dripdrop.simubank.model.constant

enum class Command(val value: String) {
    OVERVIEW("overview"),
    PUBLISH_ANNOUNCE("publish"),
    QUERY_ANNOUNCEMENT("announcement"),
    RELOAD("reload"),
    PRODUCT("product"),
    DEPOSIT("deposit"),
    WITHDRAW("withdraw")
}