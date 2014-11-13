(ns wonders7.game.state
  (:require [wonders7.game.cards]))

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
   :trash []
   :picks {}
   :age 1
   :free-seats 7
   :in-progress false})

; create refs on the inner keys in the game state
(def current-state
  (into {} (for [[k v] initial-state] [k (ref v)])))

; utility function to check if a given player sits at the table
(defn player-exists [player-id]
  (some (conj #{} player-id) (for [[k v] @(:players current-state)] (:id v))))

; this resolves all the references to provide a snapshot of the game state
(defn state-view []
  (into {} [[:players (into (sorted-map)
                        (for [[k v] @(:players current-state)]
                             [k (into {} [[:name (:name v)]
                                          [:id (:id v)]
                                          [:hand (deref (:hand v))]
                                          [:table (deref (:table v))]
                                          [:cash (deref (:cash v))]
                                          [:war-score (deref (:war-score v))]])]))]
            [:trash (deref (:trash current-state))]
            [:picks (deref (:picks current-state))]
            [:age (deref (:age current-state))]
            [:free-seats (deref (:free-seats current-state))]
            [:in-progress (deref (:in-progress current-state))]]))

; adds an entry to the players map located in the current game state
(defn join-game [& {:keys [player-name player-id]}]
  (dosync
    (when-not @(:in-progress current-state)
      (let [free-seats (:free-seats current-state)]
        (when (> @free-seats 0)
          (alter (:players current-state) into [[(- 8 @free-seats)
                                                 {:name player-name
                                                  :id player-id
                                                  :hand (ref {})
                                                  :table (ref #{})
                                                  :cash (ref 3)
                                                  :war-score (ref {})}]])
          (alter free-seats dec))))))

; hepler for inc/dec cash or possibly sth else, must be called in a transaction
(defn gain [player quantity subject]
  (alter (get-in @(:players current-state) [player subject]) + quantity))

; api handler for populating hands
(defn deal [& {:keys [age] :or {age 1}}]
  (loop [card-pool (shuffle (get-deck age)) player-pool (keys @(:players current-state))]
    (if
      (empty? player-pool) nil
      (do
        (dosync
          (alter
            (get-in @(:players current-state) [(first player-pool) :hand])
            (fn [x] (apply merge-with + (map (fn [y] {y 1}) (take 7 card-pool))))))
        (recur (drop 7 card-pool) (drop 1 player-pool))))))

; this takes the card out of a player's hand
(defn hand-pull [card player]
  (alter
    (get-in @(:players current-state) [player :hand])
    (fn [x] (into {} (filter #(> (second %) 0) (update-in x [card] dec))))))

; remove from hand, place in the trash
(defn trash [card player]
  (do
    (hand-pull card player)
    (alter (:trash current-state) conj card)))

; remove from hand, place on the table
(defn table-put [card player]
  (do
    (hand-pull card player)
    (alter (get-in @(:players current-state) [player :table]) conj card)))

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
        (do-effects (get-in wonders7.game.cards/cards [card :effect]) player)))))

; helper for passing cards round the table
(defn pass-along []
  (dosync
    (let [hand-mapping (into {}
                         (for [player-no (keys @(:players current-state))]
                           [(inc (mod (+ (dec player-no) (if (odd? @(:age current-state)) 1 -1)) (count @(:players current-state))))
                            @(get-in @(:players current-state) [player-no :hand])]))]
      (doseq [player-no (keys @(:players current-state))]
        (alter (get-in @(:players current-state) [player-no :hand]) (fn [x] (get hand-mapping player-no)))))))

; play the picked cards, pass hands round the table
(defn process-picks []
  (dosync
    (doseq [[k v] @(:picks current-state)]
      (play :card (:card v) :player k :sell (:sell v) :trades (:trades v)))
    (alter (:picks current-state) (fn [x] {}))
    (pass-along)))

; mark the game as started, deal the first age
(defn start-game []
  (dosync
    (when (not @(:in-progress current-state))
      (alter (:in-progress current-state) (fn [x] true))
      (add-watch (:picks current-state)
                 :picks-watch
                 (fn [k r old-state new-state]
                   (when (= (count new-state) (count @(get current-state :players)))
                     (process-picks))))
      (deal :age 1))))

; reset the game state
(defn reset-game []
  (dosync
    (alter (:in-progress current-state) (fn [x] false))
    (alter (:free-seats current-state) (fn [x] 7))
    (alter (:age current-state) (fn [x] 1))
    (alter (:trash current-state) (fn [x] []))
    (alter (:players current-state) (fn [x] {}))
    (remove-watch (:picks current-state) :picks-watch)
    (alter (:picks current-state) (fn [x] {}))))

; save a decission for further processing
(defn pick [& {:keys [player card sell trades] :or {sell false}}]
  (dosync
    (alter (:picks current-state) into [[player {:card card, :sell sell, :trades trades}]])))

(defn test-turn []
  (do
    (pick :card (first (shuffle (keys @(get-in @(:players current-state) [1 :hand])))) :player 1)
    (pick :card (first (shuffle (keys @(get-in @(:players current-state) [2 :hand])))) :player 2 :sell true)
    (pick :card (first (shuffle (keys @(get-in @(:players current-state) [3 :hand])))) :player 3)))
