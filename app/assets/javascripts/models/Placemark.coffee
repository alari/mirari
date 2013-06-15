define "zeezoo", ->
  angular.module("zeezoo").factory 'Placemark', ($resource) ->
    urlTemplate = '/api/placemark/:PlacemarkId'

    Placemark = $resource urlTemplate, {PlacemarkId: '@id'}, {
      find: {method: "POST", isArray: true}
    }

    Placemark::equalsTo = (placemark) ->
      @id == placemark.id

    Placemark