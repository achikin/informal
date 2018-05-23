(ns example.views
  (:require [re-frame.core :as re-frame]
            [example.subs :as subs]
            ))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div "Hello from " @name]))
