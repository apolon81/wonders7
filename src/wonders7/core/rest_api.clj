(ns wonders7.core.rest-api
  (:require [wonders7.game.state :as state]
            [wonders7.game.setup :as setup]
            [wonders7.game.play :as play]
            [clojure.data.json :as json]
            [clojure.tools.logging :refer [info]]
            [wonders7.core.ws-api :refer [notify-clients json-response]]
            [wonders7.game.util :as util]))

; TODO - obfuscate cards not on the table, except for this player's hand (lookup by id)
(defn state [player-id]
  (json-response (util/state-view)))

(defn join [nick player-id]
  (if (util/player-exists player-id)
    (json-response (json/write-str {:reason "can not join again from the same client"}) 406)
    (if @(:in-progress state/current-state)
      (json-response (json/write-str {:reason "can not join to an in progress game"}) 406)
      (do
        (setup/join-game :player-name nick :player-id player-id)
        (notify-clients)
        (json-response (json/write-str {:your_player_number (first
                                                              (first
                                                                (filter #(= (:id (second %)) player-id)
                                                                        (:players (util/state-view)))))}))))))

(defn reset [player-id]
  (do
    (setup/reset-game)
    (notify-clients)))

(defn start [player-id]
  (if @(:in-progress state/current-state)
    (json-response (json/write-str {:reason "can not start a game which is in progress"}) 406)
    (do
      (setup/start-game)
      (notify-clients))))

(defn pick [plrno card id]
  (let [player (Integer. plrno)]
    (if @(:in-progress state/current-state)
      (if (= id (:id (get @(:players state/current-state) player)))
        (do
          (play/pick :card card :player player)
          (notify-clients))
        (json-response (json/write-str {:reason "you cannot pick for other player"}) 406))
      (json-response (json/write-str {:reason "can not pick when the game is not started yet"}) 406))))
