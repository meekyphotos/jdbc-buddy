package com.experive.buddy.exceptions

import org.springframework.dao.DataAccessException

open class InvalidResultException(message: String) : DataAccessException(message)
