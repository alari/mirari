define ["angular"], ->
  angular.module("resolver", []).provider "routeResolver", ->
    @app = "app"

    @providers = {}

    resolveDependencies = ($q, $rootScope, dependencies)=>
      queueLen = angular.module(@app)._invokeQueue.length
      defer = $q.defer();
      require dependencies, =>
        queue = angular.module(@app)._invokeQueue
        for i in [queueLen .. queue.length-1]
          call = queue[i];
          if call
            provider = @providers[call[0]];
            if(provider)
              provider[call[1]].apply(provider, call[2]);
        if not $rootScope.$$phase
          $rootScope.$apply -> defer.resolve()
        else defer.resolve()
      defer.promise

    @$get = =>
      resolve: (dependencies, tpl, ctrl)=>
        templateUrl: "/assets/templates/#{tpl}.html"
        controller: "#{ctrl}Controller"
        resolve:
          deps: ['$q', '$rootScope', ($q, $rootScope) -> resolveDependencies($q, $rootScope, dependencies)]