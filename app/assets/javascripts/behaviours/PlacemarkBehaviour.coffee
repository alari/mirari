define ["zeezoo", "markers/Marker", "map/behaviours"], ->
  m = angular.module "zeezoo"

  m.factory "PlacemarkBehaviour", (AbstractLeafletBehaviour, Marker)->
    class PlacemarkBehaviour extends AbstractLeafletBehaviour

      @MARKER_CLICKED: 'marker-clicked'

      @PLACEMARK_ACTIVATED: 'placemark-activated'

      @PLACEMARK_DEACTIVATED: 'placemark-deactivated'

      @PLACEMARK_DEACTIVATION_REQUESTED: 'placemark-deactivation-requested'

      constructor: (map, scope, prefix, @_placemarksArrayLabel)->
        @_layer = new L.LayerGroup()
        @_markers = []
        @_listenerCancelers = []
        super(map, scope, prefix)

      _doAttach: ->
        @map.addLayer @_layer
        @_watchForPlacemarks()
        @_registerEventListeners()

      _doDetach: ->
        @map.removeLayer @_layer
        @_unwatch()
        cancelListener() for  cancelListener in @_listenerCancelers
        @_listenerCancelers = []

      _registerEventListeners: ->
        @_listenerCancelers.push @_registerListener @_createPrefixedEvent(PlacemarkBehaviour.MARKER_CLICKED), @_proceedPlacemarkActivation
        @_listenerCancelers.push @_registerListener @_createPrefixedEvent(PlacemarkBehaviour.PLACEMARK_DEACTIVATION_REQUESTED), @_deactivateActiveMarker

      _registerListener: (eventType, listener) ->
        @scope.$on eventType, (event, parameters...) ->
          listener(parameters...)

      _proceedPlacemarkActivation: (marker) =>
        if @_activeMarker != marker
          @_changeActiveMarker marker
        else
          @_deactivateActiveMarker()

      _deactivateActiveMarker: =>
        @_activeMarker?.deactivate()
        @_activeMarker = null
        @_emit PlacemarkBehaviour.PLACEMARK_DEACTIVATED, marker?.placemark

      _changeActiveMarker: (marker) ->
        @_deactivateActiveMarker()
        @_activeMarker = marker
        @_emit PlacemarkBehaviour.PLACEMARK_ACTIVATED, marker?.placemark

      _replaceActiveMarker: (marker) ->
        marker.setState @_activeMarker.getState()
        @_activeMarker = marker


      _addMarker: (placemark) ->
        marker = new Marker placemark, @_layer

        onMarkerClickCallback = @_createOnMarkerClickCallback marker
        marker.registerOnClickCallback onMarkerClickCallback

        if @_activeMarker?.placemark?.equalsTo(placemark)
          @_replaceActiveMarker marker

        @_markers.push marker

      _cleanUpMarkers: ->
        marker.remove() for marker in @_markers
        @_markers = []

      _createOnMarkerClickCallback: (marker) ->
        @_createEventCallback =>
          @_emit PlacemarkBehaviour.MARKER_CLICKED, marker

      _createEventCallback: (callbackBody) ->
        (event) =>
          @scope.$apply ($scope) ->
            callbackBody event

      _placemarksListener: (newValue, oldValue, $scope) =>
        @_cleanUpMarkers()
        @_addMarker placemark for placemark in newValue

      _watchForPlacemarks: ->
        @_unwatch = @scope.$watch @_placemarksArrayLabel, @_placemarksListener, true