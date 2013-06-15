require ["zeezoo"], ->
  m = angular.module "zeezoo"

  m.factory "ZeezooIcon", ->
    class extends L.Icon
      options:
        shadowUrl: '/assets/images/marker-shadow.png'
        iconAnchor: [13, 41]