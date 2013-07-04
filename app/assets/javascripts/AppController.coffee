define ["main", "auth/AuthService"], (m)->
  m.controller "AppController", ["$scope", "authService", ($scope, authService)->
    $scope.isAuthenticated = authService.isAuthenticated
    authService.onUpdate (e, data)->
      $scope.isAuthenticated = data.isAuthenticated

    authService.checkAuth()
  ]