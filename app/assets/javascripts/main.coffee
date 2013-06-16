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

define ["angular-ui", "resolver"], ->

  app = angular.module "mirari", ["ui", "resolver"],
  ['$routeProvider', '$locationProvider', '$controllerProvider', '$compileProvider', '$filterProvider', '$provide',
   'routeResolverProvider',
    ($routeProvider, $locationProvider, $controllerProvider, $compileProvider, $filterProvider, $provide, routeResolverProvider)->
      routeResolverProvider.app = "mirari"

      routeResolverProvider.providers = {
        $controllerProvider: $controllerProvider,
        $compileProvider: $compileProvider,
        $provide: $provide
      };

      routeResolver = routeResolverProvider.$get()

      $routeProvider
        .when("/auth",
          routeResolver.resolve(["auth/AuthController"], "auth", "Auth"))

        .when("/chat",
          routeResolver.resolve(["talk/ChatController"], "chat", "Chat"))

      $locationProvider.html5Mode(true)
  ]

  angular.bootstrap(document, ['mirari']);

  app