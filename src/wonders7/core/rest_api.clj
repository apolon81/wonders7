(ns wonders7.core.rest-api
  (:require [wonders7.game.state :as game]
            [clojure.data.json :as json]
            [wonders7.core.handler :refer [msg-broadcast]]
            [clojure.tools.logging :refer [info]]))

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/write-str data)})

; TODO - obfuscate cards not on the table, except for this player's hand (lookup by id)
(defn state [id]
  (json-response (game/state-view)))

(defn join [nick id]
  (do
    (game/join-game :player-name nick :player-id id)
    (state id)))

(defn reset [id]
  (do
    (game/reset-game)
    (state id)))
