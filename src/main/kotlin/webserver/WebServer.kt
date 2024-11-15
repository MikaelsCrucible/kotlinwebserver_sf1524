package webserver

// write your web framework code her
typealias HttpHandler = (Request)-> Response
val routeconfig = listOf("/say-hello" to ::helloHandler,"/" to ::homePageHandler,"/computing" to ::homePageHandler,"/exam-marks" to requireToken("password1",::examMarkHandler))

fun scheme(url: String): String = url.substringBefore("://")

fun host(url: String): String = url.substringAfter("://").substringBefore("/")

fun path(url: String): String = url.substringAfter(host(url)).substringBefore("?")

fun queryParams(url: String): List<Pair<String, String>> {
  val querys = url.substringAfter("?", "")
  if (querys.isEmpty())
    return emptyList<Pair<String, String>>()
  return querys.split("&").map { param ->
    val (name, value) = param.split("=")
    Pair(name, value)
  }
}

fun route (request: Request): Response {
  val pathstr = path(request.url)
  var rtnval = Response(Status.NOT_FOUND, "Page not found!")
  when (pathstr) {//why it's not called switch case :(
    "/say-hello" -> rtnval = helloHandler(request)
    "/" -> rtnval = homePageHandler(request)
    "/computing" -> rtnval = homePageHandler(request)
  }
  return rtnval
}

fun configureRoutes (request: Request): HttpHandler {
  val cfg = routeconfig.find{ config->
    config.first == path(request.url)
  }

  if (cfg != null)
    return cfg.second
  return { request->
    Response(Status.NOT_FOUND,"Page not found!")
  }
}

fun requireToken(token: String, wrapped: HttpHandler): HttpHandler {
  val func :HttpHandler = { request ->
    if (request.authToken==token)
      wrapped(request)
    else
      Response(Status.FORBIDDEN,"You don't have access to this page!")
  }
  return func
}

// http handlers for a particular website...

fun homePageHandler(request: Request): Response {
  val pathstr = path(request.url)
  var resp = "This is Imperial."
  if (pathstr == "/computing")
    resp = "This is DoC."
  return Response(Status.OK, resp)
}

fun helloHandler(request: Request): Response {
  var resp = "Hello, World!"
  val params = queryParams(request.url)
  val name = params.find { param ->
    param.first == "name"
  }
  val style = params.find { param ->
    param.first == "style"
  }
  if (name != null)
    resp = "Hello, " + name.second + "!"
  if (style != null && style.second == "shouting")
    resp = resp.uppercase()
  return Response(Status.OK, resp)
}

fun examMarkHandler(request: Request): Response = Response(Status.OK, "This is very secret.")