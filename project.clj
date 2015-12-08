(defproject movement "0.1.0-SNAPSHOT"
  :description "Generating your next movement session."
  :url "http://www.movementsession.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "src/cljs"]

  :dependencies [[org.clojure/clojure "1.7.0" :exclusions [time]]
                 [org.clojure/clojurescript "0.0-3196" :scope "provided"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [com.datomic/datomic-pro "0.9.5201" :exclusions [joda-time]]
                 [clj-time "0.11.0"]
                 [ring "1.3.2"]
                 [ring-server "0.4.0"]
                 [ring/ring-defaults "0.1.4"]
                 [ring/ring-headers "0.1.2"]
                 [ring/ring-anti-forgery "1.0.0"]
                 [fogus/ring-edn "0.3.0"]
                 [cljsjs/react "0.13.1-0"]
                 [reagent "0.5.0"]
                 [reagent-forms "0.5.5"]
                 [reagent-utils "0.1.4"]
                 [prone "0.8.1"]
                 [compojure "1.3.4"]
                 [selmer "0.8.2"]
                 [environ "1.0.0"]
                 [secretary "1.2.2"]
                 [buddy/buddy-auth "0.6.1"]
                 [buddy/buddy-hashers "0.7.0"]
                 [buddy/buddy-sign "0.7.1"]
                 [cljs-ajax "0.5.0"]
                 [prismatic/dommy "1.1.0"]
                 [hiccup "1.0.5"]
                 [com.draines/postal "1.11.3"]
                 [com.taoensso/timbre "4.1.4"]
                 [slingshot "0.12.2"]]

  :plugins [[lein-cljsbuild "1.0.4"]
            [lein-environ "1.0.0"]
            [lein-ring "0.9.1"]
            [lein-asset-minifier "0.2.2"]]
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

  :minify-assets
  {:assets
    {"resources/public/css/site.min.css" "resources/public/css/site.css"}}

  :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                             :compiler     {:output-to     "resources/public/js/app.js"
                                            :output-dir    "resources/public/js/out"
                                            :asset-path    "js/out"
                                            :optimizations :none
                                            :pretty-print true}}}}

  :profiles {:dev {:repl-options {:init-ns movement.repl
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :dependencies [[ring-mock "0.1.5"]
                                  [ring/ring-devel "1.3.2"]
                                  [leiningen "2.5.1"]
                                  [figwheel "0.2.5"]
                                  [weasel "0.6.0"]
                                  [com.cemerick/piggieback "0.1.6-SNAPSHOT"]
                                  [pjstadig/humane-test-output "0.7.0"]]

                   :source-paths ["env/dev/clj"]
                   :plugins [[lein-figwheel "0.2.3-SNAPSHOT"]]

                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]

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
