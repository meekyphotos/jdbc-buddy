package com.experive.buddy

import com.experive.buddy.expressions.LiteralExpression
import com.experive.buddy.impl.Introspector

fun <I> I.asExpression(): Expression<I> = LiteralExpression(this)

fun <E> Class<E>.table(): Table<E> = Table(Introspector.analyze(this))
