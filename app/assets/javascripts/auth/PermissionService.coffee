define ["zeezoo", "auth/AuthService"], ->
  angular.module("zeezoo").service 'PermissionService', (authService, $http) ->
    @userCanAddPlacemark = ->
      authService.isAuthenticated

    @userCanModify = (placemark, callback) ->
      $http.get("/api/placemark/#{placemark?.id}/canModify").success (data) =>
        callback data