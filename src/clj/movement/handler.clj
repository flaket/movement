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
            [clojure.string :as str]
            [movement.movements :refer [strength-template morning-ritual-template
                                        mobility-template locomotion-template
                                        bas-template sass-template
                                        leg-strength-template movnat-template
                                        maya-template]]))

(def uri "datomic:dev://localhost:4334/movement4")
(def conn (d/connect uri))
(def db (d/db conn))

(defn generate-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(defn movements []
  (let [db (d/db conn)
        movements (d/q '[:find ?n
                         :where
                         [_ :movement/name ?n]]
                       db)]
    (generate-response movements)))

(defn movement [name]
  (let [db (d/db conn)
        movement (d/q '[:find ?e
                        :in $ ?name
                        :where
                        [?e :movement/name ?name]]
                      db
                      name)]
    (generate-response movement)))

(defn get-all-template-titles []
  (let [db (d/db conn)
        templates (d/q '[:find [?t ...]
                         :where
                         [_ :template/title ?t]]
                       db)]
    (generate-response templates)))

(defn get-template [title]
  (let [entity (d/pull db '[*] [:template/title title])
        part-entities (vec (flatten (map vals (:template/part entity))))
        parts (map #(d/pull db '[*] %) part-entities)]
    {:title       (:template/title entity)
     :description (:template/description entity)
     :parts       (vec (for [p parts]
                         {:title    (:part/name p)
                          :category (let [categories (:part/category p)
                                          x (for [c categories]
                                              (d/pull db '[:category/name] (:db/id c)))]
                                      (vec (map :category/name x)))
                          :movements (d/q '[:find [(sample 2 ?name)]
                                            :where [_ :movement/name ?name]]
                                          db)}))}))

(defroutes routes
           (GET "/" [] (render-file "templates/index.html" {:dev (env :dev?)}))
           (GET "/raw" [] (render-file "templates/indexraw.html" {:dev (env :dev?)}))
           (GET "/movements" [] (movements))
           (GET "/movement/:name" [name] (movement name))
           (GET "/templates" [] (get-all-template-titles))
           (GET "/template/:title" [title] (get-template (str/replace title "-" " ")))

           (GET "/Strength" [] (generate-response strength-template))
           (GET "/Bent-Arm-Strength" [] (generate-response bas-template))

           (GET "/sass" [] (generate-response sass-template))
           (GET "/ritual" [] (generate-response morning-ritual-template))
           (GET "/mobility" [] (generate-response mobility-template))
           (GET "/locomotion" [] (generate-response locomotion-template))
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
