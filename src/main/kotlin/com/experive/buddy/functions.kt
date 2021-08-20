package com.experive.buddy

fun lower(value: Expression<String?>): Expression<String?> = SQLFunction("lower", value)
fun upper(value: Expression<String?>): Expression<String?> = SQLFunction("upper", value)
fun count(): Expression<Long> = SQLFunction("count", Asterisk)
fun concat(value: Expression<String?>, other: Expression<String?>): Expression<String> {
    return SQLFunction("concat", value, other)
}
