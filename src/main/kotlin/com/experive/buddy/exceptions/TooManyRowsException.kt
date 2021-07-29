package com.experive.buddy.exceptions

class TooManyRowsException : InvalidResultException("Expecting one result, got many")