(ns wonders7)

(def initial-state
  {:player-one {:hand [], :table [], :cash 3, :war-score 0}
   :player-two {:hand [], :table [], :cash 3, :war-score 0}
   :player-three {:hand [], :table [], :cash 3, :war-score 0}})

(defn map-vals [m f]
  (into {} (for [[k v] m] [k (f v)])))

(defn initialize [state]
  (map-vals state #(map-vals % ref)))

(def current-state (initialize initial-state))

(defn gain [player quantity subject]
  (dosync
    (alter (get-in current-state [player subject]) + quantity)))

(defn deal [age]
  (loop [card-pool (shuffle (keys age)) player-pool (keys current-state)]
    (if
      (empty? player-pool) nil
      (do
        (dosync
          (alter (get-in current-state [(first player-pool) :hand]) into (take 7 card-pool)))
        (recur (drop 7 card-pool) (drop 1 player-pool))))))

(get-in current-state [:player-one :hand])

(eval (get-in second-age ["Vineyard" :effect]))

current-state

(deal first-age)

;(play-card :player-one "Vineyard")
