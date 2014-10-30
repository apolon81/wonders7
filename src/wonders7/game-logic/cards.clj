(in-ns 'wonders7)

(def cards {

  "Lumber Yard"
  {:provides :wood, :color :brown}

  "Ore Vein"
  {:provides :ore, :color :brown}

  "Clay Pool"
  {:provides :clay, :color :brown}

  "Stone Pit"
  {:provides :stone, :color :brown}

  "Timber Yard"
  {:provides [:stone :wood], :cost {:cash 1}, :color :brown}

  "Clay Pit"
  {:provides [:clay :ore], :cost {:cash 1}, :color :brown}

  "Loom"
  {:provides :cloth, :color :grey}

  "Glassworks"
  {:provides :glass, :color :grey}

  "Press"
  {:provides :papyrus, :color :grey}

  "East Trading Post"
  {:provides :trade-east, :color :yellow}

  "West Trading Post"
  {:provides :trade-west, :color :yellow}

  "Marketplace"
  {:provides :trade-luxury, :color :yellow}

  "Altar"
  {:provides {:point 2}, :color :blue}

  "Theater"
  {:provides {:point 2}, :color :blue}

  "Baths"
  {:provides {:point 3}, :cost {:stone 1}, :color :blue}

  "Stockade"
  {:provides :shield, :cost {:wood 1}, :color :red}

  "Barracks"
  {:provides :shield, :cost {:ore 1}, :color :red}

  "Guard Tower"
  {:provides :shield, :cost {:clay 1}, :color :red}

  "Apothecary"
  {:provides :caliper, :cost {:cloth 1}, :color :green}

  "Workshop"
  {:provides :cog, :cost {:glass 1}, :color :green}

  "Scriptorium"
  {:provides :tablet, :cost {:papyrus 1}, :color :green}

; second age

  "Sawmill"
  {:provides {:wood 2}, :cost {:cash 1}, :color :brown}

  "Foundry"
  {:provides {:ore 2}, :cost {:cash 1}, :color :brown}

  "Brickyard"
  {:provides {:clay 2}, :cost {:cash 1}, :color :brown}

  "Quarry"
  {:provides {:stone 2}, :cost {:cash 1}, :color :brown}

;  "Loom"
;  {:provides :cloth, :color :grey}

;  "Glassworks"
;  {:provides :glass, :color :grey}

;  "Press"
;  {:provides :papyrus, :color :grey}

  "Caravansery"
  {:provides [:wood :stone :ore :clay], :cost {:wood 2}, :gratis-condition "Marketplace", :color :yellow}

  "Forum"
  {:provides [:glass :cloth :papyrus], :cost {:clay 2}, :gratis-condition ["East Trading Post" "West Trading Post"], :color :yellow}

  "Vineyard"
  {:effect '(gain 5 #_(count-cards :brown :self :east :west) :cash), :color :yellow}

  "Temple"
  {:provides {:point 3}, :cost {:wood 1, :clay 1, :glass 1}, :gratis-condition "Altar", :color :blue}

  "Courthouse"
  {:provides {:point 4}, :cost {:clay 2, :cloth 1}, :gratis-condition "Scriptorium", :color :blue}

  "Statue"
  {:provides {:point 4}, :cost {:ore 2, :wood 1}, :gratis-condition "Theater", :color :blue}

  "Aqueduct"
  {:provides {:point 5}, :cost {:stone 3}, :gratis-condition "Altar", :color :blue}

  "Stables"
  {:provides {:shield 2}, :cost {:clay 1, :wood 1, :ore 1}, :gratis-condition "Apothecary", :color :red}

  "Archery Range"
  {:provides {:shield 2}, :cost {:wood 2, :ore 1}, :gratis-condition "Workshop", :color :red}

  "Walls"
  {:provides {:shield 2}, :cost {:stone 3}, :color :red}

  "Library"
  {:provides :tablet, :cost {:stone 2, :cloth 1}, :gratis-condition "Scriptorium", :color :green}

  "Laboratory"
  {:provides :cog, :cost {:clay 2, :papyrus 1}, :gratis-condition "Workshop", :color :green}

  "Dispensary"
  {:provides :caliper, :cost {:ore 2, :glass 1}, :gratis-condition "Apothecary", :color :green}

  "School"
  {:provides :tablet, :cost {:wood 1, :papyrus 1}, :color :green}

; third-age

})
