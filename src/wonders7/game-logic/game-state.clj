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
  {:players {}
   :trash #{}
   :age 1
   :free-seats 7
   :in-progress false})

; create refs on the inner keys in the game state
(def current-state
  (into {} (for [[k v] initial-state] [k (ref v)])))

; adds an entry to the players map located in the current game state
(defn join-game [player-name]
  (dosync
    (when-not @(get current-state :in-progress)
      (let [free-seats (get current-state :free-seats)]
        (when (> @free-seats 0)
          (alter (get current-state :players) into [[(- 8 @free-seats) {:name player-name :hand (ref {}) :table (ref #{}) :cash (ref 3) :war-score (ref 0)}]])
          (alter free-seats dec))))))

; hepler for inc/dec cash or war-score, must be called in a transaction
(defn gain [player quantity subject]
  (alter (get-in @(get current-state :players) [player subject]) + quantity))

; helper for populating hands
(defn deal [& {:keys [age] :or {age 1}}]
  (loop [card-pool (shuffle (get-deck age)) player-pool (keys @(get current-state :players))]
    (if
      (empty? player-pool) nil
      (do
        (dosync
          (alter
            (get-in @(get current-state :players) [(first player-pool) :hand])
            (fn [x] (apply merge-with + (map (fn [y] {y 1}) (take 7 card-pool))))))
        (recur (drop 7 card-pool) (drop 1 player-pool))))))

; helper for playing a card
(defn play [& {:keys [player card sell] :or {sell false}}]
  (let [card-effect (get-in cards [card :effect])]
    (dosync
      (alter (get-in @(get current-state :players) [player :hand]) (fn [x] (into {} (filter #(> (second %) 0) (update-in x [card] dec)))))
      (if sell
        (do
          (gain player 3 :cash)
          (alter (get current-state :trash) conj card))
        (do
          (alter (get-in @(get current-state :players) [player :table]) conj card)
          (when-not (nil? card-effect) (eval (conj (drop 1 card-effect) player (first card-effect)))))))))

(join-game "apo")
(join-game "wuj")
(join-game "zoll")

;(start-game)

(deal :age 1)

current-state

;(play :card "Altar" :player 1)
