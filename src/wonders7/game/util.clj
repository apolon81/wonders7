(ns wonders7.game.util
  (:require [wonders7.game.state :as state]))

; utility function to check if a given player sits at the table
(defn player-exists [player-id]
  (some (conj #{} player-id) (for [[k v] @(:players state/current-state)] (:id v))))

; this resolves all the references to provide a snapshot of the game state
(defn state-view []
  (into {} [[:players (into (sorted-map)
                        (for [[k v] @(:players state/current-state)]
                             [k (into {} [[:name (:name v)]
                                          [:id (:id v)]
                                          [:hand (deref (:hand v))]
                                          [:table (deref (:table v))]
                                          [:cash (deref (:cash v))]
                                          [:war-score (deref (:war-score v))]])]))]
            [:trash (deref (:trash state/current-state))]
            [:picks (deref (:picks state/current-state))]
            [:age (deref (:age state/current-state))]
            [:free-seats (deref (:free-seats state/current-state))]
            [:in-progress (deref (:in-progress state/current-state))]]))
