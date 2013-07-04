require.config
  paths:
    angular: "lib/angular/angular"
    jquery: "lib/jquery/jquery"
    "angular-ui": "lib/angular-ui/build/angular-ui"
    "angular-ui-ieshiv": "lib/angular-ui/build/angular-ui-ieshiv"
  shim:
    angular:
      deps: ["jquery"]
    "angular-ui":
      deps: ["angular"]
    "angular-ui-ieshiv":
      deps: ["angular-ui"]

define ["angular-ui", "resolver", "auth/interceptor"], ->

  app = angular.module "mirari", ["ui", "resolver", "auth.interceptor"],
  ['$routeProvider', '$locationProvider', '$controllerProvider', '$compileProvider', '$filterProvider', '$provide',
   'routeResolverProvider', 'authInterceptorProvider',
    ($routeProvider, $locationProvider, $controllerProvider, $compileProvider, $filterProvider, $provide, routeResolverProvider, authInterceptorProvider)->
      routeResolverProvider.app = "mirari"

      routeResolverProvider.providers =
        $controllerProvider: $controllerProvider
        $compileProvider: $compileProvider
        $provide: $provide
        $filterProvider: $filterProvider


      routeResolver = routeResolverProvider.$get()

      $routeProvider
        .when("/auth",
          routeResolver.resolve(["auth/AuthController"], "auth/auth", "Auth"))

        .when("/talk/new",
          routeResolver.resolve(["talk/NewTalkController"], "talk/new", "NewTalk"))

        .when("/talk/:id",
          routeResolver.resolve(["talk/ChatController"], "talk/talk", "Chat"))

      $locationProvider.html5Mode(true)

      authInterceptorProvider.unauthorized "/auth"
  ]

  require ["AppController"], ->
    angular.element(document).ready ->
      angular.bootstrap(document, ['mirari'])

  app