define ["main"], (m)->
  m.controller "NewTalkController", ["$scope", "$http", "$location", ($scope, $http, $location)->
    $scope.msg = {}

    $scope.sendMsg = ->
      $http.post("/api/talk/new", $scope.msg).success (data)->
        $location.path "/talk/#{data._id.$oid}" if data._id
  ]