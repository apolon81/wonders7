(ns wonders7.core.rest-api
  (:require [wonders7.game.state :as game]
            [clojure.data.json :as json]
            [clojure.tools.logging :refer [info]]))

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/write-str data)})

; TODO - obfuscate cards not on the table, except for this player's hand (lookup by id)
(defn state [player-id]
  (json-response (game/state-view)))

(defn join [nick player-id]
  (do
    (game/join-game :player-name nick :player-id player-id)
    (state player-id)))

(defn reset [player-id]
  (do
    (game/reset-game)
    (state player-id)))

(defn start [player-id]
  (do
    (game/start-game)
    (game/deal :age 1)
    (state player-id)))
