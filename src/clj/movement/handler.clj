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

(def uri "datomic:dev://localhost:4334/movement")
(def conn (d/connect uri))
(def db (d/db conn))

(defn generate-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body    (pr-str data)})

(defn all-movement-names []
  "Returns the names of all movements in the database."
  (let [movements (d/q '[:find ?n
                         :where
                         [_ :movement/name ?n]]
                       db)]
    (generate-response movements)))

(defn movement [name]
  "Returns the whole entity of a named movement."
  (let [movement (d/pull db '[*] [:movement/name name])]
    (generate-response movement)))

(defn get-movements [n categories]
  "Get n random movement entities drawn from param list of categories."
  (let [movements (for [c categories]
                    (d/q '[:find (pull ?m [*]) :in $ ?cat
                           :where [?c :category/name ?cat] [?m :movement/category ?c] ]
                         db c))
        m (->> movements flatten set shuffle (take n))]
    m))

(defn all-template-titles []
  (let [db (d/db conn)
        templates (d/q '[:find [?t ...]
                         :where
                         [_ :template/title ?t]]
                       db)]
    (generate-response templates)))

(defn get-template [title]
  (let [db (d/db conn)
        entity (d/pull db '[*] [:template/title title])
        part-entities (vec (flatten (map vals (:template/part entity))))
        parts (map #(d/pull db '[*] %) part-entities)]

    (generate-response {:title       (:template/title entity)
                        :description (:template/description entity)
                        :parts       (vec (for [p parts]
                                            {:title    (:part/name p)
                                             :category (let [categories (:part/category p)
                                                             x (for [c categories]
                                                                 (d/pull db '[:category/name] (:db/id c)))]
                                                         (vec (map :category/name x)))}))})))

(defroutes routes
           (GET "/" [] (render-file "templates/index.html" {:dev (env :dev?)}))
           (GET "/raw" [] (render-file "templates/indexraw.html" {:dev (env :dev?)}))
           (GET "/movements" [] (all-movement-names))
           (GET "/movement/:name" [name] (movement name))
           (GET "/templates" [] (all-template-titles))
           (GET "/template/:title" [title] (get-template (str/replace title "-" " ")))
           (resources "/")
           (not-found "Not Found"))

(def app
  (let [handler (wrap-frame-options
                  (wrap-defaults routes site-defaults)
                  {:allow-from (or "http://movementsession.com"
                                   "http://www.movementsession.com")})]
    (if (env :dev?) (wrap-reload (wrap-exceptions handler)) handler)))
