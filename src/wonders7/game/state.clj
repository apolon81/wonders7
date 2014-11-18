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

(def initial-state
  {:players (sorted-map)
   :trash []
   :picks {}
   :age 1
   :free-seats 7
   :in-progress false})

(def current-state
  (into {} (for [[k v] initial-state] [k (ref v)])))
