package controllers
import play.api.mvc._
case class Dep(message: String)
class FrontPage(dep: Dep) extends Controller
{
  def index = Action {
    Ok(views.html.index(dep.message))
  }
}
