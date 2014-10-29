(def initial-state
  {:player-one {:hand [], :table [], :cash 3, :war-score 0}
   :player-two {:hand [], :table [], :cash 3, :war-score 0}
   :player-three {:hand [], :table [], :cash 3, :war-score 0}})

(def current-state (ref initial-state))

(defn gain [player quantity subject]
  (dosync
    (alter current-state
      (fn [state player quantity subject]
        (assoc-in state [player]
          (update-in (get state player) [subject] #(+ % quantity))))
      player quantity subject)))

(gain :player-one 2 :cash)
