(ns movement.server
  (:require [movement.handler :refer [app]]
            [ring.adapter.jetty :refer [run-jetty]]
            [org.httpkit.server :refer [run-server]])
  (:gen-class))

 (defn -main [& args]
   (let [port (Integer/parseInt (or (System/getenv "PORT") "8000"))]
     (run-server app {:port port})
     (println "Started server on localhost:" port)
     #_(run-jetty app {:port port :join? false})))
