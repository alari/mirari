define ->
  m = angular.module "infra-map", []

  m.service "mapUtils", ->
    @createPrefixedEvent = (event, prefix) ->
      if prefix
        "#{prefix}-#{event}"
      else
        event

    # function from CoffeeScript Cookbook
    @uniqueId = (length = 8) ->
      id = ""
      id += Math.random().toString(36).substr(2) while id.length < length
      id.substr 0, length
