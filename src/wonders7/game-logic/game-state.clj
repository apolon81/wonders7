(ns wonders7)

(def base-decks
  {1 ["Barracks" "Altar" "Ore Vein" "Loom" "Guard Tower" "West Trading Post" "Clay Pool"
      "Clay Pit" "Marketplace" "Press" "Stone Pit" "Apothecary" "Scriptorium" "Glassworks"
      "Stockade" "Workshop" "Lumber Yard" "East Trading Post" "Timber Yard" "Theater" "Baths"]
   2 ["Aqueduct" "Statue" "Foundry" "Temple" "School" "Press" "Vineyard"
      "Loom" "Dispensary" "Library" "Quarry" "Laboratory" "Sawmill" "Archery Range"
      "Forum" "Walls" "Courthouse" "Brickyard" "Caravansery" "Stables" "Glassworks"]
   3 []})

(def guilds [])

(defn get-deck [age]
  (get base-decks age))

; fresh game state template
(def initial-state
  {1 {:hand {}, :table #{}, :cash 3, :war-score 0}
   2 {:hand {}, :table #{}, :cash 3, :war-score 0}
   3 {:hand {}, :table #{}, :cash 3, :war-score 0}})

; runs a function on each of the values in a map
(defn map-vals [m f]
  (into {} (for [[k v] m] [k (f v)])))

; create refs on the inner keys in the game state
(def current-state (map-vals initial-state #(map-vals % ref)))

; hepler for inc/dec cash or war-score, must be called in a transaction
(defn gain [player quantity subject]
  (alter (get-in current-state [player subject]) + quantity))

; helper for populating hands
(defn deal [& {:keys [age] :or {age 1}}]
  (loop [card-pool (shuffle (get-deck age)) player-pool (keys current-state)]
    (if
      (empty? player-pool) nil
      (do
        (dosync
          (alter
            (get-in current-state [(first player-pool) :hand])
            (fn [x] (apply merge-with + (map (fn [y] {y 1}) (take 7 card-pool))))))
        (recur (drop 7 card-pool) (drop 1 player-pool))))))

; helper for playing a card
(defn play [& {:keys [player card sell] :or {sell false}}]
  (let [card-effect (get-in cards [card :effect])]
    (dosync
      (alter (get-in current-state [player :hand]) (fn [x] (into {} (filter #(> (second %) 0) (update-in x [card] dec)))))
      (if sell
        (gain player 3 :cash)
        (do
          (alter (get-in current-state [player :table]) conj card)
          (when-not (nil? card-effect) (eval (conj (drop 1 card-effect) player (first card-effect)))))))))

current-state

(deal :age 1)

(play :card "Baths" :player 2 :sell true)
