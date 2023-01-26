package com.urbysoft.server

class InvalidResponseException : Exception {
    constructor() : super()
    constructor(message: String): super(message)
    constructor(cause: Throwable): super(cause)
}