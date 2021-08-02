package com.experive.buddy

import com.experive.buddy.expressions.LiteralExpression
import com.experive.buddy.impl.Introspector
import kotlin.reflect.KClass

fun <I> I.asExpression(): Expression<I> = LiteralExpression(this)

fun <E : Any> KClass<E>.table(): TableInfo<E> = TableInfo(Introspector.analyze(this))
