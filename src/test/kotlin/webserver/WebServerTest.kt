package webserver

import org.junit.Test
import kotlin.test.assertEquals

class WebServerTest {
  @Test
  fun `can extract scheme`() {
    assertEquals("http", scheme("http://www.imperial.ac.uk/"))
    assertEquals("https", scheme("https://www.imperial.ac.uk/"))
    assertEquals("ftp", scheme("ftp://I.dont.actually.know.one/"))
  }

  @Test
  fun `can extract host`() {
    assertEquals("www.imperial.ac.uk", host("http://www.imperial.ac.uk/"))
    assertEquals("www.imperial.ac.uk", host("https://www.imperial.ac.uk/"))
    assertEquals("www.imperial.ac.uk", host("https://www.imperial.ac.uk/computing"))
    assertEquals("www.imperial.ac.uk", host("https://www.imperial.ac.uk/computing/programming"))
  }

  @Test
  fun `can extract path`() {
    assertEquals("/", path("http://www.imperial.ac.uk/"))
    assertEquals("/", path("https://www.imperial.ac.uk/"))
    assertEquals("/computing", path("https://www.imperial.ac.uk/computing"))
    assertEquals("/computing/programming", path("https://www.imperial.ac.uk/computing/programming"))
    assertEquals("/computing", path("https://www.imperial.ac.uk/computing?q=abc"))
    assertEquals("/computing/programming", path("https://www.imperial.ac.uk/computing/programming?q=abc"))
  }

  @Test
  fun `can extract query params`() {
    assertEquals(listOf(Pair("q", "xxx")), queryParams("http://www.imperial.ac.uk/?q=xxx"))
    assertEquals(listOf(Pair("q", "xxx"), Pair("rr", "zzz")), queryParams("http://www.imperial.ac.uk/?q=xxx&rr=zzz"))
  }

  @Test
  fun `when no query params in url, empty list is extracted`() {
    assertEquals(listOf(), queryParams("http://www.imperial.ac.uk/"))
  }

// ***** Tests for Handlers *****

  @Test
  fun `says hello world`() {
    val request = Request("http://www.imperial.ac.uk/say-hello")
    assertEquals("Hello, World!", helloHandler(request).body)
  }

  @Test
  fun `can be customised with particular name`() {
    val request = Request("http://www.imperial.ac.uk/say-hello?name=Fred")
    assertEquals("Hello, Fred!", helloHandler(request).body)
  }

//
  @Test
  fun `can process multiple params`() {
    val request = Request("http://www.imperial.ac.uk/say-hello?name=Fred&style=shouting")
    assertEquals("HELLO, FRED!", helloHandler(request).body)
  }

  @Test
  fun `can process multiple params including meaningless ones`() {
    val request = Request("http://www.imperial.ac.uk/say-hello?name=Fred&style=shouting&thisdoesnotmean=anything")
    assertEquals("HELLO, FRED!", helloHandler(request).body)
  }

// ***** Tests for Routing *****

  @Test
  fun `can route to hello handler`() {
    val request = Request("http://www.imperial.ac.uk/say-hello?name=Fred")
    assertEquals("Hello, Fred!", route(request).body)
  }

  @Test
  fun `can route to homepage handler`() {
    assertEquals("This is Imperial.", route(Request("http://www.imperial.ac.uk/")).body)
    assertEquals("This is DoC.", route(Request("http://www.imperial.ac.uk/computing")).body)
  }

  @Test
  fun `gives 404 when no matching route`() {
    assertEquals(Status.NOT_FOUND, route(Request("http://www.imperial.ac.uk/not-here")).status)
  }

//  ***** Tests for the Extensions *****

// *** More flexible routing ***

  @Test
  fun `calling configureRoutes() returns app which can handle requests`() {
    val app1 = configureRoutes(Request("http://www.imperial.ac.uk/"))
    val app2 = configureRoutes(Request("http://www.imperial.ac.uk/computing"))
    val app3 = configureRoutes(Request("http://www.imperial.ac.uk/say-hello?name=Terry&style=shouting"))
    val app4 = configureRoutes(Request("http://www.imperial.ac.uk/not-here"))

    assertEquals("This is Imperial.", app1(Request("http://www.imperial.ac.uk/")).body)
    assertEquals("This is DoC.", app2(Request("http://www.imperial.ac.uk/computing")).body)
    assertEquals("HELLO, TERRY!", app3(Request("http://www.imperial.ac.uk/say-hello?name=Terry&style=shouting")).body)
    assertEquals(Status.NOT_FOUND, app4(Request("http://www.imperial.ac.uk/not_here")).status)
  }

//  *** Filters ***

  @Test
  fun `filter prevents access to protected resources `() {

    val request = Request("http://www.imperial.ac.uk/exam-marks")
    val app = configureRoutes(request)

    assertEquals(Status.FORBIDDEN, app(request).status)
  }

  @Test
  fun `filter allows access to protected resources with token`() {
    val request = Request("http://www.imperial.ac.uk/exam-marks", "password1")
    val app = configureRoutes(request)

    assertEquals(Status.OK, app(request).status)
    assertEquals("This is very secret.", app(request).body)
  }
}
