define ["main"], (m)->
  m.service 'authService', ['$http', '$rootScope', ($http, $rootScope) ->
    @isAuthenticated = false
    @username = null
    @error = null

    @STATUS_UPDATED = "auth status updated"

    changeCallbacks = []

    updateStatus = (data, callback) =>
      @isAuthenticated = data.isAuthenticated
      @username = data.username
      @error = data.error
      callback data if callback
      $rootScope.$broadcast @STATUS_UPDATED, data
      data


    @checkAuth = (onSuccessCallback) ->
      $http.get('/api/auth/status').success (data) =>
        updateStatus data, onSuccessCallback

    @signIn = (credentials, onSuccessCallback) ->
      $http.post('/api/auth/in', credentials).success (data) =>
        updateStatus data, onSuccessCallback

    @signOut = (onSuccessCallback)->
      $http.post('/api/auth/out').success (data) =>
        updateStatus data, onSuccessCallback

    @onUpdate = (callback)->
      changeCallbacks.push(callback)

    @
  ]