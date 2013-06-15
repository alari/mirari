require [
  "lib/jquery/jquery",
  "lib/angular/angular",
], ->
  require [
    "lib/angular-resource/angular-resource"
    "lib/angular-ui/build/angular-ui",
    "lib/angular-ui/build/angular-ui-ieshiv",
    "lib/angular-bootstrap/ui-bootstrap"
  ], ->
    require ["zeezoo"], ->
      console.log "run!"