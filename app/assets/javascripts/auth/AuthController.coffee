define ["zeezoo", "auth/AuthService"], ->
  angular.module("zeezoo").controller 'AuthController', ($scope, authService) ->
    $scope.authService = authService
    $scope.credentials = {}
    # workaround for $watch (We can only watch something in $scope)

    updateAuthStatus = ->
      $scope.isAuthenticated = authService.isAuthenticated
      $scope.username = authService.username

    $scope.error = authService.error

    $scope.signIn = ->
      authService.signIn $scope.credentials, ->
        if authService.error
          alert authService.error
        else
          $scope.credentials = {}

    $scope.signOut = ->
      authService.signOut()

    authService.checkAuth()

    $scope.$watch 'authService.isAuthenticated', updateAuthStatus