(ns movement.server
  (:require [movement.handler :refer [app]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

 (defn -main [& args]
   (let [port (Integer/parseInt (or (System/getenv "PORT") "8000"))]
     (println "Started server on localhost:" port)
     (run-jetty app {:port port :join? false})))
