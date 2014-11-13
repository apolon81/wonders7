(ns wonders7.core.rest-api
  (:require [wonders7.game.state :as game]
            [clojure.data.json :as json]
            [clojure.tools.logging :refer [info]]
            [wonders7.core.ws-api :refer [notify-clients json-response]]))

; TODO - obfuscate cards not on the table, except for this player's hand (lookup by id)
(defn state [player-id]
  (json-response (game/state-view)))

(defn join [nick player-id]
  (if (game/player-exists player-id)
    (json-response (json/write-str {:reason "can not join again from the same client"}) 406)
    (if @(:in-progress game/current-state)
      (json-response (json/write-str {:reason "can not join to an in progress game"}) 406)
      (do
        (game/join-game :player-name nick :player-id player-id)
        (notify-clients)
        (json-response (json/write-str {:your_player_number (first
                                                              (first
                                                                (filter #(= (:id (second %)) player-id)
                                                                        (:players (game/state-view)))))}))))))

(defn reset [player-id]
  (do
    (game/reset-game)
    (notify-clients)))

(defn start [player-id]
  (if @(:in-progress game/current-state)
    (json-response (json/write-str {:reason "can not start a game which is in progress"}) 406)
    (do
      (game/start-game)
      (notify-clients))))

;(do (game/test-turn) (notify-clients))
