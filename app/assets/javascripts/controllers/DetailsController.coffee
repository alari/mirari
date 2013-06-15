define ["zeezoo", "auth/PermissionService"], ->
  angular.module('zeezoo').controller 'DetailsController', ($scope, PermissionService) ->
    templateForMode = {}
    templateForMode[$scope.modes.VIEWER] = 'templates/placemark-view.html'
    templateForMode[$scope.modes.EDITOR] = 'templates/placemark-edit.html'
    templateForMode[$scope.modes.CREATOR] = 'templates/placemark-create.html'

    changeTemplate = (mode) ->
      $scope.template = "/assets/" + templateForMode[mode]

    changePermissions = (placemark) ->
      PermissionService.userCanModify placemark, (modificationStatus) ->
        $scope.userCanModify = modificationStatus.canModify

    $scope.$watch 'mode', changeTemplate
    $scope.$watch 'activePlacemark', changePermissions

    $scope.deactivate = ->
      $scope.deactivateCurrentPlacemark()
      $scope.changeMode $scope.modes.VIEWER

    $scope.editActivePlacemark = ->
      $scope.changeMode $scope.modes.EDITOR

    $scope.deleteActivePlacemark = ->
      $scope.activePlacemark.$delete ->
        $scope.removePlacemark $scope.activePlacemark
        $scope.deactivate()
