(ns wonders7.core.ws-api
  (:require [manifold.stream :as stream]
            [clojure.data.json :as json]
            [clojure.tools.logging :refer [info]]))

(def clients (atom {}))

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/write-str data)})

(defn msg-to-client [[client-stream uuid] msg]
  (stream/put! client-stream msg))

(defn msg-broadcast [msg]
  (doseq [[client-stream uuid] @clients]
    (stream/put! client-stream msg)))

(defn msg-from-client [msg ws]
  (let [data (json/read-json msg)]
    (info "mesg received" data)
    (when (= (:command data) "join")
      (info "processing join command"))))

(defn notify-clients []
  (msg-broadcast "ping")
  (json-response nil))
