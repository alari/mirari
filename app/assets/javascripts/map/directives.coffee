define ["map/utils", "map/LeafletMapFactory", "map/behaviours", "map/MapContainer"], ->
  module = angular.module 'infra-map'

  module.directive 'sleepyMap', (LeafletMapFactory, MapContainer, BasicBehaviour)->
    defaultParameters =
      initialCenter: [59.943, 30.305]
      initialZoom: 13

    restrict: 'E'
    template: '<div></div>'
    replace: true

    link: (scope, element, attributes) ->
      scope.prepareParameters = ->
        if attributes.parameters
          scope.parameters = scope.$eval attributes.parameters
        else
          scope.parameters = defaultParameters

        if attributes.init
          scope.delegateInit = scope.$eval attributes.init
        else
          scope.delegateInit = ->

      scope.initialize = ->
        factory = new LeafletMapFactory scope.parameters
        map = factory.createMap element[0]
        scope.container = new MapContainer map, scope
        scope.container.add BasicBehaviour
        scope.container.attach BasicBehaviour


      scope.prepareParameters()
      scope.initialize()
      scope.delegateInit(scope.container)
