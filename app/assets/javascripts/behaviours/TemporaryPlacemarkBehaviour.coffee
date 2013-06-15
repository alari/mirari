define ["zeezoo", "map/behaviours", "behaviours/PlacemarkBehaviour", "markers/TemporaryMarker"], ->
  m = angular.module "zeezoo"

  m.factory "TemporaryPlacemarkBehaviour", (AbstractLeafletBehaviour, PlacemarkBehaviour, TemporaryMarker)->
    class TemporaryPlacemarkBehaviour extends AbstractLeafletBehaviour

      @TEMPORARY_PLACEMARK_CREATED: 'temporary-placemark-created'

      constructor: (map, scope, prefix) ->
        @_layer = new L.LayerGroup()
        @_listenerCancelers = []
        super(map, scope, prefix)

      _doAttach: ->
        @map.addLayer @_layer
        @_registerEventListeners()

      _doDetach: ->
        @map.removeLayer @_layer
        cancelListener() for cancelListener in @_listenerCancelers
        @_listenerCancelers = []
        @_deactivateActiveMarker()

      _registerEventListeners: ->
        @_listenerCancelers.push @_registerListener @_createPrefixedEvent(PlacemarkBehaviour.PLACEMARK_DEACTIVATION_REQUESTED), @_deactivateActiveMarker
        @_listenerCancelers.push @_registerListener @_createPrefixedEvent(TemporaryPlacemarkBehaviour.TEMPORARY_PLACEMARK_CREATED), @_addTemporaryMarker

      _registerListener: (eventType, listener) ->
        @scope.$on eventType, (event, parameters...) ->
          listener parameters...

      _addTemporaryMarker: (location) =>
        @_changeActiveMarker new TemporaryMarker location, @_layer

      _deactivateActiveMarker: =>
        @_activeMarker?.deactivate()
        @_activeMarker = null
        @_emit PlacemarkBehaviour.PLACEMARK_DEACTIVATED, marker?.placemark

      _changeActiveMarker: (marker) ->
        @_deactivateActiveMarker()
        @_activeMarker = marker
        @_emit PlacemarkBehaviour.PLACEMARK_ACTIVATED, marker?.placemark