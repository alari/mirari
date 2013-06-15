define "zeezoo", ->
  angular.module("zeezoo").service "mapService", ($http, Placemark, $rootScope)->
    # WTF?
    @vertical = {title: null, filters: {}, props: {}}
    @filters = {filters: {}, props: {}}

    @UPDATE_PLACEMARKS = "updatePlacemarks"

    @loadVertical = (code, callback=null)=>
      $http.get("/api/vertical/#{code}").success (data)=>
        @vertical = data
        @filters = {filters: {}, props: {}}
        callback?(@vertical)

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
      q.coordinates = @borderQuery.coordinates
      q.filters = {"$in" : @filterQuery.filters} if @filterQuery.filters
      console.log q
      Placemark.query {q: JSON.stringify(q)}, (placemarks)=>
        $rootScope.$broadcast(@UPDATE_PLACEMARKS, placemarks)
        callback?()

    @pushPlacemark = (placemark, callback=null)=>
      p = new Placemark(placemark)

      p.filters = @filterQuery.filters if @filterQuery.filters
      p.props = @filterQuery.props if @filterQuery.props
      p.vertical = @vertical.code
      p.$save (data)->
        Placemark.get({id: data.id}, callback)