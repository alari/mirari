define ["main", "auth/AuthService"], (m)->
  m.controller "AppController", ["$scope", "authService", "$location", ($scope, authService, $location)->
    $scope.auth = authService

    authService.onUpdate ->
      $scope.$apply() if not $scope.$$phase

    authService.checkAuth (data)->
      if !data.isAuthenticated and $location.path() isnt "/auth"
        $location.path "/auth"
  ]