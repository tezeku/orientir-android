package ru.akuzyukhin.orientir.core.ui

import retrofit2.HttpException
import ru.akuzyukhin.orientir.core.data.network.serverMessage
import java.io.IOException

/** Преобразование технического Throwable в текстовое сообщение для пользователя */
fun Throwable.toUserMessage(): String = when (this) {
    is IOException -> "Нет соединения с сервером. Проверьте интернет."
    is HttpException -> serverMessage() ?: when (code()) {
        in 500..599 -> "Сервер временно недоступен. Попробуйте позже."
        else -> "Ошибка ${code()}"
    }
    else -> "Что-то пошло не так. Попробуйте ещё раз."
}