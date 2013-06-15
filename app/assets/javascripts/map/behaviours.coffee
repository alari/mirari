define ["map/utils"], ->
  m = angular.module "infra-map"

  m.factory "AbstractLeafletBehaviour", (mapUtils)->
    class

      constructor: (@map, @scope, @prefix) ->
        @_attached = false

      isAttached: ->
        @_attached

      attach: ->
        @_attached = true
        @_doAttach?()

      detach: ->
        @_attached = false
        @_doDetach?()

      _emit: (event, parameters...) ->
        prefixedEvent = @_createPrefixedEvent event
        @scope?.$emit prefixedEvent, parameters...

      _createPrefixedEvent: (event) ->
        mapUtils.createPrefixedEvent event, @prefix

  m.factory "BasicBehaviour", (AbstractLeafletBehaviour)->
    class extends AbstractLeafletBehaviour

      @BOUNDS_CHANGED: 'map-bounds-changed'

      @MAP_CLICKED: 'map-clicked'

      _doAttach: ->
        @map.on 'dragend zoomend', @_onBoundsChange
        @map.on 'click', @_onMapClick
        @_onBoundsChange()

      _doDetach: ->
        @map.off 'dragend zoomend', @_onBoundsChange
        @map.off 'click', @_onMapClick

      _onBoundsChange: =>
        @_emit @constructor.BOUNDS_CHANGED, @map.getBounds()

      _onMapClick: (event) =>
        @_emit @constructor.MAP_CLICKED, event.latlng