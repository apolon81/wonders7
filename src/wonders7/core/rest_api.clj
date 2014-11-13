(ns wonders7.core.rest-api
  (:require [wonders7.game.state :as game]
            [clojure.tools.logging :refer [info]]
            [wonders7.core.ws-api :refer [notify-clients json-response]]))

; TODO - obfuscate cards not on the table, except for this player's hand (lookup by id)
(defn state [player-id]
  (json-response (game/state-view)))

(defn join [nick player-id]
  (do
    (game/join-game :player-name nick :player-id player-id)
    (notify-clients)))

(defn reset [player-id]
  (do
    (game/reset-game)
    (notify-clients)))

(defn start [player-id]
  (do
    (game/start-game)
    (notify-clients)))
