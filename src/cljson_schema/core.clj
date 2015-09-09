(ns cljson-schema.core
  (:require [cheshire.core :as json])
  (:import [com.fasterxml.jackson.databind ObjectMapper JsonNode]
           [com.github.fge.jackson JsonLoader]
           [com.github.fge.jsonschema.core.load.configuration LoadingConfiguration]
           [com.github.fge.jsonschema.core.load.uri URITranslatorConfiguration]
           [com.github.fge.jsonschema.core.report ListProcessingReport ProcessingMessage]
           [com.github.fge.jsonschema.main JsonSchema]
           [com.github.fge.jsonschema.main JsonSchemaFactory]))

(defmulti validator* (fn [factory schema opts] (class schema)))

(defmethod validator* java.net.URI [factory schema opts]
  (.getJsonSchema ^JsonSchemaFactory factory (.toString schema)))

(defmethod validator* java.net.URL [factory schema opts]
  (validator* factory (.toURI schema) opts))

(defmethod validator* String [factory schema opts]
  (let [schema-object (.readTree ^ObjectMapper (ObjectMapper.) ^String schema)]
    (.getJsonSchema ^JsonSchemaFactory factory ^JsonNode schema-object)))

(defmethod validator* clojure.lang.APersistentMap [factory schema opts]
  (validator* factory (json/generate-string schema) opts))

(defn validator [schema {:keys [namespace] :as opts}]
  (let [uri-translator
        (cond-> (URITranslatorConfiguration/newBuilder)
          namespace (.setNamespace namespace)
          true (.freeze))

        loading-configuration
        (cond-> (LoadingConfiguration/newBuilder)
          namespace (.setURITranslatorConfiguration uri-translator)
          true (.freeze))

        factory
        (-> (JsonSchemaFactory/newBuilder)
            (.setLoadingConfiguration loading-configuration)
            (.freeze))]

    (validator* factory schema opts)))

(defmulti validate* (fn [_ obj] (class obj)))

(defmethod validate* clojure.lang.APersistentMap [validator obj]
  (validate* validator (json/generate-string obj)))

(defmethod validate* String [validator obj]
  (.validate ^JsonSchema validator ^JsonNode (JsonLoader/fromString obj)))

(defn validate [validator obj]
  (let [report (validate* validator obj)
        errors (-> (ListProcessingReport.)
                   (doto (.mergeWith report))
                   (.iterator)
                   (iterator-seq))]
    (some->>
     (seq errors)
     (map (fn [error]
            (-> (.asJson ^ProcessingMessage error) str (json/parse-string true)))))))
