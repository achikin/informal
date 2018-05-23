(ns informal.views
  (:require [re-frame.core :as re-frame]
            [informal.subs :as subs]
            ))

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div "Hello from " @name]))
