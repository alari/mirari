define ["main"], (m)->
  m.service 'authService', ['$http', '$rootScope', ($http, $rootScope) ->
    @isAuthenticated = false
    @username = null
    @error = null

    @STATUS_UPDATED = "auth:status:updated"
    @STATUS_OUT = "auth:status:out"
    @STATUS_IN = "auth:status:in"

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
        if @isAuthenticated
          $rootScope.$broadcast @STATUS_IN, data
      .error (data) =>
        updateStatus data, onSuccessCallback

    @signOut = (onSuccessCallback)->
      $http.post('/api/auth/out').success (data) =>
        updateStatus data, onSuccessCallback
        if not @isAuthenticated
          $rootScope.$broadcast @STATUS_OUT, data

    @onUpdate = (callback)=>
      $rootScope.$on @STATUS_UPDATED, callback

    @
  ]