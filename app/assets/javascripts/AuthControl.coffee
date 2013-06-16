define ['main', 'auth/AuthService'], (m)->
  m.service "authControl", ['authService', '$location', (authService, $location)->
    authService.onUpdate ->
      if $location.path() is "/auth" and authService.isAuthenticated
        $location.path "/grid"
      else if not authService.isAuthenticated and $location.path() != "/auth"
        $location.path "/auth"

    authChecked = false

    authService.checkAuth ->
      authChecked = true

    @shouldRedirect = ->
      (($location.path() is "/auth" and authService.isAuthenticated) or (not authService.isAuthenticated and $location.path() != "/auth"))

    @ifAuthenticated = (callback)=>
      if authChecked
        if authService.isAuthenticated
          callback()
      else
        authService.checkAuth =>
          @ifAuthenticated callback

    @
  ]