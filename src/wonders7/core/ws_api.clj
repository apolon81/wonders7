(ns wonders7.core.ws-api
  (:require [manifold.stream :as stream]
            [clojure.data.json :as json]
            [clojure.tools.logging :refer [info]]))

(def clients (atom {}))

(defn msg-to-client [[client-stream uuid] msg]
  (stream/put! client-stream msg))

(defn msg-broadcast [msg]
  (map #(msg-to-client % msg) @clients))

(defn msg-from-client [msg ws]
  (let [data (json/read-json msg)]
    (info "mesg received" data)
    (when (= (:command data) "join")
      (info "processing join command"))))
