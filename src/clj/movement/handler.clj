(ns movement.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.x-headers :refer [wrap-frame-options]]
            [selmer.parser :refer [render-file]]
            [prone.middleware :refer [wrap-exceptions]]
            [environ.core :refer [env]]
            [datomic.api :as d]
            [clojure.edn :as edn]
            [movement.movements :refer [strength-template morning-ritual-template
                                        mobility-template locomotion-template
                                        bas-template sass-template
                                        leg-strength-template movnat-template
                                        maya-template]]))

#_(def uri "datomic:pro://localhost:8000/move")
#_(def conn (d/connect uri))

(defn generate-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(defroutes routes
  (GET "/" [] (render-file "templates/index.html" {:dev (env :dev?)}))
           (GET "/strength" [] (generate-response strength-template))
           (GET "/ritual" [] (generate-response morning-ritual-template))
           (GET "/mobility" [] (generate-response mobility-template))
           (GET "/locomotion" [] (generate-response locomotion-template))
           (GET "/bas" [] (generate-response bas-template))
           (GET "/sass" [] (generate-response sass-template))
           (GET "/leg" [] (generate-response leg-strength-template))
           (GET "/movnat" [] (generate-response movnat-template))
           (GET "/maya" [] (generate-response maya-template))
           (resources "/")
  (not-found "Not Found"))

(def app
  (let [handler (wrap-frame-options
                  (wrap-defaults routes site-defaults)
                  {:allow-from (or "http://movementsession.com"
                                   "http://www.movementsession.com")})]
    (if (env :dev?) (wrap-reload (wrap-exceptions handler)) handler)))
