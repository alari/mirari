define ["zeezoo"], ->
  angular.module("zeezoo").service 'authService', ($http) ->
    @isAuthenticated = false
    @username = null
    @error = null

    updateStatus = (data, callback) =>
      @isAuthenticated = data.isAuthenticated
      @username = data.username
      @error = data.error
      callback data if callback

    @checkAuth = (onSuccessCallback) ->
      $http.get('/api/auth/status').success (data) =>
        updateStatus data, onSuccessCallback

    @signIn = (credentials, onSuccessCallback) ->
      $http.post('/api/auth/in', credentials).success (data) =>
        updateStatus data, onSuccessCallback

    @signOut = (onSuccessCallback)->
      $http.post('/api/auth/out').success (data) =>
        updateStatus data, onSuccessCallback

    @