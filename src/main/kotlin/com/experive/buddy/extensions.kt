package com.experive.buddy

import com.experive.buddy.expressions.LiteralExpression
import com.experive.buddy.impl.Introspector
import kotlin.reflect.KClass

fun <I> I.asExpression(): Expression<I> {
    return LiteralExpression(this)
}

fun <E : Any> KClass<E>.table(): TableInfo<E> {
    return TableInfo(Introspector.analyze(this))
}
