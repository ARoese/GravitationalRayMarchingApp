package org.fufu.grmapp.renderclient

class RequestException(message: String): Exception(message)
class ResponseException(message: String): Exception(message)
class RenderException(message: String): Exception(message)