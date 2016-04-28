(defproject movement "0.1.0-SNAPSHOT"
  :description "Generating your next movement session."
  :url "http://www.movementsession.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "src/cljs"]

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.7.228"]

                 [org.clojure/core.async "0.2.374"]
                 [com.datomic/datomic-pro "0.9.5201" :exclusions [joda-time]]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [clj-time "0.11.0"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 [org.clojure/data.codec "0.1.0"]

                 [ring "1.4.0" :exclusions [org.clojure/java.classpath]]
                 [ring-server "0.4.0" :exclusions [org.clojure/java.classpath]]
                 [ring/ring-defaults "0.2.0"]
                 [ring/ring-headers "0.2.0"]
                 [ring/ring-anti-forgery "1.0.0"]
                 [fogus/ring-edn "0.3.0"]
                 [compojure "1.5.0"]

                 [prone "1.0.2"]
                 [com.taoensso/timbre "4.3.1"]

                 [clj-aws-s3 "0.3.10" :exclusions [joda-time]]
                 [io.nervous/hildebrand "0.4.3"]
                 [com.taoensso/faraday "1.9.0-alpha3"]

                 [hiccup "1.0.5"]
                 [reagent "0.6.0-alpha"]
                 [reagent-forms "0.5.21"]

                 [reagent-utils "0.1.7"]
                 [secretary "1.2.3"]
                 [cljs-ajax "0.5.4"]
                 [prismatic/dommy "1.1.0"]

                 [selmer "1.0.2"]
                 [environ "1.0.2"]
                 [com.draines/postal "1.11.4"]

                 [buddy/buddy-auth "0.9.0"]
                 [buddy/buddy-hashers "0.11.0"]
                 [buddy/buddy-sign "0.9.0"]

                 [slingshot "0.12.2"]]

  :plugins [[lein-clean-m2 "0.1.2"]
            [lein-cljsbuild "1.1.3"]
            [lein-environ "1.0.2"]
            [lein-ring "0.9.7"]
            [lein-asset-minifier "0.2.8"]]

  :ring {:handler movement.handler/app
         :uberwar-name "movement.war"}

  :min-lein-version "2.5.0"

  :uberjar-name "movement.jar"

  :main movement.server

  :clean-targets ^{:protect false} ["resources/public/js" "resources/public/css/garden"]

  :repositories {"my.datomic.com" {:url      "https://my.datomic.com/repo"
                                   :username ~(System/getenv "DATOMIC_EMAIL")
                                   :password ~(System/getenv "DATOMIC_KEY")
                                   ;:creds :gpg
                                   }}
  :minify-assets {:assets {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :compiler     {:output-to     "resources/public/js/app.js"
                                            :output-dir    "resources/public/js/out"
                                            :asset-path    "js/out"
                                            :optimizations :none
                                            :pretty-print true}}
                       :prod {:source-paths ["src/cljs"]
                              :compiler     {:output-to     "resources/public/js/app.js"
                                             :output-dir    "resources/public/js/prod-out"
                                             :asset-path    "js/out"
                                             :optimizations :advanced}}}}

  :profiles {:dev {:repl-options {:init-ns movement.repl
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :dependencies [[leiningen "2.6.1" :exclusions [org.codehaus.plexus/plexus-utils]]
                                  [weasel "0.7.0"]
                                  [com.cemerick/piggieback "0.2.1"]]

                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.5.0-6"]]

                   ;:injections [(require 'pjstadig.humane-test-output) (pjstadig.humane-test-output/activate!)]

                   :figwheel {:http-server-root "public"
                              :server-port 3449
                              :css-dirs ["resources/public/css"]
                              :ring-handler movement.handler/app}

                   :env {:dev? true}

                   :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs"]
                                              :compiler {
                                                         :main "movement.dev"}}}}}

             :uberjar {:hooks [leiningen.cljsbuild minify-assets.plugin/hooks]
                       :env {:production true}
                       :aot :all
                       :omit-source true
                       :cljsbuild {:jar true
                                   :builds {:app
                                             {:source-paths ["env/prod/cljs"]
                                              :compiler
                                              {:optimizations :advanced
                                               :pretty-print false}}}}}})