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