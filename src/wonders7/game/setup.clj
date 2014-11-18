(ns wonders7.game.setup
  (:require [wonders7.game.state :as state]
            [wonders7.game.play :as play]))

; adds an entry to the players map located in the current game state
(defn join-game [& {:keys [player-name player-id]}]
  (dosync
    (when-not @(:in-progress state/current-state)
      (let [free-seats (:free-seats state/current-state)]
        (when (> @free-seats 0)
          (alter (:players state/current-state) into [[(- 8 @free-seats)
                                                       {:name player-name
                                                        :id player-id
                                                        :hand (ref {})
                                                        :table (ref #{})
                                                        :cash (ref 3)
                                                        :war-score (ref {})}]])
          (alter free-seats dec))))))

; function for populating hands
(defn deal [& {:keys [age] :or {age 1}}]
  (loop [card-pool (shuffle (state/get-deck age)) player-pool (keys @(:players state/current-state))]
    (if
      (empty? player-pool) nil
      (do
        (dosync
          (alter
            (get-in @(:players state/current-state) [(first player-pool) :hand])
            (fn [x] (apply merge-with + (map (fn [y] {y 1}) (take 7 card-pool))))))
        (recur (drop 7 card-pool) (drop 1 player-pool))))))

; SETUP mark the game as started, deal the first age
(defn start-game []
  (dosync
    (when (not @(:in-progress state/current-state))
      (alter (:in-progress state/current-state) (fn [x] true))
      (add-watch (:picks state/current-state)
                 :picks-watch
                 (fn [k r old-state new-state]
                   (when (= (count new-state) (count @(get state/current-state :players)))
                     (play/process-picks))))
      (deal :age 1))))

; SETUP reset the game state
(defn reset-game []
  (dosync
    (alter (:in-progress state/current-state) (fn [x] false))
    (alter (:free-seats state/current-state) (fn [x] 7))
    (alter (:age state/current-state) (fn [x] 1))
    (alter (:trash state/current-state) (fn [x] []))
    (alter (:players state/current-state) (fn [x] {}))
    (remove-watch (:picks state/current-state) :picks-watch)
    (alter (:picks state/current-state) (fn [x] {}))))
