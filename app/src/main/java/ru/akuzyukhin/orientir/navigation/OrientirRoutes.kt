package ru.akuzyukhin.orientir.navigation

/** Маршруты приложения */
object OrientirRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"

    const val LOGIN_ARG_PHONE = "phone"
    const val LOGIN_ROUTE = "$LOGIN?$LOGIN_ARG_PHONE={$LOGIN_ARG_PHONE}"

    fun loginWithPhone(phone: String?): String {
        return if (phone.isNullOrBlank()) {
            LOGIN
        } else {
            "$LOGIN?$LOGIN_ARG_PHONE=${java.net.URLEncoder.encode(phone, "UTF-8")}"
        }
    }
}

object HomeTabRoutes {
    const val PROFILE = "tab_profile"
    const val SCHEDULE = "tab_schedule"
    const val NOTIFICATIONS = "tab_notifications"
    const val STATISTICS = "tab_statistics"

    const val PROFILE_EDIT = "profile_edit"
    const val CHANGE_PASSWORD = "profile_change_password"

    const val CONNECTIONS = "profile_connections"

    const val ADD_WARD = "profile_add_ward"
}