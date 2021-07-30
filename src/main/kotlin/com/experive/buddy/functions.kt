package com.experive.buddy

import java.util.*


fun randomUUID() = SQLFunction<Nothing, UUID>("random_uuid")
fun rand() = SQLFunction<Nothing, Double>("rand")

fun lower(value: Expression<String?>): Expression<String?> = SQLFunction("lower", value)
fun upper(value: Expression<String?>): Expression<String?> = SQLFunction("upper", value)
fun count(): Expression<Long> = SQLFunction("count", Asterisk)
fun concat(value: Expression<String?>, other: Expression<String?>): Expression<String> = SQLFunction("concat", value, other)