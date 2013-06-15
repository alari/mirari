define ["zeezoo"], ->
  m = angular.module "zeezoo"

  m.factory "BaseMarker", ->
    class
      constructor: (location, @_layer) ->
        @_marker = new L.Marker location, @_defaultMarkerParameters
        @_layer.addLayer @_marker

      remove: ->
        @_layer.removeLayer @_marker

  m.factory "Marker", (BaseMarker, ZeezooIcon)->
    class extends BaseMarker
      _defaultMarkerIcon: new ZeezooIcon(iconUrl: '/assets/images/marker-icon.png')
      _clickedMarkerIcon: new ZeezooIcon(iconUrl: '/assets/images/clicked-marker-icon.png')

      _defaultMarkerParameters:
        icon: @:: _defaultMarkerIcon

      constructor: (@placemark, @_layer) ->

        location = @placemark.coordinates

        super(location, @_layer)
        @_marker.bindPopup @placemark.title, {offset: new L.Point 0, -30}
        @isActive = false

      activate: ->
        @_marker.setIcon @_clickedMarkerIcon
        @_marker.openPopup()
        @isActive = true

      deactivate: ->
        @_marker.setIcon @_defaultMarkerIcon
        @_marker.closePopup()
        @isActive = false

      toggle: ->
        if @isActive
          @deactivate()
        else
          @activate()

      registerOnClickCallback: (callback) ->
        @_marker.on 'click', =>
          @toggle()
          callback()

      getState: ->
        isActive: @isActive

      setState: (state) ->
        if @isActive != state.isActive
          @toggle()