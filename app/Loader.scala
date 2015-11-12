import controllers._
import play.api._
import play.api.ApplicationLoader.Context
import play.api.http._
import play.api.inject.Injector
import play.api.mvc.{Handler, RequestHeader, EssentialFilter}
import play.api.routing.Router
import router.Routes

// so the whole point of this is to allow
// the switching of backend dependencies at runtime
// while supporting compile time dependency injection
// the only way I can think of to do that is to have
// two banks of instantiated controller classes
// with different backends mixed in, and then
// two routers to direct requests to them
class Loader extends ApplicationLoader {

  // so the idea here is if something depends on BuiltInComponents but one of the lazy vals
  // has been evaluated by some other thing also depending on BuiltInComponents you can use this
  // to copy across the evaluated lazy val, rather than making a new BuiltInComponents and ending up
  // with say a test controller depending on one ActorSystem and a live controller on another
  class ComponentsFromComponents(components: BuiltInComponents) extends BuiltInComponents {

    def environment = components.environment
    def sourceMapper = components.sourceMapper
    def webCommands = components.webCommands
    def configuration = components.configuration
    def router = components.router

    override lazy val injector: Injector = components.injector
    override lazy val httpConfiguration = components.httpConfiguration
    override lazy val httpRequestHandler = components.httpRequestHandler
    override lazy val httpErrorHandler = components.httpErrorHandler
    override lazy val httpFilters = components.httpFilters

    override lazy val applicationLifecycle = components.applicationLifecycle
    override lazy val application = components.application
    override lazy val actorSystem = components.actorSystem

    override lazy val cryptoConfig = components.cryptoConfig
    override lazy val crypto = components.crypto
  }

  // todo essential filters are ignored, perhaps should not be.
  class DodgyHttpRequestHandler(prodRouter: Router, testRouter: Router, errorHandler: HttpErrorHandler, configuration: HttpConfiguration) extends DefaultHttpRequestHandler(prodRouter, errorHandler, configuration) {
    override def routeRequest(request: RequestHeader): Option[Handler] = {
      if (request.cookies.get("mode").exists { c => c.value == "test" })
        testRouter.handlerFor(request)
      else
        prodRouter.handlerFor(request)
    }
  }

  // we have to override application so it picks up the DodgyHttpRequestHandler rather than the lazy val in BuiltInComponents
  class DualRouterComponents(components: BuiltInComponents, prodRouter: Router, testRouter: Router) extends ComponentsFromComponents(components)  {
    override lazy val application: Application = new DefaultApplication(environment, applicationLifecycle, injector, configuration, httpRequestHandler, httpErrorHandler, actorSystem, Plugins.empty)
    override lazy val httpRequestHandler = new DodgyHttpRequestHandler(prodRouter, testRouter, httpErrorHandler, httpConfiguration)
  }
  
  def load(context: Context) = {
    trait NoRouterPlease { self: BuiltInComponents =>
      val router: Router = Router.empty
    }
    
    trait AnActualRouter { self: ControllerComponents with BuiltInComponents =>
      override val router: Router = new Routes(httpErrorHandler, frontPage) //use macwire for this in reality
    }

    val components = new BuiltInComponentsFromContext(context) with NoRouterPlease {}
    val prod = new ComponentsFromComponents(components) with LiveControllerComponents with AnActualRouter
    val test = new ComponentsFromComponents(components) with TestControllerComponents with AnActualRouter
    new DualRouterComponents(components, prod.router, test.router).application
  }
}


