define ["main"], (m)->
  m.controller "ChatController", ($scope, $http, $route)->
    id = $route.current.params.id

    $scope.messages = []

    $http.get("/api/talk/#{id}").success (data)=>
      $scope.messages = data

    ws = new WebSocket("ws://localhost:9000/api/talk/socket")

    ws.onopen = ->
      console.log "websocket is opened"

    ws.onmessage = (message)->
      $scope.$apply ->
        $scope.messages.push message

    $scope.send = ->
      ws.send JSON.stringify({text:$scope.msg,username:$scope.username})
      $scope.msg = ""