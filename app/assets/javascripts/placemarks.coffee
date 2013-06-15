define "zeezoo", ->
  m = angular.module "zeezoo"

  m.factory 'Placemark', ($resource) ->
    urlTemplate = '/api/placemark/:id'

    Placemark = $resource urlTemplate, {id: '@id'}, {
      find: {method: "POST", isArray: true}
    }

    Placemark::equalsTo = (placemark) ->
      @id == placemark.id

    Placemark

  m.service "placemarksService", ($http, Placemark, $rootScope)->
    # WTF?
    @vertical = {title: null, filters: {}, props: {}}
    @filters = {filters: {}, props: {}}

    @UPDATE_PLACEMARKS = "updatePlacemarks"

    @loadVertical = (code, callback=null)=>
      $http.get("/api/vertical/#{code}").success (data)=>
        @vertical = data
        @filters = {filters: {}, props: {}}
        @filterQuery = {}
        callback?(@vertical)
        @loadPlacemarks()

    @filtersChanged = {}

    @filterQuery = {}
    @borderQuery = {}

    @borderChanged = (border)=>
      southEast = border.getSouthEast()
      northWest = border.getNorthWest()
      @borderQuery =
        coordinates:
          '$within':
            '$box': [[southEast.lng, northWest.lat], [northWest.lng, southEast.lat]]
      @loadPlacemarks()


    @filterChanged = (filter)=>
      @filterQuery = {}

      filters = (k for k, v of filter.filters when v)
      props = (k for k, v of filter.props when v)

      if filters?.length
        @filterQuery.filters = filters

      if props?.length
        @filterQuery.props = props

      @loadPlacemarks()

    @loadPlacemarks = (callback=null)=>
      q = angular.copy @filterQuery
      q.vertical = @vertical.code
      q.coordinates = @borderQuery.coordinates
      console.log @filterQuery
      q.filters = {"$in" : @filterQuery.filters} if @filterQuery.filters?.length
      q.props = {"$all" : @filterQuery.props} if @filterQuery.props?.length
      console.log q

      Placemark.query {q: JSON.stringify(q)}, (placemarks)=>
        $rootScope.$broadcast(@UPDATE_PLACEMARKS, placemarks)
        callback?()

    @pushPlacemark = (placemark, callback=null)=>
      p = new Placemark(placemark)

      p.filters = @filterQuery.filters if @filterQuery.filters
      p.props = @filterQuery.props if @filterQuery.props
      p.vertical = @vertical.code

      p.$save (data)=>
        @getPlacemark(data.id, callback)

    @createPlacemark = (data)=> new Placemark(data)

    @getPlacemark = (id, callback=null) => Placemark.get({id: id}, callback)
