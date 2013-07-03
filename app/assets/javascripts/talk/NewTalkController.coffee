define ["main"], (m)->
  m.controller "NewTalkController", ["$scope", "$http", ($scope, $http)->
    $scope.msg = {}

    $scope.sendMsg = ->
      $http.post("/api/talk/new", $scope.msg).success (data)->
        console.log data
  ]