(defproject wonders7 "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.2.1"]
                 [ring/ring-defaults "0.1.2"]
                 [aleph "0.4.0-alpha9"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/data.json "0.2.5"]]
  :plugins [[lein-ring "0.8.13"]]
  :ring {:handler wonders7.core.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
