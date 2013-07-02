define ["angular"], ->
  m = angular.module "auth.interceptor", []

  m.provider "authInterceptor", ["$httpProvider", ($httpProvider)->
    @redirect = (status, path)->
      $httpProvider.responseInterceptors.push ['$q', '$location', ($q, $location)->
        (promise)->
          r = (response)->
            if response.status is status
              $location.path path
              $q.reject response
            else
              response

          promise.then r, r
      ]

    @$get = =>
      @

    @unauthorized = (path)=>
      @redirect 401, path

    @
  ]

  m