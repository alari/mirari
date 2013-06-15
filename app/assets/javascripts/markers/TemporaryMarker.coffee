define ["zeezoo", "markers/Marker", "markers/ZeezooIcon"], ->
  m = angular.module "zeezoo"

  m.factory "TemporaryMarker", (BaseMarker, ZeezooIcon)->
    class extends BaseMarker
      _defaultMarkerParameters:
        icon: new ZeezooIcon({iconUrl: '/assets/images/temporary-marker-icon.png'})
        clickable: false

      deactivate: ->
        @remove()