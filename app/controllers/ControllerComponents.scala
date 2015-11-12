package controllers
import play.api.BuiltInComponents

trait ControllerComponents { self: BuiltInComponents =>
  lazy val assets = new Assets(httpErrorHandler)
  val frontPage: FrontPage
}

trait LiveControllerComponents extends ControllerComponents{ self: BuiltInComponents =>
  lazy val frontPage = new FrontPage(Dep("I AM A LIVE CONTROLLER"))
}
trait TestControllerComponents extends ControllerComponents{ self: BuiltInComponents =>
  lazy val frontPage = new FrontPage(Dep("I AM A TEST CONTROLLER"))
}
