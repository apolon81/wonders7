(ns wonders7.game.play
  (:require [wonders7.game.state :as state]))

; hepler for inc/dec cash or possibly sth else, must be called in a transaction
(defn gain [player quantity subject]
  (alter (get-in @(:players state/current-state) [player subject]) + quantity))

; this takes the card out of a player's hand
(defn hand-pull [card player]
  (alter
    (get-in @(:players state/current-state) [player :hand])
    (fn [x] (into {} (filter #(> (second %) 0) (update-in x [card] dec))))))

; remove from hand, place in the trash
(defn trash [card player]
  (do
    (hand-pull card player)
    (alter (:trash state/current-state) conj card)))

; remove from hand, place on the table
(defn table-put [card player]
  (do
    (hand-pull card player)
    (alter (get-in @(:players state/current-state) [player :table]) conj card)))

; TODO real implementation
(defn can-afford [player card trades]
  true)

; TODO real implementation
(defn pay-costs [player card trades]
  true)

; inject the player as the first argument, then call the effect
(defn do-effects [card-effect player]
  (when-not (nil? card-effect)
    (eval (conj (drop 1 card-effect) player (first card-effect)))))

; function for playing a card
(defn play [& {:keys [player card sell trades] :or {sell false}}]
  (dosync
    (if (or sell (not (can-afford card player trades)))
      (do
        (gain player 3 :cash)
        (trash card player))
      (do
        (pay-costs card player trades)
        (table-put card player)
        (do-effects (get-in wonders7.game.cards/cards [card :effect]) player)))))

; helper for passing cards round the table
(defn pass-along []
  (dosync
    (let [hand-mapping (into {}
                         (for [player-no (keys @(:players state/current-state))]
                           [(inc (mod (+ (dec player-no) (if (odd? @(:age state/current-state)) 1 -1)) (count @(:players state/current-state))))
                            @(get-in @(:players state/current-state) [player-no :hand])]))]
      (doseq [player-no (keys @(:players state/current-state))]
        (alter (get-in @(:players state/current-state) [player-no :hand]) (fn [x] (get hand-mapping player-no)))))))

; play the picked cards, pass hands round the table
(defn process-picks []
  (dosync
    (doseq [[k v] @(:picks state/current-state)]
      (play :card (:card v) :player k :sell (:sell v) :trades (:trades v)))
    (alter (:picks state/current-state) (fn [x] {}))
    (pass-along)))

; save a decission for further processing
(defn pick [& {:keys [player card sell trades] :or {sell false}}]
  (dosync
    (alter (:picks state/current-state) into [[player {:card card, :sell sell, :trades trades}]])))
