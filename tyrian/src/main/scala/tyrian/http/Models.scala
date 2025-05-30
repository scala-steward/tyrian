package tyrian.http

import org.scalajs.dom.BodyInit

/** An Error will be returned if something goes wrong with an HTTP request. */
enum HttpError:
  /** A BadRequest means that the provide request was not valid for some reason.
    * @param msg
    *   error message
    */
  case BadRequest(msg: String) extends HttpError

  /** A Timeout means that it took too long to get a response. */
  case Timeout extends HttpError

  /** A NetworkError means that there is a problem with the network. */
  case NetworkError extends HttpError

/** An HTTP method */
enum Method derives CanEqual:
  case Get, Post, Put, Patch, Delete, Options, Head

  def asString: String =
    this match
      case Get     => "GET"
      case Post    => "POST"
      case Put     => "PUT"
      case Patch   => "PATCH"
      case Delete  => "DELETE"
      case Options => "OPTIONS"
      case Head    => "HEAD"

/** The body of a request */
enum Body derives CanEqual:
  /** Represents an empty body e.g. for GET requests or POST request without any data.
    */
  case Empty extends Body

  /** Create a request body with a string.
    * @param contentType
    *   the content type of the body
    * @param body
    *   the content of the body
    */
  case PlainText(contentType: String, body: String) extends Body
  case File(contentType: String, body: BodyInit)    extends Body

object Body:
  def html(body: String): Body      = Body.PlainText("text/html", body)
  def json(body: String): Body      = Body.PlainText("application/json", body)
  def xml(body: String): Body       = Body.PlainText("application/xml", body)
  def plainText(body: String): Body = Body.PlainText("text/plain", body)

/** A request header
  * @param name
  *   header field name
  * @param value
  *   header field value
  */
final case class Header(name: String, value: String)

/** The response from an HTTP request.
  * @param url
  *   the url
  * @param status
  *   the status code
  * @param headers
  *   the response headers
  * @param body
  *   the response body
  * @tparam A
  *   type of the response body
  */
final case class Response(
    url: String,
    status: Status,
    headers: Map[String, String],
    body: String
)

/** The response status code
  * @param code
  *   the status code
  * @param message
  *   the status message
  */
final case class Status(code: Int, message: String)

enum RequestCredentials derives CanEqual:
  case Omit, SameOrigin, Include

  def asString: String =
    this match
      case Omit       => "omit"
      case SameOrigin => "same-origin"
      case Include    => "include"

enum RequestCache derives CanEqual:
  case Default, NoStore, Reload, NoCache, ForceCache, OnlyIfCached

  def asString: String =
    this match
      case Default      => "default"
      case NoStore      => "no-store"
      case Reload       => "reload"
      case NoCache      => "no-cache"
      case ForceCache   => "force-cache"
      case OnlyIfCached => "only-if-cached"
