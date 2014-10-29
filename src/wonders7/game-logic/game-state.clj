(ns wonders7)

(def initial-state
  {:player-one {:hand [], :table [], :cash 3, :war-score 0}
   :player-two {:hand [], :table [], :cash 3, :war-score 0}
   :player-three {:hand [], :table [], :cash 3, :war-score 0}})

(def current-state (ref initial-state))

(defn gain [player quantity subject]
  (dosync
    (alter current-state update-in [player subject] + quantity)))

(defn deal [age]
  (loop [card-pool (shuffle (keys age)) player-pool (keys @current-state)]
    (if
      (empty? player-pool) nil
      (do
        (dosync
          (alter current-state update-in [(first player-pool) :hand] into (take 7 card-pool)))
        (recur (drop 7 card-pool) (drop 1 player-pool))))))

(eval (get-in second-age ["Vineyard" :effect]))

(deal first-age)

@current-state

;(play-card :player-one "Vineyard")
