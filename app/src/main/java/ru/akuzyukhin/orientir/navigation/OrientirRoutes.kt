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

    const val NOTIFICATION_SETTINGS = "profile_notification_settings"

    const val ACCESSIBILITY_SETTINGS = "profile_accessibility_settings"

    const val WARD_DETAIL = "ward_detail"
    const val WARD_ID_ARG = "wardId"

    const val WARD_DETAIL_ROUTE = "$WARD_DETAIL/{$WARD_ID_ARG}"
    fun wardDetail(wardId: Long) = "$WARD_DETAIL/$wardId"

    const val WARD_SCHEDULES = "profile_ward_schedules"

    const val WARD_SCHEDULES_ROUTE = "$WARD_SCHEDULES/{$WARD_ID_ARG}"

    fun wardSchedules(wardId: Long) = "$WARD_SCHEDULES/$wardId"

    const val SCHEDULE_DETAIL = "profile_schedule_detail"
    const val SCHEDULE_ID_ARG = "scheduleId"
    const val SCHEDULE_DETAIL_ROUTE = "$SCHEDULE_DETAIL/{$WARD_ID_ARG}/{$SCHEDULE_ID_ARG}"
    fun scheduleDetail(wardId: Long, scheduleId: Long) =
        "$SCHEDULE_DETAIL/$wardId/$scheduleId"

    const val TASK_EDITOR = "profile_task_editor"
    const val TASK_ID_ARG = "taskId"
    const val TASK_EDITOR_ROUTE = "$TASK_EDITOR/{$WARD_ID_ARG}/{$SCHEDULE_ID_ARG}/{$TASK_ID_ARG}"
    fun taskEditorCreate(wardId: Long, scheduleId: Long) =
        "$TASK_EDITOR/$wardId/$scheduleId/-1"
    fun taskEditorEdit(wardId: Long, scheduleId: Long, taskId: Long) =
        "$TASK_EDITOR/$wardId/$scheduleId/$taskId"

    const val WARD_DAILY = "profile_ward_daily"
    const val WARD_DAILY_ROUTE = "$WARD_DAILY/{$WARD_ID_ARG}"
    fun wardDaily(wardId: Long) = "$WARD_DAILY/$wardId"

    const val WARD_STATISTICS = "ward_statistics"
    const val WARD_STATISTICS_ROUTE = "$WARD_STATISTICS/{$WARD_ID_ARG}"
    fun wardStatistics(wardId: Long) = "$WARD_STATISTICS/$wardId"

    const val WARD_THRESHOLDS = "ward_thresholds"
    const val WARD_THRESHOLDS_ROUTE = "$WARD_THRESHOLDS/{$WARD_ID_ARG}"
    fun wardThresholds(wardId: Long) = "$WARD_THRESHOLDS/$wardId"

    val PROFILE_TAB_ROUTES: Set<String> = setOf(
        PROFILE,
        PROFILE_EDIT,
        CHANGE_PASSWORD,
        CONNECTIONS,
        ADD_WARD,
        NOTIFICATION_SETTINGS,
        ACCESSIBILITY_SETTINGS,
        WARD_DETAIL_ROUTE
    )

    val SCHEDULE_TAB_ROUTES: Set<String> = setOf(
        SCHEDULE,
        WARD_SCHEDULES_ROUTE,
        SCHEDULE_DETAIL_ROUTE,
        TASK_EDITOR_ROUTE,
        WARD_DAILY_ROUTE
    )

    val STATISTICS_TAB_ROUTES: Set<String> = setOf(
        STATISTICS,
        WARD_STATISTICS_ROUTE,
        WARD_THRESHOLDS_ROUTE
    )
}