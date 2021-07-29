package com.experive.buddy.annotations

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Column(val value: String = "", val insertable: Boolean = true, val updatable: Boolean = true)
