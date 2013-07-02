define ["main", "auth/AuthService"], (m)->
  m.controller 'AuthController', ['$scope', 'authService', ($scope, authService) ->
    $scope.credentials = {}

    $scope.updateAuthStatus = ->
      $scope.isAuthenticated = authService.isAuthenticated
      $scope.username = authService.username
      $scope.error = authService.error
      $scope.$apply() if not $scope.$$phase

    authService.onUpdate $scope.updateAuthStatus

    $scope.error = authService.error

    $scope.closeError = ->
      $scope.error = null

    $scope.signIn = ->
      authService.signIn $scope.credentials, ->
        if not authService.error
          $scope.credentials = {}

    $scope.signOut = ->
      authService.signOut()
  ]