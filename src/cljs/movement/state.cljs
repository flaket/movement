(ns movement.state
  (:require [reagent.session :as session]
            [cljs.core.async :as async :refer [chan close!]]
            [cljs.reader :refer [read-string]]
            [goog.net.XhrIo :as xhr]
            )
  (:require-macros
    [cljs.core.async.macros :refer [go]]))

(defonce templates (atom []))
(defonce movement-session (atom {}))

(defn GET
  "Issue a get request to a url through a core.async channel.
  Returns a channel that the result can be read from.
  goog.net.XhrIo.send(url, opt_callback, opt_method, opt_content, opt_headers, opt_timeoutInterval)
  https://developers.google.com/closure/library/docs/xhrio"
  [url]
  (let [ch (chan 1)]
    (xhr/send url
              (fn [event]
                (let [res (-> event .-target .getResponseText)]
                  (go (>! ch res)
                      (close! ch)))))
    ch))

(defn handler-fn
  "Wrapper function to force component handler functions to return nil.
  This is a React requirement."
  [func]
  (fn [] func nil))

(defn log-session []
  (let [log (session/get :logged-sessions)
        timestamp (.getTime (js/Date.))
        s (swap! movement-session assoc :timestamp)
        new-sessions (conj log movement-session)]
    (session/put! :logged-sessions new-sessions)))


(go (reset! templates (read-string (<! (GET "templates")))))

(defn update! [kw id title] (swap! movement-session assoc-in [kw id :title] title))

(defn delete! [kw id] (swap! movement-session update-in [kw] dissoc id))

(defn refresh! [id category] (update! :movements id (first (take 1 (shuffle category)))))