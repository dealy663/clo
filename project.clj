(defproject clo "0.1.0-SNAPSHOT"

  :description "Ask Clo - project for managing questions and links to their answeres"
  :url "http://www.grandprixsw.com/clo"

  :dependencies [[luminus-log4j "0.1.3"]
                 [cljs-ajax "0.5.8"]
                 [secretary "1.2.3"]
                 [reagent-utils "0.1.9"]
                 [reagent "0.6.0-rc"]
                 [org.clojure/clojurescript "1.9.92" :scope "provided"]
                 [org.clojure/clojure "1.8.0"]
                 [selmer "1.0.6"]
                 [markdown-clj "0.9.89"]
                 [ring-middleware-format "0.7.0"]
                 [metosin/ring-http-response "0.7.0"]
                 [bouncer "1.0.0"]
                 [org.webjars/bootstrap "4.0.0-alpha.2"]
                 [org.webjars/font-awesome "4.6.3"]
                 [org.webjars.bower/tether "1.3.2"]
                 [org.clojure/tools.logging "0.3.1"]
                 [compojure "1.5.1"]
                 [ring-webjars "0.1.1"]
                 [ring/ring-defaults "0.2.1"]
                 [mount "0.1.10"]
                 [cprop "0.1.8"]
                 [org.clojure/tools.cli "0.3.5"]
                 [luminus-nrepl "0.1.4"]
                 [luminus-migrations "0.2.2"]
                 [conman "0.5.8"]
                 [edu.stanford.nlp/stanford-corenlp "3.6.0"]
                 [edu.stanford.nlp/stanford-corenlp "3.6.0" :classifier "models"]
                 [org.postgresql/postgresql "9.4-1206-jdbc4"]
                 [org.webjars/webjars-locator-jboss-vfs "0.1.0"]
                 [luminus-immutant "0.2.0"]
                 [buddy "1.3.0"]
                 [metosin/compojure-api "1.1.10"]
                 [http.async.client "1.2.0"]]

  :min-lein-version "2.0.0"

  :jvm-opts ["-server" "-Dconf=.lein-env"]
  :source-paths ["src/clj" "src/cljc"]
  :resource-paths ["resources" "target/cljsbuild"]
  :target-path "target/%s/"
  :main clo.core
  :migratus {:store :database :db ~(get (System/getenv) "DATABASE_URL")}

  :plugins [[lein-cprop "1.0.1"]
            [migratus-lein "0.3.7"]
            [lein-cljsbuild "1.1.3"]
            [lein-immutant "2.1.0"]]
  :clean-targets ^{:protect false}
  [:target-path [:cljsbuild :builds :app :compiler :output-dir] [:cljsbuild :builds :app :compiler :output-to]]

  :cljsbuild
  {:builds
   {:app
    {:source-paths ["src/cljc" "src/cljs" "env/dev/cljs"]
     :figwheel true
     :compiler
     {:main "clo.app"
      :asset-path "/js/out"
      :output-to "target/cljsbuild/public/js/app.js"
      :output-dir "target/cljsbuild/public/js/out"
      :optimizations :none
      :source-map true
      :pretty-print true}}
    :test
    {:source-paths ["src/cljc" "src/cljs" "test/cljs"]
     :compiler
     {:output-to "target/test.js"
      :main "clo.doo-runner"
      :optimizations :whitespace
      :pretty-print true}}
    :min
    {:source-paths ["src/cljc" "src/cljs" "env/prod/cljs"]
     :compiler
     {:output-to "target/cljsbuild/public/js/app.js"
      :output-dir "target/uberjar"
      :externs ["react/externs/react.js"
                "resources/externs.js"]
      :optimizations :advanced
      :pretty-print false
      :closure-warnings
      {:externs-validation :off :non-standard-jsdoc :off}}}}}

  :figwheel
  {:http-server-root "public"
   :nrepl-port 7002
   :css-dirs ["resources/public/css"]
   :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}


  :profiles
  {:uberjar {:omit-source true

             :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
             :aot :all
             :uberjar-name "clo.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/test :profiles/test]

   :project/dev  {:dependencies [[prone "1.1.1"]
                                 [ring/ring-mock "0.3.0"]
                                 [ring/ring-devel "1.5.0"]
                                 [pjstadig/humane-test-output "0.8.0"]
                                 [doo "0.1.6"]
                                 [binaryage/devtools "0.7.2"]
                                 [figwheel-sidecar "0.5.4-4"]
                                 [com.cemerick/piggieback "0.2.2-SNAPSHOT"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.14.0"]
                                 [lein-doo "0.1.6"]
                                 [lein-figwheel "0.5.4-4"]
                                 [org.clojure/clojurescript "1.9.92"]]

                  :doo {:build "test"}
                  :source-paths ["env/dev/clj" "test/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:resource-paths ["env/dev/resources" "env/test/resources"]}
   :profiles/dev {}
   :profiles/test {}})
