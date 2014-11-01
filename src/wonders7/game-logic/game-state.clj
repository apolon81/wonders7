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
  {:players (sorted-map)
   :trash #{}
   :picks {}
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

; api handler for populating hands
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

; this takes the card out of a player's hand
(defn hand-pull [card player]
  (alter
    (get-in @(get current-state :players) [player :hand])
    (fn [x] (into {} (filter #(> (second %) 0) (update-in x [card] dec))))))

; remove from hand, place in the trash
(defn trash [card player]
  (do
    (hand-pull card player)
    (alter (get current-state :trash) conj card)))

; remove from hand, place on the table
(defn table-put [card player]
  (do
    (hand-pull card player)
    (alter (get-in @(get current-state :players) [player :table]) conj card)))

(defn can-afford [player card trades]
  true)

(defn pay-costs [player card trades]
  true)

; inject the player as the first argument, then call the effect
(defn do-effects [card-effect player]
  (when-not (nil? card-effect)
    (eval (conj (drop 1 card-effect) player (first card-effect)))))

; api handler for playing a card
(defn play [& {:keys [player card sell trades] :or {sell false}}]
  (dosync
    (if (or sell (not (can-afford card player trades)))
      (do
        (gain player 3 :cash)
        (trash card player))
      (do
        (pay-costs card player trades)
        (table-put card player)
        (do-effects (get-in cards [card :effect]) player)))))

; helper for passing cards round the table
(defn pass-along []
  (dosync
    (let [hand-mapping (into {}
                         (for [player-no (keys @(get current-state :players))]
                           [(inc (mod (+ (dec player-no) (if (odd? @(get current-state :age)) 1 -1)) (count @(get current-state :players))))
                            @(get-in @(get current-state :players) [player-no :hand])]))]
      (doseq [player-no (keys @(get current-state :players))]
        (alter (get-in @(get current-state :players) [player-no :hand]) (fn [x] (get hand-mapping player-no)))))))

; play the picked cards, pass hands round the table
(defn process-picks []
  (dosync
    (doseq [[k v] @(get current-state :picks)]
      (play :card (get v :card) :player k :sell (get v :sell) :trades (get v :trades)))
    (alter (get current-state :picks) (fn [x] {}))
    (pass-along)))

; mark the game as started
(defn start-game []
  (dosync
    (alter (get current-state :in-progress) (fn [x] true)))
    (remove-watch (get current-state :picks) :picks-watch)
    (add-watch (get current-state :picks)
               :picks-watch
               (fn [k r old-state new-state]
                 (when (= (count new-state) (count @(get current-state :players)))
                   (process-picks)))))

; reset the game state
(defn reset-game []
  (dosync
    (alter (get current-state :in-progress) (fn [x] false))
    (alter (get current-state :free-seats) (fn [x] 7))
    (alter (get current-state :age) (fn [x] 1))
    (alter (get current-state :trash) (fn [x] #{}))
    (alter (get current-state :players) (fn [x] {}))
    (alter (get current-state :picks) (fn [x] {}))))

; save a decission for further processing
(defn pick [& {:keys [player card sell trades] :or {sell false}}]
  (dosync
    (alter (get current-state :picks) into [[player {:card card, :sell sell, :trades trades}]])))

(join-game "apo")
(join-game "wuj")
(join-game "zoll")

(start-game)

(deal :age 1)

(do
  (pick :card (first (shuffle (keys @(get-in @(get current-state :players) [1 :hand])))) :player 1)
  (pick :card (first (shuffle (keys @(get-in @(get current-state :players) [2 :hand])))) :player 2)
  (pick :card (first (shuffle (keys @(get-in @(get current-state :players) [3 :hand])))) :player 3))

current-state

;(reset-game)
