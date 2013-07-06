define ["main"], (m)->
  m.controller "TalksListController", ["$scope", "$http", ($scope, $http)->
    $http.get("/api/talk/").success (data)->
      $scope.talks = data
  ]