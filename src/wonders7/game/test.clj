(ns wonders7.game.test
  (:require [wonders7.game.state :as state]
            [wonders7.game.play :as play]
            [wonders7.core.ws-api :refer [notify-clients]]))

; just a random turn
(defn test-turn []
  (do
    (play/pick :card (first (shuffle (keys @(get-in @(:players state/current-state) [1 :hand])))) :player 1)
    (play/pick :card (first (shuffle (keys @(get-in @(:players state/current-state) [2 :hand])))) :player 2 :sell true)
    (play/pick :card (first (shuffle (keys @(get-in @(:players state/current-state) [3 :hand])))) :player 3)))

;(do (test-turn) (notify-clients))
