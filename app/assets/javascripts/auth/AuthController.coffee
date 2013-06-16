define ["main", "auth/AuthService"], (m)->
  m.controller 'AuthController', ['$scope', 'authService', ($scope, authService) ->
    $scope.credentials = {}

    updateAuthStatus = ->
      $scope.isAuthenticated = authService.isAuthenticated
      $scope.username = authService.username

    $scope.$on authService.STATUS_UPDATED, updateAuthStatus

    $scope.error = authService.error

    $scope.signIn = ->
      authService.signIn $scope.credentials, ->
        if authService.error
          alert authService.error
        else
          $scope.credentials = {}

    $scope.signOut = ->
      authService.signOut()
      $scope.$apply() if not $scope.$$phase

    authService.checkAuth()
  ]