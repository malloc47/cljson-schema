# cljson-schema

JSON Schema validation in Clojure

[![Clojars Project](http://clojars.org/com.malloc47/cljson-schema/latest-version.svg)](http://clojars.org/com.malloc47/cljson-schema)

## Background

Uses
[fge/json-schema-validator](https://github.com/fge/json-schema-validator)
under the hood. Similar to [scjsv](https://github.com/metosin/scjsv)
but uses multimethods to dispatch on the validator and validated types
and includes a configuration map to customize the `JsonSchemaFactory`
used for the validator.

## Usage

```clojure
(require '[cljson-schema.core :as jschema])

(let [validator (-> "your-schema.json"
                    io/as-file
                    (.toURI)
                    (jschema/validator))]
  (when-not (nil? (jschema/validate validator {:put :your :data :here}))
    (throw (ex-info "Schema validation error!" {}))))
```

## License

Copyright © 2015 [Jarrell Waggoner](http://www.malloc47.com)

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
