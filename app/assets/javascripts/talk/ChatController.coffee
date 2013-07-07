define ["main", "util/WebSocketService"], (m)->
  m.controller "ChatController", ($scope, $http, $route, webSocketService)->
    id = $route.current.params.id

    $scope.messages = []

    $http.get("/api/talk/#{id}").success (data)=>
      $scope.messages = data

    ws = webSocketService.getLocal("/api/talk/#{id}/socket")

    ws.onmessage = (message)->
      $scope.$apply ->
        $scope.messages.push message

    $scope.send = ->
      ws.send JSON.stringify({text:$scope.msg})
      $scope.msg = ""