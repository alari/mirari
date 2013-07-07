define ["../main"], (m)->
  m.service "webSocketService", ["$rootScope", "$window", ($rootScope, $window)->

    @getLocal = (path)->
      if path[0] != "/"
        path = "/#{path}"

      ws = new WebSocket("ws://#{$window.location.host}#{path}")

      closer = $rootScope.$on "$routeChangeStart", ->
        ws.close()
        closer()

      ws
  ]