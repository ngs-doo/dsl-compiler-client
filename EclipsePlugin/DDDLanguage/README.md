##### DSL Editor eclipse plugin

Editor has options to:

1. Log work (DSLLogger)
2. Mark errors (DSLMarker)
3. Syntax highlight (Line style listener in editor)

Marker and syntax highlight parts need an object that holds line and style information.
Currently that object is of DSLToken type.
Information that is needed is line number, offset and associated style

Parser always returns DSLToken(DSLError is also included)

Reconciler is the part that scans the document when something is written, or the resource is saved.
Reconciler calls parser, that returns dsl token.

dsl token is passed to marker handler, and line style listener.

