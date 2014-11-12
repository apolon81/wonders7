(ns wonders7.core.ws-api
  (:require [manifold.stream :as stream]
            [clojure.data.json :as json]
            [clojure.tools.logging :refer [info]]
            [wonders7.game.state :as game]))

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
    (when (= (:message data) "introduce")
      (if (game/player-exists (:uuid data))
        (swap! clients update-in [ws] (fn [x] (:uuid data)))
        (stream/put! ws (json/write-str {:command "introduce", :uuid (get @clients ws)}))))))

(defn notify-clients []
  (msg-broadcast (json/write-str {:command "refresh"}))
  (json-response nil))


@clients

(game/player-exists "e9ec92de-2b3a-4d8f-9ec6-e22418370dd5")
