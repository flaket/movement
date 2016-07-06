(ns movement.handler
  (:require [compojure.core :refer [GET HEAD defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.anti-forgery :refer [*anti-forgery-token*]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [selmer.parser :refer [render-file]]
            [prone.middleware :refer [wrap-exceptions]]
            [environ.core :refer [env]]))

(selmer.parser/set-resource-path! (clojure.java.io/resource "templates"))

(defroutes routes
           (HEAD "/" [] "")
           (GET "/" [] (render-file "app.html" {:dev (env :dev?) :csrf-token *anti-forgery-token*}))
           (resources "/")
           (not-found "Not Found"))

(def app
  (let [handler (-> routes
                    (wrap-edn-params)
                    (wrap-params)
                    (wrap-session)
                    (wrap-defaults site-defaults))]
    (if (env :dev?)
      (wrap-reload (wrap-exceptions handler))
      handler)))