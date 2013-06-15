define ["zeezoo"], ->
  angular.module('zeezoo').controller 'BusinessController', ($scope, Business) ->
    updateBusinesses = ->
      $scope.businesses = Business.query()

    $scope.freshBusiness = new Business {}

    $scope.addBusiness = ->
      $scope.freshBusiness.$save ->
        updateBusinesses()
        $scope.freshBusiness = new Business {}

    updateBusinesses()